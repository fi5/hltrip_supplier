package com.huoli.trip.supplier.web.yaochufa.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.huoli.trip.common.constant.BizTagConst;
import com.huoli.trip.common.constant.Constants;
import com.huoli.trip.common.constant.ProductType;
import com.huoli.trip.common.entity.*;
import com.huoli.trip.common.entity.mpo.scenicSpotTicket.*;
import com.huoli.trip.common.util.*;
import com.huoli.trip.data.api.DataService;
import com.huoli.trip.supplier.api.DynamicProductItemService;
import com.huoli.trip.supplier.api.YcfSyncService;
import com.huoli.trip.supplier.feign.client.yaochufa.client.IYaoChuFaClient;
import com.huoli.trip.supplier.self.yaochufa.constant.YcfConfigConstants;
import com.huoli.trip.supplier.self.yaochufa.constant.YcfConstants;
import com.huoli.trip.supplier.self.yaochufa.vo.*;
import com.huoli.trip.supplier.self.yaochufa.vo.basevo.YcfBaseRequest;
import com.huoli.trip.supplier.self.yaochufa.vo.basevo.YcfBaseResult;
import com.huoli.trip.supplier.web.dao.*;
import com.huoli.trip.supplier.web.service.CommonService;
import com.huoli.trip.supplier.web.yaochufa.convert.YcfConverter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.huoli.trip.common.constant.Constants.SUPPLIER_CODE_YCF;


/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2020/6/28<br>
 */
@Slf4j
@Service(timeout = 10000,group = "hltrip")
public class YcfSyncServiceImpl implements YcfSyncService {

    @Autowired
    private ProductDao productDao;

    @Autowired
    private ProductItemDao productItemDao;

    @Autowired
    private PriceDao priceDao;

    @Autowired
    private IYaoChuFaClient yaoChuFaClient;

    @Autowired
    private DynamicProductItemService dynamicProductItemService;

    @Autowired
    private CommonService commonService;

    @Autowired
    private ScenicSpotProductDao scenicSpotProductDao;

    @Autowired
    private ScenicSpotMappingDao scenicSpotMappingDao;

    @Autowired
    private ScenicSpotDao scenicSpotDao;

    @Autowired
    private ScenicSpotProductPriceDao scenicSpotProductPriceDao;

    @Autowired
    private ScenicSpotRuleDao scenicSpotRuleDao;

    @Override
    public void syncProduct(List<YcfProduct> ycfProducts){
        if(ListUtils.isEmpty(ycfProducts)){
            log.error("要出发推送的产品列表为空");
            return;
        }
        ycfProducts.forEach(ycfProduct -> {
            if(!filterProduct(ycfProduct)){
                return;
            }
            ProductPO productPO = YcfConverter.convertToProductPO(ycfProduct);
            List<ProductItemPO> productItemPOs = syncProductItem(ycfProduct.getProductItemIds());
            if(ListUtils.isEmpty(productItemPOs)){
                log.error("没有同步到主项目，过滤掉");
                return;
            }
            int ycfPoiType = productItemPOs.get(0).getItemType();
            // 只有餐饮项有数据的都认为是单餐
            if(ListUtils.isEmpty(ycfProduct.getTicketList()) && ListUtils.isEmpty(ycfProduct.getRoomList())){
                productPO.setProductType(convertToProductType(ycfPoiType, false, ProductType.RESTAURANT.getCode()));
            }
            // 只有门票项有数据的都认为是单票
            else if(ListUtils.isEmpty(ycfProduct.getRoomList()) && ListUtils.isEmpty(ycfProduct.getFoodList())){
                productPO.setProductType(convertToProductType(ycfPoiType, false, ProductType.SCENIC_TICKET.getCode()));
            } else {
                // 进到这里说明子项一定大于1，按优先级分类
                if(ListUtils.isNotEmpty(ycfProduct.getRoomList())){
                    productPO.setProductType(convertToProductType(ycfPoiType, true, ProductType.FREE_TRIP.getCode()));
                } else if(ListUtils.isNotEmpty(ycfProduct.getTicketList())){
                    productPO.setProductType(convertToProductType(ycfPoiType, true, ProductType.SCENIC_TICKET_PLUS.getCode()));
                } else {
                    log.error("要出发无法归类productId={}，过滤掉，套餐里没有任何具体poi", ycfProduct.getProductID());
                    return;
                }
            }
            log.info("同步poi完成。");
            if(productPO.getRoom() != null && ListUtils.isNotEmpty(productPO.getRoom().getRooms())){
                productPO.getRoom().getRooms().stream().filter(roomInfoPO -> roomInfoPO != null).forEach(roomInfoPO -> {
                    ProductItemPO productItemPO = productItemDao.selectByCode(roomInfoPO.getItemId());
                    roomInfoPO.setProductItem(productItemPO);
                });
            }
            if(productPO.getTicket() != null && ListUtils.isNotEmpty(productPO.getTicket().getTickets())){
                productPO.getTicket().getTickets().stream().filter(ticketInfoPO -> ticketInfoPO != null).forEach(ticketInfoPO -> {
                    ProductItemPO productItemPO = productItemDao.selectByCode(ticketInfoPO.getItemId());
                    ticketInfoPO.setProductItem(productItemPO);
                });
            }
            if(productPO.getFood() != null && ListUtils.isNotEmpty(productPO.getFood().getFoods())){
                productPO.getFood().getFoods().stream().filter(foodInfoPO -> foodInfoPO != null).forEach(foodInfoPO -> {
                    ProductItemPO productItemPO = productItemDao.selectByCode(foodInfoPO.getItemId());
                    foodInfoPO.setProductItem(productItemPO);
                });
            }
            ProductItemPO productItemPO = productItemDao.selectByCode(productPO.getMainItemCode());
            log.info("主项目={}", JSON.toJSONString(productItemPO));
            productPO.setMainItem(productItemPO);
            productPO.setCity(productItemPO.getCity());
            ProductPO exist = productDao.getBySupplierProductId(productPO.getSupplierProductId());
            if(exist == null){
                productPO.setCreateTime(MongoDateUtils.handleTimezoneInput(new Date()));
                // todo 暂时默认通过
//                productPO.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
                productPO.setAuditStatus(Constants.VERIFY_STATUS_PASSING);
                productPO.setSupplierStatus(Constants.SUPPLIER_STATUS_OPEN);
                BackChannelEntry backChannelEntry = commonService.getSupplierById(productPO.getSupplierId());
                if(backChannelEntry == null
                        || backChannelEntry.getStatus() == null
                        || backChannelEntry.getStatus() != 1){
                    productPO.setSupplierStatus(Constants.SUPPLIER_STATUS_CLOSED);
                }
                if(backChannelEntry != null || StringUtils.isNotBlank(backChannelEntry.getAppSource())){
                    List<String> appFroms = Arrays.asList(backChannelEntry.getAppSource().split(","));
                    productPO.setAppFrom(appFroms);
                }
            }
            productPO.setOperator(Constants.SUPPLIER_CODE_YCF);
            productPO.setOperatorName(Constants.SUPPLIER_NAME_YCF);
            productPO.setUpdateTime(MongoDateUtils.handleTimezoneInput(new Date()));
            commonService.saveBackupProduct(productPO);
            if(exist != null){
                productPO.setAuditStatus(exist.getAuditStatus());
                productPO.setSupplierStatus(exist.getSupplierStatus());
                productPO.setRecommendFlag(exist.getRecommendFlag());
                productPO.setAppFrom(exist.getAppFrom());
                // 下面对比信息会处理这个
//                productPO.setBookDescList(exist.getBookDescList());
                productPO.setDescriptions(exist.getDescriptions());
                if(exist.getCreateTime() == null){
                    productPO.setCreateTime(MongoDateUtils.handleTimezoneInput(new Date()));
                } else {
                    productPO.setCreateTime(MongoDateUtils.handleTimezoneInput(exist.getCreateTime()));
                }
                commonService.compareProduct(productPO, exist);
            }
            productDao.updateByCode(productPO);

        });
    }

