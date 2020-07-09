package com.huoli.trip.supplier.self.yaochufa.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 描述: <br>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：王德铭<br>
 * 版本：1.0<br>
 * 创建日期：2020/4/26<br>
 */
@Data
public class YcfBookFood implements Serializable {
    //餐饮编号
    private String foodId;
    //开始使用日期
    private String checkInDate;
}
