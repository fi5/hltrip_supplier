package com.huoli.trip.supplier.web.lvmama.service;

import com.huoli.trip.supplier.self.lvmama.vo.LmmGoods;
import com.huoli.trip.supplier.self.lvmama.vo.LmmPriceProduct;
import com.huoli.trip.supplier.self.lvmama.vo.LmmProduct;
import com.huoli.trip.supplier.self.lvmama.vo.LmmScenic;
import com.huoli.trip.supplier.self.lvmama.vo.push.LmmProductPushRequest;
import com.huoli.trip.supplier.self.lvmama.vo.request.*;

import javax.xml.bind.JAXBException;
import java.util.List;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/3/17<br>
 */
public interface LmmSyncService {

    /**
     * 同步景点，分页批量同步
     * @param request
     */
    boolean syncScenicList(LmmScenicListRequest request);

    /**
     * 同步景点
     * @param request
     */
    void syncScenicListById(LmmScenicListByIdRequest request);

    /**
     * 同步景点
     * @param id
     */
    void syncScenicListById(String id);

    /**
     * 获取供应商景点id
     * @return
     */
    List<String> getSupplierScenicIds();

    /**
     * 同步产品、商品
     * @param request
     * @param syncMode
     * @return
     */
    boolean syncProductList(LmmProductListRequest request, int syncMode);

    /**
     * 同步价格
     * @param goodsId
     */
    void syncPrice(String goodsId);

    /**
     * 同步价格
     * @param request
     * @return
     */
    boolean syncPrice(LmmPriceRequest request);

    /**
     * 获取景区列表，分页
     * @param request
     * @return
     */
    List<LmmScenic> getScenicList(LmmScenicListRequest request);

    /**
     * 获取景区列表，id
     * @param request
     * @return
     */
    List<LmmScenic> getScenicListById(LmmScenicListByIdRequest request);

    /**
     * 获取产品、商品列表，分页
     * @param request
     * @return
     */
    List<LmmProduct> getProductList(LmmProductListRequest request);

    /**
     * 获取产品列表，id
     * @param request
     * @return
     */
    List<LmmProduct> getProductListById(LmmProductListByIdRequest request);

    /**
     * 获取商品
     * @param goodsId
     * @return
     */
    List<LmmGoods> getGoodsListById(String goodsId);

    /**
     * 获取商品列表，id
     * @param request
     * @return
     */
    List<LmmGoods> getGoodsListById(LmmGoodsListByIdRequest request);

    /**
     * 获取价格列表，商品id
     * @param request
     * @return
     */
    List<LmmPriceProduct> getPriceList(LmmPriceRequest request);

    /**
     * 同步产品
     * @param request
     * @param syncMode
     * @return
     */
    boolean syncProductListById(LmmProductListByIdRequest request, int syncMode);

    /**
     * 同步产品
     * @param productIds
     * @param syncMode
     * @return
     */
    boolean syncProductListById(String productIds, int syncMode);

    /**
     * 同步商品
     * @param request
     * @param syncMode
     * @return
     */
    boolean syncGoodsListById(LmmGoodsListByIdRequest request, int syncMode);

    /**
     * 同步商品
     * @param goodsId
     * @param syncMode
     * @return
     */
    boolean syncGoodsListById(String goodsId, int syncMode);

    /**
     * 推送更新
     * @param product
     * @throws JAXBException
     */
    void pushUpdate(String product) throws JAXBException;

    /**
     * 获取渠道产品id
     * @return
     */
    List<String> getSupplierProductIds();


    /**
     * 同步景点
     * @param request
     * @return
     */
    boolean syncScenicListV2(LmmScenicListRequest request);

    /**
     * 根据id同步
     * @param request
     */
    void syncScenicListByIdV2(LmmScenicListByIdRequest request);

    /**
     * 根据id同步
     * @param id
     */
    void syncScenicListByIdV2(String id);

    /**
     * 同步商品，分页
     * @param request
     * @return
     */
    boolean syncProductListV2(LmmProductListRequest request);

    /**
     * 同步产品，id
     * @param request
     * @return
     */
    boolean syncProductListByIdV2(LmmProductListByIdRequest request);

    /**
     * 同步产品，id
     * @param productIds
     * @return
     */
    boolean syncProductListByIdV2(String productIds);

    /**
     * 同步商品，id
     * @param request
     * @return
     */
    boolean syncGoodsListByIdV2(LmmGoodsListByIdRequest request);

    /**
     * 同步商品，id
     * @param goodsId
     * @return
     */
    boolean syncGoodsListByIdV2(String goodsId);

    /**
     * 获取供应商景点id
     * @return
     */
    List<String> getSupplierScenicIdsV2();

    /**
     * 获取供应商商品id
     * @return
     */
    List<String> getSupplierProductIdsV2();

    /**
     * 接收产品推送
     * @param product
     */
    void pushUpdateV2(String product) throws JAXBException;

    /**
     * 统计没城市景点，临时
     * @param request
     */
    boolean syncScenic(LmmScenicListRequest request);
}
