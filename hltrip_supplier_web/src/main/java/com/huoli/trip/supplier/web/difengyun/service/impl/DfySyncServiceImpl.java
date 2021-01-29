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
import com.huoli.trip.supplier.web.dao.HodometerDao;
import com.huoli.trip.supplier.web.dao.PriceDao;
import com.huoli.trip.supplier.web.dao.ProductDao;
import com.huoli.trip.supplier.web.dao.ProductItemDao;
import com.huoli.trip.supplier.web.difengyun.convert.DfyTicketConverter;
import com.huoli.trip.supplier.web.difengyun.convert.DfyToursConverter;
import com.huoli.trip.supplier.web.difengyun.service.DfySyncService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.huoli.trip.supplier.self.difengyun.constant.DfyConfigConstants.*;
import static com.huoli.trip.supplier.self.difengyun.constant.DfyConstants.PRODUCT_SYNC_MODE_ONLY_ADD;
import static com.huoli.trip.supplier.self.difengyun.constant.DfyConstants.PRODUCT_SYNC_MODE_ONLY_UPDATE;

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
            ProductItemPO productItem = DfyTicketConverter.convertToProductItemPO(scenicDetail);
            ProductItemPO productItemPO = productItemDao.selectByCode(productItem.getCode());
            if(productItemPO == null){
                productItem.setCreateTime(MongoDateUtils.handleTimezoneInput(new Date()));
            }
            productItem.setUpdateTime(MongoDateUtils.handleTimezoneInput(new Date()));
            productItem.setOperator(Constants.SUPPLIER_CODE_DFY);
            productItem.setOperatorName(Constants.SUPPLIER_NAME_DFY);
            productItemDao.updateByCode(productItem);
            productItemPO = productItemDao.selectByCode(productItem.getCode());
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
                    syncProduct(dfyTicket.getProductId(), productItemPO, PRODUCT_SYNC_MODE_ONLY_ADD);
                }
            }
            // 产品同步有刷新。这里先不刷了。
