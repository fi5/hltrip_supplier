package com.huoli.trip.supplier.web.lvmama.service.impl;

import com.huoli.trip.supplier.api.LvmamaOrderService;
import com.huoli.trip.supplier.feign.client.lvmama.client.ILvmamaClient;
import com.huoli.trip.supplier.feign.client.yaochufa.client.IYaoChuFaClient;
import com.huoli.trip.supplier.self.lvmama.vo.request.*;
import com.huoli.trip.supplier.self.lvmama.vo.response.BaseResponse;
import com.huoli.trip.supplier.self.lvmama.vo.response.OrderResponse;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author lunatic
 * @Title:
 * @Package
 * @Description:
 * @date 2021/3/1517:48
 */
public class LvmamaOrderServiceImpl implements LvmamaOrderService {
    @Autowired
    private ILvmamaClient iLvmamaClient;
    @Override
    public BaseResponse getCheckInfos(ValidateOrderRequest request) {
        return iLvmamaClient.getCheckInfos(request);
    }

    @Override
    public OrderResponse payOrder(OrderPaymentRequest request) {
        return iLvmamaClient.payOrder(request);
    }

    @Override
    public OrderResponse createOrder(CreateOrderRequest request) {
        return iLvmamaClient.createOrder(request);
    }

    @Override
    public OrderResponse cancelOrder(OrderUnpaidCancelRequest request) {
        return iLvmamaClient.cancelOrder(request);
    }

    @Override
    public BaseResponse rufundTicket(OrderCancelRequest request) {
        return iLvmamaClient.rufundTicket(request);
    }
}
