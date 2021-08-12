package com.huoli.trip.supplier.self.universal.vo.response;

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
public class UBRRefundCheckResponseCustom extends UBRRefundCheckResponse implements Serializable {

    /**
     * 渠道退款金额
     */
    private BigDecimal refundPrice;

}
