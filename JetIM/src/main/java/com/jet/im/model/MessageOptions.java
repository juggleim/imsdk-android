package com.jet.im.model;

/**
 * @author Ye_Guli
 * @create 2024-06-11 16:09
 */
public class MessageOptions {
    private MessageReferredInfo mReferredInfo;
    private MessageMentionInfo mMentionInfo;

    public MessageReferredInfo getReferredInfo() {
        return mReferredInfo;
    }

    public void setReferredInfo(MessageReferredInfo referredInfo) {
        this.mReferredInfo = referredInfo;
    }

    public MessageMentionInfo getMentionInfo() {
        return mMentionInfo;
    }

    public void setMentionInfo(MessageMentionInfo mentionInfo) {
        this.mMentionInfo = mentionInfo;
    }
}