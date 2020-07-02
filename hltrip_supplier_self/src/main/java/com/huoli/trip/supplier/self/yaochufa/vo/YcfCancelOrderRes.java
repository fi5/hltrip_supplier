package com.huoli.trip.supplier.self.yaochufa.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 描述: <br>申请取消订单返回
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：王德铭<br>
 * 版本：1.0<br>
 * 创建日期：2020/6/19<br>
 */
@Data
public class YcfCancelOrderRes implements Serializable {
    //订单状态
    private int orderStatus;
    //【要】订单编号
    private String orderId;
    //异步处理状态 0：同步即时处理（同步即时处理时OrderStatus为必填项）；1：异步处理；
    private int async;
}
