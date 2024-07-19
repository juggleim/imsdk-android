package com.juggle.im.model;

import com.juggle.im.internal.util.JLogger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ConversationMentionInfo {
    public static class MentionMsg {
        private String mSenderId;
        private String mMsgId;
        private long mMsgTime;

        public String getSenderId() {
            return mSenderId;
        }

        public void setSenderId(String senderId) {
            this.mSenderId = senderId;
        }

        public String getMsgId() {
            return mMsgId;
        }

        public void setMsgId(String msgId) {
            this.mMsgId = msgId;
        }

        public long getMsgTime() {
            return mMsgTime;
        }

        public void setMsgTime(long msgTime) {
            this.mMsgTime = msgTime;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MentionMsg that = (MentionMsg) o;
            return Objects.equals(mMsgId, that.mMsgId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(mMsgId);
        }
    }

    public String encodeToJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            if (mMentionMsgList != null && mMentionMsgList.size() > 0) {
                JSONArray jsonMentionMsgs = new JSONArray();
                for (MentionMsg mentionMsg : mMentionMsgList) {
                    JSONObject jsonMentionMsg = new JSONObject();
                    jsonMentionMsg.putOpt(MENTION_MSG_SENDER_ID, mentionMsg.getSenderId());
                    jsonMentionMsg.putOpt(MENTION_MSG_ID, mentionMsg.getMsgId());
                    jsonMentionMsg.putOpt(MENTION_MSG_TIME, mentionMsg.getMsgTime());
                    jsonMentionMsgs.put(jsonMentionMsg);
                }
                jsonObject.putOpt(MENTION_MSG_LIST, jsonMentionMsgs);
            }
        } catch (JSONException e) {
            JLogger.e("MSG-Encode", "ConcreteConversationMention encodeToJson JSONException " + e.getMessage());
        }
        return jsonObject.toString();
    }

    public ConversationMentionInfo(String json) {
        if (json == null || json.length() == 0) {
            return;
        }
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonMentionMsgs = jsonObject.optJSONArray(MENTION_MSG_LIST);
            if (jsonMentionMsgs != null) {
                List<MentionMsg> mentionMsgs = new ArrayList<>();
                for (int i = 0; i < jsonMentionMsgs.length(); i++) {
                    JSONObject jsonUser = jsonMentionMsgs.optJSONObject(i);
                    MentionMsg mentionMsg = new MentionMsg();
                    mentionMsg.setSenderId(jsonUser.optString(MENTION_MSG_SENDER_ID));
                    mentionMsg.setMsgId(jsonUser.optString(MENTION_MSG_ID));
                    mentionMsg.setMsgTime(jsonUser.optLong(MENTION_MSG_TIME));
                    mentionMsgs.add(mentionMsg);
                }
                mMentionMsgList = mentionMsgs;
            }
        } catch (JSONException e) {
            JLogger.e("MSG-Decode", "ConcreteConversationMention decode JSONException " + e.getMessage());
        }
    }

    public ConversationMentionInfo() {
    }

    public void setMentionMsgList(List<MentionMsg> mentionMsgList) {
        this.mMentionMsgList = mentionMsgList;
    }

    public List<MentionMsg> getMentionMsgList() {
        return mMentionMsgList;
    }

    private List<MentionMsg> mMentionMsgList;

    private static final String MENTION_MSG_LIST = "mentionMsgs";
    private static final String MENTION_MSG_SENDER_ID = "senderId";
    private static final String MENTION_MSG_ID = "msgId";
    private static final String MENTION_MSG_TIME = "msgTime";
}
