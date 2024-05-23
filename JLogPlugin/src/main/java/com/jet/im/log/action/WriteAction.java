package com.jet.im.log.action;

import com.jet.im.log.JLogLevel;

import java.util.List;

/**
 * @author Ye_Guli
 * @create 2024-05-23 9:40
 */
class WriteAction implements IAction {
    JLogLevel mLevel;
    String mTag;
    List<String> mLogs;
    long mLogTime;

    String mThreadInfo;
    boolean mIsMainThread;

    public WriteAction(Builder builder) {
        this.mLevel = builder.mLevel;
        this.mTag = builder.mTag;
        this.mLogs = builder.mLogs;
        this.mLogTime = builder.mLogTime;
        this.mThreadInfo = builder.mThreadInfo;
        this.mIsMainThread = builder.mIsMainThread;
    }

    @Override
    public boolean isValid() {
        if (mLevel == null) return false;
        if (mTag == null) return false;
        if (mLogs == null || mLogs.isEmpty()) return false;
        return true;
    }

    @Override
    @ActionTypeEnum.ActionType
    public int getType() {
        return ActionTypeEnum.TYPE_WRITE;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"levelCode\": \"").append(mLevel.getCode()).append("\",");
        sb.append("\"levelName\": \"").append(mLevel.getName()).append("\",");
        sb.append("\"tag\": \"").append(mTag).append("\",");
        sb.append("\"keys\": [");
        for (int i = 0; i < mLogs.size(); i++) {
            sb.append("\"").append(mLogs.get(i)).append("\"");
            if (i < mLogs.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("],");
        sb.append("\"logTime\": ").append(mLogTime).append(",");
        sb.append("\"threadInfo\": \"").append(mThreadInfo).append("\",");
        sb.append("\"isMainThread\": ").append(mIsMainThread);
        sb.append("}");
        return sb.toString();
    }

    static class Builder {
        private JLogLevel mLevel;
        private String mTag;
        private List<String> mLogs;
        private long mLogTime;
        private String mThreadInfo;
        private boolean mIsMainThread;

        public Builder setLevel(JLogLevel mLevel) {
            this.mLevel = mLevel;
            return this;
        }

        public Builder setTag(String mTag) {
            this.mTag = mTag;
            return this;
        }

        public Builder setLogs(List<String> mLogs) {
            this.mLogs = mLogs;
            return this;
        }

        public Builder setLogTime(long mLogTime) {
            this.mLogTime = mLogTime;
            return this;
        }

        public Builder setThreadInfo(String mThreadInfo) {
            this.mThreadInfo = mThreadInfo;
            return this;
        }

        public Builder setIsMainThread(boolean mIsMainThread) {
            this.mIsMainThread = mIsMainThread;
            return this;
        }

        public WriteAction build() {
            return new WriteAction(this);
        }
    }
}
