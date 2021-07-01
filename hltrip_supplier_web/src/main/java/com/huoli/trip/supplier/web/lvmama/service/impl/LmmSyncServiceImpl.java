package com.huoli.trip.supplier.web.lvmama.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huoli.trip.common.constant.*;
import com.huoli.trip.common.entity.*;
import com.huoli.trip.common.entity.mpo.DescInfo;
import com.huoli.trip.common.entity.mpo.scenicSpotTicket.*;
import com.huoli.trip.common.util.CommonUtils;
import com.huoli.trip.common.util.DateTimeUtil;
import com.huoli.trip.common.util.ListUtils;
import com.huoli.trip.common.util.MongoDateUtils;
import com.huoli.trip.data.api.DataService;
import com.huoli.trip.supplier.api.DynamicProductItemService;
import com.huoli.trip.supplier.feign.client.lvmama.client.ILvmamaProductClient;
import com.huoli.trip.supplier.self.lvmama.vo.*;
import com.huoli.trip.supplier.self.lvmama.vo.push.LmmProductPushRequest;
import com.huoli.trip.supplier.self.lvmama.vo.request.*;
import com.huoli.trip.supplier.self.lvmama.vo.response.LmmGoodsListByIdResponse;
import com.huoli.trip.supplier.self.lvmama.vo.response.LmmPriceResponse;
import com.huoli.trip.supplier.self.lvmama.vo.response.LmmProductListResponse;
import com.huoli.trip.supplier.self.lvmama.vo.response.LmmScenicListResponse;
import com.huoli.trip.supplier.self.yaochufa.vo.YcfImageBase;
import com.huoli.trip.supplier.self.yaochufa.vo.YcfProduct;
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

