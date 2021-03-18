package com.huoli.trip.supplier.self.lvmama.vo;

import lombok.Data;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/3/15<br>
 */
@Data
public class LmmState {

    /**
     * 状态码
     */
    private String code;

    /**
     * 状态描述
     */
    private String message;

    /**
     * 信息描述
     */
    private String solution;
}
