package com.huoli.trip.supplier.self.lvmama.vo.push;

import lombok.Data;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
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
@XmlRootElement(name = "request")
public class LmmProductPushRequest implements Serializable {

    private PushBody body;

    @XmlElement(name = "body")
    public PushBody getBody() {
        return body;
    }

    public void setBody(PushBody body) {
        this.body = body;
    }

    public static class PushBody{
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
        private LmmPushProduct product;

        @XmlElement(name = "changeType")
        public String getChangeType() {
            return changeType;
        }

        public void setChangeType(String changeType) {
            this.changeType = changeType;
        }

        @XmlElement(name = "product")
        public LmmPushProduct getProduct() {
            return product;
        }

        public void setProduct(LmmPushProduct product) {
            this.product = product;
        }
    }


    public static class LmmPushProduct{
        /**
         * 变更产品 ID
         */
        private Long productId;

        /**
         * 变更商品 ID,变更的商品 ID 列表，多个商品 ID 以英文逗号隔开
         */
        private Long goodsId;

        @XmlElement(name = "productId")
        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        @XmlElement(name = "goodsId")
        public Long getGoodsId() {
            return goodsId;
        }

        public void setGoodsId(Long goodsId) {
            this.goodsId = goodsId;
        }
    }
}
