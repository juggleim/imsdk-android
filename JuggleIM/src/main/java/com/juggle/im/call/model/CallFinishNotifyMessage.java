package com.juggle.im.call.model;

import com.juggle.im.internal.util.JLogger;
import com.juggle.im.model.MessageContent;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class CallFinishNotifyMessage extends MessageContent {

    public CallFinishNotifyMessage() {
        this.mContentType = "jg:callfinishntf";
    }

    @Override
    public byte[] encode() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(REASON, mFinishNotifyType.getValue());
            jsonObject.put(DURATION, mDuration);
        } catch (JSONException e) {
            JLogger.e("MSG-Encode", "CallFinishNotifyMessage JSONException " + e.getMessage());
        }

        return jsonObject.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public void decode(byte[] data) {
        if (data == null) {
            JLogger.e("MSG-Decode", "CallFinishNotifyMessage decode data is null");
            return;
        }
        String jsonStr = new String(data, StandardCharsets.UTF_8);

        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            if (jsonObject.has(REASON)) {
                mFinishNotifyType = CallFinishNotifyType.setValue(jsonObject.optInt(REASON));
            }
            if (jsonObject.has(DURATION)) {
                mDuration = jsonObject.optLong(DURATION);
            }
        } catch (JSONException e) {
            JLogger.e("MSG-Decode", "CallFinishNotifyMessage decode JSONException " + e.getMessage());
        }

    }

    @Override
    public int getFlags() {
        return MessageFlag.IS_SAVE.getValue();
    }

    @Override
    public String conversationDigest() {
        return CALL;
    }

    public enum CallFinishNotifyType {
        /// 主叫取消
        CANCEL(0),
        /// 被叫拒绝
        REJECT(1),
        /// 被叫无应答
        NO_RESPONSE(2),
        /// 通话结束
        COMPLETE(3);

        CallFinishNotifyType(int value) {
            this.mValue = value;
        }
        public int getValue() {
            return mValue;
        }
        public static CallFinishNotifyType setValue(int value) {
            for (CallFinishNotifyType t : CallFinishNotifyMessage.CallFinishNotifyType.values()) {
                if (value == t.mValue) {
                    return t;
                }
            }
            return CANCEL;
        }
        private final int mValue;
    }

    public CallFinishNotifyType getFinishNotifyType() {
        return mFinishNotifyType;
    }

    public long getDuration() {
        return mDuration;
    }

    private CallFinishNotifyType mFinishNotifyType;
    private long mDuration;

    private static final String REASON = "reason";
    private static final String DURATION = "duration";
    private static final String CALL = "[Call]";
}
