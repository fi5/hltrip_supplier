package com.huoli.trip.supplier.self.universal.vo;

import lombok.Data;

import java.util.List;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/7/27<br>
 */
@Data
public class UBRTicketList {

    /**
     * 当前页码
     */
    private int currentPage;

    /**
     * 总页数
     */
    private int totalPageCount;

    /**
     * 数据总数
     */
    private int totalProductCount;

    /**
     * 数据更新时间
     */
    private String dataGetDateTime;

    /**
     * 门票数据
     */
    private List<UBRTicketInfo> production;

}
