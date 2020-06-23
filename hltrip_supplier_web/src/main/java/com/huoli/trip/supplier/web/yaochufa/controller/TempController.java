package com.huoli.trip.supplier.web.yaochufa.controller;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2020/6/19<br>
 */
@RestController()
@Slf4j
public class TempController {

    @PostMapping("/test/push")
    public String cabinFilter(@RequestBody Object body){
        log.info("yaochufa body = {}", JSON.toJSONString(body));
        return "ok";
    }

}
