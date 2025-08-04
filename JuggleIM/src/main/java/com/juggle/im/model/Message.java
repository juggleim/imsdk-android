package com.juggle.im.model;

public class Message {
    /// 消息方向，发送/接收
    public enum MessageDirection {
        SEND(1),
        RECEIVE(2);

        MessageDirection(int value) {
            this.mValue = value;
        }

        public static MessageDirection setValue(int value) {
            for (MessageDirection d : MessageDirection.values()) {
                if (value == d.mValue) {
                    return d;
                }
            }
            return SEND;
        }

        public int getValue() {
            return mValue;
        }

        private final int mValue;
    }

    /// 消息状态
    public enum MessageState {
        UNKNOWN(0),
        SENDING(1),
        SENT(2),
        FAIL(3),
        UPLOADING(4);

        MessageState(int value) {
            this.mValue = value;
        }

        public static MessageState setValue(int value) {
            for (MessageState s : MessageState.values()) {
                if (value == s.mValue) {
                    return s;
                }
            }
            return UNKNOWN;
        }

        public int getValue() {
            return mValue;
        }

        private final int mValue;
    }

    public Message() {
    }

    public Message(Message other) {
        mConversation = other.getConversation();
        mContentType = other.getContentType();
        mClientMsgNo = other.getClientMsgNo();
        mMessageId = other.getMessageId();
        mDirection = other.getDirection();
        mState = MessageState.setValue(other.getState().getValue());
        mHasRead = other.isHasRead();
        mTimestamp = other.getTimestamp();
        mSenderUserId = other.getSenderUserId();
        mContent = other.getContent();
        mGroupMessageReadInfo = other.getGroupMessageReadInfo();
        mReferredMessage = other.getReferredMessage();
        mMentionInfo = other.getMentionInfo();
        mLocalAttribute = other.getLocalAttribute();
        mIsDelete = other.isDelete();
        mIsEdit = other.isEdit();
        mLifeTimeAfterRead = other.getLifeTimeAfterRead();
        mDestroyTime = other.getDestroyTime();
    }

    public Conversation getConversation() {
        return mConversation;
    }

    public void setConversation(Conversation conversation) {
        mConversation = conversation;
    }

    public String getContentType() {
        return mContentType;
    }

    public void setContentType(String contentType) {
        mContentType = contentType;
    }

    public long getClientMsgNo() {
        return mClientMsgNo;
    }

    public void setClientMsgNo(long clientMsgNo) {
        mClientMsgNo = clientMsgNo;
    }

    public String getMessageId() {
        return mMessageId;
    }

    public void setMessageId(String messageId) {
        mMessageId = messageId;
    }

    public MessageDirection getDirection() {
        return mDirection;
    }

    public void setDirection(MessageDirection direction) {
        mDirection = direction;
    }

    public MessageState getState() {
        return mState;
    }

    public void setState(MessageState state) {
        mState = state;
    }

    public boolean isHasRead() {
        return mHasRead;
    }

    public void setHasRead(boolean hasRead) {
        mHasRead = hasRead;
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    public void setTimestamp(long timestamp) {
        mTimestamp = timestamp;
    }

    public String getSenderUserId() {
        return mSenderUserId;
    }

    public void setSenderUserId(String senderUserId) {
        mSenderUserId = senderUserId;
    }

    public MessageContent getContent() {
        return mContent;
    }

    public void setContent(MessageContent content) {
        this.mContent = content;
    }

    public GroupMessageReadInfo getGroupMessageReadInfo() {
        return mGroupMessageReadInfo;
    }

    public void setGroupMessageReadInfo(GroupMessageReadInfo groupMessageReadInfo) {
        mGroupMessageReadInfo = groupMessageReadInfo;
    }

    public Message getReferredMessage() {
        return mReferredMessage;
    }

    public void setReferredMessage(Message referredMessage) {
        this.mReferredMessage = referredMessage;
    }

    public MessageMentionInfo getMentionInfo() {
        return mMentionInfo;
    }

    public void setMentionInfo(MessageMentionInfo mentionInfo) {
        this.mMentionInfo = mentionInfo;
    }

    public boolean hasMentionInfo() {
        return mMentionInfo != null;
    }

    public boolean hasReferredInfo() {
        return mReferredMessage != null;
    }

    public String getLocalAttribute() {
        return mLocalAttribute;
    }

    public void setLocalAttribute(String localAttribute) {
        this.mLocalAttribute = localAttribute;
    }

    public boolean isDelete() {
        return mIsDelete;
    }

    public void setDelete(boolean delete) {
        mIsDelete = delete;
    }

    public boolean isEdit() {
        return mIsEdit;
    }

    public void setEdit(boolean edit) {
        mIsEdit = edit;
    }

    public long getDestroyTime() {
        return mDestroyTime;
    }

    public void setDestroyTime(long destroyTime) {
        mDestroyTime = destroyTime;
    }

    public long getLifeTimeAfterRead() {
        return mLifeTimeAfterRead;
    }

    public void setLifeTimeAfterRead(long lifeTimeAfterRead) {
        mLifeTimeAfterRead = lifeTimeAfterRead;
    }

    private Conversation mConversation;
    /// 消息类型
    private String mContentType;
    /// 本端消息唯一编号（只对当前设备生效）
    private long mClientMsgNo;
    /// 消息 id，全局唯一
    private String mMessageId;
    /// 消息方向，发送/接收
    private MessageDirection mDirection;
    /// 消息状态
    private MessageState mState;
    /// 是否已读
    private boolean mHasRead;
    /// 消息发送的时间戳（服务端时间，单位毫秒）
    private long mTimestamp;
    /// 发送者 userId
    private String mSenderUserId;
    /// 消息内容
    private MessageContent mContent;
    private GroupMessageReadInfo mGroupMessageReadInfo;
    private Message mReferredMessage;
    private MessageMentionInfo mMentionInfo;
    /// 消息本地属性（仅对本端生效，不会同步到服务端）
    private String mLocalAttribute;
    ///是否已删除
    private boolean mIsDelete;
    ///是否被编辑
    private boolean mIsEdit;
    /// 消息销毁时间戳（服务器时间，单位毫秒）。
    /// 由消息的发送时间，加上发送消息时 JMessageOptions 的 lifeTime 和 lifeTimeAfterRead 共同决定，取其中较小的那个时间戳。
    /// 默认值为 0，表示不自动销毁。
    private long mDestroyTime;
    /// 消息已读后的生存周期，单位毫秒。
    /// 默认值为 0，表示读后不自动销毁。
    private long mLifeTimeAfterRead;
}
