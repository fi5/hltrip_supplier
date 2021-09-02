package com.huoli.trip.supplier.self.universal.vo;

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
public class UBRStock implements Serializable {

    /**
     * 日期
     */
    private String datetime;

    /**
     * 状态, soldout, normal
     */
    private String status;

    /**
     * 可用库存数量，供应商已经不用这个了
     */
    @Deprecated
    private int stock;
}
