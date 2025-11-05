package com.juggle.im.internal.model;

import com.juggle.im.model.GroupMember;
import com.juggle.im.model.GroupInfo;
import com.juggle.im.model.Message;
import com.juggle.im.model.PushData;
import com.juggle.im.model.UserInfo;

public class ConcreteMessage extends Message {
    public ConcreteMessage() {
    }

    public ConcreteMessage(ConcreteMessage other) {
        super(other);
        mSeqNo = other.getSeqNo();
        mMsgIndex = other.getMsgIndex();
        mClientUid = other.getClientUid();
        mFlags = other.getFlags();
        mExisted = other.isExisted();
        mGroupInfo = other.getGroupInfo();
        mTargetUserInfo = other.getTargetUserInfo();
        mGroupMemberInfo = other.getGroupMemberInfo();
        mReferMsgId = other.getReferMsgId();
        mPushData = other.getPushData();
        mLifeTime = other.getLifeTime();
    }

    public long getSeqNo() {
        return mSeqNo;
    }

    public void setSeqNo(long seqNo) {
        mSeqNo = seqNo;
    }

    public long getMsgIndex() {
        return mMsgIndex;
    }

    public void setMsgIndex(long msgIndex) {
        mMsgIndex = msgIndex;
    }

    public String getClientUid() {
        return mClientUid;
    }

    public void setClientUid(String clientUid) {
        mClientUid = clientUid;
    }

    public int getFlags() {
        return mFlags;
    }

    public void setFlags(int flags) {
        this.mFlags = flags;
    }

    public boolean isExisted() {
        return mExisted;
    }

    public void setExisted(boolean existed) {
        mExisted = existed;
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

    public void setTargetUserInfo(UserInfo targetUserInfo) {
        mTargetUserInfo = targetUserInfo;
    }

    public GroupMember getGroupMemberInfo() {
        return mGroupMemberInfo;
    }

    public void setGroupMemberInfo(GroupMember groupMemberInfo) {
        mGroupMemberInfo = groupMemberInfo;
    }

    public String getReferMsgId() {
        return mReferMsgId;
    }

    public void setReferMsgId(String referMsgId) {
        this.mReferMsgId = referMsgId;
    }

    public PushData getPushData() {
        return mPushData;
    }

    public void setPushData(PushData pushData) {
        mPushData = pushData;
    }

    public long getLifeTime() {
        return mLifeTime;
    }

    public void setLifeTime(long lifeTime) {
        mLifeTime = lifeTime;
    }

    public long getReadTime() {
        return mReadTime;
    }

    public void setReadTime(long readTime) {
        mReadTime = readTime;
    }

    private long mSeqNo;
    private long mMsgIndex;
    private String mClientUid;
    private int mFlags;
    private boolean mExisted;
    private GroupInfo mGroupInfo;
    private UserInfo mTargetUserInfo;
    private GroupMember mGroupMemberInfo;
    private String mReferMsgId;
    private PushData mPushData;
    private long mLifeTime;
    private long mReadTime;
}
