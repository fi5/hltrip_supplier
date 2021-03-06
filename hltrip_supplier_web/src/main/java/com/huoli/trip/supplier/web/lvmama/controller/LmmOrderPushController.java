package com.huoli.trip.supplier.web.lvmama.controller;

import com.alibaba.fastjson.JSONObject;
import com.huoli.trip.supplier.api.LvmamaOrderService;
import com.huoli.trip.supplier.self.lvmama.vo.push.LmmOrderPushRequest;
import com.huoli.trip.supplier.self.lvmama.vo.push.LmmRefundPushRequest;
import com.huoli.trip.supplier.self.lvmama.vo.response.LmmBaseResponse;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    LmmBaseResponse pushOrderStatus(@RequestParam("order") String order) {
        log.info("驴妈供应商触发了订单推送：{}", order);
        return lvmamaOrderService.orderStatusNotice(order);
    }

    @PostMapping(path = "/pushOrderRefund")
    LmmBaseResponse pushOrderRefund(@RequestParam("order") String order) {
        log.info("驴妈供应商触发了退款推送：{}", order);
        return lvmamaOrderService.pushOrderRefund(order);
    }
}
