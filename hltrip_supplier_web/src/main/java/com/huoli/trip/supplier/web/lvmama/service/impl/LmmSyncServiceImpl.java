package com.huoli.trip.supplier.web.lvmama.service.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huoli.eagle.eye.core.HuoliAtrace;
import com.huoli.eagle.eye.core.statistical.Event;
import com.huoli.eagle.eye.core.statistical.EventStatusEnum;
import com.huoli.trip.common.constant.*;
import com.huoli.trip.common.entity.*;
import com.huoli.trip.common.entity.mpo.DescInfo;
import com.huoli.trip.common.entity.mpo.scenicSpotTicket.*;
import com.huoli.trip.common.util.*;
import com.huoli.trip.supplier.api.DynamicProductItemService;
import com.huoli.trip.supplier.feign.client.lvmama.client.ILvmamaProductClient;
import com.huoli.trip.supplier.self.lvmama.constant.LmmConfigConstants;
import com.huoli.trip.supplier.self.lvmama.vo.*;
import com.huoli.trip.supplier.self.lvmama.vo.push.LmmProductPushRequest;
import com.huoli.trip.supplier.self.lvmama.vo.request.*;
import com.huoli.trip.supplier.self.lvmama.vo.response.LmmGoodsListByIdResponse;
import com.huoli.trip.supplier.self.lvmama.vo.response.LmmPriceResponse;
import com.huoli.trip.supplier.self.lvmama.vo.response.LmmProductListResponse;
import com.huoli.trip.supplier.self.lvmama.vo.response.LmmScenicListResponse;
import com.huoli.trip.supplier.web.dao.*;
import com.huoli.trip.supplier.web.lvmama.convert.LmmTicketConverter;
import com.huoli.trip.supplier.web.lvmama.service.LmmSyncService;
import com.huoli.trip.supplier.web.mapper.TripDictionaryMapper;
import com.huoli.trip.supplier.web.service.CommonService;
import com.huoli.trip.supplier.web.util.XmlConvertUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.huoli.trip.supplier.self.common.SupplierConstants.PRODUCT_SYNC_MODE_ONLY_ADD;
import static com.huoli.trip.supplier.self.common.SupplierConstants.PRODUCT_SYNC_MODE_ONLY_UPDATE;

/**
 * ?????????<br/>
 * ?????????Copyright (c) 2011-2020<br>
 * ?????????????????????<br>
 * ??????????????????<br>
 * ?????????1.0<br>
 * ???????????????2021/3/17<br>
 */
@Service
@Slf4j
public class LmmSyncServiceImpl implements LmmSyncService {

    @Autowired
    private ILvmamaProductClient lvmamaClient;

    @Autowired
    private ProductItemDao productItemDao;

    @Autowired
    private ProductDao productDao;

    @Autowired
    private DynamicProductItemService dynamicProductItemService;

    @Autowired
    private CommonService commonService;

    @Autowired
    private PriceDao priceDao;

    @Autowired
    private ScenicSpotMappingDao scenicSpotMappingDao;

    @Autowired
    private ScenicSpotDao scenicSpotDao;

    @Autowired
    private ScenicSpotProductDao scenicSpotProductDao;

    @Autowired
    private ScenicSpotRuleDao scenicSpotRuleDao;

    @Autowired
    private ScenicSpotProductPriceDao scenicSpotProductPriceDao;

    @Autowired
    private ScenicSpotProductBackupDao scenicSpotProductBackupDao;

    @Autowired
    private TripDictionaryMapper tripDictionaryMapper;

    /**
     * ?????????????????????
     */
    @Autowired
    private HuoliAtrace huoliAtrace;

    @Override
    public List<LmmScenic> getScenicList(LmmScenicListRequest request){
        LmmScenicListResponse lmmScenicResponse = lvmamaClient.getScenicList(request.getCurrentPage());
        if(!checkLmmScenicListResponse(lmmScenicResponse)){
            return null;
        }
        String str = removeCDATA(JSON.toJSONString(lmmScenicResponse));
        lmmScenicResponse = JSON.parseObject(str, LmmScenicListResponse.class);
        return lmmScenicResponse.getScenicNameList();
    }

    @Override
    public List<LmmScenic> getScenicListById(LmmScenicListByIdRequest request){
        LmmScenicListResponse lmmScenicResponse = lvmamaClient.getScenicListById(request.getScenicId());
        if(!checkLmmScenicListResponse(lmmScenicResponse)){
            return null;
        }
        String str = removeCDATA(JSON.toJSONString(lmmScenicResponse));
        lmmScenicResponse = JSON.parseObject(str, LmmScenicListResponse.class);
        return lmmScenicResponse.getScenicNameList();
    }

    private boolean checkLmmScenicListResponse(LmmScenicListResponse lmmScenicResponse){
        if(lmmScenicResponse == null){
            log.error("????????????????????????????????????");
            return false;
        }
        if(lmmScenicResponse.getState() == null){
            log.error("?????????????????????????????????????????????");
            return false;
        }
        if(!StringUtils.equals(lmmScenicResponse.getState().getCode(), "1000")){
            log.error("??????????????????????????????????????????code={}, message={}, solution={}",
                    lmmScenicResponse.getState().getCode(), lmmScenicResponse.getState().getMessage(),
                    lmmScenicResponse.getState().getSolution());
            return false;
        }
        if(ListUtils.isEmpty(lmmScenicResponse.getScenicNameList())){
            log.error("????????????????????????????????????????????????");
            return false;
        }
        return true;
    }

    private String removeCDATA(String str){
        String newStr = str.replace("<![CDATA[", "");
        newStr = newStr.replace("]]>", "");
        return newStr;
    }

    @Override
    public List<LmmProduct> getProductList(LmmProductListRequest request){
        LmmProductListResponse lmmProductListResponse = lvmamaClient.getProductList(request.getCurrentPage());
        if(lmmProductListResponse == null){
            log.error("????????????????????????????????????");
            return null;
        }
        if(lmmProductListResponse.getState() == null){
            log.error("?????????????????????????????????????????????");
            return null;
        }
        if(!StringUtils.equals(lmmProductListResponse.getState().getCode(), "1000")){
            log.error("??????????????????????????????????????????code={}, message={}, solution={}",
                    lmmProductListResponse.getState().getCode(), lmmProductListResponse.getState().getMessage(),
                    lmmProductListResponse.getState().getSolution());
            return null;
        }
        if(ListUtils.isEmpty(lmmProductListResponse.getProductList())){
            log.error("????????????????????????????????????????????????");
            return null;
        }
        String str = removeCDATA(JSON.toJSONString(lmmProductListResponse));
        lmmProductListResponse = JSON.parseObject(str, LmmProductListResponse.class);
        return lmmProductListResponse.getProductList();
    }

    @Override
    public List<LmmProduct> getProductListById(LmmProductListByIdRequest request){
        LmmProductListResponse lmmProductListResponse = lvmamaClient.getProductListById(request.getProductIds());
        if(lmmProductListResponse == null){
            log.error("????????????????????????????????????");
            return null;
        }
        if(lmmProductListResponse.getState() == null){
            log.error("?????????????????????????????????????????????");
            return null;
        }
        if(!StringUtils.equals(lmmProductListResponse.getState().getCode(), "1000")){
            log.error("??????????????????????????????????????????code={}, message={}, solution={}",
                    lmmProductListResponse.getState().getCode(), lmmProductListResponse.getState().getMessage(),
                    lmmProductListResponse.getState().getSolution());
            return null;
        }
        if(ListUtils.isEmpty(lmmProductListResponse.getProductList())){
            log.error("????????????????????????????????????????????????");
            return null;
        }
        String str = removeCDATA(JSON.toJSONString(lmmProductListResponse));
        lmmProductListResponse = JSON.parseObject(str, LmmProductListResponse.class);
        return lmmProductListResponse.getProductList();
    }

    @Override
    public List<LmmGoods> getGoodsListById(String goodsId) {
        LmmGoodsListByIdRequest request = new LmmGoodsListByIdRequest();
        request.setGoodsIds(goodsId);
        return getGoodsListById(request);
    }

    @Override
    public List<LmmGoods> getGoodsListById(LmmGoodsListByIdRequest request){
        LmmGoodsListByIdResponse lmmGoodsListByIdResponse = lvmamaClient.getGoodsListById(request.getGoodsIds());
        if(lmmGoodsListByIdResponse == null){
            log.error("????????????????????????????????????");
            return null;
        }
        if(lmmGoodsListByIdResponse.getState() == null){
            log.error("?????????????????????????????????????????????");
            return null;
        }
        if(!StringUtils.equals(lmmGoodsListByIdResponse.getState().getCode(), "1000")){
            log.error("??????????????????????????????????????????code={}, message={}, solution={}",
                    lmmGoodsListByIdResponse.getState().getCode(), lmmGoodsListByIdResponse.getState().getMessage(),
                    lmmGoodsListByIdResponse.getState().getSolution());
            return null;
        }
        if(ListUtils.isEmpty(lmmGoodsListByIdResponse.getGoodsList())){
            log.error("????????????????????????????????????????????????");
            return null;
        }
        String str = removeCDATA(JSON.toJSONString(lmmGoodsListByIdResponse));
        lmmGoodsListByIdResponse = JSON.parseObject(str, LmmGoodsListByIdResponse.class);
        return lmmGoodsListByIdResponse.getGoodsList();
    }

    @Override
    public List<LmmPriceProduct> getPriceList(LmmPriceRequest request){
        LmmPriceResponse lmmPriceResponse = lvmamaClient.getPriceList(request.getGoodsIds(), request.getBeginDate(), request.getEndDate());
        if(lmmPriceResponse == null){
            log.error("??????????????????????????????");
            return null;
        }
        if(lmmPriceResponse.getState() == null){
            log.error("???????????????????????????????????????");
            return null;
        }
        if(!StringUtils.equals(lmmPriceResponse.getState().getCode(), "1000")){
            log.error("????????????????????????????????????code={}, message={}, solution={}",
                    lmmPriceResponse.getState().getCode(), lmmPriceResponse.getState().getMessage(),
                    lmmPriceResponse.getState().getSolution());
            return null;
        }
        if(ListUtils.isEmpty(lmmPriceResponse.getPriceList())){
            log.error("????????????????????????????????????????????????");
            return null;
        }
        String str = removeCDATA(JSON.toJSONString(lmmPriceResponse));
        lmmPriceResponse = JSON.parseObject(str, LmmPriceResponse.class);
        return lmmPriceResponse.getPriceList();
    }

