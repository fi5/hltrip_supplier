package com.huoli.trip.supplier.self.lvmama.vo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/3/18<br>
 */
@Data
public class LmmProduct {

    /**
     * 主景区ID
     */
    private String placeId;

    private String productId;

    /**
     * 多景区套票时候含有多个景区名称
     */
    private HashMap<String, String> placeName;

    private  String productName;

    /**
     * 景点门票: category_single_ticket
     * 其他票: category_other_ticket
     * 组合套餐票: category_comb_ticket
     */
    private String productType;

    /**
     * 上线状态
     */
    private String productStatus;

    /**
     * 景点描述
     */
    private List<String> characteristic;

    /**
     * 产品的预定须知
     */
    private BookInfo bookingInfo;

    /**
     * 产品简介
     */
    private String introdution;

    /**
     * 公告
     */
    private List<Post> postList;

    /**
     * 游玩景点
     */
    private List<PlayAttraction> playAttractions;

    /**
     * 产品主题列表
     */
    private List<String> productTheme;

    /**
     * 旅游保障服务
     * 取值范围如下
     * 保证入园: ENSURING_THE_GARDEN
     * 快速入园: FAST_INTO_THE_GARDEN
     * 随时退: BACK_AT_ANY_TIME
     * 贵就赔: YOU_WILL_LOSE
     */
    private String serviceGuarantee;

    /**
     * 图片
     * 最多五个
     */
    private List<String> images;

    /**
     * 商品信息
     * 数据库中需要设置例外
     */
    private  List<LmmGoods> goodsList;

    @Setter
    @Getter
    public class Post{
        /**
         * 公告内容
         */
        private String postInfo;

        /**
         * 公告开始
         */
        private String postBegin;

        /**
         * 公告结束
         */
        private String  postEnd;
    }

    @Getter
    @Setter
    public class BookInfo{

        /**
         * 免票政策
         */
        private String freePolicy;

        /**
         * 优惠人群
         */
        private String offerCrowd;

        /**
         * 老人:
         */
        private String oldPeople;

        /**
         * 年龄:
         */
        private String age;

        /**
         * 说明
         */
        private String explanation;
    }

    @Getter
    @Setter
    public class PlayAttraction{
        /**
         * 游玩景点
         */
        private String playName;

        /**
         * 游玩描述
         */
        private String playInfo;

        /**
         * 图片,最多五个
         */
        private List<String> playImages;
    }
}
