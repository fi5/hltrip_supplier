package com.huoli.trip.supplier.web.aop;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.huoli.trip.common.constant.CentralError;
import com.huoli.trip.common.vo.response.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
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

    @Pointcut("execution(* com.huoli.trip.supplier.web..controller..*.*(..)))")
    public void pointcut() {

    }

    @Around(value = "pointcut()")
    public Object around(ProceedingJoinPoint joinPoint){
        String function = joinPoint.getSignature().getName();
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {
            Object args[] = joinPoint.getArgs();
            Object result;
            String params;
            if(ArrayUtils.isNotEmpty(args) && args[0] != null){
                try {
                    params = JSON.toJSONString(args);
                    JSONObject param = JSONObject.parseObject(JSON.toJSONString(args[0]));
                } catch (Exception e) {
                    log.error("反序列化方法 {} 的请求参数异常，这是为了获取traceId，不影响主流程。", e);
                    params = "参数不能序列化";
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
