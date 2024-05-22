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
        if (mJLogConfig == null || mJLogConfig.getContext() == null) return;

    }

    @Override
    public void uploadLog(long startTime, long endTime, Callback callback) {
        if (mJLogConfig == null || mJLogConfig.getContext() == null) return;
    }

    @Override
    public void write(JLogLevel level, String tag, String... keys) {
        if (mJLogConfig == null || mJLogConfig.getLogLevel() == null) return;
        if (level == null || level.getCode() > mJLogConfig.getLogLevel().getCode()) return;

        Log.v("JLog", "level= " + level.getName() + ", tag= " + tag + ", keys= " + Arrays.toString(keys));
    }
}