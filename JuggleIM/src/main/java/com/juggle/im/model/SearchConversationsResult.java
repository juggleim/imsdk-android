package com.juggle.im.model;

public class SearchConversationsResult {
    public int getMatchedCount() {
        return mMatchedCount;
    }

    public void setMatchedCount(int matchedCount) {
        this.mMatchedCount = matchedCount;
    }

    public ConversationInfo getConversationInfo() {
        return mConversationInfo;
    }

    public void setConversationInfo(ConversationInfo conversationInfo) {
        mConversationInfo = conversationInfo;
    }

    private ConversationInfo mConversationInfo;
    private int mMatchedCount;
}
