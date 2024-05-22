package com.jet.im.log;

import android.util.Log;

import java.util.Arrays;

/**
 * @author Ye_Guli
 * @create 2024-05-22 17:01
 */
public class JLog implements IJLog {
    private JLogConfig mJLogConfig;

    @Override
    public void setLogConfig(JLogConfig config) {
        this.mJLogConfig = config;
    }

    @Override
    public void removeExpiredLogs() {

    }

    @Override
    public void uploadLog(long startTime, long endTime, Callback callback) {

    }

    @Override
    public void write(JLogLevel level, String tag, String... keys) {
        Log.v("JLog", "level= " + level.getCode() + ", tag= " + tag + ", keys= " + Arrays.toString(keys));
    }
}