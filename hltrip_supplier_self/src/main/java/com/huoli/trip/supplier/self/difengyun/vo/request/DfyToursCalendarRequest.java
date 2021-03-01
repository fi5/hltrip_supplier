package com.huoli.trip.supplier.self.difengyun.vo.request;

import lombok.Data;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/1/28<br>
 */
@Data
public class DfyToursCalendarRequest {

    /**
     * 产品id
     */
    private Integer productId;

    /**
     * 出发城市编码（注：同一产品、不同出发城市的价格、库存不一样）
     */
    private Integer departCityCode;

    /**
     * 团期(yyyy-MM-dd)
     */
    private String departDate;
}
