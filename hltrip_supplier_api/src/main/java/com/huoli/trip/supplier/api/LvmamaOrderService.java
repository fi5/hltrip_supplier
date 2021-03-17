package com.huoli.trip.supplier.api;

import com.huoli.trip.common.vo.response.BaseResponse;
import com.huoli.trip.supplier.self.lvmama.vo.LvOrderDetail;
import com.huoli.trip.supplier.self.lvmama.vo.request.*;
import com.huoli.trip.supplier.self.lvmama.vo.response.LmmBaseResponse;
import com.huoli.trip.supplier.self.lvmama.vo.response.OrderResponse;
import com.huoli.trip.supplier.self.yaochufa.vo.BaseOrderRequest;

/**
 * @author lunatic
 * @Title:
 * @Package
 * @Description:
 * @date 2021/3/1514:51
 */
public interface LvmamaOrderService {

	/**
     * 订单详情
     * @param request
     * @return
     */
    BaseResponse<LvOrderDetail> orderDetail(BaseOrderRequest request);
    /**
     * 可预订检查
     */
    LmmBaseResponse getCheckInfos(ValidateOrderRequest request);
    /**
     * 支付订单
     */
    OrderResponse payOrder(OrderPaymentRequest request);
    /**
     * 创建订单
     */
    OrderResponse createOrder(CreateOrderRequest request);
    /**
     * 取消订单
     */
    OrderResponse cancelOrder(OrderUnpaidCancelRequest request);

    /**
     * 退票申请
     * @param request
     * @return
     */
    LmmBaseResponse rufundTicket(OrderCancelRequest request);
}
