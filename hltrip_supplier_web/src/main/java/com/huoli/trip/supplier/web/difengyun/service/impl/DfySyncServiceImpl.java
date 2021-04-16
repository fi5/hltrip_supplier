package com.huoli.trip.supplier.web.difengyun.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.huoli.trip.common.constant.ConfigConstants;
import com.huoli.trip.common.constant.Constants;
import com.huoli.trip.common.constant.ProductType;
import com.huoli.trip.common.entity.*;
import com.huoli.trip.common.util.*;
import com.huoli.trip.supplier.api.DynamicProductItemService;
import com.huoli.trip.supplier.feign.client.difengyun.client.IDiFengYunClient;
import com.huoli.trip.supplier.self.difengyun.constant.DfyConstants;
import com.huoli.trip.supplier.self.difengyun.vo.*;
import com.huoli.trip.supplier.self.difengyun.vo.request.*;
import com.huoli.trip.supplier.self.difengyun.vo.response.*;
import com.huoli.trip.supplier.web.config.FeignLogger;
import com.huoli.trip.supplier.web.dao.*;
import com.huoli.trip.supplier.web.difengyun.convert.DfyTicketConverter;
import com.huoli.trip.supplier.web.difengyun.convert.DfyToursConverter;
import com.huoli.trip.supplier.web.difengyun.service.DfySyncService;
import com.huoli.trip.supplier.web.service.CommonService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.huoli.trip.supplier.self.difengyun.constant.DfyConfigConstants.*;
import static com.huoli.trip.supplier.self.difengyun.constant.DfyConstants.*;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2020/12/9<br>
 */
@Service(timeout = 10000,group = "hltrip")
@Slf4j
public class DfySyncServiceImpl implements DfySyncService {

    @Autowired
    private IDiFengYunClient diFengYunClient;

    @Autowired
    private ProductItemDao productItemDao;

    @Autowired
    private ProductDao productDao;

    @Autowired
    private PriceDao priceDao;

    @Autowired
    private DynamicProductItemService dynamicProductItemService;

    @Autowired
    private HodometerDao hodometerDao;

    @Autowired
    private CommonService commonService;

    @Autowired
    private BackupProductDao backupProductDao;

    @Override
    public boolean syncScenicList(DfyScenicListRequest request){
        try {
            DfyBaseRequest<DfyScenicListRequest> listRequest = new DfyBaseRequest<>(request);
            DfyBaseResult<DfyScenicListResponse> baseResult = diFengYunClient.getScenicList(listRequest);
            if(baseResult != null && baseResult.getData() != null && ListUtils.isNotEmpty(baseResult.getData().getRows())){
                List<DfyScenic> scenics = baseResult.getData().getRows();
                scenics.forEach(s -> syncScenicDetail(s.getScenicId()));
                return true;
            } else {
                log.error("笛风云门票列表返回空，request = {}", JSON.toJSONString(listRequest));
                return false;
            }
        } catch (Exception e) {
            log.error("笛风云同步景点、产品异常", e);
            return false;
        }
    }

