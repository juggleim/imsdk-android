package com.jet.im.kit.utils;

import android.content.Context;

import androidx.annotation.NonNull;

import com.jet.im.kit.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * A class with static util methods.
 */

@SuppressWarnings("unused")
public class DateUtils {

    // This class should not be initialized
    private DateUtils() {

    }

    @NonNull
    public static String formatTime(@NonNull Context context, long timeInMillis) {
        int flags = android.text.format.DateUtils.FORMAT_SHOW_TIME;
        return android.text.format.DateUtils.formatDateTime(context, timeInMillis, flags);
    }

    /**
     * If the given time is of a different date, display the date.
     * If it is of the same date, display the time.
     * @param timeInMillis  The time to convert, in milliseconds.
     * @return The time or date.
     */
    @NonNull
    public static String formatDateTime(@NonNull Context context, long timeInMillis) {
        if (isToday(timeInMillis)) {
            return formatTime(context, timeInMillis);
        } else if (isYesterday(timeInMillis)) {
            return context.getString(R.string.sb_text_yesterday);
        } else if (isThisYear(timeInMillis)) {
            return formatDate2(timeInMillis);
        } else {
            return formatDate3(timeInMillis);
        }
    }

    /**
     * Formats timestamp to 'date month' format (e.g. 'Wed, 19 Dec').
     */
    @NonNull
    public static String formatDate(long timeInMillis) {
        int flags = android.text.format.DateUtils.FORMAT_NO_YEAR
            | android.text.format.DateUtils.FORMAT_ABBREV_MONTH
            | android.text.format.DateUtils.FORMAT_ABBREV_WEEKDAY
            | android.text.format.DateUtils.FORMAT_SHOW_WEEKDAY
            | android.text.format.DateUtils.FORMAT_SHOW_DATE;
        return android.text.format.DateUtils.formatDateTime(null, timeInMillis, flags);
    }

    /**
     * Formats timestamp to 'date month' format (e.g. '19 Dec').
     */
    @NonNull
    public static String formatDate2(long timeInMillis) {
        int flags = android.text.format.DateUtils.FORMAT_NO_YEAR
            | android.text.format.DateUtils.FORMAT_ABBREV_MONTH
            | android.text.format.DateUtils.FORMAT_SHOW_DATE;
        return android.text.format.DateUtils.formatDateTime(null, timeInMillis, flags);
    }

    /**
     * Formats timestamp to 'month/date/year' format (e.g. '12/19/2022').
     */
    @NonNull
    public static String formatDate3(long timeInMillis) {
        int flags = android.text.format.DateUtils.FORMAT_SHOW_YEAR
            | android.text.format.DateUtils.FORMAT_SHOW_DATE
            | android.text.format.DateUtils.FORMAT_NUMERIC_DATE;
        return android.text.format.DateUtils.formatDateTime(null, timeInMillis, flags);
    }

    /**
     * Formats timestamp to 'date month, year' format (e.g. '19 Dec, 2022').
     */
    @NonNull
    public static String formatDate4(long timeInMillis) {
        int flags = android.text.format.DateUtils.FORMAT_SHOW_YEAR
            | android.text.format.DateUtils.FORMAT_ABBREV_MONTH
            | android.text.format.DateUtils.FORMAT_SHOW_DATE;
        return android.text.format.DateUtils.formatDateTime(null, timeInMillis, flags);
    }

    @NonNull
    public static String formatTimelineMessage(long timeInMillis) {
        if (isThisYear(timeInMillis)) {
            return formatDate(timeInMillis);
        } else {
            return formatDate4(timeInMillis);
        }
    }

    /**
     * Returns whether the given date is today, based on the user's current locale.
     */
    public static boolean isToday(long timeInMillis) {
        return android.text.format.DateUtils.isToday(timeInMillis);
    }

    public static boolean isYesterday(long timeInMillis) {
        Calendar now = Calendar.getInstance();
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(timeInMillis);

        now.add(Calendar.DATE, -1);

        return now.get(Calendar.YEAR) == date.get(Calendar.YEAR)
            && now.get(Calendar.MONTH) == date.get(Calendar.MONTH)
            && now.get(Calendar.DATE) == date.get(Calendar.DATE);
    }

    public static boolean isThisYear(long timeInMillis) {
        Calendar now = Calendar.getInstance();
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(timeInMillis);
        return now.get(Calendar.YEAR) == date.get(Calendar.YEAR);
    }

    @NonNull
    public static String getDateString(long dateMillis) {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        return dateFormat.format(dateMillis);
    }

    /**
     * Checks if two dates are of the same day.
     * @param millisFirst   The time in milliseconds of the first date.
     * @param millisSecond  The time in milliseconds of the second date.
     * @return Whether {@param millisFirst} and {@param millisSecond} are off the same day.
     */
    public static boolean hasSameDate(long millisFirst, long millisSecond) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        return dateFormat.format(millisFirst).equals(dateFormat.format(millisSecond));
    }

    /**
     * Checks if two dates are of the same time up to the minute.
     *
     * @param millisFirst   The time in milliseconds of the first date.
     * @param millisSecond  The time in milliseconds of the second date.
     * @return Whether {@param millisFirst} and {@param millisSecond} are off the same day.
     * since 1.2.1
     */
    public static boolean hasSameTimeInMinute(long millisFirst, long millisSecond) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        return dateFormat.format(millisFirst).equals(dateFormat.format(millisSecond));
    }

    /**
     * Get time the difference.
     * Date shows 'date month' format (e.g. '19 Dec').
     * Minimum resolution is 1 minute.
     */
    @NonNull
    public static String getTimeDiff(long time) {
        int flags = android.text.format.DateUtils.FORMAT_NO_YEAR
            | android.text.format.DateUtils.FORMAT_ABBREV_MONTH
            | android.text.format.DateUtils.FORMAT_SHOW_DATE;
        return (String) android.text.format.DateUtils.getRelativeTimeSpanString(time, System.currentTimeMillis(), 60000, flags);
    }
}
