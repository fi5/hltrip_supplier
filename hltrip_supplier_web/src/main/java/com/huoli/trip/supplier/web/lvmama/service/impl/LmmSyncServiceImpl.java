package com.huoli.trip.supplier.web.lvmama.service.impl;

import com.google.common.collect.Lists;
import com.huoli.trip.common.constant.Constants;
import com.huoli.trip.common.entity.*;
import com.huoli.trip.common.util.CommonUtils;
import com.huoli.trip.common.util.DateTimeUtil;
import com.huoli.trip.common.util.ListUtils;
import com.huoli.trip.common.util.MongoDateUtils;
import com.huoli.trip.supplier.api.DynamicProductItemService;
import com.huoli.trip.supplier.feign.client.lvmama.client.ILvmamaClient;
import com.huoli.trip.supplier.self.lvmama.vo.LmmProductListRequest;
import com.huoli.trip.supplier.self.lvmama.vo.request.LmmScenicListByIdRequest;
import com.huoli.trip.supplier.self.lvmama.vo.request.LmmScenicListRequest;
import com.huoli.trip.supplier.self.lvmama.vo.response.LmmProductListResponse;
import com.huoli.trip.supplier.self.lvmama.vo.response.LmmScenicListResponse;
import com.huoli.trip.supplier.web.dao.ProductDao;
import com.huoli.trip.supplier.web.dao.ProductItemDao;
import com.huoli.trip.supplier.web.lvmama.convert.LmmTicketConverter;
import com.huoli.trip.supplier.web.lvmama.service.LmmSyncService;
import com.huoli.trip.supplier.web.service.CommonService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

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

    @Override
    public boolean syncScenicList(LmmScenicListRequest request){
        LmmScenicListResponse lmmScenicResponse = lvmamaClient.getScenicList(request);
        return updateScenic(lmmScenicResponse);
    }

    @Override
    public void syncScenicListById(LmmScenicListByIdRequest request){
        LmmScenicListResponse lmmScenicResponse = lvmamaClient.getScenicListById(request);
        updateScenic(lmmScenicResponse);
    }

    @Override
    public void syncScenicListById(String id){
        LmmScenicListByIdRequest request = new LmmScenicListByIdRequest();
        request.setScenicId(id);
        LmmScenicListResponse lmmScenicResponse = lvmamaClient.getScenicListById(request);
        updateScenic(lmmScenicResponse);
    }

    @Override
    public List<String> getSupplierScenicIds(){
        return productItemDao.selectSupplierItemIdsBySupplierIdAndType(Constants.SUPPLIER_CODE_LMM_TICKET,
                Constants.PRODUCT_ITEM_TYPE_TICKET);
    }

    private boolean updateScenic(LmmScenicListResponse lmmScenicResponse){
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
        lmmScenicResponse.getScenicNameList().forEach(s -> {
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

    public boolean syncProductList(LmmProductListRequest request, int syncMode){
        LmmProductListResponse lmmProductListResponse = lvmamaClient.getProductList(request);
        if(lmmProductListResponse == null){
            log.error("驴妈妈产品列表接口返回空");
            return false;
        }
        if(lmmProductListResponse.getState() == null){
            log.error("驴妈妈产品列表接口返回状态为空");
            return false;
        }
        if(!StringUtils.equals(lmmProductListResponse.getState().getCode(), "1000")){
            log.error("驴妈妈产品列表接口返回失败，code={}, message={}, solution={}",
                    lmmProductListResponse.getState().getCode(), lmmProductListResponse.getState().getMessage(),
                    lmmProductListResponse.getState().getSolution());
            return false;
        }
        if(ListUtils.isEmpty(lmmProductListResponse.getProductList())){
            log.error("驴妈妈产品列表接口返回的数据为空");
            return false;
        }
        if(ListUtils.isEmpty(lmmProductListResponse.getProductList())){
            log.error("驴妈妈产品列表接口返回的数据为空");
            return false;
        }
        lmmProductListResponse.getProductList().forEach(p -> {
            if(ListUtils.isEmpty(p.getGoodsList())){
                log.error("产品{},{}的商品列表为空，跳过。。", p.getProductId(), p.getProductName());
                return;
            }
            String itemCode = CommonUtils.genCodeBySupplier(Constants.SUPPLIER_CODE_LMM_TICKET, p.getPlaceId());
            p.getGoodsList().forEach(g -> {
                if(StringUtils.equals(g.getTicketSeason(), "true")){
                    log.info("跳过场次票，productId={}, goodsId={}");
                    return;
                }
                ProductPO newProduct = LmmTicketConverter.convertToProductPO(p, g);
                ProductItemPO productItemPO = productItemDao.selectByCode(itemCode);
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
                    return;
                }
                if(PRODUCT_SYNC_MODE_ONLY_UPDATE == syncMode && oldProduct == null){
                    log.error("笛风云，本次同步不包括新增产品，跳过，supplierProductCode={}", newProduct.getSupplierProductId());
                    return;
                }
                newProduct.setUpdateTime(MongoDateUtils.handleTimezoneInput(new Date()));
                newProduct.setOperator(Constants.SUPPLIER_CODE_LMM_TICKET);
                newProduct.setOperatorName(Constants.SUPPLIER_NAME_LMM_TICKET);
                newProduct.setValidTime(MongoDateUtils.handleTimezoneInput(DateTimeUtil.trancateToDate(new Date())));
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
                dynamicProductItemService.refreshItemByProductCode(Lists.newArrayList(newProduct.getCode()));
                // 保存副本
                commonService.saveBackupProduct(newProduct);
            });
        });
        return false;
    }
}
