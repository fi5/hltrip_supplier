package com.huoli.trip.supplier.web.controller;

import com.huoli.trip.common.vo.response.BaseResponse;
import com.huoli.trip.supplier.web.task.RefreshItemTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2020/8/31<br>
 */
@RestController
@RequestMapping(value = "/common", produces = "application/json")
@Slf4j
public class CommonController {

    @Autowired
    private RefreshItemTask refreshItemTask;

    @PostMapping("/refresh/item")
    public BaseResponse receivePrice(@RequestParam @NotBlank(message = "user不能为空") String user){
        try {
            log.info("开始刷新item。。word={}", user);
            refreshItemTask.refreshItemProduct();
        } catch (Exception e) {
            log.error("刷新item异常，word={}", user, e);
            return BaseResponse.withFail(-1, "刷新item失败");
        }
        return BaseResponse.withSuccess();
    }
}