//            dynamicProductItemService.refreshItemByCode(productItemPO.getCode());
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
            if(productPO == null){
                product.setCreateTime(MongoDateUtils.handleTimezoneInput(new Date()));
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
            productDao.updateByCode(product);
            dynamicProductItemService.refreshItemByProductCode(Lists.newArrayList(product.getCode()));
        } else {
            log.error("笛风云产品详情返回空，request = {}", JSON.toJSONString(ticketDetailBaseRequest));
            // 笛风云的产品下线就不会返回，所以没拿到就认为已下线
            String code = CommonUtils.genCodeBySupplier(Constants.SUPPLIER_CODE_DFY, productId);
            ProductPO productPO = productDao.getByCode(code);
            if(productPO != null){
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
            List<DfyProductNotice> productNotices = request.getProductIds();
            if(ListUtils.isEmpty(productNotices)){
                log.error("笛风云通知更新产品列表为空");
                return;
            }
            productNotices.forEach(p -> {
                if(p.getClassBrandParentId() == DfyConstants.BRAND_GROUP){
                    log.error("笛风云更新产品 {} 是跟团，跳过。", p.getProductId());
                    return;
                }
                // 如果只是更新状态直接在这里改就行
                if(p.getNoticeType() == DfyConstants.NOTICE_TYPE_INVALID || p.getNoticeType() == DfyConstants.NOTICE_TYPE_VALID){
                    ProductPO productPO = productDao.getByCode(CommonUtils.genCodeBySupplier(Constants.SUPPLIER_CODE_DFY, p.getProductId().toString()));
                    // 如果本地有就直接更新
                    if(productPO != null){
                        productPO.setStatus(p.getNoticeType() == DfyConstants.NOTICE_TYPE_INVALID ? Constants.PRODUCT_STATUS_INVALID : Constants.PRODUCT_STATUS_VALID);
                        productPO.setUpdateTime(MongoDateUtils.handleTimezoneInput(new Date()));
                        productDao.updateByCode(productPO);
                        dynamicProductItemService.refreshItemByProductCode(Lists.newArrayList(productPO.getCode()));
                        return;
                    }
                }
                // 如果本地没有就走同步产品流程
                syncProduct(p.getProductId().toString(), null);
            });
        } catch (Exception e) {
            log.error("笛风云接收通知更新产品异常，", e);
        }
    }

    @Override
    public List<ProductPO> getSupplierProductIds(Integer productType){
        return productDao.getSupplierProductIds(Constants.SUPPLIER_CODE_DFY, productType);
    }

    @Override
    public DfyBaseResult<DfyToursListResponse> getToursList(DfyToursListRequest request){
        try {
            DfyBaseRequest<DfyToursListRequest> listRequest = new DfyBaseRequest<>(request);
            setToursApiKey(listRequest);
            DfyBaseResult<DfyToursListResponse> baseResult = diFengYunClient.getToursList(listRequest);
            if(baseResult == null || baseResult.getData() == null || ListUtils.isNotEmpty(baseResult.getData().getProductList())){
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
        DfyBaseResult<DfyToursDetailResponse> baseResult = diFengYunClient.getToursDetail(detailRequest);
        if(baseResult == null || baseResult.getData() == null){
            log.error("笛风云跟团游详情没有返回数据，productId={}", productId);
            return null;
        }
        return baseResult;
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
        productInfos.forEach(p -> syncToursDetail(p.getProductId(), PRODUCT_SYNC_MODE_ONLY_ADD));
        return true;
    }

    @Override
    public void syncToursDetail(String productId, int syncMode) {
        DfyBaseResult<DfyToursDetailResponse> baseResult = getToursDetail(productId);
        if (baseResult == null) {
            // 笛风云的产品下线就不会返回，所以没拿到就认为已下线
            List<ProductPO> productPOs = productDao.getBySupplierProductIdAndSupplierId(productId, Constants.SUPPLIER_CODE_DFY_TOURS);
            if (ListUtils.isNotEmpty(productPOs)) {
                for (ProductPO productPO : productPOs) {
                    productDao.updateStatusByCode(productPO.getCode(), Constants.PRODUCT_STATUS_INVALID);
                    dynamicProductItemService.refreshItemByProductCode(Lists.newArrayList(productPO.getCode()));
                    log.info("笛风云跟团游产品详情返回空，产品已下线，productCode = {}", productPO.getCode());
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
        if (dfyToursDetail.getJourneyInfo() == null ||
                dfyToursDetail.getJourneyInfo().getJourneyDescJson() == null ||
                dfyToursDetail.getJourneyInfo().getJourneyDescJson().getData() == null ||
                ListUtils.isEmpty(dfyToursDetail.getJourneyInfo().getJourneyDescJson().getData().getData())) {
            log.error("笛风云跟团游产品{}没有行程信息，跳过。。", productId);
            return;
        }
        ProductItemPO productItem = DfyToursConverter.convertToProductItemPO(dfyToursDetail, productId);
        ProductItemPO productItemPO = productItemDao.selectByCode(productItem.getCode());
        if (productItemPO == null) {
            productItem.setCreateTime(MongoDateUtils.handleTimezoneInput(new Date()));
        }
        productItem.setUpdateTime(MongoDateUtils.handleTimezoneInput(new Date()));
        productItem.setOperator(Constants.SUPPLIER_CODE_DFY);
        productItem.setOperatorName(Constants.SUPPLIER_NAME_DFY);
        productItemDao.updateByCode(productItem);
        productItemPO = productItemDao.selectByCode(productItem.getCode());
        List<String> citys = Lists.newArrayList(productItemPO.getOriCityCode().split(","));
        for (String city : citys) {
            ProductPO product = DfyToursConverter.convertToProductPO(dfyToursDetail, productId, city);
            product.setMainItemCode(productItemPO.getCode());
            product.setMainItem(productItemPO);
            product.setCity(productItemPO.getCity());
            product.setDesCity(productItemPO.getDesCity());
            product.setOriCity(city);
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
            if (oldProduct == null) {
                product.setCreateTime(MongoDateUtils.handleTimezoneInput(new Date()));
            }
            product.setUpdateTime(MongoDateUtils.handleTimezoneInput(new Date()));
            product.setOperator(Constants.SUPPLIER_CODE_DFY);
            product.setOperatorName(Constants.SUPPLIER_NAME_DFY);
            product.setValidTime(MongoDateUtils.handleTimezoneInput(DateTimeUtil.trancateToDate(new Date())));
            productDao.updateByCode(product);
            syncToursPrice(productId, city);
            HodometerPO hodometerPO = DfyToursConverter.convertToHodometerPO(dfyToursDetail.getJourneyInfo(), product.getCode());
            hodometerDao.updateByCode(hodometerPO);
            dynamicProductItemService.refreshItemByProductCode(Lists.newArrayList(product.getCode()));
        }
    }

    @Override
    public void syncToursPrice(String supplierProductId, String city){
        String productCode = CommonUtils.genCodeBySupplier(Constants.SUPPLIER_CODE_DFY, supplierProductId, city);
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
        pricePO.setOperator(Constants.SUPPLIER_CODE_DFY);
        pricePO.setOperatorName(Constants.SUPPLIER_NAME_DFY);
        List<PriceInfoPO> priceInfoPOs = priceBaseResult.getData().stream().map(data -> {
            PriceInfoPO priceInfoPO = new PriceInfoPO();
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
        }
    }
}
