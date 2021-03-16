package com.huoli.trip.supplier.api;

import com.huoli.trip.supplier.self.difengyun.vo.request.*;
import com.huoli.trip.supplier.self.lvmama.vo.OrderPaymentInfo;
import com.huoli.trip.supplier.self.lvmama.vo.request.*;
import com.huoli.trip.supplier.self.lvmama.vo.response.BaseResponse;
import com.huoli.trip.supplier.self.lvmama.vo.response.OrderResponse;

/**
 * @author lunatic
 * @Title:
 * @Package
 * @Description:
 * @date 2021/3/1514:51
 */
public interface LvmamaOrderService {
    /**
     * 可预订检查
     */
    BaseResponse getCheckInfos(ValidateOrderRequest request);
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
    BaseResponse rufundTicket(OrderCancelRequest request);
}
