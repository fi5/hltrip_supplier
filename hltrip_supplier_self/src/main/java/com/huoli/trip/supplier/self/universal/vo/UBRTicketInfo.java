package com.huoli.trip.supplier.self.universal.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/7/27<br>
 */
@Data
public class UBRTicketInfo implements Serializable {

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

    /**
     * 票验证类型 GID/FR 表示证件或人脸识别，QR Code 表示二维码，Alternate Media表示其他方式
     */
    private String mediaType;

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
     *
     */
    private String refundable;

    /**
     * 退票费
     * refundable=true，这里也可能是空的，但不是免费退款，退款最终是否收费，是根据退款预检查结果来的，亦或者是根据园区的规定来的
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
     * 供应商返回的产品中文名
     */
    private String description;

    /**
     * 园区返回的产品信息
     */
    private UBRBaseProduct baseProduct;

    /**
     * 距离入园几天可以改签
     * -1 表示入园日期1天前可改期，1表示入园日期1天后之前可以改期， null 表示都可改期
     */
    private Integer changePerformanceAdvanceDays;

    /**
     * 离入园日期几天前是否可退
     */
    private Integer ticketVoidAdvanceDays;

    /**
     * 票种类型 成人 Adult， 儿童 Child， 老人 Senior，通用 Guest
     */
    private String personType;
}
