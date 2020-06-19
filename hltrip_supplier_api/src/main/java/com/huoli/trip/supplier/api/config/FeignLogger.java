package com.huoli.trip.supplier.api.config;

import feign.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 描述: <br> Feign日志配置类
 * FeignClientLoggerLevel：
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：王德铭<br>
 * 版本：1.0<br>
 * 创建日期：2020/6/19<br>
 */
@Configuration
public class FeignLogger {
    //全量日志，debug环境下可以使用该配置
    @Bean
    Logger.Level FeignClientLoggerLevel() {
        return Logger.Level.FULL;
    }
    //优先加载该配置，上正式环境只输出Info级别日志即可
    @Bean
    Logger HltripFeignLogger() {
        return new HltripFeignLogger();
    }
}
