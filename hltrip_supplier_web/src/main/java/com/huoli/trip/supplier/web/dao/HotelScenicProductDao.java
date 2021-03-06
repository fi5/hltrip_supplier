package com.huoli.trip.supplier.web.dao;

import com.huoli.trip.common.entity.mpo.hotelScenicSpot.HotelScenicSpotProductMPO;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/4/21<br>
 */
public interface HotelScenicProductDao {

    /**
     * 查询酒景产品
     * @param supplierProductId
     * @param channel
     * @return
     */
    HotelScenicSpotProductMPO getBySupplierProductId(String supplierProductId, String channel);

    /**
     * 保存酒景产品
     * @param productMPO
     */
    void saveProduct(HotelScenicSpotProductMPO productMPO);

    HotelScenicSpotProductMPO getByProductId(String productId);
}
