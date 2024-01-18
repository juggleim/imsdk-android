package com.jet.im.model;

public class Message {
    /// 消息方向，发送/接收
    enum MessageDirection {
        SEND(1),
        RECEIVE(2);
        MessageDirection(int value) {
            this.mValue = value;
        }
        private final int mValue;
    }

    /// 消息状态
    enum MessageState {
        UNKNOWN(0),
        SENDING(1),
        SENT(2),
        FAIL(3);
        MessageState(int value) {
            this.mValue = value;
        }
        private final int mValue;
    }

    private Conversation mConversation;
    /// 消息类型
    private String mContentType;
    /// 本端消息唯一编号（支队当前设备生效）
    private long mClientMsgNo;
    /// 消息 id，全局唯一
    private String mMessageId;
    /// 消息方向，发送/接收
    private MessageDirection mDirection;
    /// 消息状态
    private MessageState mState;
    /// 是否已读
    private boolean mHasRead;
    /// 消息发送的时间戳（服务端时间）
    private long mTimestamp;
    /// 发送者 userId
    private String mSenderUserId;
    /// 消息内容
    private MessageContent content;

}
