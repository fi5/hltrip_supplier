package com.huoli.trip.supplier.self.vo.order;

import lombok.Data;

/**
 * 描述: <br>支付订单业务实体返回
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：王德铭<br>
 * 版本：1.0<br>
 * 创建日期：2020/6/18<br>
 */
@Data
public class PayOrderRes {

    //订单状态
    private int orderStatus;
    //订单编号(yaochufa)
    private String orderId;

}
