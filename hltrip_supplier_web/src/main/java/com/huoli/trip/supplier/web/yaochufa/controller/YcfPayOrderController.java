package com.huoli.trip.supplier.web.yaochufa.controller;

import com.huoli.trip.supplier.feign.clinet.yaochufa.IYaoChuFaClient;
import com.huoli.trip.supplier.self.yaochufa.vo.basevo.YcfCommonResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.huoli.trip.supplier.self.yaochufa.vo.basevo.YcfBaseRequest;
import com.huoli.trip.supplier.self.yaochufa.vo.YcfPayOrderReq;
import com.huoli.trip.supplier.self.yaochufa.vo.YcfPayOrderRes;


/**
 * 描述: <br>支付订单
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：王德铭<br>
 * 版本：1.0<br>
 * 创建日期：2020/6/18<br>
 */
@RestController
@Api(description = "订单支付相关")
public class YcfPayOrderController {
    @Autowired
    private IYaoChuFaClient iYaoChuFaClient;
    @ApiOperation("支付订单服务")
    @PostMapping(path = "/payOrder")
    YcfCommonResult<YcfPayOrderRes> payOrder(@RequestBody YcfBaseRequest<YcfPayOrderReq> req) {
        return iYaoChuFaClient.payOrder(req);
    }

}