    @Override
    public void syncScenicDetail(String scenicId){
        DfyScenicDetailRequest detailRequest = new DfyScenicDetailRequest();
        detailRequest.setScenicId(scenicId);
        DfyBaseRequest detailBaseRequest = new DfyBaseRequest<>(detailRequest);
        DfyBaseResult<DfyScenicDetail> detailBaseResult = diFengYunClient.getScenicDetail(detailBaseRequest);
        if(detailBaseResult != null && detailBaseResult.getData() != null){
            DfyScenicDetail scenicDetail = detailBaseResult.getData();
            ProductItemPO newProductItem = DfyTicketConverter.convertToProductItemPO(scenicDetail);
            ProductItemPO oldProductItem = productItemDao.selectByCode(newProductItem.getCode());
            // 已存在的景点不更新
            if(oldProductItem == null){
                newProductItem.setCreateTime(MongoDateUtils.handleTimezoneInput(new Date()));
                newProductItem.setUpdateTime(MongoDateUtils.handleTimezoneInput(new Date()));
                newProductItem.setOperator(Constants.SUPPLIER_CODE_DFY);
                newProductItem.setOperatorName(Constants.SUPPLIER_NAME_DFY);
                newProductItem.setAuditStatus(Constants.VERIFY_STATUS_PASSING);
                try {
                    // 保存副本
                    commonService.saveBackupProductItem(newProductItem);
                } catch (Exception e) {
                    log.error("保存{}副本异常", newProductItem.getCode(), e);
                }
                if(ListUtils.isEmpty(newProductItem.getImages()) && ListUtils.isEmpty(newProductItem.getMainImages())){
                    log.info("{}没有列表图、轮播图，设置待审核", Constants.VERIFY_STATUS_WAITING);
                    newProductItem.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
                }
                productItemDao.updateByCode(newProductItem);
                // 拿到最新的景点
                oldProductItem = productItemDao.selectByCode(newProductItem.getCode());
            }
            List<DfyTicket> allTickets = Lists.newArrayList();
            if(ListUtils.isNotEmpty(scenicDetail.getTicketList())){
                allTickets.addAll(scenicDetail.getTicketList());
            }
            if(ListUtils.isNotEmpty(scenicDetail.getDisTickets())){
                scenicDetail.getDisTickets().forEach(t -> t.setExclusive(1));
                allTickets.addAll(scenicDetail.getDisTickets());
            }
            if(ListUtils.isNotEmpty(allTickets)){
                for (DfyTicket dfyTicket : allTickets) {
                    // 只同步新增产品，同步更新单独有定时任务执行
                    syncProduct(dfyTicket.getProductId(), oldProductItem, PRODUCT_SYNC_MODE_ONLY_ADD);
                }
            }
            dynamicProductItemService.refreshItemByCode(newProductItem.getCode());
        } else {
            log.error("笛风云门票详情返回空，request = {}", JSON.toJSONString(detailBaseRequest));
        }
    }

    @Override
    public void syncProduct(String productId, ProductItemPO productItemPO){
        syncProduct(productId, productItemPO, DfyConstants.PRODUCT_SYNC_MODE_UNLIMITED);
    }

