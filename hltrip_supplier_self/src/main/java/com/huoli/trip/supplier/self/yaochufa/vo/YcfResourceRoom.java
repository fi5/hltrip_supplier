package com.huoli.trip.supplier.self.yaochufa.vo;

import lombok.Data;

import java.util.Date;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2020/6/18<br>
 */
@Data
public class YcfResourceRoom {

    /**
     * 房型编号
     */
    private String roomId;

    /**
     * 房型名称
     */
    private String roomName;

    /**
     * 酒景编号
     */
    private String poiId;

    /**
     * 基准数量 当一份产品售卖两间房时，该值为2
     */
    private Integer roomBaseNum;

    /**
     * 基准晚数 当一份产品的房间为3天2晚时，该值为2
     */
    private Integer roomBaseNight;

    /**
     * 床型
     * 0：大床
     * 1：双床
     * 2：大/双床
     * 3：三床
     * 4：一单一双
     * 5：单人床
     * 6：上下铺
     * 7：通铺
     * 8：榻榻米
     * 9：水床
     * 10：圆床
     * 11：拼床
     * 12：四床
     * 16：其他床
     */
    private Integer bedType;

    /**
     * 床型尺寸
     */
    private String bedSize;

    /**
     * 早餐份数
     * 0：无早
     * 1：单早
     * 2：双早
     * 以此类推
     */
    private String breakfast;

    /**
     * 网络 多个","分隔
     * 0：不含有有线
     * 1：收费有线
     * 2：免费有线
     * 3：不含有无线
     * 4：收费无线
     * 5：免费无线
     * 支持免费有线和无线时传 2,5
     */
    private String broadNet;

    /**
     * 面积
     */
    private Double area;

    /**
     * 房间设施 多个","分隔
     * 0：空调
     * 1：冰箱
     * 2：电脑
     * 3：窗户
     * 4：电视
     * 5：阳台
     * 6：独立卫生间
     * 7：洗漱产品
     * 8：厨房
     * 9：拖鞋
     * 10：热水
     * 含有空调、冰箱时传 0,1
     */
    private String roomFac;

    /**
     * 最早到店时间
     */
    private Date earliestTime;

    /**
     * 最晚离店时间
     */
    private Date latestTime;
}