    @Override
    public boolean syncScenicList(LmmScenicListRequest request){
        List<LmmScenic> lmmScenicList = getScenicList(request);
        return updateScenic(lmmScenicList);
    }

    @Override
    public void syncScenicListById(LmmScenicListByIdRequest request){
        List<LmmScenic> lmmScenicList = getScenicListById(request);
        updateScenic(lmmScenicList);
    }

    @Override
    public void syncScenicListById(String id){
        LmmScenicListByIdRequest request = new LmmScenicListByIdRequest();
        request.setScenicId(id);
        syncScenicListById(request);
    }

    @Override
    public List<String> getSupplierScenicIds(){
        return productItemDao.selectSupplierItemIdsBySupplierIdAndType(Constants.SUPPLIER_CODE_LMM_TICKET,
                Constants.PRODUCT_ITEM_TYPE_TICKET);
    }

    private boolean updateScenic(List<LmmScenic> lmmScenicList){
        if(ListUtils.isEmpty(lmmScenicList)){
            return false;
        }
        lmmScenicList.forEach(s -> {
            ProductItemPO newItem = LmmTicketConverter.convertToProductItemPO(s);
            if(StringUtils.isBlank(newItem.getCity())){
                log.error("???????????????{}???????????????????????????", s.getScenicId());
                return;
            }
            ProductItemPO oldItem = productItemDao.selectByCode(newItem.getCode());
            List<ItemFeaturePO> featurePOs = null;
            ProductPO productPO = null;
            List<ImageBasePO> imageDetails = null;
            List<ImageBasePO> images = null;
            List<ImageBasePO> mainImages = null;
            if (oldItem == null) {
                newItem.setCreateTime(MongoDateUtils.handleTimezoneInput(new Date()));
                newItem.setAuditStatus(Constants.VERIFY_STATUS_PASSING);
            } else {
                imageDetails = oldItem.getImageDetails();
                images = oldItem.getImages();
                mainImages = oldItem.getMainImages();
                newItem.setAuditStatus(oldItem.getAuditStatus());
                productPO = oldItem.getProduct();
                featurePOs = oldItem.getFeatures();
                // ????????????
                commonService.compareProductItem(newItem);
            }
            newItem.setUpdateTime(MongoDateUtils.handleTimezoneInput(new Date()));
            newItem.setOperator(Constants.SUPPLIER_CODE_LMM_TICKET);
            newItem.setOperatorName(Constants.SUPPLIER_NAME_LMM_TICKET);
            // ????????????
            commonService.saveBackupProductItem(newItem);
            newItem.setProduct(productPO);
            newItem.setImageDetails(imageDetails);
            newItem.setImages(images);
            newItem.setMainImages(mainImages);
            newItem.setFeatures(featurePOs);
            if(ListUtils.isEmpty(newItem.getImages()) && ListUtils.isEmpty(newItem.getMainImages())){
                log.info("{}?????????????????????????????????????????????", newItem.getCode());
                newItem.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
            }
            // ???????????????????????????
            if(oldItem == null){
                productItemDao.updateByCode(newItem);
            }
        });
        return true;
    }

    @Override
    public boolean syncProductList(LmmProductListRequest request, int syncMode){
        List<LmmProduct> lmmProductList = getProductList(request);
        if(ListUtils.isEmpty(lmmProductList)){
            return false;
        }
        lmmProductList.forEach(p -> updateProduct(p, p.getGoodsList(), syncMode));
        return true;
    }

    @Override
    public boolean syncProductListById(LmmProductListByIdRequest request, int syncMode){
        List<LmmProduct> lmmProductList = getProductListById(request);
        if(ListUtils.isEmpty(lmmProductList)){
            return false;
        }
        lmmProductList.forEach(p -> {
            String goodsIds = p.getGoodsIds();
            LmmGoodsListByIdRequest lmmGoodsListByIdRequest = new LmmGoodsListByIdRequest();
            lmmGoodsListByIdRequest.setGoodsIds(goodsIds);
            List<LmmGoods> goodsList = getGoodsListById(lmmGoodsListByIdRequest);
            updateProduct(p, goodsList, syncMode);
        });
        return true;
    }

    @Override
    public boolean syncProductListById(String productIds, int syncMode){
        LmmProductListByIdRequest request = new LmmProductListByIdRequest();
        request.setProductIds(productIds);
        return syncProductListById(request, syncMode);
    }


    @Override
    public boolean syncGoodsListById(LmmGoodsListByIdRequest request, int syncMode){
        List<LmmGoods> lmmGoodsList = getGoodsListById(request);
        if(ListUtils.isEmpty(lmmGoodsList)){
            return false;
        }
        Map<String, List<LmmGoods>> goodsMap = lmmGoodsList.stream().collect(Collectors.groupingBy(LmmGoods::getProductId));
        goodsMap.forEach((k, v) -> {
            LmmProductListByIdRequest lmmProductListByIdRequest = new LmmProductListByIdRequest();
            lmmProductListByIdRequest.setProductIds(k);
            List<LmmProduct> lmmProductList = getProductListById(lmmProductListByIdRequest);
            if(ListUtils.isEmpty(lmmProductList)){
                return;
            }
            LmmProduct lmmProduct = lmmProductList.get(0);
            updateProduct(lmmProduct, v, syncMode);
        });
        return true;
    }

    @Override
    public boolean syncGoodsListById(String productIds, int syncMode){
        LmmGoodsListByIdRequest request = new LmmGoodsListByIdRequest();
        request.setGoodsIds(productIds);
        return syncGoodsListById(request, syncMode);
    }

    private void updateProduct(LmmProduct lmmProduct, List<LmmGoods> goodsList, int syncMode){
        if(ListUtils.isEmpty(goodsList)){
            log.error("??????{},{}????????????????????????????????????", lmmProduct.getProductId(), lmmProduct.getProductName());
            return;
        }
        String itemCode = CommonUtils.genCodeBySupplier(Constants.SUPPLIER_CODE_LMM_TICKET, lmmProduct.getPlaceId());
        ProductItemPO productItemPO = productItemDao.selectByCode(itemCode);
        if(productItemPO == null){
            syncScenicListById(lmmProduct.getPlaceId());
            productItemPO = productItemDao.selectByCode(itemCode);
        }
        if(productItemPO == null){
            log.error("item??????????????????????????????");
            return;
        }
        for (LmmGoods g : goodsList) {
            if(StringUtils.equals(g.getTicketSeason(), "true")){
                log.info("??????????????????productId={}, goodsId={}");
                continue;
            }
            ProductPO newProduct = LmmTicketConverter.convertToProductPO(lmmProduct, g);
            newProduct.setMainItemCode(productItemPO.getCode());
            newProduct.setMainItem(productItemPO);
            newProduct.setCity(productItemPO.getCity());
            newProduct.setDesCity(productItemPO.getDesCity());
            newProduct.setOriCity(productItemPO.getOriCity());
            if(newProduct.getTicket() != null && ListUtils.isNotEmpty(newProduct.getTicket().getTickets())){
                for (TicketInfoPO ticket : newProduct.getTicket().getTickets()) {
                    ticket.setItemId(productItemPO.getCode());
                    ticket.setProductItem(productItemPO);
                }
            }
            ProductPO oldProduct = productDao.getByCode(newProduct.getCode());
            // ????????????????????????????????????
            if(PRODUCT_SYNC_MODE_ONLY_ADD == syncMode && oldProduct != null){
                log.error("???????????????????????????????????????????????????????????????supplierProductCode={}", newProduct.getSupplierProductId());
                continue;
            }
            if(PRODUCT_SYNC_MODE_ONLY_UPDATE == syncMode && oldProduct == null){
                log.error("?????????????????????????????????????????????????????????supplierProductCode={}", newProduct.getSupplierProductId());
                continue;
            }
            newProduct.setUpdateTime(MongoDateUtils.handleTimezoneInput(new Date()));
            newProduct.setOperator(Constants.SUPPLIER_CODE_LMM_TICKET);
            newProduct.setOperatorName(Constants.SUPPLIER_NAME_LMM_TICKET);
            newProduct.setValidTime(MongoDateUtils.handleTimezoneInput(DateTimeUtil.trancateToDate(new Date())));
            newProduct.setInvalidTime(MongoDateUtils.handleTimezoneInput(DateTimeUtil.addDay(new Date(), 30)));
            Map<String, String> params = Maps.newHashMap();
            params.put("productId", lmmProduct.getProductId());
            newProduct.setExtendParams(params);
            if(oldProduct == null){
                newProduct.setCreateTime(MongoDateUtils.handleTimezoneInput(new Date()));
//                product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
                newProduct.setAuditStatus(Constants.VERIFY_STATUS_PASSING);
                newProduct.setSupplierStatus(Constants.SUPPLIER_STATUS_OPEN);
                BackChannelEntry backChannelEntry = commonService.getSupplierById(newProduct.getSupplierId());
                if(backChannelEntry == null
                        || backChannelEntry.getStatus() == null
                        || backChannelEntry.getStatus() != 1){
                    newProduct.setSupplierStatus(Constants.SUPPLIER_STATUS_CLOSED);
                }
                if(backChannelEntry != null && StringUtils.isNotBlank(backChannelEntry.getAppSource())){
                    List<String> appFroms = Arrays.asList(backChannelEntry.getAppSource().split(","));
                    newProduct.setAppFrom(appFroms);
                }
            } else {
                if(oldProduct.getSupplierStatus() == null
                        || ListUtils.isEmpty(oldProduct.getAppFrom())){
                    BackChannelEntry backChannelEntry = commonService.getSupplierById(newProduct.getSupplierId());
                    if(backChannelEntry == null
                            || backChannelEntry.getStatus() == null
                            || backChannelEntry.getStatus() != 1){
                        newProduct.setSupplierStatus(Constants.SUPPLIER_STATUS_CLOSED);
                    }
                    if(backChannelEntry != null && StringUtils.isNotBlank(backChannelEntry.getAppSource())){
                        List<String> appFroms = Arrays.asList(backChannelEntry.getAppSource().split(","));
                        newProduct.setAppFrom(appFroms);
                    }
                }
                newProduct.setAuditStatus(oldProduct.getAuditStatus());
                newProduct.setSupplierStatus(oldProduct.getSupplierStatus());
                newProduct.setRecommendFlag(oldProduct.getRecommendFlag());
                newProduct.setAppFrom(oldProduct.getAppFrom());
//                newProduct.setBookDescList(oldProduct.getBookDescList());
                newProduct.setDescriptions(oldProduct.getDescriptions());
//                newProduct.setBookNoticeList(oldProduct.getBookNoticeList());
                commonService.compareProduct(newProduct, oldProduct);
            }
            productDao.updateByCode(newProduct);
            syncPrice(g.getGoodsId());
            dynamicProductItemService.refreshItemByProductCode(Lists.newArrayList(newProduct.getCode()));
            // ????????????
            commonService.saveBackupProduct(newProduct);
        }
    }

