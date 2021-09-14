package com.huoli.trip.supplier.web.universal.controller;

import com.huoli.trip.common.entity.TripOrder;
import com.huoli.trip.supplier.api.UBROrderService;
import com.huoli.trip.supplier.api.UBRProductService;
import com.huoli.trip.supplier.self.difengyun.vo.response.DfyBaseResult;
import com.huoli.trip.supplier.self.yaochufa.vo.BaseOrderRequest;
import com.huoli.trip.supplier.web.mapper.TripOrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/5/17<br>
 */
@RestController
@Slf4j
@RequestMapping(value = "/ubr/test")
public class UBRTestController {

    @Autowired
    private UBRProductService ubrProductService;

    @Autowired
    private UBROrderService ubrOrderService;

    @Autowired
    private TripOrderMapper tripOrderMapper;

    @Autowired

    @PostMapping(path = "/init")
    public DfyBaseResult UBRInit() {
        ubrProductService.init();
        return DfyBaseResult.success();
    }

    @PostMapping(path = "/refresh")
    public DfyBaseResult UBRRefreshToken() {
        ubrProductService.refreshToken();
        return DfyBaseResult.success();
    }

    @PostMapping(path = "/login")
    public DfyBaseResult UBRLogin() {
        ubrProductService.getToken();
        return DfyBaseResult.success();
    }

    @PostMapping(path = "/sync/product")
    public DfyBaseResult UBRSyncProduct(@RequestBody String type) {
        ubrProductService.syncProduct(type);
        return DfyBaseResult.success();
    }

    @PostMapping(path = "/refund/check")
    public DfyBaseResult UBRRefundCheck(@RequestBody String outOrderId) {
        BaseOrderRequest request = new BaseOrderRequest();
        request.setSupplierOrderId(outOrderId);
        return DfyBaseResult.success(ubrOrderService.refundCheck(request));
    }

    @PostMapping(path = "/order/detail")
    public DfyBaseResult UBROrderDetail(@RequestBody String orderId) {
        TripOrder tripOrder = tripOrderMapper.getOrderByOrderId(orderId);
        if(tripOrder != null && StringUtils.isNotBlank(tripOrder.getOutOrderId())){
            orderId = tripOrder.getOutOrderId();
        }
        BaseOrderRequest request = new BaseOrderRequest();
        request.setSupplierOrderId(orderId);
        return DfyBaseResult.success(ubrOrderService.orderDetail(request));
    }
}
