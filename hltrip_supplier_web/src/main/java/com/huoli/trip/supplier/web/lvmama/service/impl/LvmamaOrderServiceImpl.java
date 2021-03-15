package com.huoli.trip.supplier.web.lvmama.service.impl;

import com.huoli.trip.supplier.api.LvmamaOrderService;
import com.huoli.trip.supplier.self.lvmama.vo.request.*;
import com.huoli.trip.supplier.self.lvmama.vo.response.BaseResponse;
import com.huoli.trip.supplier.self.lvmama.vo.response.OrderResponse;

/**
 * @author lunatic
 * @Title:
 * @Package
 * @Description:
 * @date 2021/3/1517:48
 */
public class LvmamaOrderServiceImpl implements LvmamaOrderService {
    @Override
    public BaseResponse getCheckInfos(ValidateOrderRequest request) {
        return null;
    }

    @Override
    public OrderResponse payOrder(OrderPaymentRequest request) {
        return null;
    }

    @Override
    public OrderResponse createOrder(CreateOrderRequest request) {
        return null;
    }

    @Override
    public OrderResponse cancelOrder(OrderUnpaidCancelRequest request) {
        return null;
    }

    @Override
    public BaseResponse rufundTicket(OrderCancelRequest request) {
        return null;
    }
}
