package com.huoli.trip.supplier.web.yaochufa.controller;

import com.huoli.trip.supplier.feign.clinet.yaochufa.IYaoChuFaClient;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
@Api(description = "测试接口")
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

//    @ApiOperation(value = "feign测试", notes="测试")
//    @ApiImplicitParam(name = "req", value = "请求参数", paramType = "Map", required = true, dataType = "Map")
    @RequestMapping(path = "/feign",method = RequestMethod.POST)
    String feign(@RequestBody Map<String,String> req) {
        return "成功了";
    }
}
