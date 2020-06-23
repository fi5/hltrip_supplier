package com.huoli.trip.supplier.self.yaochufa.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 描述: <br>价格库存业务实体
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：王德铭<br>
 * 版本：1.0<br>
 * 创建日期：2020/6/18<br>
 */
@Data
public class YcfBookSaleInfo {

    //价格与库存时间
    private Date date;
    //价格（财务结算单价，两位小数，30.00（单位：元））
    private BigDecimal price;
    //价格类型（1：底价模式）
    private int priceType;
    //总库存
    private int totalStock;
    //库存明细列表
    private List<YcfStockItem> stockList;
}
