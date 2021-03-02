package com.huoli.trip.supplier.web.service;

import com.huoli.trip.common.entity.BackChannelEntry;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/3/2<br>
 */
public interface CommonService {

    /**
     * 获取渠道
     * @param supplierId
     * @return
     */
    BackChannelEntry getSupplierById(String supplierId);
}
