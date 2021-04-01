package com.huoli.trip.supplier.web.dao;

import com.huoli.trip.common.entity.mpo.groupTour.GroupTourProductMPO;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/3/31<br>
 */
public interface GroupTourProductDao {

    /**
     * 根据供应商产品id获取产品
     * @param supplierProductId
     * @param channel
     * @return
     */
    GroupTourProductMPO getTourProduct(String supplierProductId, String channel);

    /**
     * 根据供应商产品id修改状态
     * @param supplierProductId
     * @param channel
     */
    void updateStatus(String supplierProductId, String channel);
}
