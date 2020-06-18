package com.huoli.trip.supplier.feign.clinet;

import org.springframework.http.ResponseEntity;

import java.util.Map;

/**
 * 描述：desc<br>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：顾刘川<br>
 * 版本：1.0<br>
 * 创建日期：2020/6/18<br>
 */
public class YaoChuFaFailBack implements IYaoChuFaClient {
    @Override
    public ResponseEntity<Map> getWeather() {
        return null;
    }
}
