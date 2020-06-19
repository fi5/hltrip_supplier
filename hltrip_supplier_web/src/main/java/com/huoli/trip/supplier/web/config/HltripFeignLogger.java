package com.huoli.trip.supplier.web.config;

import feign.Request;
import feign.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * 描述: <br> 自定义info级别日志输出
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：王德铭<br>
 * 版本：1.0<br>
 * 创建日期：2020/6/19<br>
 */
public class HltripFeignLogger extends feign.Logger {

    private final Logger logger;

    public HltripFeignLogger() {
        this(feign.Logger.class);
    }
    public HltripFeignLogger(Class<?> clazz) {
        this(LoggerFactory.getLogger(clazz));
    }
    public HltripFeignLogger(String name) {
        this(LoggerFactory.getLogger(name));
    }
    HltripFeignLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    protected void logRequest(String configKey, Level logLevel, Request request) {
        if (logger.isInfoEnabled()) {
            super.logRequest(configKey, logLevel, request);
        }
    }

    @Override
    protected Response logAndRebufferResponse(String configKey, Level logLevel, Response response, long elapsedTime)
            throws IOException {
        if (logger.isInfoEnabled()) {
            return super.logAndRebufferResponse(configKey, logLevel, response, elapsedTime);
        }
        return response;
    }

    @Override
    protected void log(String configKey, String format, Object... args) {
        // Not using SLF4J's support for parameterized messages (even though it
        // would be more efficient) because it would
        // require the incoming message formats to be SLF4J-specific.
        if (logger.isInfoEnabled()) {
            logger.info(String.format(methodTag(configKey) + format, args));
        }
    }
}
