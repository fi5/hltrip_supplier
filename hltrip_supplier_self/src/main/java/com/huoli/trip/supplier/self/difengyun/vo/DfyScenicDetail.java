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
public class DfyScenicDetail {

    /**
     * 景点ID
     */
    private String scenicId;

    /**
     * 景点名称
     */
    private String scenicName;

    /**
     * 预订须知，该字段为Unicode编码
     */
    private String bookNotice;

    /**
     * 景点所在城市
     */
    private String cityName;

    /**
     * 景点所在省份（国外景点可能是国家）
     */
    private String provinceName;

    /**
     * 谷歌经纬度
     */
    private String glocation;

    /**
     * 百度经纬度
     */
    private String blocation;

    /**
     * 景点营业时间
     */
    private String openTime;

    /**
     * 景点地址
     */
    private String scenicAddress;

    /**
     * 景点图片。
     *
     * URL需要处理，详见《图片尺寸设置说明》
     */
    private String defaultPic;

    /**
     * 景点描述
     */
    private String scenicDescription;

    /**
     * 推荐
     */
    private String recommend;

    /**
     * 交通信息
     */
    private String trafficBus;

    /**
     * 笛风专属门票列表（高利润）
     */
    private List<DfyTicket> disTickets;

    /**
     * 笛风普通门票列表
     */
    private List<DfyTicket> ticketList;

}
