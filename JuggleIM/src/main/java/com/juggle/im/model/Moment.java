package com.juggle.im.model;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Moment {
    public static Moment fromJson(JSONObject json) {
        if (json == null || json.length() == 0) {
            return null;
        }

        Moment moment = new Moment();

        moment.mMomentId = json.optString("moment_id");
        JSONObject contentJson = json.optJSONObject("content");
        if (contentJson != null) {
            moment.mContent = contentJson.optString("text");

            JSONArray mediasJsonArray = contentJson.optJSONArray("medias");
            if (mediasJsonArray != null && mediasJsonArray.length() > 0) {
                List<MomentMedia> mediaList = new ArrayList<>(mediasJsonArray.length());
                for (int i = 0; i < mediasJsonArray.length(); i++) {
                    JSONObject mediaJson = mediasJsonArray.optJSONObject(i);
                    MomentMedia media = MomentMedia.fromJson(mediaJson);
                    if (media != null) {
                        mediaList.add(media);
                    }
                }
                moment.mMediaList = mediaList;
            }
        }

        JSONObject userInfoJson = json.optJSONObject("user_info");
        moment.mUserInfo = UserInfo.fromJson(userInfoJson);

        JSONArray reactionsJsonArray = json.optJSONArray("reactions");
        if (reactionsJsonArray != null && reactionsJsonArray.length() > 0) {
            moment.mReactionList = MomentReaction.mergeReactionListWithJson(reactionsJsonArray);
        }

        JSONArray commentsJsonArray = json.optJSONArray("top_comments");
        if (commentsJsonArray != null && commentsJsonArray.length() > 0) {
            List<MomentComment> commentList = new ArrayList<>(commentsJsonArray.length());
            for (int i = 0; i < commentsJsonArray.length(); i++) {
                JSONObject commentJson = commentsJsonArray.optJSONObject(i);
                MomentComment comment = MomentComment.fromJson(commentJson);
                if (comment != null) {
                    commentList.add(comment);
                }
            }
            moment.mCommentList = commentList;
        }

        moment.mCreateTime = json.optLong("moment_time");
        return moment;
    }


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

    private String mMomentId;
    private String mContent;
    private List<MomentMedia> mMediaList;
    private UserInfo mUserInfo;
    private List<MomentReaction> mReactionList;
    private List<MomentComment> mCommentList;
    private long mCreateTime;
}
