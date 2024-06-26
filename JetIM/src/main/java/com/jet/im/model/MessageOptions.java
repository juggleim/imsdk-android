package com.jet.im.model;

/**
 * @author Ye_Guli
 * @create 2024-06-11 16:09
 */
public class MessageOptions {
    private String mReferredMessageId;
    private MessageMentionInfo mMentionInfo;

    public String getReferredMessageId() {
        return mReferredMessageId;
    }

    public void setReferredMessageId(String referredMessageId) {
        this.mReferredMessageId = referredMessageId;
    }

    public MessageMentionInfo getMentionInfo() {
        return mMentionInfo;
    }

    public void setMentionInfo(MessageMentionInfo mentionInfo) {
        this.mMentionInfo = mentionInfo;
    }
}