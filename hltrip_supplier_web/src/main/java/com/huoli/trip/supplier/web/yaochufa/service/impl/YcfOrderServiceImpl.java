package com.huoli.trip.supplier.web.yaochufa.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.huoli.trip.supplier.api.YcfOrderService;
import com.huoli.trip.supplier.feign.client.yaochufa.client.IYaoChuFaClient;
import com.huoli.trip.supplier.self.yaochufa.vo.*;
import com.huoli.trip.supplier.self.yaochufa.vo.basevo.YcfBaseRequest;
import com.huoli.trip.supplier.self.yaochufa.vo.basevo.YcfBaseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * 要出发订单dubbo服务接口实现
 */

@Service(timeout = 10000,group = "hltrip")
public class YcfOrderServiceImpl implements YcfOrderService {
    @Autowired
    private IYaoChuFaClient iYaoChuFaClient;

    @Override
    public YcfBaseResult<YcfOrderStatusResult> getOrder(BaseOrderRequest request) {
            YcfBaseRequest ycfBaseRequest = new YcfBaseRequest();
            YcfOrderBaSeRequest orderBaSeRequest = new YcfOrderBaSeRequest();
            orderBaSeRequest.setPartnerOrderId(request.getOrderId());
            ycfBaseRequest.setData(orderBaSeRequest);
            return iYaoChuFaClient.getOrderStatus(ycfBaseRequest);
    }

    @Override
    public YcfBaseResult<YcfVouchersResult> getVochers(BaseOrderRequest request) {
        YcfBaseRequest ycfBaseRequest = new YcfBaseRequest();
        YcfOrderBaSeRequest orderBaSeRequest = new YcfOrderBaSeRequest();
        orderBaSeRequest.setPartnerOrderId(request.getOrderId());
        ycfBaseRequest.setData(orderBaSeRequest);
        return iYaoChuFaClient.getVouchers(ycfBaseRequest);
    }

    @Override
    public YcfBaseResult<YcfBookCheckRes> getCheckInfos(YcfBookCheckReq checkReq) {
        YcfBaseRequest<YcfBookCheckReq> req = new YcfBaseRequest<>();
        req.setData(checkReq);
        return iYaoChuFaClient.getCheckInfos(req);
    }

    @Override
    public YcfBaseResult<YcfGetPriceResponse> getStockPrice(YcfGetPriceRequest stockPriceReq) {
        YcfBaseRequest<YcfGetPriceRequest> req = new YcfBaseRequest<>();
        req.setData(stockPriceReq);
        return iYaoChuFaClient.getPrice(req);
    }


    @Override
    public YcfBaseResult<YcfPayOrderRes> payOrder(YcfPayOrderReq payOrderReq) {
        YcfBaseRequest<YcfPayOrderReq> req = new YcfBaseRequest<>();
        req.setData(payOrderReq);
        return iYaoChuFaClient.payOrder(req);
    }

    @Override
    public YcfBaseResult<YcfCreateOrderRes> createOrder(YcfCreateOrderReq createOrderReq) {
        YcfBaseRequest<YcfCreateOrderReq> req = new YcfBaseRequest<>();
        req.setData(createOrderReq);
        return iYaoChuFaClient.createOrder(req);
    }

    @Override
    public YcfBaseResult<YcfCancelOrderRes> cancelOrder(YcfCancelOrderReq cancelOrderReq) {
        YcfBaseRequest<YcfCancelOrderReq> req = new YcfBaseRequest<>();
        req.setData(cancelOrderReq);
        return iYaoChuFaClient.cancelOrder(req);
    }

}

