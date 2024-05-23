package com.jet.im.log.action;

import java.util.Calendar;

/**
 * @author Ye_Guli
 * @create 2024-05-23 13:49
 */
class TimeUtils {

    //判断当前时间是否位于指定的小时范围内
    static boolean isSameHour(long currentHour) {
        long currentTime = System.currentTimeMillis();
        return currentHour < currentTime && currentHour + Constants.HOUR > currentTime;
    }

    //获取当前时间所在的整小时时间戳
    static long getCurrentHour() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }
}