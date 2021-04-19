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

    void saveGroupProductBackup(GroupTourProductSetMealBackupMPO groupTourProductSetMealBackupMPO);

    GroupTourProductSetMealBackupMPO getGroupProductBackupBySetMealId(String setMealId);

    GroupTourProductSetMealBackupMPO getGroupProductBackupByProductId(String productId);

}
