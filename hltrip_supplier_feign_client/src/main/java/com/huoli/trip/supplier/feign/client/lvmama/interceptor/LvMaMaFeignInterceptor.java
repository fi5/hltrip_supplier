package com.huoli.trip.supplier.feign.client.lvmama.interceptor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.huoli.trip.common.constant.ConfigConstants;
import com.huoli.trip.common.util.ConfigGetter;
import com.huoli.trip.common.util.MD5Util;
import com.huoli.trip.supplier.self.lvmama.vo.request.LmmBaseRequest;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;


import java.util.Locale;

import static com.huoli.trip.supplier.self.lvmama.constant.LmmConfigConstants.CONFIG_ITEM_API_KEY;
import static com.huoli.trip.supplier.self.lvmama.constant.LmmConfigConstants.CONFIG_ITEM_API_SECRET;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2020/12/8<br>
 */
@Slf4j
public class LvMaMaFeignInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate requestTemplate) {
        try {
            String appKey = ConfigGetter.getByFileItemString(ConfigConstants.CONFIG_FILE_LVMAMA, CONFIG_ITEM_API_KEY);
            String secretKey = ConfigGetter.getByFileItemString(ConfigConstants.CONFIG_FILE_LVMAMA, CONFIG_ITEM_API_SECRET);
            long time = System.currentTimeMillis();
            String sign = MD5Util.encode(String.format("%s%s%s", secretKey, time, secretKey)).toLowerCase(Locale.ROOT);
            log.info("公共参数：appKey={},secret={},time={},sign={}", appKey, secretKey, time, sign);
            if(StringUtils.equalsIgnoreCase(requestTemplate.method(), RequestMethod.GET.toString())){
                requestTemplate.query("appKey", appKey);
                requestTemplate.query("timestamp", String.valueOf(time));
                requestTemplate.query("sign", sign);
                requestTemplate.query("messageFormat", "json");
            } else if(StringUtils.equalsIgnoreCase(requestTemplate.method(), RequestMethod.POST.toString())){
                byte[] body = requestTemplate.body();
                if(body != null && body.length > 0){
                    LmmBaseRequest request = JSONObject.parseObject(body, LmmBaseRequest.class);
                    request.setAppKey(appKey);
                    request.setSign(sign);
                    request.setTimestamp(String.valueOf(time));
                    log.info("驴妈妈feign拦截器POST，最终请求参数，request = {}", JSON.toJSONString(request));
                    requestTemplate.body(JSON.toJSONString(request));
                } else {
                    log.error("驴妈妈feign拦截器POST，没有请求体，过滤掉。");
                }
            }
        } catch (Throwable e) {
            log.error("设置驴妈妈公共请求参数异常，", e);
        }
    }
}
