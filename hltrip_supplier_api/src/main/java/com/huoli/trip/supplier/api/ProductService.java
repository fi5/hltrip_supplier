package com.huoli.trip.supplier.api;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/1/8<br>
 */
public interface ProductService {

    /**
     * 更新产品状态
     * @param code
     * @param status
     */
    void updateStatusByCode(String code, int status);
}