    @Override
    public List<ProductItemPO> syncProductItem(List<String> productItemIds){
        log.info("开始同步poi，id list = {}", JSON.toJSONString(productItemIds));
        if(ListUtils.isEmpty(productItemIds)){
            log.error("同步poi失败，poi id集合为空");
            return null;
        }
        List<ProductItemPO> productItemPOs = Lists.newArrayList();
        YcfGetPoiRequest ycfGetPoiRequest = new YcfGetPoiRequest();
        ycfGetPoiRequest.setPoiIdList(productItemIds);
        YcfBaseRequest ycfBaseRequest = new YcfBaseRequest(ycfGetPoiRequest);
        log.info("准备请求供应商(要出发)获取poi接口，参数={}", JSON.toJSONString(ycfGetPoiRequest));
        YcfBaseResult<YcfGetPoiResponse> baseResult = yaoChuFaClient.getPoi(ycfBaseRequest);
        log.info("供应商(要出发)获取poi接口返回，结果={}", JSON.toJSONString(baseResult));
        if(baseResult.getSuccess() && baseResult.getStatusCode() == YcfConstants.RESULT_CODE_SUCCESS){
            YcfGetPoiResponse response = baseResult.getData();
            if(response == null){
                log.error("同步poi失败，供应商（要出发）没有返回data");
                return null;
            }
            List<YcfProductItem> ycfProductItems = response.getPoiList();
            if(ListUtils.isEmpty(ycfProductItems)){
                log.error("同步poi失败，供应商（要出发）没有返回poi信息");
                return null;
            }
            ycfProductItems.forEach(item -> {
                try {
                    ProductItemPO productItemPO = YcfConverter.convertToProductItemPO(item);
                    ProductItemPO exist = productItemDao.selectByCode(productItemPO.getCode());
                    // 已存在的景点不更新
                    if(exist == null){
                        productItemPO.setCreateTime(MongoDateUtils.handleTimezoneInput(new Date()));
                        productItemPO.setAuditStatus(Constants.VERIFY_STATUS_PASSING);
                        try {
                            commonService.saveBackupProductItem(productItemPO);
                        } catch (Exception e) {
                            log.error("保存{}的副本异常", productItemPO.getCode(), e);
                        }
                        if(ListUtils.isEmpty(productItemPO.getImages()) && ListUtils.isEmpty(productItemPO.getMainImages())){
                            log.info("{}没有列表图、轮播图，设置待审核", Constants.VERIFY_STATUS_WAITING);
                            productItemPO.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
                        }
                        productItemPO.setUpdateTime(MongoDateUtils.handleTimezoneInput(new Date()));
                        productItemPO.setOperator(Constants.SUPPLIER_CODE_YCF);
                        productItemPO.setOperatorName(Constants.SUPPLIER_NAME_YCF);
                        productItemDao.updateByCode(productItemPO);
                    }
                    productItemPOs.add(productItemPO);
                } catch (Exception e) {
                    log.error("poi落地失败，", e);
                }
            });
        }
        return productItemPOs;
    }

