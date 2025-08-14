package com.juggle.im.model;

public class FavoriteMessage {
    public Message getMessage() {
        return mMessage;
    }

    public void setMessage(Message message) {
        mMessage = message;
    }

    public long getCreatedTime() {
        return mCreatedTime;
    }

    public void setCreatedTime(long createdTime) {
        mCreatedTime = createdTime;
    }

    private Message mMessage;
    //收藏时间
    private long mCreatedTime;
}
