package com.huoli.trip.supplier.self.difengyun.vo;

import lombok.Data;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2020/12/8<br>
 */
@Data
public class DfyTicket {

    /**
     * 门票产品ID
     */
    private String productId;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 单位元,景点价 非下单价格
     */
    private String webPrice;

    /**
     * 单位元,销售价,当价格为0时，需要单独查询门票详情，其中有价格日历内容，获取正确的价格，在下单时也是要使用详情中的价格。非下单价格
     */
    private String salePrice;
}
