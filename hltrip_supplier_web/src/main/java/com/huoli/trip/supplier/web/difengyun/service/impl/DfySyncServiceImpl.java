package com.huoli.trip.supplier.web.difengyun.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.huoli.trip.common.constant.*;
import com.huoli.trip.common.entity.*;
import com.huoli.trip.common.entity.mpo.AddressInfo;
import com.huoli.trip.common.entity.mpo.DescInfo;
import com.huoli.trip.common.entity.mpo.groupTour.*;
import com.huoli.trip.common.entity.mpo.scenicSpotTicket.*;
import com.huoli.trip.common.entity.po.PassengerTemplatePO;
import com.huoli.trip.common.util.*;
import com.huoli.trip.supplier.api.DynamicProductItemService;
import com.huoli.trip.supplier.feign.client.difengyun.client.IDiFengYunClient;
import com.huoli.trip.supplier.self.difengyun.constant.DfyConstants;
import com.huoli.trip.supplier.self.difengyun.vo.*;
import com.huoli.trip.supplier.self.difengyun.vo.request.*;
import com.huoli.trip.supplier.self.difengyun.vo.response.*;
import com.huoli.trip.supplier.web.dao.*;
import com.huoli.trip.supplier.web.difengyun.convert.DfyTicketConverter;
import com.huoli.trip.supplier.web.difengyun.convert.DfyToursConverter;
import com.huoli.trip.supplier.web.difengyun.service.DfySyncService;
import com.huoli.trip.supplier.web.mapper.PassengerTemplateMapper;
import com.huoli.trip.supplier.web.service.CommonService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.huoli.trip.supplier.self.common.SupplierConstants.*;
import static com.huoli.trip.supplier.self.difengyun.constant.DfyConfigConstants.*;

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
    private ScenicSpotProductDao scenicSpotProductDao;

    @Autowired
    private ScenicSpotMappingDao scenicSpotMappingDao;

    @Autowired
    private ScenicSpotDao scenicSpotDao;

    @Autowired
    private ScenicSpotRuleDao scenicSpotRuleDao;

    @Autowired
    private ScenicSpotProductPriceDao scenicSpotProductPriceDao;

    @Autowired
    private GroupTourProductDao groupTourProductDao;

    @Autowired
    private GroupTourProductSetMealDao groupTourProductSetMealDao;

    @Autowired
    private ScenicSpotBackupDao scenicSpotBackupDao;

    @Autowired
    private ScenicSpotProductBackupDao scenicSpotProductBackupDao;

    @Autowired
    private GroupProductBackupDao groupProductBackupDao;

    @Autowired
    private PassengerTemplateMapper passengerTemplateMapper;

    private boolean imageChanged = false;

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
            } else {
                productItemDao.updateItemCoordinateByCode(oldProductItem.getCode(), newProductItem.getItemCoordinate());
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
        syncProduct(productId, productItemPO, PRODUCT_SYNC_MODE_UNLIMITED);
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
                log.error("笛风云，本次同步不包括更新产品，跳过，supplierProductCode={}", product.getSupplierProductId());
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
            commonService.checkProduct(productPO, DateTimeUtil.trancateToDate(new Date()));
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
                syncToursDetail(p.getProductId().toString(), PRODUCT_SYNC_MODE_UNLIMITED);
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
            log.info("{}没有列表图、轮播图，设置待审核", productItem.getCode());
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
            commonService.checkProduct(product, DateTimeUtil.trancateToDate(new Date()));
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



    // ==================================新结构==============================


    @Override
    public boolean syncScenicListV2(DfyScenicListRequest request){
        try {
            DfyBaseRequest<DfyScenicListRequest> listRequest = new DfyBaseRequest<>(request);
            DfyBaseResult<DfyScenicListResponse> baseResult = diFengYunClient.getScenicList(listRequest);
            if(baseResult != null && baseResult.getData() != null && ListUtils.isNotEmpty(baseResult.getData().getRows())){
                List<DfyScenic> scenics = baseResult.getData().getRows();
                scenics.forEach(s -> syncScenicDetailV2(s.getScenicId()));
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
    public void syncScenicDetailV2(String scenicId){
        DfyScenicDetailRequest detailRequest = new DfyScenicDetailRequest();
        detailRequest.setScenicId(scenicId);
        DfyBaseRequest detailBaseRequest = new DfyBaseRequest<>(detailRequest);
        DfyBaseResult<DfyScenicDetail> detailBaseResult = diFengYunClient.getScenicDetail(detailBaseRequest);
        if(detailBaseResult != null && detailBaseResult.getData() != null){
            DfyScenicDetail scenicDetail = detailBaseResult.getData();
            // 转本地结构
            ScenicSpotMPO newScenic = DfyTicketConverter.convertToScenicSpotMPO(scenicDetail);
            // 设置省市区
            commonService.setCity(newScenic);
            // 同时保存映射关系
            commonService.updateScenicSpotMapping(scenicDetail.getScenicId(), Constants.SUPPLIER_CODE_DFY, Constants.SUPPLIER_NAME_DFY, newScenic);
            ScenicSpotBackupMPO scenicSpotBackupMPO = scenicSpotBackupDao.getScenicSpotBySupplierScenicIdAndSupplierId(scenicDetail.getScenicId(), Constants.SUPPLIER_CODE_DFY);
            if(scenicSpotBackupMPO != null){
                ScenicSpotMPO backup = scenicSpotBackupMPO.getScenicSpotMPO();
                if((ListUtils.isNotEmpty(backup.getImages()) && ListUtils.isEmpty(newScenic.getImages()))
                        || (ListUtils.isEmpty(backup.getImages()) && ListUtils.isNotEmpty(newScenic.getImages()))){
                    imageChanged = true;
                } else if(ListUtils.isNotEmpty(backup.getImages()) && ListUtils.isNotEmpty(newScenic.getImages())){
                    if(backup.getImages().size() != newScenic.getImages().size()
                            || backup.getImages().stream().filter(i -> !newScenic.getImages().contains(i)).findAny().isPresent()){
                        imageChanged = true;
                    }
                }
            }
            // 更新备份
            commonService.updateScenicSpotMPOBackup(newScenic, scenicDetail.getScenicId(), Constants.SUPPLIER_CODE_DFY, scenicDetail);
            List<String> ticketIds = Lists.newArrayList();
            if(ListUtils.isNotEmpty(scenicDetail.getTicketList())){
                ticketIds.addAll(scenicDetail.getTicketList().stream().map(DfyTicket::getProductId).collect(Collectors.toList()));
            }
            if(ListUtils.isNotEmpty(scenicDetail.getDisTickets())){
                ticketIds.addAll(scenicDetail.getDisTickets().stream().map(DfyTicket::getProductId).collect(Collectors.toList()));
            }
            // todo 真正上线的时候要发开这里，现在只为了落景点数据
//            ticketIds.forEach(id -> syncProductV2(id));
        } else {
            log.error("笛风云门票详情返回空，request = {}", JSON.toJSONString(detailBaseRequest));
        }
    }

    @Override
    public void syncProductV2(String productId) {
        DfyTicketDetailRequest ticketDetailRequest = new DfyTicketDetailRequest();
        ticketDetailRequest.setProductId(Integer.valueOf(productId));
        DfyBaseRequest ticketDetailBaseRequest = new DfyBaseRequest<>(ticketDetailRequest);
        DfyBaseResult<DfyTicketDetail> ticketDetailDfyBaseResult = diFengYunClient.getTicketDetail(ticketDetailBaseRequest);

        if (ticketDetailDfyBaseResult != null && ticketDetailDfyBaseResult.getData() != null) {
            DfyTicketDetail dfyTicketDetail = ticketDetailDfyBaseResult.getData();

            ScenicSpotProductMPO scenicSpotProductMPO = scenicSpotProductDao.getBySupplierProductId(dfyTicketDetail.getProductId(), Constants.SUPPLIER_CODE_DFY);
            boolean fresh = false;
            ScenicSpotMPO scenicSpotMPO = null;
            List<String> changedFields = Lists.newArrayList();
            if(scenicSpotProductMPO == null){
                ScenicSpotMappingMPO scenicSpotMappingMPO = scenicSpotMappingDao.getScenicSpotByChannelScenicSpotIdAndChannel(dfyTicketDetail.getScenicId(), Constants.SUPPLIER_CODE_DFY);
                if(scenicSpotMappingMPO == null){
                    log.error("笛风云产品{}没有查到关联景点{}", dfyTicketDetail.getProductId(), dfyTicketDetail.getScenicId());
                    return;
                }
                scenicSpotMPO = scenicSpotDao.getScenicSpotById(scenicSpotMappingMPO.getScenicSpotId());
                if(scenicSpotMPO == null){
                    log.error("景点{}不存在", scenicSpotMPO.getId());
                    return;
                }
                scenicSpotProductMPO = new ScenicSpotProductMPO();
                scenicSpotProductMPO.setId(commonService.getId(BizTagConst.BIZ_SCENICSPOT_PRODUCT));
                scenicSpotProductMPO.setCreateTime(new Date());
                scenicSpotProductMPO.setScenicSpotId(scenicSpotMPO.getId());
                if(ListUtils.isNotEmpty(scenicSpotMPO.getImages())){
                    scenicSpotProductMPO.setImages(scenicSpotMPO.getImages());
                    scenicSpotProductMPO.setMainImage(scenicSpotMPO.getImages().get(0));
                }
                scenicSpotProductMPO.setIsDel(0);
                scenicSpotProductMPO.setSellType(1);
                scenicSpotProductMPO.setSupplierProductId(dfyTicketDetail.getProductId());
                scenicSpotProductMPO.setPayServiceType(0);
                scenicSpotProductMPO.setChannel(Constants.SUPPLIER_CODE_DFY);
                // 默认销售中
                scenicSpotProductMPO.setStatus(1);
                fresh = true;
            } else {
                if(imageChanged){
                    // 笛风云的产品图片取自景点
                    changedFields.add("images");
                    scenicSpotProductMPO.setImages(scenicSpotMPO.getImages());
                    if(StringUtils.isNotBlank(scenicSpotProductMPO.getMainImage())){
                        // 原来有值，现在是空的
                        if(ListUtils.isEmpty(scenicSpotMPO.getImages())){
                            changedFields.add("mainImage");
                        } else {
                            // 原来的图没有了，换一张
                            if(!scenicSpotMPO.getImages().contains(scenicSpotProductMPO.getMainImage())){
                                changedFields.add("mainImage");
                                scenicSpotProductMPO.setMainImage(scenicSpotMPO.getImages().get(0));
                            }
                        }
                    } else {
                        // 原来是空的，现在有值
                        if(ListUtils.isNotEmpty(scenicSpotMPO.getImages())){
                            changedFields.add("mainImage");
                            scenicSpotProductMPO.setMainImage(scenicSpotMPO.getImages().get(0));
                        }
                    }
                }
            }
            // 默认未删除
            scenicSpotProductMPO.setUpdateTime(new Date());
            // 目前更新供应商端信息全覆盖
            scenicSpotProductMPO.setName(dfyTicketDetail.getProductName());
            // 基础设置
            ScenicSpotProductBaseSetting baseSetting = new ScenicSpotProductBaseSetting();
            BackChannelEntry backChannelEntry = commonService.getSupplierById(scenicSpotProductMPO.getChannel());
            if(backChannelEntry != null || StringUtils.isNotBlank(backChannelEntry.getAppSource())){
                baseSetting.setAppSource(backChannelEntry.getAppSource());
            }
            // 默认当前
            baseSetting.setLaunchDateTime(new Date());
            // 默认及时
            baseSetting.setLaunchType(1);
            baseSetting.setStockCount(0);
            baseSetting.setCategoryCode("d_ss_ticket");
            scenicSpotProductMPO.setScenicSpotProductBaseSetting(baseSetting);
            // 交易设置
            ScenicSpotProductTransaction transaction = new ScenicSpotProductTransaction();
            if(StringUtils.isNotBlank(dfyTicketDetail.getIndate())){
                transaction.setAppointInDate(1);
                transaction.setAppointnType(2);
                transaction.setInDate(dfyTicketDetail.getIndate());
            }
            if(dfyTicketDetail.getAdvanceDay() != null){
                transaction.setBookBeforeDay(dfyTicketDetail.getAdvanceDay());
            }
            if(dfyTicketDetail.getAdvanceHour() != null){
                transaction.setBookBeforeTime(dfyTicketDetail.getAdvanceHour().toString());
            }
            scenicSpotProductMPO.setScenicSpotProductTransaction(transaction);
            scenicSpotProductMPO.setChangedFields(changedFields);
            ScenicSpotRuleMPO ruleMPO = saveRule(scenicSpotProductMPO, dfyTicketDetail, fresh);
            scenicSpotProductMPO.setRuleId(ruleMPO.getId());
            scenicSpotProductDao.saveProduct(scenicSpotProductMPO);
            String scenicSpotProductId = scenicSpotProductMPO.getId();
            String ruleId = ruleMPO.getId();
            savePrice(dfyTicketDetail, scenicSpotProductId, ruleId);

            ScenicSpotProductBackupMPO scenicSpotProductBackupMPO = new ScenicSpotProductBackupMPO();
            scenicSpotProductBackupMPO.setId(commonService.getId(BizTagConst.BIZ_SCENICSPOT_PRODUCT));
            scenicSpotProductBackupMPO.setScenicSpotProduct(scenicSpotProductMPO);
            scenicSpotProductBackupMPO.setOriginContent(JSON.toJSONString(dfyTicketDetail));
            scenicSpotProductBackupDao.saveScenicSpotProductBackup(scenicSpotProductBackupMPO);

            commonService.refreshList(0, scenicSpotProductMPO.getId(), 1, fresh);
            // 有重要信息更新需要通知
            if(ListUtils.isNotEmpty(scenicSpotProductMPO.getChangedFields()) || ListUtils.isNotEmpty(ruleMPO.getChangedFields()) || fresh){
                log.info("准备发订阅通知");
                // 添加订阅通知
                commonService.addScenicProductSubscribe(scenicSpotMPO, scenicSpotProductMPO, fresh);
            }
        } else {
            log.error("笛风云产品详情返回空，request = {}", JSON.toJSONString(ticketDetailBaseRequest));
            ScenicSpotProductMPO scenicSpotProductMPO = scenicSpotProductDao.getBySupplierProductId(productId, Constants.SUPPLIER_CODE_DFY);
            // 笛风云的产品下线就不会返回，所以没拿到就认为已下线，
            // 正常下线只是data为空，errorCode是231000，其它错误码说明是接口有异常，不下线产品，防止误下线
            if (scenicSpotProductMPO != null
                    && ticketDetailDfyBaseResult != null
                    && ticketDetailDfyBaseResult.getData() == null
                    && Arrays.asList("231000", "350204").contains(ticketDetailDfyBaseResult.getErrorCode())) {
                scenicSpotProductDao.updateStatusById(scenicSpotProductMPO.getId(), 3);
                log.info("笛风云产品详情返回空，产品已下线，productCode = {}", scenicSpotProductMPO.getId());
            }
        }
    }

    private void savePrice(DfyTicketDetail dfyTicketDetail, String scenicSpotProductId, String ruleId){
        if(ListUtils.isNotEmpty(dfyTicketDetail.getPriceCalendar())){
            Integer type = null;
            if(StringUtils.isNotBlank(dfyTicketDetail.getMpType())){
                switch (Integer.parseInt(dfyTicketDetail.getMpType())){
                    case DfyConstants.TICKET_TYPE_0:
                        type = TicketType.TICKET_TYPE_19.getCode();
                        break;
                    case DfyConstants.TICKET_TYPE_1:
                        type = TicketType.TICKET_TYPE_2.getCode();
                        break;
                    case DfyConstants.TICKET_TYPE_2:
                        type = TicketType.TICKET_TYPE_4.getCode();
                        break;
                    case DfyConstants.TICKET_TYPE_3:
                        type = TicketType.TICKET_TYPE_9.getCode();
                        break;
                    case DfyConstants.TICKET_TYPE_4:
                        type = TicketType.TICKET_TYPE_7.getCode();
                        break;
                    case DfyConstants.TICKET_TYPE_5:
                        type = TicketType.TICKET_TYPE_8.getCode();
                        break;
                    case DfyConstants.TICKET_TYPE_6:
                        type = TicketType.TICKET_TYPE_14.getCode();
                        break;
                    case DfyConstants.TICKET_TYPE_7:
                        type = TicketType.TICKET_TYPE_17.getCode();
                        break;
                    case DfyConstants.TICKET_TYPE_8:
                        type = TicketType.TICKET_TYPE_16.getCode();
                        break;
                    case DfyConstants.TICKET_TYPE_9:
                        type = TicketType.TICKET_TYPE_20.getCode();
                        break;
                    case DfyConstants.TICKET_TYPE_10:
                        type = TicketType.TICKET_TYPE_21.getCode();
                        break;
                    case DfyConstants.TICKET_TYPE_11:
                        type = TicketType.TICKET_TYPE_23.getCode();
                        break;
                    case DfyConstants.TICKET_TYPE_12:
                        type = TicketType.TICKET_TYPE_22.getCode();
                        break;
                    default:
                        type = TicketType.TICKET_TYPE_1.getCode();
                        break;
                }
            }
            Integer ticketKind = type;
            List<ScenicSpotProductPriceMPO> priceMPOs = scenicSpotProductPriceDao.getByProductId(scenicSpotProductId);
            dfyTicketDetail.getPriceCalendar().forEach(p -> {
                if(DateTimeUtil.parseDate(p.getDepartDate()).getTime() < DateTimeUtil.trancateToDate(new Date()).getTime()){
                    // 历史库存不更新
                    return;
                }
                ScenicSpotProductPriceMPO scenicSpotProductPriceMPO = priceMPOs.stream().filter(pm -> StringUtils.equals(pm.getStartDate(), p.getDepartDate())).findFirst().orElse(null);
                if(scenicSpotProductPriceMPO == null){
                    scenicSpotProductPriceMPO = new ScenicSpotProductPriceMPO();
                    scenicSpotProductPriceMPO.setId(commonService.getId(BizTagConst.BIZ_SCENICSPOT_PRODUCT));
                    scenicSpotProductPriceMPO.setScenicSpotProductId(scenicSpotProductId);
                    scenicSpotProductPriceMPO.setMerchantCode(dfyTicketDetail.getProductId());
                    scenicSpotProductPriceMPO.setScenicSpotRuleId(ruleId);
                    scenicSpotProductPriceMPO.setStartDate(p.getDepartDate());
                    scenicSpotProductPriceMPO.setEndDate(p.getDepartDate());
                    scenicSpotProductPriceMPO.setWeekDay("1,2,3,4,5,6,7");
                    if(ticketKind != null){
                        scenicSpotProductPriceMPO.setTicketKind(ticketKind.toString());
                    }
                }
                if(StringUtils.isNotBlank(p.getSalePrice())){
                    scenicSpotProductPriceMPO.setSellPrice(new BigDecimal(p.getSalePrice()));
                    scenicSpotProductPriceMPO.setSettlementPrice(scenicSpotProductPriceMPO.getSellPrice());
                }
                scenicSpotProductPriceMPO.setStock(99);
                scenicSpotProductPriceDao.saveScenicSpotProductPrice(scenicSpotProductPriceMPO);
            });
        }
    }

    private ScenicSpotRuleMPO saveRule(ScenicSpotProductMPO scenicSpotProductMPO, DfyTicketDetail dfyTicketDetail, boolean fresh){
        ScenicSpotRuleMPO ruleMPO;
        if(StringUtils.isBlank(scenicSpotProductMPO.getRuleId())){
            ruleMPO = new ScenicSpotRuleMPO();
            ruleMPO.setId(commonService.getId(BizTagConst.BIZ_SCENICSPOT_PRODUCT));
            ruleMPO.setRuleName("退改规则");
            ruleMPO.setScenicSpotId(scenicSpotProductMPO.getScenicSpotId());
            ruleMPO.setRuleCode(String.valueOf(System.currentTimeMillis() + (Math.random() * 1000)));
            ruleMPO.setIsCouponRule(0);
            ruleMPO.setChannel(scenicSpotProductMPO.getChannel());
            ruleMPO.setValid(1);
            ruleMPO.setCreateTime(new Date());
            ruleMPO.setRefundCondition(2);
        } else {
            ruleMPO = scenicSpotRuleDao.getScenicSpotRuleById(scenicSpotProductMPO.getRuleId());
        }
        ScenicSpotProductBackupMPO scenicSpotProductBackupMPO = scenicSpotProductBackupDao.getScenicSpotProductBackupByProductId(scenicSpotProductMPO.getId());
        ruleMPO.setChangeTicketAddress(dfyTicketDetail.getDrawAddress());
        if(dfyTicketDetail.getDrawType() != null){
            if(dfyTicketDetail.getDrawType() == 1){
                ruleMPO.setTicketType(1);
            } else if(dfyTicketDetail.getDrawType() == 8){
                ruleMPO.setTicketType(0);
            } else {
                log.error("不支持的门票类型drawType={}", dfyTicketDetail.getDrawType());
            }
        }
        if(dfyTicketDetail.getCustInfoLimit() != null){
            List<Integer> booker = Lists.newArrayList();
            booker.add(0);
            booker.add(1);
            if(dfyTicketDetail.getCustInfoLimit() > 3){
                booker.add(2);
            }
            ruleMPO.setTicketInfos(booker);
            List<Integer> traveller = Lists.newArrayList();
            if(Arrays.asList(3, 7).contains(dfyTicketDetail.getCustInfoLimit())){
                traveller.add(0);
                traveller.add(1);
            } else if(Arrays.asList(2, 6).contains(dfyTicketDetail.getCustInfoLimit())) {
                traveller.add(0);
                traveller.add(1);
                traveller.add(2);
            }
            if(ListUtils.isNotEmpty(traveller)){
                ruleMPO.setTravellerInfos(traveller);
                if(StringUtils.isNotBlank(dfyTicketDetail.getCertificateType())){
                    List<Integer> creds = Lists.newArrayList(dfyTicketDetail.getCertificateType().split(",")).stream().map(c -> {
                        switch (Integer.parseInt(c)){
                            case DfyConstants.CRED_TYPE_ID:
                                return Certificate.ID_CARD.getCode();
                            case DfyConstants.CRED_TYPE_PP:
                                return Certificate.PASSPORT.getCode();
                            case DfyConstants.CRED_TYPE_OF:
                                return Certificate.OFFICER.getCode();
                            case DfyConstants.CRED_TYPE_HK:
                                return Certificate.HKM_PASS.getCode();
                            case DfyConstants.CRED_TYPE_TW:
                                return Certificate.TW_CARD.getCode();
                            default:
                                // 其它类型直接舍弃（笛风云建议这样操作）
                                return Integer.MIN_VALUE;
                        }
                    }).distinct().filter(c -> c.intValue() != Integer.MIN_VALUE).collect(Collectors.toList());
                    ruleMPO.setTravellerTypes(creds);
                } else {
                    // 如果空的只支持身份证
                    ruleMPO.setTravellerTypes(Lists.newArrayList(Certificate.ID_CARD.getCode()));
                }
            }
        }
        if(dfyTicketDetail.getLimitNumHigh() != null){
            ruleMPO.setLimitBuy(1);
            ruleMPO.setMaxCount(dfyTicketDetail.getLimitNumHigh());
        }
        if(dfyTicketDetail.getAdmissionVoucher() != null){
            String code = dfyTicketDetail.getAdmissionVoucher().getAdmissionVoucherCode();
            try {
                if(StringUtils.isNotBlank(code)){
                    code = code.split(",")[0];
                    if(Integer.valueOf(code) < 300){
                        ruleMPO.setInType(1);
                    } else if(Integer.valueOf(code) >= 300){
                        ruleMPO.setInType(0);
                    }
                    if(Arrays.asList("202","301").contains(code)){
                        ruleMPO.setVoucherType(0);
                    } else if(Arrays.asList("206","303").contains(code)){
                        ruleMPO.setVoucherType(1);
                    } else if(Arrays.asList("203").contains(code)){
                        ruleMPO.setVoucherType(4);
                    } else {
                        ruleMPO.setVoucherType(5);
                        switch (code){
                            case "1":
                                ruleMPO.setCardType("实体票");
                                break;
                            case "201":
                                ruleMPO.setCardType("短信");
                                break;
                            case "204":
                                ruleMPO.setCardType("换票证");
                                break;
                            case "205":
                            case "302":
                                ruleMPO.setCardType("邮件");
                                break;
                            case "207":
                            case "304":
                                ruleMPO.setCardType("护照");
                                break;
                            case "208":
                            case "305":
                                ruleMPO.setCardType("港澳通行证");
                                break;
                            case "209":
                            case "306":
                                ruleMPO.setCardType("军官证");
                                break;
                            case "210":
                            case "307":
                                ruleMPO.setCardType("台胞证");
                                break;
                        }
                    }
                }
            } catch (Exception e) {
                log.error("转化入园方式失败，不影响继续执行，value = {}", code);
            }
        }

        ruleMPO.setUpdateTime(new Date());
        if(scenicSpotProductBackupMPO != null){
            List<String> changedFields = Lists.newArrayList();
            DfyTicketDetail backup = JSON.parseObject(scenicSpotProductBackupMPO.getOriginContent(), DfyTicketDetail.class);
            if(!StringUtils.equals(backup.getMpLossInfo(), dfyTicketDetail.getMpLossInfo())){
                changedFields.add("refundRuleDesc");
                ruleMPO.setRefundRuleDesc(dfyTicketDetail.getMpLossInfo());
            }
            if(!StringUtils.equals(backup.getInfo(), dfyTicketDetail.getInfo())){
                changedFields.add("supplementDesc");
                ruleMPO.setSupplementDesc(dfyTicketDetail.getInfo());
            }
            //  预定说明没有 dfyTicketDetail.bookNotice 加动态说明字段
            if(StringUtils.isNotBlank(dfyTicketDetail.getBookNotice())){
                DescInfo descInfo = new DescInfo();
                descInfo.setTitle("预订须知");
                descInfo.setContent(dfyTicketDetail.getBookNotice());
                if(StringUtils.isNotBlank(backup.getBookNotice())){
                    if(!StringUtils.equals(dfyTicketDetail.getBookNotice(), backup.getBookNotice())){
                        descInfo.setChangedFields(Lists.newArrayList("content"));
                    }
                } else {
                    descInfo.setChangedFields(Lists.newArrayList("content"));
                }
                ruleMPO.setDescInfos(Lists.newArrayList(descInfo));
            } else {
                if(ListUtils.isNotEmpty(ruleMPO.getDescInfos())){
                    ruleMPO.getDescInfos().removeIf(d -> StringUtils.equals(d.getTitle(), "预订须知"));
                }
            }
            ruleMPO.setChangedFields(changedFields);
        } else {
            ruleMPO.setRefundRuleDesc(dfyTicketDetail.getMpLossInfo());
            ruleMPO.setSupplementDesc(dfyTicketDetail.getInfo());
            DescInfo descInfo = new DescInfo();
            descInfo.setTitle("预订须知");
            descInfo.setContent(dfyTicketDetail.getBookNotice());
            ruleMPO.setDescInfos(Lists.newArrayList(descInfo));
        }
        scenicSpotRuleDao.saveScenicSpotRule(ruleMPO);
        return ruleMPO;
    }

    @Override
    public List<String> getSupplierProductIdsV2(){
        return scenicSpotProductDao.getSupplierProductIdByChannel(Constants.SUPPLIER_CODE_DFY);
    }

    @Override
    public List<String> getSupplierToursProductIdsV2(){
        return groupTourProductDao.getSupplierProductIdByChannel(Constants.SUPPLIER_CODE_DFY_TOURS);
    }

    @Override
    public boolean syncToursListV2(DfyToursListRequest request){
        DfyBaseResult<DfyToursListResponse> baseResult = getToursList(request);
        if(baseResult == null){
            return false;
        }
        List<DfyProductInfo> productInfos = baseResult.getData().getProductList();
        productInfos.forEach(p -> syncToursDetailV2(p.getProductId()));
        return true;
    }

    @Override
    public void syncToursDetailV2(String productId) {
        DfyBaseResult<DfyToursDetailResponse> baseResult = getToursDetail(productId);
        if (baseResult == null) {
            log.error("笛风云跟团游详情没有返回数据，productId={}", productId);
            return;
        }
        if(baseResult.getData() == null){
            log.error("笛风云跟团游详情返回data为空，productId={}", productId);
            if(Arrays.asList("231000", "350204").contains(baseResult.getErrorCode())){
                // 笛风云的产品下线就不会返回，所以没拿到就认为已下线，当data为空并且code=231000才认为下线，其它情况可能是接口异常，防止误下线
                GroupTourProductMPO groupTourProductMPO = groupTourProductDao.getTourProduct(productId, Constants.SUPPLIER_CODE_DFY_TOURS);
                if(groupTourProductMPO != null){
                    groupTourProductDao.updateStatus(productId, Constants.SUPPLIER_CODE_DFY_TOURS);
                    commonService.refreshList(1, groupTourProductMPO.getId(), 1, false);
                    log.info("笛风云跟团游产品详情返回空，产品已下线，productId = {}", productId);
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
        } else {
            for (DfyDepartCity departCity : dfyToursDetail.getDepartCitys()) {
                if (StringUtils.equals(departCity.getName(), "全国")) {
                    // 过滤全国这种产品，将来放到当地参团单独处理
                    log.info("过滤全国出发的产品。跳过。");
                    return;
                }
            }
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
        boolean add = false;
        GroupTourProductMPO groupTourProductMPO = groupTourProductDao.getTourProduct(productId, Constants.SUPPLIER_CODE_DFY_TOURS);
        if(groupTourProductMPO == null ){
            groupTourProductMPO = new GroupTourProductMPO();
            groupTourProductMPO.setId(commonService.getId(BizTagConst.BIZ_GROUP_TOUR_PRODUCT));
            groupTourProductMPO.setCreateTime(new Date());
            groupTourProductMPO.setSupplierProductId(productId);
            groupTourProductMPO.setMerchantCode(productId);
            groupTourProductMPO.setChannel(Constants.SUPPLIER_CODE_DFY_TOURS);
            add = true;
        } else {
            GroupTourProductSetMealBackupMPO backupMPO = groupProductBackupDao.getGroupProductBackupByProductId(groupTourProductMPO.getId());
            if (backupMPO != null) {
                List<String> changedFields = Lists.newArrayList();
                GroupTourProductMPO backupProduct = backupMPO.getGroupTourProductMPO();
                if ((ListUtils.isNotEmpty(backupProduct.getImages()) && ListUtils.isEmpty(dfyToursDetail.getProductPicList()))
                        || (ListUtils.isEmpty(backupProduct.getImages()) && ListUtils.isNotEmpty(dfyToursDetail.getProductPicList()))) {
                    changedFields.add("images");
                    changedFields.add("mainImage");
                    if (ListUtils.isEmpty(dfyToursDetail.getProductPicList())) {
                        groupTourProductMPO.setImages(null);
                        groupTourProductMPO.setMainImage(null);
                    } else {
                        groupTourProductMPO.setImages(dfyToursDetail.getProductPicList().stream().map(DfyImage::getPath).collect(Collectors.toList()));
                        groupTourProductMPO.setMainImage(groupTourProductMPO.getImages().get(0));
                    }
                } else if (ListUtils.isNotEmpty(backupProduct.getImages()) && ListUtils.isNotEmpty(dfyToursDetail.getProductPicList())) {
                    if (backupProduct.getImages().size() != dfyToursDetail.getProductPicList().size()
                            || backupProduct.getImages().stream().anyMatch(i ->
                            !dfyToursDetail.getProductPicList().stream().map(DfyImage::getPath).collect(Collectors.toList()).contains(i))) {
                        changedFields.add("images");
                        groupTourProductMPO.setImages(dfyToursDetail.getProductPicList().stream().map(DfyImage::getPath).collect(Collectors.toList()));
                        // 原来的图没有了，换一张
                        if (!dfyToursDetail.getProductPicList().stream().map(DfyImage::getPath).collect(Collectors.toList()).contains(backupProduct.getMainImage())) {
                            changedFields.add("mainImage");
                            groupTourProductMPO.setMainImage(groupTourProductMPO.getImages().get(0));
                        }
                    }
                }
                groupTourProductMPO.setChangedFields(changedFields);
            }
        }
        String name = dfyToursDetail.getProductName();
        String point = null;
        if(name.startsWith("<")){
            name = name.substring(1);
            if(name.indexOf(">") > 0){
                point = name.substring(name.indexOf(">") + 1);
                name = name.substring(0, name.indexOf(">"));
            }
        }
        groupTourProductMPO.setProductName(name);
        groupTourProductMPO.setHighlights(Lists.newArrayList(point));
        Integer goTfc = DfyToursConverter.convertToTraffic(dfyToursDetail.getTrafficGo());
        Integer backTfc = DfyToursConverter.convertToTraffic(dfyToursDetail.getTrafficBack());
        groupTourProductMPO.setGoTraffic(goTfc == null ? null : goTfc.toString());
        groupTourProductMPO.setBackTraffice(backTfc == null ? null : backTfc.toString());
        groupTourProductMPO.setIsDel(0);
        groupTourProductMPO.setUpdateTime(new Date());
        setCity(groupTourProductMPO, dfyToursDetail);
        GroupTourProductPayInfo productPayInfo = new GroupTourProductPayInfo();
        productPayInfo.setSellType(1);
        productPayInfo.setConfirmType(1);
        groupTourProductMPO.setGroupTourProductPayInfo(productPayInfo);

        GroupTourProductBaseSetting baseSetting = new GroupTourProductBaseSetting();
        baseSetting.setLaunchType(1);
        baseSetting.setLaunchDateTime(new Date());
        BackChannelEntry backChannelEntry = commonService.getSupplierById(groupTourProductMPO.getChannel());
        if(backChannelEntry != null || StringUtils.isNotBlank(backChannelEntry.getAppSource())){
            baseSetting.setAppSource(backChannelEntry.getAppSource());
        }
        groupTourProductMPO.setGroupTourProductBaseSetting(baseSetting);
        if (ListUtils.isEmpty(dfyToursDetail.getProductPicList())) {
            groupTourProductMPO.setImages(null);
            groupTourProductMPO.setMainImage(null);
        } else {
            groupTourProductMPO.setImages(dfyToursDetail.getProductPicList().stream().map(DfyImage::getPath).collect(Collectors.toList()));
            groupTourProductMPO.setMainImage(groupTourProductMPO.getImages().get(0));
        }
        // 创建默认的出行人模板
        String idInfo = String.join(",", (CharSequence) Arrays.asList(Certificate.ID_CARD.getCode(),
                Certificate.PASSPORT.getCode(), Certificate.OFFICER.getCode(), Certificate.HKM_PASS.getCode(), Certificate.TW_CARD.getCode()));
        String passengerInfo = "2,6,10";
        PassengerTemplatePO passengerTemplatePO = passengerTemplateMapper.getPassengerTemplateByCond(Constants.SUPPLIER_CODE_DFY_TOURS, 1, passengerInfo, idInfo);
        if(passengerTemplatePO == null){
            passengerTemplatePO = new PassengerTemplatePO();
            passengerTemplatePO.setChannel(Constants.SUPPLIER_CODE_DFY_TOURS);
            passengerTemplatePO.setCreateTime(new Date());
            passengerTemplatePO.setStatus(1);
            passengerTemplatePO.setIdInfo(idInfo);
            passengerTemplatePO.setPassengerInfo(passengerInfo);
            passengerTemplatePO.setPeopleLimit(1);
            passengerTemplateMapper.addPassengerTemplate(passengerTemplatePO);
        }
        groupTourProductMPO.setTravelerTemplateId(passengerTemplatePO.getId());
        // 笛风云没有退改
        groupTourProductDao.saveProduct(groupTourProductMPO);

        boolean setMealChanged = false;
        for (DfyDepartCity departCity : dfyToursDetail.getDepartCitys()) {
            DfyJourneyInfo journeyInfo = dfyToursDetail.getJourneyInfo();
            AddressInfo addressInfo = commonService.setCity(null, departCity.getName(), null);
            if(addressInfo != null && StringUtils.isNotBlank(addressInfo.getCityCode())
                    && StringUtils.isNotBlank(addressInfo.getCityName())){
            } else {
                log.error("产品={}没有查到城市={}，跳过。", groupTourProductMPO.getProductName(), departCity.getName());
                continue;
            }
            GroupTourProductSetMealMPO setMealMPO = groupTourProductSetMealDao.getSetMeal(groupTourProductMPO.getId(), addressInfo.getCityCode());
            boolean addSm = false;
            GroupTourProductSetMealBackupMPO backupMPO = null;
            if(setMealMPO == null){
                setMealMPO = new GroupTourProductSetMealMPO();
                setMealMPO.setId(commonService.getId(BizTagConst.BIZ_GROUP_TOUR_PRODUCT));
                setMealMPO.setConstInclude(journeyInfo.getCostInclude());
                setMealMPO.setCostExclude(journeyInfo.getCostExclude());
                setMealMPO.setBookNotices(DfyToursConverter.buildBookNoticeListV2(journeyInfo));
                addSm = true;
                setMealChanged = true;
            } else {
                backupMPO = groupProductBackupDao.getGroupProductBackupByProductId(groupTourProductMPO.getId());
                if(backupMPO != null){
                    List<String> changedFields = Lists.newArrayList();
                    GroupTourProductSetMealMPO backup = backupMPO.getGroupTourProductSetMealMPO();
                    if(!StringUtils.equals(backup.getConstInclude(), journeyInfo.getCostInclude())){
                        changedFields.add("constInclude");
                        setMealMPO.setConstInclude(journeyInfo.getCostInclude());
                        setMealChanged = true;
                    }
                    if(!StringUtils.equals(backup.getCostExclude(), journeyInfo.getCostExclude())){
                        changedFields.add("costExclude");
                        setMealMPO.setCostExclude(journeyInfo.getCostExclude());
                        setMealChanged = true;
                    }
                    List<DescInfo> newDesc = DfyToursConverter.buildBookNoticeListV2(journeyInfo);
                    if(ListUtils.isNotEmpty(backup.getBookNotices())){
                        if(ListUtils.isNotEmpty(newDesc)){
                            // 删除新数据中已经删掉的元素
                            backup.getBookNotices().removeIf(n ->
                                    !newDesc.stream().map(DescInfo::getTitle).anyMatch(t -> StringUtils.equals(n.getTitle(), t)));
                            newDesc.forEach(nd -> {
                                DescInfo descInfo = backup.getBookNotices().stream().filter(bn -> StringUtils.equals(bn.getTitle(), nd.getTitle())).findFirst().orElse(null);
                                // 新增的节点
                                if(descInfo == null){
                                    nd.setChangedFields(Lists.newArrayList("title", "content"));
                                } else {
                                    // 有变化的节点
                                    if(!StringUtils.equals(nd.getContent(), descInfo.getContent())){
                                        nd.setChangedFields(Lists.newArrayList("content"));
                                    }
                                }
                            });
                        }
                    } else {
                        // 所有节点都是新增的
                        if(ListUtils.isNotEmpty(newDesc)){
                            newDesc.forEach(d -> d.setChangedFields(Lists.newArrayList("title", "content")));
                        }
                    }
                    setMealMPO.setBookNotices(newDesc);
                    DfyToursDetailResponse dfyBackup = JSON.parseObject(backupMPO.getOriginContent(), DfyToursDetailResponse.class);
                    if(dfyBackup != null){
                        String journeyDesc = JSON.toJSONString(dfyBackup.getJourneyInfo().getJourneyDescJson());
                        if(!StringUtils.equals(journeyDesc, JSON.toJSONString(journeyInfo.getJourneyDescJson()))){
                            changedFields.add("groupTourTripInfos");
                        }
                    }
                    if(ListUtils.isNotEmpty(changedFields)){
                        setMealMPO.setChangedFields(changedFields);
                    }
                }
            }
            setMealMPO.setGroupTourProductId(groupTourProductMPO.getId());
            setMealMPO.setName(groupTourProductMPO.getProductName());
            setMealMPO.setTripDay(dfyToursDetail.getDuration());
            setMealMPO.setDepCode(addressInfo.getCityCode());
            setMealMPO.setDepName(addressInfo.getCityName());
            if(journeyInfo.getJourneyDescJson() != null && journeyInfo.getJourneyDescJson().getData() != null
                    && ListUtils.isNotEmpty(journeyInfo.getJourneyDescJson().getData().getData())){
                setMealMPO.setGroupTourTripInfos(journeyInfo.getJourneyDescJson().getData().getData().stream().map(j -> {
                    GroupTourTripInfo groupTourTripInfo = new GroupTourTripInfo();
                    if(journeyInfo.getJourneyDescJson().getType() == 1) {
                        groupTourTripInfo.setDay(j.getDay());
                    }
                    if(j.getTraffic() != null){
                        StringBuffer sb = new StringBuffer();
                        sb.append(j.getTraffic().getFrom());
                        if(ListUtils.isNotEmpty(j.getTraffic().getToList())){
                            j.getTraffic().getToList().forEach(t -> sb.append(t.getTo()));
                        }
                        groupTourTripInfo.setTitle(sb.toString());
                    }
                    List<GroupTourProductTripItem> items = Lists.newArrayList();
                    if(ListUtils.isNotEmpty(j.getModuleList())){
                        for (DfyJourneyDetail.JourneyModule journeyModule : j.getModuleList()) {
                            int type = journeyModule.getModuleTypeValue();
                            if (type == DfyConstants.MODULE_TYPE_SCENIC &&
                                    ListUtils.isNotEmpty(journeyModule.getScenicList())) {
                                for (DfyJourneyDetail.ModuleScenic scenic : journeyModule.getScenicList()) {
                                    GroupTourProductTripItem item = new GroupTourProductTripItem();
                                    item.setTime(journeyModule.getMoment());
                                    // 计算一下转换成hh:mm
                                    if(scenic.getTimes() != null){
                                        int hh = scenic.getTimes() / 60;
                                        int mm = scenic.getTimes() % 60;
                                        item.setPlayTime(String.format("%s:%s", hh >= 10 ? hh : String.format("0%s", hh), mm >= 10 ? mm : String.format("0%s", mm)));
                                    }
                                    item.setType(String.valueOf(TripModuleTypeEnum.MODULE_TYPE_SCENIC.getCode()));
                                    item.setPoiName(scenic.getTitle());
                                    item.setPoiDesc(scenic.getContent());
                                    if(ListUtils.isNotEmpty(scenic.getPicture())){
                                        item.setImages(scenic.getPicture().stream().map(DfyJourneyDetail.JourneyPicture::getUrl).collect(Collectors.toList()));
                                    }
                                    items.add(item);
                                }
                            }
                            if (type == DfyConstants.MODULE_TYPE_HOTEL
                                    && ListUtils.isNotEmpty(journeyModule.getHotelList())) {
                                GroupTourProductTripItem item = new GroupTourProductTripItem();
                                item.setType(String.valueOf(TripModuleTypeEnum.MODULE_TYPE_HOTEL.getCode()));
                                item.setGroupTourHotels(journeyModule.getHotelList().stream().map(h -> {
                                    GroupTourHotel groupTourHotel = new GroupTourHotel();
                                    groupTourHotel.setStar(h.getStarName());
                                    groupTourHotel.setDesc(h.getDescription());
                                    if(ListUtils.isNotEmpty(h.getRoom())){
                                        groupTourHotel.setRoomName(h.getRoom().stream().map(DfyJourneyDetail.ModuleHotelRoom::getTitle).collect(Collectors.joining(",")));
                                        groupTourHotel.setImages(h.getRoom().stream().flatMap(r -> r.getPicture().stream().map(DfyJourneyDetail.JourneyPicture::getUrl)).collect(Collectors.toList()));
                                        groupTourHotel.setDesc(String.format("%s<br>%s", StringUtils.isBlank(groupTourHotel.getDesc()) ? "" : groupTourHotel.getDesc(),
                                                h.getRoom().stream().filter(r ->
                                                        StringUtils.isNotBlank(r.getDescription())).map(r ->
                                                        String.format("%s<br>%s<br>", r.getTitle(), r.getDescription())).collect(Collectors.joining())));
                                    }
                                    return groupTourHotel;
                                }).collect(Collectors.toList()));
                                item.setTime(journeyModule.getMoment());
                                items.add(item);
                            }
                            if (type == DfyConstants.MODULE_TYPE_TRAFFIC && journeyModule.getTraffic() != null) {
                                GroupTourProductTripItem item = new GroupTourProductTripItem();
                                item.setType(String.valueOf(TripModuleTypeEnum.MODULE_TYPE_TRAFFIC.getCode()));
                                item.setPlayTime(journeyModule.getTraffic().getTimes() == null
                                        || journeyModule.getTraffic().getTimes() <= 0 ? null : journeyModule.getTraffic().getTimes().toString());
                                String means = "";
                                if(journeyModule.getTraffic().getMeansType() != null){
                                    switch (journeyModule.getTraffic().getMeansType()){
                                        case 2:
                                            means = "水飞";
                                            break;
                                        case 3:
                                            means = "内飞";
                                            break;
                                        case 4:
                                            means = "飞机";
                                            break;
                                        case 5:
                                            means = "快艇";
                                            break;
                                        case 6:
                                            means = "轮船";
                                            break;
                                        case 7:
                                            means = "汽车";
                                            break;
                                        case 8:
                                            means = "火车";
                                            break;
                                    }
                                }
                                if(StringUtils.isBlank(means)){
                                    item.setPoiName(String.format("从%s到%s", journeyModule.getTraffic().getFrom(), journeyModule.getTraffic().getTo()));
                                } else {
                                    item.setPoiName(String.format("从%s乘%s到%s", journeyModule.getTraffic().getFrom(), means, journeyModule.getTraffic().getTo()));
                                }
                                item.setPoiDesc(journeyModule.getDescription());
                                if(ListUtils.isNotEmpty(journeyModule.getPicture())){
                                    item.setImages(journeyModule.getPicture().stream().map(DfyJourneyDetail.JourneyPicture::getUrl).collect(Collectors.toList()));
                                }
                                item.setTime(journeyModule.getMoment());
                                items.add(item);
                            }

                            if (type == DfyConstants.MODULE_TYPE_FOOD && journeyModule.getFood() != null) {
                                // 餐饮类型管理后台必填，没有就不要这条记录
                                if(ListUtils.isNotEmpty(journeyModule.getFood().getHasList())
                                    && journeyModule.getFood().getHasList().stream().anyMatch(f -> f.getHas() == 1)){
                                    GroupTourProductTripItem item = new GroupTourProductTripItem();
                                    // 含餐
                                    item.setSubType("20");
                                    item.setTime(journeyModule.getMoment());
                                    item.setPlayTime(journeyModule.getFood().getTimes() == null ? null : journeyModule.getFood().getTimes().toString());
                                    item.setType(String.valueOf(TripModuleTypeEnum.MODULE_TYPE_FOOD.getCode()));
                                    item.setPoiName(journeyModule.getFood().getTitle());
                                    item.setPoiDesc(journeyModule.getDescription());
                                    if(ListUtils.isNotEmpty(journeyModule.getPicture())){
                                        item.setImages(journeyModule.getPicture().stream().map(DfyJourneyDetail.JourneyPicture::getUrl).collect(Collectors.toList()));
                                    }
                                    StringBuffer sb = new StringBuffer();
                                    journeyModule.getFood().getHasList().forEach(h -> {
                                        if(StringUtils.equals("breakfast", h.getType())){
                                            sb.append("早餐：");
                                        } else if(StringUtils.equals("lunch", h.getType())){
                                            sb.append("午餐：");
                                        } else if(StringUtils.equals("dinner", h.getType())){
                                            sb.append("晚餐：");
                                        }
                                        if(h.getHas() == 1){
                                            sb.append("含;");
                                        } else {
                                            sb.append("敬请自理;");
                                        }
                                    });
                                    item.setPoiName(sb.toString());
                                    item.setCostInclude(-1);
                                    items.add(item);
                                }
                            }
                            if (type == DfyConstants.MODULE_TYPE_SHOPPING && ListUtils.isNotEmpty(journeyModule.getShopList())) {
                                for (DfyJourneyDetail.ModuleShop moduleShop : journeyModule.getShopList()) {
                                    GroupTourProductTripItem item = new GroupTourProductTripItem();
                                    item.setTime(journeyModule.getMoment());
                                    item.setPoiName(moduleShop.getTitle());
                                    if (StringUtils.isNotBlank(moduleShop.getInstruction())) {
                                        item.setPoiDesc(moduleShop.getInstruction().replace("<pre>", "").replace("</pre>", ""));
                                    }
                                    if(StringUtils.isNotBlank(moduleShop.getProduct())){
                                        item.setPoiDesc(String.format("%s<br>%s", item.getPoiDesc(), moduleShop.getProduct()));
                                    }
                                    item.setPlayTime(moduleShop.getTimes() == null ? null : moduleShop.getTimes().toString());
                                    item.setType(String.valueOf(TripModuleTypeEnum.MODULE_TYPE_SHOPPING.getCode()));
                                    items.add(item);
                                }
                            }
                            if (type == DfyConstants.MODULE_TYPE_ACTIVITY && journeyModule.getActivity() != null) {
                                GroupTourProductTripItem item = new GroupTourProductTripItem();
                                item.setTime(journeyModule.getMoment());
                                if(ListUtils.isNotEmpty(journeyModule.getPicture())){
                                    item.setImages(journeyModule.getPicture().stream().map(DfyJourneyDetail.JourneyPicture::getUrl).collect(Collectors.toList()));
                                }
                                item.setType(String.valueOf(TripModuleTypeEnum.MODULE_TYPE_ACTIVITY.getCode()));
                                item.setPoiName(journeyModule.getActivity().getTitle());
                                item.setPlayTime(journeyModule.getActivity().getTimes() == null
                                        || journeyModule.getActivity().getTimes() <= 0 ? null : journeyModule.getActivity().getTimes().toString());
                                item.setPoiDesc(journeyModule.getDescription());
                                items.add(item);
                            }
                            if (type == DfyConstants.MODULE_TYPE_REMINDER && journeyModule.getRemind() != null) {
                                GroupTourProductTripItem item = new GroupTourProductTripItem();
                                item.setTime(journeyModule.getMoment());
                                if(ListUtils.isNotEmpty(journeyModule.getPicture())){
                                    item.setImages(journeyModule.getPicture().stream().map(DfyJourneyDetail.JourneyPicture::getUrl).collect(Collectors.toList()));
                                }
                                item.setType(String.valueOf(TripModuleTypeEnum.MODULE_TYPE_REMINDER.getCode()));
                                item.setPoiName(journeyModule.getRemind().getType());
                                if (StringUtils.isNotBlank(journeyModule.getRemind().getContent())) {
                                    item.setPoiDesc(journeyModule.getRemind().getContent().replace("<pre>", "").replace("</pre>", ""));
                                }
                                items.add(item);
                            }
                        }
                    }
                    groupTourTripInfo.setGroupTourProductTripItems(items);
                    return groupTourTripInfo;
                }).collect(Collectors.toList()));
            }
            setMealMPO.setGroupTourPrices(syncToursPriceV2(groupTourProductMPO.getSupplierProductId(), departCity.getCode()));
            groupTourProductSetMealDao.saveSetMeals(setMealMPO);
            commonService.refreshList(1, groupTourProductMPO.getId(), 1, add);
            GroupTourProductSetMealBackupMPO groupTourProductSetMealBackupMPO = new GroupTourProductSetMealBackupMPO();
            groupTourProductSetMealBackupMPO.setGroupTourProductMPO(groupTourProductMPO);
            groupTourProductSetMealBackupMPO.setGroupTourProductSetMealMPO(setMealMPO);
            groupTourProductSetMealBackupMPO.setId(BizTagConst.BIZ_GROUP_TOUR_PRODUCT);
            groupTourProductSetMealBackupMPO.setOriginContent(JSON.toJSONString(dfyToursDetail));
            groupProductBackupDao.saveGroupProductBackup(groupTourProductSetMealBackupMPO);
            if(ListUtils.isNotEmpty(groupTourProductMPO.getChangedFields()) || setMealChanged || add){
                // 添加订阅通知
                commonService.addToursProductSubscribe(groupTourProductMPO, add);
            }
        }
    }

    private void setCity(GroupTourProductMPO groupTourProductMPO, DfyToursDetailResponse dfyToursDetail){
        if(ListUtils.isNotEmpty(dfyToursDetail.getDepartCitys())){
            groupTourProductMPO.setDepInfos(dfyToursDetail.getDepartCitys().stream().map(d ->
                    commonService.setCity(null, d.getName(), null)).filter(a -> a != null).collect(Collectors.toList()));
        }
        if(ListUtils.isNotEmpty(dfyToursDetail.getDesPoiNameList())){
            groupTourProductMPO.setArrInfos(dfyToursDetail.getDesPoiNameList().stream().map(d ->
                    commonService.setCity(d.getDesProvinceName(), d.getDesCityName(), d.getDesCountyName())).collect(Collectors.toList()));
        }
    }

    @Override
    public List<GroupTourPrice> syncToursPriceV2(String supplierProductId, String city){
        DfyToursCalendarRequest calendarRequest = new DfyToursCalendarRequest();
        calendarRequest.setProductId(Integer.valueOf(supplierProductId));
        calendarRequest.setDepartCityCode(Integer.valueOf(city));
        DfyBaseResult<List<DfyToursCalendarResponse>> priceBaseResult = getToursCalendar(calendarRequest);
        if (priceBaseResult == null || ListUtils.isEmpty(priceBaseResult.getData())){
            log.error("同步笛风云跟团游价格失败，产品码={}，接口没有返回数据", supplierProductId);
            return null;
        }
        return priceBaseResult.getData().stream().map(data -> {
            GroupTourPrice groupTourPrice = new GroupTourPrice();
            groupTourPrice.setDate(data.getDepartDate());
            groupTourPrice.setAdtPrice(BigDecimal.valueOf(data.getDistributeAdultPrice() == null ? 0 : data.getDistributeAdultPrice()));
            groupTourPrice.setAdtSellPrice(groupTourPrice.getAdtPrice());
            if(data.getStockSign() != null){
                switch (data.getStockSign()){
                    case DfyConstants.STOCK_TYPE_NOM:
                        groupTourPrice.setAdtStock(data.getStockNum());
                        break;
                    case DfyConstants.STOCK_TYPE_UNLIMITED:
                        groupTourPrice.setAdtStock(999);
                        break;
                    case DfyConstants.STOCK_TYPE_OFFLINE:
                        groupTourPrice.setAdtStock(0);
                        break;
                }
            }
            if(data.getExcludeChildFlag() != null && data.getExcludeChildFlag() == 0){
                groupTourPrice.setChdPrice(BigDecimal.valueOf(data.getDistributeChildPrice() == null ? 0 : data.getDistributeChildPrice()));
                groupTourPrice.setChdSellPrice(groupTourPrice.getChdPrice());
            }
            groupTourPrice.setDiffPrice(data.getRoomChargeprice() == null ? null : BigDecimal.valueOf(data.getRoomChargeprice()));
            return groupTourPrice;
        }).collect(Collectors.toList());
    }
}
