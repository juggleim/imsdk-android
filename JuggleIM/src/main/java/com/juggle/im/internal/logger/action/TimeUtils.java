package com.juggle.im.internal.logger.action;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * @author Ye_Guli
 * @create 2024-05-23 13:49
 */
class TimeUtils {

    //Check whether the current time is within the specified hour range
    static boolean needCreateLogFile(long currentHour, long createInterval) {
        long currentTime = System.currentTimeMillis();
        return currentHour < currentTime && currentHour + createInterval > currentTime;
    }

    //Get the hour timestamp for the current time
    static long getCurrentHour() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    static String formatTimeSpanDetailed(long timeSpan){
        SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.LOG_TIMESTAMP_FORMAT_DETAILED, Locale.US);
        return dateFormat.format(new Date(timeSpan));
    }
}