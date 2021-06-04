package com.huoli.trip.supplier.self.lvmama.vo;

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
 * 创建日期：2021/3/19<br>
 */
@Data
public class LmmPriceProduct {

    /**
     * 产品 id
     */
    private String productId;

    /**
     * 产品是否有效
     */
    private String productStatus;

    private List<LmmPriceGoods> goodsList;

    @Getter
    @Setter
    public static class LmmPriceGoods{

        /**
         * 商品 Id
         */
        private String goodsId;

        /**
         * 商品上下线状态
         */
        private String goodsOnLine;

        /**
         * 商品名称
         */
        private String goodsName;

        /**
         * 商品点评返现金额
         */
        private String commentsCashback;

        /**
         * 价格
         */
        private List<LmmPrice> prices;
    }
}
