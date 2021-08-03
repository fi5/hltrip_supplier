package com.huoli.trip.supplier.self.universal.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/8/3<br>
 */
@Data
public class UBRGuest implements Serializable {

    /**
     * 出行人名称
     */
    private String name;

    /**
     * 证件类型
     */
    @JsonProperty(value = "id_type")
    private String idType;

    /**
     * 证件号
     */
    @JsonProperty(value = "id_no")
    private String idNo;

    /**
     * 电话
     */
    private String telephone;
}
