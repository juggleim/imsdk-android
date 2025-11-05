package com.juggle.im.model;

/**
 * @author Ye_Guli
 * @create 2024-06-11 16:09
 */
public class MessageOptions {
    private String mReferredMessageId;
    private MessageMentionInfo mMentionInfo;
    private PushData mPushData;
    /// 消息生存周期，单位毫秒。例：86400000 = 24 * 60 * 60 * 1000，该消息1天后会被自动删除。
    /// 默认值为 0，表示不自动销毁。
    private long mLifeTime;
    /// 消息已读后的生存周期，单位毫秒。通常小于 lifeTime。
    /// 默认值为 0，表示读后不自动销毁。
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