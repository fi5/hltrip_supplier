package com.huoli.trip.supplier.web.difengyun.service;

import com.huoli.trip.common.entity.ProductItemPO;
import com.huoli.trip.common.entity.ProductPO;
import com.huoli.trip.supplier.self.difengyun.vo.request.DfyProductNoticeRequest;
import com.huoli.trip.supplier.self.difengyun.vo.request.DfyScenicListRequest;
import com.huoli.trip.supplier.self.difengyun.vo.request.DfyToursListRequest;

import java.util.List;

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

    /**
     * 同步门票
     * @param productId
     * @param productItemPO
     * @param syncMode 0: 不限; 1: 只同步本地不存在的产品（创建新产品）; 2: 只同步本地已有的产品（更新产品）
     */
    void syncProduct(String productId, ProductItemPO productItemPO, int syncMode);

    /**
     * 接收产品通知更新
     * @param request
     */
    void productUpdate(DfyProductNoticeRequest request);

    /**
     * 获取所欲渠道产品码
     * @return
     */
    List<ProductPO> getSupplierProductIds();

    Object getToursList(DfyToursListRequest request);

    Object getToursDetail(String productId);

    Object getToursMultiDetail(String productId);
}
