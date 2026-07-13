package com.juggle.im.model;

/**
 * @author Ye_Guli
 * @create 2024-06-11 16:09
 */
public class MessageOptions {
    private String mReferredMessageId;
    private MessageMentionInfo mMentionInfo;
    private PushData mPushData;
    /// Message lifetime in milliseconds. Example: 86400000 = 24 * 60 * 60 * 1000; the message is automatically deleted after 1 day.
    /// The default value is 0, which means no automatic destruction.
    private long mLifeTime;
    /// Message lifetime after it is read, in milliseconds. Usually less than lifeTime.
    /// The default value is 0, which means no automatic destruction after read.
    private long mLifeTimeAfterRead;

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

    public PushData getPushData() {
        return mPushData;
    }

    public void setPushData(PushData pushData) {
        mPushData = pushData;
    }

    public long getLifeTime() {
        return mLifeTime;
    }

    public void setLifeTime(long lifeTime) {
        mLifeTime = lifeTime;
    }

    public long getLifeTimeAfterRead() {
        return mLifeTimeAfterRead;
    }

    public void setLifeTimeAfterRead(long lifeTimeAfterRead) {
        mLifeTimeAfterRead = lifeTimeAfterRead;
    }
}