    @Override
    public void syncPrice(String goodsId){
        LmmPriceRequest request = new LmmPriceRequest();
        request.setGoodsIds(goodsId);
        request.setBeginDate(DateTimeUtil.formatDate(new Date()));
        request.setEndDate(DateTimeUtil.formatDate(DateTimeUtil.addDay(new Date(), 45)));
        syncPrice(request);
    }

    @Override
    public boolean syncPrice(LmmPriceRequest request){
        if(StringUtils.isBlank(request.getBeginDate())){
            request.setBeginDate(DateTimeUtil.formatDate(new Date()));
        }
        if(StringUtils.isBlank(request.getEndDate())){
            request.setEndDate(DateTimeUtil.formatDate(DateTimeUtil.addDay(DateTimeUtil.parseDate(request.getBeginDate()), 30)));
        }
        List<LmmPriceProduct> priceList = getPriceList(request);
        priceList.forEach(p -> {
            if(ListUtils.isEmpty(p.getGoodsList().getGoods())){
                return;
            }
            p.getGoodsList().getGoods().forEach(g -> {
                if(ListUtils.isEmpty(g.getPrices().getPrice())){
                    return;
                }
                String productCode = CommonUtils.genCodeBySupplier(Constants.SUPPLIER_CODE_LMM_TICKET, g.getGoodsId());
                ProductPO productPO = productDao.getByCode(productCode);
                if(productPO == null){
                    log.error("??????????????????????????????{}??????????????????????????????", productPO.getCode());
                    return;
                }
                List<PriceInfoPO> priceInfoPOs = Lists.newArrayList();
                g.getPrices().getPrice().forEach(price -> {
                    PriceInfoPO priceInfoPO = new PriceInfoPO();
                    priceInfoPO.setSaleDate(MongoDateUtils.handleTimezoneInput(DateTimeUtil.parseDate(price.getDate())));
                    if(price.getB2bPrice() != null){
                        priceInfoPO.setSalePrice(BigDecimal.valueOf(price.getB2bPrice()));
                    }
                    if(price.getSellPrice() != null){
                        priceInfoPO.setSettlePrice(BigDecimal.valueOf(price.getSellPrice()));
                    }
                    if(priceInfoPO.getSalePrice() == null){
                        priceInfoPO.setSalePrice(priceInfoPO.getSettlePrice());
                    }
                    priceInfoPO.setStock(price.getStock());
                    if(price.getStock() == -1){
                        priceInfoPO.setStock(999);
                    }
                    priceInfoPOs.add(priceInfoPO);
                });
                PricePO pricePO = priceDao.getByProductCode(productPO.getCode());
                if(pricePO == null){
                    pricePO = new PricePO();
                    pricePO.setProductCode(productPO.getCode());
                    pricePO.setSupplierProductId(g.getGoodsId());
                    pricePO.setOperator(Constants.SUPPLIER_CODE_LMM_TICKET);
                    pricePO.setOperatorName(Constants.SUPPLIER_NAME_LMM_TICKET);
                    pricePO.setCreateTime(MongoDateUtils.handleTimezoneInput(new Date()));
                }
                pricePO.setUpdateTime(MongoDateUtils.handleTimezoneInput(new Date()));
                pricePO.setPriceInfos(priceInfoPOs);
                priceDao.updateByProductCode(pricePO);
            });
        });
        return true;
    }

    @Override
    public void pushUpdate(String product) throws JAXBException {
        log.info("?????????????????????????????????");
        LmmProductPushRequest request = XmlConvertUtil.convertToJava(product, LmmProductPushRequest.class);
        String changeType = request.getBody().getChangeType();
        LmmProductPushRequest.LmmPushProduct lmmPushProduct = request.getBody().getProduct();
        if(Arrays.asList("product_online", "product_create", "product_info_change").contains(changeType)){
            syncProductListById(lmmPushProduct.getProductId().toString(), 0);
        } else if(Arrays.asList("goods_online", "goods_create", "goods_info_change", "price_change").contains(changeType)){
            syncGoodsListById(lmmPushProduct.getGoodsId().toString(), 0);
        } else if(Arrays.asList("product_offline").contains(changeType)){
            Map<String, String> cond = Maps.newHashMap();
            cond.put("extendParams.productId", lmmPushProduct.getProductId().toString());
            List<ProductPO> productPOs = productDao.getByCond(Constants.SUPPLIER_CODE_LMM_TICKET, cond);
            if(ListUtils.isNotEmpty(productPOs)){
                productPOs.forEach(p -> productDao.updateStatusByCode(p.getCode(), 0));
            }
        } else if(Arrays.asList("goods_offline").contains(changeType)){
            ProductPO productPO = productDao.getBySupplierProductId(lmmPushProduct.getGoodsId().toString());
            if(productPO != null){
                productDao.updateStatusByCode(productPO.getCode(), 0);
            }
        }
    }

    @Override
    public List<String> getSupplierProductIds() {
        return productDao.selectSupplierProductIdsBySupplierIdAndType(Constants.SUPPLIER_CODE_LMM_TICKET,
                ProductType.SCENIC_TICKET.getCode());
    }



    // ==================================???????????????????????????===============================

    @Override
    public boolean syncScenicListV2(LmmScenicListRequest request){
        List<LmmScenic> lmmScenicList = getScenicList(request);
        if(ListUtils.isEmpty(lmmScenicList)){
            return false;
        }
        syncScenic(lmmScenicList);
        return true;
    }

    @Override
    public void syncScenicListByIdV2(LmmScenicListByIdRequest request){
        List<LmmScenic> lmmScenicList = getScenicListById(request);
        syncScenic(lmmScenicList);
    }

    @Override
    public void syncScenicListByIdV2(LmmScenicListByIdRequest request, boolean checkCity){
        List<LmmScenic> lmmScenicList = getScenicListById(request);
        syncScenic(lmmScenicList, checkCity);
    }

    @Override
    public void syncScenicListByIdV2(String id){
        LmmScenicListByIdRequest request = new LmmScenicListByIdRequest();
        request.setScenicId(id);
        syncScenicListByIdV2(request);
    }

    @Override
    public void syncScenicListByIdV2(String id, boolean checkCity){
        LmmScenicListByIdRequest request = new LmmScenicListByIdRequest();
        request.setScenicId(id);
        syncScenicListByIdV2(request, checkCity);
    }

    @Override
    public boolean syncProductListV2(LmmProductListRequest request) {
        List<LmmProduct> lmmProductList = getProductList(request);
        if (ListUtils.isEmpty(lmmProductList)) {
            return false;
        }
        lmmProductList.forEach(p -> {
            try {
                updateProductV2(p, p.getGoodsList());
            } catch (Exception e) {
                log.error("?????????????????????{}????????????????????????????????????????????????????????????", p.getProductId(), e);
                return;
            }
        });
        return true;
    }

    @Override
    public boolean syncProductListByIdV2(LmmProductListByIdRequest request){
        List<LmmProduct> lmmProductList = getProductListById(request);
        if(ListUtils.isEmpty(lmmProductList)){
            return false;
        }
        lmmProductList.forEach(p -> {
            String goodsIds = p.getGoodsIds();
            LmmGoodsListByIdRequest lmmGoodsListByIdRequest = new LmmGoodsListByIdRequest();
            lmmGoodsListByIdRequest.setGoodsIds(goodsIds);
            List<LmmGoods> goodsList = getGoodsListById(lmmGoodsListByIdRequest);
            updateProductV2(p, goodsList);
        });
        return true;
    }

    @Override
    public boolean syncProductListByIdV2(String productIds){
        LmmProductListByIdRequest request = new LmmProductListByIdRequest();
        request.setProductIds(productIds);
        return syncProductListByIdV2(request);
    }

    @Override
    public boolean syncGoodsListByIdV2(LmmGoodsListByIdRequest request){
        List<LmmGoods> lmmGoodsList = getGoodsListById(request);
        if(ListUtils.isEmpty(lmmGoodsList)){
            return false;
        }
        Map<String, List<LmmGoods>> goodsMap = lmmGoodsList.stream().collect(Collectors.groupingBy(LmmGoods::getProductId));
        goodsMap.forEach((k, v) -> {
            LmmProductListByIdRequest lmmProductListByIdRequest = new LmmProductListByIdRequest();
            lmmProductListByIdRequest.setProductIds(k);
            List<LmmProduct> lmmProductList = getProductListById(lmmProductListByIdRequest);
            if(ListUtils.isEmpty(lmmProductList)){
                return;
            }
            LmmProduct lmmProduct = lmmProductList.get(0);
            updateProductV2(lmmProduct, v);
        });
        return true;
    }

    @Override
    public boolean syncGoodsListByIdV2(String goodsId){
        LmmGoodsListByIdRequest request = new LmmGoodsListByIdRequest();
        request.setGoodsIds(goodsId);
        return syncGoodsListByIdV2(request);
    }

