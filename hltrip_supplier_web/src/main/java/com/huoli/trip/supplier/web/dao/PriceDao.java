package com.huoli.trip.supplier.web.dao;

import com.huoli.trip.common.entity.PricePO;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2020/6/28<br>
 */
public interface PriceDao {

    /**
     * 同步价格
     * @param pricePO
     */
    void updateByProductCode(PricePO pricePO);

    /**
     * 根据产品码获取价格
     * @param productCode
     * @return
     */
    PricePO getByProductCode(String productCode);

    /**
     * 更新价格库存
     * @param pricePO
     */
    void updateStocksByProductCode(PricePO pricePO);
}
