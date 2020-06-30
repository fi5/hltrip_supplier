package com.huoli.trip.supplier.web.yaochufa.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.huoli.trip.supplier.api.YcfOrderService;
import com.huoli.trip.supplier.feign.client.yaochufa.client.IYaoChuFaClient;
import com.huoli.trip.supplier.self.yaochufa.vo.*;
import com.huoli.trip.supplier.self.yaochufa.vo.basevo.YcfBaseRequest;
import com.huoli.trip.supplier.self.yaochufa.vo.basevo.YcfCommonResult;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 要出发订单dubbo服务接口实现
 */

@Service(timeout = 10000,group = "hllx")
public class YcfOrderServiceImpl implements YcfOrderService {
    @Autowired
    private IYaoChuFaClient iYaoChuFaClient;

    @Override
    public YcfCommonResult<YcfOrderStatusResult> getOrder(String orderId) {
            YcfBaseRequest request = new YcfBaseRequest();
            YcfOrderBaSeRequest orderBaSeRequest = new YcfOrderBaSeRequest();
            orderBaSeRequest.setPartnerOrderId(orderId);
            request.setData(orderBaSeRequest);
            return iYaoChuFaClient.getOederStatus(request);
    }

    @Override
    public YcfCommonResult<YcfVochersResult> getVochers(String orderId) {
        YcfBaseRequest request = new YcfBaseRequest();
        YcfOrderBaSeRequest orderBaSeRequest = new YcfOrderBaSeRequest();
        orderBaSeRequest.setPartnerOrderId(orderId);
        request.setData(orderBaSeRequest);
        return iYaoChuFaClient.getVochers(request);
    }

    @Override
    public YcfCommonResult<YcfBookCheckRes> getCheckInfos(YcfBookCheckReq checkReq) {
        YcfBaseRequest<YcfBookCheckReq> req = new YcfBaseRequest<>();
        req.setData(checkReq);
        return iYaoChuFaClient.getCheckInfos(req);
    }

    @Override
    public YcfCommonResult<YcfPayOrderRes> payOrder(YcfPayOrderReq payOrderReq) {
        YcfBaseRequest<YcfPayOrderReq> req = new YcfBaseRequest<>();
        req.setData(payOrderReq);
        return iYaoChuFaClient.payOrder(req);
    }

    @Override
    public YcfCommonResult<YcfCreateOrderRes> createOrder(YcfCreateOrderReq createOrderReq) {
        YcfBaseRequest<YcfCreateOrderReq> req = new YcfBaseRequest<>();
        req.setData(createOrderReq);
        return iYaoChuFaClient.createOrder(req);
    }

    @Override
    public YcfCommonResult<YcfCancelOrderRes> cancelOrder(YcfCancelOrderReq cancelOrderReq) {
        YcfBaseRequest<YcfCancelOrderReq> req = new YcfBaseRequest<>();
        req.setData(cancelOrderReq);
        return iYaoChuFaClient.cancelOrder(req);
    }

}

