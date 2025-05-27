package com.jet.im.kit.model.message;

import android.util.Log;

import com.juggle.im.internal.util.JLogger;
import com.juggle.im.model.MessageContent;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class FriendNotifyMessage extends MessageContent {

    public FriendNotifyMessage() {
        mContentType = "jgd:friendntf";
    }

    @Override
    public byte[] encode() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(TYPE, mType);
        } catch (JSONException e) {
            Log.e("FriendNotifyMessage", "encode JSONException " + e.getMessage());
        }

        return jsonObject.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public void decode(byte[] data) {
        if (data == null) {
            Log.e("FriendNotifyMessage", "decode data is null");
            return;
        }
        String jsonStr = new String(data, StandardCharsets.UTF_8);

        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            if (jsonObject.has(TYPE)) {
                mType = jsonObject.optInt(TYPE);
            }
        } catch (JSONException e) {
            Log.e("FriendNotifyMessage", "decode JSONException " + e.getMessage());
        }
    }

    @Override
    public int getFlags() {
        return MessageFlag.IS_SAVE.getValue();
    }

    @Override
    public String conversationDigest() {
        return "[好友通知]";
    }

    public int getType() {
        return mType;
    }

    public void setType(int type) {
        this.mType = type;
    }

    private int mType = 0;
    private static final String TYPE = "type";
}
