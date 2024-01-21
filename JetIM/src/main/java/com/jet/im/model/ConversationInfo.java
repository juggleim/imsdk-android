package com.jet.im.model;

public class ConversationInfo {
    public Conversation getConversation() {
        return mConversation;
    }

    public void setConversation(Conversation conversation) {
        mConversation = conversation;
    }

    public int getUnreadCount() {
        return mUnreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        mUnreadCount = unreadCount;
    }

    public long getUpdateTime() {
        return mUpdateTime;
    }

    public void setUpdateTime(long updateTime) {
        mUpdateTime = updateTime;
    }

    public Message getLastMessage() {
        return mLastMessage;
    }

    public void setLastMessage(Message lastMessage) {
        mLastMessage = lastMessage;
    }

    public boolean isTop() {
        return mIsTop;
    }

    public void setTop(boolean top) {
        mIsTop = top;
    }

    public long getTopTime() {
        return mTopTime;
    }

    public void setTopTime(long topTime) {
        mTopTime = topTime;
    }

    public boolean isMute() {
        return mMute;
    }

    public void setMute(boolean mute) {
        mMute = mute;
    }

    public String getDraft() {
        return mDraft;
    }

    public void setDraft(String draft) {
        mDraft = draft;
    }

    private Conversation mConversation;
    private int mUnreadCount;
    private long mUpdateTime;
    private Message mLastMessage;
    private boolean mIsTop;
    private long mTopTime;
    private boolean mMute;
    private String mDraft;
}