    @Override
    public void syncProduct(String productId, ProductItemPO productItemPO, int syncMode){
        DfyTicketDetailRequest ticketDetailRequest = new DfyTicketDetailRequest();
        ticketDetailRequest.setProductId(Integer.valueOf(productId));
        DfyBaseRequest ticketDetailBaseRequest = new DfyBaseRequest<>(ticketDetailRequest);
        DfyBaseResult<DfyTicketDetail> ticketDetailDfyBaseResult = diFengYunClient.getTicketDetail(ticketDetailBaseRequest);

        if(ticketDetailDfyBaseResult != null && ticketDetailDfyBaseResult.getData() != null){
            DfyTicketDetail dfyTicketDetail = ticketDetailDfyBaseResult.getData();
            // 当通知更新的时候这个item是空的
            if(productItemPO == null){
                String scenicId = dfyTicketDetail.getScenicId();
                // 如果没有景点id更新不了产品，因为产品是绑在景点下的
                if(StringUtils.isBlank(scenicId)){
                    log.error("门票productId={}，没有景点id", productId);
                    return;
                }
                // 如果景点不存在就去更新景点，更新景点的同时会更新门票，所以下面就不用走了。
                productItemPO = productItemDao.selectByCode(CommonUtils.genCodeBySupplier(Constants.SUPPLIER_CODE_DFY, scenicId));
                if(productItemPO == null){
                    syncScenicDetail(scenicId);
                    return;
                }
            }
            ProductPO product = DfyTicketConverter.convertToProductPO(dfyTicketDetail);
            product.setMainItemCode(productItemPO.getCode());
            product.setMainItem(productItemPO);
            product.setCity(productItemPO.getCity());
            product.setDesCity(productItemPO.getDesCity());
            product.setOriCity(productItemPO.getOriCity());
            if(product.getTicket() != null && ListUtils.isNotEmpty(product.getTicket().getTickets())){
                for (TicketInfoPO ticket : product.getTicket().getTickets()) {
                    ticket.setItemId(productItemPO.getCode());
                    ticket.setProductItem(productItemPO);
                }
            }
            ProductPO productPO = productDao.getByCode(product.getCode());
            // 是否只同步本地没有的产品
            if(PRODUCT_SYNC_MODE_ONLY_ADD == syncMode && productPO != null){
                log.error("笛风云，本次同步不包括更新更新，跳过，supplierProductCode={}", product.getSupplierProductId());
                return;
            }
            if(PRODUCT_SYNC_MODE_ONLY_UPDATE == syncMode && productPO == null){
                log.error("笛风云，本次同步不包括新增产品，跳过，supplierProductCode={}", product.getSupplierProductId());
                return;
            }
            product.setUpdateTime(MongoDateUtils.handleTimezoneInput(new Date()));
            product.setOperator(Constants.SUPPLIER_CODE_DFY);
            product.setOperatorName(Constants.SUPPLIER_NAME_DFY);
            product.setValidTime(MongoDateUtils.handleTimezoneInput(DateTimeUtil.trancateToDate(new Date())));
            log.info("准备更新价格。。。");
            if(ListUtils.isNotEmpty(ticketDetailDfyBaseResult.getData().getPriceCalendar())){
                log.info("有价格信息。。。");
                PricePO pricePO = syncPrice(product.getCode(), ticketDetailDfyBaseResult.getData().getPriceCalendar());
                if(pricePO != null && ListUtils.isNotEmpty(pricePO.getPriceInfos())){
                    // 笛风云没有上下架时间，就把最远的销售日期作为下架时间
                    PriceInfoPO priceInfoPO = pricePO.getPriceInfos().stream().max(Comparator.comparing(PriceInfoPO::getSaleDate)).get();
                    product.setInvalidTime(MongoDateUtils.handleTimezoneInput(priceInfoPO.getSaleDate()));
                }
            } else {
                product.setInvalidTime(MongoDateUtils.handleTimezoneInput(product.getValidTime()));
                log.error("没有价格信息。。。。");
            }
            ProductPO backup;
            if(productPO == null){
                product.setCreateTime(MongoDateUtils.handleTimezoneInput(new Date()));
                // todo 暂时默认通过
//                product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
                product.setAuditStatus(Constants.VERIFY_STATUS_PASSING);
                product.setSupplierStatus(Constants.SUPPLIER_STATUS_OPEN);
                BackChannelEntry backChannelEntry = commonService.getSupplierById(product.getSupplierId());
                if(backChannelEntry == null
                        || backChannelEntry.getStatus() == null
                        || backChannelEntry.getStatus() != 1){
                    product.setSupplierStatus(Constants.SUPPLIER_STATUS_CLOSED);
                }
                if(backChannelEntry != null || StringUtils.isNotBlank(backChannelEntry.getAppSource())){
                    List<String> appFroms = Arrays.asList(backChannelEntry.getAppSource().split(","));
                    product.setAppFrom(appFroms);
                }
                backup = JSON.parseObject(JSON.toJSONString(product), ProductPO.class);
            } else {
                backup = JSON.parseObject(JSON.toJSONString(product), ProductPO.class);
                product.setAuditStatus(productPO.getAuditStatus());
                product.setSupplierStatus(productPO.getSupplierStatus());
                product.setRecommendFlag(productPO.getRecommendFlag());
                product.setAppFrom(productPO.getAppFrom());
                product.setDescriptions(productPO.getDescriptions());
                // 下面对比信息会处理这个信息
//                product.setBookDescList(productPO.getBookDescList());
                if(productPO.getCreateTime() == null){
                    product.setCreateTime(MongoDateUtils.handleTimezoneInput(new Date()));
                } else {
                    product.setCreateTime(MongoDateUtils.handleTimezoneInput(productPO.getCreateTime()));
                }
                commonService.compareProduct(product, productPO);
            }
            productDao.updateByCode(product);
            // 保存副本
            commonService.saveBackupProduct(backup);
            dynamicProductItemService.refreshItemByProductCode(Lists.newArrayList(product.getCode()));
        } else {
            log.error("笛风云产品详情返回空，request = {}", JSON.toJSONString(ticketDetailBaseRequest));

            String code = CommonUtils.genCodeBySupplier(Constants.SUPPLIER_CODE_DFY, productId);
            ProductPO productPO = productDao.getByCode(code);
            // 笛风云的产品下线就不会返回，所以没拿到就认为已下线，
            // 正常下线只是data为空，errorCode是231000，其它错误码说明是接口有异常，不下线产品，防止误下线
            if(productPO != null
                    && ticketDetailDfyBaseResult != null
                    && ticketDetailDfyBaseResult.getData() == null
                    && Arrays.asList("231000", "350204").contains(ticketDetailDfyBaseResult.getErrorCode())){
                productDao.updateStatusByCode(productPO.getCode(), Constants.PRODUCT_STATUS_INVALID);
                dynamicProductItemService.refreshItemByProductCode(Lists.newArrayList(productPO.getCode()));
                log.info("笛风云产品详情返回空，产品已下线，productCode = {}", productPO.getCode());
            }
        }
    }



