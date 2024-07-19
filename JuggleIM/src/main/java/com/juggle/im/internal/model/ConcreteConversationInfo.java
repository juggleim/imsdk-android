package com.juggle.im.internal.model;

import com.juggle.im.model.ConversationInfo;
import com.juggle.im.model.GroupInfo;
import com.juggle.im.model.UserInfo;

import java.util.List;

public class ConcreteConversationInfo extends ConversationInfo {
    public long getLastReadMessageIndex() {
        return mLastReadMessageIndex;
    }

    public void setLastReadMessageIndex(long lastReadMessageIndex) {
        this.mLastReadMessageIndex = lastReadMessageIndex;
    }

    public long getLastMessageIndex() {
        return mLastMessageIndex;
    }

    public void setLastMessageIndex(long lastMessageIndex) {
        mLastMessageIndex = lastMessageIndex;
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

    public List<UserInfo> getMentionUserList() {
        return mMentionUserList;
    }

    public void setMentionUserList(List<UserInfo> mentionUserList) {
        this.mMentionUserList = mentionUserList;
    }

    private long mLastReadMessageIndex;
    private long mLastMessageIndex;
    private long mSyncTime;
    private GroupInfo mGroupInfo;
    private UserInfo mTargetUserInfo;
    private List<UserInfo> mMentionUserList;
}