    @Override
    public YcfBaseResult<YcfGetPriceResponse> getPrice(YcfGetPriceRequest request){
        YcfGetPriceResponse ycfGetPriceResponse = new YcfGetPriceResponse();
        ycfGetPriceResponse.setPartnerProductID(request.getPartnerProductID());
        if(StringUtils.isBlank(request.getProductID())){
            ProductPO productPO = productDao.getByCode(request.getPartnerProductID());
            if(productPO != null){
                request.setProductID(productPO.getSupplierProductId());
            }
        }
        ycfGetPriceResponse.setProductID(request.getProductID());
        List<YcfPriceInfo> ycfPriceInfos = Lists.newArrayList();
        ycfGetPriceResponse.setSaleInfos(ycfPriceInfos);
        if(StringUtils.isBlank(request.getEndDate())){
            request.setEndDate(request.getStartDate());
        }
        int diffDays = DateTimeUtil.getDateDiffDays(DateTimeUtil.parseDate(request.getEndDate()), DateTimeUtil.parseDate(request.getStartDate()));
        // 要出发最多请求30天，
        int round = diffDays / 30;
        int tail = diffDays % 30;
        // 有余数需要循环商+1次
        int n = round + (tail == 0 ? 0 : 1);
        // 开始结束日期相同n会=0
        n = n == 0 ? 1 : n;
        for(int i = 0; i < n; i++){
            YcfGetPriceRequest newRequest = new YcfGetPriceRequest();
            newRequest.setTraceId(request.getTraceId());
            newRequest.setFull(request.getFull());
            newRequest.setPartnerProductID(request.getPartnerProductID());
            newRequest.setProductID(request.getProductID());
            // 从结束日期+1天开始
            newRequest.setStartDate(DateTimeUtil.formatDate(DateTimeUtil.addDay(DateTimeUtil.parseDate(request.getStartDate()), i * 30)));
            // 前后共30天
            newRequest.setEndDate(DateTimeUtil.formatDate(DateTimeUtil.addDay(DateTimeUtil.parseDate(newRequest.getStartDate()), 29)));
            if(i == n - 1){
                newRequest.setEndDate(request.getEndDate());
            }
            List<YcfPriceInfo> list;
            try {
                log.info("开始同步价格，产品编码 = {} ；日期 = {} 至 {} ", request.getPartnerProductID(), newRequest.getStartDate(), newRequest.getEndDate());
                list = syncPrice(newRequest);
                log.info("同步价格完成，产品编码 = {} ；日期 = {} 至 {} ", request.getPartnerProductID(), newRequest.getStartDate(), newRequest.getEndDate());
                // 要出发限制1分钟最多请求200次
                Thread.sleep(310);
            } catch (Exception e) {
                log.info("同步价异常，产品编码 = {} ；日期 = {} 至 {} ", request.getPartnerProductID(), newRequest.getStartDate(), newRequest.getEndDate(), e);
                continue;
            }
            if(ListUtils.isNotEmpty(list)){
                ycfPriceInfos.addAll(list);
            }
        }
        return YcfBaseResult.success(ycfGetPriceResponse);
    }

    @Override
    public void syncPrice(YcfPrice ycfPrice){
        String ycfProductId = ycfPrice.getProductID();
        List<YcfPriceInfo> ycfPriceInfos = ycfPrice.getSaleInfos();
        if(StringUtils.isBlank(ycfProductId) || ListUtils.isEmpty(ycfPriceInfos)){
            return;
        }
        String productCode = CommonUtils.genCodeBySupplier(SUPPLIER_CODE_YCF, ycfProductId);
        PricePO pricePO = priceDao.getByProductCode(productCode);
        if(pricePO == null){
            pricePO = new PricePO();
            pricePO.setProductCode(productCode);
            pricePO.setPriceInfos(Lists.newArrayList());
            pricePO.setSupplierProductId(ycfProductId);
            pricePO.setCreateTime(MongoDateUtils.handleTimezoneInput(new Date()));
        } else {
            pricePO.setCreateTime(MongoDateUtils.handleTimezoneInput(pricePO.getCreateTime()));
        }
        pricePO.setUpdateTime(MongoDateUtils.handleTimezoneInput(new Date()));
        pricePO.setOperator(SUPPLIER_CODE_YCF);
        pricePO.setOperatorName(Constants.SUPPLIER_NAME_YCF);
        List<PriceInfoPO> priceInfoPOs = pricePO.getPriceInfos();
        List<PriceInfoPO> newPriceInfos = Lists.newArrayList();
        ycfPriceInfos.forEach(ycfPriceInfo -> {
            setPrice(priceInfoPOs, newPriceInfos, ycfPriceInfo);
        });
        priceInfoPOs.addAll(newPriceInfos);
        priceInfoPOs.sort(Comparator.comparing(po -> po.getSaleDate().getTime(), Long::compareTo));
        priceDao.updateByProductCode(pricePO);
        // 更新价格要刷新item的低价产品(异步)
        dynamicProductItemService.refreshItemByProductCode(Lists.newArrayList(productCode));
    }

