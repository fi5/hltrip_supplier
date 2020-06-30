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
public class YcfResourceTicket {

    /**
     * 票种编号
     */
    private String ticketID;

    /**
     * 票种名称
     */
    private String ticketName;

    /**
     * 酒景编号
     */
    private String poiId;

    /**
     * 基准数量 当一个产品售卖两张票时，该值为2
     */
    private Integer ticketBaseNum;

}
