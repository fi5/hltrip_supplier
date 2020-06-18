package com.huoli.trip.supplier.api.controller;

import com.huoli.trip.supplier.feign.clinet.IYaoChuFaClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 描述：desc<br>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：顾刘川<br>
 * 版本：1.0<br>
 * 创建日期：2020/6/18<br>
 */
@RestController
public class TestController {
    @Autowired
    private IYaoChuFaClient iYaoChuFaClient;

    @GetMapping(path = "/test")
    Map test() {
        return iYaoChuFaClient.getWeather();
    }

}
