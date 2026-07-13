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

    // Message timestamp. Defaults to the current time when set to 0 or left unset.
    long mStartTime;
    // Pull count. Defaults to 100; values greater than 100 return 100 messages.
    int mCount;
    // Message content type list to pull. If empty, all message types are pulled.
    List<String> mContentTypes;
}
