package com.huoli.trip.supplier.web.service;

import com.huoli.trip.common.entity.BackChannelEntry;
import com.huoli.trip.common.entity.HodometerPO;
import com.huoli.trip.common.entity.ProductItemPO;
import com.huoli.trip.common.entity.ProductPO;
import com.huoli.trip.common.entity.mpo.AddressInfo;
import com.huoli.trip.common.entity.mpo.groupTour.GroupTourProductMPO;
import com.huoli.trip.common.entity.mpo.hotelScenicSpot.HotelScenicSpotProductMPO;
import com.huoli.trip.common.entity.mpo.hotelScenicSpot.HotelScenicSpotProductSetMealMPO;
import com.huoli.trip.common.entity.mpo.scenicSpotTicket.ScenicSpotMPO;
import com.huoli.trip.common.entity.mpo.scenicSpotTicket.ScenicSpotProductMPO;

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

    void compareProduct(ProductPO product, ProductPO existProduct);

    void compareToursProduct(ProductPO product, ProductPO existProduct);

    void compareProductItem(ProductItemPO productItem);

    boolean compareHodometer(HodometerPO hodometerPO);

    void saveBackupProduct(ProductPO product);

    void saveBackupProductItem(ProductItemPO productItem);

    void saveBackupHodometer(HodometerPO hodometerPO);

    void setCity(ScenicSpotMPO scenic);

    AddressInfo setCity(String provinceName, String cityName, String districtName);

    void updateScenicSpotMPOBackup(ScenicSpotMPO newScenic, String channelScenicId, String channel, Object origin);

    void updateScenicSpotMapping(String channelScenicId, String channel, String channelName, ScenicSpotMPO newScenic);

    String getId(String bizTag);

    void refreshList(int type, String productId, int updateType, boolean add);

    void transTours();

    void transScenic();

    /**
     * 添加门票订阅通知
     * @param scenicSpotMPO
     * @param scenicSpotProductMPO
     * @param fresh
     */
    void addScenicProductSubscribe(ScenicSpotMPO scenicSpotMPO, ScenicSpotProductMPO scenicSpotProductMPO, boolean fresh);

    void addHotelProductSubscribe(HotelScenicSpotProductMPO hotelScenicSpotProductMPO, HotelScenicSpotProductSetMealMPO hotelScenicSpotProductSetMealMPO, boolean fresh);

    /**
     * 添加跟团游订阅通知
     * @param groupTourProductMPO
     * @param fresh
     */
    void addToursProductSubscribe(GroupTourProductMPO groupTourProductMPO, boolean fresh);

}
