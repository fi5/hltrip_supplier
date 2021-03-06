package com.huoli.trip.supplier.self.universal.vo.reqeust;

import lombok.Data;

import java.io.Serializable;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/7/28<br>
 */
@Data
public class UBRTicketListRequest implements Serializable {

    /**
     * SINGLE 单日票
     * YEAR 年票
     * SEASON 季票
     * VIP
     * ALL
     */
    private String type;
}
