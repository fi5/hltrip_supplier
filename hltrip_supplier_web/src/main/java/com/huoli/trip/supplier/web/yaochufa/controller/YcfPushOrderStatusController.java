package com.huoli.trip.supplier.web.yaochufa.controller;

import com.huoli.trip.supplier.api.YcfOrderService;
import com.huoli.trip.supplier.feign.client.yaochufa.client.IYaoChuFaClient;
import com.huoli.trip.supplier.self.yaochufa.vo.YcfRefundNoticeRequest;
import com.huoli.trip.supplier.self.yaochufa.vo.basevo.YcfBaseResult;
import com.huoli.trip.supplier.self.yaochufa.vo.push.OrderStatusInfo;
import com.huoli.trip.supplier.self.yaochufa.vo.push.YcfPushOrderStatusReq;
import com.huoli.trip.supplier.web.yaochufa.service.IYaoChuFaCallBackService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


/**
 * 描述: <br>订单推送
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：王德铭<br>
 * 版本：1.0<br>
 * 创建日期：2020/6/18<br>
 */
@RestController
@Api(description = "订单推送相关")
public class YcfPushOrderStatusController {
    @Autowired
    private IYaoChuFaClient iYaoChuFaClient;
    @Autowired
    private IYaoChuFaCallBackService ycfSynOrderStatusService;
    @Autowired
    YcfOrderService ycfOrderService;


    @ApiOperation("推送订单状态【【要触发渠道】调用】")
    @PostMapping(path = "/pushOrderStatus")
    YcfBaseResult<Boolean> payOrder(@RequestBody YcfPushOrderStatusReq req) {
        YcfBaseResult<Boolean> result = new YcfBaseResult<>();
        //TODO 接收到数据处理逻辑
        OrderStatusInfo orderStatusInfo = ycfSynOrderStatusService.synOrderStatus(req);
        result.setData(true);
        return result;
    }

    @ApiOperation("推送退款通知")
    @PostMapping(path = "/refundNotice")
    YcfBaseResult<Boolean> refundNotice(@RequestBody YcfRefundNoticeRequest req) {
        YcfBaseResult<Boolean> result = new YcfBaseResult<>();
        ycfOrderService.refundNotice(req);
        return YcfBaseResult.success();
    }

}
