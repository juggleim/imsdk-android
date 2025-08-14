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

    //查询的起始位置，第一次可以传空，后续可以从成功回调里获取下一次的 offset
    private String mOffset;
    //查询数量
    private int mCount;
}
