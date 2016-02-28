package com.lulee007.mocklocations.util;

import com.orhanobut.logger.Logger;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * User: lulee007@live.com
 * Date: 2015-12-13
 * Time: 20:03
 */
public class DateUtil {

    public static String upToNow(String dateTimeStr){
        try {
            String strDate = dateTimeStr.replace("T", " ").substring(0, 19);

            Date date = strToDateLong(strDate);
            Date current = new Date();
            long between = current.getTime() - date.getTime();
            if (between < 60 * 1000) {
                return "一分钟前";
            } else if (between < 60L * 1000 * 60) {
                return String.format("%d分钟前", (int) (1.0 * between / (60 * 1000) + 0.5));
            } else if (between < 60L * 1000 * 60 * 24) {
                return String.format("%d小时前", (int) (1.0 * between / (60 * 1000 * 60) + 0.5));

            } else if (between < 60L * 1000 * 60 * 24 * 7) {
                return String.format("%d天前", (int) (1.0 * between / (60 * 1000 * 60 * 24) + 0.5));

            } else if (between < 60L * 1000 * 60 * 24 * 30) {
                return String.format("%d周前", (int) (1.0 * between / (60L * 1000 * 60 * 24 * 7) + 0.5));

            } else if (between < 60L * 1000 * 60 * 24 * 365) {
                return String.format("%d月前", (int) ((1.0 * between) / (60L * 1000 * 60 * 24 * 30) + 0.5));

            } else {
                return String.format("%d年前", (int) ((1.0 * between) / (60D * 1000 * 60 * 24 * 365) + 0.5));
            }
        }catch (Exception ex){
            Logger.e(ex, "DateUtil.upToNow error");
        }
        return "";

    }

    /**
     * 将长时间格式字符串 yyyy-MM-dd HH:mm:ss 转换为时间
     * @param strDate
     * @return
     */
    public static Date strToDateLong(String strDate) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        ParsePosition pos = new ParsePosition(0);
        Date strtodate = formatter.parse(strDate, pos);
        return strtodate;
    }

    /**
     * 时间转为字符串 yyyy-MM-dd HH:mm:ss
     * @param date
     * @return 'yyyy-MM-dd HH:mm:ss'
     */
    public static String dateToStr(Date date){
        SimpleDateFormat formatter = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.CHINESE);
        Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
        return formatter.format(curDate);
    }

    /**
     * 当前时间转为字符串 yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static String currentDateToStr(){
        return dateToStr(new Date());
    }
}
