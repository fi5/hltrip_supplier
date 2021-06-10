package com.huoli.trip.supplier.web.dao;

import com.huoli.trip.common.entity.mpo.hotelScenicSpot.HotelScenicSpotProductBackupMPO;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/4/22<br>
 */
public interface HotelScenicProductBackupDao {

    /**
     * 保存备份
     * @param hotelScenicSpotProductBackupMPO
     */
    void saveHotelScenicSpotProductBackup(HotelScenicSpotProductBackupMPO hotelScenicSpotProductBackupMPO);

    /**
     * 根据套餐id查
     * @param setMealId
     * @return
     */
    HotelScenicSpotProductBackupMPO getHotelScenicSpotProductBackupBySetMealId(String setMealId);

    /**
     * 根据产品id查
     * @param productId
     * @return
     */
    HotelScenicSpotProductBackupMPO getHotelScenicSpotProductBackupByProductId(String productId);
}
