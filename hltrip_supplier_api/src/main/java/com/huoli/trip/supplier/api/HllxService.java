package com.huoli.trip.supplier.api;

import com.huoli.trip.supplier.self.hllx.vo.*;

public interface HllxService {
    /**
     * 可预订检查
     */
    HllxBaseResult<HllxBookCheckRes> getCheckInfos(HllxBookCheckReq req);

    /**
     * 创建订单
     * @param req
     * @return
     */
    HllxBaseResult<HllxCreateOrderRes> createOrder(HllxCreateOrderReq req);

    /**
     * 支付订单
     */
    HllxBaseResult<HllxPayOrderRes> payOrder(HllxPayOrderReq req);

    /**
     * 取消订单
     * @param req
     * @return
     */
    HllxBaseResult<HllxCancelOrderRes> cancelOrder(HllxCancelOrderReq req);

    /**
     * 查询订单
     * @param orderId
     * @return
     */
    HllxBaseResult<HllxOrderStatusResult> getOrder(String orderId);

    /**
     * 申请退款
     */
    HllxBaseResult<HllxOrderStatusResult> drawback(String orderId);

}
