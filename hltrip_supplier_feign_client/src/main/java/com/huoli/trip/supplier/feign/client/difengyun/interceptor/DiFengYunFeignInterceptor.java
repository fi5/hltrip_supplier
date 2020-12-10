package com.huoli.trip.supplier.feign.client.difengyun.interceptor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.huoli.trip.common.util.DateTimeUtil;
import com.huoli.trip.supplier.self.difengyun.vo.request.DfyBaseRequest;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2020/12/8<br>
 */
@Component
@Slf4j
public class DiFengYunFeignInterceptor implements RequestInterceptor {

    @Value("${dyf.api.key}")
    private String apiKey;

    @Override
    public void apply(RequestTemplate requestTemplate) {
        try {
            String body = requestTemplate.bodyTemplate();
            if(StringUtils.isNotBlank(body)){
                DfyBaseRequest request = JSONObject.parseObject(body, DfyBaseRequest.class);
                String time = DateTimeUtil.formatFullDate(new Date());
                String sign = String.format("%s%s", apiKey, time);
                request.setApiKey(apiKey);
                request.setSign(sign);
                request.setTimestamp(time);
                requestTemplate.body(JSON.toJSONString(request));
            }
        } catch (Throwable e) {
            log.error("设置笛风云公共请求参数异常，", e);
        }
    }
}
