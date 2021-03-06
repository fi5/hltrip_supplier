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
public class UBRPrice implements Serializable {

    /**
     * 日期
     */
    private String datetime;

    /**
     * 价格
     */
    private String value;
}
