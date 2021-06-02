package com.huoli.trip.supplier.web.lvmama.service;

import com.huoli.trip.supplier.self.lvmama.vo.LmmGoods;
import com.huoli.trip.supplier.self.lvmama.vo.LmmPriceProduct;
import com.huoli.trip.supplier.self.lvmama.vo.LmmProduct;
import com.huoli.trip.supplier.self.lvmama.vo.LmmScenic;
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
     * @param productId
     * @param syncMode
     * @return
     */
    boolean syncProductListById(String productId, int syncMode);

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
}
