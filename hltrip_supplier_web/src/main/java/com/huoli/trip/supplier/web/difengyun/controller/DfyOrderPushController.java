package com.huoli.trip.supplier.web.difengyun.controller;

import com.alibaba.fastjson.JSONObject;
import com.huoli.trip.common.vo.response.BaseResponse;
import com.huoli.trip.supplier.self.difengyun.vo.push.DfyOrderPushRequest;
import com.huoli.trip.supplier.self.difengyun.vo.push.DfyOrderPushResponse;
import com.huoli.trip.supplier.self.difengyun.vo.response.DfyBaseResult;
import com.huoli.trip.supplier.self.yaochufa.vo.basevo.YcfBaseResult;
import com.huoli.trip.supplier.self.yaochufa.vo.push.YcfPushOrderStatusReq;
import com.huoli.trip.supplier.web.difengyun.service.impl.DfyCallBackService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author lunatic
 * @Title:
 * @Package
 * @Description:
 * @date 2020/12/1015:05
 */
@RestController
@Api(description = "订单推送相关")
@Slf4j
@RequestMapping(value = "/api")
public class DfyOrderPushController {
    @Autowired
    DfyCallBackService dfyCallBackService;

    @PostMapping(path = "/pushOrderStatus")
    DfyBaseResult pushOrderStatus(@RequestBody DfyOrderPushRequest request) {
        log.info("供应商触发了订单推送 订单号：{}", JSONObject.toJSONString(request));
        return dfyCallBackService.orderStatusNotice(request);
    }
}
