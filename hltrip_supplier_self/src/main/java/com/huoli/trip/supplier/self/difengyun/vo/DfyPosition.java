package com.huoli.trip.supplier.self.difengyun.vo;

import lombok.Data;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/1/26<br>
 */
@Data
public class DfyPosition {

    /**
     * 目的地洲
     */
    private String desContinentName;

    /**
     * 目的地国家
     */
    private String desCountryName;

    /**
     * 目的地省、一级行政区
     */
    private String desProvinceName;

    /**
     * 目的地市、二级行政区
     */
    private String desCityName;

    /**
     * 目的地区县、三级行政区
     */
    private String desCountyName;

    /**
     * 是否主目的地 0.否 1.是
     */
    private Integer isMain;
}
