package com.huoli.trip.supplier.self.difengyun.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.huoli.trip.common.util.MD5Util;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2020/12/17<br>
 */
public class DfySignature {
    private static final String API_SIGNATURE_KEY = "sign";

    /**
     * 根据入参和密钥获取签名
     */
    public static String getSignature(JSONObject data, String secretKey) {

        // 第一步：获取并排序json数据
        //忽略签名
        data.remove("sign");

        //递归获取json结构中的键值对，组合键值并保存到列表中
        List<String> keyValueList = new ArrayList<String>();
        propertyFilter(null, data, keyValueList);

        //对列表进行排序，区分大小写
        Collections.sort(keyValueList);
        Object json = JSON.toJSON(keyValueList);
        System.out.println(json.toString());


        // 第二步：格式化数据，用&分割
        String formatText = StringUtils.join(keyValueList, "&");

        //在首尾加上秘钥，用&分割
        String finalText = secretKey + "&" + formatText + "&" + secretKey;

        // 第三步：MD5加密并转换成大写的16进制(finalText为utf-8编码)
        String md5 = MD5Util.encode(finalText).toUpperCase();


        return md5;
    }


    private static String byte2hex(byte[] bytes) {
        StringBuilder sign = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if (hex.length() == 1) {
                sign.append("0");
            }
            sign.append(hex.toUpperCase());
        }
        return sign.toString();
    }

    /**
     * PropertyPreFilter
     * 与jsonObjectPropertyFilter，jsonArrayPropertyFilter配合完成键值对的抽取组合
     */
    private static void propertyFilter(String key, Object value, List<String> list) {
        if (null == value) {
            return;
        }
        if (value instanceof JSONObject) {
            jsonObjectPropertyFilter(key, (JSONObject) value, list);
        } else if (value instanceof JSONArray) {
            jsonArrayPropertyFilter(key, (JSONArray) value, list);
        } else {
            if (value.toString().length() > 0) {
                list.add(key.trim() + "=" + value);
            }
        }
    }

    /**
     * jsonObjectPropertyFilter 过滤json对象
     */
    private static void jsonObjectPropertyFilter(String key, JSONObject value, List<String> list) {
        JSONObject jsonObject = value;
        if (jsonObject.isEmpty()) {
            return;
        }
        for (String str : jsonObject.keySet()) {
            propertyFilter(str, jsonObject.get(str), list);
        }
    }

    /**
     * jsonArrayPropertyFilter 过滤json数组
     */
    private static void jsonArrayPropertyFilter(String key, JSONArray value, List<String> list) {
        JSONArray jsonArray = value;
        if (jsonArray.isEmpty()) {
            return;
        }
        for (Object json : jsonArray) {
            propertyFilter(key, json, list);
        }
    }
}
