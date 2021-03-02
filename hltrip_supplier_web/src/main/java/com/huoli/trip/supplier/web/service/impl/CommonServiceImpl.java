package com.huoli.trip.supplier.web.service.impl;

import com.alibaba.fastjson.JSON;
import com.huoli.trip.common.entity.BackChannelEntry;
import com.huoli.trip.supplier.web.mapper.BackChannelMapper;
import com.huoli.trip.supplier.web.service.CommonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/3/2<br>
 */
public class CommonServiceImpl implements CommonService {

    @Autowired
    private BackChannelMapper backChannelMapper;

    @Autowired
    private RedisTemplate jedisTemplate;

    @Override
    public BackChannelEntry getSupplierById(String supplierId){
        String key = String.join("_", "SUPPLIER_", supplierId);
        if(jedisTemplate.hasKey(key)){
            Object value = jedisTemplate.opsForValue().get(key);
            if(value != null){
                return JSON.parseObject(value.toString(), BackChannelEntry.class);
            }
            return null;
        }
        BackChannelEntry backChannelEntry = backChannelMapper.getChannelInfoByChannelCode(supplierId);
        if(backChannelEntry != null){
            jedisTemplate.opsForValue().set(key, JSON.toJSONString(backChannelEntry), 1, TimeUnit.DAYS);
            return backChannelEntry;
        }
        return null;
    }
}
