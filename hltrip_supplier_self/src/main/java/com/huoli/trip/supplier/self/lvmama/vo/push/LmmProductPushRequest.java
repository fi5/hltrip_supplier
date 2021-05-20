package com.huoli.trip.supplier.self.lvmama.vo.push;

import lombok.Data;

import java.io.Serializable;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/5/20<br>
 */
@Data
public class LmmProductPushRequest implements Serializable {

    /**
     * 变更类型
     * 1.针对产品相关变更 product_create:新增产品 product_info_change:产品信 息变更 product_online:产品上线 product_offline:产品下线
     * 2.针对商品相关变更 goods_create:新增商品 goods_info_change:商品信息 变更
     * goods_online:商品上线 goods_offline:商品下线 price_change:时间价格表变更
     */
    private String changeType;

    /**
     * 变更信息, 变更信息内容
     */
    private String product;

    /**
     * 变更产品 ID
     */
    private Long productId;

    /**
     * 变更商品 ID,变更的商品 ID 列表，多个商品 ID 以英文逗号隔开
     */
    private Long goodsId;
}
