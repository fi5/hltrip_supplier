package com.huoli.trip.supplier.self.universal.vo;

import lombok.Data;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/7/27<br>
 */
@Data
public class UBRTicketInfo {

    private String admissionReEntry;

    private String capacityControlType;

    private String eventCode;

    private String extProductId;

    /**
     * 单次最大购买数量
     */
    private String maxQuantity;

    /**
     * 单次最小购买数量
     */
    private String minQuantity;

    private String offlineDate;

    /**
     * 年卡季卡类型：Seasonal, Silver, Gold, Platinum
     */
    private String passType;

    /**
     * 购买年龄最大
     */
    private String personTypeMaxAge;

    /**
     * 购买年龄最小
     */
    private String personTypeMinAge;

    /**
     * 是否允许退票
     */
    private String refundable;

    /**
     * 退票费
     */
    private String serviceFee;

    /**
     * 票类别：Park Ticket, Express, VIP Experiences, Annual Pass
     */
    private String ticketCategory;

    /**
     * 快速通道票的类型
     */
    private String uepEntitlement;

    /**
     *
     */
    private String validityWindow;

    /**
     * 门票描述
     */
    private String description;

    private UBRBaseProduct baseProduct;
}