    private void updateProductV2(LmmProduct oriLmmProduct, List<LmmGoods> goodsList){
        LmmProduct lmmProduct = JSON.parseObject(JSON.toJSONString(oriLmmProduct), LmmProduct.class);
        if(ListUtils.isNotEmpty(goodsList)){
            goodsList.forEach(g -> {
                // ??????????????????
                if(StringUtils.equals(g.getPaymentType(), "offline")){
                    log.error("????????????productId={}, goodsId={}???????????????", g.getProductId(), g.getGoodsId());
                    return;
                }
                ScenicSpotProductMPO scenicSpotProductMPO = scenicSpotProductDao.getBySupplierProductId(g.getGoodsId(), Constants.SUPPLIER_CODE_LMM_TICKET);
                ScenicSpotMPO scenicSpotMPO = null;
                boolean fresh = false;
                ScenicSpotProductBackupMPO backupMPO = null;
                if(scenicSpotProductMPO == null){
                    ScenicSpotMappingMPO scenicSpotMappingMPO = scenicSpotMappingDao.getScenicSpotByChannelScenicSpotIdAndChannel(lmmProduct.getPlaceId(), Constants.SUPPLIER_CODE_LMM_TICKET);
                    if(scenicSpotMappingMPO == null){
                        log.error("???????????????{}????????????????????????{}??? ??????????????????", lmmProduct.getProductId(), lmmProduct.getPlaceId());
                        syncScenicListByIdV2(lmmProduct.getPlaceId());
                        scenicSpotMappingMPO = scenicSpotMappingDao.getScenicSpotByChannelScenicSpotIdAndChannel(lmmProduct.getPlaceId(), Constants.SUPPLIER_CODE_LMM_TICKET);
                        if(scenicSpotMappingMPO == null){
                            log.error("???????????????{}?????????????????????{}??? ????????????", lmmProduct.getProductId(), lmmProduct.getPlaceId());
                            return;
                        }
                    }
                    scenicSpotMPO = scenicSpotDao.getScenicSpotById(scenicSpotMappingMPO.getScenicSpotId());
                    if(scenicSpotMPO == null){
                        log.error("??????{}?????????", scenicSpotMPO.getId());
                        return;
                    }
                    scenicSpotProductMPO = new ScenicSpotProductMPO();
                    scenicSpotProductMPO.setId(commonService.getId(BizTagConst.BIZ_SCENICSPOT_PRODUCT));
                    scenicSpotProductMPO.setCreateTime(new Date());
                    scenicSpotProductMPO.setScenicSpotId(scenicSpotMPO.getId());
                    scenicSpotProductMPO.setIsDel(0);
                    scenicSpotProductMPO.setSellType(1);
                    scenicSpotProductMPO.setSupplierProductId(g.getGoodsId());
                    scenicSpotProductMPO.setPayServiceType(0);
                    scenicSpotProductMPO.setChannel(Constants.SUPPLIER_CODE_LMM_TICKET);
                    // goods?????????????????????product???
                    scenicSpotProductMPO.setImages(lmmProduct.getImages());
                    if(ListUtils.isNotEmpty(scenicSpotProductMPO.getImages())){
                        scenicSpotProductMPO.setMainImage(scenicSpotProductMPO.getImages().get(0));
                    }
                    // ????????????   ????????????????????????????????????????????????????????????????????? ?????????????????????????????????????????????;??????
                    List<DescInfo> descInfos = buildDescInfos(lmmProduct, g);
                    scenicSpotProductMPO.setDescInfos(descInfos);
                    Map<String, String> extendParams = Maps.newHashMap();
                    extendParams.put("productId", lmmProduct.getProductId());
                    scenicSpotProductMPO.setExtendParams(extendParams);
                    fresh = true;
                } else {
                    if(scenicSpotProductMPO.getExtendParams() == null
                            || !scenicSpotProductMPO.getExtendParams().containsKey("productId")){
                        Map<String, String> extendParams = Maps.newHashMap();
                        extendParams.put("productId", lmmProduct.getProductId());
                        scenicSpotProductMPO.setExtendParams(extendParams);
                    }
                    backupMPO = scenicSpotProductBackupDao.getScenicSpotProductBackupByProductId(scenicSpotProductMPO.getId());
                    if(backupMPO != null){
                        List<String> changedFields = Lists.newArrayList();
                        ScenicSpotProductMPO backup = backupMPO.getScenicSpotProduct();
                        if((ListUtils.isNotEmpty(backup.getImages()) && ListUtils.isEmpty(lmmProduct.getImages()))
                                || (ListUtils.isEmpty(backup.getImages()) && ListUtils.isNotEmpty(lmmProduct.getImages()))){
                            changedFields.add("images");
                            changedFields.add("mainImage");
                            if(ListUtils.isEmpty(lmmProduct.getImages())){
                                scenicSpotProductMPO.setImages(null);
                                scenicSpotProductMPO.setMainImage(null);
                            } else {
                                scenicSpotProductMPO.setImages(lmmProduct.getImages());
                                scenicSpotProductMPO.setMainImage(scenicSpotProductMPO.getImages().get(0));
                            }
                        } else if(ListUtils.isNotEmpty(backup.getImages()) && ListUtils.isNotEmpty(lmmProduct.getImages())){
                            if(backup.getImages().size() != lmmProduct.getImages().size()
                                    || backup.getImages().stream().anyMatch(i ->
                                    !lmmProduct.getImages().contains(i))){
                                changedFields.add("images");
                                scenicSpotProductMPO.setImages(lmmProduct.getImages());
                                // ?????????????????????????????????
                                if(!lmmProduct.getImages().contains(scenicSpotProductMPO.getMainImage())){
                                    changedFields.add("mainImage");
                                    scenicSpotProductMPO.setMainImage(scenicSpotProductMPO.getImages().get(0));
                                }
                            }
                        }
//                         ????????????????????????
                        // ????????????????????????
                        // ?????????????????????????????????
//                        if(!StringUtils.equals(backup.getPcDescription(), lmmProduct.getIntrodution())){
//                            scenicSpotProductMPO.setPcDescription(lmmProduct.getIntrodution());
//                            changedFields.add("pcDescription");
//                        }
                        List<DescInfo> newDescInfos = buildDescInfos(lmmProduct, g);
                        scenicSpotProductMPO.setChangedFields(changedFields);
                        if(ListUtils.isEmpty(backup.getDescInfos())){
                            if(ListUtils.isNotEmpty(newDescInfos)){
                                newDescInfos.forEach(d -> {
                                    List<String> descChg = Lists.newArrayList();
                                    descChg.add("title");
                                    descChg.add("content");
                                    d.setChangedFields(descChg);
                                });
                            }
                        } else {
                            if(ListUtils.isEmpty(newDescInfos)){
                                scenicSpotProductMPO.setDescInfos(null);
                            } else {
                                // ???????????????
                                backup.getDescInfos().removeIf(d -> !newDescInfos.stream().map(nd -> nd.getTitle()).anyMatch(nd -> StringUtils.equals(nd, d.getTitle())));
                                // ??????????????????
                                backup.getDescInfos().stream().forEach(d ->
                                    newDescInfos.stream().filter(nd -> StringUtils.equals(nd.getTitle(), d.getTitle()) &&
                                            StringUtils.equals(nd.getContent(), d.getContent())).findFirst().ifPresent(nd ->
                                                d.setChangedFields(Lists.newArrayList("content"))));
                                // ???????????????(??????)
                                List<DescInfo> newDesc = newDescInfos.stream().filter(d ->
                                        backup.getDescInfos().stream().filter(nd ->
                                                !StringUtils.equals(nd.getTitle(), d.getTitle())).findAny().isPresent()).collect(Collectors.toList());
                                newDesc.forEach(nd ->
                                        nd.setChangedFields(Lists.newArrayList("title", "content")));
                                backup.getDescInfos().addAll(newDesc);
                                scenicSpotProductMPO.setDescInfos(backup.getDescInfos());
                            }
                        }
                    }
                }
                // ??????????????????????????????????????????????????????????????????????????????????????????
//                updateScenic(scenicSpotMPO, scenicSpotProductMPO, lmmProduct);
                if(lmmProduct.getServiceGuarantee() == null){
                    scenicSpotProductMPO.setTags(null);
                } else {
                    scenicSpotProductMPO.setTags(Lists.newArrayList(lmmProduct.getServiceGuarantee()));
                }
                if(StringUtils.equals(g.getStatus(), "true") && StringUtils.equals(lmmProduct.getProductStatus(), "true")){
                    // ?????????????????????????????????
                    if(scenicSpotProductMPO.getManuallyStatus() != 1){
                        scenicSpotProductMPO.setStatus(1);
                    }
                } else {
                    scenicSpotProductMPO.setStatus(3);
                }
                scenicSpotProductMPO.setUpdateTime(new Date());
                // ???????????????????????????????????????
                scenicSpotProductMPO.setName(g.getGoodsName());
                // ????????????
                ScenicSpotProductBaseSetting baseSetting = new ScenicSpotProductBaseSetting();
                BackChannelEntry backChannelEntry = commonService.getSupplierById(scenicSpotProductMPO.getChannel());
                if(backChannelEntry != null || StringUtils.isNotBlank(backChannelEntry.getAppSource())){
                    baseSetting.setAppSource(backChannelEntry.getAppSource());
                }
                // ????????????
                baseSetting.setLaunchDateTime(new Date());
                // ????????????
                baseSetting.setLaunchType(1);
                baseSetting.setStockCount(0);
                baseSetting.setCategoryCode("d_ss_ticket");
                scenicSpotProductMPO.setScenicSpotProductBaseSetting(baseSetting);
                // ????????????
                ScenicSpotProductTransaction transaction = new ScenicSpotProductTransaction();
                transaction.setAppointInDate(1);
                transaction.setAppointnType(1);
                transaction.setInDay(g.getEffective());
                // ????????????????????????
                if(g.getPassTimeLimit() != null && ListUtils.isNotEmpty(g.getPassTimeLimit().getPassLimit())){
                    int time = g.getPassTimeLimit().getPassLimit().get(0).getPassLimitTime();
                    transaction.setTicketOutMinute(time);
                }

                scenicSpotProductMPO.setScenicSpotProductTransaction(transaction);
                ScenicSpotRuleMPO ruleMPO = convertRule(scenicSpotProductMPO, g, backupMPO);
                scenicSpotProductMPO.setRuleId(ruleMPO.getId());
                scenicSpotProductDao.saveProduct(scenicSpotProductMPO);
                LmmPriceRequest request = new LmmPriceRequest();
                request.setGoodsIds(g.getGoodsId());
                request.setBeginDate(DateTimeUtil.formatDate(new Date()));
                Integer days = ConfigGetter.getByFileItemInteger(LmmConfigConstants.CONFIG_FILE_LVMAMA, LmmConfigConstants.CONFIG_ITEM_PRICE_DAYS);
                if(days == null){
                    days = 60;
                }
                request.setEndDate(DateTimeUtil.formatDate(DateTimeUtil.addDay(new Date(), days)));
                List<LmmPriceProduct> priceList = getPriceList(request);
                String scenicSpotProductId = scenicSpotProductMPO.getId();
                String ruleId = ruleMPO.getId();
                priceList.forEach(p -> {
                    if(ListUtils.isEmpty(p.getGoodsList().getGoods())){
                        return;
                    }
                    p.getGoodsList().getGoods().forEach(gl -> {
                        if(ListUtils.isEmpty(gl.getPrices().getPrice())){
                            return;
                        }
                        gl.getPrices().getPrice().forEach(price ->
                            updatePrice(scenicSpotProductId, ruleId, price, g));
                        LmmPrice lmmPrice = gl.getPrices().getPrice().stream().filter(price ->
                                DateTimeUtil.parseDate(price.getDate()).getTime() >=
                                        DateTimeUtil.trancateToDate(new Date()).getTime()).findFirst().orElse(null);
                        if(lmmPrice.getAheadHour() > 0){
                            int min = lmmPrice.getAheadHour();
                            int day = 0;
                            String time;
                            if(min > 0){
                                int hour = min / 60;
                                int newMin = min % 60;
                                day = hour / 24;
                                int newHour = hour % 24;
                                time = String.format("%s:%s", newHour < 10 ? String.format("0%s", newHour) : String.valueOf(newHour),
                                        newMin < 10 ? String.format("0%s", newMin) : String.valueOf(newMin));

                            } else {
                                min = Math.abs(min);
                                int hour = min / 60;
                                int newMin = min % 60;
                                time = String.format("%s:%s", hour < 10 ? String.format("0%s", hour) : String.valueOf(hour),
                                        newMin < 10 ? String.format("0%s", newMin) : String.valueOf(newMin));
                            }
                            ScenicSpotProductMPO updateProduct = scenicSpotProductDao.getByProductId(scenicSpotProductId);
                            updateProduct.getScenicSpotProductTransaction().setBookBeforeDay(day);
                            updateProduct.getScenicSpotProductTransaction().setBookBeforeTime(time);
                            scenicSpotProductDao.saveProduct(updateProduct);
                        }
                    });
                });

                ScenicSpotProductBackupMPO scenicSpotProductBackupMPO = new ScenicSpotProductBackupMPO();
                scenicSpotProductBackupMPO.setId(commonService.getId(BizTagConst.BIZ_SCENICSPOT_PRODUCT));
                scenicSpotProductBackupMPO.setScenicSpotProduct(scenicSpotProductMPO);
                // ????????????????????????
                lmmProduct.setGoodsList(Lists.newArrayList(g));
                scenicSpotProductBackupMPO.setOriginContent(JSON.toJSONString(lmmProduct));
                scenicSpotProductBackupDao.saveScenicSpotProductBackup(scenicSpotProductBackupMPO);

                commonService.refreshList(0, scenicSpotProductMPO.getId(), 1, fresh);
                if(ListUtils.isNotEmpty(scenicSpotProductMPO.getChangedFields()) || ListUtils.isNotEmpty(ruleMPO.getChangedFields()) || fresh){
                    commonService.addScenicProductSubscribe(scenicSpotMPO, scenicSpotProductMPO, fresh);
                }
            });
        }
    }

