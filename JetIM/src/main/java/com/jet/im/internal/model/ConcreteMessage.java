package com.jet.im.internal.model;

import com.jet.im.model.GroupInfo;
import com.jet.im.model.Message;
import com.jet.im.model.UserInfo;

public class ConcreteMessage extends Message {
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

    public ConcreteMessage getReferMsg() {
        return mReferMsg;
    }

    public void setReferMsg(ConcreteMessage mReferMsg) {
        this.mReferMsg = mReferMsg;
    }

    private long mSeqNo;
    private long mMsgIndex;
    private String mClientUid;
    private int mFlags;
    private boolean mExisted;
    private GroupInfo mGroupInfo;
    private UserInfo mTargetUserInfo;
    private ConcreteMessage mReferMsg;
}
