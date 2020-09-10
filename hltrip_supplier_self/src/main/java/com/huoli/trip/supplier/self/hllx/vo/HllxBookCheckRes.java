package com.huoli.trip.supplier.self.hllx.vo;


import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class HllxBookCheckRes implements Serializable {

    private static final long serialVersionUID = -1386904520469948063L;
    //产品编号
    private String productId;
    //价格库存列表
    private List<HllxBookSaleInfo> saleInfos;
}
