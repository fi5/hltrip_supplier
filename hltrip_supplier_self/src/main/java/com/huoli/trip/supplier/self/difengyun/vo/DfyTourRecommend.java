package com.huoli.trip.supplier.self.difengyun.vo;

import lombok.Data;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/1/29<br>
 */
@Data
public class DfyTourRecommend {
    /**
     * 类型推荐 1 吃 2 住 3行 4 游 5 购 6 娱 7 赠 8 概述
     * 9 特别优惠 10 重要提示 11 交通信息 12 详情 13 其他信息
     * 14产品特色图片(description存的是图片地址,多张图时逗号分隔)
     * 15.产品特色详情(description是json)
     */
    private Integer type;

    /**
     * 具体信息描述
     */
    private String description;

    /**
     * 排序
     */
    private Integer sort;

}
