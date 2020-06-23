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
public class YcfProductLimitRule {

    /**
     * 限制类型
     * 1：身份证
     * 2：手机
     */
    private Integer buyRuleType;

    /**
     * x天内
     */
    private Integer buyDay;

    /**
     * 购买x份
     */
    private Integer buyCount;
}
