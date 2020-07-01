package com.huoli.trip.supplier.api;

import com.huoli.trip.supplier.self.yaochufa.vo.*;
import com.huoli.trip.supplier.self.yaochufa.vo.basevo.YcfBaseResult;

/**
 * 要出发订单dubbo服务接口定义
 */
public interface YcfOrderService {
    /**
     * 主动获取要出发订单相关信息
     * @param orderId
     * @return
     */
    YcfBaseResult<YcfOrderStatusResult> getOrder(String orderId);

    /**
     * 重新获取凭证
     * @param orderId
     * @return
     */
    YcfBaseResult<YcfVouchersResult> getVochers(String orderId);
    /**
     * 可预订检查
     * @param= bookCheckReq
     * @return= BookCheckRes
     * @author= wangdm
     *
     */
    YcfBaseResult<YcfBookCheckRes> getCheckInfos(YcfBookCheckReq bookCheckReq);
    /**
     * 支付订单
     * @param= payOrderReq
     * @return= YcfPayOrderRes
     * @author= wangdm
     *
     */
    YcfBaseResult<YcfPayOrderRes> payOrder(YcfPayOrderReq payOrderReq);
    /**
     * 创建订单
     * @param= createOrderReq
     * @return= YcfCreateOrderRes
     * @author= wangdm
     *
     */
    YcfBaseResult<YcfCreateOrderRes> createOrder(YcfCreateOrderReq createOrderReq);
    /**
     * 取消订单
     * @param= cancelOrderReq
     * @return= YcfCancelOrderRes
     * @author= wangdm
     *
     */
    YcfBaseResult<YcfCancelOrderRes> cancelOrder(YcfCancelOrderReq cancelOrderReq);


}
