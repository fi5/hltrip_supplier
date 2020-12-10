package com.huoli.trip.supplier.self.difengyun.vo;

import lombok.Data;

import java.util.List;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2020/12/8<br>
 */
@Data
public class DfyScenic {

    /**
     * 景点id
     */
    private String scenicId;

    /**
     * 景点名称
     */
    private String scenicName;

    /**
     * 景点图片
     */
    private String newPicUrl;

    /**
     * 地址
     */
    private String address;

    /**
     * 开放时间
     */
    private String bizTime;

    /**
     * 谷歌地图经纬度
     */
    private String glocation;

    /**
     * 百度地图经纬度
     */
    private String blocation;

    /**
     * 笛风专属门票列表
     */
    private List<DfyTicket> disTickets;
}
