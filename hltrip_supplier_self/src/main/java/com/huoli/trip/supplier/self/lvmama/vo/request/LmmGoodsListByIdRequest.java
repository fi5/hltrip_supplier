package com.huoli.trip.supplier.self.lvmama.vo.request;

import lombok.Data;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/3/18<br>
 */
@Data
public class LmmGoodsListByIdRequest extends LmmListBaseRequest {

    /**
     * 商品 ID(如果有多个以英文逗号分割)
     */
    private String goodsIds;
}
