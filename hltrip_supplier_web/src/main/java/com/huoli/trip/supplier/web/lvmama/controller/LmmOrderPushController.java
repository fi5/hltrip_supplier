package com.huoli.trip.supplier.web.lvmama.controller;

import com.alibaba.fastjson.JSONObject;
import com.huoli.trip.supplier.api.LvmamaOrderService;
import com.huoli.trip.supplier.self.lvmama.vo.push.LmmOrderPushRequest;
import com.huoli.trip.supplier.self.lvmama.vo.response.LmmBaseResponse;
import io.swagger.annotations.Api;
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
@RequestMapping(value = "/lmm/receive")
public class LmmOrderPushController {
    @Autowired
    LvmamaOrderService lvmamaOrderService;

    @PostMapping(path = "/pushOrderStatus")
    LmmBaseResponse pushOrderStatus(@RequestBody LmmOrderPushRequest request) {
        log.info("驴妈供应商触发了订单推送 订单号：{}", JSONObject.toJSONString(request));
        return lvmamaOrderService.orderStatusNotice(request);
    }
}
