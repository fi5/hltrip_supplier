package com.huoli.trip.supplier.web.difengyun.service;

import com.huoli.trip.common.entity.ProductItemPO;
import com.huoli.trip.common.entity.ProductPO;
import com.huoli.trip.common.entity.mpo.groupTour.GroupTourPrice;
import com.huoli.trip.supplier.self.difengyun.vo.DfyProductInfo;
import com.huoli.trip.supplier.self.difengyun.vo.request.*;
import com.huoli.trip.supplier.self.difengyun.vo.response.DfyBaseResult;
import com.huoli.trip.supplier.self.difengyun.vo.response.DfyToursCalendarResponse;
import com.huoli.trip.supplier.self.difengyun.vo.response.DfyToursDetailResponse;
import com.huoli.trip.supplier.self.difengyun.vo.response.DfyToursListResponse;

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
    List<ProductPO> getSupplierProductIds(Integer productType);

    /**
     * 获取跟团游列表
     * @param request
     * @return
     */
    DfyBaseResult<DfyToursListResponse> getToursList(DfyToursListRequest request);

    /**
     * 获取跟团游详情
     * @param productId
     * @return
     */
    DfyBaseResult<DfyToursDetailResponse> getToursDetail(String productId);

    /**
     * 获取跟团游多程详情
     * @param productId
     * @return
     */
    DfyBaseResult<DfyToursDetailResponse> getToursMultiDetail(String productId);

    /**
     * 获取价格日历
     * @param request
     * @return
     */
    DfyBaseResult<List<DfyToursCalendarResponse>> getToursCalendar(DfyToursCalendarRequest request);

    /**
     * 同步跟团游列表，默认同步所有
     * @param request
     * @return
     */
    boolean syncToursList(DfyToursListRequest request);

    /**
     * 同步跟团游列表
     * @param request
     * @param syncMode
     * @return
     */
    boolean syncToursList(DfyToursListRequest request, int syncMode);

    /**
     * 同步跟团游产品
     * @param productId
     * @param syncMode
     */
    void syncToursDetail(String productId, int syncMode);

    /**
     * 同步跟团游价格
     * @param supplierProductId
     * @param city
     */
    void syncToursPrice(String supplierProductId, String city);

    /**
     * 同步景点，新版
     * @param request
     * @return
     */
    boolean syncScenicListV2(DfyScenicListRequest request);

    /**
     * 同步景点，新版
     * @param scenicId
     */
    void syncScenicDetailV2(String scenicId);

    /**
     * 同步产品，新版
     * @param productId
     */
    void syncProductV2(String productId);

    /**
     * 获取供应商产品id
     * @return
     */
    List<String> getSupplierProductIdsV2();

    /**
     * 同步跟团游列表，新版
     * @param request
     * @return
     */
    boolean syncToursListV2(DfyToursListRequest request);

    /**
     * 同步跟团游，新版
     * @param productId
     */
    void syncToursDetailV2(String productId);

    /**
     * 同步跟团游价格，新版
     * @param supplierProductId
     * @param city
     */
    List<GroupTourPrice> syncToursPriceV2(String supplierProductId, String city);


}


