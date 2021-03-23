package com.huoli.trip.supplier.web.yaochufa.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.huoli.trip.common.constant.Constants;
import com.huoli.trip.common.constant.ProductType;
import com.huoli.trip.common.entity.*;
import com.huoli.trip.common.util.CommonUtils;
import com.huoli.trip.common.util.DateTimeUtil;
import com.huoli.trip.common.util.ListUtils;
import com.huoli.trip.common.util.MongoDateUtils;
import com.huoli.trip.supplier.api.DynamicProductItemService;
import com.huoli.trip.supplier.api.YcfSyncService;
import com.huoli.trip.supplier.feign.client.yaochufa.client.IYaoChuFaClient;
import com.huoli.trip.supplier.self.yaochufa.constant.YcfConstants;
import com.huoli.trip.supplier.self.yaochufa.vo.*;
import com.huoli.trip.supplier.self.yaochufa.vo.basevo.YcfBaseRequest;
import com.huoli.trip.supplier.self.yaochufa.vo.basevo.YcfBaseResult;
import com.huoli.trip.supplier.web.dao.PriceDao;
import com.huoli.trip.supplier.web.dao.ProductDao;
import com.huoli.trip.supplier.web.dao.ProductItemDao;
import com.huoli.trip.supplier.web.service.CommonService;
import com.huoli.trip.supplier.web.yaochufa.convert.YcfConverter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

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
            } else {
                productPO.setCreateTime(MongoDateUtils.handleTimezoneInput(productPO.getCreateTime()));
                productPO.setAuditStatus(exist.getAuditStatus());
                productPO.setSupplierStatus(exist.getSupplierStatus());
                productPO.setRecommendFlag(exist.getRecommendFlag());
                productPO.setAppFrom(exist.getAppFrom());
                productPO.setBookDescList(exist.getBookDescList());
                productPO.setDescriptions(exist.getDescriptions());
                productPO.setBookNoticeList(exist.getBookNoticeList());
                commonService.compareProduct(productPO);
            }
            productPO.setUpdateTime(MongoDateUtils.handleTimezoneInput(new Date()));
            productPO.setOperator(Constants.SUPPLIER_CODE_YCF);
            productPO.setOperatorName(Constants.SUPPLIER_NAME_YCF);
            productDao.updateByCode(productPO);
            commonService.saveBackupProduct(productPO);
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
                    List<ItemFeaturePO> featurePOs = null;
                    List<ImageBasePO> imageDetails = null;
                    ProductPO productPO = null;
                    List<ImageBasePO> images = null;
                    List<ImageBasePO> mainImages = null;
                    if(exist == null){
                        productItemPO.setCreateTime(MongoDateUtils.handleTimezoneInput(new Date()));
                        // todo 暂时默认通过
                        productItemPO.setAuditStatus(Constants.VERIFY_STATUS_PASSING);
//            productItem.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
                    } else {
                        featurePOs = exist.getFeatures();
                        imageDetails = exist.getImageDetails();
                        productPO = exist.getProduct();
                        images = exist.getImages();
                        mainImages = exist.getMainImages();
                        productItemPO.setCreateTime(MongoDateUtils.handleTimezoneInput(productItemPO.getCreateTime()));
                        productItemPO.setAuditStatus(exist.getAuditStatus());
                        productItemPO.setProduct(exist.getProduct());
                        commonService.compareProductItem(productItemPO);
                    }
                    productItemPO.setUpdateTime(MongoDateUtils.handleTimezoneInput(new Date()));
                    productItemPO.setOperator(Constants.SUPPLIER_CODE_YCF);
                    productItemPO.setOperatorName(Constants.SUPPLIER_NAME_YCF);
                    try {
                        commonService.saveBackupProductItem(productItemPO);
                    } catch (Exception e) {
                        log.error("保存{}的副本异常", productItemPO.getCode(), e);
                    }
                    // 这个不更新，用原有的
                    productItemPO.setFeatures(featurePOs);
                    productItemPO.setImageDetails(imageDetails);
                    productItemPO.setProduct(productPO);
                    productItemPO.setImages(images);
                    productItemPO.setMainImages(mainImages);
                    if(ListUtils.isEmpty(productItemPO.getImages()) && ListUtils.isEmpty(productItemPO.getMainImages())){
                        log.info("{}没有列表图、轮播图，设置待审核", productItemPO.getCode());
                        productItemPO.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
                    }
                    productItemDao.updateByCode(productItemPO);
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
        String productCode = CommonUtils.genCodeBySupplier(Constants.SUPPLIER_CODE_YCF, ycfProductId);
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
        pricePO.setOperator(Constants.SUPPLIER_CODE_YCF);
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
        String productCode = CommonUtils.genCodeBySupplier(Constants.SUPPLIER_CODE_YCF, ycfProductId);
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
        pricePO.setOperator(Constants.SUPPLIER_CODE_YCF);
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

}
