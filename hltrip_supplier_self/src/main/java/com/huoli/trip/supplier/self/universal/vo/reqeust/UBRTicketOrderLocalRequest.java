package com.huoli.trip.supplier.self.universal.vo.reqeust;

import lombok.Data;

import java.io.Serializable;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/8/3<br>
 */
@Data
public class UBRTicketOrderLocalRequest implements Serializable {

    /**
     * 本地(hbgj)订单id
     */
    private String orderId;

    /**
     * 供应商下单请求序列化
     */
    private String ubrOrderRequest;
}