    @Override
    public void syncPrice(YcfPrice ycfPrice, String start, String end){
        String ycfProductId = ycfPrice.getProductID();
        List<YcfPriceInfo> ycfPriceInfos = ycfPrice.getSaleInfos();
        if(StringUtils.isBlank(ycfProductId)){
            return;
        }
        String productCode = CommonUtils.genCodeBySupplier(SUPPLIER_CODE_YCF, ycfProductId);
        PricePO pricePO = priceDao.getByProductCode(productCode);
        if(pricePO == null){
            pricePO = new PricePO();
            pricePO.setProductCode(productCode);
            pricePO.setPriceInfos(Lists.newArrayList());
            pricePO.setSupplierProductId(ycfProductId);
            pricePO.setCreateTime(MongoDateUtils.handleTimezoneInput(new Date()));
        } else {
            pricePO.setCreateTime(MongoDateUtils.handleTimezoneInput(pricePO.getCreateTime()));
        }
        pricePO.setUpdateTime(MongoDateUtils.handleTimezoneInput(new Date()));
        pricePO.setOperator(SUPPLIER_CODE_YCF);
        pricePO.setOperatorName(Constants.SUPPLIER_NAME_YCF);
        List<PriceInfoPO> priceInfoPOs = pricePO.getPriceInfos();
        List<PriceInfoPO> newPriceInfos = Lists.newArrayList();
        if(ListUtils.isEmpty(ycfPriceInfos)){
            int diff = DateTimeUtil.getDateDiffDays(DateTimeUtil.parseDate(start), DateTimeUtil.parseDate(end));
            for (int i = 0; i < diff; i++){
                Date day = DateTimeUtil.addDay(DateTimeUtil.parseDate(start), i);
                priceInfoPOs.removeIf(priceInfoPO -> priceInfoPO.getSaleDate().getTime() == day.getTime());
            }
        } else {
            ycfPriceInfos.forEach(ycfPriceInfo -> {
                setPrice(priceInfoPOs, newPriceInfos, ycfPriceInfo);
            });
        }
        priceInfoPOs.addAll(newPriceInfos);
        priceInfoPOs.sort(Comparator.comparing(po -> po.getSaleDate().getTime(), Long::compareTo));
        priceDao.updateByProductCode(pricePO);
        // 更新价格要刷新item的低价产品(异步)
        dynamicProductItemService.refreshItemByProductCode(Lists.newArrayList(productCode));
    }

    @Override
    public void syncFullPrice(YcfPrice ycfPrice){
        PricePO pricePO = YcfConverter.convertToPricePO(ycfPrice);
        ProductPO productPO = productDao.getBySupplierProductId(ycfPrice.getProductID());
        if(productPO != null){
            pricePO.setProductCode(productPO.getCode());
        } else {
            log.error("同步价格日历，根据供应商产品id={} 没有查到相关产品", ycfPrice.getProductID());
            return;
        }
        priceDao.updateByProductCode(pricePO);
        // 更新价格要刷新item的低价产品(异步)
        dynamicProductItemService.refreshItemByCode(productPO.getMainItemCode());
    }

    private List<YcfPriceInfo> syncPrice(YcfGetPriceRequest request){
        YcfBaseRequest ycfBaseRequest = new YcfBaseRequest(request);
        log.info("准备请求供应商(要出发)获取价格接口，参数={}", JSON.toJSONString(request));
        YcfBaseResult<YcfGetPriceResponse> baseResult = yaoChuFaClient.getPrice(ycfBaseRequest);
        log.info("供应商(要出发)获取价格接口返回，结果={}", JSON.toJSONString(baseResult));
        if(baseResult.getSuccess() && baseResult.getStatusCode() == YcfConstants.RESULT_CODE_SUCCESS){
            YcfGetPriceResponse response = baseResult.getData();
            if(response == null){
                log.error("获取价格失败，供应商（要出发）没有返回data");
                return null;
            }
            YcfPrice ycfPrice = new YcfPrice();
            ycfPrice.setProductID(response.getProductID());
            ycfPrice.setSaleInfos(response.getSaleInfos());
            if(request.getFull()){
                syncFullPrice(ycfPrice);
            } else {
                syncPrice(ycfPrice, request.getStartDate(), request.getEndDate());
            }
            return response.getSaleInfos();
        }
        return null;
    }

    private void setPrice(List<PriceInfoPO> priceInfoPOs, List<PriceInfoPO> newPriceInfos, YcfPriceInfo ycfPriceInfo){
        PriceInfoPO priceInfoPO = priceInfoPOs.stream().filter(po ->
                DateTimeUtil.trancateToDate(po.getSaleDate()).getTime() == DateTimeUtil.trancateToDate(ycfPriceInfo.getDate()).getTime()).findFirst().orElse(null);
        if(priceInfoPO == null){
            priceInfoPO = new PriceInfoPO();
            priceInfoPO.setSaleDate(MongoDateUtils.handleTimezoneInput(ycfPriceInfo.getDate()));
            priceInfoPO.setPriceType(ycfPriceInfo.getPriceType());
            newPriceInfos.add(priceInfoPO);
        }
        priceInfoPO.setStock(ycfPriceInfo.getStock());
        priceInfoPO.setSalePrice(ycfPriceInfo.getPrice());
        priceInfoPO.setSettlePrice(ycfPriceInfo.getSettlementPrice());
        priceInfoPO.setSaleDate(MongoDateUtils.handleTimezoneInput(ycfPriceInfo.getDate()));
    }

