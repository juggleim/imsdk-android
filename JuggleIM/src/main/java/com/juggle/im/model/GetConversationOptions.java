package com.juggle.im.model;

import com.juggle.im.JIMConst;

public class GetConversationOptions {
    public int[] getConversationTypes() {
        return mConversationTypes;
    }

    public void setConversationTypes(int[] conversationTypes) {
        mConversationTypes = conversationTypes;
    }

    public int getCount() {
        return mCount;
    }

    public void setCount(int count) {
        mCount = count;
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    public void setTimestamp(long timestamp) {
        mTimestamp = timestamp;
    }

    public JIMConst.PullDirection getPullDirection() {
        return mPullDirection;
    }

    public void setPullDirection(JIMConst.PullDirection pullDirection) {
        mPullDirection = pullDirection;
    }

    public String getTagId() {
        return mTagId;
    }

    public void setTagId(String tagId) {
        this.mTagId = tagId;
    }

    public boolean isIgnoreTop() {
        return mIgnoreTop;
    }

    public void setIgnoreTop(boolean ignoreTop) {
        mIgnoreTop = ignoreTop;
    }

    /// Conversation type list. Pass null to include all types.
    private int[] mConversationTypes;
    /// Pull count.
    private int mCount;
    /// Pull timestamp. 0 means the current time.
    private long mTimestamp;
    /// Pull direction.
    private JIMConst.PullDirection mPullDirection;
    /// Tag ID. Pass null to ignore tags.
    private String mTagId;
    /// true means fetch without top conversations.
    private boolean mIgnoreTop;
}
