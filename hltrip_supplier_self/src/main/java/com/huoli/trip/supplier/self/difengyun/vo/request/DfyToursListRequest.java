package com.huoli.trip.supplier.self.difengyun.vo.request;

import lombok.Data;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/1/20<br>
 */
@Data
public class DfyToursListRequest {

    /**
     * 产品id
     */
    private String productId;

    /**
     * 二级品类 值从【查询二级品类接口】获取
     */
    private Integer classBrandId;

    /**
     * 目的地大类 值从【查询目的地大类接口】获取
     */
    private Integer productNewLineTypeId;

    /**
     * 目的地分组（二级分组）  值从【查询目的地分组接口】获取
     */
    private Integer destGroupId;

    /**
     * 出发城市id
     */
    private Integer departCityCode;

    /**
     * 出发城市名称
     */
    private String departCityName;

    /**
     * 分页查询起始条目
     */
    private Integer start;

    /**
     * 分页查询一页显示条目
     * 例如：
     * 第一页 start:0   limit:10
     * 第二页 start:10 limit:10
     * 第三页 start:20 limit:10
     * 以此类推
     */
    private Integer limit;

}
