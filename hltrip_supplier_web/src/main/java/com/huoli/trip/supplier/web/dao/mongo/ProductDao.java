package com.huoli.trip.supplier.web.dao.mongo;

import com.huoli.trip.common.entity.ProductPO;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2020/6/28<br>
 */
public interface ProductDao {

    /**
     * 同步产品
     * @param productPO
     */
    void updateBySupplierProductId(ProductPO productPO);
}