import javax.annotation.Resource;
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
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/3/17<br>
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
            log.error("驴妈妈景点列表接口返回空");
            return false;
        }
        if(lmmScenicResponse.getState() == null){
            log.error("驴妈妈景点列表接口返回状态为空");
            return false;
        }
        if(!StringUtils.equals(lmmScenicResponse.getState().getCode(), "1000")){
            log.error("驴妈妈景点列表接口返回失败，code={}, message={}, solution={}",
                    lmmScenicResponse.getState().getCode(), lmmScenicResponse.getState().getMessage(),
                    lmmScenicResponse.getState().getSolution());
            return false;
        }
        if(ListUtils.isEmpty(lmmScenicResponse.getScenicNameList())){
            log.error("驴妈妈景点列表接口返回的数据为空");
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
            log.error("驴妈妈产品列表接口返回空");
            return null;
        }
        if(lmmProductListResponse.getState() == null){
            log.error("驴妈妈产品列表接口返回状态为空");
            return null;
        }
        if(!StringUtils.equals(lmmProductListResponse.getState().getCode(), "1000")){
            log.error("驴妈妈产品列表接口返回失败，code={}, message={}, solution={}",
                    lmmProductListResponse.getState().getCode(), lmmProductListResponse.getState().getMessage(),
                    lmmProductListResponse.getState().getSolution());
            return null;
        }
        if(ListUtils.isEmpty(lmmProductListResponse.getProductList())){
            log.error("驴妈妈产品列表接口返回的数据为空");
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
            log.error("驴妈妈产品列表接口返回空");
            return null;
        }
        if(lmmProductListResponse.getState() == null){
            log.error("驴妈妈产品列表接口返回状态为空");
            return null;
        }
        if(!StringUtils.equals(lmmProductListResponse.getState().getCode(), "1000")){
            log.error("驴妈妈产品列表接口返回失败，code={}, message={}, solution={}",
                    lmmProductListResponse.getState().getCode(), lmmProductListResponse.getState().getMessage(),
                    lmmProductListResponse.getState().getSolution());
            return null;
        }
        if(ListUtils.isEmpty(lmmProductListResponse.getProductList())){
            log.error("驴妈妈产品列表接口返回的数据为空");
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
            log.error("驴妈妈商品列表接口返回空");
            return null;
        }
        if(lmmGoodsListByIdResponse.getState() == null){
            log.error("驴妈妈商品列表接口返回状态为空");
            return null;
        }
        if(!StringUtils.equals(lmmGoodsListByIdResponse.getState().getCode(), "1000")){
            log.error("驴妈妈商品列表接口返回失败，code={}, message={}, solution={}",
                    lmmGoodsListByIdResponse.getState().getCode(), lmmGoodsListByIdResponse.getState().getMessage(),
                    lmmGoodsListByIdResponse.getState().getSolution());
            return null;
        }
        if(ListUtils.isEmpty(lmmGoodsListByIdResponse.getGoodsList())){
            log.error("驴妈妈商品列表接口返回的数据为空");
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
            log.error("驴妈妈价格接口返回空");
            return null;
        }
        if(lmmPriceResponse.getState() == null){
            log.error("驴妈妈价格接口返回状态为空");
            return null;
        }
        if(!StringUtils.equals(lmmPriceResponse.getState().getCode(), "1000")){
            log.error("驴妈妈价格接口返回失败，code={}, message={}, solution={}",
                    lmmPriceResponse.getState().getCode(), lmmPriceResponse.getState().getMessage(),
                    lmmPriceResponse.getState().getSolution());
            return null;
        }
        if(ListUtils.isEmpty(lmmPriceResponse.getPriceList())){
            log.error("驴妈妈产品列表接口返回的数据为空");
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
                log.error("驴妈妈景点{}没有城市，跳过。。", s.getScenicId());
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
                // 比对信息
                commonService.compareProductItem(newItem);
            }
            newItem.setUpdateTime(MongoDateUtils.handleTimezoneInput(new Date()));
            newItem.setOperator(Constants.SUPPLIER_CODE_LMM_TICKET);
            newItem.setOperatorName(Constants.SUPPLIER_NAME_LMM_TICKET);
            // 保存副本
            commonService.saveBackupProductItem(newItem);
            newItem.setProduct(productPO);
            newItem.setImageDetails(imageDetails);
            newItem.setImages(images);
            newItem.setMainImages(mainImages);
            newItem.setFeatures(featurePOs);
            if(ListUtils.isEmpty(newItem.getImages()) && ListUtils.isEmpty(newItem.getMainImages())){
                log.info("{}没有列表图、轮播图，设置待审核", newItem.getCode());
                newItem.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
            }
            // 已存在的景点不更新
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
            log.error("产品{},{}的商品列表为空，跳过。。", lmmProduct.getProductId(), lmmProduct.getProductName());
            return;
        }
        String itemCode = CommonUtils.genCodeBySupplier(Constants.SUPPLIER_CODE_LMM_TICKET, lmmProduct.getPlaceId());
        ProductItemPO productItemPO = productItemDao.selectByCode(itemCode);
        if(productItemPO == null){
            syncScenicListById(lmmProduct.getPlaceId());
            productItemPO = productItemDao.selectByCode(itemCode);
        }
        if(productItemPO == null){
            log.error("item没有同步到，跳过。。");
            return;
        }
        for (LmmGoods g : goodsList) {
            if(StringUtils.equals(g.getTicketSeason(), "true")){
                log.info("跳过场次票，productId={}, goodsId={}");
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
            // 是否只同步本地没有的产品
            if(PRODUCT_SYNC_MODE_ONLY_ADD == syncMode && oldProduct != null){
                log.error("驴妈妈，本次同步不包括更新本地产品，跳过，supplierProductCode={}", newProduct.getSupplierProductId());
                continue;
            }
            if(PRODUCT_SYNC_MODE_ONLY_UPDATE == syncMode && oldProduct == null){
                log.error("驴妈妈，本次同步不包括新增产品，跳过，supplierProductCode={}", newProduct.getSupplierProductId());
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
            // 保存副本
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
                    log.error("驴妈妈同步价格，产品{}本地不存在，跳过。。", productPO.getCode());
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
        log.info("接收驴妈妈产品通知。。");
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



    // ==================================↓↓↓新结构↓↓↓===============================

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
    public void syncScenicListByIdV2(String id){
        LmmScenicListByIdRequest request = new LmmScenicListByIdRequest();
        request.setScenicId(id);
        syncScenicListByIdV2(request);
    }

    @Override
    public boolean syncProductListV2(LmmProductListRequest request) {
        List<LmmProduct> lmmProductList = getProductList(request);
        if (ListUtils.isEmpty(lmmProductList)) {
            return false;
        }
        lmmProductList.forEach(p -> updateProductV2(p, p.getGoodsList()));
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

    private void updateProductV2(LmmProduct lmmProduct, List<LmmGoods> goodsList){

        if(ListUtils.isNotEmpty(goodsList)){
            goodsList.forEach(g -> {
                // 过滤到付产品
                if(StringUtils.equals(g.getPaymentType(), "offline")){
                    log.error("到付产品productId={}, goodsId={}，跳过。。", g.getProductId(), g.getGoodsId());
                    return;
                }
                ScenicSpotProductMPO scenicSpotProductMPO = scenicSpotProductDao.getBySupplierProductId(g.getGoodsId(), Constants.SUPPLIER_CODE_LMM_TICKET);
                ScenicSpotMPO scenicSpotMPO = null;
                boolean fresh = false;
                ScenicSpotProductBackupMPO backupMPO = null;
                if(scenicSpotProductMPO == null){
                    ScenicSpotMappingMPO scenicSpotMappingMPO = scenicSpotMappingDao.getScenicSpotByChannelScenicSpotIdAndChannel(lmmProduct.getPlaceId(), Constants.SUPPLIER_CODE_LMM_TICKET);
                    if(scenicSpotMappingMPO == null){
                        log.error("驴妈妈产品{}没有查到关联景点{}", lmmProduct.getProductId(), lmmProduct.getPlaceId());
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
                    scenicSpotProductMPO.setIsDel(0);
                    scenicSpotProductMPO.setSellType(1);
                    scenicSpotProductMPO.setSupplierProductId(g.getGoodsId());
                    scenicSpotProductMPO.setPayServiceType(0);
                    scenicSpotProductMPO.setChannel(Constants.SUPPLIER_CODE_LMM_TICKET);
                    // goods没有图片，都用product的
                    scenicSpotProductMPO.setImages(lmmProduct.getImages());
                    if(ListUtils.isNotEmpty(scenicSpotProductMPO.getImages())){
                        scenicSpotProductMPO.setMainImage(scenicSpotProductMPO.getImages().get(0));
                    }
                    // 动态说明   入园须知：取票时间、取票地点、有效期、限制时间 拼到一起，换行分隔，限制时间用;分隔
                    List<DescInfo> descInfos = buildDescInfos(lmmProduct, g);
                    scenicSpotProductMPO.setDescInfos(descInfos);
                    fresh = true;
                } else {
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
                                // 原来的图没有了，换一张
                                if(!lmmProduct.getImages().contains(scenicSpotProductMPO.getMainImage())){
                                    changedFields.add("mainImage");
                                    scenicSpotProductMPO.setMainImage(scenicSpotProductMPO.getImages().get(0));
                                }
                            }
                        }
//                         这个放到动态里了
                        // 又放回来了。。。
                        // 这个又放到景点简介里了
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
                                // 删除减少的
                                backup.getDescInfos().removeIf(d -> !newDescInfos.stream().map(nd -> nd.getTitle()).anyMatch(nd -> StringUtils.equals(nd, d.getTitle())));
                                // 更新有变化的
                                backup.getDescInfos().stream().forEach(d ->
                                    newDescInfos.stream().filter(nd -> StringUtils.equals(nd.getTitle(), d.getTitle()) &&
                                            StringUtils.equals(nd.getContent(), d.getContent())).findFirst().ifPresent(nd ->
                                                d.setChangedFields(Lists.newArrayList("content"))));
                                // 添加新增的(差集)
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
                // todo 补充景点数据，这里后面开始人工维护以后要去掉，否则可能会覆盖
                updateScenic(scenicSpotMPO, scenicSpotProductMPO, lmmProduct);
                if(lmmProduct.getServiceGuarantee() == null){
                    scenicSpotProductMPO.setTags(null);
                } else {
                    scenicSpotProductMPO.setTags(Lists.newArrayList(lmmProduct.getServiceGuarantee()));
                }
                if(StringUtils.equals(g.getStatus(), "true")){
                    scenicSpotProductMPO.setStatus(1);
                } else {
                    scenicSpotProductMPO.setStatus(3);
                }
                scenicSpotProductMPO.setUpdateTime(new Date());
                // 目前更新供应商端信息全覆盖
                scenicSpotProductMPO.setName(g.getGoodsName());
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
                transaction.setAppointInDate(1);
                transaction.setAppointnType(1);
                transaction.setInDay(g.getEffective());
                // 通关时间取第一个
                if(g.getPassTimeLimit() != null && ListUtils.isNotEmpty(g.getPassTimeLimit().getPassLimit())){
                    int time = g.getPassTimeLimit().getPassLimit().get(0).getPassLimitTime();
                    transaction.setTicketOutMinute(time);
                }

                scenicSpotProductMPO.setScenicSpotProductTransaction(transaction);
                ScenicSpotRuleMPO ruleMPO;
                if(StringUtils.isBlank(scenicSpotProductMPO.getRuleId())){
                    ruleMPO = new ScenicSpotRuleMPO();
                    ruleMPO.setId(commonService.getId(BizTagConst.BIZ_SCENICSPOT_PRODUCT));
                    ruleMPO.setRuleName("退改规则");
                    ruleMPO.setScenicSpotId(scenicSpotProductMPO.getScenicSpotId());
                    ruleMPO.setRuleCode(String.valueOf(System.currentTimeMillis() + (Math.random() * 1000)));
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
                        log.error("不支持的门票类型goodsType={}", g.getGoodsType());
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
                // limitType 限购类型在本地要加字典值，身份证、身份证+手机号
                // limitation  限制购买字段都要加，再加个限购数量类型对应 limitWay
                // 只有限购数量>0才有意义，否则不视为限购
                if(g.getLimitation() != null && g.getLimitation().getLimitAmount() > 0){
                    LmmGoods.Limitation limitation = g.getLimitation();
                    ruleMPO.setLimitBuy(1);
                    // -1 这些是为了防止0起作用，实际只为设置maxcount
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
                // importentPoint 放到退改说明，优先判断这个，如果没有取分开的退改规则和重要说明
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
                            // 至少前一天
                            day = r.getAheadTime() / 1440 + 1;
                            // 供应商给的分钟是到的游玩当天0点的差，逆向时间，所以要反推实际时间
                            min = 1440 - (r.getAheadTime() % 1440);
                            if(min > 60){
                                hour = min / 60;
                                min = min % 60;
                            }
                        } else if(r.getAheadTime() < 0 && r.getAheadTime() > -1440){
                            refundRule.setRefundRuleType(2);
                            // 当天和游玩后分钟就是正向时间，可以直接计算
                            hour = r.getAheadTime() / -60;
                            min = Math.abs(r.getAheadTime()) % 60;
                        } else if(r.getAheadTime() <= -1440){
                            refundRule.setRefundRuleType(4);
                            // 小于-1440的肯定是从后一天开始，所以不用+1
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
                    ruleMPO.setRefundRules(refundRules);
                    Map<Boolean, List<LmmGoods.Rule>> ruleMap = g.getRules().stream().collect(Collectors.groupingBy(r -> r.isChange()));
                    ruleMPO.setRefundCondition(2);
                    // 全部不可退就认为是不可退，其它都认为是条件退，没有全退
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
                if(StringUtils.isNotBlank(g.getCostNoinclude())){
                    List<DescInfo> descInfos = Lists.newArrayList();
                    DescInfo exclude = new DescInfo();
                    exclude.setTitle("费用不包含");
                    exclude.setContent(g.getCostNoinclude());
                    descInfos.add(exclude);
                    ruleMPO.setDescInfos(descInfos);
                }
                scenicSpotRuleDao.saveScenicSpotRule(ruleMPO);
                scenicSpotProductMPO.setRuleId(ruleMPO.getId());
                scenicSpotProductDao.saveProduct(scenicSpotProductMPO);
                LmmPriceRequest request = new LmmPriceRequest();
                request.setGoodsIds(g.getGoodsId());
                request.setBeginDate(DateTimeUtil.formatDate(new Date()));
                request.setEndDate(DateTimeUtil.formatDate(DateTimeUtil.addDay(new Date(), 45)));
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
                    });
                });

                ScenicSpotProductBackupMPO scenicSpotProductBackupMPO = new ScenicSpotProductBackupMPO();
                scenicSpotProductBackupMPO.setId(commonService.getId(BizTagConst.BIZ_SCENICSPOT_PRODUCT));
                scenicSpotProductBackupMPO.setScenicSpotProduct(scenicSpotProductMPO);
                // 备份当前这个商品
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
            // 接口供应商库存不会主动减，所以这里不会有问题
            int stock = price.getStock() == -1 ? 9999 : price.getStock();
            if(exist.getStock() != stock){
                exist.setStock(stock);
                b = true;
            }
            // 有变化才更新，避免频繁更新，mongo撑不住
            if(b){
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
            scenicSpotProductPriceDao.addScenicSpotProductPrice(scenicSpotProductPriceMPO);
        }
    }

    private void updateScenic(ScenicSpotMPO scenicSpotMPO, ScenicSpotProductMPO productMPO, LmmProduct lmmProduct){
        if(scenicSpotMPO == null){
            scenicSpotMPO = scenicSpotDao.getScenicSpotById(productMPO.getScenicSpotId());
        }
        boolean b = false;
        // 更新景点，用产品的数据填充
        if(StringUtils.isBlank(scenicSpotMPO.getBriefDesc())){
            scenicSpotMPO.setBriefDesc(lmmProduct.getIntrodution());
            b = true;
            log.info("驴妈妈补充景点简要介绍{}，用产品{}，内容={}", scenicSpotMPO.getId(), productMPO.getId(), lmmProduct.getIntrodution());
        }
        if(StringUtils.isBlank(scenicSpotMPO.getCharacteristic()) && ListUtils.isNotEmpty(lmmProduct.getCharacteristic())){
            scenicSpotMPO.setCharacteristic(lmmProduct.getCharacteristic().get(0));
            b = true;
            log.info("驴妈妈补充景点特色描述{}，用产品{}，内容={}", scenicSpotMPO.getId(), productMPO.getId(), lmmProduct.getCharacteristic().get(0));
        }
        if((ListUtils.isEmpty(scenicSpotMPO.getCrowdNotices())
                || !scenicSpotMPO.getCrowdNotices().stream().anyMatch(c -> StringUtils.equals(c.getCrowdType(), "10")))
                && lmmProduct.getBookingInfo() != null){
            StringBuffer sb = new StringBuffer();
            if(StringUtils.isNotBlank(lmmProduct.getBookingInfo().getFreePolicy())){
                String fp = lmmProduct.getBookingInfo().getFreePolicy().replace("\r\n", "<br>").replace("\n", "<br>");
                sb.append("免票政策").append("<br>")
                        .append(fp).append("<br>");
            }
            if(StringUtils.isNotBlank(lmmProduct.getBookingInfo().getOfferCrowd())){
                String oc = lmmProduct.getBookingInfo().getOfferCrowd().replace("\r\n", "<br>").replace("\n", "<br>");
                sb.append("优惠政策").append("<br>")
                        .append(oc).append("<br>");
            }
            CrowdNotice crowdNotice = new CrowdNotice();
            crowdNotice.setContent(sb.toString());
            crowdNotice.setCrowdType("10");
            scenicSpotMPO.setCrowdNotices(Lists.newArrayList(crowdNotice));
            b = true;
            log.info("驴妈妈补充景点结构化说明{}，用产品{}，内容={}", scenicSpotMPO.getId(), productMPO.getId(), JSON.toJSONString(Lists.newArrayList(crowdNotice)));
        }
        // TODO 先刷一下主题，后面去掉
//        if(StringUtils.isBlank(scenicSpotMPO.getTheme()) && ListUtils.isNotEmpty(lmmProduct.getProductTheme())){
        if(ListUtils.isNotEmpty(lmmProduct.getProductTheme())){
            String theme = lmmProduct.getProductTheme().get(0);
            String code = tripDictionaryMapper.getCodeByName(theme, 21);
            if(StringUtils.isBlank(code)){
                List<String> codes = tripDictionaryMapper.getCodesByType(21);
                int lastCode = codes.stream().mapToInt(Integer::parseInt).max().getAsInt();
                tripDictionaryMapper.addDictionary(String.valueOf(lastCode + 1), theme, 21);
            }
            scenicSpotMPO.setTheme(code);
            b = true;
            log.info("驴妈妈补充景点主题{}，用产品{}，内容code={},name={}", scenicSpotMPO.getId(), productMPO.getId(), code, theme);
        }
        if(ListUtils.isEmpty(scenicSpotMPO.getImages())){
            scenicSpotMPO.setImages(lmmProduct.getImages());
            b = true;
            log.info("驴妈妈补充景点图片{}，用产品{}", scenicSpotMPO.getId(), productMPO.getId());
        }
        // TODO 先刷一下详情，后面去掉
//        if(StringUtils.isBlank(scenicSpotMPO.getDetailDesc()) && ListUtils.isNotEmpty(lmmProduct.getPlayAttractions())){
        if(ListUtils.isNotEmpty(lmmProduct.getPlayAttractions())){
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
            scenicSpotMPO.setDetailDesc(sb.toString());
            b = true;
            log.info("驴妈妈补充景点详细介绍{}，用产品{}，内容={}", scenicSpotMPO.getId(), productMPO.getId(), sb.toString());
        }
        if(b){
            scenicSpotDao.saveScenicSpot(scenicSpotMPO);
            log.info("驴妈妈补充了一条景点{}，用产品{}", scenicSpotMPO.getId(), productMPO.getId());
        }
    }

    private void buildRefundDesc(ScenicSpotRuleMPO ruleMPO, LmmGoods g){
        if(StringUtils.isNotBlank(g.getImportentPoint())){
            ruleMPO.setRefundRuleDesc(g.getImportentPoint());
        } else {
            if(StringUtils.isNotBlank(g.getRefundRuleNotice())){
                ruleMPO.setRefundRuleDesc(g.getRefundRuleNotice());
            }
            if(StringUtils.isNotBlank(g.getImportantNotice())){
                if(StringUtils.isNotBlank(ruleMPO.getRefundRuleDesc())){
                    ruleMPO.setRefundRuleDesc(String.format("%s<br>%s", ruleMPO.getRefundRuleDesc(), g.getImportantNotice()));
                } else {
                    ruleMPO.setRefundRuleDesc(g.getImportantNotice());
                }
            }
        }
    }

    private List<DescInfo> buildDescInfos(LmmProduct lmmProduct, LmmGoods g){
        List<DescInfo> descInfos = Lists.newArrayList();
        // 产品要求这个放到规则里
//        if(StringUtils.isNotBlank(g.getCostNoinclude())){
//            DescInfo exclude = new DescInfo();
//            exclude.setTitle("费用不包含");
//            exclude.setContent(g.getCostNoinclude());
//            descInfos.add(exclude);
//        }
        // 又要放回电脑端描述了。。。。
//        if(StringUtils.isNotBlank(lmmProduct.getIntrodution())){
//            DescInfo productDesc = new DescInfo();
//            productDesc.setTitle("产品简介");
//            productDesc.setContent(lmmProduct.getIntrodution());
//            descInfos.add(productDesc);
//        }
        if(g.getNotice() != null){
            LmmGoods.Notice notice = g.getNotice();
            StringBuffer sb = new StringBuffer();
            if(StringUtils.isNotBlank(notice.getGetTicketTime())){
                sb.append("取票时间:").append(notice.getGetTicketTime()).append("<br>");
            }
            if(StringUtils.isNotBlank(notice.getGetTicketPlace())){
                sb.append("取票地点:").append(notice.getGetTicketPlace()).append("<br>");
            }
            if(StringUtils.isNotBlank(notice.getEffectiveDesc())){
                sb.append("有效期:").append(notice.getEffectiveDesc()).append("<br>");
            }
            if(StringUtils.isNotBlank(notice.getWays())){
                sb.append("入园方式:").append(notice.getWays()).append("<br>");
            }
            if(notice.getEnterLimit() != null && notice.getEnterLimit().isLimitFlag()){
                sb.append("入园限制:").append(notice.getEnterLimit().getLimitTime()).append("<br>");
            }
            if(StringUtils.isNotBlank(sb.toString())){
                DescInfo noticeDesc = new DescInfo();
                noticeDesc.setTitle("入园须知");
                noticeDesc.setContent(sb.toString());
                descInfos.add(noticeDesc);
            }
        }
        return descInfos;
    }

    private void syncScenic(List<LmmScenic> lmmScenicList){
        if(ListUtils.isNotEmpty(lmmScenicList)){
            lmmScenicList.forEach(s -> syncScenic(s));
        }
    }

    private void syncScenic(LmmScenic lmmScenic){
        // 转本地结构
        ScenicSpotMPO newScenic = LmmTicketConverter.convertToScenicSpotMPO(lmmScenic);
        if(StringUtils.isBlank(newScenic.getCity())){
            log.error("驴妈妈景点[{}],[{}]没有城市v2，跳过。。", lmmScenic.getScenicId(), lmmScenic.getScenicName());
            return;
        }
        // 设置省市区
        commonService.setCity(newScenic);
        if(StringUtils.isBlank(newScenic.getCityCode())){
            log.error("驴妈妈景点[{}],[{}]城市不存在[{}]v2，跳过。。", lmmScenic.getScenicId(), lmmScenic.getScenicName(), lmmScenic.getPlaceCity());
            return;
        }
        if(StringUtils.equals(newScenic.getCity(), newScenic.getName())){
            log.error("驴妈妈景点名称[{}]和城市[{}]相同v2，跳过。。", newScenic.getName(), newScenic.getCity());
            return;
        }
        // 同时保存映射关系
        commonService.updateScenicSpotMapping(lmmScenic.getScenicId().toString(), Constants.SUPPLIER_CODE_LMM_TICKET, Constants.SUPPLIER_NAME_LMM_TICKET, newScenic);
        // 更新备份
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
        log.info("接收驴妈妈产品通知v2。。");
        LmmProductPushRequest request = XmlConvertUtil.convertToJava(product, LmmProductPushRequest.class);
        String changeType = request.getBody().getChangeType();
        LmmProductPushRequest.LmmPushProduct lmmPushProduct = request.getBody().getProduct();
        if(Arrays.asList("product_online", "product_create", "product_info_change").contains(changeType)){
            syncProductListByIdV2(lmmPushProduct.getProductId().toString());
        } else if(Arrays.asList("goods_online", "goods_create", "goods_info_change", "price_change").contains(changeType)){
            syncGoodsListByIdV2(lmmPushProduct.getGoodsId().toString());
        } else if(Arrays.asList("product_online", "product_offline").contains(changeType)){
            Map<String, String> cond = Maps.newHashMap();
            cond.put("extendParams.productId", lmmPushProduct.getProductId().toString());
            List<ScenicSpotProductMPO> productMPOs = scenicSpotProductDao.getByCond(Constants.SUPPLIER_CODE_LMM_TICKET, cond);
            if(ListUtils.isNotEmpty(productMPOs)){
                productMPOs.forEach(p -> scenicSpotProductDao.updateStatusById(p.getId(), 3));
            }
        } else if(Arrays.asList("goods_offline").contains(changeType)){
            ScenicSpotProductMPO productMPO = scenicSpotProductDao.getBySupplierProductId(lmmPushProduct.getGoodsId().toString(), Constants.SUPPLIER_CODE_LMM_TICKET);
            if(productMPO != null){
                scenicSpotProductDao.updateStatusById(productMPO.getId(), 3);
            }
        }
    }
}
