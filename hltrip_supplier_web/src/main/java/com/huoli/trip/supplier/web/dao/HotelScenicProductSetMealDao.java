package com.huoli.trip.supplier.web.dao;

import com.huoli.trip.common.entity.mpo.hotelScenicSpot.HotelScenicSpotProductSetMealMPO;

import java.util.List;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/4/21<br>
 */
public interface HotelScenicProductSetMealDao {

    /**
     * 根据产品id获取
     * @param productId
     * @return
     */
    List<HotelScenicSpotProductSetMealMPO> getByProductId(String productId);

    /**
     * 保存套餐
     * @param setMealMPO
     */
    void saveProduct(HotelScenicSpotProductSetMealMPO setMealMPO);

    HotelScenicSpotProductSetMealMPO getSetMealByPackageId(String packageId);

    void updatePriceStock(HotelScenicSpotProductSetMealMPO hotelScenicSpotProductSetMealMPO);
}
