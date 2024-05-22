package com.jet.im.log;

import android.content.Context;

/**
 * @author Ye_Guli
 * @create 2024-05-22 9:48
 */
public class JLogConfig {
    private Context mContext;
    private JLogLevel mLogLevel;
    private String mLogFileDir;
    private long mExpiredTime;
    private boolean mIsDebugMode;

    public JLogConfig(Builder builder) {
        this.mContext = builder.mContext;
        this.mLogLevel = builder.mLogLevel;
        this.mLogFileDir = builder.mLogFileDir;
        this.mExpiredTime = builder.mExpiredTime;
        this.mIsDebugMode = builder.mIsDebugMode;
    }

    public Context getContext() {
        return mContext;
    }

    public void setContext(Context context) {
        this.mContext = context;
    }

    public JLogLevel getLogLevel() {
        return mLogLevel;
    }

    public void setLogLevel(JLogLevel logLevel) {
        this.mLogLevel = logLevel;
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

    public boolean isDebugMode() {
        return mIsDebugMode;
    }

    public void setIsDebugMode(boolean isDebugMode) {
        this.mIsDebugMode = isDebugMode;
    }

    public static class Builder {
        private Context mContext;
        private JLogLevel mLogLevel;
        private String mLogFileDir;
        private long mExpiredTime;
        private boolean mIsDebugMode;

        public Builder(Context context) {
            this.mContext = context;
        }

        public Builder setLogLevel(JLogLevel logLevel) {
            this.mLogLevel = logLevel;
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

        public void setIsDebugMode(boolean isDebugMode) {
            this.mIsDebugMode = isDebugMode;
        }

        public JLogConfig build() {
            return new JLogConfig(this);
        }
    }
}