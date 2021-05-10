package com.huoli.trip.supplier.web.lvmama.controller;

import com.alibaba.fastjson.JSONObject;
import com.huoli.trip.supplier.self.lvmama.vo.push.LmmRefundPushRequest;
import com.huoli.trip.supplier.self.lvmama.vo.response.LmmBaseResponse;
import lombok.extern.slf4j.Slf4j;
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
 * 创建日期：2021/5/10<br>
 */
@RestController
@Slf4j
@RequestMapping(value = "/lmm/product")
public class LmmProductController {

    @PostMapping(path = "/receive")
    public LmmBaseResponse productUpdate(@RequestBody LmmRefundPushRequest request) {
        // todo 接收推送
        log.info("：{}", JSONObject.toJSONString(request));
        return null;
    }
}
