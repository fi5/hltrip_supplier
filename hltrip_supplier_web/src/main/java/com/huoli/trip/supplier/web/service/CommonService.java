package com.huoli.trip.supplier.web.service;

import com.huoli.trip.common.entity.BackChannelEntry;
import com.huoli.trip.common.entity.HodometerPO;
import com.huoli.trip.common.entity.ProductItemPO;
import com.huoli.trip.common.entity.ProductPO;
import com.huoli.trip.common.entity.mpo.AddressInfo;
import com.huoli.trip.common.entity.mpo.groupTour.GroupTourProductMPO;
import com.huoli.trip.common.entity.mpo.hotel.HotelMPO;
import com.huoli.trip.common.entity.mpo.hotelScenicSpot.HotelScenicSpotProductMPO;
import com.huoli.trip.common.entity.mpo.hotelScenicSpot.HotelScenicSpotProductSetMealMPO;
import com.huoli.trip.common.entity.mpo.scenicSpotTicket.ScenicSpotMPO;
import com.huoli.trip.common.entity.mpo.scenicSpotTicket.ScenicSpotProductMPO;

import java.util.Date;
import java.util.List;

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

    /**
     * 检查产品状态，主要把正常状态重置为异常
     * @param productPO
     * @param date
     */
    void checkProduct(ProductPO productPO, Date date);

    /**
     * 检查产品状态，主要把异常状态重置为正常
     * @param productCode
     */
    void checkProductReverse(String productCode);

    /**
     * 检查产品状态，主要把异常状态重置为正常，根据itemcode
     * @param itemCode
     */
    void checkProductReverseByItemId(String itemCode);

    void setCity(ScenicSpotMPO scenic);

    void setCity(HotelMPO hotel);

    AddressInfo setCity(String provinceName, String cityName, String districtName);

    void updateScenicSpotMPOBackup(ScenicSpotMPO newScenic, String channelScenicId, String channel, Object origin);

    void updateScenicSpotMapping(String channelScenicId, String channel, String channelName, ScenicSpotMPO newScenic);

    void updateHotelMapping(String channelHotelId, String channel, String channelName, HotelMPO hotelMPO);

    String getId(String bizTag);

    void refreshList(int type, String productId, int updateType, boolean add);

    void transTours(String code);

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

    /**
     * 迁移老版景点
     * @param codes
     */
    void transScenic(List<String> codes);

    /**
     * 设置审核城市
     */
    void setPoiReviewCity();
    /**
     * 将远程图片上传到本地
     */
    void upLoadImageToLocal(List<String> ids);

    void refreshScenicSpotDetailDesc(List<String> ids);

    String queryCityCodeByName(String cityName);

    /**
     * 清除重复的出行人模板
     * @param channel
     */
    void cleanPsTmp(String channel);

}
