package com.huoli.trip.supplier.self.yaochufa.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2020/6/18<br>
 */
@Data
public class YcfPriceInfo implements Serializable {

    /**
     * 价格日期
     */
    @JsonFormat(timezone = "GMT+8")
    private Date date;

    /**
     * 售卖价格
     */
    private BigDecimal price;

    /**
     * 结算价格
     */
    private BigDecimal settlementPrice;

    /**
     * 价格类型
     */
    private Integer priceType;

    /**
     * 库存
     */
    private Integer stock;

}
