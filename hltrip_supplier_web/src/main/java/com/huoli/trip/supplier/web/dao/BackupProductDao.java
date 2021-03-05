package com.huoli.trip.supplier.web.dao;

import com.huoli.trip.common.entity.BackupProductItemPO;
import com.huoli.trip.common.entity.BackupProductPO;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/3/3<br>
 */
public interface BackupProductDao {

    /**
     * 查询备份产品
     * @param code
     * @return
     */
    BackupProductPO getBackupProductByCode(String code);

    /**
     * 查询备份item
     * @param code
     * @return
     */
    BackupProductItemPO getBackupProductItemByCode(String code);

    /**
     * 更新产品备份
     * @param backupProductPO
     */
    void updateBackupProductByCode(BackupProductPO backupProductPO);

    /**
     * 更新item备份
     * @param backupProductItemPO
     */
    void updateBackupProductItemByCode(BackupProductItemPO backupProductItemPO);
}
