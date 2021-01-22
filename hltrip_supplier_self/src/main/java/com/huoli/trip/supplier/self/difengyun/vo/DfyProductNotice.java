package com.huoli.trip.supplier.self.difengyun.vo;

import lombok.Data;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2020/12/11<br>
 */
@Data
public class DfyProductNotice {

    /**
     * 产品id
     */
    private Integer productId;

    /**
     * 通知类型：1更新 2下线 3上线
     */
    private Integer noticeType;

    /**
     * 一级品类：1跟团2自助
     */
    private Integer classBrandParentId;
}
