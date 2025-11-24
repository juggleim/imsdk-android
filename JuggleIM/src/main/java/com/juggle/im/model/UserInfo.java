package com.juggle.im.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class UserInfo implements Parcelable {

    public JSONObject toJson() throws JSONException {
        JSONObject jsonObject = new JSONObject();

        if (mUserId != null && !mUserId.isEmpty()) {
            jsonObject.put("user_id", mUserId);
        }
        if (mUserName != null && !mUserName.isEmpty()) {
            jsonObject.put("nickname", mUserName);
        }
        if (mPortrait != null && !mPortrait.isEmpty()) {
            jsonObject.put("avatar", mPortrait);
        }
        if (mExtra != null && !mExtra.isEmpty()) {
            jsonObject.put("ext_fields", new JSONObject(mExtra));
        }
        jsonObject.put("updated_time", mUpdatedTime);

        return jsonObject;
    }

    public static UserInfo fromJson(JSONObject json) {
        if (json == null || json.length() == 0) {
            return null;
        }

        UserInfo userInfo = new UserInfo();
        userInfo.mUserId = json.optString("user_id");
        userInfo.mUserName = json.optString("nickname");
        userInfo.mPortrait = json.optString("avatar");
        JSONObject extraObject = json.optJSONObject("ext_fields");
        if (extraObject != null) {
            Map<String, String> extraMap = new HashMap<>();
            for (Iterator<String> it = extraObject.keys(); it.hasNext(); ) {
                String key = it.next();
                String value = extraObject.optString(key);
                extraMap.put(key, value);
            }
            userInfo.mExtra = extraMap;
        }
        userInfo.mUpdatedTime = json.optLong("updated_time");
        return userInfo;
    }

    public String getUserId() {
        return mUserId;
    }

    public void setUserId(String userId) {
        mUserId = userId;
    }

    public String getUserName() {
        return mUserName;
    }

    public void setUserName(String userName) {
        mUserName = userName;
    }

    public String getPortrait() {
        return mPortrait;
    }

    public void setPortrait(String portrait) {
        mPortrait = portrait;
    }

    public Map<String, String> getExtra() {
        return mExtra;
    }

    public void setExtra(Map<String, String> extra) {
        mExtra = extra;
    }

    public long getUpdatedTime() {
        return mUpdatedTime;
    }

    public void setUpdatedTime(long updatedTime) {
        mUpdatedTime = updatedTime;
    }

    private String mUserId;
    private String mUserName;
    private String mPortrait;
    private Map<String, String> mExtra;
    private long mUpdatedTime;

    public UserInfo() {

    }

    protected UserInfo(Parcel in) {
        mUserId = in.readString();
        mUserName = in.readString();
        mPortrait = in.readString();
    }

    public static final Creator<UserInfo> CREATOR = new Creator<UserInfo>() {
        @Override
        public UserInfo createFromParcel(Parcel source) {
            return new UserInfo(source);
        }

        @Override
        public UserInfo[] newArray(int size) {
            return new UserInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(mUserId);
        dest.writeString(mUserName);
        dest.writeString(mPortrait);
    }
}
