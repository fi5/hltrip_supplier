package com.huoli.trip.supplier.api.controller;

import com.huoli.trip.common.entity.CommonResult;
import com.huoli.trip.common.entity.OrderStatusResult;
import com.huoli.trip.common.entity.VochersResult;
import com.huoli.trip.supplier.feign.clinet.yaochufa.IYaoChuFaClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

/**
 * yaochufa订单相关接口控制器
 */
@RestController
public class YaoChuFaOrderController {
    @Autowired
    private IYaoChuFaClient iYaoChuFaClient;


    CommonResult<VochersResult> getVochers(String partnerOrderId){
        return iYaoChuFaClient.getVochers(partnerOrderId);

    }

    CommonResult<OrderStatusResult> getOrderStatus(String partnerOrderId){
        return iYaoChuFaClient.getOederStatus(partnerOrderId);
    }



}
