package com.huoli.trip.supplier.self.util;

import org.apache.commons.lang3.StringUtils;

/**
 * 描述：<br/>
 * 版权：Copyright (c) 2011-2020<br>
 * 公司：活力天汇<br>
 * 作者：冯志强<br>
 * 版本：1.0<br>
 * 创建日期：2021/2/3<br>
 */
public class CommonUtil {

    /**
     * 把"市"字去掉
     * @param city
     * @return
     */
    public static String getCity(String city){
        if(StringUtils.isNotBlank(city) && city.endsWith("市")){
            return city.substring(0, city.length() - 1);
        }
        return city;
    }
}
