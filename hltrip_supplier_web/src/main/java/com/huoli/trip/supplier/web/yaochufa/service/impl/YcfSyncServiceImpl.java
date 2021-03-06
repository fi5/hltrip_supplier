package com.huoli.trip.supplier.web.yaochufa.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.huoli.trip.common.constant.BizTagConst;
import com.huoli.trip.common.constant.Certificate;
import com.huoli.trip.common.constant.Constants;
import com.huoli.trip.common.constant.ProductType;
import com.huoli.trip.common.entity.*;
import com.huoli.trip.common.entity.mpo.AddressInfo;
import com.huoli.trip.common.entity.mpo.DescInfo;
import com.huoli.trip.common.entity.mpo.hotel.HotelMPO;
import com.huoli.trip.common.entity.mpo.hotelScenicSpot.*;
import com.huoli.trip.common.entity.mpo.scenicSpotTicket.*;
import com.huoli.trip.common.entity.po.PassengerTemplatePO;
import com.huoli.trip.common.util.*;
import com.huoli.trip.common.vo.v2.ScenicSpotRuleCompare;
import com.huoli.trip.supplier.api.DynamicProductItemService;
import com.huoli.trip.supplier.api.YcfSyncService;
import com.huoli.trip.supplier.feign.client.yaochufa.client.IYaoChuFaClient;
import com.huoli.trip.supplier.self.difengyun.vo.DfyScenic;
import com.huoli.trip.supplier.self.difengyun.vo.DfyScenicDetail;
import com.huoli.trip.supplier.self.difengyun.vo.request.DfyBaseRequest;
import com.huoli.trip.supplier.self.difengyun.vo.request.DfyScenicDetailRequest;
import com.huoli.trip.supplier.self.difengyun.vo.request.DfyScenicListRequest;
import com.huoli.trip.supplier.self.difengyun.vo.response.DfyBaseResult;
import com.huoli.trip.supplier.self.difengyun.vo.response.DfyScenicListResponse;
import com.huoli.trip.supplier.self.yaochufa.constant.YcfConfigConstants;
import com.huoli.trip.supplier.self.yaochufa.constant.YcfConstants;
import com.huoli.trip.supplier.self.yaochufa.vo.*;
import com.huoli.trip.supplier.self.yaochufa.vo.basevo.YcfBaseRequest;
import com.huoli.trip.supplier.self.yaochufa.vo.basevo.YcfBaseResult;
import com.huoli.trip.supplier.web.dao.*;
import com.huoli.trip.supplier.web.mapper.PassengerTemplateMapper;
import com.huoli.trip.supplier.web.service.CommonService;
import com.huoli.trip.supplier.web.yaochufa.convert.YcfConverter;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.huoli.trip.common.constant.Constants.SUPPLIER_CODE_YCF;
import static com.huoli.trip.common.constant.Constants.SUPPLIER_NAME_YCF;


