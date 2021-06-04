package com.huoli.trip.supplier.web.lvmama.service.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huoli.trip.common.constant.Constants;
import com.huoli.trip.common.constant.ProductType;
import com.huoli.trip.common.entity.*;
import com.huoli.trip.common.entity.mpo.scenicSpotTicket.ScenicSpotProductMPO;
import com.huoli.trip.common.util.CommonUtils;
import com.huoli.trip.common.util.DateTimeUtil;
import com.huoli.trip.common.util.ListUtils;
import com.huoli.trip.common.util.MongoDateUtils;
import com.huoli.trip.supplier.api.DynamicProductItemService;
import com.huoli.trip.supplier.feign.client.lvmama.client.ILvmamaClient;
import com.huoli.trip.supplier.self.lvmama.vo.*;
import com.huoli.trip.supplier.self.lvmama.vo.push.LmmProductPushRequest;
import com.huoli.trip.supplier.self.lvmama.vo.request.*;
import com.huoli.trip.supplier.self.lvmama.vo.response.LmmGoodsListByIdResponse;
import com.huoli.trip.supplier.self.lvmama.vo.response.LmmPriceResponse;
import com.huoli.trip.supplier.self.lvmama.vo.response.LmmProductListResponse;
import com.huoli.trip.supplier.self.lvmama.vo.response.LmmScenicListResponse;
import com.huoli.trip.supplier.web.dao.PriceDao;
import com.huoli.trip.supplier.web.dao.ProductDao;
import com.huoli.trip.supplier.web.dao.ProductItemDao;
import com.huoli.trip.supplier.web.lvmama.convert.LmmTicketConverter;
import com.huoli.trip.supplier.web.lvmama.service.LmmSyncService;
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
    public boolean syncProductListById(String productId, int syncMode){
        LmmProductListByIdRequest request = new LmmProductListByIdRequest();
        request.setProductIds(productId);
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
    public boolean syncGoodsListById(String goodsId, int syncMode){
        LmmGoodsListByIdRequest request = new LmmGoodsListByIdRequest();
        request.setGoodsIds(goodsId);
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
            Map<String, String> params = Maps.newHashMap();
            params.put("productId", lmmProduct.getProductId());
            newProduct.setExtendParams(params);
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
    public List<String> getSupplierProductIds(){
        return productDao.selectSupplierProductIdsBySupplierIdAndType(Constants.SUPPLIER_CODE_LMM_TICKET,
                ProductType.SCENIC_TICKET.getCode());
    }
