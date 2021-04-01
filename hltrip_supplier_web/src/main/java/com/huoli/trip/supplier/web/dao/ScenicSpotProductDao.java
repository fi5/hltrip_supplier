package com.huoli.trip.supplier.web.dao;

import com.huoli.trip.common.entity.mpo.scenicSpotTicket.ScenicSpotProductMPO;

import java.util.List;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/3/23<br>
 */
public interface ScenicSpotProductDao {

    /**
     * 保存产品
     * @param productMPO
     */
    void saveProduct(ScenicSpotProductMPO productMPO);

    /**
     * 新增产品
     * @param productMPO
     */
    void addProduct(ScenicSpotProductMPO productMPO);

    /**
     * 根据供应商产品id获取产品
     * @param supplierProductId
     * @param channel
     * @return
     */
    ScenicSpotProductMPO getBySupplierProductId(String supplierProductId, String channel);

    /**
     * 更新状态
     * @param id
     * @param status
     */
    void updateStatusById(String id, Integer status);

    /**
     * 查渠道产品
     * @param channel
     * @return
     */
    List<String> getSupplierProductIdByChannel(String channel);

    /**
     *
     * @param channel
     * @return
     */
    List<ScenicSpotProductMPO> getByChannel(String channel);

    /**
     * 根据id获取产品
     * @param productId
     * @return
     */
    ScenicSpotProductMPO getByProductId(String productId);
}
