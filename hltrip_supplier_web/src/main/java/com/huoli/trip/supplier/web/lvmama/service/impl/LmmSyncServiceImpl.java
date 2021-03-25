package com.huoli.trip.supplier.web.lvmama.service.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.huoli.trip.common.constant.Certificate;
import com.huoli.trip.common.constant.Constants;
import com.huoli.trip.common.constant.ProductType;
import com.huoli.trip.common.constant.TicketType;
import com.huoli.trip.common.entity.*;
import com.huoli.trip.common.entity.mpo.scenicSpotTicket.*;
import com.huoli.trip.common.util.CommonUtils;
import com.huoli.trip.common.util.DateTimeUtil;
import com.huoli.trip.common.util.ListUtils;
import com.huoli.trip.common.util.MongoDateUtils;
import com.huoli.trip.supplier.api.DynamicProductItemService;
import com.huoli.trip.supplier.feign.client.lvmama.client.ILvmamaClient;
import com.huoli.trip.supplier.self.lvmama.vo.*;
import com.huoli.trip.supplier.self.lvmama.vo.request.*;
import com.huoli.trip.supplier.self.lvmama.vo.response.LmmGoodsListByIdResponse;
import com.huoli.trip.supplier.self.lvmama.vo.response.LmmPriceResponse;
import com.huoli.trip.supplier.self.lvmama.vo.response.LmmProductListResponse;
import com.huoli.trip.supplier.self.lvmama.vo.response.LmmScenicListResponse;
import com.huoli.trip.supplier.web.dao.*;
import com.huoli.trip.supplier.web.lvmama.convert.LmmTicketConverter;
import com.huoli.trip.supplier.web.lvmama.service.LmmSyncService;
import com.huoli.trip.supplier.web.mapper.ChinaCityMapper;
import com.huoli.trip.supplier.web.service.CommonService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    private ILvmamaClient lvmamaClient;

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
    private ChinaCityMapper chinaCityMapper;

    @Autowired
    private ScenicSpotMappingDao scenicSpotMappingDao;

    @Autowired
    private ScenicSpotDao scenicSpotDao;

    @Autowired
    private ScenicSpotBackupDao scenicSpotBackupDao;

    @Autowired
    private ScenicSpotProductDao scenicSpotProductDao;

    @Autowired
    private ScenicSpotRuleDao scenicSpotRuleDao;

    @Autowired
    private ScenicSpotProductPriceDao scenicSpotProductPriceDao;

    @Autowired
    private ScenicSpotProductPriceRuleDao scenicSpotProductPriceRuleDao;

    @Override
    public List<LmmScenic> getScenicList(LmmScenicListRequest request){
        LmmScenicListResponse lmmScenicResponse = lvmamaClient.getScenicList(request);
        if(!checkLmmScenicListResponse(lmmScenicResponse)){
            return null;
        }
        return lmmScenicResponse.getScenicNameList();
    }

    @Override
    public List<LmmScenic> getScenicListById(LmmScenicListByIdRequest request){
        LmmScenicListResponse lmmScenicResponse = lvmamaClient.getScenicListById(request);
        if(!checkLmmScenicListResponse(lmmScenicResponse)){
            return null;
        }
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

    @Override
    public List<LmmProduct> getProductList(LmmProductListRequest request){
        LmmProductListResponse lmmProductListResponse = lvmamaClient.getProductList(request);
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
        return lmmProductListResponse.getProductList();
    }

    @Override
    public List<LmmProduct> getProductListById(LmmProductListByIdRequest request){
        LmmProductListResponse lmmProductListResponse = lvmamaClient.getProductListById(request);
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
        LmmGoodsListByIdResponse lmmGoodsListByIdResponse = lvmamaClient.getGoodsListById(request);
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
        if(ListUtils.isEmpty(lmmGoodsListByIdResponse.getGoodList())){
            log.error("驴妈妈商品列表接口返回的数据为空");
            return null;
        }
        return lmmGoodsListByIdResponse.getGoodList();
    }

    @Override
    public List<LmmPriceProduct> getPriceList(LmmPriceRequest request){
        LmmPriceResponse lmmPriceResponse = lvmamaClient.getPriceList(request);
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

    @Override
    public List<String> getSupplierProductIds(){
        return productDao.selectSupplierProductIdsBySupplierIdAndType(Constants.SUPPLIER_CODE_LMM_TICKET,
                ProductType.SCENIC_TICKET.getCode());
    }

    private boolean updateScenic(List<LmmScenic> lmmScenicList){
        if(ListUtils.isEmpty(lmmScenicList)){
            return false;
        }
        lmmScenicList.forEach(s -> {
            ProductItemPO newItem = LmmTicketConverter.convertToProductItemPO(s);
            ProductItemPO oldItem = productItemDao.selectByCode(newItem.getCode());
            List<ItemFeaturePO> featurePOs = null;
            ProductPO productPO = null;
            List<ImageBasePO> imageDetails = null;
            List<ImageBasePO> images = null;
            List<ImageBasePO> mainImages = null;
            if (oldItem == null) {
                oldItem.setCreateTime(MongoDateUtils.handleTimezoneInput(new Date()));
                oldItem.setAuditStatus(Constants.VERIFY_STATUS_PASSING);
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
            productItemDao.updateByCode(newItem);
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
            // todo 不知道他们是不是每次都返回product下的所有goodsid,如果不是的话本地的有些数据可能变成垃圾，需要查出来一起更新
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
            if(oldProduct == null){
                newProduct.setCreateTime(MongoDateUtils.handleTimezoneInput(new Date()));
                // todo 暂时默认通过
//                product.setAuditStatus(Constants.VERIFY_STATUS_WAITING);
                newProduct.setAuditStatus(Constants.VERIFY_STATUS_PASSING);
                newProduct.setSupplierStatus(Constants.SUPPLIER_STATUS_OPEN);
                BackChannelEntry backChannelEntry = commonService.getSupplierById(newProduct.getSupplierId());
                if(backChannelEntry == null
                        || backChannelEntry.getStatus() == null
                        || backChannelEntry.getStatus() != 1){
                    newProduct.setSupplierStatus(Constants.SUPPLIER_STATUS_CLOSED);
                }
                if(backChannelEntry != null || StringUtils.isNotBlank(backChannelEntry.getAppSource())){
                    List<String> appFroms = Arrays.asList(backChannelEntry.getAppSource().split(","));
                    newProduct.setAppFrom(appFroms);
                }
            } else {
                newProduct.setAuditStatus(oldProduct.getAuditStatus());
                newProduct.setSupplierStatus(oldProduct.getSupplierStatus());
                newProduct.setRecommendFlag(oldProduct.getRecommendFlag());
                newProduct.setAppFrom(oldProduct.getAppFrom());
                newProduct.setBookDescList(oldProduct.getBookDescList());
                newProduct.setDescriptions(oldProduct.getDescriptions());
                newProduct.setBookNoticeList(oldProduct.getBookNoticeList());
                commonService.compareProduct(newProduct);
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
            if(ListUtils.isEmpty(p.getGoodsList())){
                return;
            }
            p.getGoodsList().forEach(g -> {
                if(ListUtils.isEmpty(g.getPrices())){
                    return;
                }
                String productCode = CommonUtils.genCodeBySupplier(Constants.SUPPLIER_CODE_LMM_TICKET, g.getGoodsId());
                ProductPO productPO = productDao.getByCode(productCode);
                if(productPO == null){
                    log.error("驴妈妈同步价格，产品{}本地不存在，跳过。。", productPO.getCode());
                    return;
                }
                List<PriceInfoPO> priceInfoPOs = Lists.newArrayList();
                g.getPrices().forEach(price -> {
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
                PricePO pricePO = new PricePO();
                pricePO.setPriceInfos(priceInfoPOs);
                priceDao.updateByProductCode(pricePO);
            });
        });
        return true;
    }



    // ==================================↓↓↓新结构↓↓↓===============================

    @Override
    public boolean syncScenicListV2(LmmScenicListRequest request){
        List<LmmScenic> lmmScenicList = getScenicList(request);
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
            // todo 不知道他们是不是每次都返回product下的所有goodsid,如果不是的话本地的有些数据可能变成垃圾，需要查出来一起更新
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
    public boolean syncGoodsListByIdV2(String productIds){
        LmmGoodsListByIdRequest request = new LmmGoodsListByIdRequest();
        request.setGoodsIds(productIds);
        return syncGoodsListByIdV2(request);
    }


    private void updateProductV2(LmmProduct lmmProduct, List<LmmGoods> goodsList){
        // todo 预定须知、产品简介、特色说明、公告、景点描述等目前都没有
        if(ListUtils.isNotEmpty(goodsList)){
            goodsList.forEach(g -> {
                ScenicSpotProductMPO scenicSpotProductMPO = scenicSpotProductDao.getBySupplierProductId(g.getGoodsId(), Constants.SUPPLIER_CODE_LMM_TICKET);
                if(scenicSpotProductMPO == null){
                    scenicSpotProductMPO = new ScenicSpotProductMPO();
                    scenicSpotProductMPO.setCreateTime(MongoDateUtils.handleTimezoneInput(new Date()));
                    ScenicSpotMappingMPO scenicSpotMappingMPO = scenicSpotMappingDao.getScenicSpotByChannelScenicSpotIdAndChannel(lmmProduct.getPlaceId(), Constants.SUPPLIER_CODE_LMM_TICKET);
                    if(scenicSpotMappingMPO == null){
                        log.error("驴妈妈产品{}没有查到关联景点{}", lmmProduct.getProductId(), lmmProduct.getPlaceId());
                        return;
                    }
                    ScenicSpotMPO scenicSpotMPO = scenicSpotDao.getScenicSpotById(scenicSpotMappingMPO.getScenicSpotId());
                    if(scenicSpotMPO == null){
                        log.error("景点{}不存在", scenicSpotMPO.getId());
                        return;
                    }
                    scenicSpotProductMPO.setScenicSpotId(scenicSpotMPO.getId());
                }
                scenicSpotProductMPO.setSupplierProductId(g.getGoodsId());
                if(StringUtils.equals(g.getStatus(), "true")){
                    scenicSpotProductMPO.setStatus(1);
                } else {
                    scenicSpotProductMPO.setStatus(3);
                }
                // 默认未删除
                scenicSpotProductMPO.setIsDel(0);
                scenicSpotProductMPO.setUpdateTime(MongoDateUtils.handleTimezoneInput(new Date()));
                scenicSpotProductMPO.setChannel(Constants.SUPPLIER_CODE_LMM_TICKET);
                // 目前更新供应商端信息全覆盖
                // goods没有图片，都用product的
                scenicSpotProductMPO.setImages(lmmProduct.getImages());
                scenicSpotProductMPO.setName(g.getGoodsName());
                if(ListUtils.isNotEmpty(scenicSpotProductMPO.getImages())){
                    scenicSpotProductMPO.setMainImage(scenicSpotProductMPO.getImages().get(0));
                }
                // 基础设置
                ScenicSpotProductBaseSetting baseSetting = new ScenicSpotProductBaseSetting();
                BackChannelEntry backChannelEntry = commonService.getSupplierById(scenicSpotProductMPO.getChannel());
                if(backChannelEntry != null || StringUtils.isNotBlank(backChannelEntry.getAppSource())){
                    baseSetting.setAppSource(backChannelEntry.getAppSource());
                }
                // 默认当前
                baseSetting.setLaunchDateTime(MongoDateUtils.handleTimezoneInput(new Date()));
                // 默认及时
                baseSetting.setLaunchType(1);
                baseSetting.setStockCount(0);
                scenicSpotProductMPO.setScenicSpotProductBaseSetting(baseSetting);
                // 交易设置
                ScenicSpotProductTransaction transaction = new ScenicSpotProductTransaction();
                transaction.setAppointInDate(1);
                transaction.setAppointnType(1);
                transaction.setInDay(g.getEffective());
                // todo 限制时间（HH:mm）目前没有  g.getNotice().getEnterLimit().getLimitTime(), 取票相关的没有:取票时间，取票地点，入园方式，g.getNotice() ;是否取票
                scenicSpotProductMPO.setScenicSpotProductTransaction(transaction);
                // todo  票种说明要不要，放到哪儿
                ScenicSpotRuleMPO ruleMPO = new ScenicSpotRuleMPO();
                ruleMPO.setScenicSpotId(scenicSpotProductMPO.getScenicSpotId());
                ruleMPO.setRuleCode(String.valueOf(System.currentTimeMillis() + (Math.random() * 1000)));
                ruleMPO.setIsCouponRule(0);
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
                    // -1 这些是为了防止0起作用，实际只为设置maxcount
                    ruleMPO.setLimitBuyType(-1);
                    ruleMPO.setRangeType(-1);
                    ruleMPO.setDistinguishUser(-1);
                    ruleMPO.setMaxCount(g.getMaximum());
                }
                ruleMPO.setRefundRuleDesc(g.getRefundRuleNotice());
                List<RefundRule> refundRules;
                if(ListUtils.isNotEmpty(g.getRules())){
                    refundRules = g.getRules().stream().map(r -> {
                        RefundRule refundRule = new RefundRule();
                        if(r.getAheadTime() > 0){
                            refundRule.setRefundRuleType(1);
                        } else if(r.getAheadTime() < 0 && r.getAheadTime() > -1440){
                            refundRule.setRefundRuleType(2);
                        } else if(r.getAheadTime() <= -1440){
                            refundRule.setRefundRuleType(4);
                        } else {
                            refundRule.setRefundRuleType(5);
                        }
                        if(r.isChange()){
                            refundRule.setRefundCondition(2);
                        } else {
                            refundRule.setRefundCondition(1);
                        }
                        if(StringUtils.equals(r.getDeductionType(), "AMOUNT")){
                            refundRule.setDeductionType(1);
                        } else if(StringUtils.equals(r.getDeductionType(), "PERCENT")){
                            refundRule.setDeductionType(0);
                        }
                        refundRule.setFee(r.getDeductionValue());
                        return refundRule;
                    }).collect(Collectors.toList());
                    ruleMPO.setRefundRules(refundRules);
                }
                ruleMPO.setInAddress(g.getVisitAddress());
                ruleMPO.setFeeInclude(g.getCostInclude());
                // todo 费用不包含没有
                // todo 补充说明用重要说明赋值有没有问题
                ruleMPO.setSupplementDesc(g.getImportantNotice());
                ruleMPO.setCreateTime(MongoDateUtils.handleTimezoneInput(new Date()));
                ruleMPO.setUpdateTime(MongoDateUtils.handleTimezoneInput(new Date()));
                ruleMPO.setChannel(scenicSpotProductMPO.getChannel());
                ruleMPO.setValid(1);
                ruleMPO = scenicSpotRuleDao.addScenicSpotRule(ruleMPO);
                LmmPriceRequest request = new LmmPriceRequest();
                request.setGoodsIds(g.getGoodsId());
                request.setBeginDate(DateTimeUtil.formatDate(new Date()));
                request.setEndDate(DateTimeUtil.formatDate(DateTimeUtil.addDay(new Date(), 45)));
                List<LmmPriceProduct> priceList = getPriceList(request);
                String scenicSpotProductId = scenicSpotProductMPO.getId();
                String ruleId = ruleMPO.getId();
                priceList.forEach(p -> {
                    if(ListUtils.isEmpty(p.getGoodsList())){
                        return;
                    }
                    p.getGoodsList().forEach(gl -> {
                        ScenicSpotProductPriceMPO scenicSpotProductPriceMPO = new ScenicSpotProductPriceMPO();
                        scenicSpotProductPriceMPO.setScenicSpotProductId(scenicSpotProductId);
                        scenicSpotProductPriceMPO.setSellType(1);
                        scenicSpotProductPriceMPO = scenicSpotProductPriceDao.addScenicSpotProductPrice(scenicSpotProductPriceMPO);
                        String priceId = scenicSpotProductPriceMPO.getId();
                        if(ListUtils.isEmpty(gl.getPrices())){
                            return;
                        }
                        gl.getPrices().forEach(price -> {
                            ScenicSpotProductPriceRuleMPO priceRuleMPO = new ScenicSpotProductPriceRuleMPO();
                            priceRuleMPO.setScenicSpotProductPriceId(priceId);
                            priceRuleMPO.setScenicSpotRuleId(ruleId);
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
                            priceRuleMPO.setTicketKind(ticketType.toString());
                            priceRuleMPO.setStartDate(price.getDate());
                            priceRuleMPO.setEndDate(price.getDate());
                            priceRuleMPO.setStock(price.getStock());
                            if(price.getB2bPrice() != null){
                                priceRuleMPO.setSellPrice(BigDecimal.valueOf(price.getB2bPrice()));
                            }
                            if(price.getSellPrice() != null){
                                priceRuleMPO.setSettlementPrice(BigDecimal.valueOf(price.getSellPrice()));
                            }
                            scenicSpotProductPriceRuleDao.addScenicSpotProductPriceRule(priceRuleMPO);
                        });
                    });
                });
            });
        }
    }

    private void syncScenic(List<LmmScenic> lmmScenicList){
        if(ListUtils.isNotEmpty(lmmScenicList)){
            lmmScenicList.forEach(s -> syncScenic(s));
        }
    }

    private void syncScenic(LmmScenic lmmScenic){
        // 转本地结构
        ScenicSpotMPO newScenic = LmmTicketConverter.convertToScenicSpotMPO(lmmScenic);
        // 设置省市区
        commonService.setCity(newScenic);
        // 更新备份
        commonService.updateScenicSpotMPOBackup(newScenic, lmmScenic.getScenicId().toString(), lmmScenic);
        // 同时保存映射关系
        commonService.updateScenicSpotMapping(lmmScenic.getScenicId().toString(), Constants.SUPPLIER_CODE_LMM_TICKET, newScenic);
    }

    @Override
    public List<String> getSupplierScenicIdsV2(){
        return scenicSpotMappingDao.getScenicSpotByChannel(Constants.SUPPLIER_CODE_LMM_TICKET);
    }

    @Override
    public List<String> getSupplierProductIdsV2(){
        return scenicSpotProductDao.getSupplierProductIdByChannel(Constants.SUPPLIER_CODE_LMM_TICKET);
    }

}