    /**
     * 设置价格日历
     * @param productCode
     * @param priceCalendar
     */
    private PricePO syncPrice(String productCode, List<DfyPriceCalendar> priceCalendar){
        log.info("查询价格。。");
        PricePO pricePO = priceDao.getByProductCode(productCode);
        PricePO price = DfyTicketConverter.convertToPricePO(priceCalendar);
        price.setProductCode(productCode);
        if(pricePO == null){
            log.info("没有查询到价格，准备新建。。");
            price.setCreateTime(MongoDateUtils.handleTimezoneInput(new Date()));
        }
        price.setUpdateTime(MongoDateUtils.handleTimezoneInput(new Date()));
        price.setOperator(Constants.SUPPLIER_CODE_DFY);
        price.setOperatorName(Constants.SUPPLIER_NAME_DFY);
        priceDao.updateByProductCode(price);
        log.info("价格已更新。。。");
        return price;
    }

    @Override
    @Async
    public void productUpdate(DfyProductNoticeRequest request){
        try {
            log.info("接收到笛风云产品变更通知。。");
            List<DfyProductNotice> productNotices = request.getProductIds();
            if(ListUtils.isEmpty(productNotices)){
                log.error("笛风云通知更新产品列表为空");
                return;
            }
            productNotices.forEach(p -> {
                if(p.getClassBrandParentId() == DfyConstants.BRAND_SELF){
                    log.error("笛风云通知更新产品 {} 是自助游，跳过。", p.getProductId());
                    return;
                }
                // 如果只是更新状态直接在这里改就行
                if(p.getNoticeType() == DfyConstants.NOTICE_TYPE_INVALID || p.getNoticeType() == DfyConstants.NOTICE_TYPE_VALID){
                    ProductPO productPO = productDao.getBySupplierProductId(p.getProductId().toString());
                    // 如果本地有就直接更新
                    if(productPO != null){
                        productPO.setStatus(p.getNoticeType() == DfyConstants.NOTICE_TYPE_INVALID ? Constants.PRODUCT_STATUS_INVALID : Constants.PRODUCT_STATUS_VALID);
                        productPO.setUpdateTime(MongoDateUtils.handleTimezoneInput(new Date()));
                        productDao.updateByCode(productPO);
                        dynamicProductItemService.refreshItemByProductCode(Lists.newArrayList(productPO.getCode()));
                        return;
                    }
                }
                // 如不不是改状态或者产品不存在就走同步
                syncToursDetail(p.getProductId().toString(), DfyConstants.PRODUCT_SYNC_MODE_UNLIMITED);
            });
        } catch (Exception e) {
            log.error("笛风云接收通知更新产品异常，", e);
        }
    }

    @Override
    public List<ProductPO> getSupplierProductIds(Integer productType){
        String supplier = Constants.SUPPLIER_CODE_DFY;
        if(productType == ProductType.TRIP_GROUP.getCode()){
            supplier = Constants.SUPPLIER_CODE_DFY_TOURS;
        }
        return productDao.getSupplierProductIds(supplier, productType);
    }

