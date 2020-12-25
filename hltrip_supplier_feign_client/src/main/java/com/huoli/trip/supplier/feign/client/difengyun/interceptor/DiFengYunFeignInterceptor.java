package com.huoli.trip.supplier.feign.client.difengyun.interceptor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.huoli.trip.common.constant.ConfigConstants;
import com.huoli.trip.common.util.ConfigGetter;
import com.huoli.trip.common.util.DateTimeUtil;
import com.huoli.trip.common.util.DesUtil;
import com.huoli.trip.supplier.self.difengyun.util.DfySignature;
import com.huoli.trip.supplier.self.difengyun.vo.request.DfyBaseRequest;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
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

//    @Value("${difengyun.api.key}")
//    private String apiKey;
//
//    @Value("${difengyun.api.secret.key}")
//    private String secretKey;

    @Override
    public void apply(RequestTemplate requestTemplate) {
        try {
            String apiKey = ConfigGetter.getByFileItemString(ConfigConstants.CONFIG_FILE_DIFENGYUN,"difengyun.api.key");
            String secretKey = ConfigGetter.getByFileItemString(ConfigConstants.CONFIG_FILE_DIFENGYUN,"difengyun.api.secret.key");
            byte[] body = requestTemplate.body();
            if(body != null && body.length > 0){
                String time = DateTimeUtil.formatFullDate(new Date());
                DfyBaseRequest request = JSONObject.parseObject(body, DfyBaseRequest.class);
                // 签名要把data层的属性和base层的合起来，所以以data为准加上base层的参数，因为base层的参数少这样合方便
                JSONObject bodyObj = JSONObject.parseObject(JSON.toJSONString(request.getData()));
                bodyObj.put("apiKey", apiKey);
                // 不同的业务key不同，如果请求里有就用请求的
                if(StringUtils.isNotBlank(request.getApiKey())){
                    bodyObj.put("apiKey", request.getApiKey());
                }
                bodyObj.put("timestamp", time);
                log.info("笛风云feign拦截器，准备获取签名，secretKey = {}, bodyObj = {} ", secretKey, bodyObj.toJSONString());
                String sign = DfySignature.getSignature(bodyObj, secretKey);
                log.info("获取到签名，sign = {}", sign);
                request.setApiKey(apiKey);
                request.setSign(sign);
                request.setTimestamp(time);
                // 业务参数data需要单独加密，实际传参的时候传加密后的
//                log.info("笛风云feign拦截器，准备加密业务数据，secretKey = {}, data = {}", secretKey, JSON.toJSONString(request.getData()));
//                String data = new String(Base64.getEncoder().encode(DesUtil.encrypt(JSON.toJSONString(request.getData()).getBytes(StandardCharsets.UTF_8), secretKey)), StandardCharsets.UTF_8);
//                log.info("业务数据加密完成，base64data = {}", data);
//                request.setData(data);
                log.info("笛风云feign拦截器，最终请求参数，request = {}", JSON.toJSONString(request));
                requestTemplate.body(JSON.toJSONString(request));
            } else {
                log.error("笛风云feign拦截器，没有请求体，过滤掉。");
            }
        } catch (Throwable e) {
            log.error("设置笛风云公共请求参数异常，", e);
        }
    }
}
