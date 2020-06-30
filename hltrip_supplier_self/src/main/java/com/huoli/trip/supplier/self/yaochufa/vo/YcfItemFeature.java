package com.huoli.trip.supplier.self.yaochufa.vo;

import lombok.Data;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2020/6/18<br>
 */
@Data
public class YcfItemFeature {

    /**
     * 类型
     * 1 购买须知
     * 2 交通指南
     * 3 酒景图文
     */
    private Integer type;

    /**
     * 详细描述 (可以是富文本)
     */
    private String detail;
}
