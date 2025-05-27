package com.jet.im.kit.model.message;

import android.util.Log;

import com.juggle.im.model.MessageContent;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class ContactCardMessage extends MessageContent {
    public ContactCardMessage() {
        mContentType = "jgd:contactcard";
    }

    @Override
    public byte[] encode() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(USER_ID, mUserId);
            jsonObject.put(NAME, mName);
            jsonObject.put(PORTRAIT, mPortrait);
        } catch (JSONException e) {
            Log.e("ContactCardMessage", "encode JSONException " + e.getMessage());
        }
        return jsonObject.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public void decode(byte[] data) {
        if (data == null) {
            Log.e("ContactCardMessage", "decode data is null");
            return;
        }
        String jsonStr = new String(data, StandardCharsets.UTF_8);

        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            if (jsonObject.has(USER_ID)) {
                mUserId = jsonObject.optString(USER_ID);
            }
            if (jsonObject.has(NAME)) {
                mName = jsonObject.optString(NAME);
            }
            if (jsonObject.has(PORTRAIT)) {
                mPortrait = jsonObject.optString(PORTRAIT);
            }
        } catch (JSONException e) {
            Log.e("FriendNotifyMessage", "decode JSONException " + e.getMessage());
        }
    }

    @Override
    public String conversationDigest() {
        return "[个人名片]";
    }

    public String getUserId() {
        return mUserId;
    }

    public void setUserId(String userId) {
        mUserId = userId;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getPortrait() {
        return mPortrait;
    }

    public void setPortrait(String portrait) {
        mPortrait = portrait;
    }

    private String mUserId;
    private String mName;
    private String mPortrait;

    private static final String USER_ID = "user_id";
    private static final String NAME = "name";
    private static final String PORTRAIT = "portrait";
}
