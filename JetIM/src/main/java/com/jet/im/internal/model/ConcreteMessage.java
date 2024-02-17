package com.jet.im.internal.model;

import com.jet.im.model.Message;

public class ConcreteMessage extends Message {
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

    private long mMsgIndex;
    private String mClientUid;
    private int mFlags;
    private boolean mExisted;
}
