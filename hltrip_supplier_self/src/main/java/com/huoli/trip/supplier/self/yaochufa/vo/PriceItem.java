package com.huoli.trip.supplier.self.yaochufa.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 描述: <br>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：王德铭<br>
 * 版本：1.0<br>
 * 创建日期：2020/6/19<br>
 */
@Data
public class PriceItem {
    //日期（2016-01-12）
    private Date date;
    //价格（底价模式：结算价；）
    private BigDecimal price;
}
