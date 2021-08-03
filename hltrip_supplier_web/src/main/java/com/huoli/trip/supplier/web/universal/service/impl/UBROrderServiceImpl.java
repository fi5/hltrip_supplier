package com.huoli.trip.supplier.web.universal.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.huoli.eagle.eye.core.HuoliTrace;
import com.huoli.trip.supplier.api.UBROrderService;
import com.huoli.trip.supplier.feign.client.universal.client.IUBRClient;
import com.huoli.trip.supplier.self.universal.vo.UBROrderDetailResponse;
import com.huoli.trip.supplier.self.universal.vo.reqeust.UBRTicketOrderRequest;
import com.huoli.trip.supplier.self.universal.vo.response.UBRBaseResponse;
import com.huoli.trip.supplier.self.universal.vo.response.UBRTicketOrderResponse;
import com.huoli.trip.supplier.self.yaochufa.vo.BaseOrderRequest;
import com.huoli.trip.supplier.web.dao.ScenicSpotProductPriceDao;
import com.huoli.trip.supplier.web.mapper.TripOrderMapper;
import com.huoli.trip.supplier.web.mapper.TripOrderRefundMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/8/3<br>
 */
@Slf4j
@Service(timeout = 10000,group = "hltrip")
public class UBROrderServiceImpl implements UBROrderService {

    @Autowired
    private IUBRClient iubrClient;

    @Autowired
    TripOrderRefundMapper tripOrderRefundMapper;
    @Autowired
    TripOrderMapper tripOrderMapper;

    @Autowired
    private HuoliTrace huoliTrace;

    @Autowired
    private ScenicSpotProductPriceDao scenicSpotProductPriceDao;

    @Override
    public UBRBaseResponse<UBRTicketOrderResponse> createOrder(UBRTicketOrderRequest request){
        return iubrClient.order(request);
    }

    @Override
    public UBRBaseResponse refundCheck(BaseOrderRequest request){
        return iubrClient.refundCheck(request.getSupplierOrderId());
    }

    @Override
    public UBRBaseResponse<UBRTicketOrderResponse> refund(BaseOrderRequest request){
        return iubrClient.refund(request.getSupplierOrderId());
    }

    @Override
    public UBRBaseResponse<UBROrderDetailResponse> orderDetail(BaseOrderRequest request){
        return iubrClient.orderDetail(request.getSupplierOrderId());
    }
}
