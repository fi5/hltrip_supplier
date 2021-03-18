package com.huoli.trip.supplier.self.lvmama.vo;

import lombok.Data;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/3/18<br>
 */
@Data
public class LmmOpenTime {
    /**
     * 开业时间说明
     */
    private String openTimeInfo;

    /**
     * 起始
     */
    private String sightStart;

    /**
     *截止
     */
    private String sightEnd;
}