    @Override
    public DfyBaseResult<DfyToursListResponse> getToursList(DfyToursListRequest request){
        try {
            DfyBaseRequest<DfyToursListRequest> listRequest = new DfyBaseRequest<>(request);
            setToursApiKey(listRequest);
            DfyBaseResult<DfyToursListResponse> baseResult = diFengYunClient.getToursList(listRequest);
            if(baseResult == null || baseResult.getData() == null || ListUtils.isEmpty(baseResult.getData().getProductList())){
                log.error("笛风云跟团游列表返回空，request = {}", JSON.toJSONString(listRequest));
                return null;
            }
            return baseResult;
        } catch (Exception e) {
            log.error("笛风云获取跟团游列表异常，", e);
            return null;
        }
    }

    @Override
    public DfyBaseResult<DfyToursDetailResponse> getToursDetail(String productId){
        DfyToursDetailRequest request = new DfyToursDetailRequest();
        request.setProductId(Integer.valueOf(productId));
        DfyBaseRequest<DfyToursDetailRequest> detailRequest = new DfyBaseRequest<>(request);
        setToursApiKey(detailRequest);
        return diFengYunClient.getToursDetail(detailRequest);
    }

    @Override
    public DfyBaseResult<DfyToursDetailResponse> getToursMultiDetail(String productId){
        DfyToursDetailRequest request = new DfyToursDetailRequest();
        request.setProductId(Integer.valueOf(productId));
        DfyBaseRequest<DfyToursDetailRequest> detailRequest = new DfyBaseRequest<>(request);
        setToursApiKey(detailRequest);
        DfyBaseResult<DfyToursDetailResponse> baseResult = diFengYunClient.getToursMultiDetail(detailRequest);
        return baseResult;
    }

    @Override
    public DfyBaseResult<List<DfyToursCalendarResponse>> getToursCalendar(DfyToursCalendarRequest request){
        DfyBaseRequest<DfyToursCalendarRequest> detailRequest = new DfyBaseRequest<>(request);
        setToursApiKey(detailRequest);
        DfyBaseResult<List<DfyToursCalendarResponse>> baseResult = diFengYunClient.getToursCalendar(detailRequest);
        return baseResult;
    }

    private void setToursApiKey(DfyBaseRequest request){
        String apiKey = ConfigGetter.getByFileItemString(ConfigConstants.CONFIG_FILE_DIFENGYUN, CONFIG_ITEM_API_TOURS_KEY);
        String secretKey = ConfigGetter.getByFileItemString(ConfigConstants.CONFIG_FILE_DIFENGYUN,CONFIG_ITEM_API_TOURS_SECRET_KEY);
        request.setSecretKey(secretKey);
        request.setApiKey(apiKey);
    }

    @Override
    public boolean syncToursList(DfyToursListRequest request){
        DfyBaseResult<DfyToursListResponse> baseResult = getToursList(request);
        if(baseResult == null){
            return false;
        }
        List<DfyProductInfo> productInfos = baseResult.getData().getProductList();
        productInfos.forEach(p -> syncToursDetail(p.getProductId(), PRODUCT_SYNC_MODE_UNLIMITED));
        return true;
    }

    @Override
    public boolean syncToursList(DfyToursListRequest request, int syncMode){
        DfyBaseResult<DfyToursListResponse> baseResult = getToursList(request);
        if(baseResult == null){
            return false;
        }
        List<DfyProductInfo> productInfos = baseResult.getData().getProductList();
        productInfos.forEach(p -> syncToursDetail(p.getProductId(), syncMode));
        return true;
    }

