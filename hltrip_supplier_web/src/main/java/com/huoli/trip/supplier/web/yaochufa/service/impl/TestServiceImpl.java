package com.huoli.trip.supplier.web.yaochufa.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.huoli.trip.supplier.api.TestService;

/**
 * 描述：desc<br>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：顾刘川<br>
 * 版本：1.0<br>
 * 创建日期：2020/7/2<br>
 */
@Service(group = "test")
//@Component
public class TestServiceImpl implements TestService {
    @Override
    public String test() {
        return "测试";
    }
}
