package com.huoli.trip.supplier.web.dao;

import com.huoli.trip.common.entity.PriceInfoPO;
import com.huoli.trip.common.entity.ProductItemPO;
import com.huoli.trip.common.entity.ProductPO;

import java.util.List;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2020/6/28<br>
 */
public interface ProductItemDao {

    /**
     * 同步产品项目
     * @param productItemPO
     */
    void updateByCode(ProductItemPO productItemPO);

    /**
     * 根据城市和类型查项目
     * @param city
     * @param type
     * @param pageSize
     * @return
     */
    List<ProductItemPO> selectByCityAndType(String city, Integer type, int pageSize);

    /**
     * 根据code查询
     * @param code
     * @return
     */
    ProductItemPO selectByCode(String code);

    /**
     * 更新产品和价格
     * @param code
     * @param productPO
     */
    void updateProductAndPriceByCode(String code, ProductPO productPO);
}