    private ScenicSpotRuleMPO convertRule(ScenicSpotProductMPO scenicSpotProductMPO, LmmGoods g, ScenicSpotProductBackupMPO backupMPO){
        ScenicSpotRuleMPO ruleMPO;
        if(StringUtils.isBlank(scenicSpotProductMPO.getRuleId())){
            ruleMPO = new ScenicSpotRuleMPO();
            ruleMPO.setId(commonService.getId(BizTagConst.BIZ_SCENICSPOT_PRODUCT));
            ruleMPO.setRuleName("????????????");
            ruleMPO.setScenicSpotId(scenicSpotProductMPO.getScenicSpotId());
            ruleMPO.setIsCouponRule(0);
        } else {
            ruleMPO = scenicSpotRuleDao.getScenicSpotRuleById(scenicSpotProductMPO.getRuleId());
        }
        if(StringUtils.equals("Y", g.getNeedTicket())){
            ruleMPO.setInType(1);
        } else if(StringUtils.equals("N", g.getNeedTicket())){
            ruleMPO.setInType(0);
        }
        if(StringUtils.isNotBlank(g.getGoodsType())){
            if(StringUtils.equals(g.getGoodsType(), "EXPRESSTYPE_DISPLAY")){
                ruleMPO.setTicketType(1);
            } else if(StringUtils.equals(g.getGoodsType(), "NOTICETYPE_DISPLAY")){
                ruleMPO.setTicketType(0);
            } else {
                log.error("????????????????????????goodsType={}", g.getGoodsType());
            }
        }
        if(g.getBooker() != null){
            List<Integer> booker = Lists.newArrayList();
            if(g.getBooker().isEmail()){
                booker.add(3);
            }
            if(g.getBooker().isMobile()){
                booker.add(1);
            }
            if(g.getBooker().isName()){
                booker.add(0);
            }
            ruleMPO.setTicketInfos(booker);
        }
        if(g.getTraveller() != null){
            List<Integer> traveller = Lists.newArrayList();
            List<Integer> creds = Lists.newArrayList();
            if(!StringUtils.equals(g.getTraveller().getName(), "TRAV_NUM_NO")){
                traveller.add(0);
            }
            if(!StringUtils.equals(g.getTraveller().getMobile(), "TRAV_NUM_NO")){
                traveller.add(1);
            }
            if(!StringUtils.equals(g.getTraveller().getCredentials(), "TRAV_NUM_NO")){
                traveller.add(2);
                if(StringUtils.isNotBlank(g.getTraveller().getCredentialsType())){
                    creds = Arrays.asList(g.getTraveller().getCredentialsType().split("-")).stream().map(t -> {
                        switch (t) {
                            case "ID_CARD":
                                return Certificate.ID_CARD.getCode();
                            case "HUZHAO":
                                return Certificate.PASSPORT.getCode();
                            case "GANGAO":
                                return Certificate.HKM_PASS.getCode();
                            case "TAIBAO":
                                return Certificate.TW_PASS.getCode();
                            case "TAIBAOZHENG":
                                return Certificate.TW_CARD.getCode();
                            case "CHUSHENGZHENGMING":
                            case "HUKOUBO":
                                return Certificate.OTHER.getCode();
                            case "SHIBING":
                                return Certificate.SOLDIERS.getCode();
                            case "JUNGUAN":
                                return Certificate.OFFICER.getCode();
                            case "HUIXIANG":
                                return Certificate.HOME_CARD.getCode();
                            default:
                                return Certificate.ID_CARD.getCode();
                        }
                    }).collect(Collectors.toList());
                }
            }
            if(!StringUtils.equals(g.getTraveller().getEmail(), "TRAV_NUM_NO")){
                traveller.add(3);
            }
            ruleMPO.setTravellerInfos(traveller);
            ruleMPO.setTravellerTypes(creds);
        }
        if(g.getMaximum() > 0){
            ruleMPO.setLimitBuy(1);
            ruleMPO.setMaxCount(g.getMaximum());
        }
        // limitType ????????????????????????????????????????????????????????????+?????????
        // limitation  ??????????????????????????????????????????????????????????????? limitWay
        // ??????????????????>0????????????????????????????????????
        if(g.getLimitation() != null && g.getLimitation().getLimitAmount() > 0){
            LmmGoods.Limitation limitation = g.getLimitation();
            ruleMPO.setLimitBuy(1);
            // -1 ?????????????????????0??????????????????????????????maxcount
            ruleMPO.setLimitWay(-1);
            if(StringUtils.isNotBlank(limitation.getTimeType())){
                if(StringUtils.equals(limitation.getTimeType(), "orderTime")){
                    ruleMPO.setLimitBuyType(0);
                } else if(StringUtils.equals(limitation.getTimeType(), "playTime")){
                    ruleMPO.setLimitBuyType(1);
                }
            }
            ruleMPO.setLimitDay(limitation.getAmountCycle());
            if(StringUtils.isNotBlank(limitation.getLimitWay())){
                if(StringUtils.equals(limitation.getLimitWay(), "ORDERNUM")){
                    ruleMPO.setLimitWay(1);
                } else if(StringUtils.equals(limitation.getLimitWay(), "GOODSNUM")){
                    ruleMPO.setLimitWay(2);
                }
            }
            if(StringUtils.isNotBlank(limitation.getLimitType())){
                if(StringUtils.equals(limitation.getLimitType(), "phoneNum")){
                    ruleMPO.setDistinguishUser(1);
                } else if(StringUtils.equals(limitation.getLimitType(), "IDcard")){
                    ruleMPO.setDistinguishUser(2);
                } else if(StringUtils.equals(limitation.getLimitType(), "phoneAndIDCard")){
                    ruleMPO.setDistinguishUser(3);
                }
            }
            ruleMPO.setMaxCount(limitation.getLimitAmount());
            ruleMPO.setOrderStartTime(limitation.getOrderStartTime());
            ruleMPO.setOrderEndTime(limitation.getOrderEntTime());
            ruleMPO.setPlayStartTime(limitation.getPlayStartTime());
            ruleMPO.setPlayEndTime(limitation.getPlayEntTime());
        }
        // importentPoint ?????????????????????????????????????????????????????????????????????????????????????????????
        buildRefundDesc(ruleMPO, g);
        List<RefundRule> refundRules;
        if(ListUtils.isNotEmpty(g.getRules())){
            refundRules = g.getRules().stream().map(r -> {
                RefundRule refundRule = new RefundRule();
                int day = 0;
                int hour = 0;
                int min = 0;
                if(r.getAheadTime() > 0){
                    refundRule.setRefundRuleType(1);
                    // ???????????????
                    day = r.getAheadTime() / 1440 + 1;
                    // ??????????????????????????????????????????0??????????????????????????????????????????????????????
                    min = 1440 - (r.getAheadTime() % 1440);
                    if(min > 60){
                        hour = min / 60;
                        min = min % 60;
                    }
                } else if(r.getAheadTime() < 0 && r.getAheadTime() > -1440){
                    refundRule.setRefundRuleType(2);
                    // ???????????????????????????????????????????????????????????????
                    hour = r.getAheadTime() / -60;
                    min = Math.abs(r.getAheadTime()) % 60;
                } else if(r.getAheadTime() <= -1440){
                    refundRule.setRefundRuleType(4);
                    // ??????-1440?????????????????????????????????????????????+1
                    day = r.getAheadTime() / -1440;
                    min = Math.abs(r.getAheadTime()) % 1440;
                    if(min > 60){
                        hour = min / 60;
                        min = min % 60;
                    }
                } else {
                    refundRule.setRefundRuleType(5);
                }
                refundRule.setDay(day);
                refundRule.setHour(hour);
                refundRule.setMinute(min);
                if(StringUtils.equals(r.getDeductionType(), "AMOUNT")){
                    refundRule.setDeductionType(1);
                } else if(StringUtils.equals(r.getDeductionType(), "PERCENT")){
                    refundRule.setDeductionType(0);
                }
                refundRule.setFee(r.getDeductionValue());
                return refundRule;
            }).collect(Collectors.toList());
            int condition = 2;
            if(ListUtils.isNotEmpty(refundRules)) {
                // ????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
                if(refundRules.size() == 1){
                    RefundRule rr = refundRules.get(0);
                    // ?????????????????????????????????????????????????????????????????????????????????????????????????????????"??????????????????????????????"
                    if(rr.getDay() == 0 && rr.getHour() == 0
                            && rr.getMinute() == 0 && rr.getFee() == 0d && rr.getRefundRuleType() == 5){
                        condition = 0;
                    }
                    // ?????????????????????????????????????????????????????????????????????????????????????????????????????????
                    // ????????????????????????????????????????????????????????????????????????100%?????????
                }
                // ???????????? ???????????????????????????"??????"???????????????????????????100%??????????????????
                if(!refundRules.stream().anyMatch(r -> r.getRefundRuleType() == 5)){
                    RefundRule refundRule = new RefundRule();
                    refundRule.setRefundRuleType(5);
                    refundRule.setDeductionType(0);
                    refundRule.setFee(100);
                    refundRules.add(refundRule);
                }
            }
            ruleMPO.setRefundRules(refundRules);
            // ???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????consumer???????????????????????????
            if(condition == 0){
                ruleMPO.setRefundRules(null);
            }
            ruleMPO.setRefundCondition(condition);
            Map<Boolean, List<LmmGoods.Rule>> ruleMap = g.getRules().stream().collect(Collectors.groupingBy(r -> r.isChange()));
            // ?????????????????????????????????????????????????????????????????????????????????
            if(ruleMap.size() == 1 && !ruleMap.keySet().iterator().next()){
                ruleMPO.setRefundCondition(1);
            }
        }
        ruleMPO.setInAddress(g.getVisitAddress());
        ruleMPO.setCreateTime(new Date());
        ruleMPO.setUpdateTime(new Date());
        ruleMPO.setChannel(scenicSpotProductMPO.getChannel());
        ruleMPO.setValid(1);
        if(backupMPO != null){
            List<String> ruleChanged = Lists.newArrayList();
            LmmProduct backup = JSON.parseObject(backupMPO.getOriginContent(), LmmProduct.class);
            LmmGoods lmmGoods = backup.getGoodsList().stream().filter(bg -> StringUtils.equals(bg.getGoodsId(), g.getGoodsId())).findFirst().orElse(null);
            if(lmmGoods != null){
                if(!StringUtils.equals(lmmGoods.getCostInclude(), g.getCostInclude())){
                    ruleChanged.add("feeInclude");
                    ruleMPO.setFeeInclude(g.getCostInclude());
                }
                if(!StringUtils.equals(lmmGoods.getImportentPoint(), g.getImportentPoint())){
                    ruleChanged.add("refundRuleDesc");
                    buildRefundDesc(ruleMPO, g);
                }
                ruleMPO.setChangedFields(ruleChanged);
            }
        } else {
            ruleMPO.setFeeInclude(g.getCostInclude());
            buildRefundDesc(ruleMPO, g);
        }
        List<DescInfo> descInfos = Lists.newArrayList();
        if(StringUtils.isNotBlank(g.getCostNoinclude())){
            DescInfo exclude = new DescInfo();
            exclude.setTitle("???????????????");
            exclude.setContent(g.getCostNoinclude());
            descInfos.add(exclude);
        }
        if(g.getNotice() != null){
            LmmGoods.Notice notice = g.getNotice();
            StringBuffer sb = new StringBuffer();
            if(StringUtils.isNotBlank(notice.getGetTicketTime())){
                sb.append("????????????:").append(notice.getGetTicketTime()).append("<br>");
            }
            if(StringUtils.isNotBlank(notice.getGetTicketPlace())){
                sb.append("????????????:").append(notice.getGetTicketPlace()).append("<br>");
            }
            if(StringUtils.isNotBlank(notice.getEffectiveDesc())){
                sb.append("?????????:").append(notice.getEffectiveDesc()).append("<br>");
            }
            if(StringUtils.isNotBlank(notice.getWays())){
                sb.append("????????????:").append(notice.getWays()).append("<br>");
            }
            if(notice.getEnterLimit() != null && notice.getEnterLimit().isLimitFlag()){
                sb.append("????????????:").append(notice.getEnterLimit().getLimitTime()).append("<br>");
            }
            if(StringUtils.isNotBlank(sb.toString())){
                DescInfo noticeDesc = new DescInfo();
                noticeDesc.setTitle("????????????");
                noticeDesc.setContent(sb.toString());
                descInfos.add(noticeDesc);
            }
        }
        ruleMPO.setDescInfos(descInfos);
//        return commonService.compareRule(scenicSpotProductMPO.getScenicSpotId(), scenicSpotProductMPO.getId(), ruleMPO);
        scenicSpotRuleDao.saveScenicSpotRule(ruleMPO);
        return ruleMPO;
    }

