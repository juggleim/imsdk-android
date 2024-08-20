package com.juggle.im.internal.logger.action;

import com.juggle.im.internal.logger.IJLog;

/**
 * @author Ye_Guli
 * @create 2024-05-23 9:40
 */
class UploadAction implements IAction {
    long mStartTime;
    long mEndTime;
    String mUploadLocalPath;
    IJLog.Callback mCallback;
    UploadRunnable mUploadRunnable;

    public UploadAction(Builder builder) {
        this.mStartTime = builder.mStartTime;
        this.mEndTime = builder.mEndTime;
        this.mCallback = builder.mCallback;
        this.mUploadRunnable = builder.mUploadRunnable;
    }

    @Override
    public boolean isValid() {
        if (mUploadRunnable == null) return false;
        if (mStartTime < 0 || mEndTime <= 0) return false;
        return true;
    }

    @Override
    @ActionTypeEnum.ActionType
    public int getType() {
        return ActionTypeEnum.TYPE_UPLOAD;
    }

    static class Builder {
        long mStartTime;
        long mEndTime;
        IJLog.Callback mCallback;
        UploadRunnable mUploadRunnable;

        public Builder setStartTime(long mStartTime) {
            this.mStartTime = mStartTime;
            return this;
        }

        public Builder setEndTime(long mEndTime) {
            this.mEndTime = mEndTime;
            return this;
        }

        public Builder setCallback(IJLog.Callback mCallback) {
            this.mCallback = mCallback;
            return this;
        }

        public Builder setUploadRunnable(UploadRunnable mUploadRunnable) {
            this.mUploadRunnable = mUploadRunnable;
            return this;
        }

        public UploadAction build() {
            return new UploadAction(this);
        }
    }
}