    private boolean filterProduct(YcfProduct ycfProduct){
        if(StringUtils.isBlank(ycfProduct.getPoiId())){
            log.info("要出发推送的产品没有主项目id,过滤掉。。");
            return false;
        }
        if(!YcfConstants.PRODUCT_TYPE_LIST.contains(ycfProduct.getProductType())){
            log.info("要出发推送了未知类型产品，类型={}", ycfProduct.getProductType());
            return false;
        }
        if(ListUtils.isEmpty(ycfProduct.getFoodList()) &&
                ListUtils.isEmpty(ycfProduct.getRoomList()) &&
                ListUtils.isEmpty(ycfProduct.getTicketList())){
            log.info("要出发推送的产品所有子项都是空，过滤掉。。");
            return false;
        }
        // 类型是单房 或者 只有酒店项有数据的都认为是单房，过滤
        if(ycfProduct.getProductType() == YcfConstants.PRODUCT_TYPE_ROOM ||
                (ListUtils.isEmpty(ycfProduct.getFoodList()) && ListUtils.isEmpty(ycfProduct.getTicketList()))){
            log.info("要出发推送的单房，过滤掉。。");
            return false;
        }
        if(ycfProduct.getRoomChoiceNum() != null && ycfProduct.getRoomOptionNum() != null &&
                ycfProduct.getRoomChoiceNum().intValue() != ycfProduct.getRoomOptionNum().intValue()){
            log.info("酒店可选和必选不一样（M选N），过滤掉。。");
            return false;
        }
        if(ycfProduct.getFoodChoiceNum() != null && ycfProduct.getFoodOptionNum() != null &&
                ycfProduct.getFoodChoiceNum().intValue() != ycfProduct.getFoodOptionNum().intValue()){
            log.info("餐饮可选和必选不一样（M选N），过滤掉。。");
            return false;
        }
        if(ycfProduct.getTicketChoiceNum() != null && ycfProduct.getTicketOptionNum() != null &&
                ycfProduct.getTicketChoiceNum().intValue() != ycfProduct.getTicketOptionNum().intValue()){
            log.info("景点可选和必选不一样（M选N），过滤掉。。");
            return false;
        }
        return true;
    }

    private boolean filterScenicProduct(YcfProduct ycfProduct){
        if(!filterProduct(ycfProduct)){
            return false;
        }
        // 是单票类型并且只有门票列表有数据
        if(ycfProduct.getProductType() != YcfConstants.PRODUCT_TYPE_TICKET
                || !(ListUtils.isEmpty(ycfProduct.getFoodList()) &&
                        ListUtils.isEmpty(ycfProduct.getRoomList()) &&
                        ListUtils.isNotEmpty(ycfProduct.getTicketList()))){
            return false;
        }
        return true;
    }

    /**
     * 转换类型
     * 以产品主poi类型为准，为了修正同一个poi挂在多个不同类型的产品上
     * @param poiType ycf poi类型
     * @param pack 是否套餐
     * @param productType 产品类型，如果没有算出来就原样返回
     * @return
     */
    private int convertToProductType(int poiType, boolean pack, int productType){
        if(poiType == YcfConstants.POI_TYPE_ROOM){
            if(pack){
                return ProductType.FREE_TRIP.getCode();
            }
            return productType;
        }
        if(poiType == YcfConstants.POI_TYPE_TICKET){
            if(pack){
                return ProductType.SCENIC_TICKET_PLUS.getCode();
            }
            return ProductType.SCENIC_TICKET.getCode();
        }
        if(poiType == YcfConstants.POI_TYPE_FOOD){
            return ProductType.RESTAURANT.getCode();
        }
        return productType;
    }

    // ============================新结构============================