    private void updatePrice(String scenicSpotProductId, String ruleId, LmmPrice price, LmmGoods g){
        ScenicSpotProductPriceMPO exist = scenicSpotProductPriceDao.getExistPrice(scenicSpotProductId, ruleId, price.getDate());
        if(exist != null){
            boolean b = false;
            if((exist.getSellPrice() == null && price.getB2bPrice() != null) || (exist.getSellPrice() != null && price.getB2bPrice() == null)
                    || (exist.getSellPrice() != null && price.getB2bPrice() != null && exist.getSellPrice().compareTo(BigDecimal.valueOf(price.getB2bPrice())) != 0)){
                exist.setSellPrice(price.getB2bPrice() == null ? null : BigDecimal.valueOf(price.getB2bPrice()));
                b = true;
            }
            if((exist.getSettlementPrice() == null && price.getSellPrice() != null)
                    || (exist.getSettlementPrice() != null && price.getSellPrice() == null)
                    || (exist.getSettlementPrice() != null && price.getSellPrice() != null && exist.getSettlementPrice().compareTo(BigDecimal.valueOf(price.getSellPrice())) != 0)){
                exist.setSettlementPrice(price.getSellPrice() == null ? null : BigDecimal.valueOf(price.getSellPrice()));
                b = true;
            }
            // ??????????????????????????????????????????????????????????????????
            int stock = price.getStock() == -1 ? 9999 : price.getStock();
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
            ScenicSpotProductPriceMPO scenicSpotProductPriceMPO = new ScenicSpotProductPriceMPO();
            scenicSpotProductPriceMPO.setId(commonService.getId(BizTagConst.BIZ_SCENICSPOT_PRODUCT));
            scenicSpotProductPriceMPO.setScenicSpotProductId(scenicSpotProductId);
            scenicSpotProductPriceMPO.setMerchantCode(g.getGoodsId());
            scenicSpotProductPriceMPO.setScenicSpotRuleId(ruleId);
            scenicSpotProductPriceMPO.setWeekDay("1,2,3,4,5,6,7");
            Integer ticketType;
            switch (g.getTicketType()){
                case "PARENTAGE":
                    ticketType = TicketType.TICKET_TYPE_3.getCode();
                    break;
                case "FAMILY":
                    ticketType = TicketType.TICKET_TYPE_4.getCode();
                    break;
                case "LOVER":
                    ticketType = TicketType.TICKET_TYPE_5.getCode();
                    break;
                case "COUPE":
                    ticketType = TicketType.TICKET_TYPE_6.getCode();
                    break;
                case "ADULT":
                    ticketType = TicketType.TICKET_TYPE_2.getCode();
                    break;
                case "CHILDREN":
                    ticketType = TicketType.TICKET_TYPE_7.getCode();
                    break;
                case "OLDMAN":
                    ticketType = TicketType.TICKET_TYPE_8.getCode();
                    break;
                case "STUDENT":
                    ticketType = TicketType.TICKET_TYPE_9.getCode();
                    break;
                case "ACTIVITY":
                    ticketType = TicketType.TICKET_TYPE_19.getCode();
                    break;
                case "SOLDIER":
                    ticketType = TicketType.TICKET_TYPE_10.getCode();
                    break;
                case "TEACHER":
                    ticketType = TicketType.TICKET_TYPE_11.getCode();
                    break;
                case "DISABILITY":
                    ticketType = TicketType.TICKET_TYPE_12.getCode();
                    break;
                case "GROUP":
                    ticketType = TicketType.TICKET_TYPE_13.getCode();
                    break;
                case "FREE":
                    ticketType = TicketType.TICKET_TYPE_1.getCode();
                    break;
                case "MAN":
                    ticketType = TicketType.TICKET_TYPE_31.getCode();
                    break;
                case "WOMAN":
                    ticketType = TicketType.TICKET_TYPE_30.getCode();
                    break;
                default:
                    ticketType = TicketType.TICKET_TYPE_1.getCode();
            }
            scenicSpotProductPriceMPO.setTicketKind(ticketType.toString());
            scenicSpotProductPriceMPO.setStartDate(price.getDate());
            scenicSpotProductPriceMPO.setEndDate(price.getDate());
            scenicSpotProductPriceMPO.setStock(price.getStock() == -1 ? 9999 : price.getStock());
            if(price.getB2bPrice() != null){
                scenicSpotProductPriceMPO.setSellPrice(BigDecimal.valueOf(price.getB2bPrice()));
            }
            if(price.getSellPrice() != null){
                scenicSpotProductPriceMPO.setSettlementPrice(BigDecimal.valueOf(price.getSellPrice()));
            }
            if(price.getMarketPrice() != null){
                scenicSpotProductPriceMPO.setMarketPrice(BigDecimal.valueOf(price.getMarketPrice()));
            }
            scenicSpotProductPriceMPO.setCreateTime(new Date());
            scenicSpotProductPriceMPO.setUpdateTime(new Date());
            scenicSpotProductPriceDao.addScenicSpotProductPrice(scenicSpotProductPriceMPO);
        }
    }

