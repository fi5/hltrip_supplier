package com.huoli.trip.supplier.self.universal.vo.response;

import lombok.Data;

import java.io.Serializable;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/7/27<br>
 */
@Data
public class UBRBaseResponse<T> implements Serializable {

    /**
     * 结果码
     */
    private Integer code;

    /**
     * 结果消息
     */
    private String msg;

    /**
     * 结果数据
     */
    private T data;
}
