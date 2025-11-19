package com.juggle.im.model;

import java.util.List;

public class Moment {
    public String getMomentId() {
        return mMomentId;
    }

    public void setMomentId(String momentId) {
        mMomentId = momentId;
    }

    public String getContent() {
        return mContent;
    }

    public void setContent(String content) {
        mContent = content;
    }

    public List<MomentMedia> getMediaList() {
        return mMediaList;
    }

    public void setMediaList(List<MomentMedia> mediaList) {
        mMediaList = mediaList;
    }

    public UserInfo getUserInfo() {
        return mUserInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        mUserInfo = userInfo;
    }

    public List<MomentReaction> getReactionList() {
        return mReactionList;
    }

    public void setReactionList(List<MomentReaction> reactionList) {
        mReactionList = reactionList;
    }

    public List<MomentComment> getCommentList() {
        return mCommentList;
    }

    public void setCommentList(List<MomentComment> commentList) {
        mCommentList = commentList;
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

    private String mMomentId;
    private String mContent;
    private List<MomentMedia> mMediaList;
    private UserInfo mUserInfo;
    private List<MomentReaction> mReactionList;
    private List<MomentComment> mCommentList;
    private long mCreateTime;
    private long mUpdateTime;
}
