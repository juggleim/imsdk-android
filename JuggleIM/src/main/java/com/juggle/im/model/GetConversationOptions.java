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
        return tagId;
    }

    public void setTagId(String tagId) {
        this.tagId = tagId;
    }

    /// 会话类型列表，传空表示全部类型
    private int[] mConversationTypes;
    /// 拉取数量
    private int mCount;
    /// 拉取时间戳，0 表示当前时间
    private long mTimestamp;
    /// 拉取方向
    private JIMConst.PullDirection mPullDirection;
    /// 标签 id，传空表示不限标签
    private String tagId;
}
