package com.juggle.im.model;

public class MomentComment {

    public String getCommentId() {
        return mCommentId;
    }

    public void setCommentId(String commentId) {
        mCommentId = commentId;
    }

    public String getMomentId() {
        return mMomentId;
    }

    public void setMomentId(String momentId) {
        mMomentId = momentId;
    }

    public String getParentCommentId() {
        return mParentCommentId;
    }

    public void setParentCommentId(String parentCommentId) {
        mParentCommentId = parentCommentId;
    }

    public String getContent() {
        return mContent;
    }

    public void setContent(String content) {
        mContent = content;
    }

    public UserInfo getUserInfo() {
        return mUserInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        mUserInfo = userInfo;
    }

    public UserInfo getParentUserInfo() {
        return mParentUserInfo;
    }

    public void setParentUserInfo(UserInfo parentUserInfo) {
        mParentUserInfo = parentUserInfo;
    }

    public long getCreateTime() {
        return mCreateTime;
    }

    public void setCreateTime(long createTime) {
        mCreateTime = createTime;
    }

    public long getUpdateTime() {
        return mUpdateTime;
    }

    public void setUpdateTime(long updateTime) {
        mUpdateTime = updateTime;
    }

    private String mCommentId;
    private String mMomentId;
    private String mParentCommentId;
    private String mContent;
    private UserInfo mUserInfo;
    private UserInfo mParentUserInfo;
    private long mCreateTime;
    private long mUpdateTime;
}
