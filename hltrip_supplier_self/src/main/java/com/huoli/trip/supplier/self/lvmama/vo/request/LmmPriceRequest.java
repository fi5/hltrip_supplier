package com.huoli.trip.supplier.self.lvmama.vo.request;

import lombok.Data;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/3/19<br>
 */
@Data
public class LmmPriceRequest extends LmmBaseRequest{

    /**
     * 商品 ID(多个以英文逗号分割)
     */
    private String goodsIds;

    /**
     * 产品价格:开始间 yyyy-MM-dd
     */
    private String beginDate;

    /**
     * 产品价格:结束时间 yyyy-MM-dd
     */
    private String endDate;
}
