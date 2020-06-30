package com.huoli.trip.supplier.self.yaochufa.vo;

import lombok.Data;

import java.util.List;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2020/6/29<br>
 */
@Data
public class YcfGetPriceResponse {

    /**
     * 产品id
     */
    private String productID;

    /**
     * 合作商id
     */
    private String partnerProductID;

    /**
     * 价格列表
     */
    private List<YcfPriceInfo> saleInfos;
}
