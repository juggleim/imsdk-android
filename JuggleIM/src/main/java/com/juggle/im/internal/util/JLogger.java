package com.juggle.im.internal.util;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.juggle.im.internal.core.JIMCore;
import com.juggle.im.internal.logger.IJLog;
import com.juggle.im.internal.logger.JLogConfig;
import com.juggle.im.internal.logger.JLogLevel;
import com.juggle.im.internal.logger.action.ActionManager;

import java.io.File;
import java.util.Arrays;

public class JLogger implements IJLog {
    public static JLogger getInstance() {
        return JLogger.SingletonHolder.sInstance;
    }

    private static class SingletonHolder {
        static final JLogger sInstance = new JLogger();
    }

    private JLogger() {
    }

    private static final long DEFAULT_EXPIRED_TIME = 7 * 24 * 60 * 60 * 1000;//默认日志过期时间，7天
    private static final long DEFAULT_LOG_FILE_CREATE_INTERVAL = 60 * 60 * 1000;//默认新日志文件创建间隔，1小时
    private static final String TAG = "JLogger";
    private static final String DEFAULT_LOG_FILE_DIR = "jet_im/jlog";//默认日志保存目录

    private JLogConfig mJLogConfig;
    private ActionManager mActionManager;
    private JIMCore mCore;

    public static void e(String tag, String... msg) {
        String logTag = generateLogTag(tag);
        if (getInstance().canPrintConsole(JLogLevel.JLogLevelError)) {
            Log.e(logTag, generateLogContent(msg));
        }
        getInstance().write(JLogLevel.JLogLevelError, tag, msg);
    }

    public static void w(String tag, String... msg) {
        String logTag = generateLogTag(tag);
        if (getInstance().canPrintConsole(JLogLevel.JLogLevelWarning)) {
            Log.w(logTag, generateLogContent(msg));
        }
        getInstance().write(JLogLevel.JLogLevelWarning, tag, msg);
    }

    public static void i(String tag, String... msg) {
        String logTag = generateLogTag(tag);
        if (getInstance().canPrintConsole(JLogLevel.JLogLevelInfo)) {
            Log.i(logTag, generateLogContent(msg));
        }
        getInstance().write(JLogLevel.JLogLevelInfo, tag, msg);
    }

    public static void d(String tag, String... msg) {
        String logTag = generateLogTag(tag);
        if (getInstance().canPrintConsole(JLogLevel.JLogLevelDebug)) {
            Log.d(logTag, generateLogContent(msg));
        }
        getInstance().write(JLogLevel.JLogLevelDebug, tag, msg);
    }

    public static void v(String tag, String... msg) {
        String logTag = generateLogTag(tag);
        if (getInstance().canPrintConsole(JLogLevel.JLogLevelVerbose)) {
            Log.v(logTag, generateLogContent(msg));
        }
        getInstance().write(JLogLevel.JLogLevelVerbose, tag, msg);
    }

    //构造日志打印tag
    private static String generateLogTag(String tag) {
        return String.format("[%s:%s]", TAG, tag == null ? "" : tag);
    }

    //构造控制台日志打印内容
    private static String generateLogContent(String... msg) {
        if (msg == null) return "";
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < msg.length; i++) {
            builder.append(msg[i]);
            if (i < msg.length - 1) {
                builder.append(",");
            }
        }
        return builder.toString();
    }

    public void init(JLogConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("log config is null");
        }
        if (config.getContext() == null) {
            throw new IllegalArgumentException("log config context is null");
        }
        if (config.getLogConsoleLevel() == null) {
            config.setLogConsoleLevel(JLogLevel.JLogLevelNone);
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
            mActionManager = ActionManager.getInstance();
        }
        mActionManager.setJLogConfig(config);
    }

    @Override
    public void removeExpiredLogs() {
        if (mJLogConfig == null || mActionManager == null) return;
        mActionManager.addRemoveAction();
    }

    @Override
    public void uploadLog(String messageId, long startTime, long endTime, Callback callback) {
        if (mJLogConfig == null || mActionManager == null) {
            callback.onError(-1, "IJLog not initialized yet");
            return;
        }
        mActionManager.addUploadAction(mCore, messageId, startTime, endTime, callback);
    }

    @Override
    public void write(JLogLevel level, String tag, String... keys) {
        if (mJLogConfig == null || mActionManager == null) return;
        if (level == null || level.getCode() > mActionManager.getJLogConfig().getLogWriteLevel().getCode())
            return;
        if (TextUtils.isEmpty(tag)) return;
        if (keys == null || keys.length == 0) return;
        mActionManager.addWriteAction(level, tag, Arrays.asList(keys));
    }

    //检查当前是否允许在控制台打印日志
    private boolean canPrintConsole(JLogLevel printLevel) {
        return mJLogConfig != null && printLevel.getCode() <= mJLogConfig.getLogConsoleLevel().getCode();
    }

    //构造默认日志保存目录
    private String getDefaultJLogDir(Context context) {
        File file = context.getFilesDir();
        String path = file.getAbsolutePath();
        path = String.format("%s/%s", path, DEFAULT_LOG_FILE_DIR);
        return path;
    }

    //初始化日志目录
    private void createJLogDir(String dirPath) {
        try {
            File file = new File(dirPath);
            if (!file.exists()) {
                file.mkdirs();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setCore(JIMCore core) {
        mCore = core;
    }
}
