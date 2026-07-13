package com.juggle.im.model;

public class GetFavoriteMessageOption {
    public int getCount() {
        return mCount;
    }

    public void setCount(int count) {
        mCount = count;
    }

    public String getOffset() {
        return mOffset;
    }

    public void setOffset(String offset) {
        mOffset = offset;
    }

    // Query start position. Pass null for the first request; later requests can use the next offset from the success callback.
    private String mOffset;
    // Query count.
    private int mCount;
}
