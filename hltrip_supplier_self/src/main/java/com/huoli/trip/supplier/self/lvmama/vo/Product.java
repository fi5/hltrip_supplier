package com.huoli.trip.supplier.self.lvmama.vo;

import java.io.Serializable;

/**
 * @author lunatic
 * @Title:
 * @Package
 * @Description:
 * @date 2021/3/1515:13
 */
public class Product implements Serializable {
    /**
     * yes
     * 产品 ID 驴妈妈销售产品 ID
     */
    private Long productId;
    /**
     * yes
     * 商品 ID 驴妈妈销售商品 ID
     */
    private Long goodsId;

    /**
     * yes
     * 订购份数
     */
    private int quantity;
    /**
     * yes
     * 游玩日期 游客游玩日期（yyyy-MM
     * dd）
     */
    private String visitDate;
    /**
     * yes
     * 驴妈妈售价
     */
    private float sellPrice;


}
