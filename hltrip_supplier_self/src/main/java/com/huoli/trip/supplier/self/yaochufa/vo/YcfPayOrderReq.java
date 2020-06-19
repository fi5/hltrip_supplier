package com.huoli.trip.supplier.self.yaochufa.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 描述: <br>支付订单请求实体
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：王德铭<br>
 * 版本：1.0<br>
 * 创建日期：2020/6/18<br>
 */
@Data
public class YcfPayOrderReq {
    //订单号(hbgj)
    private String partnerOrderId;
    //支付金额 （售价模式：总售价；底价模式：总结算价；）
    private BigDecimal price;
    //支付流水号（必须唯一）
    private String paySerialNumber;
}
