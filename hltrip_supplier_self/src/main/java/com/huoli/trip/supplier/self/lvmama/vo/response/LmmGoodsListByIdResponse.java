package com.huoli.trip.supplier.self.lvmama.vo.response;

import com.huoli.trip.supplier.self.lvmama.vo.LmmGoods;
import com.huoli.trip.supplier.self.lvmama.vo.LmmProduct;
import lombok.Data;

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
public class LmmGoodsListByIdResponse extends LmmListBaseResponse{


    /**
     * 产品
     */
    private List<LmmGoods> goodList;

    /**
     * 不分销的id
     */
    private String notDistGoodsIds;
}
