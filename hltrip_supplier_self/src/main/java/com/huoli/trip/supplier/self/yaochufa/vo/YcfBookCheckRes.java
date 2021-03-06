package com.huoli.trip.supplier.self.yaochufa.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 描述: <br> 可以预订检查返回
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：王德铭<br>
 * 版本：1.0<br>
 * 创建日期：2020/6/18<br>
 */
@Data
public class YcfBookCheckRes implements Serializable {
    //产品编号
    private String productId;
    //价格库存列表
    private List<YcfBookSaleInfo> saleInfos;
}
