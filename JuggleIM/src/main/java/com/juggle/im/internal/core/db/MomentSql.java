package com.juggle.im.internal.core.db;

import android.database.Cursor;
import android.text.TextUtils;

import com.juggle.im.JIMConst;
import com.juggle.im.model.GetMomentOption;
import com.juggle.im.model.Moment;
import com.juggle.im.model.MomentComment;
import com.juggle.im.model.MomentMedia;
import com.juggle.im.model.MomentReaction;
import com.juggle.im.model.UserInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MomentSql {
    static final String SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS moment ("
            + "moment_id VARCHAR (64) PRIMARY KEY,"
            + "content TEXT,"
            + "media_array TEXT,"
            + "user_info TEXT,"
            + "reaction_array TEXT,"
            + "comment_array TEXT,"
            + "create_time INTEGER"
            + ")";
    private static final String SQL_GET_MOMENTS = "SELECT * FROM moment WHERE ";
    static final String SQL_UPDATE_MOMENTS = "INSERT OR REPLACE INTO moment (moment_id, content, media_array, user_info, reaction_array, comment_array, create_time) VALUES (?, ?, ?, ?, ?, ?, ?)";
    static final String SQL_DELETE_MOMENT = "DELETE FROM moment WHERE moment_id = ?";

    static String sqlGetMoments(GetMomentOption option, List<String> args) {
        String sql = MomentSql.SQL_GET_MOMENTS;
        if (option.getDirection() == JIMConst.PullDirection.NEWER) {
            sql += " create_time > ?";
        } else {
            sql += " create_time < ?";
        }
        if (option.getStartTime() == 0) {
            option.setStartTime(Long.MAX_VALUE);
        }
        args.add(String.valueOf(option.getStartTime()));
        sql += " ORDER BY create_time";
        if (option.getDirection() == JIMConst.PullDirection.NEWER) {
            sql += " ASC";
        } else {
            sql += " DESC";
        }
        sql += " LIMIT ?";
        args.add(String.valueOf(option.getCount()));
        return sql;
    }

    static Moment momentWithCursor(Cursor cursor) {
        Moment moment = new Moment();
        moment.setMomentId(CursorHelper.readString(cursor, "moment_id"));
        moment.setContent(CursorHelper.readString(cursor, "content"));
        moment.setCreateTime(CursorHelper.readLong(cursor, "create_time"));
        String mediaArrayStr = CursorHelper.readString(cursor, "media_array");
        try {
            if (!TextUtils.isEmpty(mediaArrayStr)) {
                JSONArray mediaJsonArray = new JSONArray(mediaArrayStr);
                List<MomentMedia> mediaList = new ArrayList<>();
                for (int i = 0; i < mediaJsonArray.length(); i++) {
                    JSONObject mediaJson = mediaJsonArray.getJSONObject(i);
                    MomentMedia media = MomentMedia.fromJson(mediaJson);
                    if (media != null) {
                        mediaList.add(media);
                    }
                }
                moment.setMediaList(mediaList);
            }
            String userInfoStr = CursorHelper.readString(cursor, "user_info");
            if (!TextUtils.isEmpty(userInfoStr)) {
                JSONObject userJson = new JSONObject(userInfoStr);
                moment.setUserInfo(UserInfo.fromJson(userJson));
            }
            String reactionArrayStr = CursorHelper.readString(cursor, "reaction_array");
            if (!TextUtils.isEmpty(reactionArrayStr)) {
                JSONArray reactionJsonArray = new JSONArray(reactionArrayStr);
                List<MomentReaction> reactionList = new ArrayList<>();
                for (int i = 0; i < reactionJsonArray.length(); i++) {
                    JSONObject reactionJson = reactionJsonArray.getJSONObject(i);
                    MomentReaction reaction = reactionFromJson(reactionJson);
                    if (reaction != null) {
                        reactionList.add(reaction);
                    }
                }
                moment.setReactionList(reactionList);
            }
            String commentArrayStr = CursorHelper.readString(cursor, "comment_array");
            if (!TextUtils.isEmpty(commentArrayStr)) {
                JSONArray commentJsonArray = new JSONArray(commentArrayStr);
                List<MomentComment> commentList = new ArrayList<>();
                for (int i = 0; i < commentJsonArray.length(); i++) {
                    JSONObject commentJson = commentJsonArray.getJSONObject(i);
                    MomentComment comment = MomentComment.fromJson(commentJson);
                    if (comment != null) {
                        commentList.add(comment);
                    }
                }
                moment.setCommentList(commentList);
            }
        } catch (JSONException ignored) {

        }
        return moment;
    }

    private static MomentReaction reactionFromJson(JSONObject json) {
        if (json == null) {
            return null;
        }
        MomentReaction reaction = new MomentReaction();
        reaction.setKey(json.optString("key"));
        JSONArray userArray = json.optJSONArray("userArray");
        if (userArray != null && userArray.length() > 0) {
            List<UserInfo> userList = new ArrayList<>();
            for (int i = 0; i < userArray.length(); i++) {
                try {
                    JSONObject userJson = userArray.getJSONObject(i);
                    UserInfo userInfo = UserInfo.fromJson(userJson);
                    if (userInfo != null) {
                        userList.add(userInfo);
                    }
                } catch (JSONException ignored) {

                }
            }
            reaction.setUserList(userList);
        }

        return reaction;
    }
}
