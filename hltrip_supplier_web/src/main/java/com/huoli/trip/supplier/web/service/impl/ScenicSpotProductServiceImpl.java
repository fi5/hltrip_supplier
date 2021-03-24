package com.huoli.trip.supplier.web.service.impl;

import com.huoli.trip.common.entity.mpo.scenicSpotTicket.ScenicSpotBackupMPO;
import com.huoli.trip.supplier.web.dao.ScenicSpotBackupDao;
import com.huoli.trip.supplier.web.service.ScenicSpotProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/3/23<br>
 */
@Service
public class ScenicSpotProductServiceImpl implements ScenicSpotProductService {

    @Autowired
    private ScenicSpotBackupDao scenicSpotBackupDao;

    @Override
    public void saveScenicSpotBackup(ScenicSpotBackupMPO scenicSpotBackupMPO){
        scenicSpotBackupDao.saveScenicSpotBackup(scenicSpotBackupMPO);
    }
}
