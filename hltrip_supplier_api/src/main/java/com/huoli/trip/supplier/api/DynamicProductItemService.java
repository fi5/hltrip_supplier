package com.huoli.trip.supplier.api;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2020/8/27<br>
 */
public interface DynamicProductItemService {

    /**
     * 刷新item低价产品
     * @param code
     */
    void refreshItemByCode(String code);

    /**
     * 刷新item低价产品
     * @param productCode
     */
    void refreshItemByProductCode(String productCode);

}
