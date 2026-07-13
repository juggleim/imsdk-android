package com.juggle.im.model.messages;

import android.text.TextUtils;

import com.juggle.im.internal.util.JLogger;
import com.juggle.im.model.MessageContent;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class StreamTextMessage extends MessageContent {
    public StreamTextMessage() {
        mContentType = CONTENT_TYPE;
    }

    @Override
    public byte[] encode() {
        JSONObject jsonObject = new JSONObject();
        try {
            if (!TextUtils.isEmpty(mContent)) {
                jsonObject.put(CONTENT, mContent);
            }
            jsonObject.put(IS_FINISHED, mIsFinished);
            jsonObject.put(SEQ, mSeq);
        } catch (JSONException e) {
            JLogger.e("MSG-Encode", "StreamTextMessage JSONException " + e.getMessage());
        }
        return jsonObject.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public void decode(byte[] data) {
        if (data == null) {
            JLogger.e("MSG-Decode", "StreamTextMessage decode data is null");
            return;
        }
        String jsonStr = new String(data, StandardCharsets.UTF_8);

        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            if (jsonObject.has(CONTENT)) {
                mContent = jsonObject.optString(CONTENT);
            }
            if (jsonObject.has(IS_FINISHED)) {
                mIsFinished = jsonObject.optBoolean(IS_FINISHED);
            }
            if (jsonObject.has(SEQ)) {
                mSeq = jsonObject.optInt(SEQ);
            }
        } catch (JSONException e) {
            JLogger.e("MSG-Decode", "StreamTextMessage decode JSONException " + e.getMessage());
        }
    }

    @Override
    public String conversationDigest() {
        if (!TextUtils.isEmpty(mContent)) {
            return mContent;
        }
        return "";
    }

    @Override
    public String getSearchContent() {
        return TextUtils.isEmpty(mContent) ? "" : mContent;
    }

    public String getContent() {
        return mContent;
    }

    public boolean isFinished() {
        return mIsFinished;
    }

    public void setFinished(boolean finished) {
        mIsFinished = finished;
    }

    public void setContent(String content) {
        mContent = content;
    }

    public int getSeq() {
        return mSeq;
    }

    public void setSeq(int seq) {
        mSeq = seq;
    }

    public static final String CONTENT_TYPE = "jg:streamtext";
    private String mContent;
    private boolean mIsFinished;
    private int mSeq;

    private static final String CONTENT = "content";
    private static final String IS_FINISHED = "is_finished";
    private static final String SEQ = "seq";
}
