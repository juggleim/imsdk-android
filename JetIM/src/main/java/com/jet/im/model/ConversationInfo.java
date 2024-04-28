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

    public long getSortTime() {
        return mSortTime;
    }

    public void setSortTime(long sortTime) {
        mSortTime = sortTime;
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

    public boolean hasMentioned() {
        return mHasMentioned;
    }

    public void setHasMentioned(boolean hasMentioned) {
        mHasMentioned = hasMentioned;
    }

    private Conversation mConversation;
    private int mUnreadCount;
    private long mSortTime;
    private Message mLastMessage;
    private boolean mIsTop;
    private long mTopTime;
    private boolean mMute;
    private String mDraft;
    private boolean mHasMentioned;
}
