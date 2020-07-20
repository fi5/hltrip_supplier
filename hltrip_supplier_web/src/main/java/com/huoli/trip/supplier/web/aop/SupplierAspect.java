package com.huoli.trip.supplier.web.aop;

import brave.Span;
import com.alibaba.fastjson.JSON;
import com.huoli.eagle.eye.core.HuoliAtrace;
import com.huoli.eagle.eye.core.HuoliTrace;
import com.huoli.eagle.eye.core.statistical.Event;
import com.huoli.trip.common.constant.CentralError;
import com.huoli.trip.common.vo.response.BaseResponse;
import com.huoli.trip.supplier.web.config.TraceConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;


/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2020/7/20<br>
 */
@Slf4j
@Component
@Aspect
public class SupplierAspect {

    /**
     * 日志跟踪类
     */
    @Autowired
    private HuoliTrace huoliTrace;
    /**
     * 事件上报处理类
     */
    @Autowired
    private HuoliAtrace huoliAtrace;

    @Pointcut("execution(* com.huoli.trip.supplier.web..controller..*.*(..)))")
    public void pointcut() {

    }

    @Around(value = "pointcut()")
    public Object around(ProceedingJoinPoint joinPoint){
        String function = joinPoint.getSignature().getName();
        Event.EventBuilder eventBuilder = new Event.EventBuilder();
        eventBuilder.withData("method", function);
        eventBuilder.withIndex(huoliAtrace.getAppname(), "service");
        Span span = (Span) TraceConfig.createSpan(function, this.huoliTrace);
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {
            Object args[] = joinPoint.getArgs();
            Object result;
            String params = "";
            if(ArrayUtils.isNotEmpty(args) && args[0] != null){
                for (Object arg : args) {
                    try {
                        log.info("直接打印参数  {}", arg);
                        params = JSON.toJSONString(arg);
                    } catch (Exception e) {
                        log.error("反序列化方法 {} 的请求参数异常", function, e);
                        params = "参数不能序列化";
                    }
                }
            } else {
                params = "无参数。";
            }
            try {
                log.info("[{}] request: {}", function, params);
                result = joinPoint.proceed(args);
            } catch  (Throwable e) {
                log.error("[{}] 服务器内部错误异常: ", function, e);
                result = BaseResponse.withFail(CentralError.ERROR_SERVER_ERROR);
            } finally {
                stopWatch.stop();
            }
            if (result == null) {
                log.error("[{}] result 为空", function);
                result = BaseResponse.fail(CentralError.ERROR_UNKNOWN);
            }
            log.info("[{}], response: {}, cost: {},", function, JSON.toJSONString(result),
                    stopWatch.getTotalTimeMillis());
            return result;
        } catch (Throwable e) {
            log.error("切面执行异常：", e);
            return BaseResponse.fail(CentralError.ERROR_UNKNOWN);
        }
    }
}
