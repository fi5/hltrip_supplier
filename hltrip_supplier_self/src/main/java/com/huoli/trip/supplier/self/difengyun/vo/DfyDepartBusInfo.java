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
public class DfyDepartBusInfo {
    /**
     *
     */
    private Integer id;

    /**
     * 发车时间
     */
    private String departTime;

    /**
     * 出发地点
     */
    private String departPlace;

    /**
     * 返回地点
     */
    private String backPlace;

    /**
     * 备注
     */
    private String departRemark;
}
