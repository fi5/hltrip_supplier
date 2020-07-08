package com.huoli.trip.supplier.web.yaochufa.service.impl;

import com.alibaba.fastjson.JSON;
import com.huoli.trip.common.constant.ProductType;
import com.huoli.trip.common.entity.PricePO;
import com.huoli.trip.common.entity.ProductItemPO;
import com.huoli.trip.common.entity.ProductPO;
import com.huoli.trip.common.util.ListUtils;
import com.huoli.trip.supplier.feign.client.yaochufa.client.IYaoChuFaClient;
import com.huoli.trip.supplier.self.yaochufa.constant.YcfConstants;
import com.huoli.trip.supplier.self.yaochufa.vo.*;
import com.huoli.trip.supplier.self.yaochufa.vo.basevo.YcfBaseRequest;
import com.huoli.trip.supplier.self.yaochufa.vo.basevo.YcfBaseResult;
import com.huoli.trip.supplier.web.dao.PriceDao;
import com.huoli.trip.supplier.web.dao.ProductDao;
import com.huoli.trip.supplier.web.dao.ProductItemDao;
import com.huoli.trip.supplier.web.yaochufa.convert.YcfConverter;
import com.huoli.trip.supplier.web.yaochufa.service.YcfSyncService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2020/6/28<br>
 */
@Service
@Slf4j
public class YcfSyncServiceImpl implements YcfSyncService {

    @Autowired
    private ProductDao productDao;

    @Autowired
    private ProductItemDao productItemDao;

    @Autowired
    private PriceDao priceDao;

    @Autowired
    private IYaoChuFaClient yaoChuFaClient;

    public void syncProduct(List<YcfProduct> ycfProducts){
        if(ListUtils.isEmpty(ycfProducts)){
            log.error("要出发推送的产品列表为空");
            return;
        }
        ycfProducts.forEach(ycfProduct -> {
            if(StringUtils.isBlank(ycfProduct.getPoiId())){
                log.info("要出发推送的产品没有主项目id,过滤掉。。");
                return;
            }
            if(!YcfConstants.PRODUCT_TYPE_LIST.contains(ycfProduct.getProductType())){
                log.info("要出发推送了未知类型产品，类型={}", ycfProduct.getProductType());
                return;
            }
            if(ycfProduct.getProductType() == YcfConstants.PRODUCT_TYPE_ROOM){
                log.info("要出发推送的单房，过滤掉。。");
                return;
            }
            if(ycfProduct.getRoomChoiceNum() != null && ycfProduct.getRoomOptionNum() != null &&
                    ycfProduct.getRoomChoiceNum() != ycfProduct.getRoomOptionNum()){
                log.info("酒店可选和必选不一样（M选N），过滤掉。。");
                return;
            }
            if(ycfProduct.getFoodChoiceNum() != null && ycfProduct.getFoodOptionNum() != null &&
                    ycfProduct.getFoodChoiceNum() != ycfProduct.getFoodOptionNum()){
                log.info("餐饮可选和必选不一样（M选N），过滤掉。。");
                return;
            }
            if(ycfProduct.getTicketChoiceNum() != null && ycfProduct.getTicketOptionNum() != null &&
                    ycfProduct.getTicketChoiceNum() != ycfProduct.getTicketOptionNum()){
                log.info("景点可选和必选不一样（M选N），过滤掉。。");
                return;
            }
            ProductPO productPO = YcfConverter.convertToProductPO(ycfProduct);
            if(ycfProduct.getProductType() == YcfConstants.PRODUCT_TYPE_FOOD){
                productPO.setProductType(ProductType.RESTAURANT.getCode());
            } else if(ycfProduct.getProductType() == YcfConstants.PRODUCT_TYPE_TICKET){
                productPO.setProductType(ProductType.SCENIC_TICKET.getCode());
            } else {
                if(ListUtils.isNotEmpty(ycfProduct.getRoomList())){
                    productPO.setProductType(ProductType.FREE_TRIP.getCode());
                } else if(ListUtils.isNotEmpty(ycfProduct.getTicketList())){
                    productPO.setProductType(ProductType.SCENIC_TICKET_PLUS.getCode());
                } else if(ListUtils.isNotEmpty(ycfProduct.getFoodList())){
                    productPO.setProductType(ProductType.RESTAURANT.getCode());
                } else {
                    log.error("要出发无法归类productId={}，过滤掉，套餐里没有任何具体poi", ycfProduct.getProductID());
                    return;
                }
            }
            syncProductItem(ycfProduct.getProductItemIds());
            ProductItemPO productItemPO = productItemDao.selectByCode(productPO.getMainItemCode());
            productPO.setMainItem(productItemPO);
            log.info("项目：{}", JSON.toJSONString(productPO));
            productPO.setCity(productItemPO.getCity());
            productDao.updateBySupplierProductId(productPO);
        });
    }

