package com.huoli.trip.supplier.web.dao;

import com.huoli.trip.common.entity.BackupHodometerPO;
import com.huoli.trip.common.entity.HodometerPO;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/1/29<br>
 */
public interface HodometerDao {

    /**
     * 更新行程
     * @param hodometerPO
     */
    void updateByCode(HodometerPO hodometerPO);

    /**
     * 获取行程
     * @param code
     * @return
     */
    HodometerPO getHodometerPO(String code);

    /**
     * 获取备份行程
     * @param code
     * @return
     */
    BackupHodometerPO getBackupHodometerPO(String code);

    /**
     * 更新备份表
     * @param backupHodometerPO
     */
    void updateBackupByCode(BackupHodometerPO backupHodometerPO);
}
