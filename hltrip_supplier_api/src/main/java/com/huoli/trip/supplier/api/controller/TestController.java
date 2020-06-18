package com.huoli.trip.supplier.api.controller;

import com.huoli.trip.supplier.feign.clinet.yaochufa.IYaoChuFaClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
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
    String test() {
        //{
        //"data": {
        //"productId": "68859_42041",
        //"beginDate": "2016-05-01",
        //"endDate": "2016-05-05"
        //}
        //}
        Map<String,Object> req = new HashMap<>();
        Map<String,String> data = new HashMap<>();
        data.put("productId","247533_266960");
        data.put("beginDate","2020-07-01");
        data.put("endDate","2020-07-02");
        req.put("data",data);
        return iYaoChuFaClient.getWeather(req);
    }


    @RequestMapping(path = "/feign")
    String feign(HttpServletRequest request, @RequestBody Map<String,String> req) {
        System.out.println(request.getHeader("hotelId"));
        return "成功了";
    }
}
