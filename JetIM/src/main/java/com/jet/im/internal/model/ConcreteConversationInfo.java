package com.jet.im.internal.model;

import com.jet.im.model.ConversationInfo;
import com.jet.im.model.GroupInfo;
import com.jet.im.model.UserInfo;

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

    public GroupInfo getGroupInfo() {
        return mGroupInfo;
    }

    public void setGroupInfo(GroupInfo groupInfo) {
        mGroupInfo = groupInfo;
    }

    public UserInfo getTargetUserInfo() {
        return mTargetUserInfo;
    }

    public void setTargetUserInfo(UserInfo userInfo) {
        mTargetUserInfo = userInfo;
    }

    private long mLastReadMessageIndex;
    private long mSyncTime;
    private GroupInfo mGroupInfo;
    private UserInfo mTargetUserInfo;
}
