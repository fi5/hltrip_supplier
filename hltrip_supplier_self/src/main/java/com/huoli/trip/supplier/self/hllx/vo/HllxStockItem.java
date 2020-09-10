package com.huoli.trip.supplier.self.hllx.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class HllxStockItem implements Serializable {
    private static final long serialVersionUID = 658654606740146821L;
    //元素编号（房型编号，票种编号，餐饮编号）
    private String itemId;
    //库存量（可购买产品的份数）
    private int stock;
}