package com.huoli.trip.supplier.self.yaochufavo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;


/**
 * 退款通知请求对象
 */
@Data
public class RefundNoticeRequest implements Serializable {
    /**
     * 订单号
     * 必填
     */
    private String partnerOrderId;
    /**
     * 退款状态
     * 0：待处理；
     * 1：退款成功；
     * -1：退款失败。
     */
    private int refundStatus;
    /**
     * 退款由来
     * 1：由合作商发起的退款；
     * 2：由要出发发起的退款
     */
    private int refundFrom;

    /**
     * 退款金额
     * 售价模式：总售价；
     * 底价模式：总结算价；
     */
    private BigDecimal refundPrice;

    /**
     * 退款手续费
     *
     */
    private BigDecimal refundCharge;

    /**
     * 退款申请时间
     * 客人发起退款的时间
     */
    private Date refundTime;

    /**
     * 退款处理时间
     * 要出发处理完退款的时间
     */
    private Date responseTime;
    /**
     * 退款原因
     * 合】退款的原因
     */
    private String refundReason;

    /**
     * 处理备注
     * 【要】处理退款的备注
     */
    private String handleRemark;
}
