package com.juggle.im.model;

import org.json.JSONException;
import org.json.JSONObject;

public class MomentComment {
    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        if (mCommentId != null) {
            json.put("comment_id", mCommentId);
        }
        if (mMomentId != null) {
            json.put("moment_id", mMomentId);
        }
        if (mParentCommentId != null) {
            json.put("parent_comment_id", mParentCommentId);
        }
        JSONObject contentJson = new JSONObject();
        if (mContent != null) {
            contentJson.put("text", mContent);
        }
        json.put("content", contentJson);
        if (mUserInfo != null) {
            json.put("user_info", mUserInfo.toJson());
        }
        if (mParentUserInfo != null) {
            json.put("parent_user_info", mParentUserInfo.toJson());
        }
        json.put("comment_time", mCreateTime);
        return json;
    }

    public static MomentComment fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }
        MomentComment comment = new MomentComment();
        comment.mCommentId = json.optString("comment_id");
        comment.mMomentId = json.optString("moment_id");
        comment.mParentCommentId = json.optString("parent_comment_id");
        JSONObject contentJson = json.optJSONObject("content");
        if (contentJson != null) {
            comment.mContent = contentJson.optString("text");
        }
        JSONObject parentUserJson = json.optJSONObject("parent_user_info");
        comment.mParentUserInfo = UserInfo.fromJson(parentUserJson);
        JSONObject userJson = json.optJSONObject("user_info");
        comment.mUserInfo = UserInfo.fromJson(userJson);
        comment.mCreateTime = json.optLong("comment_time");
        return comment;
    }

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

    private String mCommentId;
    private String mMomentId;
    private String mParentCommentId;
    private String mContent;
    private UserInfo mUserInfo;
    private UserInfo mParentUserInfo;
    private long mCreateTime;
}
