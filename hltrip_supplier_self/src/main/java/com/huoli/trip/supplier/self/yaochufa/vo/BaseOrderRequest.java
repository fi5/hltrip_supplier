package com.huoli.trip.supplier.self.yaochufa.vo;

import com.huoli.trip.common.vo.request.TraceRequest;
import lombok.Data;

import java.io.Serializable;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2020/10/27<br>
 */
@Data
public class BaseOrderRequest extends TraceRequest implements Serializable {

    /**
     * 订单号
     */
    public String orderId;

    private String supplierOrderId;//supplier订单id

}
