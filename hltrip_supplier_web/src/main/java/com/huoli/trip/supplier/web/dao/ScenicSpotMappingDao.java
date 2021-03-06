package com.huoli.trip.supplier.web.dao;

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
public interface ScenicSpotMappingDao {

    /**
     * 查询供应商景区是否已有对应
     * @param channelScenicSpotId
     * @return
     */
    ScenicSpotMappingMPO getScenicSpotByChannelScenicSpotIdAndChannel(String channelScenicSpotId, String channel);

    ScenicSpotMappingMPO addScenicSpotMapping(ScenicSpotMappingMPO scenicSpotMappingMPO);

    /**
     * 获取渠道映射对象
     * @param channel
     * @return
     */
    List<String> getScenicSpotByChannel(String channel);
}
