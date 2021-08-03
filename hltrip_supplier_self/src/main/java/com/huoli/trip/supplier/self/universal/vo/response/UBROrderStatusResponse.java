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
public class UBROrderStatusResponse implements Serializable {

    /**
     * 状态 PROCESS 时证明订单正在处理，为 DONE 时证明已经处理完成
     */
    private String status;

    /**
     * 状态描述
     */
    @JsonProperty("status_display")
    private String statusDisplay;
}
