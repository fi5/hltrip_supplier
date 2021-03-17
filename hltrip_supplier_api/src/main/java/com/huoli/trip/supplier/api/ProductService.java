package com.huoli.trip.supplier.api;

import java.util.List;

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

    /**
     * 更新供应商状态
     * @param code 供应商编码
     * @param supplierStatus
     */
    void updateSupplierStatusByCode(String code, Integer supplierStatus);

    /**
     * 更新供应商信息
     * @param code 供应商编码
     * @param supplierStatus
     * @param appFroms
     */
    void updateSupplierStatusAndAppFromByCode(String code, Integer supplierStatus, List<String> appFroms);

    /**
     * 更新appfrom
     * @param code 供应商编码
     * @param appFroms
     */
    void updateAppFromByCode(String code, List<String> appFroms);
}
