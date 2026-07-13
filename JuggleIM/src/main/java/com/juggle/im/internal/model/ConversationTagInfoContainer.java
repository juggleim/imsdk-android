package com.juggle.im.internal.model;

import com.juggle.im.model.ConversationTagInfo;

public class ConversationTagInfoContainer {
    public ConversationTagInfo getConversationTagInfo() {
        return mConversationTagInfo;
    }

    public void setConversationTagInfo(ConversationTagInfo conversationTagInfo) {
        mConversationTagInfo = conversationTagInfo;
    }

    public boolean isAdd() {
        return mIsAdd;
    }

    public void setAdd(boolean add) {
        mIsAdd = add;
    }

    private ConversationTagInfo mConversationTagInfo;
    private boolean mIsAdd = false;
}