    @Override
    public void syncToursDetail(String productId, int syncMode) {
        DfyBaseResult<DfyToursDetailResponse> baseResult = getToursDetail(productId);
        if (baseResult == null) {
            log.error("笛风云跟团游详情没有返回数据，productId={}", productId);
            return;
        }
        if(baseResult.getData() == null){
            log.error("笛风云跟团游详情返回data为空，productId={}", productId);
            // 返回成功码和数据权限不足认为下线
            if(Arrays.asList("231000", "350204").contains(baseResult.getErrorCode())){
                // 笛风云的产品下线就不会返回，所以没拿到就认为已下线，当data为空并且code=231000、350204才认为下线，其它情况可能是接口异常，防止误下线
                List<ProductPO> productPOs = productDao.getBySupplierProductIdAndSupplierId(productId, Constants.SUPPLIER_CODE_DFY_TOURS);
                if (ListUtils.isNotEmpty(productPOs)) {
                    for (ProductPO productPO : productPOs) {
                        productDao.updateStatusByCode(productPO.getCode(), Constants.PRODUCT_STATUS_INVALID);
                        dynamicProductItemService.refreshItemByProductCode(Lists.newArrayList(productPO.getCode()));
                        log.info("笛风云跟团游产品详情返回空，产品已下线，productCode = {}", productPO.getCode());
                    }
                }
            }
            return;
        }
        DfyToursDetailResponse dfyToursDetail = baseResult.getData();
        if (dfyToursDetail.getBrandId() == null) {
            log.error("笛风云跟团游产品{}不是牛人专线[{}]，跳过。。", productId, dfyToursDetail.getBrandName());
            return;
        }
        if (ListUtils.isEmpty(dfyToursDetail.getDepartCitys())) {
            log.error("笛风云跟团游产品{}没有出发城市，跳过。。", productId);
            return;
        }
        if (dfyToursDetail.getJourneyInfo() == null) {
            log.error("笛风云跟团游产品{}没有行程信息，跳过。。", productId);
            return;
        }
        if(ListUtils.isNotEmpty(dfyToursDetail.getDesPoiNameList())){
            DfyPosition position = dfyToursDetail.getDesPoiNameList().stream().filter(d ->
                    StringUtils.isNotBlank(d.getDesCountryName())
                            && !StringUtils.equals("中国", d.getDesCountryName())).findAny().orElse(null);
            if(position != null){
                log.error("境外产品，跳过。。包含境外目的地国家={}", position.getDesCountryName());
                return;
            }
        }
        ProductItemPO productItem = DfyToursConverter.convertToProductItemPO(dfyToursDetail, productId);
        ProductItemPO productItemPO = productItemDao.selectByCode(productItem.getCode());

        if (productItemPO == null) {
            productItem.setCreateTime(MongoDateUtils.handleTimezoneInput(new Date()));
            // 笛风云跟团游默认审核通过
            productItem.setAuditStatus(Constants.VERIFY_STATUS_PASSING);
            productItem.setOperator(Constants.SUPPLIER_CODE_DFY_TOURS);
            productItem.setOperatorName(Constants.SUPPLIER_NAME_DFY_TOURS);
        } else {
            // 比对信息
            commonService.compareProductItem(productItem);
        }
        productItem.setUpdateTime(MongoDateUtils.handleTimezoneInput(new Date()));
        // 保存副本
        commonService.saveBackupProductItem(productItem);
        if (productItemPO != null) {
            productItem.setAuditStatus(productItemPO.getAuditStatus());
            productItem.setProduct(productItemPO.getProduct());
            productItem.setImageDetails(productItemPO.getImageDetails());
            productItem.setImages(productItemPO.getImages());
            productItem.setMainImages(productItemPO.getMainImages());
            if(productItemPO.getCreateTime() == null){
                productItem.setCreateTime(MongoDateUtils.handleTimezoneInput(new Date()));
            } else {
                productItem.setCreateTime(MongoDateUtils.handleTimezoneInput(productItemPO.getCreateTime()));
            }
        }
        if(ListUtils.isEmpty(productItem.getImages()) && ListUtils.isEmpty(productItem.getMainImages())){
            log.info("{}没有列表图、轮播图，设置待审核", Constants.VERIFY_STATUS_WAITING);
            productItem.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
        }
        productItemDao.updateByCode(productItem);
        productItemPO = productItemDao.selectByCode(productItem.getCode());
        List<String> citys = Lists.newArrayList(productItemPO.getOriCityCode().split(","));
        List<String> cityNames = Lists.newArrayList(productItemPO.getOriCity().split(","));
        int i = 0;
        for (String city : citys) {
            ProductPO product = DfyToursConverter.convertToProductPO(dfyToursDetail, productId, city);
            product.setMainItemCode(productItemPO.getCode());
            product.setMainItem(productItemPO);
            product.setCity(productItemPO.getCity());
            product.setDesCity(productItemPO.getDesCity());
            product.setOriCity(cityNames.get(i++));
            product.setOriCityCode(city);
            product.setUpdateTime(MongoDateUtils.handleTimezoneInput(new Date()));
            product.setValidTime(MongoDateUtils.handleTimezoneInput(DateTimeUtil.trancateToDate(new Date())));
            product.setOperator(Constants.SUPPLIER_CODE_DFY_TOURS);
            product.setOperatorName(Constants.SUPPLIER_NAME_DFY_TOURS);
            ProductPO oldProduct = productDao.getByCode(product.getCode());
            // 是否只同步本地没有的产品
            if (PRODUCT_SYNC_MODE_ONLY_ADD == syncMode && oldProduct != null) {
                log.error("笛风云跟团游，本次同步不包括更新更新，跳过，supplierProductCode={}", product.getSupplierProductId());
                return;
            }
            if (PRODUCT_SYNC_MODE_ONLY_UPDATE == syncMode && oldProduct == null) {
                log.error("笛风云跟团游，本次同步不包括新增产品，跳过，supplierProductCode={}", product.getSupplierProductId());
                return;
            }
            ProductPO backup;
            if (oldProduct == null) {
                product.setCreateTime(MongoDateUtils.handleTimezoneInput(new Date()));
                // todo 暂时默认通过
//                product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
                product.setAuditStatus(Constants.VERIFY_STATUS_PASSING);
                product.setSupplierStatus(Constants.SUPPLIER_STATUS_OPEN);
                BackChannelEntry backChannelEntry = commonService.getSupplierById(product.getSupplierId());
                if(backChannelEntry == null
                        || backChannelEntry.getStatus() == null
                        || backChannelEntry.getStatus() != 1){
                    product.setSupplierStatus(Constants.SUPPLIER_STATUS_CLOSED);
                }
                if(backChannelEntry != null || StringUtils.isNotBlank(backChannelEntry.getAppSource())){
                    List<String> appFroms = Arrays.asList(backChannelEntry.getAppSource().split(","));
                    product.setAppFrom(appFroms);
                }
                backup = JSON.parseObject(JSON.toJSONString(product), ProductPO.class);
            } else {
                backup = JSON.parseObject(JSON.toJSONString(product), ProductPO.class);
                product.setSupplierStatus(oldProduct.getSupplierStatus());
                product.setAuditStatus(oldProduct.getAuditStatus());
                product.setRecommendFlag(oldProduct.getRecommendFlag());
                product.setAppFrom(oldProduct.getAppFrom());
                product.setDescriptions(oldProduct.getDescriptions());
                // 下面对比信息会处理这两个
//                product.setBookDescList(oldProduct.getBookDescList());
//                product.setBookNoticeList(oldProduct.getBookNoticeList());
                if(oldProduct.getCreateTime() == null){
                    product.setCreateTime(MongoDateUtils.handleTimezoneInput(new Date()));
                } else {
                    product.setCreateTime(MongoDateUtils.handleTimezoneInput(oldProduct.getCreateTime()));
                }
                commonService.compareToursProduct(product, oldProduct);
            }
            productDao.updateByCode(product);
            // 保存副本
            commonService.saveBackupProduct(backup);
            syncToursPrice(productId, city);
            if(dfyToursDetail.getJourneyInfo().getJourneyDescJson() != null
                    && dfyToursDetail.getJourneyInfo().getJourneyDescJson().getData() != null
                    && dfyToursDetail.getJourneyInfo().getJourneyDescJson().getData().getData() != null){
                HodometerPO hodometerPO = DfyToursConverter.convertToHodometerPO(dfyToursDetail.getJourneyInfo(), product.getCode());
                hodometerDao.updateByCode(hodometerPO);
                if(commonService.compareHodometer(hodometerPO)){
                    productDao.updateVerifyStatusByCode(product.getCode(), Constants.VERIFY_STATUS_WAITING);
                }
                // 保存行程副本
                commonService.saveBackupHodometer(hodometerPO);
            }
            dynamicProductItemService.refreshItemByProductCode(Lists.newArrayList(product.getCode()));
        }
    }

