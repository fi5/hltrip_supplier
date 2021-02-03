package com.huoli.trip.supplier.self.difengyun.vo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

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
public class DfyCustomerCondition {

    /**
     * 父条件id
     */
    private Integer conditionId;

    /**
     * 父条件name
     */
    private String conditionName;

    /**
     * 子条件
     */
    private List<DfySubCondition> subConditions;

    @Getter
    @Setter
    public static class DfySubCondition {
        /**
         * 子条件name
         */
        private String conditionName;
    }
}
