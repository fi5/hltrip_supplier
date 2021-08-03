package com.huoli.trip.supplier.self.universal.vo.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/8/3<br>
 */
@Data
public class UBRRefundCheckResponse implements Serializable {

    /**
     * 退款服务费
     */
    @JsonProperty("refund_fee")
    private BigDecimal refundFee;

    /**
     * 调度服务费
     */
    @JsonProperty("reschedule_fee")
    private BigDecimal rescheduleFee;

    /**
     * 是否允许退款
     */
    @JsonProperty("refund_allow")
    private Boolean refundAllow;

    /**
     * 是否允许调度
     */
    @JsonProperty("reschedule_allow")
    private Boolean rescheduleAllow;

}
