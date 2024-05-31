package com.jet.im.internal.logger;

import android.content.Context;
import android.text.TextUtils;

import com.jet.im.internal.logger.action.ActionManager;
import com.jet.im.internal.util.JLogger;

import java.io.File;
import java.util.Arrays;

public class JLogManager implements IJLog {
    private static final String TAG = "JLogManager";
    private static final long DEFAULT_EXPIRED_TIME = 7 * 24 * 60 * 60 * 1000;//默认日志过期时间，7天
    private static final long DEFAULT_LOG_FILE_CREATE_INTERVAL = 60 * 60 * 1000;//默认新日志文件创建间隔，1小时
    private static final String DEFAULT_LOG_FILE_DIR = "jet_im/jlog";

    private JLogConfig mJLogConfig;
    private ActionManager mActionManager;

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
        if (config == null) {
            throw new IllegalArgumentException("log config is null");
        }
        if (config.getContext() == null) {
            throw new IllegalArgumentException("log config context is null");
        }
        if (config.getLogPrintLevel() == null) {
            config.setLogPrintLevel(JLogLevel.JLogLevelDebug);
        }
        if (config.getLogWriteLevel() == null) {
            config.setLogWriteLevel(JLogLevel.JLogLevelInfo);
        }
        if (config.getExpiredTime() <= 0) {
            config.setExpiredTime(DEFAULT_EXPIRED_TIME);
        }
        if (config.getLogFileCreateInterval() <= 0) {
            config.setLogFileCreateInterval(DEFAULT_LOG_FILE_CREATE_INTERVAL);
        }
        if (TextUtils.isEmpty(config.getLogFileDir())) {
            config.setLogFileDir(getDefaultJLogDir(config.getContext()));
        }
        createJLogDir(config.getLogFileDir());
        this.mJLogConfig = config;
        if (mActionManager == null) {
            mActionManager = ActionManager.instance(config);
        } else {
            mActionManager.setJLogConfig(config);
        }
    }

    @Override
    public void removeExpiredLogs() {
        if (mJLogConfig == null) return;
        if (mActionManager == null) return;
        mActionManager.addRemoveAction();
    }

    @Override
    public void uploadLog(long startTime, long endTime, IJLog.Callback callback) {
        if (mJLogConfig == null || mActionManager == null) {
            callback.onError(-1, "IJLog not initialized yet");
            return;
        }
        mActionManager.addUploadAction(startTime, endTime, callback);
    }

    @Override
    public void write(JLogLevel level, String tag, String... keys) {
        if (mJLogConfig == null) return;
        if (mActionManager == null) return;
        if (level == null || level.getCode() > mActionManager.getJLogConfig().getLogWriteLevel().getCode())
            return;
        if (keys == null || keys.length == 0) return;
        mActionManager.addWriteAction(level, tag, Arrays.asList(keys));
    }

    public boolean isDebugModel() {
        return mJLogConfig != null && mJLogConfig.isDebugMode();
    }

    public boolean canPrintConsole(JLogLevel printLevel) {
        return isDebugModel() && printLevel.getCode() <= mJLogConfig.getLogPrintLevel().getCode();
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
