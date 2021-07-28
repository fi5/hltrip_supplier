package com.huoli.trip.supplier.feign.client.universal.interceptor;

import com.alibaba.fastjson.JSON;
import com.huoli.trip.common.constant.ConfigConstants;
import com.huoli.trip.common.util.ConfigGetter;
import com.huoli.trip.supplier.feign.client.universal.client.IUBRClient;
import com.huoli.trip.supplier.self.universal.constant.UBRConstants;
import com.huoli.trip.supplier.self.universal.vo.reqeust.UBRLoginRequest;
import com.huoli.trip.supplier.self.universal.vo.response.UBRBaseResponse;
import com.huoli.trip.supplier.self.universal.vo.response.UBRLoginResponse;
import com.xiaoleilu.hutool.lang.Base64;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

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
public class UBRFeignInterceptor implements RequestInterceptor {

    @Autowired
    private RedisTemplate clientJedisTemplate;

    @Autowired
    private IUBRClient iubrClient;

    @Override
    public void apply(RequestTemplate requestTemplate) {
        try {
            if(StringUtils.equals(requestTemplate.url(), "/api/v1/user/login")){
                return;
            }
            String token;
            if(!clientJedisTemplate.hasKey(UBRConstants.AUTH_KEY)) {
                token = getToken();
            } else {
                token = clientJedisTemplate.opsForValue().get(UBRConstants.AUTH_KEY).toString();
            }
            if(StringUtils.isNotBlank(token)){
                token = String.format("%s%s", "Bearer ", token);
                log.info("环球影城token={}", token);
            } else {
                log.error("环球影城token过期了，redis拿不到。");
                return;
            }
            requestTemplate.header("Authorization", token);
            // token 有效期7天。小于24小时的时候就刷新一下
            if(clientJedisTemplate.getExpire(UBRConstants.AUTH_KEY, TimeUnit.HOURS) < 24){
                refreshToken();
            }
        } catch (Throwable e) {
            log.error("设置环球影城鉴权参数异常，", e);
        }
    }

    private String getToken(){
        String account = ConfigGetter.getByFileItemString(UBRConstants.CONFIG_FILE_UBR, UBRConstants.CONFIG_ITEM_ACCOUNT);
        String password = ConfigGetter.getByFileItemString(UBRConstants.CONFIG_FILE_UBR, UBRConstants.CONFIG_ITEM_PASSWORD);
        UBRLoginRequest request = new UBRLoginRequest();
        request.setAccount(account);
        request.setPassword(Base64.encode(password));
        log.info("请求环球影城登录，request={}", JSON.toJSONString(request));
        UBRBaseResponse<UBRLoginResponse> response = iubrClient.login(request);
        if(response.getCode() != 200){
            log.error("环球影城登录失败，code={}, msg={}", response.getCode(), response.getMsg());
            return null;
        }
        if(response.getData() == null || response.getData().getAuth() == null
         || StringUtils.isBlank(response.getData().getAuth().getToken())){
            log.error("环球影城没有返回正确的登录信息，code={}, msg={}, data={}",
                    response.getCode(), response.getMsg(), response.getData() == null ? null : JSON.toJSONString(response.getData()));
            return null;
        }
        String token = response.getData().getAuth().getToken();
        clientJedisTemplate.opsForValue().set(UBRConstants.AUTH_KEY, token);
        return token;
    }

    private String refreshToken(){
        UBRBaseResponse<UBRLoginResponse> response = iubrClient.refreshToken();
        if(response.getCode() != 200){
            log.error("环球影城刷新token失败，code={}, msg={}", response.getCode(), response.getMsg());
            return null;
        }
        if(response.getData() == null || response.getData().getAuth() == null
                || StringUtils.isBlank(response.getData().getAuth().getToken())){
            log.error("环球影城没有返回正确的鉴权信息，code={}, msg={}, data={}",
                    response.getCode(), response.getMsg(), response.getData() == null ? null : JSON.toJSONString(response.getData()));
            return null;
        }
        String token = response.getData().getAuth().getToken();
        clientJedisTemplate.opsForValue().set(UBRConstants.AUTH_KEY, token, (7 * 24), TimeUnit.HOURS);
        return token;
    }
}