    @Override
    public void syncToursPrice(String supplierProductId, String city){
        String productCode = CommonUtils.genCodeBySupplier(Constants.SUPPLIER_CODE_DFY_TOURS, supplierProductId, city);
        ProductPO product = productDao.getByCode(productCode);
        if(product == null){
            log.error("同步笛风云跟团游价格失败，产品{}不存在", productCode);
            return;
        }
        DfyToursCalendarRequest calendarRequest = new DfyToursCalendarRequest();
        calendarRequest.setProductId(Integer.valueOf(supplierProductId));
        calendarRequest.setDepartCityCode(Integer.valueOf(city));
        DfyBaseResult<List<DfyToursCalendarResponse>> priceBaseResult = getToursCalendar(calendarRequest);
        if (priceBaseResult == null || ListUtils.isEmpty(priceBaseResult.getData())){
            log.error("同步笛风云跟团游价格失败，产品码={}，接口没有返回数据", productCode);
            return;
        }
        PricePO pricePO = new PricePO();
        pricePO.setProductCode(product.getCode());
        pricePO.setSupplierProductId(product.getSupplierProductId());
        pricePO.setOperator(Constants.SUPPLIER_CODE_DFY_TOURS);
        pricePO.setOperatorName(Constants.SUPPLIER_NAME_DFY_TOURS);
        List<PriceInfoPO> priceInfoPOs = priceBaseResult.getData().stream().map(data -> {
            PriceInfoPO priceInfoPO = new PriceInfoPO();
            priceInfoPO.setSaleDate(MongoDateUtils.handleTimezoneInput(DateTimeUtil.parseDate(data.getDepartDate())));
            priceInfoPO.setSettlePrice(BigDecimal.valueOf(data.getDistributeAdultPrice() == null ? 0 : data.getDistributeAdultPrice()));
            priceInfoPO.setSalePrice(priceInfoPO.getSettlePrice());
            if(data.getStockSign() != null){
                switch (data.getStockSign()){
                    case DfyConstants.STOCK_TYPE_NOM:
                        priceInfoPO.setStock(data.getStockNum());
                        break;
                    case DfyConstants.STOCK_TYPE_UNLIMITED:
                        priceInfoPO.setStock(999);
                        break;
                    case DfyConstants.STOCK_TYPE_OFFLINE:
                        priceInfoPO.setStock(0);
                        break;
                }
            }
            if(data.getExcludeChildFlag() != null && data.getExcludeChildFlag() == 0){
                priceInfoPO.setChdSettlePrice(BigDecimal.valueOf(data.getDistributeChildPrice() == null ? 0 : data.getDistributeChildPrice()));
                priceInfoPO.setChdSalePrice(priceInfoPO.getChdSettlePrice());
            }
            priceInfoPO.setRoomDiffPrice(data.getRoomChargeprice() == null ? null : BigDecimal.valueOf(data.getRoomChargeprice()));
            priceInfoPO.setDeadline(data.getDeadlineTime());
            return priceInfoPO;
        }).collect(Collectors.toList());
        pricePO.setPriceInfos(priceInfoPOs);
        priceDao.updateByProductCode(pricePO);
        List<PriceInfoPO> priceList = priceInfoPOs.stream().sorted(Comparator.comparing(p -> p.getSaleDate().getTime())).filter(p ->
                p.getSaleDate().getTime() >= DateTimeUtil.trancateToDate(new Date()).getTime() &&
                        p.getSalePrice() != null && p.getSalePrice().compareTo(BigDecimal.valueOf(0)) == 1 &&
                        p.getStock() != null && p.getStock() > 0).collect(Collectors.toList());
        if(priceList.size() > 0){
            product.setValidTime(MongoDateUtils.handleTimezoneInput(DateTimeUtil.trancateToDate(new Date())));
            PriceInfoPO endPrice = priceList.get(priceList.size() - 1);
            if(StringUtils.isNotBlank(endPrice.getDeadline())){
                Date deadline = DateTimeUtil.parseDate(endPrice.getDeadline());
                if(deadline.getTime() >= DateTimeUtil.trancateToDate(new Date()).getTime()){
                    product.setInvalidTime(MongoDateUtils.handleTimezoneInput(deadline));
                }
            } else {
                product.setInvalidTime(MongoDateUtils.handleTimezoneInput(endPrice.getSaleDate()));
            }
            productDao.updateByCode(product);
        }
    }
}