    private void updateScenic(ScenicSpotMPO scenicSpotMPO, ScenicSpotProductMPO productMPO, LmmProduct lmmProduct){
        if(scenicSpotMPO == null){
            scenicSpotMPO = scenicSpotDao.getScenicSpotById(productMPO.getScenicSpotId());
        }
        boolean b = false;
        // ???????????????????????????????????????
        if(StringUtils.isBlank(scenicSpotMPO.getBriefDesc())){
            scenicSpotMPO.setBriefDesc(StringUtil.replaceImgSrc(StringUtil.delHTMLTag(lmmProduct.getIntrodution())));
            b = true;
            log.info("?????????????????????????????????{}????????????{}?????????={}", scenicSpotMPO.getId(), productMPO.getId(), lmmProduct.getIntrodution());
        }
        if(StringUtils.isBlank(scenicSpotMPO.getCharacteristic()) && ListUtils.isNotEmpty(lmmProduct.getCharacteristic())){
            scenicSpotMPO.setCharacteristic(lmmProduct.getCharacteristic().get(0));
            b = true;
            log.info("?????????????????????????????????{}????????????{}?????????={}", scenicSpotMPO.getId(), productMPO.getId(), lmmProduct.getCharacteristic().get(0));
        }
        if((ListUtils.isEmpty(scenicSpotMPO.getCrowdNotices())
                || !scenicSpotMPO.getCrowdNotices().stream().anyMatch(c -> StringUtils.equals(c.getCrowdType(), "10")))
                && lmmProduct.getBookingInfo() != null){
            StringBuffer sb = new StringBuffer();
            if(StringUtils.isNotBlank(lmmProduct.getBookingInfo().getFreePolicy())){
                String fp = lmmProduct.getBookingInfo().getFreePolicy().replace("\r\n", "<br>").replace("\n", "<br>");
                sb.append("????????????").append("<br>")
                        .append(fp).append("<br>");
            }
            if(StringUtils.isNotBlank(lmmProduct.getBookingInfo().getOfferCrowd())){
                String oc = lmmProduct.getBookingInfo().getOfferCrowd().replace("\r\n", "<br>").replace("\n", "<br>");
                sb.append("????????????").append("<br>")
                        .append(oc).append("<br>");
            }
            CrowdNotice crowdNotice = new CrowdNotice();
            crowdNotice.setContent(sb.toString());
            crowdNotice.setCrowdType("10");
            scenicSpotMPO.setCrowdNotices(Lists.newArrayList(crowdNotice));
            b = true;
            log.info("????????????????????????????????????{}????????????{}?????????={}", scenicSpotMPO.getId(), productMPO.getId(), JSON.toJSONString(Lists.newArrayList(crowdNotice)));
        }
        if(StringUtils.isBlank(scenicSpotMPO.getTheme()) && ListUtils.isNotEmpty(lmmProduct.getProductTheme())){
            String theme = lmmProduct.getProductTheme().get(0);
            String code = tripDictionaryMapper.getCodeByName(theme, 21);
            if(StringUtils.isBlank(code)){
                List<String> codes = tripDictionaryMapper.getCodesByType(21);
                int lastCode = codes.stream().mapToInt(Integer::parseInt).max().getAsInt();
                tripDictionaryMapper.addDictionary(String.valueOf(lastCode + 1), theme, 21);
            }
            scenicSpotMPO.setTheme(code);
            b = true;
            log.info("???????????????????????????{}????????????{}?????????code={},name={}", scenicSpotMPO.getId(), productMPO.getId(), code, theme);
        }
        if(ListUtils.isEmpty(scenicSpotMPO.getImages())){
            scenicSpotMPO.setImages(UploadUtil.getNetUrlAndUpload(lmmProduct.getImages()));
            b = true;
            log.info("???????????????????????????{}????????????{}", scenicSpotMPO.getId(), productMPO.getId());
        }
        if(StringUtils.isBlank(scenicSpotMPO.getDetailDesc()) && ListUtils.isNotEmpty(lmmProduct.getPlayAttractions())){
            StringBuffer sb = new StringBuffer();
            for (LmmProduct.PlayAttraction playAttraction : lmmProduct.getPlayAttractions()) {
                String info = "";
                if(StringUtils.isNotBlank(playAttraction.getPlayInfo())){
                    info = playAttraction.getPlayInfo().replace("\r\n", "<br>").replace("\n", "<br>");
                }
                sb.append(playAttraction.getPlayName()).append("<br>")
                        .append(info).append("<br>");
                if(ListUtils.isNotEmpty(playAttraction.getPlayImages())){
                    playAttraction.getPlayImages().forEach(i ->
                        sb.append("<img src=\"").append(i).append("\"/>").append("<br>"));
                }
            }
            scenicSpotMPO.setDetailDesc(StringUtil.replaceImgSrc(StringUtil.delHTMLTag(sb.toString())));
            b = true;
            log.info("?????????????????????????????????{}????????????{}?????????={}", scenicSpotMPO.getId(), productMPO.getId(), sb.toString());
        }
        if(b){
            scenicSpotDao.saveScenicSpot(scenicSpotMPO);
            log.info("??????????????????????????????{}????????????{}", scenicSpotMPO.getId(), productMPO.getId());
        }
    }

    private void buildRefundDesc(ScenicSpotRuleMPO ruleMPO, LmmGoods g){
//        if(StringUtils.isNotBlank(g.getImportentPoint())){
//            ruleMPO.setRefundRuleDesc(StringUtil.delHTMLTag(g.getImportentPoint()));
//        } else {
//            if(StringUtils.isNotBlank(g.getRefundRuleNotice())){
//                ruleMPO.setRefundRuleDesc(StringUtil.delHTMLTag(g.getRefundRuleNotice()));
//            }
//            if(StringUtils.isNotBlank(g.getImportantNotice())){
//                if(StringUtils.isNotBlank(ruleMPO.getRefundRuleDesc())){
//                    ruleMPO.setRefundRuleDesc(String.format("%s<br>%s", ruleMPO.getRefundRuleDesc(), StringUtil.delHTMLTag(g.getImportantNotice())));
//                } else {
//                    ruleMPO.setRefundRuleDesc(StringUtil.delHTMLTag(g.getImportantNotice()));
//                }
//            }
//        }
        // ???????????????????????????????????????????????????????????????????????????????????????importentPoint?????????
        if(StringUtils.isNotBlank(g.getRefundRuleNotice())){
            ruleMPO.setRefundRuleDesc(StringUtil.delHTMLTag(g.getRefundRuleNotice()));
        }
        if(StringUtils.isNotBlank(g.getRefundRuleNotice())){
            List<DescInfo> descInfos = ruleMPO.getDescInfos();
            if(ListUtils.isEmpty(descInfos)){
                descInfos = Lists.newArrayList();
            }
            DescInfo descInfo = new DescInfo();
            descInfo.setTitle("????????????");
            descInfo.setContent(g.getRefundRuleNotice());
            descInfos.add(descInfo);
            ruleMPO.setDescInfos(descInfos);
        }
    }

    private List<DescInfo> buildDescInfos(LmmProduct lmmProduct, LmmGoods g){
        List<DescInfo> descInfos = Lists.newArrayList();
        // ?????????????????????????????????
//        if(StringUtils.isNotBlank(g.getCostNoinclude())){
//            DescInfo exclude = new DescInfo();
//            exclude.setTitle("???????????????");
//            exclude.setContent(g.getCostNoinclude());
//            descInfos.add(exclude);
//        }
        // ??????????????????????????????????????????
//        if(StringUtils.isNotBlank(lmmProduct.getIntrodution())){
//            DescInfo productDesc = new DescInfo();
//            productDesc.setTitle("????????????");
//            productDesc.setContent(lmmProduct.getIntrodution());
//            descInfos.add(productDesc);
//        }
        // ??????????????????????????????
//        if(g.getNotice() != null){
//            LmmGoods.Notice notice = g.getNotice();
//            StringBuffer sb = new StringBuffer();
//            if(StringUtils.isNotBlank(notice.getGetTicketTime())){
//                sb.append("????????????:").append(notice.getGetTicketTime()).append("<br>");
//            }
//            if(StringUtils.isNotBlank(notice.getGetTicketPlace())){
//                sb.append("????????????:").append(notice.getGetTicketPlace()).append("<br>");
//            }
//            if(StringUtils.isNotBlank(notice.getEffectiveDesc())){
//                sb.append("?????????:").append(notice.getEffectiveDesc()).append("<br>");
//            }
//            if(StringUtils.isNotBlank(notice.getWays())){
//                sb.append("????????????:").append(notice.getWays()).append("<br>");
//            }
//            if(notice.getEnterLimit() != null && notice.getEnterLimit().isLimitFlag()){
//                sb.append("????????????:").append(notice.getEnterLimit().getLimitTime()).append("<br>");
//            }
//            if(StringUtils.isNotBlank(sb.toString())){
//                DescInfo noticeDesc = new DescInfo();
//                noticeDesc.setTitle("????????????");
//                noticeDesc.setContent(sb.toString());
//                descInfos.add(noticeDesc);
//            }
//        }
        return descInfos;
    }

    private void syncScenic(List<LmmScenic> lmmScenicList){
        if(ListUtils.isNotEmpty(lmmScenicList)){
            lmmScenicList.forEach(s -> {
                try {
                    syncScenic(s, true);
                } catch (Exception e){
                    log.error("?????????????????????{},{} ??????", s.getScenicId(), s.getScenicName(), e);
                }
            });
        }
    }

