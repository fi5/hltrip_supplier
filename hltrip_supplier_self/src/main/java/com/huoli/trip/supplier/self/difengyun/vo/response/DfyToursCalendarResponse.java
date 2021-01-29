package com.huoli.trip.supplier.self.difengyun.vo.response;

import lombok.Data;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/1/28<br>
 */
@Data
public class DfyToursCalendarResponse {

    /**
     * 团期
     */
    private String departDate;
    /**
     * 成人分销结算价-促销前
     */
    private Double distributeAdultPrice;
    /**
     * 儿童分销结算价-促销前
     */
    private Double distributeChildPrice;
    /**
     * 成人分销结算起价-促销后（必须满足活动条件才能拿到此价格）
     */
    private Double adultDisPmfPrice;
    /**
     * 儿童分销结算起价-促销后（必须满足活动条件才能拿到此价格）
     */
    private Double childDisPmfPrice;
    /**
     * 参考C端成人起价-促销后（必须满足活动条件才能拿到此价格）
     */
    private Double retailAdultPrice;
    /**
     * 参考C端儿童起价-促销后（必须满足活动条件才能拿到此价格）
     */
    private Double retailChildPrice;
    /**
     * 单房差
     */
    private Double roomChargeprice;
    /**
     * 对应行程id
     */
    private Integer journeyId;
    /**
     * 儿童是否可订 0 支持儿童出游，1：不支持儿童出游
     */
    private Integer excludeChildFlag;
    /**
     * 报名截止时间
     */
    private String deadlineTime;
    /**
     * 库存类型  1-库存  2-FS（无限量） 3-现询（人工确认）
     */
    private Integer stockSign;
    /**
     * 库存余位（仅当stockSign=1时，才需要关注此字段）
     */
    private Integer stockNum;

}
