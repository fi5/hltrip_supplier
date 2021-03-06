package com.huoli.trip.supplier.web.dao;

import com.huoli.trip.common.entity.ProductPO;

import java.util.List;
import java.util.Map;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2020/6/28<br>
 */
public interface ProductDao {

    /**
     * 同步产品
     * @param productPO
     */
    void updateByCode(ProductPO productPO);

    /**
     * 根据item查产品
     * @param itemIds
     * @return
     */
    List<ProductPO> getProductListByItemIds(List<String> itemIds);

    /**
     * 根据供应商产品id查询
     * @param supplierProductId
     * @return
     */
    ProductPO getBySupplierProductId(String supplierProductId);

    /**
     * 根据供应商id获取产品编码
     * @param supplierId
     * @return
     */
    List<ProductPO> getCodeBySupplierId(String supplierId);

    /**
     * 根据itemid获取最低价产品
     * @param itemId
     * @return
     */
    ProductPO getProductListByItemId(String itemId);

    /**
     * 根据编码获取产品
     * @param code
     * @return
     */
    ProductPO getByCode(String code);

    /**
     * 上下架
     * @param code
     * @param status
     */
    void updateStatusByCode(String code, int status);

    /**
     * 更新跟团游状态
     * @param productId
     * @param status
     */
    void updateGroupTourStatusByCode(String productId, int status);

    /**
     * 更新门票状态
     * @param productId
     * @param status
     */
    void updateScenicspotStatusByCode(String productId, int status);

    /**
     * 更新酒景状态
     * @param productId
     * @param status
     */
    void updateHotelScenicSpotStatusByCode(String productId, int status);

    /**
     * 根据状态查询产品
     * @param status
     * @return
     */
    List<ProductPO> getProductsByStatus(int status);

    /**
     * * 根据渠道获取所有渠道的产品码
     * @param supplierId
     * @return
     */
    List<ProductPO> getSupplierProductIds(String supplierId, Integer productType);

    /**
     * 根据编码模糊匹配获取产品
     * @param code
     * @return
     */
    List<ProductPO> getByRegexCode(String code);

    /**
     * 根据渠道和渠道产品id获取产品
     * @param supplierProductId
     * @param supplierId
     * @return
     */
    List<ProductPO> getBySupplierProductIdAndSupplierId(String supplierProductId, String supplierId);

    /**
     * 更新审核状态
     * @param code
     * @param verifyStatus
     */
    void updateVerifyStatusByCode(String code, int verifyStatus);

    /**
     * 修改供应商状态
     * @param code
     * @param supplierStatus
     */
    void updateSupplierStatusByCode(String code, int supplierStatus);

    /**
     * 更新appfrom
     * @param code
     * @param appFrom
     */
    void updateAppFromByCode(String code, String appFrom);

    /**
     * 获取供应商产品id
     * @param supplierId
     * @param productType
     * @return
     */
    List<String> selectSupplierProductIdsBySupplierIdAndType(String supplierId, Integer productType);

    /**
     * 根据供应商获取产品
     * @param supplierId
     * @return
     */
    List<ProductPO> getBySupplierId(String supplierId);

    /**
     *
     * @param channel
     * @param cond
     * @return
     */
    List<ProductPO> getByCond(String channel, Map<String, String> cond);

}
