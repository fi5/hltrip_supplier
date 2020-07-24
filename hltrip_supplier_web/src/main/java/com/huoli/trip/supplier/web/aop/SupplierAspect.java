package com.huoli.trip.supplier.web.aop;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.huoli.eagle.BraveTrace;
import com.huoli.eagle.eye.core.HuoliAtrace;
import com.huoli.eagle.eye.core.statistical.Event;
import com.huoli.eagle.eye.core.statistical.EventStatusEnum;
import com.huoli.trip.common.constant.SupplierError;
import com.huoli.trip.common.vo.response.BaseResponse;
import com.huoli.trip.supplier.self.yaochufa.vo.basevo.YcfBaseResult;
import com.huoli.trip.supplier.web.config.TraceConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
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
    private BraveTrace huoliTrace;
    /**
     * 事件上报处理类
     */
    @Autowired
    private HuoliAtrace huoliAtrace;

    @Pointcut("execution(* com.huoli.trip.supplier.web..controller..*.*(..)))")
    public void controller() {

    }
    /**
     * dubbo 切面
     */
    @Pointcut("(execution(* com.huoli.trip.supplier.web.yaochufa.service..*.*(..)))) && (@within(com.alibaba.dubbo.config.annotation.Service))")
    public void yaochufaService() {
    }

    @Around(value = "controller()")
    public Object aroundController(ProceedingJoinPoint joinPoint){
        String function = joinPoint.getSignature().getName();
        Event.EventBuilder eventBuilder = new Event.EventBuilder();
        eventBuilder.withData("method", function);
        eventBuilder.withIndex(huoliAtrace.getAppname(), "service");
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
                eventBuilder.withData("code", 0);
                eventBuilder.withStatus(EventStatusEnum.SUCCESS);
            } catch  (Throwable e) {
                log.error("[{}] 服务器内部错误异常: ", function, e);
                result = BaseResponse.withFail(SupplierError.SERVER_ERROR.getCode(), SupplierError.SERVER_ERROR.getError());
                eventBuilder.withData("code", SupplierError.SERVER_ERROR.getCode());
                eventBuilder.withStatus(EventStatusEnum.FAIL);
            } finally {
                stopWatch.stop();
            }
            if (result == null) {
                log.error("[{}] result 为空", function);
                result = BaseResponse.withFail(SupplierError.UNKNOWN_ERROR.getCode(), SupplierError.UNKNOWN_ERROR.getError());
                eventBuilder.withData("code", SupplierError.UNKNOWN_ERROR.getCode());
                eventBuilder.withStatus(EventStatusEnum.FAIL);
            }
            log.info("[{}], response: {}, cost: {},", function, JSON.toJSONString(result),
                    stopWatch.getTotalTimeMillis());
            return result;
        } catch (Throwable e) {
            log.error("切面执行异常：", e);
            return BaseResponse.withFail(SupplierError.UNKNOWN_ERROR.getCode(), SupplierError.UNKNOWN_ERROR.getError());
        }
    }

    @Around(value = "yaochufaService()")
    public Object aroundService(ProceedingJoinPoint joinPoint){
        try {
            return serviceCut(joinPoint);
        } catch (Throwable e) {
            return YcfBaseResult.fail();
        }
    }

    private Object serviceCut(ProceedingJoinPoint joinPoint) throws Throwable{
        String function = joinPoint.getSignature().getName();
        Event.EventBuilder eventBuilder = new Event.EventBuilder();
        eventBuilder.withData("method", function);
        eventBuilder.withIndex(huoliAtrace.getAppname(), "service");

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {
            Object args[] = joinPoint.getArgs();
            Object result;
            String params;
            if(ArrayUtils.isNotEmpty(args) && args[0] != null){
                try {
                    params = JSON.toJSONString(args[0]);
                    JSONObject param = JSONObject.parseObject(params);
                    if(StringUtils.isBlank(param.getString("traceId"))){
                        log.error("方法 {} 参数不包含traceId", function);
                    } else {
                        // 设置traceId
                        TraceConfig.createSpan(function, this.huoliTrace, param.getString("traceId"));
                    }
                } catch (Exception e) {
                    log.error("反序列化方法 {} 的请求参数异常，这是为了获取traceId，不影响主流程。", function, e);
                    params = "参数不能序列化";
                }
            } else {
                params = "无参数。";
            }
            try {
                log.info("[{}] request: {}", function, params);
                result = joinPoint.proceed(args);
                eventBuilder.withData("code", 0);
                eventBuilder.withStatus(EventStatusEnum.SUCCESS);
            } catch  (Throwable e) {
                log.error("[{}] 服务器内部错误异常: ", function, e);
                eventBuilder.withData("code", SupplierError.SERVER_ERROR.getCode());
                eventBuilder.withStatus(EventStatusEnum.FAIL);
                throw e;
            } finally {
                stopWatch.stop();
            }
            log.info("[{}], response: {}, cost: {},", function, JSON.toJSONString(result),
                    stopWatch.getTotalTimeMillis());
            return result;
        } catch (Throwable e) {
            eventBuilder.withData("code", SupplierError.UNKNOWN_ERROR.getCode());
            eventBuilder.withStatus(e);
            log.error("切面执行异常：", e);
            throw e;
        } finally {
            Event event = eventBuilder.build();
            huoliAtrace.reportEvent(event);
        }
    }
}