    private void syncScenic(List<LmmScenic> lmmScenicList, boolean checkCity){
        if(ListUtils.isNotEmpty(lmmScenicList)){
            lmmScenicList.forEach(s -> {
                try {
                    syncScenic(s, checkCity);
                } catch (Exception e){
                    log.error("?????????????????????{},{} ??????", s.getScenicId(), s.getScenicName(), e);
                }
            });
        }
    }

    private void syncScenic(LmmScenic lmmScenic){
        syncScenic(lmmScenic, true);
    }

    private void syncScenic(LmmScenic lmmScenic, boolean checkCity){
        // ???????????????
        ScenicSpotMPO newScenic = LmmTicketConverter.convertToScenicSpotMPO(lmmScenic);
        if(checkCity){
            if(StringUtils.isBlank(newScenic.getCity())){
                log.error("???????????????[{}],[{}]????????????v2???????????????", lmmScenic.getScenicId(), lmmScenic.getScenicName());
                return;
            }
            // ???????????????
            commonService.setCity(newScenic);
            if(StringUtils.isBlank(newScenic.getCityCode())){
                Map<String, Object> data = Maps.newHashMap();
                Map<String, String> subData = Maps.newHashMap();
                subData.put("failed", "noCity");
                subData.put("channel", Constants.SUPPLIER_CODE_LMM_TICKET);
                data.put("supplier", subData);
                report(data, EventStatusEnum.FAIL);
                log.error("???????????????[{}],[{}]???????????????[{}]v2???????????????", lmmScenic.getScenicId(), lmmScenic.getScenicName(), lmmScenic.getPlaceCity());
                return;
            }
            if(StringUtils.equals(newScenic.getCity(), newScenic.getName())){
                log.error("?????????????????????[{}]?????????[{}]??????v2???????????????", newScenic.getName(), newScenic.getCity());
                return;
            }
        }
        // ????????????????????????
        commonService.updateScenicSpotMapping(lmmScenic.getScenicId().toString(), Constants.SUPPLIER_CODE_LMM_TICKET, Constants.SUPPLIER_NAME_LMM_TICKET, newScenic);
        // ????????????
        commonService.updateScenicSpotMPOBackup(newScenic, lmmScenic.getScenicId().toString(), Constants.SUPPLIER_CODE_LMM_TICKET, lmmScenic);
    }

    @Override
    public List<String> getSupplierScenicIdsV2(){
        return scenicSpotMappingDao.getScenicSpotByChannel(Constants.SUPPLIER_CODE_LMM_TICKET);
    }

    @Override
    public List<String> getSupplierProductIdsV2(){
        return scenicSpotProductDao.getSupplierProductIdByChannel(Constants.SUPPLIER_CODE_LMM_TICKET);
    }

    @Override
    public void pushUpdateV2(String product) throws JAXBException {
        log.info("???????????????????????????v2??????{}", product);
        LmmProductPushRequest request = XmlConvertUtil.convertToJava(product, LmmProductPushRequest.class);
        String changeType = request.getBody().getChangeType();
        log.info("xml???json????????????{}", JSON.toJSONString(request));
        LmmProductPushRequest.LmmPushProduct lmmPushProduct = request.getBody().getProduct();
        if(Arrays.asList("product_online", "product_create", "product_info_change").contains(changeType)){
            log.info("????????????????????????");
            syncProductListByIdV2(lmmPushProduct.getProductId().toString());
        } else if(Arrays.asList("goods_online", "goods_create", "goods_info_change", "price_change").contains(changeType)){
            log.info("????????????????????????");
            syncGoodsListByIdV2(lmmPushProduct.getGoodsId().toString());
        } else if(Arrays.asList("product_online", "product_offline").contains(changeType)){
            log.info("????????????????????????");
            Map<String, String> cond = Maps.newHashMap();
            cond.put("extendParams.productId", lmmPushProduct.getProductId().toString());
            List<ScenicSpotProductMPO> productMPOs = scenicSpotProductDao.getByCond(Constants.SUPPLIER_CODE_LMM_TICKET, cond);
            if(ListUtils.isNotEmpty(productMPOs)){
                productMPOs.forEach(p -> scenicSpotProductDao.updateStatusById(p.getId(), 3));
            }
        } else if(Arrays.asList("goods_offline").contains(changeType)){
            log.info("????????????????????????");
            ScenicSpotProductMPO productMPO = scenicSpotProductDao.getBySupplierProductId(lmmPushProduct.getGoodsId().toString(), Constants.SUPPLIER_CODE_LMM_TICKET);
            if(productMPO != null){
                scenicSpotProductDao.updateStatusById(productMPO.getId(), 3);
            }
        }
    }

    private void report(Map<String, Object> data, EventStatusEnum status){
        try {
            Event.EventBuilder eventBuilder = new Event.EventBuilder();
            eventBuilder.withIndex(huoliAtrace.getAppname(), "service");
            data.forEach((k, v) -> eventBuilder.withData(k, v));
            eventBuilder.withStatus(status);
            Event event = eventBuilder.build();
            huoliAtrace.reportEvent(event);
        } catch (Throwable e) {
            log.error("??????????????????, ????????????={}", JSON.toJSONString(data), e);
        }
    }

    // ??????????????????????????????????????????
    @Override
    public boolean syncNoCityScenic(LmmScenicListRequest request){
        List<LmmScenic> lmmScenicList = getScenicList(request);
        if(ListUtils.isEmpty(lmmScenicList)){
            return false;
        }
        if(ListUtils.isNotEmpty(lmmScenicList)){
            lmmScenicList.forEach(lmmScenic -> {
                try {
                    // ???????????????
                    ScenicSpotMPO newScenic = LmmTicketConverter.convertToScenicSpotMPO(lmmScenic);
                    if(StringUtils.isBlank(newScenic.getCity())){
                        log.error("???????????????[{}],[{}]????????????v2???????????????", lmmScenic.getScenicId(), lmmScenic.getScenicName());
                        addNoCity(lmmScenic);
                        return;
                    }
                    // ???????????????
                    commonService.setCity(newScenic);
                    if(StringUtils.isBlank(newScenic.getCityCode())){
                        log.error("???????????????[{}],[{}]???????????????[{}]v2???????????????", lmmScenic.getScenicId(), lmmScenic.getScenicName(), lmmScenic.getPlaceCity());
                        addNoCity(lmmScenic);
                        return;
                    }
                } catch (Exception e){
                    log.error("?????????????????????{},{} ??????", lmmScenic.getScenicId(), lmmScenic.getScenicName(), e);
                }
            });
        }
        return true;
    }

    private void addNoCity(LmmScenic lmmScenic){
        if(StringUtils.isBlank(lmmScenic.getScenicName())){
            return;
        }
        if(StringUtils.equals(lmmScenic.getScenicName(), lmmScenic.getPlaceProvince()) ||
                StringUtils.equals(lmmScenic.getScenicName(), lmmScenic.getPlaceXian()) ||
                StringUtils.equals(lmmScenic.getScenicName(), lmmScenic.getPlaceCountry()) ||
                StringUtils.equals(lmmScenic.getScenicName(), lmmScenic.getPlaceCity()) ){
            return;
        }
        if(StringUtils.isNotBlank(lmmScenic.getPlaceCountry()) && !StringUtils.equals("??????", lmmScenic.getPlaceCountry())){
            return;
        }
        if(Arrays.asList("??????","??????","??????").contains(StringUtils.isBlank(lmmScenic.getPlaceProvince()) ? "a" : lmmScenic.getPlaceProvince()) ||
                Arrays.asList("??????","??????","??????").contains(StringUtils.isBlank(lmmScenic.getPlaceCity()) ? "a" : lmmScenic.getPlaceCity())){
            return;
        }
        if(lmmScenic.getScenicName().contains("??????") ||
                lmmScenic.getScenicName().contains("??????") ||
                lmmScenic.getScenicName().contains("?????????") ||
                lmmScenic.getScenicName().contains("??????") ||
                lmmScenic.getScenicName().contains("??????") ||
                lmmScenic.getScenicName().contains("?????????") ||
                lmmScenic.getScenicName().contains("??????") ||
                lmmScenic.getScenicName().contains("?????????") ||
                lmmScenic.getScenicName().contains("???") ||
                lmmScenic.getScenicName().contains("??????") ||
                lmmScenic.getScenicName().contains("??????") ||
                lmmScenic.getScenicName().contains("??????") ||
                lmmScenic.getScenicName().contains("??????") ||
                lmmScenic.getScenicName().contains("??????") ||
                lmmScenic.getScenicName().contains("??????") ||
                lmmScenic.getScenicName().contains("??????") ||
                lmmScenic.getScenicName().contains("??????") ||
                lmmScenic.getScenicName().contains("??????") ||
                lmmScenic.getScenicName().contains("??????") ||
                lmmScenic.getScenicName().contains("?????????") ||
                lmmScenic.getScenicName().contains("??????") ||
                lmmScenic.getScenicName().contains("??????") ||
                lmmScenic.getScenicName().contains("??????") ||
                lmmScenic.getScenicName().contains("??????") ||
                lmmScenic.getScenicName().contains("?????????") ||
                lmmScenic.getScenicName().contains("????????????") ||
                lmmScenic.getScenicName().contains("??????") ||
                lmmScenic.getScenicName().contains("??????") ||
                lmmScenic.getScenicName().contains("??????") ||
                lmmScenic.getScenicName().contains("?????????") ||
                lmmScenic.getScenicName().contains("??????") ||
                lmmScenic.getScenicName().contains("?????????") ||
                lmmScenic.getScenicName().contains("test") ||
                lmmScenic.getScenicName().contains("??????") ||
                lmmScenic.getScenicName().contains("???????????????") ||
                lmmScenic.getScenicName().contains("????????????") ||
                lmmScenic.getScenicName().contains("?????????") ||
                lmmScenic.getScenicName().contains("??????") ||
                lmmScenic.getScenicName().contains("??????") ||
                lmmScenic.getScenicName().contains("???")){
            return;
        }
        scenicSpotDao.addNoCityScenic(lmmScenic);
    }
}
