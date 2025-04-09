package com.jet.im.kit.model.message;

import android.util.Log;

import com.juggle.im.model.MessageContent;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class StreamTextMessage extends MessageContent {
    public StreamTextMessage() {
        mContentType = "jgs:text";
    }

    @Override
    public byte[] encode() {
        return new byte[0];
    }

    @Override
    public int getFlags() {
        return MessageFlag.IS_STATUS.getValue();
    }

    @Override
    public void decode(byte[] data) {
        if (data == null) {
            Log.e("StreamTextMessage", "decode data is null");
            return;
        }
        String jsonStr = new String(data, StandardCharsets.UTF_8);

        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            if (jsonObject.has(CONTENT)) {
                mContent = jsonObject.optString(CONTENT);
            }
            if (jsonObject.has(STREAM_MSG_ID)) {
                mStreamId = jsonObject.optString(STREAM_MSG_ID);
            }
        } catch (JSONException e) {
            Log.e("StreamTextMessage", "decode JSONException " + e.getMessage());
        }

    }

    public String getContent() {
        return mContent;
    }

    public void setContent(String content) {
        mContent = content;
    }

    public String getStreamId() {
        return mStreamId;
    }

    public void setStreamId(String streamId) {
        mStreamId = streamId;
    }

    private String mContent;
    private String mStreamId;

    private static final String CONTENT = "content";
    private static final String STREAM_MSG_ID = "stream_msg_id";
}
