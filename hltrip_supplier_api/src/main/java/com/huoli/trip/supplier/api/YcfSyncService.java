package com.huoli.trip.supplier.api;

import com.huoli.trip.supplier.self.yaochufa.vo.YcfGetPriceRequest;
import com.huoli.trip.supplier.self.yaochufa.vo.YcfPrice;
import com.huoli.trip.supplier.self.yaochufa.vo.YcfProduct;

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
     */
    void syncProductItem(List<String> productItemIds);

    /**
     * 同步价格日历
     * @param ycfPrice
     */
    void syncPrice(YcfPrice ycfPrice);

    void getPrice(YcfGetPriceRequest request);
}
