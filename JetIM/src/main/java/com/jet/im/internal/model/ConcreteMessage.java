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

    private long mMsgIndex;
    private String mClientUid;
}