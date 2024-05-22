package com.jet.im.log;

import android.content.Context;
import android.text.TextUtils;

import com.jet.im.internal.util.JLogger;

import java.io.File;

public class JLogManager implements IJLog {
    private static final String TAG = "JLogManager";
    private static final int DEFAULT_EXPIRED_TIME = 7 * 24 * 60 * 60 * 1000;
    private static final String DEFAULT_LOG_FILE_DIR = "jet_im/jlog";

    private IJLog mInternalJLog;
    private JLogConfig mJLogConfig;

    public static JLogManager getInstance() {
        return JLogManager.SingletonHolder.sInstance;
    }

    private static class SingletonHolder {
        static final JLogManager sInstance = new JLogManager();
    }

    private JLogManager() {
    }

    public JLogConfig getJLogConfig() {
        return mJLogConfig;
    }

    @Override
    public void setLogConfig(JLogConfig config) {
        if (config.getContext() == null) {
            throw new IllegalArgumentException("context is null");
        }
        if (config.getLogLevel() == null) {
            config.setLogLevel(JLogLevel.JLogLevelInfo);
        }
        if (config.getExpiredTime() <= 0) {
            config.setExpiredTime(DEFAULT_EXPIRED_TIME);
        }
        if (TextUtils.isEmpty(config.getLogFileDir())) {
            config.setLogFileDir(getDefaultJLogDir(config.getContext()));
        }
        createJLogDir(config.getLogFileDir());
        this.mJLogConfig = config;

        this.mInternalJLog = initInternalJLog("com.jet.im.log.JLog");
        if (mInternalJLog == null) return;
        mInternalJLog.setLogConfig(config);
    }

    @Override
    public void removeExpiredLogs() {
        if (mJLogConfig == null) return;
        if (mInternalJLog == null) return;
        mInternalJLog.removeExpiredLogs();
    }

    @Override
    public void uploadLog(long startTime, long endTime, IJLog.Callback callback) {
        if (mJLogConfig == null || mInternalJLog == null) {
            callback.onError(-1, "IJLog not initialized yet");
            return;
        }
        mInternalJLog.uploadLog(startTime, endTime, callback);
    }

    @Override
    public void write(JLogLevel level, String tag, String... keys) {
        if (mJLogConfig == null) return;
        if (mInternalJLog == null) return;
        mInternalJLog.write(level, tag, keys);
    }

    public boolean isDebugModel() {
        return mJLogConfig != null && mJLogConfig.isDebugMode();
    }

    private String getDefaultJLogDir(Context context) {
        File file = context.getFilesDir();
        String path = file.getAbsolutePath();
        path = String.format("%s/%s", path, DEFAULT_LOG_FILE_DIR);
        return path;
    }

    private void createJLogDir(String dirPath) {
        try {
            File file = new File(dirPath);
            if (!file.exists()) {
                if (!file.mkdirs()) {
                    JLogger.e("create jLog path fail, path = " + dirPath);
                }
            }
        } catch (Exception e) {
            JLogger.e("create jLog path fail, path= " + dirPath + ", e= " + e.getMessage());
        }
    }

    private IJLog initInternalJLog(String className) {
        try {
            Class<?> aClass = Class.forName(className);
            return (IJLog) aClass.newInstance();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            JLogger.d(TAG + ", not register " + className);
        }
        return null;
    }
}