    public void syncProductItem(List<String> productItemIds){
        log.info("开始同步poi，id list = {}", JSON.toJSONString(productItemIds));
        if(ListUtils.isEmpty(productItemIds)){
            log.error("同步poi失败，poi id集合为空");
            return;
        }
        YcfGetPoiRequest ycfGetPoiRequest = new YcfGetPoiRequest();
        ycfGetPoiRequest.setPoiIdList(productItemIds);
        YcfBaseRequest ycfBaseRequest = new YcfBaseRequest(ycfGetPoiRequest);
        log.info("准备请求供应商(要出发)获取poi接口，参数={}", JSON.toJSONString(ycfGetPoiRequest));
        YcfBaseResult<YcfGetPoiResponse> baseResult = yaoChuFaClient.getPoi(ycfBaseRequest);
        log.info("供应商(要出发)获取poi接口返回，结果={}", JSON.toJSONString(baseResult));
        if(baseResult.getSuccess() && StringUtils.equals(baseResult.getStatusCode(), String.valueOf(YcfConstants.RESULT_CODE_SUCCESS))){
            YcfGetPoiResponse response = baseResult.getData();
            if(response == null){
                log.error("同步poi失败，供应商（要出发）没有返回data");
                return;
            }
            List<YcfProductItem> ycfProductItems = response.getPoiList();
            if(ListUtils.isEmpty(ycfProductItems)){
                log.error("同步poi失败，供应商（要出发）没有返回poi信息");
                return;
            }
            ycfProductItems.forEach(item -> {
                try {
                    ProductItemPO productItemPO = YcfConverter.convertToProductItemPO(item);
                    productItemDao.updateBySupplierItemId(productItemPO);
                } catch (Exception e) {
                    log.error("poi落地失败，", e);
                }
            });
        }
    }

    public void getPrice(YcfGetPriceRequest request){
        YcfBaseRequest ycfBaseRequest = new YcfBaseRequest(request);
        log.info("准备请求供应商(要出发)获取价格接口，参数={}", JSON.toJSONString(request));
        YcfBaseResult<YcfGetPriceResponse> baseResult = yaoChuFaClient.getPrice(ycfBaseRequest);
        log.info("供应商(要出发)获取价格接口返回，结果={}", JSON.toJSONString(baseResult));
        if(baseResult.getSuccess() && StringUtils.equals(baseResult.getStatusCode(), String.valueOf(YcfConstants.RESULT_CODE_SUCCESS))){
            YcfGetPriceResponse response = baseResult.getData();
            if(response == null){
                log.error("获取价格失败，供应商（要出发）没有返回data");
                return;
            }
            YcfPrice ycfPrice = new YcfPrice();
            ycfPrice.setProductID(response.getProductID());
            ycfPrice.setSaleInfos(response.getSaleInfos());
            syncPrice(ycfPrice);
        }
    }

    public void syncPrice(YcfPrice ycfPrice){
        PricePO pricePO = YcfConverter.convertToPricePO(ycfPrice);
        ProductPO productPO = productDao.getBySupplierProductId(ycfPrice.getProductID());
        if(productPO != null){
            pricePO.setProductCode(productPO.getCode());
        } else {
            log.error("同步价格日历，根据供应商产品id={} 没有查到数据", ycfPrice.getProductID());
            return;
        }
        priceDao.updateBySupplierProductId(pricePO);
    }
}
