package com.huoli.trip.supplier.web.caissa.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class RedisQueue {
    // 这是单例
    private static RedisTemplate<String, String> stringJedisTemplate;

    @Autowired
    public RedisQueue(StringRedisTemplate _stringJedisTemplate) {
        stringJedisTemplate = _stringJedisTemplate;
    }


    //将未访问的url加入到toVisit表中(使用的是尾插法)
    public static void addForSet(String key, String url) {
        stringJedisTemplate.opsForSet().add(key, url);
    }

    //将未访问的url弹出进行解析
    public static String popForSet(String key) {
        return stringJedisTemplate.opsForSet().pop(key);
    }

    //将已经解析过的url添加到已访问队列中

    //判断待访问url队列是否为空
    public static boolean isEmpty(String key) {
        Long length = stringJedisTemplate.opsForSet().size(key);
        if (length == null) {
            return true;
        }
        return length == 0;
    }

    public static List<String> getSubUtil(String soap, String rgex, int flag) {

        List<String> list = new ArrayList<String>();
        Pattern pattern = Pattern.compile(rgex);// 匹配的模式
        Matcher m = pattern.matcher(soap);
        while (m.find()) {
            list.add(m.group(flag));
        }
        return list;
    }

    public static List<String> getNumber(String text) {
        List<String> list = new ArrayList<String>();
        String regEx="[^0-9]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(text);

        String[] split = m.replaceAll(",").split(",");
        for (String s : split) {
            list.add(s);
        }

        return list;
    }



    public static void main(String[] args) {
        String st = "双方一致确认，由于旅行社为推出此次旅游产品，已与履行辅助人（包括航空公司、船方、酒店、房车公司及旅游景点等）签订了相关合同并按合同约定支付了相应费用且该等费用根据合同约定不可以要求退还，如旅游者取消订单将给旅行社造成严重损失；因此，双方一致同意，如旅游者在预订旅游产品后取消订单的，旅行社有权按照以下标准扣除取消费用：1)凡报名后至出发前15日取消，即收取订单总费用的20%2)距出发前7日，收取订单总费用的50％\n" +
                "3)距出发前3日，收取订单总费用的80％；\n" +
                "4)距出发前1日，收取订单总费用的100％；\n" +
                "2. 如按上述约定比例扣除的必要的费用低于实际发生的费用，旅游者按照实际发生的费用支付，不超过旅游费用总额；\n" +
                "3. 旅游者确认：旅行社已详细讲解产品退、改规定，本人完全理解并接受本协议的约定，不再具有异议，并同意在发生退改行为时，无条件按照本协议执行。";
        String re = "出发前(.*?)％；";

        String str = "abc3443abcfgjhgabcgfjabc";
        String rgex = "abc(.*?)abc";
        List<String> subUtil = getSubUtil(st, re, 0);
        System.out.println(subUtil);
        for (String a : subUtil) {
            System.out.println(getSubUtil(a, "前(.*?)日",1));
            System.out.println(getSubUtil(a, "的(.*?)％", 1));
        }

    }

    
}