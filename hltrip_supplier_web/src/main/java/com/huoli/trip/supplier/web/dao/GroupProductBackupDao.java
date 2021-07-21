package com.huoli.trip.supplier.web.dao;

import com.huoli.trip.common.entity.mpo.groupTour.GroupTourProductSetMealBackupMPO;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/4/16<br>
 */
public interface GroupProductBackupDao {

    /**
     * 保存备份
     * @param groupTourProductSetMealBackupMPO
     */
    void saveGroupProductBackup(GroupTourProductSetMealBackupMPO groupTourProductSetMealBackupMPO);

    /**
     * 根据id保存备份
     * @param groupTourProductSetMealBackupMPO
     */
    void saveGroupProductBackupById(GroupTourProductSetMealBackupMPO groupTourProductSetMealBackupMPO);

    /**
     * 根据套餐id查备份
     * @param setMealId
     * @return
     */
    GroupTourProductSetMealBackupMPO getGroupProductBackupBySetMealId(String setMealId);

    /**
     * 根据产品id查备份
     * @param productId
     * @return
     */
    GroupTourProductSetMealBackupMPO getGroupProductBackupByProductId(String productId);

    /**
     * 根据产品id、套餐id查备份
     * @param productId
     * @param setMealId
     * @return
     */
    GroupTourProductSetMealBackupMPO getGroupProductBackupByProductIdAndSetMealId(String productId, String setMealId);

}