/**
 * ?????????<br/>
 * ?????????Copyright (c) 2011-2020<br>
 * ?????????????????????<br>
 * ??????????????????<br>
 * ?????????1.0<br>
 * ???????????????2020/6/28<br>
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

    @Autowired
    private ScenicSpotProductBackupDao scenicSpotProductBackupDao;

    @Autowired
    private HotelScenicProductDao hotelScenicProductDao;

    @Autowired
    private HotelScenicProductSetMealDao hotelScenicProductSetMealDao;

    @Autowired
    private HotelMappingDao hotelMappingDao;

    @Autowired
    private HotelDao hotelDao;

    @Autowired
    private HotelScenicProductBackupDao hotelScenicProductBackupDao;

    @Autowired
    private ScenicSpotBackupDao scenicSpotBackupDao;

    @Autowired
    private PassengerTemplateMapper passengerTemplateMapper;

    @Override
    public void syncProduct(List<YcfProduct> ycfProducts){
        if(ListUtils.isEmpty(ycfProducts)){
            log.error("????????????????????????????????????");
            return;
        }
        ycfProducts.forEach(ycfProduct -> {
            if(!filterProduct(ycfProduct)){
                return;
            }
            ProductPO productPO = YcfConverter.convertToProductPO(ycfProduct);
            List<ProductItemPO> productItemPOs = syncProductItem(ycfProduct.getProductItemIds());
            if(ListUtils.isEmpty(productItemPOs)){
                log.error("????????????????????????????????????");
                return;
            }
            int ycfPoiType = productItemPOs.get(0).getItemType();
            // ?????????????????????????????????????????????
            if(ListUtils.isEmpty(ycfProduct.getTicketList()) && ListUtils.isEmpty(ycfProduct.getRoomList())){
                productPO.setProductType(convertToProductType(ycfPoiType, false, ProductType.RESTAURANT.getCode()));
            }
            // ?????????????????????????????????????????????
            else if(ListUtils.isEmpty(ycfProduct.getRoomList()) && ListUtils.isEmpty(ycfProduct.getFoodList())){
                productPO.setProductType(convertToProductType(ycfPoiType, false, ProductType.SCENIC_TICKET.getCode()));
            } else {
                // ????????????????????????????????????1?????????????????????
                if(ListUtils.isNotEmpty(ycfProduct.getRoomList())){
                    productPO.setProductType(convertToProductType(ycfPoiType, true, ProductType.FREE_TRIP.getCode()));
                } else if(ListUtils.isNotEmpty(ycfProduct.getTicketList())){
                    productPO.setProductType(convertToProductType(ycfPoiType, true, ProductType.SCENIC_TICKET_PLUS.getCode()));
                } else {
                    log.error("?????????????????????productId={}??????????????????????????????????????????poi", ycfProduct.getProductID());
                    return;
                }
            }
            log.info("??????poi?????????");
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
            log.info("?????????={}", JSON.toJSONString(productItemPO));
            productPO.setMainItem(productItemPO);
            productPO.setCity(productItemPO.getCity());
            productPO.setOperator(Constants.SUPPLIER_CODE_YCF);
            productPO.setOperatorName(Constants.SUPPLIER_NAME_YCF);
            productPO.setUpdateTime(MongoDateUtils.handleTimezoneInput(new Date()));
            ProductPO exist = productDao.getBySupplierProductId(productPO.getSupplierProductId());
            ProductPO backup;
            if(exist == null){
                productPO.setCreateTime(MongoDateUtils.handleTimezoneInput(new Date()));
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
                backup = JSON.parseObject(JSON.toJSONString(productPO), ProductPO.class);
            } else {
                backup = JSON.parseObject(JSON.toJSONString(productPO), ProductPO.class);
                productPO.setAuditStatus(exist.getAuditStatus());
                productPO.setSupplierStatus(exist.getSupplierStatus());
                productPO.setRecommendFlag(exist.getRecommendFlag());
                productPO.setAppFrom(exist.getAppFrom());
                // ?????????????????????????????????
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
            commonService.saveBackupProduct(backup);
            commonService.checkProduct(productPO, DateTimeUtil.trancateToDate(new Date()));
        });
    }

    @Override
    public List<ProductItemPO> syncProductItem(List<String> productItemIds){
        log.info("????????????poi???id list = {}", JSON.toJSONString(productItemIds));
        if(ListUtils.isEmpty(productItemIds)){
            log.error("??????poi?????????poi id????????????");
            return null;
        }
        List<ProductItemPO> productItemPOs = Lists.newArrayList();
        YcfGetPoiRequest ycfGetPoiRequest = new YcfGetPoiRequest();
        ycfGetPoiRequest.setPoiIdList(productItemIds);
        YcfBaseRequest ycfBaseRequest = new YcfBaseRequest(ycfGetPoiRequest);
        log.info("?????????????????????(?????????)??????poi???????????????={}", JSON.toJSONString(ycfGetPoiRequest));
        YcfBaseResult<YcfGetPoiResponse> baseResult = yaoChuFaClient.getPoi(ycfBaseRequest);
        log.info("?????????(?????????)??????poi?????????????????????={}", JSON.toJSONString(baseResult));
        if(baseResult.getSuccess() && baseResult.getStatusCode() == YcfConstants.RESULT_CODE_SUCCESS){
            YcfGetPoiResponse response = baseResult.getData();
            if(response == null){
                log.error("??????poi?????????????????????????????????????????????data");
                return null;
            }
            List<YcfProductItem> ycfProductItems = response.getPoiList();
            if(ListUtils.isEmpty(ycfProductItems)){
                log.error("??????poi?????????????????????????????????????????????poi??????");
                return null;
            }
            ycfProductItems.forEach(item -> {
                try {
                    ProductItemPO productItemPO = YcfConverter.convertToProductItemPO(item);
                    ProductItemPO exist = productItemDao.selectByCode(productItemPO.getCode());
                    // ???????????????????????????
                    if(exist == null){
                        productItemPO.setCreateTime(MongoDateUtils.handleTimezoneInput(new Date()));
                        productItemPO.setAuditStatus(Constants.VERIFY_STATUS_PASSING);
                        try {
                            commonService.saveBackupProductItem(productItemPO);
                        } catch (Exception e) {
                            log.error("??????{}???????????????", productItemPO.getCode(), e);
                        }
                        if(ListUtils.isEmpty(productItemPO.getImages()) && ListUtils.isEmpty(productItemPO.getMainImages())){
                            log.info("{}?????????????????????????????????????????????", Constants.VERIFY_STATUS_WAITING);
                            productItemPO.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
                        }
                        productItemPO.setUpdateTime(MongoDateUtils.handleTimezoneInput(new Date()));
                        productItemPO.setOperator(Constants.SUPPLIER_CODE_YCF);
                        productItemPO.setOperatorName(Constants.SUPPLIER_NAME_YCF);
                        productItemDao.updateByCode(productItemPO);
                    }
                    productItemPOs.add(productItemPO);
                } catch (Exception e) {
                    log.error("poi???????????????", e);
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
        // ?????????????????????30??????
        int round = diffDays / 30;
        int tail = diffDays % 30;
        // ????????????????????????+1???
        int n = round + (tail == 0 ? 0 : 1);
        // ????????????????????????n???=0
        n = n == 0 ? 1 : n;
        for(int i = 0; i < n; i++){
            YcfGetPriceRequest newRequest = new YcfGetPriceRequest();
            newRequest.setTraceId(request.getTraceId());
            newRequest.setFull(request.getFull());
            newRequest.setPartnerProductID(request.getPartnerProductID());
            newRequest.setProductID(request.getProductID());
            // ???????????????+1?????????
            newRequest.setStartDate(DateTimeUtil.formatDate(DateTimeUtil.addDay(DateTimeUtil.parseDate(request.getStartDate()), i * 30)));
            // ?????????30???
            newRequest.setEndDate(DateTimeUtil.formatDate(DateTimeUtil.addDay(DateTimeUtil.parseDate(newRequest.getStartDate()), 29)));
            if(i == n - 1){
                newRequest.setEndDate(request.getEndDate());
            }
            List<YcfPriceInfo> list;
            try {
                log.info("????????????????????????????????? = {} ????????? = {} ??? {} ", request.getPartnerProductID(), newRequest.getStartDate(), newRequest.getEndDate());
                list = syncPrice(newRequest);
                log.info("????????????????????????????????? = {} ????????? = {} ??? {} ", request.getPartnerProductID(), newRequest.getStartDate(), newRequest.getEndDate());
                // ???????????????1??????????????????200???
                Thread.sleep(310);
            } catch (Exception e) {
                log.info("?????????????????????????????? = {} ????????? = {} ??? {} ", request.getPartnerProductID(), newRequest.getStartDate(), newRequest.getEndDate(), e);
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
        // ??????????????????????????????????????????????????????????????????????????????????????????6????????????????????????1??????????????????????????????????????????????????????6??????????????????
        productDao.updateStatusByCode(productCode, Constants.PRODUCT_STATUS_VALID);
        ProductPO productPO = productDao.getByCode(productCode);
        commonService.checkProduct(productPO, DateTimeUtil.trancateToDate(new Date()));
        // ?????????????????????item???????????????(??????)
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
        // ?????????????????????item???????????????(??????)
        dynamicProductItemService.refreshItemByProductCode(Lists.newArrayList(productCode));
    }

    @Override
    public void syncFullPrice(YcfPrice ycfPrice){
        PricePO pricePO = YcfConverter.convertToPricePO(ycfPrice);
        ProductPO productPO = productDao.getBySupplierProductId(ycfPrice.getProductID());
        if(productPO != null){
            pricePO.setProductCode(productPO.getCode());
        } else {
            log.error("??????????????????????????????????????????id={} ????????????????????????", ycfPrice.getProductID());
            return;
        }
        priceDao.updateByProductCode(pricePO);
        // ?????????????????????item???????????????(??????)
        dynamicProductItemService.refreshItemByCode(productPO.getMainItemCode());
    }

    private List<YcfPriceInfo> syncPrice(YcfGetPriceRequest request){
        YcfBaseRequest ycfBaseRequest = new YcfBaseRequest(request);
        log.info("?????????????????????(?????????)???????????????????????????={}", JSON.toJSONString(request));
        YcfBaseResult<YcfGetPriceResponse> baseResult = yaoChuFaClient.getPrice(ycfBaseRequest);
        log.info("?????????(?????????)?????????????????????????????????={}", JSON.toJSONString(baseResult));
        if(baseResult.getSuccess() && baseResult.getStatusCode() == YcfConstants.RESULT_CODE_SUCCESS){
            YcfGetPriceResponse response = baseResult.getData();
            if(response == null){
                log.error("?????????????????????????????????????????????????????????data");
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
            log.info("???????????????????????????????????????id,???????????????");
            return false;
        }
        if(!YcfConstants.PRODUCT_TYPE_LIST.contains(ycfProduct.getProductType())){
            log.info("?????????????????????????????????????????????={}", ycfProduct.getProductType());
            return false;
        }
        if(ListUtils.isEmpty(ycfProduct.getFoodList()) &&
                ListUtils.isEmpty(ycfProduct.getRoomList()) &&
                ListUtils.isEmpty(ycfProduct.getTicketList())){
            log.info("???????????????????????????????????????????????????????????????");
            return false;
        }
        // ??????????????? ?????? ??????????????????????????????????????????????????????
        if(ycfProduct.getProductType() == YcfConstants.PRODUCT_TYPE_ROOM ||
                (ListUtils.isEmpty(ycfProduct.getFoodList()) && ListUtils.isEmpty(ycfProduct.getTicketList()))){
            log.info("??????????????????????????????????????????");
            return false;
        }
        // ??????????????? ?????? ??????????????????????????????????????????????????????
        if(ycfProduct.getProductType() == YcfConstants.PRODUCT_TYPE_FOOD ||
                (ListUtils.isNotEmpty(ycfProduct.getFoodList()) &&
                        ListUtils.isEmpty(ycfProduct.getRoomList()) &&
                        ListUtils.isEmpty(ycfProduct.getTicketList()))){
            log.info("??????????????????????????????????????????");
            return false;
        }
        if(ycfProduct.getRoomChoiceNum() != null && ycfProduct.getRoomOptionNum() != null &&
                ycfProduct.getRoomChoiceNum().intValue() != ycfProduct.getRoomOptionNum().intValue()){
            log.info("?????????????????????????????????M???N?????????????????????");
            return false;
        }
        if(ycfProduct.getFoodChoiceNum() != null && ycfProduct.getFoodOptionNum() != null &&
                ycfProduct.getFoodChoiceNum().intValue() != ycfProduct.getFoodOptionNum().intValue()){
            log.info("?????????????????????????????????M???N?????????????????????");
            return false;
        }
        if(ycfProduct.getTicketChoiceNum() != null && ycfProduct.getTicketOptionNum() != null &&
                ycfProduct.getTicketChoiceNum().intValue() != ycfProduct.getTicketOptionNum().intValue()){
            log.info("?????????????????????????????????M???N?????????????????????");
            return false;
        }
        return true;
    }

    private boolean filterScenicProduct(YcfProduct ycfProduct){
        if(!filterProduct(ycfProduct)){
            return false;
        }

        return true;
    }

    /**
     * ????????????
     * ????????????poi????????????????????????????????????poi????????????????????????????????????
     * @param poiType ycf poi??????
     * @param pack ????????????
     * @param productType ???????????????????????????????????????????????????
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

    // ============================?????????============================

    @Override
    public void syncScenicProduct(List<YcfProduct> ycfProducts){
        if(ListUtils.isEmpty(ycfProducts)){
            log.error("????????????????????????????????????");
            return;
        }
        ycfProducts.forEach(ycfProduct -> {
            if(!filterProduct(ycfProduct)){
                return;
            }
            // ????????????????????????????????????????????????
            if(ycfProduct.getProductType() == YcfConstants.PRODUCT_TYPE_TICKET
                    && ListUtils.isEmpty(ycfProduct.getFoodList()) &&
                    ListUtils.isEmpty(ycfProduct.getRoomList()) &&
                    ListUtils.isNotEmpty(ycfProduct.getTicketList())){
                syncScenicProduct(ycfProduct);
//                suppScenic(ycfProduct);
            }
            // ???????????????????????????+???????????????????????????????????????
            else if(ycfProduct.getProductType() == YcfConstants.PRODUCT_TYPE_PACKAGE
                    && ListUtils.isNotEmpty(ycfProduct.getRoomList())
                    && (ListUtils.isNotEmpty(ycfProduct.getFoodList()) ||
                        ListUtils.isNotEmpty(ycfProduct.getTicketList()))){
                syncHotelScenicProduct(ycfProduct);
            }
        });
    }

    private void suppScenic(YcfProduct ycfProduct){
        List<String> scenicIds = ycfProduct.getTicketList().stream().map(YcfResourceTicket::getPoiId).collect(Collectors.toList());
        if(ListUtils.isEmpty(scenicIds)){
            log.error("???????????????{}????????????????????????", ycfProduct.getProductID());
            return;
        }
        List<YcfProductItem> ycfProductItems = getPoi(scenicIds);
        if(ListUtils.isEmpty(ycfProductItems)){
            return;
        }
        ycfProductItems.forEach(item -> {
            ScenicSpotMPO existScenic = scenicSpotDao.getScenicSpotByNameAndAddress(item.getPoiName(), null);
            if(existScenic != null){
                boolean b = false;
                if(StringUtils.isBlank(existScenic.getPhone()) && StringUtils.isNotBlank(item.getPhone())){
                    existScenic.setPhone(item.getPhone());
                    log.info("?????????????????????{}", existScenic.getId());
                    b = true;
                }
                if(StringUtils.isBlank(existScenic.getBriefDesc()) && StringUtils.isNotBlank(item.getDescription())){
                    existScenic.setBriefDesc(StringUtil.replaceImgSrc(StringUtil.delHTMLTag(item.getDescription())));
                    log.info("???????????????????????????{}", existScenic.getId());
                    b = true;
                }
                if(ListUtils.isNotEmpty(item.getCharacterrList())){
                    item.getCharacterrList().stream().filter(c -> c.getType() == 2).findAny().ifPresent(c -> {
                        if(StringUtils.isBlank(existScenic.getTraffic())){
                            existScenic.setTraffic(c.getDetail());
                        }
                    });
                    item.getCharacterrList().stream().filter(c -> c.getType() == 3).findAny().ifPresent(c -> {
                        if(StringUtils.isBlank(existScenic.getDetailDesc())){
                            existScenic.setDetailDesc(StringUtil.replaceImgSrc(StringUtil.delHTMLTag(c.getDetail())));
                        }
                    });
                    b = true;
                }
                if(b){
                    scenicSpotDao.saveScenicSpot(existScenic);
                    log.info("??????????????????????????????{},{}", existScenic.getId(), existScenic.getName());
                }
            }
        });

    }

    private void syncScenicProduct(YcfProduct ycfProduct){
        List<String> scenicIds = ycfProduct.getTicketList().stream().map(YcfResourceTicket::getPoiId).collect(Collectors.toList());
        if(ListUtils.isEmpty(scenicIds)){
            log.error("???????????????{}????????????????????????", ycfProduct.getProductID());
            return;
        }
        syncScenic(scenicIds);
        ScenicSpotProductMPO scenicSpotProductMPO = scenicSpotProductDao.getBySupplierProductId(ycfProduct.getProductID(), SUPPLIER_CODE_YCF);
        ScenicSpotMPO scenicSpotMPO = null;
        boolean fresh = false;
        ScenicSpotProductBackupMPO backupMPO = null;
        ScenicSpotMappingMPO scenicSpotMappingMPO = scenicSpotMappingDao.getScenicSpotByChannelScenicSpotIdAndChannel(ycfProduct.getPoiId(), SUPPLIER_CODE_YCF);
        if(scenicSpotMappingMPO == null){
            log.error("???????????????{}????????????????????????{}", ycfProduct.getProductID(), ycfProduct.getPoiId());
            return;
        }
        scenicSpotMPO = scenicSpotDao.getScenicSpotById(scenicSpotMappingMPO.getScenicSpotId());
        if(scenicSpotMPO == null){
            log.error("??????{}?????????", scenicSpotMappingMPO.getId());
            return;
        }
        if(scenicSpotProductMPO == null){
            scenicSpotProductMPO = new ScenicSpotProductMPO();
            scenicSpotProductMPO.setId(commonService.getId(BizTagConst.BIZ_SCENICSPOT_PRODUCT));
            scenicSpotProductMPO.setCreateTime(new Date());
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
            fresh = true;
        } else {
            backupMPO = scenicSpotProductBackupDao.getScenicSpotProductBackupByProductId(scenicSpotProductMPO.getId());
            if(backupMPO != null){
                List<String> changedFields = Lists.newArrayList();
                ScenicSpotProductMPO backup = backupMPO.getScenicSpotProduct();
                if((ListUtils.isNotEmpty(backup.getImages()) && ListUtils.isEmpty(ycfProduct.getProductImageList()))
                        || (ListUtils.isEmpty(backup.getImages()) && ListUtils.isNotEmpty(ycfProduct.getProductImageList()))){
                    changedFields.add("images");
                    changedFields.add("mainImage");
                    if(ListUtils.isEmpty(ycfProduct.getProductImageList())){
                        scenicSpotProductMPO.setImages(null);
                        scenicSpotProductMPO.setMainImage(null);
                    } else {
                        scenicSpotProductMPO.setImages(ycfProduct.getProductImageList().stream().map(YcfImageBase::getImageUrl).collect(Collectors.toList()));
                        scenicSpotProductMPO.setMainImage(scenicSpotProductMPO.getImages().get(0));
                    }
                } else if(ListUtils.isNotEmpty(backup.getImages()) && ListUtils.isNotEmpty(ycfProduct.getProductImageList())){
                    if(backup.getImages().size() != ycfProduct.getProductImageList().size()
                            || backup.getImages().stream().anyMatch(i ->
                            !ycfProduct.getProductImageList().stream().map(YcfImageBase::getImageUrl).collect(Collectors.toList()).contains(i))){
                        changedFields.add("images");
                        scenicSpotProductMPO.setImages(ycfProduct.getProductImageList().stream().map(YcfImageBase::getImageUrl).collect(Collectors.toList()));
                        // ?????????????????????????????????
                        if(!ycfProduct.getProductImageList().stream().map(YcfImageBase::getImageUrl).collect(Collectors.toList()).contains(scenicSpotProductMPO.getMainImage())){
                            changedFields.add("mainImage");
                            scenicSpotProductMPO.setMainImage(scenicSpotProductMPO.getImages().get(0));
                        }
                    }
                }
                if(!StringUtils.equals(backup.getPcDescription(), ycfProduct.getProductDescription())){
                    scenicSpotProductMPO.setPcDescription(ycfProduct.getProductDescription());
                    changedFields.add("pcDescription");
                }
                scenicSpotProductMPO.setChangedFields(changedFields);
            }
        }
        scenicSpotProductMPO.setName(ycfProduct.getProductName());
        scenicSpotProductMPO.setUpdateTime(new Date());
        if(ycfProduct.getBookAheadMin() != null && ycfProduct.getBookAheadMin() > 0){
            ScenicSpotProductTransaction transaction = new ScenicSpotProductTransaction();
            int total = ycfProduct.getBookAheadMin();
            int day = total / (24 * 60);
            int newMin = total % (24 * 60);
            int hour = newMin / 60;
            int min = hour % 60;
            transaction.setBookBeforeDay(day);
            transaction.setBookBeforeTime(String.format("%s:%s", hour > 9 ? hour : String.format("0%s", hour), min > 9 ? min : String.format("0%s", min)));
            scenicSpotProductMPO.setScenicSpotProductTransaction(transaction);
        }
        ScenicSpotProductBaseSetting baseSetting = new ScenicSpotProductBaseSetting();
        BackChannelEntry backChannelEntry = commonService.getSupplierById(scenicSpotProductMPO.getChannel());
        if(backChannelEntry != null || StringUtils.isNotBlank(backChannelEntry.getAppSource())){
            baseSetting.setAppSource(backChannelEntry.getAppSource());
        }
        // ????????????
        baseSetting.setLaunchDateTime(new Date());
        if(StringUtils.isNotBlank(ycfProduct.getStartDate())){
            baseSetting.setLaunchDateTime(DateTimeUtil.parseDate(ycfProduct.getStartDate()));
        }
        // ????????????
        baseSetting.setLaunchType(1);
        baseSetting.setStockCount(0);
        baseSetting.setCategoryCode("d_ss_ticket");
        scenicSpotProductMPO.setScenicSpotProductBaseSetting(baseSetting);
        if(ycfProduct.getProductStatus() == YcfConstants.PRODUCT_STATUS_VALID){
            // ?????????????????????????????????
            if(scenicSpotProductMPO.getManuallyStatus() != 1){
                scenicSpotProductMPO.setStatus(1);
            }
        } else {
            scenicSpotProductMPO.setStatus(3);
        }
        ScenicSpotBackupMPO scenicSpotBackupMPO = scenicSpotBackupDao.getScenicSpotByScenicSpotId(scenicSpotMPO.getId());
        DescInfo bookNotice = new DescInfo();
        if(scenicSpotBackupMPO != null){
            YcfProductItem ycfProductItem = JSON.parseObject(scenicSpotBackupMPO.getOriginContent(), YcfProductItem.class);
            if(ListUtils.isNotEmpty(ycfProductItem.getCharacterrList())){
                ycfProductItem.getCharacterrList().stream().filter(c -> c.getType() == 1).findFirst().ifPresent(c -> {
                    bookNotice.setTitle("????????????");
                    bookNotice.setContent(c.getDetail());
                });
            }
        }
        if(StringUtils.isNotBlank(bookNotice.getContent())){
            if(ListUtils.isNotEmpty(scenicSpotProductMPO.getDescInfos())){
                scenicSpotProductMPO.getDescInfos().add(bookNotice);
            } else {
                scenicSpotProductMPO.setDescInfos(Lists.newArrayList(bookNotice));
            }
        }
        ScenicSpotRuleMPO ruleMPO = convertRule(scenicSpotProductMPO, ycfProduct, backupMPO);
        scenicSpotProductMPO.setRuleId(ruleMPO.getId());
        scenicSpotProductDao.saveProduct(scenicSpotProductMPO);
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
        syncPrice(scenicSpotProductMPO.getId(), ycfPriceInfos, ruleMPO.getId(), ycfProduct.getTicketType() == null ? null : ycfProduct.getTicketType().toString(), ycfProduct.getProductID());

        ScenicSpotProductBackupMPO scenicSpotProductBackupMPO = new ScenicSpotProductBackupMPO();
        scenicSpotProductBackupMPO.setId(commonService.getId(BizTagConst.BIZ_SCENICSPOT_PRODUCT));
        scenicSpotProductBackupMPO.setScenicSpotProduct(scenicSpotProductMPO);
        scenicSpotProductBackupMPO.setOriginContent(JSON.toJSONString(ycfProduct));
        scenicSpotProductBackupDao.saveScenicSpotProductBackup(scenicSpotProductBackupMPO);

        commonService.refreshList(0, scenicSpotProductMPO.getId(), 1, fresh);

        if(ListUtils.isNotEmpty(scenicSpotProductMPO.getChangedFields()) || ListUtils.isNotEmpty(ruleMPO.getChangedFields()) || fresh){
            commonService.addScenicProductSubscribe(scenicSpotMPO, scenicSpotProductMPO, fresh);
        }
    }

    private ScenicSpotRuleMPO convertRule(ScenicSpotProductMPO scenicSpotProductMPO, YcfProduct ycfProduct, ScenicSpotProductBackupMPO backupMPO){
        ScenicSpotRuleMPO ruleMPO;
        if(StringUtils.isBlank(scenicSpotProductMPO.getRuleId())){
            ruleMPO = new ScenicSpotRuleMPO();
            ruleMPO.setId(commonService.getId(BizTagConst.BIZ_SCENICSPOT_PRODUCT));
            ruleMPO.setRuleName("????????????");
            ruleMPO.setChannel(SUPPLIER_CODE_YCF);
            ruleMPO.setScenicSpotId(scenicSpotProductMPO.getScenicSpotId());
            ruleMPO.setIsCouponRule(0);
        } else {
            ruleMPO = scenicSpotRuleDao.getScenicSpotRuleById(scenicSpotProductMPO.getRuleId());
        }
        if(ycfProduct.getMaxNum() != null){
            ruleMPO.setLimitBuy(1);
            ruleMPO.setMaxCount(ycfProduct.getMaxNum());
        }
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
            // ??????
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
        if(backupMPO != null){
            List<String> ruleChanged = Lists.newArrayList();
            YcfProduct backup = JSON.parseObject(backupMPO.getOriginContent(), YcfProduct.class);
            if(!StringUtils.equals(backup.getRefundNote(), ycfProduct.getRefundNote())){
                ruleChanged.add("refundRuleDesc");
                ruleMPO.setRefundRuleDesc(StringUtil.delHTMLTag(ycfProduct.getRefundNote()));
            }
            if(!StringUtils.equals(backup.getFeeInclude(), ycfProduct.getFeeInclude())){
                ruleChanged.add("feeInclude");
                ruleMPO.setFeeInclude(ycfProduct.getFeeInclude());
            }
            if(StringUtils.isNotBlank(ycfProduct.getFeeExclude())){
                DescInfo descInfo = new DescInfo();
                descInfo.setTitle("???????????????");
                descInfo.setContent(ycfProduct.getFeeExclude());
                if(StringUtils.isNotBlank(backup.getFeeExclude())){
                    if(!StringUtils.equals(backup.getFeeExclude(), ycfProduct.getFeeExclude())){
                        descInfo.setChangedFields(Lists.newArrayList("content"));
                    }
                }
                ruleMPO.setDescInfos(Lists.newArrayList(descInfo));
            }
            ruleMPO.setChangedFields(ruleChanged);
        } else {
            ruleMPO.setRefundRuleDesc(StringUtil.delHTMLTag(ycfProduct.getRefundNote()));
            ruleMPO.setFeeInclude(StringUtil.delHTMLTag(ycfProduct.getFeeInclude()));
            if(StringUtils.isNotBlank(ycfProduct.getFeeExclude())){
                // ????????????????????? ???????????????
                DescInfo feeExclude = new DescInfo();
                feeExclude.setTitle("???????????????");
                feeExclude.setContent(ycfProduct.getFeeExclude());
                ruleMPO.setDescInfos(Lists.newArrayList(feeExclude));
            }
        }
        // ?????????????????????
        if(StringUtils.isNotBlank(ycfProduct.getGetTicketMode())){
            DescInfo descInfo = new DescInfo();
            descInfo.setTitle("??????????????????");
            descInfo.setContent(ycfProduct.getGetTicketMode());
            if(ruleMPO.getDescInfos() != null){
                ruleMPO.getDescInfos().addAll(Lists.newArrayList(descInfo));
            } else {
                ruleMPO.setDescInfos(Lists.newArrayList(descInfo));
            }
        }
//        return commonService.compareRule(scenicSpotProductMPO.getScenicSpotId(), scenicSpotProductMPO.getId(), ruleMPO);
        scenicSpotRuleDao.saveScenicSpotRule(ruleMPO);
        return ruleMPO;
    }

    private void syncHotelScenicProduct(YcfProduct ycfProduct){
        List<String> poiIds = Lists.newArrayList();
        if(ListUtils.isNotEmpty(ycfProduct.getTicketList())){
            List<String> scenicIds = ycfProduct.getTicketList().stream().map(YcfResourceTicket::getPoiId).collect(Collectors.toList());
            if(ListUtils.isEmpty(scenicIds)){
                log.error("???????????????{}????????????????????????", ycfProduct.getProductID());
                return;
            }
            poiIds.addAll(scenicIds);
        }
        List<String> hotelIds = ycfProduct.getRoomList().stream().map(YcfResourceRoom::getPoiId).collect(Collectors.toList());
        if(ListUtils.isEmpty(hotelIds)){
            log.error("???????????????{}????????????????????????", ycfProduct.getProductID());
            return;
        }
        syncScenic(poiIds);
        syncHotel(hotelIds);
        HotelScenicSpotProductMPO hotelScenicSpotProductMPO = hotelScenicProductDao.getBySupplierProductId(ycfProduct.getProductID(), SUPPLIER_CODE_YCF);
        HotelScenicSpotProductSetMealMPO setMealMPO = null;
        HotelScenicSpotProductBackupMPO backupMPO = null;
        boolean fresh = false;
        if(hotelScenicSpotProductMPO == null){
            hotelScenicSpotProductMPO = new HotelScenicSpotProductMPO();
            hotelScenicSpotProductMPO.setId(commonService.getId(BizTagConst.BIZ_HOTEL_SCENICSPORT_PRODUCT));
            hotelScenicSpotProductMPO.setCreateTime(new Date());
            hotelScenicSpotProductMPO.setIsDel(0);
            hotelScenicSpotProductMPO.setSupplierProductId(ycfProduct.getProductID());
            hotelScenicSpotProductMPO.setMerchantCode(ycfProduct.getProductID());
            hotelScenicSpotProductMPO.setChannel(SUPPLIER_CODE_YCF);
            if(ListUtils.isNotEmpty(ycfProduct.getProductImageList())){
                hotelScenicSpotProductMPO.setImages(ycfProduct.getProductImageList().stream().map(YcfImageBase::getImageUrl).collect(Collectors.toList()));
                hotelScenicSpotProductMPO.setMainImage(hotelScenicSpotProductMPO.getImages().get(0));
            }
            hotelScenicSpotProductMPO.setCategory("hotel_scenicSpot");
            hotelScenicSpotProductMPO.setComputerDesc(ycfProduct.getProductDescription());

            HotelScenicSpotProductPayInfo payInfo = new HotelScenicSpotProductPayInfo();
            payInfo.setSellType(1);
            payInfo.setConfirmType(1);
            payInfo.setThemeElements(1);
            // ?????????????????? ???????????????????????????????????????
            payInfo.setCostExclude(ycfProduct.getFeeExclude());
            if(StringUtils.isNotBlank(ycfProduct.getFeeInclude())){
                DescInfo descInfo = new DescInfo();
                descInfo.setTitle("???????????????");
                descInfo.setContent(ycfProduct.getFeeInclude());
                payInfo.setBookNotices(Lists.newArrayList(descInfo));
            }
            hotelScenicSpotProductMPO.setPayInfo(payInfo);
            HotelScenisSpotProductBaseSetting baseSetting = new HotelScenisSpotProductBaseSetting();
            baseSetting.setStockCount(0);
            baseSetting.setLaunchDateTime(StringUtils.isNotBlank(ycfProduct.getStartDate()) ? DateTimeUtil.parseDate(ycfProduct.getStartDate()) : new Date());
            if(baseSetting.getLaunchDateTime().getTime() > new Date().getTime()){
                baseSetting.setLaunchType(2);
                hotelScenicSpotProductMPO.setStatus(4);
            } else {
                baseSetting.setLaunchType(1);
                hotelScenicSpotProductMPO.setStatus(1);
            }
            baseSetting.setCategoryCode(hotelScenicSpotProductMPO.getCategory());
            BackChannelEntry backChannelEntry = commonService.getSupplierById(hotelScenicSpotProductMPO.getChannel());
            if(backChannelEntry != null || StringUtils.isNotBlank(backChannelEntry.getAppSource())){
                baseSetting.setAppSource(backChannelEntry.getAppSource());
            }
            hotelScenicSpotProductMPO.setBaseSetting(baseSetting);
            fresh = true;
        } else {
            List<HotelScenicSpotProductSetMealMPO> setMealMPOs = hotelScenicProductSetMealDao.getByProductId(hotelScenicSpotProductMPO.getId());
            if(ListUtils.isNotEmpty(setMealMPOs)){
                setMealMPO = setMealMPOs.get(0);
            }
            backupMPO = hotelScenicProductBackupDao.getHotelScenicSpotProductBackupByProductId(hotelScenicSpotProductMPO.getId());
            if(backupMPO != null){
                List<String> changedFields = Lists.newArrayList();
                HotelScenicSpotProductMPO backup = backupMPO.getProductMPO();
                if((ListUtils.isNotEmpty(backup.getImages()) && ListUtils.isEmpty(ycfProduct.getProductImageList()))
                        || (ListUtils.isEmpty(backup.getImages()) && ListUtils.isNotEmpty(ycfProduct.getProductImageList()))){
                    changedFields.add("images");
                    changedFields.add("mainImage");
                    if(ListUtils.isEmpty(ycfProduct.getProductImageList())){
                        hotelScenicSpotProductMPO.setImages(null);
                        hotelScenicSpotProductMPO.setMainImage(null);
                    } else {
                        hotelScenicSpotProductMPO.setImages(ycfProduct.getProductImageList().stream().map(YcfImageBase::getImageUrl).collect(Collectors.toList()));
                        hotelScenicSpotProductMPO.setMainImage(hotelScenicSpotProductMPO.getImages().get(0));
                    }
                } else if(ListUtils.isNotEmpty(backup.getImages()) && ListUtils.isNotEmpty(ycfProduct.getProductImageList())){
                    if(backup.getImages().size() != ycfProduct.getProductImageList().size()
                            || backup.getImages().stream().anyMatch(i ->
                            !ycfProduct.getProductImageList().stream().map(YcfImageBase::getImageUrl).collect(Collectors.toList()).contains(i))){
                        changedFields.add("images");
                        hotelScenicSpotProductMPO.setImages(ycfProduct.getProductImageList().stream().map(YcfImageBase::getImageUrl).collect(Collectors.toList()));
                        // ?????????????????????????????????
                        if(!ycfProduct.getProductImageList().stream().map(YcfImageBase::getImageUrl).collect(Collectors.toList()).contains(hotelScenicSpotProductMPO.getMainImage())){
                            changedFields.add("mainImage");
                            hotelScenicSpotProductMPO.setMainImage(hotelScenicSpotProductMPO.getImages().get(0));
                        }
                    }
                }
                hotelScenicSpotProductMPO.setChangedFields(changedFields);
                if(!StringUtils.equals(backup.getPayInfo().getCostExclude(), ycfProduct.getFeeExclude())){
                    hotelScenicSpotProductMPO.getPayInfo().setCostExclude(ycfProduct.getFeeExclude());
                    hotelScenicSpotProductMPO.getPayInfo().setChangedFields(Lists.newArrayList("costExclude"));
                }
                if(ListUtils.isNotEmpty(backup.getPayInfo().getBookNotices())){
                    DescInfo descInfo = backup.getPayInfo().getBookNotices().stream().filter(n -> StringUtils.equals(n.getTitle(), "???????????????")).findFirst().orElse(null);
                    if(descInfo == null){
                        // ??????
                        if(StringUtils.isNotBlank(ycfProduct.getFeeInclude())){
                            DescInfo newDesc = new DescInfo();
                            newDesc.setTitle("???????????????");
                            newDesc.setContent(ycfProduct.getFeeInclude());
                            newDesc.setChangedFields(Lists.newArrayList("title", "content"));
                            hotelScenicSpotProductMPO.getPayInfo().setBookNotices(Lists.newArrayList(newDesc));
                        }
                    } else {
                        if(StringUtils.isBlank(ycfProduct.getFeeInclude())){
                            // ??????
                            hotelScenicSpotProductMPO.getPayInfo().getBookNotices().removeIf(n -> StringUtils.equals(n.getTitle(), "???????????????"));
                        } else {
                            if(!StringUtils.equals(ycfProduct.getFeeInclude(), descInfo.getContent())){
                                if(ListUtils.isNotEmpty(hotelScenicSpotProductMPO.getPayInfo().getBookNotices())){
                                    // ??????
                                    hotelScenicSpotProductMPO.getPayInfo().getBookNotices().stream().filter(n ->
                                            StringUtils.equals(n.getTitle(), "???????????????")).findFirst().ifPresent(n -> {
                                        n.setContent(ycfProduct.getFeeInclude());
                                        n.setChangedFields(Lists.newArrayList("content"));
                                    });
                                } else {
                                    // ??????
                                    DescInfo newDesc = new DescInfo();
                                    newDesc.setTitle("???????????????");
                                    newDesc.setContent(ycfProduct.getFeeInclude());
                                    newDesc.setChangedFields(Lists.newArrayList("title", "content"));
                                    hotelScenicSpotProductMPO.getPayInfo().setBookNotices(Lists.newArrayList(newDesc));
                                }
                            }
                        }
                    }
                }
            }
        }
        ScenicSpotMappingMPO scenicSpotMapping = scenicSpotMappingDao.getScenicSpotByChannelScenicSpotIdAndChannel(ycfProduct.getPoiId(), SUPPLIER_CODE_YCF);
        if(scenicSpotMapping != null){
            ScenicSpotMPO scenicSpot = scenicSpotDao.getScenicSpotById(scenicSpotMapping.getScenicSpotId());
            if(scenicSpot != null){
                ScenicSpotBackupMPO scenicSpotBackupMPO = scenicSpotBackupDao.getScenicSpotByScenicSpotId(scenicSpot.getId());
                DescInfo bookNotice = new DescInfo();
                if(scenicSpotBackupMPO != null){
                    YcfProductItem ycfProductItem = JSON.parseObject(scenicSpotBackupMPO.getOriginContent(), YcfProductItem.class);
                    if(ListUtils.isNotEmpty(ycfProductItem.getCharacterrList())){
                        ycfProductItem.getCharacterrList().stream().filter(c -> c.getType() == 1).findFirst().ifPresent(c -> {
                            bookNotice.setTitle("????????????");
                            bookNotice.setContent(c.getDetail());
                        });
                    }
                }
                hotelScenicSpotProductMPO.setDescInfos(Lists.newArrayList(bookNotice));
            }
        }
        if(setMealMPO == null){
            setMealMPO = new HotelScenicSpotProductSetMealMPO();
            setMealMPO.setId(commonService.getId(BizTagConst.BIZ_HOTEL_SCENICSPORT_PRODUCT));
        }
        setMealMPO.setHotelScenicSpotProductId(hotelScenicSpotProductMPO.getId());
        setMealMPO.setName(ycfProduct.getProductName());
        setMealMPO.setBuyMax(ycfProduct.getMaxNum());
        setMealMPO.setBuyMin(ycfProduct.getMinNum());
        List<String> elements = Lists.newArrayList();
        List<HotelScenicSpotProductHotelElement> hotelElements = ycfProduct.getRoomList().stream().map(r -> {
            HotelMappingMPO hotelMappingMPO = hotelMappingDao.getHotelByChannelHotelIdAndChannel(r.getPoiId(), SUPPLIER_CODE_YCF);
            String hotelName = null;
            String hotelId = null;
            String hotelCityCode = null;
            String hotelCityName = null;
            if(hotelMappingMPO == null){
                // ???????????????????????????????????????poiid??????????????????????????????poiid???????????????????????????????????????????????????????????????????????????????????????poiid???????????????????????????????????????????????????????????????????????????
                ScenicSpotMappingMPO scenicSpotMappingMPO = scenicSpotMappingDao.getScenicSpotByChannelScenicSpotIdAndChannel(r.getPoiId(), SUPPLIER_CODE_YCF);
                if(scenicSpotMappingMPO != null) {
                    ScenicSpotMPO scenicSpotMPO = scenicSpotDao.getScenicSpotById(scenicSpotMappingMPO.getScenicSpotId());
                    if (scenicSpotMPO != null) {
                        hotelName = scenicSpotMPO.getName();
                        // id???????????????????????????????????????????????????????????????id
//                        hotelId = hotelMPO.getId();
                        hotelCityCode = scenicSpotMPO.getCityCode();
                        hotelCityName = scenicSpotMPO.getCity();
                    }
                } else {
                    log.error("???????????????{}????????????????????????{}", ycfProduct.getProductID(), r.getPoiId());
                }
//                return null;
            } else {
                HotelMPO hotelMPO = hotelDao.getById(hotelMappingMPO.getHotelId());
                if(hotelMPO == null){
                    log.error("??????{}?????????", hotelMappingMPO.getHotelId());
//                return null;
                } else {
                    hotelName = hotelMPO.getName();
                    hotelId = hotelMPO.getId();
                    hotelCityCode = hotelMPO.getCityCode();
                    hotelCityName = hotelMPO.getCity();
                }
            }

            HotelScenicSpotProductHotelElement hotelElement = new HotelScenicSpotProductHotelElement();
            hotelElement.setHotelName(hotelName);
            hotelElement.setHotelId(hotelId);
            hotelElement.setCityCode(hotelCityCode);
            hotelElement.setCityName(hotelCityName);
            hotelElement.setRelationHotelLib(1);
            hotelElement.setRoomName(r.getRoomName());
            hotelElement.setNight(r.getRoomBaseNight());
            return hotelElement;
        }).collect(Collectors.toList());
//        if(hotelElements.stream().anyMatch(h -> h == null)){
//            log.error("??????{}???????????????????????????", ycfProduct.getProductID());
//            return;
//        }
        setMealMPO.setHotelElements(hotelElements);
        if(ListUtils.isNotEmpty(hotelElements)){
            hotelScenicSpotProductMPO.setNight(hotelElements.get(0).getNight());
            elements.add("1");
        }
        if(ListUtils.isNotEmpty(ycfProduct.getTicketList())){
            List<HotelScenicSpotProductScenicSpotElement> scenicSpotElements = ycfProduct.getTicketList().stream().map(t -> {
                ScenicSpotMappingMPO scenicSpotMappingMPO = scenicSpotMappingDao.getScenicSpotByChannelScenicSpotIdAndChannel(t.getPoiId(), SUPPLIER_CODE_YCF);
                String scenicName = null;
                String scenicId = null;
                String scenicCityCode = null;
                String scenicCityName = null;
                // ?????????????????????
                if(scenicSpotMappingMPO == null){
                    HotelMappingMPO hotelMappingMPO = hotelMappingDao.getHotelByChannelHotelIdAndChannel(t.getPoiId(), SUPPLIER_CODE_YCF);
                    if(hotelMappingMPO != null){
                        HotelMPO hotelMPO = hotelDao.getById(hotelMappingMPO.getHotelId());
                        if(hotelMPO != null){
                            scenicName = hotelMPO.getName();
                            // id???????????????????????????????????????????????????????????????id
//                            scenicId = scenicSpotMPO.getId();
                            scenicCityCode = hotelMPO.getCityCode();
                            scenicCityName = hotelMPO.getCity();
                        }
                    } else {
                        log.error("???????????????{}????????????????????????{}", ycfProduct.getProductID(), ycfProduct.getPoiId());
                    }
//                    return null;
                } else {
                    ScenicSpotMPO scenicSpotMPO = scenicSpotDao.getScenicSpotById(scenicSpotMappingMPO.getScenicSpotId());
                    if(scenicSpotMPO == null){
                        log.error("??????{}?????????", scenicSpotMappingMPO.getScenicSpotId());
//                    return null;
                    } else {
                        scenicName = scenicSpotMPO.getName();
                        scenicId = scenicSpotMPO.getId();
                        scenicCityCode = scenicSpotMPO.getCityCode();
                        scenicCityName = scenicSpotMPO.getCity();
                    }
                }

                HotelScenicSpotProductScenicSpotElement scenicSpotElement = new HotelScenicSpotProductScenicSpotElement();
                scenicSpotElement.setScenicSpotId(scenicId);
                scenicSpotElement.setCityCode(scenicCityCode);
                scenicSpotElement.setScenicSpotName(scenicName);
                scenicSpotElement.setCityName(scenicCityName);
                scenicSpotElement.setRelationLib(1);
                scenicSpotElement.setCount(t.getTicketBaseNum());
                scenicSpotElement.setTicketKind(t.getTicketName());
                return scenicSpotElement;
            }).collect(Collectors.toList());
//            if(scenicSpotElements.stream().anyMatch(s -> s == null)){
//                log.error("??????{}???????????????????????????", ycfProduct.getProductID());
//                return;
//            }
            setMealMPO.setScenicSpotElements(scenicSpotElements);
            elements.add("2");
        }
        if(ListUtils.isNotEmpty(ycfProduct.getFoodList())){
            List<HotelScenicSpotProductRestaurantElement> restaurantElements = ycfProduct.getFoodList().stream().map(f -> {
                HotelScenicSpotProductRestaurantElement restaurantElement = new HotelScenicSpotProductRestaurantElement();
                restaurantElement.setCount(f.getFoodBaseNum());
                restaurantElement.setName(f.getFoodName());
                return restaurantElement;
            }).collect(Collectors.toList());
            setMealMPO.setRestaurantElements(restaurantElements);
            elements.add("4");
        }
        YcfGetPriceRequest request = new YcfGetPriceRequest();
        request.setPartnerProductID(hotelScenicSpotProductMPO.getId());
        request.setProductID(ycfProduct.getProductID());
        Integer days = ConfigGetter.getByFileItemInteger(YcfConfigConstants.CONFIG_FILE_NAME, YcfConfigConstants.TASK_SYNC_PRICE_INTERVAL);
        days = days == null ? Integer.valueOf(30) : days;
        String start = DateTimeUtil.formatDate(new Date());
        String end = DateTimeUtil.formatDate(DateTimeUtil.addDay(new Date(), days));
        request.setStartDate(start);
        request.setEndDate(end);
        List<YcfPriceInfo> ycfPriceInfos = getPriceV2(request);
        if(ListUtils.isNotEmpty(ycfPriceInfos)){
            setMealMPO.setPriceStocks(ycfPriceInfos.stream().map(p -> {
                HotelScenicSpotPriceStock priceStock = new HotelScenicSpotPriceStock();
                priceStock.setAdtPrice(p.getSettlementPrice());
                priceStock.setAdtSellPrice(p.getPrice());
                priceStock.setAdtStock(p.getStock());
                priceStock.setDate(DateTimeUtil.formatDate(p.getDate()));
                return priceStock;
            }).collect(Collectors.toList()));
        }
        hotelScenicSpotProductMPO.setProductName(ycfProduct.getProductName());
        hotelScenicSpotProductMPO.setUpdateTime(new Date());
        if(ycfProduct.getProductStatus() == YcfConstants.PRODUCT_STATUS_VALID){
            hotelScenicSpotProductMPO.setStatus(1);
        } else {
            hotelScenicSpotProductMPO.setStatus(3);
        }
        ScenicSpotMappingMPO scenicSpotMappingMPO = scenicSpotMappingDao.getScenicSpotByChannelScenicSpotIdAndChannel(ycfProduct.getPoiId(), SUPPLIER_CODE_YCF);
        if(scenicSpotMappingMPO != null){
            ScenicSpotMPO scenicSpotMPO = scenicSpotDao.getScenicSpotById(scenicSpotMappingMPO.getScenicSpotId());
            if(scenicSpotMPO != null){
                AddressInfo addressInfo = new AddressInfo();
                addressInfo.setCityName(scenicSpotMPO.getCity());
                addressInfo.setCityCode(scenicSpotMPO.getCityCode());
                addressInfo.setType("0");
                addressInfo.setDestinationName(scenicSpotMPO.getCity());
                addressInfo.setDestinationCode(scenicSpotMPO.getCityCode());
                addressInfo.setProvinceCode(scenicSpotMPO.getProvinceCode());
                addressInfo.setProvinceName(scenicSpotMPO.getProvince());
                hotelScenicSpotProductMPO.setAddressInfo(Lists.newArrayList(addressInfo));
            }
        } else {
            HotelMappingMPO hotelMappingMPO = hotelMappingDao.getHotelByChannelHotelIdAndChannel(ycfProduct.getPoiId(), SUPPLIER_CODE_YCF);
            if(hotelMappingMPO != null){
                HotelMPO hotelMPO = hotelDao.getById(hotelMappingMPO.getHotelId());
                if(hotelMPO != null){
                    AddressInfo addressInfo = new AddressInfo();
                    addressInfo.setCityName(hotelMPO.getCity());
                    addressInfo.setCityCode(hotelMPO.getCityCode());
                    addressInfo.setType("0");
                    addressInfo.setDestinationName(hotelMPO.getCity());
                    addressInfo.setDestinationCode(hotelMPO.getCityCode());
                    addressInfo.setProvinceCode(hotelMPO.getProvinceCode());
                    addressInfo.setProvinceName(hotelMPO.getProvinceName());
                    hotelScenicSpotProductMPO.setAddressInfo(Lists.newArrayList(addressInfo));
                }
            }
        }

        // ???????????????
        if(ListUtils.isNotEmpty(ycfProduct.getBookRules())){
            List<String> idInfos = Lists.newArrayList();
            List<String> passengerInfos = Lists.newArrayList();
            ycfProduct.getBookRules().stream().filter(br -> StringUtils.equals(br.getPersonType(), "1")).findFirst().ifPresent(bookRule -> {
                if(bookRule.getCName() != null && bookRule.getCName()){
                    passengerInfos.add("2");
                }
                if(bookRule.getMobile() != null && bookRule.getMobile()){
                    passengerInfos.add("6");
                }
                if(bookRule.getCredential() != null && bookRule.getCredential()){
                    idInfos.addAll(bookRule.getCredentialType().stream().map(String::valueOf).collect(Collectors.toList()));
                }
                if(bookRule.getEmail() != null && bookRule.getEmail()){
                    passengerInfos.add("10");
                }
            });
            String passengerInfo = passengerInfos.stream().collect(Collectors.joining(","));
            String idInfo = idInfos.stream().collect(Collectors.joining(","));
            // ??????????????????????????????
            PassengerTemplatePO passengerTemplatePO = passengerTemplateMapper.getPassengerTemplateByCond(SUPPLIER_CODE_YCF, 1,
                    passengerInfo, idInfo);
            if(passengerTemplatePO == null){
                passengerTemplatePO = new PassengerTemplatePO();
                passengerTemplatePO.setChannel(Constants.SUPPLIER_CODE_YCF);
                passengerTemplatePO.setCreateTime(new Date());
                passengerTemplatePO.setStatus(1);
                passengerTemplatePO.setIdInfo(idInfo);
                passengerTemplatePO.setPassengerInfo(passengerInfo);
                passengerTemplatePO.setPeopleLimit(1);
                passengerTemplateMapper.addPassengerTemplate(passengerTemplatePO);
            }
            hotelScenicSpotProductMPO.setTravellerTemplateId(passengerTemplatePO.getId().toString());
        }

        hotelScenicProductDao.saveProduct(hotelScenicSpotProductMPO);
        hotelScenicProductSetMealDao.saveProduct(setMealMPO);

        HotelScenicSpotProductBackupMPO hotelScenicSpotProductBackupMPO = hotelScenicProductBackupDao.getHotelScenicSpotProductBackupByProductId(hotelScenicSpotProductMPO.getId());
        if(hotelScenicSpotProductBackupMPO == null){
            hotelScenicSpotProductBackupMPO = new HotelScenicSpotProductBackupMPO();
            hotelScenicSpotProductBackupMPO.setId(commonService.getId(BizTagConst.BIZ_HOTEL_SCENICSPORT_PRODUCT));
            hotelScenicSpotProductBackupMPO.setCreateTime(new Date());
        }
        hotelScenicSpotProductBackupMPO.setSupplierProductId(ycfProduct.getProductID());
        hotelScenicSpotProductBackupMPO.setUpdateTime(new Date());
        hotelScenicSpotProductBackupMPO.setProductMPO(hotelScenicSpotProductMPO);
        hotelScenicSpotProductBackupMPO.setSetMealMPO(setMealMPO);
        hotelScenicSpotProductBackupMPO.setOriginContent(JSON.toJSONString(ycfProduct));
        hotelScenicProductBackupDao.saveHotelScenicSpotProductBackup(hotelScenicSpotProductBackupMPO);

        commonService.refreshList(2, hotelScenicSpotProductMPO.getId(), 1, fresh);

        if(ListUtils.isNotEmpty(hotelScenicSpotProductMPO.getChangedFields())
                || ListUtils.isNotEmpty(hotelScenicSpotProductMPO.getPayInfo().getChangedFields())
                || (ListUtils.isNotEmpty(hotelScenicSpotProductMPO.getPayInfo().getBookNotices())
                && hotelScenicSpotProductMPO.getPayInfo().getBookNotices().stream().anyMatch(n ->
                ListUtils.isNotEmpty(n.getChangedFields()))) || fresh){
            commonService.addHotelProductSubscribe(hotelScenicSpotProductMPO, setMealMPO, fresh);
        }
    }

    @Override
    public List<String> syncScenic(List<String> scenicIds){
        List<YcfProductItem> ycfProductItems = getPoi(scenicIds);
        if(ListUtils.isEmpty(ycfProductItems)){
            return null;
        }
       return ycfProductItems.stream().map(item -> {
            try {
                if(item.getPoiType() != 2){
                    log.error("poi {},{} ?????????????????????????????????", item.getPoiID(), item.getPoiName());
                    return null;
                }
                ScenicSpotMPO newScenic = YcfConverter.convertToScenicSpotMPO(item);
                // ???????????????
                commonService.setCity(newScenic);
                // ????????????????????????
                commonService.updateScenicSpotMapping(item.getPoiID(), SUPPLIER_CODE_YCF, SUPPLIER_NAME_YCF, newScenic);
                // ????????????
                commonService.updateScenicSpotMPOBackup(newScenic, item.getPoiID(), SUPPLIER_CODE_YCF, item);
                return newScenic.getId();
            } catch (Exception e) {
                log.error("poi???????????????", e);
                return null;
            }
        }).filter(id -> StringUtils.isNotBlank(id)).collect(Collectors.toList());
    }

    @Override
    public List<String> syncHotel(List<String> hotelIds){
        List<YcfProductItem> ycfProductItems = getPoi(hotelIds);
        if(ListUtils.isEmpty(ycfProductItems)){
            return null;
        }
        return ycfProductItems.stream().map(item -> {
            try {
                if(item.getPoiType() != 1){
                    log.error("poi {},{} ?????????????????????????????????", item.getPoiID(), item.getPoiName());
                    return null;
                }
                HotelMPO newHotel = YcfConverter.convertToHotelMPO(item);
                // ???????????????
                commonService.setCity(newHotel);
                // ????????????????????????
                commonService.updateHotelMapping(item.getPoiID(), SUPPLIER_CODE_YCF, SUPPLIER_NAME_YCF, newHotel);
                // ????????????
//                commonService.updateScenicSpotMPOBackup(newHotel, item.getPoiID(), SUPPLIER_CODE_YCF, item);
                return newHotel.getId();
            } catch (Exception e) {
                log.error("poi???????????????", e);
                return null;
            }
        }).filter(id -> StringUtils.isNotBlank(id)).collect(Collectors.toList());
    }

    @Override
    public void syncPriceV2(YcfGetPriceRequest request){
        if(StringUtils.isBlank(request.getEndDate())){
            request.setEndDate(request.getStartDate());
        }
        int diffDays = DateTimeUtil.getDateDiffDays(DateTimeUtil.parseDate(request.getEndDate()), DateTimeUtil.parseDate(request.getStartDate()));
        // ?????????????????????30??????
        int round = diffDays / 30;
        int tail = diffDays % 30;
        // ????????????????????????+1???
        int n = round + (tail == 0 ? 0 : 1);
        // ????????????????????????n???=0
        n = n == 0 ? 1 : n;
        for(int i = 0; i < n; i++){
            YcfGetPriceRequest newRequest = new YcfGetPriceRequest();
            newRequest.setTraceId(request.getTraceId());
            newRequest.setFull(request.getFull());
            newRequest.setPartnerProductID(request.getPartnerProductID());
            newRequest.setProductID(request.getProductID());
            // ???????????????+1?????????
            newRequest.setStartDate(DateTimeUtil.formatDate(DateTimeUtil.addDay(DateTimeUtil.parseDate(request.getStartDate()), i * 30)));
            // ?????????30???
            newRequest.setEndDate(DateTimeUtil.formatDate(DateTimeUtil.addDay(DateTimeUtil.parseDate(newRequest.getStartDate()), 29)));
            if(i == n - 1){
                newRequest.setEndDate(request.getEndDate());
            }
            try {
                log.info("????????????????????????????????? = {} ????????? = {} ??? {} ", request.getPartnerProductID(), newRequest.getStartDate(), newRequest.getEndDate());
                List<YcfPriceInfo> ycfPriceInfos = getPriceV2(newRequest);
                syncPrice(request.getPartnerProductID(), ycfPriceInfos, request.getProductID());
                log.info("????????????????????????????????? = {} ????????? = {} ??? {} ", request.getPartnerProductID(), newRequest.getStartDate(), newRequest.getEndDate());
                // ???????????????1??????????????????200???
                Thread.sleep(310);
            } catch (Exception e) {
                log.info("?????????????????????????????? = {} ????????? = {} ??? {} ", request.getPartnerProductID(), newRequest.getStartDate(), newRequest.getEndDate(), e);
                continue;
            }
        }
    }

    @Override
    public void syncPriceV2(YcfPrice ycfPrice){
        String ycfProductId = ycfPrice.getProductID();
        List<YcfPriceInfo> ycfPriceInfos = ycfPrice.getSaleInfos();
        if(StringUtils.isBlank(ycfProductId) || ListUtils.isEmpty(ycfPriceInfos)){
            log.error("?????????????????????{}??????id????????????????????????", SUPPLIER_CODE_YCF);
            return;
        }
        ScenicSpotProductMPO scenicSpotProductMPO = scenicSpotProductDao.getBySupplierProductId(ycfProductId, SUPPLIER_CODE_YCF);
        if(scenicSpotProductMPO == null){
            log.error("?????????????????????{}?????????{}?????????", SUPPLIER_CODE_YCF, ycfProductId);
            return;
        }
        syncPrice(scenicSpotProductMPO.getId(), ycfPriceInfos, scenicSpotProductMPO.getSupplierProductId());
    }

    private List<YcfPriceInfo> getPriceV2(YcfGetPriceRequest request){
        YcfBaseRequest ycfBaseRequest = new YcfBaseRequest(request);
        log.info("?????????????????????(?????????)???????????????????????????={}", JSON.toJSONString(request));
        YcfBaseResult<YcfGetPriceResponse> baseResult = yaoChuFaClient.getPrice(ycfBaseRequest);
        log.info("?????????(?????????)?????????????????????????????????={}", JSON.toJSONString(baseResult));
        if(baseResult.getSuccess() && baseResult.getStatusCode() == YcfConstants.RESULT_CODE_SUCCESS){
            YcfGetPriceResponse response = baseResult.getData();
            if(response == null){
                log.error("?????????????????????????????????????????????????????????data");
                return null;
            }
            return response.getSaleInfos();
        }
        return null;
    }

    private List<YcfProductItem> getPoi(List<String> scenicIds){
        if(ListUtils.isEmpty(scenicIds)){
            log.error("??????poi?????????poi id????????????");
            return null;
        }
        log.info("????????????poi???id list = {}", JSON.toJSONString(scenicIds));
        YcfGetPoiRequest ycfGetPoiRequest = new YcfGetPoiRequest();
        ycfGetPoiRequest.setPoiIdList(scenicIds);
        YcfBaseRequest ycfBaseRequest = new YcfBaseRequest(ycfGetPoiRequest);
        log.info("?????????????????????(?????????)??????poi???????????????={}", JSON.toJSONString(ycfGetPoiRequest));
        YcfBaseResult<YcfGetPoiResponse> baseResult = yaoChuFaClient.getPoi(ycfBaseRequest);
        log.info("?????????(?????????)??????poi?????????????????????={}", JSON.toJSONString(baseResult));
        if(baseResult.getSuccess() && baseResult.getStatusCode() == YcfConstants.RESULT_CODE_SUCCESS) {
            YcfGetPoiResponse response = baseResult.getData();
            if (response == null) {
                log.error("??????poi?????????????????????????????????????????????data");
                return null;
            }
            List<YcfProductItem> ycfProductItems = response.getPoiList();
            if (ListUtils.isEmpty(ycfProductItems)) {
                log.error("??????poi?????????????????????????????????????????????poi??????");
                return null;
            }
            return ycfProductItems;
        }
        return null;
    }

    private void syncPrice(String productId, List<YcfPriceInfo> ycfPriceInfos, String supplierProductId){
        syncPrice(productId, ycfPriceInfos, null, null, supplierProductId);
    }

    private void syncPrice(String productId, List<YcfPriceInfo> ycfPriceInfos, String ruleId, String ticketKind, String supplierProductId){
        if(StringUtils.isBlank(productId) || ListUtils.isEmpty(ycfPriceInfos)){
            log.error("???????????????????????????id????????????????????????");
            return;
        }
        List<ScenicSpotProductPriceMPO> existPrice = scenicSpotProductPriceDao.getByProductId(productId);
        if(ListUtils.isNotEmpty(existPrice)){
            ruleId = existPrice.get(0).getScenicSpotRuleId();
            ticketKind = existPrice.get(0).getTicketKind();
        }
        if(StringUtils.isBlank(ruleId)){
            log.error("???????????????????????????{}???????????????????????????id", productId);
            return;
        }
        for (YcfPriceInfo yp : ycfPriceInfos) {
            ScenicSpotProductPriceMPO exist = scenicSpotProductPriceDao.getExistPrice(productId, ruleId, DateTimeUtil.formatDate(yp.getDate()));
            if(exist != null){
                boolean b = false;
                if((exist.getSellPrice() == null && yp.getPrice() != null) || (exist.getSellPrice() != null && yp.getPrice() == null)
                        || (exist.getSellPrice() != null && yp.getPrice() != null && exist.getSellPrice().compareTo(yp.getPrice()) != 0)){
                    exist.setSellPrice(yp.getPrice() == null ? null : yp.getPrice());
                    b = true;
                }
                if((exist.getSettlementPrice() == null && yp.getSettlementPrice() != null)
                        || (exist.getSettlementPrice() != null && yp.getSettlementPrice() == null)
                        || (exist.getSettlementPrice() != null && yp.getSettlementPrice() != null && exist.getSettlementPrice().compareTo(yp.getSettlementPrice()) != 0)){
                    exist.setSettlementPrice(yp.getSettlementPrice() == null ? null : yp.getSettlementPrice());
                    b = true;
                }
                // ??????????????????????????????????????????????????????????????????
                int stock = yp.getStock() == null ? 0 : yp.getStock();
                if(exist.getStock() != stock){
                    exist.setStock(stock);
                    b = true;
                }
                // ??????????????????????????????????????????mongo?????????
                if(b){
                    exist.setUpdateTime(new Date());
                    scenicSpotProductPriceDao.saveScenicSpotProductPrice(exist);
                }
            } else {
                ScenicSpotProductPriceMPO priceMPO = new ScenicSpotProductPriceMPO();
                priceMPO.setId(commonService.getId(BizTagConst.BIZ_SCENICSPOT_PRODUCT));
                priceMPO.setScenicSpotProductId(productId);
                priceMPO.setMerchantCode(supplierProductId);
                priceMPO.setScenicSpotRuleId(ruleId);
                if (StringUtils.isBlank(ticketKind)) {
                    // ?????????????????????tickettype,???????????????
                    ticketKind = "1";
                }
                priceMPO.setTicketKind(ticketKind);
                priceMPO.setStartDate(DateTimeUtil.formatDate(yp.getDate()));
                priceMPO.setEndDate(DateTimeUtil.formatDate(yp.getDate()));
                priceMPO.setSellPrice(yp.getPrice());
                priceMPO.setSettlementPrice(yp.getSettlementPrice());
                priceMPO.setStock(yp.getStock());
                priceMPO.setWeekDay("1,2,3,4,5,6,7");
                priceMPO.setCreateTime(new Date());
                priceMPO.setUpdateTime(new Date());
                scenicSpotProductPriceDao.addScenicSpotProductPrice(priceMPO);
            }
        }
    }

}
