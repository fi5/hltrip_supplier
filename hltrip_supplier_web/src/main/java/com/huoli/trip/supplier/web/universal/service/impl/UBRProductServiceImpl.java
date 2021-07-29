package com.huoli.trip.supplier.web.universal.service.impl;

import com.alibaba.fastjson.JSON;
import com.huoli.trip.common.util.ConfigGetter;
import com.huoli.trip.common.vo.response.BaseResponse;
import com.huoli.trip.supplier.feign.client.universal.client.IUBRClient;
import com.huoli.trip.supplier.self.universal.constant.UBRConstants;
import com.huoli.trip.supplier.self.universal.vo.UBRTicketList;
import com.huoli.trip.supplier.self.universal.vo.reqeust.UBRLoginRequest;
import com.huoli.trip.supplier.self.universal.vo.reqeust.UBRTicketListRequest;
import com.huoli.trip.supplier.self.universal.vo.response.UBRBaseResponse;
import com.huoli.trip.supplier.self.universal.vo.response.UBRLoginResponse;
import com.huoli.trip.supplier.web.universal.service.UBRProductService;
import com.xiaoleilu.hutool.lang.Base64;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/7/28<br>
 */
@Service
@Slf4j
public class UBRProductServiceImpl implements UBRProductService {

    @Autowired
    private IUBRClient ubrClient;

    @Autowired
    private RedisTemplate jedisTemplate;

    @PostConstruct
    @Async
    public void checkUserInfo(){
        try {
            Thread.sleep(30000);
            if(!jedisTemplate.hasKey(UBRConstants.AUTH_KEY)) {
                log.info("环球影城token过期，准备重新登录。。");
                getToken();
            } else {
                Long hours = jedisTemplate.getExpire(UBRConstants.AUTH_KEY, TimeUnit.HOURS);
                log.info("环球影城token有效期还有{}小时", hours);
                // token 有效期7天。小于24小时的时候就刷新一下
                if(jedisTemplate.getExpire(UBRConstants.AUTH_KEY, TimeUnit.HOURS) < 24){
                    log.info("环球影城token有效期小于24小时，准备刷新。。");
                    refreshToken();
                    log.info("环球影城token刷新完成。。");
                }
            }
        } catch (Throwable e) {
            log.error("环球影城检查登录信息异常，", e);
        }
    }

    @Override
    public UBRTicketList getTicketList(UBRTicketListRequest request){
        UBRBaseResponse<UBRTicketList> response = ubrClient.getTicketList(request);
        if(response == null){
            log.error("环球影城门票列表无返回内容");
            return null;
        }
        if(response.getCode() != 200){
            log.error("环球影城门票列表返回失败，code={}, msg={}", response.getCode(), response.getMsg());
            return null;
        }
        if(response.getData() == null){
            log.error("环球影城门票列表返回空数据");
            return null;
        }
        return response.getData();
    }

    @Override
    public void init(){
        UBRBaseResponse response = ubrClient.init();
        if(response == null){
            log.error("环球影城初始化无返回内容");
            return;
        }
        if(response.getCode() != 200){
            log.error("环球影城初始化返回失败，code={}, msg={}", response.getCode(), response.getMsg());
            return;
        }
        if(response.getData() == null){
            log.error("环球影城初始化返回空数据");
            return;
        }
        log.info("环球影城初始化返回：{}", JSON.toJSONString(response));
    }

    public void syncProduct(String type){
        UBRTicketListRequest request = new UBRTicketListRequest();
        request.setType(type);
        UBRTicketList ubrTicketList = getTicketList(request);

    }

    @Override
    public String getToken(){
        String account = ConfigGetter.getByFileItemString(UBRConstants.CONFIG_FILE_UBR, UBRConstants.CONFIG_ITEM_ACCOUNT);
        String password = ConfigGetter.getByFileItemString(UBRConstants.CONFIG_FILE_UBR, UBRConstants.CONFIG_ITEM_PASSWORD);
        UBRLoginRequest request = new UBRLoginRequest();
        request.setAccount(account);
        request.setPassword(Base64.encode(password));
        log.info("请求环球影城登录，request={}", JSON.toJSONString(request));
        UBRBaseResponse<UBRLoginResponse> response = ubrClient.login(request);
        if(response == null){
            log.error("环球影城登录无返回内容");
            return null;
        }
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
        log.info("环球影城登录成功，token={}, result={}", token, JSON.toJSONString(response));
        jedisTemplate.opsForValue().set(UBRConstants.AUTH_KEY, token, (7 * 24), TimeUnit.HOURS);
        return token;
    }

    private String refreshToken(){
        UBRBaseResponse<UBRLoginResponse> response = ubrClient.refreshToken();
        if(response == null){
            log.error("环球影城刷新token无返回内容");
            return null;
        }
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
        log.info("环球影城刷新token成功，最新token={}, result={}", token, JSON.toJSONString(response));
        jedisTemplate.opsForValue().set(UBRConstants.AUTH_KEY, token, (7 * 24), TimeUnit.HOURS);
        return token;
    }
}
