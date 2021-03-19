package com.huoli.trip.supplier.self.lvmama.vo;

import lombok.Data;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/3/19<br>
 */
@Data
public class LmmPrice {

    /**
     *yyyy-MM-dd
     */
    private String date;

    /**
     * 市场价格
     */
    private Double marketPrice;

    /**
     * 分销商下单时所使用 的价格，单位元;
     * B2C 时为驴妈妈售价;
     * B2B 时为结算价(下单 也使用该价格)。
     */
    private Double sellPrice;

    /**
     * 产品为 B2B 时显示该 字段，单位元，
     * 可用于 B2B 零售价格
     */
    private  Double b2bPrice;

    /**
     * 库存
     * -1 代表不限库存;
     * 正整数代表库存量.
     */
    private int stock;

    /**
     *  前预订时间(单位:分钟)
     */
    private int aheadHour;
}
