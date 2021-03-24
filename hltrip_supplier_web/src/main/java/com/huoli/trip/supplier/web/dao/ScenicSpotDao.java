package com.huoli.trip.supplier.web.dao;

import com.huoli.trip.common.entity.mpo.scenicSpotTicket.ScenicSpotMPO;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/3/23<br>
 */
public interface ScenicSpotDao {

    /**
     * 匹配景点
     * @param name
     * @param address
     * @return
     */
    ScenicSpotMPO getScenicSpotByNameAndAddress(String name, String address);

    /**
     * 新增景点
     * @param scenicSpotMPO
     * @return
     */
    ScenicSpotMPO addScenicSpot(ScenicSpotMPO scenicSpotMPO);

    /**
     * 根据id查景点
     * @param id
     * @return
     */
    ScenicSpotMPO getScenicSpotById(String id);
}
