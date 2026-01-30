package com.juggle.im.internal.model.messages;

import com.juggle.im.internal.util.JLogger;
import com.juggle.im.model.MessageContent;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class StreamAppendMessage extends MessageContent {
    public StreamAppendMessage() {
        mContentType = CONTENT_TYPE;
    }

    @Override
    public byte[] encode() {
        //不会往外发
        return new byte[0];
    }

    @Override
    public void decode(byte[] data) {
        if (data == null) {
            JLogger.e("MSG-Decode", "StreamAppendMessage decode data is null");
            return;
        }
        String jsonStr = new String(data, StandardCharsets.UTF_8);

        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            if (jsonObject.has(STREAM_ID)) {
                mStreamId = jsonObject.optString(STREAM_ID);
            }
            if (jsonObject.has(CONTENT)) {
                mContent = jsonObject.optString(CONTENT);
            }
            if (jsonObject.has(SEQ)) {
                mSeq = jsonObject.optInt(SEQ);
            }
            if (jsonObject.has(IS_FINISHED)) {
                mIsFinished = jsonObject.optBoolean(IS_FINISHED);
            }
        } catch (JSONException e) {
            JLogger.e("MSG-Decode", "StreamAppendMessage decode JSONException " + e.getMessage());
        }
    }

    @Override
    public int getFlags() {
        return MessageFlag.IS_STATUS.getValue();
    }

    public String getStreamId() {
        return mStreamId;
    }

    public String getContent() {
        return mContent;
    }

    public int getSeq() {
        return mSeq;
    }

    public boolean isFinished() {
        return mIsFinished;
    }

    public static final String CONTENT_TYPE = "jg:streamappend";

    private String mStreamId;
    private String mContent;
    private int mSeq;
    private boolean mIsFinished;

    private static final String STREAM_ID = "stream_id";
    private static final String CONTENT = "content";
    private static final String SEQ = "seq";
    private static final String IS_FINISHED = "is_finished";
}
