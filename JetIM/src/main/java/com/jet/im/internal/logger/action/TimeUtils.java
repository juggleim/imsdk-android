package com.jet.im.internal.logger.action;

import java.util.Calendar;

/**
 * @author Ye_Guli
 * @create 2024-05-23 13:49
 */
class TimeUtils {

    //判断当前时间是否位于指定的小时范围内
    static boolean needCreateLogFile(long currentHour, long createInterval) {
        long currentTime = System.currentTimeMillis();
        return currentHour < currentTime && currentHour + createInterval > currentTime;
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