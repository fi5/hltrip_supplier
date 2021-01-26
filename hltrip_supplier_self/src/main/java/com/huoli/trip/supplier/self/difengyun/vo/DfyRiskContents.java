package com.huoli.trip.supplier.self.difengyun.vo;

import lombok.Data;

import java.util.List;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/1/26<br>
 */
@Data
public class DfyRiskContents {

    /**
     * 高危项目标题：如'漂流','浮潜','高原','快艇','潜水','滑雪','高空','骑马','登山'
     */
    private String riskTitle;

    /**
     * 具体信息描述
     */
    private List<String> riskDetails;
}
