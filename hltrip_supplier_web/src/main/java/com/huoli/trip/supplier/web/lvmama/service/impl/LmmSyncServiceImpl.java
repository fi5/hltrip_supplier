package com.huoli.trip.supplier.web.lvmama.service.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.huoli.trip.common.constant.Constants;
import com.huoli.trip.common.entity.*;
import com.huoli.trip.common.entity.mpo.scenicSpotTicket.ScenicSpotBackupMPO;
import com.huoli.trip.common.entity.mpo.scenicSpotTicket.ScenicSpotMPO;
import com.huoli.trip.common.entity.mpo.scenicSpotTicket.ScenicSpotMappingMPO;
import com.huoli.trip.common.entity.mpo.scenicSpotTicket.ScenicSpotProductMPO;
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
import com.huoli.trip.supplier.web.service.ScenicSpotProductService;
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

import static com.huoli.trip.supplier.self.difengyun.constant.DfyConstants.PRODUCT_SYNC_MODE_ONLY_ADD;
import static com.huoli.trip.supplier.self.difengyun.constant.DfyConstants.PRODUCT_SYNC_MODE_ONLY_UPDATE;

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
        List<LmmScenic> lmmScenicList = getScenicListById(request);
        updateScenic(lmmScenicList);
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
            ProductItemPO oldItem = productItemDao.selectByCode(newItem.getCode());
            List<ItemFeaturePO> featurePOs = null;
            ProductPO productPO = null;
            List<ImageBasePO> imageDetails = null;
            List<ImageBasePO> images = null;
            List<ImageBasePO> mainImages = null;
            if (oldItem == null) {
                oldItem.setCreateTime(MongoDateUtils.handleTimezoneInput(new Date()));
                // 笛风云跟团游默认审核通过
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
        lmmProductList.forEach(p -> {
            if(ListUtils.isEmpty(p.getGoodsList())){
                log.error("产品{},{}的商品列表为空，跳过。。", p.getProductId(), p.getProductName());
                return;
            }
            String itemCode = CommonUtils.genCodeBySupplier(Constants.SUPPLIER_CODE_LMM_TICKET, p.getPlaceId());
            ProductItemPO productItemPO = productItemDao.selectByCode(itemCode);
            if(productItemPO == null){
                syncScenicListById(p.getPlaceId());
                productItemPO = productItemDao.selectByCode(itemCode);
            }
            if(productItemPO == null){
                log.error("item没有同步到，跳过。。");
                return;
            }
            for (LmmGoods g : p.getGoodsList()) {
                if(StringUtils.equals(g.getTicketSeason(), "true")){
                    log.info("跳过场次票，productId={}, goodsId={}");
                    continue;
                }
                ProductPO newProduct = LmmTicketConverter.convertToProductPO(p, g);
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
                    log.error("笛风云，本次同步不包括更新本地产品，跳过，supplierProductCode={}", newProduct.getSupplierProductId());
                    continue;
                }
                if(PRODUCT_SYNC_MODE_ONLY_UPDATE == syncMode && oldProduct == null){
                    log.error("笛风云，本次同步不包括新增产品，跳过，supplierProductCode={}", newProduct.getSupplierProductId());
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
        });
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
                log.error("笛风云，本次同步不包括更新本地产品，跳过，supplierProductCode={}", newProduct.getSupplierProductId());
                continue;
            }
            if(PRODUCT_SYNC_MODE_ONLY_UPDATE == syncMode && oldProduct == null){
                log.error("笛风云，本次同步不包括新增产品，跳过，supplierProductCode={}", newProduct.getSupplierProductId());
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
        List<LmmScenic> lmmScenicList = getScenicListById(request);
        syncScenic(lmmScenicList);
    }

    public void syncProduct(LmmProduct lmmProduct){

        ScenicSpotProductMPO scenicSpotProductMPO = scenicSpotProductDao.getBySupplierProductId(lmmProduct.getProductId(), Constants.SUPPLIER_CODE_LMM_TICKET);
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
            scenicSpotProductMPO.setImages(lmmProduct.getImages());
            if(ListUtils.isNotEmpty(lmmProduct.getGoodsList())){
                lmmProduct.getGoodsList().forEach(g -> {
                    g.get
                });
            }
            // todo 把商品拆改对应到product上
//            scenicSpotProductMPO.set
        }
        scenicSpotProductMPO.setUpdateTime(MongoDateUtils.handleTimezoneInput(new Date()));
        scenicSpotProductMPO.setChannel(Constants.SUPPLIER_CODE_LMM_TICKET);

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
        setCity(newScenic);
        // 更新备份
        updateBackup(newScenic, lmmScenic);
        // 查映射关系
        ScenicSpotMappingMPO scenicSpotMappingMPO = scenicSpotMappingDao.getScenicSpotByChannelScenicSpotIdAndChannel(lmmScenic.getScenicId().toString(), Constants.SUPPLIER_CODE_LMM_TICKET);
        if(scenicSpotMappingMPO != null){
            return;
        }
        // 没有找到映射就往本地新增一条
        ScenicSpotMPO addScenic = scenicSpotDao.addScenicSpot(newScenic);
        // 同时保存映射关系
        updateMapping(lmmScenic, addScenic.getId());
    }

    private void updateMapping(LmmScenic lmmScenic, String ScenicSpotId){
        ScenicSpotMappingMPO scenicSpotMappingMPO = new ScenicSpotMappingMPO();
        scenicSpotMappingMPO.setChannelScenicSpotId(lmmScenic.getScenicId().toString());
        scenicSpotMappingMPO.setScenicSpotId(ScenicSpotId);
        scenicSpotMappingMPO.setChannel(Constants.SUPPLIER_CODE_LMM_TICKET);
        scenicSpotMappingMPO.setCreateTime(MongoDateUtils.handleTimezoneInput(new Date()));
        scenicSpotMappingMPO.setUpdateTime(MongoDateUtils.handleTimezoneInput(new Date()));
        scenicSpotMappingDao.addScenicSpotMapping(scenicSpotMappingMPO);
    }

    private void updateBackup(ScenicSpotMPO newScenic, LmmScenic lmmScenic){
        ScenicSpotBackupMPO scenicSpotBackupMPO = JSON.parseObject(JSON.toJSONString(newScenic), ScenicSpotBackupMPO.class);
        scenicSpotBackupMPO.setSupplierId(Constants.SUPPLIER_CODE_LMM_TICKET);
        scenicSpotBackupMPO.setSupplierScenicId(lmmScenic.getScenicId().toString());
        scenicSpotBackupMPO.setOriginContent(JSON.toJSONString(lmmScenic));
        ScenicSpotBackupMPO exist = scenicSpotBackupDao.getScenicSpotBySupplierScenicIdAndSupplierId(lmmScenic.getScenicId().toString(), Constants.SUPPLIER_CODE_LMM_TICKET);
        if(exist == null){
            scenicSpotBackupMPO.setCreateTime(MongoDateUtils.handleTimezoneInput(new Date()));
        }
        scenicSpotBackupMPO.setUpdateTime(MongoDateUtils.handleTimezoneInput(new Date()));
        scenicSpotBackupDao.saveScenicSpotBackup(scenicSpotBackupMPO);
    }

    private void setCity(ScenicSpotMPO scenic){
        if(scenic == null){
            return;
        }
        String provinceCode = null;
        String cityCode = null;
        if(StringUtils.isNotBlank(scenic.getProvince())){
            if(scenic.getProvince().endsWith("省")){
                scenic.setProvince(scenic.getProvince().substring(0, scenic.getProvince().length() - 1));
            }
            List<ChinaCity> chinaCities = chinaCityMapper.getCityByNameAndTypeAndParentId(scenic.getProvince(), 1, null);
            if(ListUtils.isNotEmpty(chinaCities)){
                provinceCode = chinaCities.get(0).getCode();
                scenic.setProvinceCode(provinceCode);
            }
        }
        if(StringUtils.isNotBlank(scenic.getCity())){
            if(scenic.getCity().endsWith("市")){
                scenic.setCity(scenic.getCity().substring(0, scenic.getCity().length() - 1));
            }
            List<ChinaCity> chinaCities = chinaCityMapper.getCityByNameAndTypeAndParentId(scenic.getCity(), 2, provinceCode);
            if(ListUtils.isNotEmpty(chinaCities)){
                cityCode = chinaCities.get(0).getCode();
                scenic.setCityCode(cityCode);
            }
        }
        if(StringUtils.isNotBlank(scenic.getDistrict())){
            List<ChinaCity> chinaCities = chinaCityMapper.getCityByNameAndTypeAndParentId(scenic.getDistrict(), 3, cityCode);
            if(ListUtils.isNotEmpty(chinaCities)){
                scenic.setDistrictCode(chinaCities.get(0).getCode());
            }
        }
    }
}
