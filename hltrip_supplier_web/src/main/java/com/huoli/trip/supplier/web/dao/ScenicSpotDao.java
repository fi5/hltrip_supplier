package com.huoli.trip.supplier.web.dao;

import com.huoli.trip.common.entity.mpo.scenicSpotTicket.ScenicSpotMPO;

import java.util.List;

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
    ScenicSpotMPO getScenicSpotByNameAndAddress(String name, String cityCode);

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

    /**
     * 保存
     * @param scenicSpotMPO
     */
    void saveScenicSpot(ScenicSpotMPO scenicSpotMPO);
    /*
     * 获取描述不为空的景点
     */
    List<ScenicSpotMPO> getdetailDesc();
    /*
    * 获取包含远程图片的景点
    */
    List<ScenicSpotMPO> getNetImages();
    /*
     * 获取包含远程图片的景点
     */
    List<ScenicSpotMPO> getNetImagesByIds(List<String> ids);
    /*
     * 更新景点图片
     */
    void updateImagesById(List<String> images,String id);
    /*
     * 更新景点描述
     */
    void updateDeatailDescById(String detailDesc,String id);
}
