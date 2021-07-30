package com.huoli.trip.supplier.web.caissa.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RgexUtil {

    public static List<String> getSubUtil(String soap, String rgex, int flag) {

        List<String> list = new ArrayList<String>();
        Pattern pattern = Pattern.compile(rgex);// 匹配的模式
        Matcher m = pattern.matcher(soap);
        while (m.find()) {
            list.add(m.group(flag));
        }
        return list;
    }

    public static String getNumber(String text) {
        String regEx="[^0-9]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(text);
        return m.replaceAll("");
    }

    public static String replaceBr(String source) {
        StringBuilder sb = new StringBuilder("");
        Matcher m = Pattern.compile("(?m)^.*$").matcher(source);  // (?m) 在这种模式下，'^'和'$'分别匹配一行的开始和结束  表示匹配整行
        while (m.find()) {
            sb.append(m.group()).append("<br>");
        }
        return String.valueOf(sb);
    }

    public static void main(String[] args) {
        System.out.println("a b".replaceAll(" ", "-"));
        String s = "<pre>ssss</pre>".replaceAll("<pre>", "").replaceAll("</pre>", "");
        System.out.println(s);
    }
}
