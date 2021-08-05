package com.huoli.trip.supplier.self.universal.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/7/27<br>
 */
@Data
public class UBRBaseProduct implements Serializable {

    /**
     * 门票编号
     */
    private String code;

    /**
     * 门票名称
     */
    private String name;

    /**
     * 门票描述，英文
     */
    private String description;

    private Boolean purchasable;

    private List<UBRCategory> categories;

    /**
     * 价格列表
     */
    private List<UBRPrice> prices;

    /**
     * 库存列表
     */
    private List<UBRStock> stocks;

    /**
     * 摘要
     */
    private String summary;
}