    @Override
    public void syncScenicProduct(List<YcfProduct> ycfProducts){
        if(ListUtils.isEmpty(ycfProducts)){
            log.error("要出发推送的产品列表为空");
            return;
        }
        ycfProducts.forEach(ycfProduct -> {
            if(!filterScenicProduct(ycfProduct)){
                return;
            }
            List<String> scenicIds = ycfProduct.getTicketList().stream().map(YcfResourceTicket::getPoiId).collect(Collectors.toList());
            if(ListUtils.isEmpty(scenicIds)){
                log.error("要出发景点不存在");
                return;
            }
            ScenicSpotProductMPO scenicSpotProductMPO = scenicSpotProductDao.getBySupplierProductId(ycfProduct.getProductID(), SUPPLIER_CODE_YCF);
            if(scenicSpotProductMPO == null){
                scenicSpotProductMPO = new ScenicSpotProductMPO();
                scenicSpotProductMPO.setId(commonService.getId(BizTagConst.BIZ_SCENICSPOT_PRODUCT));
                scenicSpotProductMPO.setCreateTime(MongoDateUtils.handleTimezoneInput(new Date()));
                ScenicSpotMappingMPO scenicSpotMappingMPO = scenicSpotMappingDao.getScenicSpotByChannelScenicSpotIdAndChannel(ycfProduct.getPoiId(), SUPPLIER_CODE_YCF);
                if(scenicSpotMappingMPO == null){
                    log.error("要出发产品{}没有查到关联景点{}", ycfProduct.getProductID(), ycfProduct.getPoiId());
                    return;
                }
                ScenicSpotMPO scenicSpotMPO = scenicSpotDao.getScenicSpotById(scenicSpotMappingMPO.getScenicSpotId());
                if(scenicSpotMPO == null){
                    log.error("景点{}不存在", scenicSpotMPO.getId());
                    return;
                }
                scenicSpotProductMPO.setScenicSpotId(scenicSpotMPO.getId());
                scenicSpotProductMPO.setIsDel(0);
                scenicSpotProductMPO.setSellType(1);
                scenicSpotProductMPO.setSupplierProductId(ycfProduct.getProductID());
                scenicSpotProductMPO.setPayServiceType(0);
                scenicSpotProductMPO.setChannel(SUPPLIER_CODE_YCF);
                if(ListUtils.isNotEmpty(ycfProduct.getProductImageList())){
                    scenicSpotProductMPO.setImages(ycfProduct.getProductImageList().stream().map(YcfImageBase::getImageUrl).collect(Collectors.toList()));
                    scenicSpotProductMPO.setMainImage(scenicSpotProductMPO.getImages().get(0));
                }
            }
            scenicSpotProductMPO.setName(ycfProduct.getProductName());
            scenicSpotProductMPO.setUpdateTime(MongoDateUtils.handleTimezoneInput(new Date()));
            ScenicSpotProductTransaction transaction = new ScenicSpotProductTransaction();
            if(ycfProduct.getBookAheadMin() != null && ycfProduct.getBookAheadMin() > 0){
                transaction.setBookBeforeTime(ycfProduct.getBookAheadMin().toString());
                scenicSpotProductMPO.setScenicSpotProductTransaction(transaction);
            }
            ScenicSpotProductBaseSetting baseSetting = new ScenicSpotProductBaseSetting();
            BackChannelEntry backChannelEntry = commonService.getSupplierById(scenicSpotProductMPO.getChannel());
            if(backChannelEntry != null || StringUtils.isNotBlank(backChannelEntry.getAppSource())){
                baseSetting.setAppSource(backChannelEntry.getAppSource());
            }
            // 默认当前
            baseSetting.setLaunchDateTime(MongoDateUtils.handleTimezoneInput(new Date()));
            if(StringUtils.isNotBlank(ycfProduct.getStartDate())){
                baseSetting.setLaunchDateTime(MongoDateUtils.handleTimezoneInput(DateTimeUtil.parseDate(ycfProduct.getStartDate())));
            }
            // 默认及时
            baseSetting.setLaunchType(1);
            baseSetting.setStockCount(0);
            scenicSpotProductMPO.setScenicSpotProductBaseSetting(baseSetting);
            if(ycfProduct.getProductStatus() == YcfConstants.PRODUCT_STATUS_VALID){
                scenicSpotProductMPO.setStatus(1);
            } else {
                scenicSpotProductMPO.setStatus(3);
            }
            if(StringUtils.isBlank(scenicSpotProductMPO.getId())){
                scenicSpotProductDao.addProduct(scenicSpotProductMPO);
            } else {
                scenicSpotProductDao.saveProduct(scenicSpotProductMPO);
            }
            ScenicSpotRuleMPO ruleMPO = new ScenicSpotRuleMPO();
            ruleMPO.setId(commonService.getId(BizTagConst.BIZ_SCENICSPOT_PRODUCT));
            ruleMPO.setChannel(SUPPLIER_CODE_YCF);
            ruleMPO.setScenicSpotId(scenicSpotProductMPO.getScenicSpotId());
            ruleMPO.setIsCouponRule(0);
            if(ycfProduct.getMaxNum() != null){
                ruleMPO.setLimitBuy(1);
                // -1 这些是为了防止0起作用，实际只为设置maxcount
                ruleMPO.setLimitBuyType(-1);
                ruleMPO.setRangeType(-1);
                ruleMPO.setDistinguishUser(-1);
                ruleMPO.setMaxCount(ycfProduct.getMaxNum());
            }
            ruleMPO.setFeeInclude(ycfProduct.getFeeInclude());
            ruleMPO.setRefundRuleDesc(ycfProduct.getRefundNote());
            if(ycfProduct.getRefundType() != null){
                if(ycfProduct.getRefundType() == 1){
                    ruleMPO.setRefundCondition(0);
                } else if(ycfProduct.getRefundType() == 2){
                    ruleMPO.setRefundCondition(1);
                } else if(ycfProduct.getRefundType() == 3){
                    ruleMPO.setRefundCondition(2);
                }
            }
            RefundRule refundRule = new RefundRule();
            if(ycfProduct.getAdvanceOrDelayType() != null){
                if(ycfProduct.getAdvanceOrDelayType() == 0){
                    refundRule.setRefundRuleType(1);
                } else if(ycfProduct.getAdvanceOrDelayType() == 1){
                    refundRule.setRefundRuleType(4);
                }
            }
            if(ycfProduct.getRefundPreMinute() != null){
                int allMin = ycfProduct.getRefundPreMinute();
                int day = 0;
                int hour = 0;
                int min = 0;
                // 有天
                if(allMin >= 1440){
                    day = allMin / 1440;
                    allMin = allMin % 1440;
                }
                if(allMin >= 60){
                    hour = allMin / 60;
                    min = allMin % 60;
                } else {
                    min = allMin;
                }
                refundRule.setDay(day);
                refundRule.setHour(hour);
                refundRule.setMinute(min);
            }
            ruleMPO.setRefundRules(Lists.newArrayList(refundRule));
            if(ListUtils.isNotEmpty(ycfProduct.getBookRules())){
                for (YcfProductBookRule bookRule : ycfProduct.getBookRules()) {
                    List<Integer> rules = Lists.newArrayList();
                    if(bookRule.getCName() != null && bookRule.getCName()){
                        rules.add(0);
                    }
                    if(bookRule.getMobile() != null && bookRule.getMobile()){
                        rules.add(1);
                    }
                    if(bookRule.getCredential() != null && bookRule.getCredential()){
                        rules.add(3);
                    }
                    if(bookRule.getEmail() != null && bookRule.getEmail()){
                        rules.add(4);
                    }
                    if(StringUtils.equals(bookRule.getPersonType(), "0")){
                        ruleMPO.setTicketInfos(rules);
                        ruleMPO.setTicketCardTypes(bookRule.getCredentialType());
                    } else if(StringUtils.equals(bookRule.getPersonType(), "1")){
                        ruleMPO.setTravellerInfos(rules);
                        ruleMPO.setTravellerTypes(bookRule.getCredentialType());
                    }
                }
            }
            scenicSpotRuleDao.addScenicSpotRule(ruleMPO);
            Integer days = ConfigGetter.getByFileItemInteger(YcfConfigConstants.CONFIG_FILE_NAME, YcfConfigConstants.TASK_SYNC_PRICE_INTERVAL);
            days = days == null ? Integer.valueOf(30) : days;
            String start = DateTimeUtil.formatDate(new Date());
            String end = DateTimeUtil.formatDate(DateTimeUtil.addDay(new Date(), days));
            YcfGetPriceRequest request = new YcfGetPriceRequest();
            request.setPartnerProductID(scenicSpotProductMPO.getId());
            request.setProductID(ycfProduct.getProductID());
            request.setStartDate(start);
            request.setEndDate(end);
            List<YcfPriceInfo> ycfPriceInfos = getPriceV2(request);
            syncPrice(scenicSpotProductMPO.getId(), ycfPriceInfos, ruleMPO.getId(), ycfProduct.getTicketType() == null ? null : ycfProduct.getTicketType().toString());
        });
    }

