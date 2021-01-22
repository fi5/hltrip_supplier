package com.huoli.trip.supplier.web.yaochufa.controller;

import com.alibaba.fastjson.JSONObject;
import com.huoli.trip.supplier.feign.client.yaochufa.client.IYaoChuFaClient;
import com.huoli.trip.supplier.self.yaochufa.vo.YcfRefundNoticeRequest;
import com.huoli.trip.supplier.self.yaochufa.vo.basevo.YcfBaseResult;
import com.huoli.trip.supplier.self.yaochufa.vo.push.YcfPushOrderStatusReq;
import com.huoli.trip.supplier.web.yaochufa.service.IYaoChuFaCallBackService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class YcfPushOrderController {
    @Autowired
    private IYaoChuFaClient iYaoChuFaClient;
    @Autowired
    private IYaoChuFaCallBackService yaoChuFaCallBackService;


    @ApiOperation("推送订单状态【【要触发渠道】调用】")
    @PostMapping(path = "/pushOrderStatus")
    YcfBaseResult<Boolean> pushOrderStatus(@RequestBody YcfPushOrderStatusReq req) {
        log.info("供应商触发了订单推送 订单号：{}",req.getPartnerOrderId());
        try{
            yaoChuFaCallBackService.orderStatusNotice(req);
        }catch (Exception e){
            log.error("推送订单状态失败",e);
            return YcfBaseResult.fail();
        }
        return YcfBaseResult.success();
    }

    @ApiOperation("推送退款通知")
    @PostMapping(path = "/refundNotice")
    YcfBaseResult<Boolean> refundNotice(@RequestBody YcfRefundNoticeRequest req) {
        log.info("供应商触发了推送退款通知：{}", JSONObject.toJSONString(req));
        yaoChuFaCallBackService.refundNotice(req);
        return YcfBaseResult.success();
    }

}
