package com.juggle.im.model;

import java.util.List;

public class GetMessageOptions {
    public long getStartTime() {
        return mStartTime;
    }

    public void setStartTime(long startTime) {
        mStartTime = startTime;
    }

    public int getCount() {
        return mCount;
    }

    public void setCount(int count) {
        mCount = count;
    }

    public List<String> getContentTypes() {
        return mContentTypes;
    }

    public void setContentTypes(List<String> contentTypes) {
        mContentTypes = contentTypes;
    }

    // 消息时间戳，传 0 或者不设置时，默认为当前时间
    long mStartTime;
    // 拉取数量，默认为 100 条，超过 100 时按 100 返回
    int mCount;
    // 拉取的消息类型列表，如果为空则拉取所有类型的消息
    List<String> mContentTypes;
}
