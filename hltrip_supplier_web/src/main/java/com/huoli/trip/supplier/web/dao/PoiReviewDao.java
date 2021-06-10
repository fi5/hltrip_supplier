package com.huoli.trip.supplier.web.dao;

import com.huoli.trip.common.entity.mpo.PoiReviewMPO;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/4/16<br>
 */
public interface PoiReviewDao {

    /**
     * 新增申请
     * @param poiReviewMPO
     * @return
     */
    PoiReviewMPO addPoiReview(PoiReviewMPO poiReviewMPO);
}
