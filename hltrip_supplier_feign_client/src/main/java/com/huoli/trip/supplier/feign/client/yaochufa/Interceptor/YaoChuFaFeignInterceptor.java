package com.huoli.trip.supplier.feign.client.yaochufa.Interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;

/**
 * 描述：desc<br> 请求头封装
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：顾刘川<br>
 * 版本：1.0<br>
 * 创建日期：2020/6/18<br>
 */
@Component
public class YaoChuFaFeignInterceptor implements RequestInterceptor {

    @Value("${yaochufa.accessToken}")
    private String accessToken;
    @Value("${yaochufa.secret}")
    private String secret;
    @Value("${yaochufa.partnerId}")
    private String partnerId;
    @Value("${yaochufa.version}")
    private String version;

    @Value("${aa.aa}")
    private String ss;

    @PostConstruct
    void tt(){
        System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++" + ss);
    }
    @Override
    public void apply(RequestTemplate requestTemplate) {
        String timeStamp = String.valueOf(System.currentTimeMillis());
        String signed = null;
        try {
            signed = md5ByHash(accessToken + timeStamp + secret + new String(requestTemplate.body(),"utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        requestTemplate.header("Content-type", "application/json");
        requestTemplate.header("PartnerId", partnerId);
        requestTemplate.header("Access_Token", accessToken);
        requestTemplate.header("TimeStamp", timeStamp);
        requestTemplate.header("Signed", signed);
        requestTemplate.header("Version", version);
    }


    private static String md5ByHash(String dataStr) {
        try {
            MessageDigest m = MessageDigest.getInstance("MD5");
            m.update(dataStr.getBytes("UTF8"));
            byte s[] = m.digest();
            String result = "";
            for (int i = 0; i < s.length; i++) {
                result += Integer.toHexString((0x000000FF & s[i]) | 0xFFFFFF00).substring(6);
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
