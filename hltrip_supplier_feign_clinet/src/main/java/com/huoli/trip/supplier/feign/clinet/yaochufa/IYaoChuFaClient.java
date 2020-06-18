package com.huoli.trip.supplier.feign.clinet.yaochufa;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Map;

/**
 * 描述：要出发客户端连接<br>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：顾刘川<br>
 * 版本：1.0<br>
 * 创建日期：2020/6/18<br>
 */
@FeignClient(name = "yaoChuFa", url = "${yaochufa.host.server}",configuration = YaoChuFaFeignInterceptor.class)
public interface IYaoChuFaClient {

    @RequestMapping(method = RequestMethod.POST,path = "/OTA/CheckAvail")
    String getWeather(@RequestBody Map req);
}
