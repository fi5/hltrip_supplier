package com.huoli.trip.supplier.web.dao;

import com.huoli.trip.common.entity.mpo.hotelScenicSpot.HotelMappingMPO;
import com.huoli.trip.common.entity.mpo.scenicSpotTicket.ScenicSpotMappingMPO;

import java.util.List;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/3/23<br>
 */
public interface HotelMappingDao {

    /**
     * 查询供应商酒店是否已有对应
     * @param channelHotelId
     * @return
     */
    HotelMappingMPO getHotelByChannelHotelIdAndChannel(String channelHotelId, String channel);

    HotelMappingMPO addHotelMapping(HotelMappingMPO hotelMappingMPO);

    /**
     * 获取渠道映射对象
     * @param channel
     * @return
     */
    List<String> getScenicSpotByChannel(String channel);
}
