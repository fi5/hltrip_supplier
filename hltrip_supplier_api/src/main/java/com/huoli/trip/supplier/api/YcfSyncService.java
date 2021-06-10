package com.huoli.trip.supplier.api;

import com.huoli.trip.common.entity.ProductItemPO;
import com.huoli.trip.supplier.self.yaochufa.vo.*;
import com.huoli.trip.supplier.self.yaochufa.vo.basevo.YcfBaseResult;

import java.util.List;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2020/6/28<br>
 */
public interface YcfSyncService {

    /**
     * 同步产品
     * @param ycfProduct
     */
    void syncProduct(List<YcfProduct> ycfProduct);

    /**
     * 同步产品项目
     * @param productItemIds
     * @return
     */
    List<ProductItemPO> syncProductItem(List<String> productItemIds);

    /**
     * 同步价格日历
     * @param ycfPrice
     */
    void syncPrice(YcfPrice ycfPrice);

    /**
     * 同步价格日历
     * @param ycfPrice
     * @param start
     * @param end
     */
    void syncPrice(YcfPrice ycfPrice, String start, String end);


    /**
     * 主动获取价格日历
     * @param request
     * @return
     */
    YcfBaseResult<YcfGetPriceResponse> getPrice(YcfGetPriceRequest request);

    /**
     * 更新产品全量价格
     * @param ycfPrice
     */
    void syncFullPrice(YcfPrice ycfPrice);

    /**
     * 同步产品，新版
     * @param ycfProducts
     */
    void syncScenicProduct(List<YcfProduct> ycfProducts);

    /**
     * 同步景点，新版
     * @param scenicIds
     */
    List<String> syncScenic(List<String> scenicIds);

    /**
     * 同步酒店，新版
     * @param hotelIds
     * @return
     */
    List<String> syncHotel(List<String> hotelIds);

    /**
     * 同步价格，新版
     * @param request
     * @return
     */
    void syncPriceV2(YcfGetPriceRequest request);

    /**
     * 被动同步价格，新版
     * @param ycfPrice
     */
    void syncPriceV2(YcfPrice ycfPrice);
}
