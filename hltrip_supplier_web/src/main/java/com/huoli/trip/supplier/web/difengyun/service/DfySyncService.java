package com.huoli.trip.supplier.web.difengyun.service;

import com.huoli.trip.common.entity.ProductItemPO;
import com.huoli.trip.supplier.self.difengyun.vo.request.DfyScenicListRequest;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2020/12/9<br>
 */
public interface DfySyncService {

    /**
     * 同步景点、产品
     * @param request
     */
    boolean syncScenicList(DfyScenicListRequest request);

    /**
     * 同步景点详情
     * @param scenicId
     */
    void syncScenicDetail(String scenicId);

    /**
     * 同步门票
     * @param productId
     * @param productItemPO
     */
    void syncProduct(String productId, ProductItemPO productItemPO);
}
