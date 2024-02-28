package com.jet.im.internal.model;

import com.jet.im.model.ConversationInfo;

public class ConcreteConversationInfo extends ConversationInfo {
    public long getLastReadMessageIndex() {
        return mLastReadMessageIndex;
    }

    public void setLastReadMessageIndex(long lastReadMessageIndex) {
        this.mLastReadMessageIndex = lastReadMessageIndex;
    }

    public long getSyncTime() {
        return mSyncTime;
    }

    public void setSyncTime(long syncTime) {
        mSyncTime = syncTime;
    }

    private long mLastReadMessageIndex;
    private long mSyncTime;
}
