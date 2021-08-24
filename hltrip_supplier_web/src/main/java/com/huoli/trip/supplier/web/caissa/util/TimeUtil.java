package com.huoli.trip.supplier.web.caissa.util;

import org.apache.commons.lang3.StringUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimeUtil {
    public static int daysBetween(Date smdate, Date bdate) {
        SimpleDateFormat sdf=new SimpleDateFormat("MM-dd");
        try {
            smdate=sdf.parse(sdf.format(smdate));
            bdate=sdf.parse(sdf.format(bdate));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(smdate);
        long time1 = cal.getTimeInMillis();
        cal.setTime(bdate);
        long time2 = cal.getTimeInMillis();
        long between_days=(time2-time1)/(1000*3600*24);

        return Integer.parseInt(String.valueOf(between_days));
    }

    public static Date getDateByStr(String str) {
        DateFormat format1 = new SimpleDateFormat("MM-dd");
        Date date = null;
        try {
            date = format1.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static Date add(Date date, int days) {
        Calendar calendar=Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, days);
        return calendar.getTime();
    }

    public static String splitStr(String str) {
        if (StringUtils.isNotEmpty(str)) {
            String[] s = str.split(" ")[0].split("-");
            return s[1] + "-" + s[2];
        } else {
            return "";
        }
    }

    public static void main(String[] args) {
        System.out.println(getDateByStr("2021-08-19 00:00:00"));
        System.out.println(daysBetween(new Date(), add(new Date(), 1)));
        System.out.println(daysBetween(getDateByStr("08-24"), getDateByStr("08-16")));
        System.out.println(daysBetween(getDateByStr("08-16"), getDateByStr(splitStr("2021-08-19 00:00:00"))));
    }
}
