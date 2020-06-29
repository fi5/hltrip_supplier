package com.huoli.trip.supplier.web.yaochufa.controller;

import com.huoli.trip.supplier.feign.client.yaochufa.client.IYaoChuFaClient;
import com.huoli.trip.supplier.self.yaochufa.vo.*;
import com.huoli.trip.supplier.self.yaochufa.vo.basevo.YcfBaseRequest;
import com.huoli.trip.supplier.self.yaochufa.vo.basevo.YcfBaseResult;
import com.huoli.trip.supplier.web.yaochufa.service.impl.YaoChuFaCallBackService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


/**
 * 描述: <br>订单
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：王德铭<br>
 * 版本：1.0<br>
 * 创建日期：2020/6/18<br>
 */
@RestController
@Api(description = "yaochufa订单相关接口控制器")
public class YcfOrderController {
    @Autowired
    private IYaoChuFaClient iYaoChuFaClient;

    @Autowired
    private YaoChuFaCallBackService yaoChuFaCallBackService;

    @ApiOperation("可预订检查服务")
    @PostMapping(path = "/getCheckInfos")
    YcfBaseResult<YcfBookCheckRes> getCheckInfos(@RequestBody YcfBaseRequest<YcfBookCheckReq> req) {
        return iYaoChuFaClient.getCheckInfos(req);
    }


    @RequestMapping(method = {RequestMethod.POST},value="/api/service/yaochufa/refundNotice")
    private void refundNotice(@RequestBody YcfRefundNoticeRequest request){
        yaoChuFaCallBackService.refundNotice(request);
    }

    @ApiOperation("支付订单服务")
    @PostMapping(path = "/payOrder")
    YcfBaseResult<YcfPayOrderRes> payOrder(@RequestBody YcfBaseRequest<YcfPayOrderReq> req) {
        return iYaoChuFaClient.payOrder(req);
    }

    @ApiOperation("创建订单")
    @PostMapping(path = "/createOrder")
    YcfBaseResult<YcfCreateOrderRes> createOrder(@RequestBody YcfBaseRequest<YcfCreateOrderReq> req) {
        return iYaoChuFaClient.createOrder(req);
    }

    @ApiOperation("申请取消订单")
    @PostMapping(path = "/cancelOrder")
    YcfBaseResult<YcfCancelOrderRes> cancelOrder(@RequestBody YcfBaseRequest<YcfCancelOrderReq> req) {
        return iYaoChuFaClient.cancelOrder(req);
    }
}
