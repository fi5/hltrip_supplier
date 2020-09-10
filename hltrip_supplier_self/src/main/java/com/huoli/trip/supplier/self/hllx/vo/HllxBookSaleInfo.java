package com.huoli.trip.supplier.self.hllx.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class HllxBookSaleInfo implements Serializable {

    private static final long serialVersionUID = -1227819729148323783L;
    //价格与库存时间
    private Date date;
    //价格（财务结算单价，两位小数，30.00（单位：元））
    private BigDecimal price;
    //价格类型（1：底价模式）
    private int priceType;
    //总库存
    private int totalStock;
    //库存明细列表
    private List<HllxStockItem> stockList;

    private BigDecimal salePrice;
}