    @Override
    public List<String> syncScenic(List<String> scenicIds){
        if(ListUtils.isEmpty(scenicIds)){
            log.error("同步poi失败，poi id集合为空");
            return null;
        }
        log.info("开始同步poi，id list = {}", JSON.toJSONString(scenicIds));
        YcfGetPoiRequest ycfGetPoiRequest = new YcfGetPoiRequest();
        ycfGetPoiRequest.setPoiIdList(scenicIds);
        YcfBaseRequest ycfBaseRequest = new YcfBaseRequest(ycfGetPoiRequest);
        log.info("准备请求供应商(要出发)获取poi接口，参数={}", JSON.toJSONString(ycfGetPoiRequest));
        YcfBaseResult<YcfGetPoiResponse> baseResult = yaoChuFaClient.getPoi(ycfBaseRequest);
        log.info("供应商(要出发)获取poi接口返回，结果={}", JSON.toJSONString(baseResult));
        if(baseResult.getSuccess() && baseResult.getStatusCode() == YcfConstants.RESULT_CODE_SUCCESS){
            YcfGetPoiResponse response = baseResult.getData();
            if(response == null){
                log.error("同步poi失败，供应商（要出发）没有返回data");
                return null;
            }
            List<YcfProductItem> ycfProductItems = response.getPoiList();
            if(ListUtils.isEmpty(ycfProductItems)){
                log.error("同步poi失败，供应商（要出发）没有返回poi信息");
                return null;
            }
           return ycfProductItems.stream().map(item -> {
                try {
                    ScenicSpotMPO newScenic = YcfConverter.convertToScenicSpotMPO(item);
                    // 设置省市区
                    commonService.setCity(newScenic);
                    // 同时保存映射关系
                    commonService.updateScenicSpotMapping(item.getPoiID(), SUPPLIER_CODE_YCF, newScenic);
                    // 更新备份
                    commonService.updateScenicSpotMPOBackup(newScenic, item.getPoiID(), SUPPLIER_CODE_YCF, item);
                    return newScenic.getId();
                } catch (Exception e) {
                    log.error("poi落地失败，", e);
                    return null;
                }
            }).filter(id -> StringUtils.isNotBlank(id)).collect(Collectors.toList());
        }
        return null;
    }

