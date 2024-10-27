package com.juggle.im.internal.model;

import java.util.Map;

public class CachedChatroom {
    public enum ChatroomStatus {
        UNKNOWN,
        JOINING,
        JOINED,
        FAILED,
        QUIT
    }

    public ChatroomStatus getStatus() {
        return mStatus;
    }

    public void setStatus(ChatroomStatus status) {
        mStatus = status;
    }

    public long getSyncTime() {
        return mSyncTime;
    }

    public void setSyncTime(long syncTime) {
        mSyncTime = syncTime;
    }

    public long getAttrSyncTime() {
        return mAttrSyncTime;
    }

    public void setAttrSyncTime(long attrSyncTime) {
        mAttrSyncTime = attrSyncTime;
    }

    public Map<String, String> getAttributes() {
        return mAttributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        mAttributes = attributes;
    }

    public int getPrevMessageCount() {
        return prevMessageCount;
    }

    public void setPrevMessageCount(int prevMessageCount) {
        this.prevMessageCount = prevMessageCount;
    }

    private ChatroomStatus mStatus;
    private long mSyncTime;
    private long mAttrSyncTime;
    private Map<String, String> mAttributes;
    private int prevMessageCount;
}
