package com.huoli.trip.supplier.api;

import com.huoli.trip.supplier.self.yaochufa.vo.*;
import com.huoli.trip.supplier.self.yaochufa.vo.basevo.YcfCommonResult;

/**
 * 要出发订单dubbo服务接口定义
 */
public interface YcfOrderService {
    /**
     * 主动获取要出发订单相关信息
     * @param orderId
     * @return
     */
    YcfCommonResult<YcfOrderStatusResult> getOrder(String orderId);

    /**
     * 重新获取凭证
     * @param orderId
     * @return
     */
    YcfCommonResult<YcfVochersResult> getVochers(String orderId);
    /**
     * 可预订检查
     * @param= bookCheckReq
     * @return= BookCheckRes
     * @author= wangdm
     *
     */
    YcfCommonResult<YcfBookCheckRes> getCheckInfos(YcfBookCheckReq bookCheckReq);
    /**
     * 支付订单
     * @param= payOrderReq
     * @return= YcfPayOrderRes
     * @author= wangdm
     *
     */
    YcfCommonResult<YcfPayOrderRes> payOrder(YcfPayOrderReq payOrderReq);
    /**
     * 创建订单
     * @param= createOrderReq
     * @return= YcfCreateOrderRes
     * @author= wangdm
     *
     */
    YcfCommonResult<YcfCreateOrderRes> createOrder(YcfCreateOrderReq createOrderReq);
    /**
     * 取消订单
     * @param= cancelOrderReq
     * @return= YcfCancelOrderRes
     * @author= wangdm
     *
     */
    YcfCommonResult<YcfCancelOrderRes> cancelOrder(YcfCancelOrderReq cancelOrderReq);
}
