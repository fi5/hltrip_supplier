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
        String a = "1.行程所示城市间飞机经济舱及火车硬卧（含机场建设税，燃油附加费）；\r\n2.行程中所标明的双人标准间酒店或同级住宿及早餐；\r\n3.行程中所标明的午、晚餐以中式餐食为主； \r\n4.游览时旅游用车，专业司机（如人数增加，根据人数调整车型）；\\r\\n5.拉萨当地全程游览中文导游； \\r\\n6.景点首道门票及行程所标注项目（特殊门票除外，行程中未标注的电瓶车或其他小交通费用均不包含）;\\r\\n7.羊湖旅拍电子版照片，每人不少于8张独立电子版照片（含1张精修电子版）；\\r\\n8.每人每天1瓶矿泉水，赠送每人全程1瓶便携式氧气；\\r\\n9.行程期间的国内旅游意外险；\n";
        System.out.println(a.replaceAll("\r\n", "\r\n<br>"));

    }
}
