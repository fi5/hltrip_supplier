package com.huoli.trip.supplier.self.universal.vo;

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
public class UBROrderDetailResponse implements Serializable {

    /**
     * 订单号
     */
    @JsonProperty("order_uid")
    private String orderUid;

    /**
     * 状态
     */
    @JsonProperty("status_display")
    private String status;
}
