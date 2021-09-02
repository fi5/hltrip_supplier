package com.huoli.trip.supplier.self.universal.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/8/3<br>
 */
@Data
public class UBRTicketEntity implements Serializable {

    /**
     * 产品编码
     */
    private String code;

    /**
     * 日期
     */
    private String datetime;

    /**
     * 价格
     */
    private String price;

    /**
     * 出新人
     */
    private List<UBRGuest> guest;
}
