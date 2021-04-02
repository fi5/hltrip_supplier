package com.huoli.trip.supplier.web.service;

import com.huoli.trip.common.entity.BackChannelEntry;
import com.huoli.trip.common.entity.HodometerPO;
import com.huoli.trip.common.entity.ProductItemPO;
import com.huoli.trip.common.entity.ProductPO;
import com.huoli.trip.common.entity.mpo.AddressInfo;
import com.huoli.trip.common.entity.mpo.scenicSpotTicket.ScenicSpotMPO;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/3/2<br>
 */
public interface CommonService {

    /**
     * 获取渠道
     * @param supplierId
     * @return
     */
    BackChannelEntry getSupplierById(String supplierId);

    void compareProduct(ProductPO product);

    void compareToursProduct(ProductPO product);

    void compareProductItem(ProductItemPO productItem);

    boolean compareHodometer(HodometerPO hodometerPO);

    void saveBackupProduct(ProductPO product);

    void saveBackupProductItem(ProductItemPO productItem);

    void saveBackupHodometer(HodometerPO hodometerPO);

    void setCity(ScenicSpotMPO scenic);

    AddressInfo setCity(String provinceName, String cityName, String districtName);

    void updateScenicSpotMPOBackup(ScenicSpotMPO newScenic, String scenicId, Object origin);

    void updateScenicSpotMapping(String channelScenicId, String channel, ScenicSpotMPO newScenic);

    String getId(String bizTag);
}
