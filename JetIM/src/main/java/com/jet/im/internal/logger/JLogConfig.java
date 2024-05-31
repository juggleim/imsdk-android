package com.jet.im.internal.logger;

import android.content.Context;

/**
 * @author Ye_Guli
 * @create 2024-05-22 9:48
 */
public class JLogConfig {
    private Context mContext;
    private JLogLevel mLogPrintLevel;
    private JLogLevel mLogWriteLevel;
    private String mLogFileDir;
    private long mExpiredTime;
    private long mLogFileCreateInterval;
    private boolean mIsDebugMode;

    public JLogConfig(Builder builder) {
        this.mContext = builder.mContext;
        this.mLogPrintLevel = builder.mLogPrintLevel;
        this.mLogWriteLevel = builder.mLogWriteLevel;
        this.mLogFileDir = builder.mLogFileDir;
        this.mExpiredTime = builder.mExpiredTime;
        this.mLogFileCreateInterval = builder.mLogFileCreateInterval;
        this.mIsDebugMode = builder.mIsDebugMode;
    }

    public Context getContext() {
        return mContext;
    }

    public void setContext(Context context) {
        this.mContext = context;
    }

    public JLogLevel getLogPrintLevel() {
        return mLogPrintLevel;
    }

    public void setLogPrintLevel(JLogLevel logPrintLevel) {
        this.mLogPrintLevel = logPrintLevel;
    }

    public JLogLevel getLogWriteLevel() {
        return mLogWriteLevel;
    }

    public void setLogWriteLevel(JLogLevel logWriteLevel) {
        this.mLogWriteLevel = logWriteLevel;
    }

    public String getLogFileDir() {
        return mLogFileDir;
    }

    public void setLogFileDir(String logFileDir) {
        this.mLogFileDir = logFileDir;
    }

    public long getExpiredTime() {
        return mExpiredTime;
    }

    public void setExpiredTime(long expiredTime) {
        this.mExpiredTime = expiredTime;
    }

    public long getLogFileCreateInterval() {
        return mLogFileCreateInterval;
    }

    public void setLogFileCreateInterval(long logFileCreateInterval) {
        this.mLogFileCreateInterval = logFileCreateInterval;
    }

    public boolean isDebugMode() {
        return mIsDebugMode;
    }

    public void setIsDebugMode(boolean isDebugMode) {
        this.mIsDebugMode = isDebugMode;
    }

    public static class Builder {
        private Context mContext;
        private JLogLevel mLogPrintLevel;
        private JLogLevel mLogWriteLevel;
        private String mLogFileDir;
        private long mExpiredTime;
        private long mLogFileCreateInterval;
        private boolean mIsDebugMode;

        public Builder(Context context) {
            this.mContext = context;
        }

        public Builder setLogPrintLevel(JLogLevel logLevel) {
            this.mLogPrintLevel = logLevel;
            return this;
        }

        public Builder setLogWriteLevel(JLogLevel logLevel) {
            this.mLogWriteLevel = logLevel;
            return this;
        }

        public Builder setLogFileDir(String logFileDir) {
            this.mLogFileDir = logFileDir;
            return this;
        }

        public Builder setExpiredInterval(long expiredTime) {
            this.mExpiredTime = expiredTime;
            return this;
        }

        public Builder setLogFileCreateInterval(long logFileCreateInterval) {
            this.mLogFileCreateInterval = logFileCreateInterval;
            return this;
        }

        public Builder setIsDebugMode(boolean isDebugMode) {
            this.mIsDebugMode = isDebugMode;
            return this;
        }

        public JLogConfig build() {
            return new JLogConfig(this);
        }
    }
}