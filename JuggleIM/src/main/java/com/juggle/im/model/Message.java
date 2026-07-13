package com.juggle.im.model;

import android.text.TextUtils;

import com.juggle.im.JIM;

public class Message {
    /// Message direction: sent or received.
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

    /// Message state.
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

    public String getSenderDisplayName() {
        String userName = "";

        userName = getFriendAlias();
        if (!TextUtils.isEmpty(userName)) {
            return userName;
        }

        if (mConversation != null) {
            if (mConversation.getConversationType() == Conversation.ConversationType.GROUP) {
                GroupMember member = JIM.getInstance().getUserInfoManager().getGroupMember(mConversation.getConversationId(), mSenderUserId);
                if (member != null) {
                    userName = member.getGroupDisplayName();
                }
                if (TextUtils.isEmpty(userName)) {
                    UserInfo userInfo = getUserInfo();
                    if (userInfo != null) {
                        userName = userInfo.getUserName();
                    }
                }
            } else {
                UserInfo userInfo = getUserInfo();
                if (userInfo != null) {
                    userName = userInfo.getUserName();
                }
            }
        }
        return userName;
    }

    public String getFriendAlias() {
        String alias = "";
        if (TextUtils.isEmpty(mSenderUserId)) {
            return alias;
        }
        FriendInfo friendInfo = JIM.getInstance().getUserInfoManager().getFriendInfo(mSenderUserId);
        if (friendInfo != null) {
            alias = friendInfo.getAlias();
        }
        return alias;
    }

    public String getGroupMemberAlias() {
        String alias = "";
        if (mConversation == null || TextUtils.isEmpty(mConversation.getConversationId()) || TextUtils.isEmpty(mSenderUserId)) {
            return alias;
        }
        GroupMember member = JIM.getInstance().getUserInfoManager().getGroupMember(mConversation.getConversationId(), mSenderUserId);
        if (member != null) {
            alias = member.getGroupDisplayName();
        }
        return alias;
    }

    public String getSenderName() {
        UserInfo userInfo = getUserInfo();
        if (userInfo != null) {
            return userInfo.getUserName();
        }
        return "";
    }

    public String getSenderPortrait() {
        UserInfo userInfo = getUserInfo();
        if (userInfo != null) {
            return userInfo.getPortrait();
        }
        return "";
    }

    private UserInfo getUserInfo() {
        if (mUserInfo == null) {
            mUserInfo = JIM.getInstance().getUserInfoManager().getUserInfo(mSenderUserId);
        }
        return mUserInfo;
    }

    private Conversation mConversation;
    /// Message type.
    private String mContentType;
    /// Unique local message number (only valid on the current device).
    private long mClientMsgNo;
    /// Globally unique message ID.
    private String mMessageId;
    /// Message direction: sent or received.
    private MessageDirection mDirection;
    /// Message state.
    private MessageState mState;
    /// Whether the message has been read.
    private boolean mHasRead;
    /// Message send timestamp (server time, in milliseconds).
    private long mTimestamp;
    /// Sender userId.
    private String mSenderUserId;
    /// Message content.
    private MessageContent mContent;
    /// Group message read info (only applies to group messages).
    private GroupMessageReadInfo mGroupMessageReadInfo;
    /// Referred message.
    private Message mReferredMessage;
    /// Mention message.
    private MessageMentionInfo mMentionInfo;
    /// Local message attributes (only valid locally and not synced to the server).
    private String mLocalAttribute;
    /// Whether the message has been deleted.
    private boolean mIsDelete;
    /// Whether the message has been edited.
    private boolean mIsEdit;
    /// Message destruction timestamp (server time, in milliseconds).
    /// Determined by the message send time plus JMessageOptions lifeTime and lifeTimeAfterRead when sending the message; the earlier timestamp is used.
    /// The default value is 0, which means no automatic destruction.
    private long mDestroyTime;
    /// Message lifetime after it is read, in milliseconds.
    /// The default value is 0, which means no automatic destruction after read.
    private long mLifeTimeAfterRead;
    private UserInfo mUserInfo;
}
