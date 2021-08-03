package com.huoli.trip.supplier.self.universal.vo.response;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class UBRTicketOrderResponse implements Serializable {

    /**
     *
     */
    @JsonProperty("order_id")
    private String orderId;

    /**
     * 状态 NORMAL 成功，BUY_FAILED 失败
     */
    private String status;
}