    @Override
    public void syncPriceV2(YcfGetPriceRequest request){
        if(StringUtils.isBlank(request.getEndDate())){
            request.setEndDate(request.getStartDate());
        }
        int diffDays = DateTimeUtil.getDateDiffDays(DateTimeUtil.parseDate(request.getEndDate()), DateTimeUtil.parseDate(request.getStartDate()));
        // 要出发最多请求30天，
        int round = diffDays / 30;
        int tail = diffDays % 30;
        // 有余数需要循环商+1次
        int n = round + (tail == 0 ? 0 : 1);
        // 开始结束日期相同n会=0
        n = n == 0 ? 1 : n;
        for(int i = 0; i < n; i++){
            YcfGetPriceRequest newRequest = new YcfGetPriceRequest();
            newRequest.setTraceId(request.getTraceId());
            newRequest.setFull(request.getFull());
            newRequest.setPartnerProductID(request.getPartnerProductID());
            newRequest.setProductID(request.getProductID());
            // 从结束日期+1天开始
            newRequest.setStartDate(DateTimeUtil.formatDate(DateTimeUtil.addDay(DateTimeUtil.parseDate(request.getStartDate()), i * 30)));
            // 前后共30天
            newRequest.setEndDate(DateTimeUtil.formatDate(DateTimeUtil.addDay(DateTimeUtil.parseDate(newRequest.getStartDate()), 29)));
            if(i == n - 1){
                newRequest.setEndDate(request.getEndDate());
            }
            try {
                log.info("开始同步价格，产品编码 = {} ；日期 = {} 至 {} ", request.getPartnerProductID(), newRequest.getStartDate(), newRequest.getEndDate());
                List<YcfPriceInfo> ycfPriceInfos = getPriceV2(newRequest);
                syncPrice(request.getPartnerProductID(), ycfPriceInfos);
                log.info("同步价格完成，产品编码 = {} ；日期 = {} 至 {} ", request.getPartnerProductID(), newRequest.getStartDate(), newRequest.getEndDate());
                // 要出发限制1分钟最多请求200次
                Thread.sleep(310);
            } catch (Exception e) {
                log.info("同步价异常，产品编码 = {} ；日期 = {} 至 {} ", request.getPartnerProductID(), newRequest.getStartDate(), newRequest.getEndDate(), e);
                continue;
            }
        }
    }

    @Override
    public void syncPriceV2(YcfPrice ycfPrice){
        String ycfProductId = ycfPrice.getProductID();
        List<YcfPriceInfo> ycfPriceInfos = ycfPrice.getSaleInfos();
        if(StringUtils.isBlank(ycfProductId) || ListUtils.isEmpty(ycfPriceInfos)){
            log.error("价格同步失败，{}产品id或者价格日历为空", SUPPLIER_CODE_YCF);
            return;
        }
        ScenicSpotProductMPO scenicSpotProductMPO = scenicSpotProductDao.getBySupplierProductId(ycfProductId, SUPPLIER_CODE_YCF);
        if(scenicSpotProductMPO == null){
            log.error("价格同步失败，{}的产品{}不存在", SUPPLIER_CODE_YCF, ycfProductId);
            return;
        }
        syncPrice(scenicSpotProductMPO.getId(), ycfPriceInfos);
    }

    private List<YcfPriceInfo> getPriceV2(YcfGetPriceRequest request){
        YcfBaseRequest ycfBaseRequest = new YcfBaseRequest(request);
        log.info("准备请求供应商(要出发)获取价格接口，参数={}", JSON.toJSONString(request));
        YcfBaseResult<YcfGetPriceResponse> baseResult = yaoChuFaClient.getPrice(ycfBaseRequest);
        log.info("供应商(要出发)获取价格接口返回，结果={}", JSON.toJSONString(baseResult));
        if(baseResult.getSuccess() && baseResult.getStatusCode() == YcfConstants.RESULT_CODE_SUCCESS){
            YcfGetPriceResponse response = baseResult.getData();
            if(response == null){
                log.error("获取价格失败，供应商（要出发）没有返回data");
                return null;
            }
            return response.getSaleInfos();
        }
        return null;
    }

    private void syncPrice(String productId, List<YcfPriceInfo> ycfPriceInfos){
        syncPrice(productId, ycfPriceInfos, null, null);
    }

    private void syncPrice(String productId, List<YcfPriceInfo> ycfPriceInfos, String ruleId, String ticketKind){
        if(StringUtils.isBlank(productId) || ListUtils.isEmpty(ycfPriceInfos)){
            log.error("同步价格失败，产品id或者价格日历为空");
            return;
        }
        List<ScenicSpotProductPriceMPO> existPrice = scenicSpotProductPriceDao.getByProductId(productId);
        if(ListUtils.isNotEmpty(existPrice)){
            ruleId = existPrice.get(0).getScenicSpotRuleId();
            ticketKind = existPrice.get(0).getTicketKind();
        }
        if(StringUtils.isBlank(ruleId)){
            log.error("同步价格失败，产品{}规则没有获取到规则id", productId);
            return;
        }
        for (YcfPriceInfo yp : ycfPriceInfos) {
            ScenicSpotProductPriceMPO priceMPO = new ScenicSpotProductPriceMPO();
            priceMPO.setId(commonService.getId(BizTagConst.BIZ_SCENICSPOT_PRODUCT));
            priceMPO.setScenicSpotProductId(productId);
            priceMPO.setScenicSpotRuleId(ruleId);
            priceMPO.setTicketKind(ticketKind);
            priceMPO.setStartDate(DateTimeUtil.formatDate(yp.getDate()));
            priceMPO.setEndDate(DateTimeUtil.formatDate(yp.getDate()));
            priceMPO.setSellPrice(yp.getPrice());
            priceMPO.setSettlementPrice(yp.getSettlementPrice());
            priceMPO.setStock(yp.getStock());
            scenicSpotProductPriceDao.addScenicSpotProductPrice(priceMPO);
        }
    }

}
