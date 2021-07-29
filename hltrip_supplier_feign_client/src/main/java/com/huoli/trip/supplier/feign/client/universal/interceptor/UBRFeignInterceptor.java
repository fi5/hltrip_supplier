package com.huoli.trip.supplier.feign.client.universal.interceptor;

import com.huoli.trip.supplier.self.universal.constant.UBRConstants;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2020/12/8<br>
 */
@Slf4j
public class UBRFeignInterceptor implements RequestInterceptor {

    @Autowired
    private RedisTemplate clientJedisTemplate;

    @Override
    public void apply(RequestTemplate requestTemplate) {
        try {
            if(StringUtils.equals(requestTemplate.url(), "/api/v1/user/login")){
                return;
            }
            if(!clientJedisTemplate.hasKey(UBRConstants.AUTH_KEY)) {
                log.error("环球影城token过期了，redis拿不到。");
                return;
            } else {
                String token = clientJedisTemplate.opsForValue().get(UBRConstants.AUTH_KEY).toString();
                requestTemplate.header("Authorization", token);
            }
        } catch (Throwable e) {
            log.error("设置环球影城鉴权参数异常，", e);
        }
    }

}
