package com.jet.im.internal.model;

import com.jet.im.model.ConversationInfo;

public class ConcreteConversationInfo extends ConversationInfo {
    public long getLastReadMessageIndex() {
        return lastReadMessageIndex;
    }

    public void setLastReadMessageIndex(long lastReadMessageIndex) {
        this.lastReadMessageIndex = lastReadMessageIndex;
    }

    private long lastReadMessageIndex;
}
