package com.huoli.trip.supplier.web.dao;

import com.huoli.trip.common.entity.mpo.scenicSpotTicket.ScenicSpotBackupMPO;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/3/23<br>
 */
public interface ScenicSpotBackupDao {

    /**
     * 保存景点备份
     * @param scenicSpotBackupMPO
     */
    void saveScenicSpotBackup(ScenicSpotBackupMPO scenicSpotBackupMPO);

    ScenicSpotBackupMPO getScenicSpotBySupplierScenicIdAndSupplierId(String supplierScenicId, String supplierId);

}
