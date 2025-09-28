package com.juggle.im.internal;

import android.content.Context;
import android.text.TextUtils;

import com.juggle.im.JErrorCode;
import com.juggle.im.JIMConst;
import com.juggle.im.call.internal.CallManager;
import com.juggle.im.call.internal.model.CallActiveCallMessage;
import com.juggle.im.call.model.CallFinishNotifyMessage;
import com.juggle.im.internal.core.network.wscallback.GetFavoriteMsgCallback;
import com.juggle.im.model.FavoriteMessage;
import com.juggle.im.model.GetFavoriteMessageOption;
import com.juggle.im.model.GroupMember;
import com.juggle.im.interfaces.IChatroomManager;
import com.juggle.im.interfaces.IMessageManager;
import com.juggle.im.interfaces.IMessageUploadProvider;
import com.juggle.im.internal.core.JIMCore;
import com.juggle.im.internal.core.network.wscallback.GetGlobalMuteCallback;
import com.juggle.im.internal.core.network.JWebSocket;
import com.juggle.im.internal.core.network.wscallback.GetTopMsgCallback;
import com.juggle.im.internal.core.network.wscallback.MessageReactionListCallback;
import com.juggle.im.internal.core.network.wscallback.QryHisMsgCallback;
import com.juggle.im.internal.core.network.wscallback.QryReadDetailCallback;
import com.juggle.im.internal.core.network.wscallback.SendMessageCallback;
import com.juggle.im.internal.core.network.wscallback.WebSocketTimestampCallback;
import com.juggle.im.internal.downloader.MediaDownloadEngine;
import com.juggle.im.internal.logger.IJLog;
import com.juggle.im.internal.model.ConcreteConversationInfo;
import com.juggle.im.internal.model.ConcreteMessage;
import com.juggle.im.internal.model.MergeInfo;
import com.juggle.im.internal.model.messages.AddConvMessage;
import com.juggle.im.internal.model.messages.CleanMsgMessage;
import com.juggle.im.internal.model.messages.ClearTotalUnreadMessage;
import com.juggle.im.internal.model.messages.ClearUnreadMessage;
import com.juggle.im.internal.model.messages.DeleteConvMessage;
import com.juggle.im.internal.model.messages.DeleteMsgMessage;
import com.juggle.im.internal.model.messages.GroupReadNtfMessage;
import com.juggle.im.internal.model.messages.LogCommandMessage;
import com.juggle.im.internal.model.messages.MarkUnreadMessage;
import com.juggle.im.internal.model.messages.MsgExSetMessage;
import com.juggle.im.internal.model.messages.MsgModifyMessage;
import com.juggle.im.internal.model.messages.ReadNtfMessage;
import com.juggle.im.internal.model.messages.RecallCmdMessage;
import com.juggle.im.internal.model.messages.TagAddConvMessage;
import com.juggle.im.internal.model.messages.TagDelConvMessage;
import com.juggle.im.internal.model.messages.TopConvMessage;
import com.juggle.im.internal.model.messages.TopMsgMessage;
import com.juggle.im.internal.model.messages.UnDisturbConvMessage;
import com.juggle.im.internal.util.FileUtils;
import com.juggle.im.internal.util.JLogger;
import com.juggle.im.internal.util.JThreadPoolExecutor;
import com.juggle.im.model.Conversation;
import com.juggle.im.model.ConversationInfo;
import com.juggle.im.model.GetMessageOptions;
import com.juggle.im.model.GroupInfo;
import com.juggle.im.model.GroupMessageReadInfo;
import com.juggle.im.model.MediaMessageContent;
import com.juggle.im.model.Message;
import com.juggle.im.model.MessageContent;
import com.juggle.im.model.MessageOptions;
import com.juggle.im.model.MessageQueryOptions;
import com.juggle.im.model.MessageReaction;
import com.juggle.im.model.MessageReactionItem;
import com.juggle.im.model.SearchConversationsResult;
import com.juggle.im.model.TimePeriod;
import com.juggle.im.model.UserInfo;
import com.juggle.im.model.messages.FileMessage;
import com.juggle.im.model.messages.ImageMessage;
import com.juggle.im.model.messages.MergeMessage;
import com.juggle.im.model.messages.RecallInfoMessage;
import com.juggle.im.model.messages.SnapshotPackedVideoMessage;
import com.juggle.im.model.messages.TextMessage;
import com.juggle.im.model.messages.ThumbnailPackedImageMessage;
import com.juggle.im.model.messages.UnknownMessage;
import com.juggle.im.model.messages.VideoMessage;
import com.juggle.im.model.messages.VoiceMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

public class MessageManager implements IMessageManager, JWebSocket.IWebSocketMessageListener, IChatroomManager.IChatroomListener {
    public MessageManager(JIMCore core, UserInfoManager userInfoManager, ChatroomManager chatroomManager, CallManager callManager) {
        this.mCore = core;
        this.mCore.getWebSocket().setMessageListener(this);
        this.mUserInfoManager = userInfoManager;
        this.mChatroomManager = chatroomManager;
        this.mCallManager = callManager;
        chatroomManager.addListener("MessageManager", this);
        this.mChatroomSyncMap = new ConcurrentHashMap<>();
        ContentTypeCenter.getInstance().registerContentType(TextMessage.class);
        ContentTypeCenter.getInstance().registerContentType(ImageMessage.class);
        ContentTypeCenter.getInstance().registerContentType(FileMessage.class);
        ContentTypeCenter.getInstance().registerContentType(VoiceMessage.class);
        ContentTypeCenter.getInstance().registerContentType(VideoMessage.class);
        ContentTypeCenter.getInstance().registerContentType(RecallInfoMessage.class);
        ContentTypeCenter.getInstance().registerContentType(RecallCmdMessage.class);
        ContentTypeCenter.getInstance().registerContentType(DeleteConvMessage.class);
        ContentTypeCenter.getInstance().registerContentType(ReadNtfMessage.class);
        ContentTypeCenter.getInstance().registerContentType(GroupReadNtfMessage.class);
        ContentTypeCenter.getInstance().registerContentType(MergeMessage.class);
        ContentTypeCenter.getInstance().registerContentType(CleanMsgMessage.class);
        ContentTypeCenter.getInstance().registerContentType(DeleteMsgMessage.class);
        ContentTypeCenter.getInstance().registerContentType(ClearUnreadMessage.class);
        ContentTypeCenter.getInstance().registerContentType(TopConvMessage.class);
        ContentTypeCenter.getInstance().registerContentType(UnDisturbConvMessage.class);
        ContentTypeCenter.getInstance().registerContentType(LogCommandMessage.class);
        ContentTypeCenter.getInstance().registerContentType(ThumbnailPackedImageMessage.class);
        ContentTypeCenter.getInstance().registerContentType(SnapshotPackedVideoMessage.class);
        ContentTypeCenter.getInstance().registerContentType(AddConvMessage.class);
        ContentTypeCenter.getInstance().registerContentType(ClearTotalUnreadMessage.class);
        ContentTypeCenter.getInstance().registerContentType(MarkUnreadMessage.class);
        ContentTypeCenter.getInstance().registerContentType(CallFinishNotifyMessage.class);
        ContentTypeCenter.getInstance().registerContentType(MsgExSetMessage.class);
        ContentTypeCenter.getInstance().registerContentType(MsgModifyMessage.class);
        ContentTypeCenter.getInstance().registerContentType(TagAddConvMessage.class);
        ContentTypeCenter.getInstance().registerContentType(TagDelConvMessage.class);
        ContentTypeCenter.getInstance().registerContentType(TopMsgMessage.class);
        ContentTypeCenter.getInstance().registerContentType(CallActiveCallMessage.class);
    }

    private ConcreteMessage saveMessageWithContent(MessageContent content,
                                                   Conversation conversation,
                                                   MessageOptions options,
                                                   Message.MessageState state,
                                                   Message.MessageDirection direction,
                                                   boolean isBroadcast) {
        //构造消息
        ConcreteMessage message = new ConcreteMessage();
        message.setContent(content);
        message.setConversation(conversation);
        if (content instanceof UnknownMessage) {
            UnknownMessage unknown = (UnknownMessage) content;
            message.setContentType(unknown.getMessageType());
        } else {
            message.setContentType(content.getContentType());
        }
        message.setDirection(direction);
        message.setState(state);
        message.setSenderUserId(mCore.getUserId());
        message.setClientUid(createClientUid());
        long now = System.currentTimeMillis();
        message.setTimestamp(now);
        int flags = content.getFlags();
        if (isBroadcast) {
            flags |= MessageContent.MessageFlag.IS_BROADCAST.getValue();
        }
        message.setFlags(flags);
        if (options != null) {
            if (options.getMentionInfo() != null) {
                message.setMentionInfo(options.getMentionInfo());
            }
            if (!TextUtils.isEmpty(options.getReferredMessageId())) {
                ConcreteMessage referMsg = mCore.getDbManager().getMessageWithMessageId(options.getReferredMessageId(), mCore.getCurrentTime());
                if (referMsg != null) {
                    message.setReferredMessage(referMsg);
                }
            }
            if (options.getPushData() != null) {
                message.setPushData(options.getPushData());
            }
            message.setLifeTime(options.getLifeTime());
            message.setLifeTimeAfterRead(options.getLifeTimeAfterRead());
        }
        if (message.getLifeTime() > 0) {
            message.setDestroyTime(mCore.getCurrentTime() + message.getLifeTime());
        }
        //保存消息
        if ((message.getFlags() & MessageContent.MessageFlag.IS_SAVE.getValue()) != 0) {
            List<ConcreteMessage> list = new ArrayList<>(1);
            list.add(message);
            mCore.getDbManager().insertMessages(list);
            //回调通知
            if (conversation.getConversationType() != Conversation.ConversationType.CHATROOM) {
                if (mSendReceiveListener != null) {
                    mSendReceiveListener.onMessageSave(message);
                }
            }
        }
        //返回消息
        return message;
    }

    private void updateMessageWithContent(ConcreteMessage message) {
        if (message.getContent() != null) {
            if (message.getContent() instanceof UnknownMessage) {
                UnknownMessage unknown = (UnknownMessage) message.getContent();
                message.setContentType(unknown.getMessageType());
            } else {
                message.setContentType(message.getContent().getContentType());
            }
        }
        if (message.hasReferredInfo()) {
            ConcreteMessage referMsg = mCore.getDbManager().getMessageWithMessageId(message.getReferredMessage().getMessageId(), mCore.getCurrentTime());
            message.setReferredMessage(referMsg);
        }
        //保存消息
        mCore.getDbManager().updateMessage(message);
    }

    private Message sendMessage(MessageContent content,
                                Conversation conversation,
                                MessageOptions options,
                                boolean isBroadcast,
                                ISendMessageCallback callback) {
        if (content == null || conversation == null) {
            if (callback != null) {
                mCore.getCallbackHandler().post(() -> callback.onError(null, JErrorCode.INVALID_PARAM));
            }
            return null;
        }
        ConcreteMessage message = saveMessageWithContent(content, conversation, options, Message.MessageState.SENDING, Message.MessageDirection.SEND, isBroadcast);
        sendWebSocketMessage(message, isBroadcast, callback);
        return message;
    }

    private void sendWebSocketMessage(ConcreteMessage m, boolean isBroadcast, ISendMessageCallback callback) {
        ConcreteMessage message = new ConcreteMessage(m);
        MergeInfo mergeInfo = null;
        if (message.getContent() instanceof MergeMessage) {
            MergeMessage mergeMessage = (MergeMessage) message.getContent();
            mergeInfo = new MergeInfo();
            mergeInfo.setConversation(mergeMessage.getConversation());
            mergeInfo.setContainerMsgId(mergeMessage.getContainerMsgId());
            mergeInfo.setMessages(mCore.getDbManager().getConcreteMessagesByMessageIds(mergeMessage.getMessageIdList()));
        }
        MessageContent content = message.getContent();
        if (content == null) {
            if (callback != null) {
                mCore.getCallbackHandler().post(() -> callback.onError(message, JErrorCode.INVALID_PARAM));
            }
            return;
        }

        SendMessageCallback messageCallback = new SendMessageCallback(message.getClientMsgNo()) {
            @Override
            public void onSuccess(long clientMsgNo, String msgId, long timestamp, long seqNo, String contentType, MessageContent content, int groupMemberCount) {
                JLogger.i("MSG-Send", "success, clientMsgNo is " + clientMsgNo);
                mCore.getDbManager().updateMessageAfterSend(clientMsgNo, msgId, timestamp, seqNo, groupMemberCount);
                message.setClientMsgNo(clientMsgNo);
                message.setMessageId(msgId);
                message.setTimestamp(timestamp);
                message.setSeqNo(seqNo);
                message.setState(Message.MessageState.SENT);
                message.setDestroyTime(timestamp + message.getLifeTime());
                if (message.getConversation().getConversationType() == Conversation.ConversationType.GROUP) {
                    GroupMessageReadInfo info = new GroupMessageReadInfo();
                    info.setReadCount(0);
                    info.setMemberCount(groupMemberCount);
                    message.setGroupMessageReadInfo(info);
                }

                if (contentType != null && content != null) {
                    mCore.getDbManager().updateMessageContentWithMessageId(content, contentType, msgId);
                    message.setContent(content);
                    message.setContentType(contentType);
                }

                if (message.getContent() instanceof MergeMessage) {
                    MergeMessage mergeMessage = (MergeMessage) message.getContent();
                    if (TextUtils.isEmpty(mergeMessage.getContainerMsgId())) {
                        mergeMessage.setContainerMsgId(message.getMessageId());
                    }
                    mCore.getDbManager().updateMessageContentWithMessageId(message.getContent(), message.getContentType(), message.getMessageId());
                }
                if (message.getConversation().getConversationType() != Conversation.ConversationType.CHATROOM) {
                    if ((message.getFlags() & MessageContent.MessageFlag.IS_STATUS.getValue()) == 0) {
                        updateMessageSendSyncTime(timestamp);
                    }
                    if ((message.getFlags() & MessageContent.MessageFlag.IS_SAVE.getValue()) != 0) {
                        if (mSendReceiveListener != null) {
                            mSendReceiveListener.onMessageSend(message);
                        }
                    }
                }

                if (callback != null) {
                    mCore.getCallbackHandler().post(() -> callback.onSuccess(message));
                }
            }

            @Override
            public void onError(int errorCode, long clientMsgNo) {
                JLogger.e("MSG-Send", "fail, clientMsgNo is " + clientMsgNo + ", errorCode is " + errorCode);
                message.setState(Message.MessageState.FAIL);
                setMessageState(clientMsgNo, Message.MessageState.FAIL);
                if (callback != null) {
                    message.setClientMsgNo(clientMsgNo);
                    mCore.getCallbackHandler().post(() -> callback.onError(message, errorCode));
                }
            }
        };
        if (mCore.getWebSocket() == null) {
            int errorCode = JErrorCode.CONNECTION_UNAVAILABLE;
            JLogger.e("MSG-Send", "fail, clientMsgNo is " + message.getClientMsgNo() + ", errorCode is " + errorCode);
            message.setState(Message.MessageState.FAIL);
            setMessageState(message.getClientMsgNo(), Message.MessageState.FAIL);
            if (callback != null) {
                mCore.getCallbackHandler().post(() -> callback.onError(message, errorCode));
            }
            return;
        }
        mCore.getWebSocket().sendIMMessage(
                content,
                message.getConversation(),
                message.getClientUid(),
                mergeInfo,
                message.hasMentionInfo() ? message.getMentionInfo() : null,
                (ConcreteMessage) message.getReferredMessage(),
                message.getPushData(),
                isBroadcast,
                mCore.getUserId(),
                message.getLifeTime(),
                message.getLifeTimeAfterRead(),
                messageCallback
        );
    }

    @Override
    public Message sendMessage(MessageContent content, Conversation conversation, ISendMessageCallback callback) {
        return sendMessage(content, conversation, null, callback);
    }

    @Override
    public Message sendMessage(MessageContent content, Conversation conversation, MessageOptions options, ISendMessageCallback callback) {
        return sendMessage(content, conversation, options, false, callback);
    }

    @Override
    public Message sendMediaMessage(MediaMessageContent content, Conversation conversation, ISendMediaMessageCallback callback) {
        return sendMediaMessage(content, conversation, null, callback);
    }

    @Override
    public Message sendMediaMessage(MediaMessageContent content, Conversation conversation, MessageOptions options, ISendMediaMessageCallback callback) {
        if (content == null) {
            if (callback != null) {
                mCore.getCallbackHandler().post(() -> callback.onError(null, JErrorCode.INVALID_PARAM));
            }
            return null;
        }
        ConcreteMessage message = saveMessageWithContent(content, conversation, options, Message.MessageState.UPLOADING, Message.MessageDirection.SEND, false);
        return sendMediaMessage(message, callback);
    }

    private Message sendMediaMessage(ConcreteMessage message, ISendMediaMessageCallback callback) {
        IMessageUploadProvider.UploadCallback uploadCallback = new IMessageUploadProvider.UploadCallback() {
            @Override
            public void onProgress(int progress) {
                if (callback != null) {
                    mCore.getCallbackHandler().post(() -> callback.onProgress(progress, new ConcreteMessage(message)));
                }
            }

            @Override
            public void onSuccess(Message uploadMessage) {
                if (!(uploadMessage instanceof ConcreteMessage)) {
                    uploadMessage.setState(Message.MessageState.FAIL);
                    setMessageState(uploadMessage.getClientMsgNo(), Message.MessageState.FAIL);
                    if (callback != null) {
                        mCore.getCallbackHandler().post(() -> callback.onError(new Message(message), JErrorCode.MESSAGE_UPLOAD_ERROR));
                    }
                    return;
                }
                ConcreteMessage cm = (ConcreteMessage) uploadMessage;
                mCore.getDbManager().updateMessageContentWithClientMsgNo(cm.getContent(), cm.getContentType(), cm.getClientMsgNo());
                cm.setState(Message.MessageState.SENDING);
                setMessageState(cm.getClientMsgNo(), Message.MessageState.SENDING);
                sendWebSocketMessage(cm, false, new ISendMessageCallback() {
                    @Override
                    public void onSuccess(Message message1) {
                        if (callback != null) {
                            mCore.getCallbackHandler().post(() -> callback.onSuccess(message1));
                        }
                    }

                    @Override
                    public void onError(Message message1, int errorCode) {
                        if (callback != null) {
                            mCore.getCallbackHandler().post(() -> callback.onError(message1, errorCode));
                        }
                    }
                });
            }

            @Override
            public void onError() {
                ConcreteMessage callbackMessage = new ConcreteMessage(message);
                callbackMessage.setState(Message.MessageState.FAIL);
                setMessageState(callbackMessage.getClientMsgNo(), Message.MessageState.FAIL);
                if (callback != null) {
                    mCore.getCallbackHandler().post(() -> callback.onError(callbackMessage, JErrorCode.MESSAGE_UPLOAD_ERROR));
                }
            }

            @Override
            public void onCancel() {
                ConcreteMessage callbackMessage = new ConcreteMessage(message);
                callbackMessage.setState(Message.MessageState.FAIL);
                setMessageState(callbackMessage.getClientMsgNo(), Message.MessageState.FAIL);
                if (callback != null) {
                    mCore.getCallbackHandler().post(() -> callback.onCancel(callbackMessage));
                }
            }
        };
        if (mMessageUploadProvider != null) {
            mMessageUploadProvider.uploadMessage(message, uploadCallback);
        } else if (mDefaultMessageUploadProvider != null) {
            mDefaultMessageUploadProvider.uploadMessage(message, uploadCallback);
        } else {
            if (callback != null) {
                mCore.getCallbackHandler().post(() -> callback.onError(message, JErrorCode.MESSAGE_UPLOAD_ERROR));
            }
        }
        return message;
    }

    @Override
    public Message resendMessage(Message message, ISendMessageCallback callback) {
        if (message.getClientMsgNo() <= 0
                || !TextUtils.isEmpty(message.getMessageId())//已发送的消息不允许重发
                || message.getContent() == null
                || message.getConversation() == null
                || message.getConversation().getConversationId() == null
                || !(message instanceof ConcreteMessage)) {
            if (callback != null) {
                mCore.getCallbackHandler().post(() -> callback.onError(message, ConstInternal.ErrorCode.INVALID_PARAM));
            }
            return message;
        }
        if (message.getClientMsgNo() > 0) {
            if (message.getState() == Message.MessageState.SENT) {
                if (callback != null) {
                    mCore.getCallbackHandler().post(() -> callback.onSuccess(message));
                }
                return message;
            }
            if (message.getState() != Message.MessageState.SENDING) {
                message.setState(Message.MessageState.SENDING);
                setMessageState(message.getClientMsgNo(), Message.MessageState.SENDING);
            }
            updateMessageWithContent((ConcreteMessage) message);
            sendWebSocketMessage((ConcreteMessage) message, false, callback);
            return message;
        } else {
            MessageOptions options = new MessageOptions();
            options.setMentionInfo(message.getMentionInfo());
            options.setReferredMessageId(message.getReferredMessage() == null ? null : message.getReferredMessage().getMessageId());
            ConcreteMessage concreteMessage = (ConcreteMessage) message;
            options.setLifeTime(concreteMessage.getLifeTime());
            options.setLifeTimeAfterRead(concreteMessage.getLifeTimeAfterRead());
            return sendMessage(message.getContent(), message.getConversation(), options, callback);
        }
    }

    @Override
    public Message resendMediaMessage(Message m,
                                      ISendMediaMessageCallback callback) {
        if (m.getClientMsgNo() <= 0
                || !TextUtils.isEmpty(m.getMessageId())//已发送的消息不允许重发
                || m.getContent() == null
                || !(m.getContent() instanceof MediaMessageContent)
                || m.getConversation() == null
                || m.getConversation().getConversationId() == null
                || !(m instanceof ConcreteMessage)) {
            if (callback != null) {
                mCore.getCallbackHandler().post(() -> callback.onError(m, ConstInternal.ErrorCode.INVALID_PARAM));
            }
            return m;
        }
        if (m.getState() == Message.MessageState.SENT) {
            if (callback != null) {
                mCore.getCallbackHandler().post(() -> callback.onSuccess(m));
            }
            return m;
        }
        ConcreteMessage message = new ConcreteMessage((ConcreteMessage) m);
        if (message.getState() != Message.MessageState.SENDING) {
            message.setState(Message.MessageState.SENDING);
            setMessageState(message.getClientMsgNo(), Message.MessageState.SENDING);
        }
        updateMessageWithContent(message);
        return sendMediaMessage(message, callback);
    }

    @Override
    public Message saveMessage(MessageContent content, Conversation conversation) {
        return saveMessage(content, conversation, null);
    }

    @Override
    public Message saveMessage(MessageContent content, Conversation conversation, MessageOptions options) {
        return saveMessageWithContent(content, conversation, options, Message.MessageState.UNKNOWN, Message.MessageDirection.SEND, false);
    }

    @Override
    public List<Message> getMessages(Conversation conversation, int count, long timestamp, JIMConst.PullDirection direction) {
        return getMessages(
                count, timestamp, direction,
                new MessageQueryOptions
                        .Builder()
                        .setConversations(Collections.singletonList(conversation))
                        .build());
    }

    @Override
    public List<Message> getMessages(Conversation conversation, int count, long timestamp, JIMConst.PullDirection direction, List<String> contentTypes) {
        return getMessages(
                count, timestamp, direction,
                new MessageQueryOptions
                        .Builder()
                        .setConversations(Collections.singletonList(conversation))
                        .setContentTypes(contentTypes)
                        .build());
    }

    @Override
    public List<Message> getMessages(int count, long timestamp, JIMConst.PullDirection pullDirection, MessageQueryOptions messageQueryOptions) {
        if (count > 100) {
            count = 100;
        }
        long now = mCore.getCurrentTime();
        return mCore.getDbManager().getMessages(
                count,
                timestamp,
                pullDirection,
                messageQueryOptions != null ? messageQueryOptions.getSearchContent() : null,
                messageQueryOptions != null ? messageQueryOptions.getDirection() : null,
                messageQueryOptions != null ? messageQueryOptions.getContentTypes() : null,
                messageQueryOptions != null ? messageQueryOptions.getSenderUserIds() : null,
                messageQueryOptions != null ? messageQueryOptions.getStates() : null,
                messageQueryOptions != null ? messageQueryOptions.getConversations() : null,
                messageQueryOptions != null ? messageQueryOptions.getConversationTypes() : null,
                now
        );
    }

    @Override
    public List<Message> getMessagesByMessageIds(List<String> messageIdList) {
        return mCore.getDbManager().getMessagesByMessageIds(messageIdList);
    }

    @Override
    public void getMessagesByMessageIds(Conversation conversation, List<String> messageIds, IGetMessagesCallback callback) {
        if (messageIds.isEmpty()) {
            if (callback != null) {
                mCore.getCallbackHandler().post(() -> callback.onError(JErrorCode.INVALID_PARAM));
            }
            return;
        }
        List<Message> localMessages = mCore.getDbManager().getMessagesByMessageIds(messageIds);
        List<String> notExistList = new ArrayList<>();
        if (localMessages.isEmpty()) {
            notExistList = messageIds;
        } else if (localMessages.size() < messageIds.size()) {
            int localMessageIndex = 0;
            for (int i = 0; i < messageIds.size(); i++) {
                if (localMessageIndex == localMessages.size()) {
                    notExistList.add(messageIds.get(i));
                    continue;
                }
                if (messageIds.get(i).equals(localMessages.get(localMessageIndex).getMessageId())) {
                    localMessageIndex++;
                } else {
                    notExistList.add(messageIds.get(i));
                }
            }
        }
        if (notExistList.isEmpty()) {
            if (callback != null) {
                mCore.getCallbackHandler().post(() -> callback.onSuccess(localMessages));
            }
            return;
        }
        if (mCore.getWebSocket() == null) {
            int errorCode = JErrorCode.CONNECTION_UNAVAILABLE;
            JLogger.e("MSG-Get", "by id, fail, errorCode is " + errorCode);
            if (!localMessages.isEmpty()) {
                if (callback != null) {
                    mCore.getCallbackHandler().post(() -> callback.onSuccess(localMessages));
                }
            } else {
                if (callback != null) {
                    mCore.getCallbackHandler().post(() -> callback.onError(errorCode));
                }
            }
            return;
        }
        mCore.getWebSocket().queryHisMsgByIds(conversation, notExistList, new QryHisMsgCallback() {
            @Override
            public void onSuccess(List<ConcreteMessage> remoteMessages, boolean isFinished) {
                JLogger.i("MSG-Get", "by id, success");
                List<Message> result = new ArrayList<>();
                for (String messageId : messageIds) {
                    boolean isMatch = false;
                    for (Message localMessage : localMessages) {
                        if (messageId.equals(localMessage.getMessageId())) {
                            result.add(localMessage);
                            isMatch = true;
                            break;
                        }
                    }
                    if (isMatch) {
                        continue;
                    }
                    for (Message remoteMessage : remoteMessages) {
                        if (messageId.equals(remoteMessage.getMessageId())) {
                            result.add(remoteMessage);
                            break;
                        }
                    }
                }
                if (callback != null) {
                    mCore.getCallbackHandler().post(() -> callback.onSuccess(result));
                }
            }

            @Override
            public void onError(int errorCode) {
                JLogger.e("MSG-Get", "by id, fail, errorCode is " + errorCode);
                if (!localMessages.isEmpty()) {
                    if (callback != null) {
                        mCore.getCallbackHandler().post(() -> callback.onSuccess(localMessages));
                    }
                } else {
                    if (callback != null) {
                        mCore.getCallbackHandler().post(() -> callback.onError(errorCode));
                    }
                }
            }
        });
    }

    @Override
    public List<Message> getMessagesByClientMsgNos(long[] clientMsgNos) {
        return mCore.getDbManager().getMessagesByClientMsgNos(clientMsgNos);
    }

    @Override
    public void getFirstUnreadMessage(Conversation conversation, IGetMessagesCallback callback) {
        mCore.getWebSocket().getFirstUnreadMessage(conversation, new QryHisMsgCallback() {
            @Override
            public void onSuccess(List<ConcreteMessage> messages, boolean isFinished) {
                JLogger.i("MSG-FirstUnread", "success");
                if (callback != null) {
                    List<Message> result = new ArrayList<>(messages);
                    mCore.getCallbackHandler().post(() -> callback.onSuccess(result));
                }
            }

            @Override
            public void onError(int errorCode) {
                JLogger.e("MSG-FirstUnread", "error, code is " + errorCode);
                if (callback != null) {
                    mCore.getCallbackHandler().post(() -> callback.onError(errorCode));
                }
            }
        });
    }

    @Override
    public List<Message> searchMessage(String searchContent, int count, long timestamp, JIMConst.PullDirection direction) {
        return getMessages(
                count, timestamp, direction,
                new MessageQueryOptions
                        .Builder()
                        .setSearchContent(searchContent)
                        .build());
    }

    @Override
    public List<Message> searchMessage(String searchContent, int count, long timestamp, JIMConst.PullDirection direction, List<String> contentTypes) {
        return getMessages(
                count, timestamp, direction,
                new MessageQueryOptions
                        .Builder()
                        .setSearchContent(searchContent)
                        .setContentTypes(contentTypes)
                        .build());
    }

    @Override
    public List<Message> searchMessageInConversation(Conversation conversation, String searchContent, int count, long timestamp, JIMConst.PullDirection direction) {
        return getMessages(
                count, timestamp, direction,
                new MessageQueryOptions
                        .Builder()
                        .setSearchContent(searchContent)
                        .setConversations(Collections.singletonList(conversation))
                        .build());
    }

    @Override
    public void searchConversationsWithMessageContent(MessageQueryOptions options, ISearchConversationWithMessageContentCallback callback) {
        JThreadPoolExecutor.runInBackground(new Runnable() {
            @Override
            public void run() {
                long now = mCore.getCurrentTime();
                List<SearchConversationsResult> resultList = mCore.getDbManager().searchMessageInConversations(options, now);
                if (callback != null) {
                    mCore.getCallbackHandler().post(() -> callback.onComplete(resultList));
                }
            }
        });
    }

    @Override
    public void downloadMediaMessage(String messageId, IDownloadMediaMessageCallback callback) {
        mCore.getSendHandler().post(() -> {
            ConcreteMessage message = mCore.getDbManager().getMessageWithMessageId(messageId, mCore.getCurrentTime());
            if(message==null){
                if (callback != null) {
                    mCore.getCallbackHandler().post(() -> callback.onError(JErrorCode.MESSAGE_NOT_EXIST));
                }
                return;
            }
            if (!(message.getContent() instanceof MediaMessageContent)) {
                if (callback != null) {
                    mCore.getCallbackHandler().post(() -> callback.onError(JErrorCode.MESSAGE_DOWNLOAD_ERROR_NOT_MEDIA_MESSAGE));
                }
                return;
            }
            MediaMessageContent content = (MediaMessageContent) message.getContent();
            if (TextUtils.isEmpty(content.getUrl())) {
                if (callback != null) {
                    mCore.getCallbackHandler().post(() -> callback.onError(JErrorCode.MESSAGE_DOWNLOAD_ERROR_URL_EMPTY));
                }
                return;
            }
            String media = "file";
            String name = (message.getMessageId() != null ? message.getMessageId() : String.valueOf(message.getClientMsgNo())) + "_" + FileUtils.getFileNameWithPath(content.getUrl());
            if (content instanceof ImageMessage) {
                media = "image";
            } else if (content instanceof VoiceMessage) {
                media = "voice";
            } else if (content instanceof VideoMessage) {
                media = "video";
            }

            String userId = mCore.getUserId();
            String appKey = mCore.getAppKey();
            Context context = mCore.getContext();
            if (TextUtils.isEmpty(appKey) || TextUtils.isEmpty(userId)) {
                if (callback != null) {
                    mCore.getCallbackHandler().post(() -> callback.onError(JErrorCode.MESSAGE_DOWNLOAD_ERROR_APP_KEY_OR_USERID_EMPTY));
                }
                return;
            }
            String dir = appKey + "/" + userId + "/" + media;
            String savePath = FileUtils.getMediaDownloadDir(context, dir, name);
            if (TextUtils.isEmpty(savePath)) {
                if (callback != null) {
                    mCore.getCallbackHandler().post(() -> callback.onError(JErrorCode.MESSAGE_DOWNLOAD_ERROR_SAVE_PATH_EMPTY));
                }
                return;
            }
            MediaDownloadEngine.getInstance().download(message.getMessageId(), content.getUrl(), savePath, new MediaDownloadEngine.DownloadEngineCallback() {
                @Override
                public void onError(int errorCode) {
                    if (callback != null) {
                        mCore.getCallbackHandler().post(() -> callback.onError(errorCode));
                    }
                }

                @Override
                public void onComplete(String savePath) {
                    content.setLocalPath(savePath);
                    mCore.getDbManager().updateMessageContentWithMessageId(message.getContent(), message.getContentType(), message.getMessageId());
                    if (mSendReceiveListener != null) {
                        mSendReceiveListener.onMessageUpdate(message);
                    }
                    if (callback != null) {
                        mCore.getCallbackHandler().post(() -> callback.onSuccess(message));
                    }
                }

                @Override
                public void onProgress(int progress) {
                    if (callback != null) {
                        mCore.getCallbackHandler().post(() -> callback.onProgress(progress, message));
                    }
                }

                @Override
                public void onCanceled(String tag) {
                    if (callback != null) {
                        mCore.getCallbackHandler().post(() -> callback.onCancel(message));
                    }
                }
            });
        });
    }

    public void cancelDownloadMediaMessage(String messageId) {
        MediaDownloadEngine.getInstance().cancel(messageId);
    }

    @Override
    public List<Message> searchMessageInConversation(Conversation conversation, String searchContent, int count, long timestamp, JIMConst.PullDirection direction, List<String> contentTypes) {
        return getMessages(
                count, timestamp, direction,
                new MessageQueryOptions
                        .Builder()
                        .setSearchContent(searchContent)
                        .setConversations(Collections.singletonList(conversation))
                        .setContentTypes(contentTypes)
                        .build());
    }

    @Override
    public void deleteMessagesByMessageIdList(Conversation conversation, List<String> messageIds, boolean forAllUsers, ISimpleCallback callback) {
        //判空
        if (conversation == null || messageIds == null || messageIds.isEmpty()) {
            if (callback != null) {
                mCore.getCallbackHandler().post(() -> callback.onError(JErrorCode.MESSAGE_NOT_EXIST));
            }
            return;
        }
        //查询消息
        List<Message> messages = getMessagesByMessageIds(messageIds);
        //按conversation过滤
        List<String> deleteIdList = new ArrayList<>();
        List<ConcreteMessage> deleteList = new ArrayList<>();
        List<Long> clientMsgNoList = new ArrayList<>();
        for (int i = messages.size() - 1; i >= 0; i--) {
            Message temp = messages.get(i);
            if (temp.getConversation().equals(conversation)) {
                deleteIdList.add(temp.getMessageId());
                deleteList.add((ConcreteMessage) temp);
                clientMsgNoList.add(temp.getClientMsgNo());
            }
        }
        //判空
        if (deleteList.isEmpty()) {
            if (callback != null) {
                mCore.getCallbackHandler().post(() -> callback.onError(JErrorCode.MESSAGE_NOT_EXIST));
            }
            return;
        }
        JLogger.i("MSG-Delete", "by messageId, count is " + deleteList.size());
        if (mCore.getWebSocket() == null) {
            int errorCode = JErrorCode.CONNECTION_UNAVAILABLE;
            JLogger.e("MSG-Delete", "by messageId, fail, code is " + errorCode);
            if (callback != null) {
                mCore.getCallbackHandler().post(() -> callback.onError(errorCode));
            }
            return;
        }
        //调用接口
        mCore.getWebSocket().deleteMessage(conversation, deleteList, forAllUsers, new WebSocketTimestampCallback() {
            @Override
            public void onSuccess(long timestamp) {
                JLogger.i("MSG-Delete", "by messageId, success");
                updateMessageSendSyncTime(timestamp);
                //删除消息
                mCore.getDbManager().deleteMessagesByMessageIds(deleteIdList);
                //通知会话更新
                notifyMessageRemoved(conversation, deleteList);
                //执行回调
                mCore.getCallbackHandler().post(() -> {
                    if (callback != null) {
                        callback.onSuccess();
                    }
                    if (mListenerMap != null) {
                        for (Map.Entry<String, IMessageListener> entry : mListenerMap.entrySet()) {
                            entry.getValue().onMessageDelete(conversation, clientMsgNoList);
                        }
                    }
                });
            }

            @Override
            public void onError(int errorCode) {
                JLogger.e("MSG-Delete", "by messageId, fail, code is " + errorCode);
                if (callback != null) {
                    mCore.getCallbackHandler().post(() -> callback.onError(errorCode));
                }
            }
        });
    }

    @Override
    public void deleteMessagesByMessageIdList(Conversation conversation, List<String> messageIds, ISimpleCallback callback) {
        deleteMessagesByMessageIdList(conversation, messageIds, false, callback);
    }

    @Override
    public void deleteMessagesByClientMsgNoList(Conversation conversation, List<Long> clientMsgNos, boolean forAllUsers, ISimpleCallback callback) {
        //判空
        if (conversation == null || clientMsgNos == null || clientMsgNos.isEmpty()) {
            if (callback != null) {
                mCore.getCallbackHandler().post(() -> callback.onError(JErrorCode.MESSAGE_NOT_EXIST));
            }
            return;
        }
        //查询消息
        long[] clientMsgNoArray = new long[clientMsgNos.size()];
        for (int i = 0; i < clientMsgNos.size(); i++) {
            clientMsgNoArray[i] = clientMsgNos.get(i);
        }
        List<Message> messages = getMessagesByClientMsgNos(clientMsgNoArray);
        //按conversation过滤，分为本地处理列表及接口处理列表
        List<Long> deleteClientMsgNoList = new ArrayList<>();
        List<ConcreteMessage> deleteLocalList = new ArrayList<>();
        List<ConcreteMessage> deleteRemoteList = new ArrayList<>();
        for (int i = messages.size() - 1; i >= 0; i--) {
            Message temp = messages.get(i);
            if (!temp.getConversation().equals(conversation)) continue;

            deleteClientMsgNoList.add(temp.getClientMsgNo());
            if (TextUtils.isEmpty(temp.getMessageId())) {
                deleteLocalList.add((ConcreteMessage) temp);
            } else {
                deleteRemoteList.add((ConcreteMessage) temp);
            }
        }
        //判空
        if (deleteClientMsgNoList.isEmpty()) {
            if (callback != null) {
                mCore.getCallbackHandler().post(() -> callback.onError(JErrorCode.MESSAGE_NOT_EXIST));
            }
            return;
        }
        JLogger.i("MSG-Delete", "by clientMsgNo, local count is " + deleteLocalList.size() + ", remote count is " + deleteRemoteList);
        //所有消息均为仅本地保存的消息，不需要调用接口
        if (deleteRemoteList.isEmpty()) {
            //删除消息
            mCore.getDbManager().deleteMessageByClientMsgNo(deleteClientMsgNoList);
            //通知会话更新
            notifyMessageRemoved(conversation, deleteLocalList);
            mCore.getCallbackHandler().post(() -> {
                if (callback != null) {
                    callback.onSuccess();
                }
                if (mListenerMap != null) {
                    for (Map.Entry<String, IMessageListener> entry : mListenerMap.entrySet()) {
                        entry.getValue().onMessageDelete(conversation, deleteClientMsgNoList);
                    }
                }
            });
            return;
        }
        //判空
        if (mCore.getWebSocket() == null) {
            int errorCode = JErrorCode.CONNECTION_UNAVAILABLE;
            JLogger.e("MSG-Delete", "by clientMsgNo, fail, code is " + errorCode);
            if (callback != null) {
                mCore.getCallbackHandler().post(() -> callback.onError(errorCode));
            }
            return;
        }
        //调用接口
        mCore.getWebSocket().deleteMessage(conversation, deleteRemoteList, forAllUsers, new WebSocketTimestampCallback() {
            @Override
            public void onSuccess(long timestamp) {
                JLogger.i("MSG-Delete", "by clientMsgNo, success");
                updateMessageSendSyncTime(timestamp);
                //删除消息
                mCore.getDbManager().deleteMessageByClientMsgNo(deleteClientMsgNoList);
                //通知会话更新
                deleteLocalList.addAll(deleteRemoteList);
                notifyMessageRemoved(conversation, deleteLocalList);
                //执行回调
                mCore.getCallbackHandler().post(() -> {
                    if (callback != null) {
                        callback.onSuccess();
                    }
                    if (mListenerMap != null) {
                        for (Map.Entry<String, IMessageListener> entry : mListenerMap.entrySet()) {
                            entry.getValue().onMessageDelete(conversation, deleteClientMsgNoList);
                        }
                    }
                });
            }

            @Override
            public void onError(int errorCode) {
                JLogger.e("MSG-Delete", "by clientMsgNo, fail, code is " + errorCode);
                if (callback != null) {
                    mCore.getCallbackHandler().post(() -> callback.onError(errorCode));
                }
            }
        });
    }

    @Override
    public void deleteMessagesByClientMsgNoList(Conversation conversation, List<Long> clientMsgNos, ISimpleCallback callback) {
        deleteMessagesByClientMsgNoList(conversation, clientMsgNos, false, callback);
    }

    @Override
    public void clearMessages(Conversation conversation, long startTime, boolean forAllUsers, ISimpleCallback callback) {
        //判空
        if (mCore.getWebSocket() == null) {
            int errorCode = JErrorCode.CONNECTION_UNAVAILABLE;
            JLogger.e("MSG-Clear", "fail, code is " + errorCode);
            if (callback != null) {
                mCore.getCallbackHandler().post(() -> callback.onError(errorCode));
            }
            return;
        }
        //startTime
        if (startTime <= 0) {
            startTime = Math.max(mCore.getMessageSendSyncTime(), mCore.getMessageReceiveTime());
            startTime = Math.max(System.currentTimeMillis(), startTime);
        }
        //调用接口
        long finalStartTime = startTime;
        mCore.getWebSocket().clearHistoryMessage(conversation, finalStartTime, forAllUsers, new WebSocketTimestampCallback() {
            @Override
            public void onSuccess(long timestamp) {
                JLogger.i("MSG-Clear", "success");
                updateMessageSendSyncTime(timestamp);
                //清空消息
                mCore.getDbManager().clearMessages(conversation, finalStartTime, null);
                //通知会话更新
                notifyMessageCleared(conversation, finalStartTime, null);
                //执行回调
                mCore.getCallbackHandler().post(() -> {
                    if (callback != null) {
                        callback.onSuccess();
                    }
                    if (mListenerMap != null) {
                        for (Map.Entry<String, IMessageListener> entry : mListenerMap.entrySet()) {
                            entry.getValue().onMessageClear(conversation, finalStartTime, "");
                        }
                    }
                });
            }

            @Override
            public void onError(int errorCode) {
                JLogger.e("MSG-Clear", "fail, code is " + errorCode);
                if (callback != null) {
                    mCore.getCallbackHandler().post(() -> callback.onError(errorCode));
                }
            }
        });
    }

    @Override
    public void clearMessages(Conversation conversation, long startTime, ISimpleCallback callback) {
        clearMessages(conversation, startTime, false, callback);
    }

    @Override
    public void recallMessage(String messageId, Map<String, String> extras, IRecallMessageCallback callback) {
        if (messageId == null || messageId.isEmpty()) {
            if (callback != null) {
                mCore.getCallbackHandler().post(() -> callback.onError(JErrorCode.INVALID_PARAM));
            }
            return;
        }

        List<String> idList = new ArrayList<>(1);
        idList.add(messageId);
        List<Message> messages = getMessagesByMessageIds(idList);
        if (messages.isEmpty()) {
            int errorCode = JErrorCode.MESSAGE_NOT_EXIST;
            JLogger.e("MSG-Recall", "fail, code is " + errorCode);
            if (callback != null) {
                mCore.getCallbackHandler().post(() -> callback.onError(errorCode));
            }
            return;
        }
        Message m = messages.get(0);
        if (m.getContentType().equals(RecallInfoMessage.CONTENT_TYPE)) {
            int errorCode = JErrorCode.MESSAGE_ALREADY_RECALLED;
            JLogger.e("MSG-Recall", "fail, code is " + errorCode);
            if (callback != null) {
                mCore.getCallbackHandler().post(() -> callback.onError(errorCode));
            }
            return;
        }
        if (mCore.getWebSocket() == null) {
            int errorCode = JErrorCode.CONNECTION_UNAVAILABLE;
            JLogger.e("MSG-Recall", "fail, code is " + errorCode);
            if (callback != null) {
                mCore.getCallbackHandler().post(() -> callback.onError(errorCode));
            }
            return;
        }
        mCore.getWebSocket().recallMessage(messageId, m.getConversation(), m.getTimestamp(), extras, new WebSocketTimestampCallback() {
            @Override
            public void onSuccess(long timestamp) {
                JLogger.i("MSG-Recall", "success");
                updateMessageSendSyncTime(timestamp);
                m.setContentType(RecallInfoMessage.CONTENT_TYPE);
                RecallInfoMessage recallInfoMessage = new RecallInfoMessage();
                recallInfoMessage.setExtra(extras);
                m.setContent(recallInfoMessage);
                mCore.getDbManager().updateMessageContentWithMessageId(recallInfoMessage, m.getContentType(), messageId);
                //通知会话更新
                List<ConcreteMessage> messageList = new ArrayList<>();
                messageList.add((ConcreteMessage) m);
                notifyMessageRemoved(m.getConversation(), messageList);
                mCore.getCallbackHandler().post(() -> {
                    if (callback != null) {
                        callback.onSuccess(m);
                    }
                    if (mListenerMap != null) {
                        for (Map.Entry<String, IMessageListener> entry : mListenerMap.entrySet()) {
                            entry.getValue().onMessageRecall(m);
                        }
                    }
                });
            }

            @Override
            public void onError(int errorCode) {
                JLogger.e("MSG-Recall", "fail, code is " + errorCode);
                if (callback != null) {
                    mCore.getCallbackHandler().post(() -> callback.onError(errorCode));
                }
            }
        });
    }

    @Override
    public void updateMessage(String messageId, MessageContent content, Conversation conversation, IMessageCallback callback) {
        if (messageId == null || messageId.isEmpty()
        || content == null
        || conversation == null
        || conversation.getConversationId() == null
        || conversation.getConversationId().isEmpty()) {
            JLogger.e("MSG-Update", "invalid parameter");
            if (callback != null) {
                mCore.getCallbackHandler().post(() -> callback.onError(JErrorCode.INVALID_PARAM));
            }
            return;
        }

        List<String> idList = new ArrayList<>(1);
        idList.add(messageId);
        List<Message> messages = getMessagesByMessageIds(idList);
        if (messages.isEmpty()) {
            int errorCode = JErrorCode.MESSAGE_NOT_EXIST;
            JLogger.e("MSG-Update", "fail, code is " + errorCode);
            if (callback != null) {
                mCore.getCallbackHandler().post(() -> callback.onError(errorCode));
            }
            return;
        }
        if (mCore.getWebSocket() == null) {
            int errorCode = JErrorCode.CONNECTION_UNAVAILABLE;
            JLogger.e("MSG-Update", "fail, code is " + errorCode);
            if (callback != null) {
                mCore.getCallbackHandler().post(() -> callback.onError(errorCode));
            }
            return;
        }
        ConcreteMessage m = (ConcreteMessage) messages.get(0);
        mCore.getWebSocket().updateMessage(messageId, content, conversation, m.getTimestamp(), m.getSeqNo(), new WebSocketTimestampCallback() {
            @Override
            public void onSuccess(long timestamp) {
                JLogger.i("MSG-Update", "success");
                updateMessageSendSyncTime(timestamp);
                if (content instanceof UnknownMessage) {
                    UnknownMessage unknown = (UnknownMessage) content;
                    m.setContentType(unknown.getMessageType());
                } else {
                    m.setContentType(content.getContentType());
                }
                m.setContent(content);
                mCore.getDbManager().updateMessageContentWithMessageId(content, m.getContentType(), messageId);
                int flags = m.getFlags() | MessageContent.MessageFlag.IS_MODIFIED.getValue();
                m.setFlags(flags);
                m.setEdit(true);
                mCore.getDbManager().setMessageFlags(messageId, flags);

                if (mSendReceiveListener != null) {
                    mSendReceiveListener.onMessageUpdate(m);
                }
                mCore.getCallbackHandler().post(() -> {
                    if (callback != null) {
                        callback.onSuccess(m);
                    }
                    if (mListenerMap != null) {
                        for (Map.Entry<String, IMessageListener> entry : mListenerMap.entrySet()) {
                            entry.getValue().onMessageUpdate(m);
                        }
                    }
                });
            }

            @Override
            public void onError(int errorCode) {
                JLogger.e("MSG-Update", "fail, code is " + errorCode);
                if (callback != null) {
                    mCore.getCallbackHandler().post(() -> callback.onError(errorCode));
                }
            }
        });
    }

    @Override
    public void getRemoteMessages(Conversation conversation, int count, long startTime, JIMConst.PullDirection direction, IGetMessagesWithFinishCallback callback) {
        getRemoteMessages(conversation, count, startTime, direction, null, callback);
    }

    private void internalGetRemoteMessages(Conversation conversation,
                                           int count,
                                           long startTime,
                                           JIMConst.PullDirection direction,
                                           List<String> contentTypes,
                                           IGetMessagesWithFinishCallback callback) {
        if (mCore.getWebSocket() == null) {
            int errorCode = JErrorCode.CONNECTION_UNAVAILABLE;
            JLogger.e("MSG-Get", "getRemoteMessages, fail, errorCode is " + errorCode);
            if (callback != null) {
                mCore.getCallbackHandler().post(() -> callback.onError(errorCode));
            }
            return;
        }
        mCore.getWebSocket().queryHisMsg(conversation, startTime, count, direction, contentTypes, new QryHisMsgCallback() {
            @Override
            public void onSuccess(List<ConcreteMessage> messages, boolean isFinished) {
                JLogger.i("MSG-Get", "getRemoteMessages, success");
                insertRemoteMessages(messages);
                if (callback != null) {
                    List<Message> result = new ArrayList<>(messages);
                    mCore.getCallbackHandler().post(() -> callback.onSuccess(result, isFinished));
                }
            }

            @Override
            public void onError(int errorCode) {
                JLogger.e("MSG-Get", "getRemoteMessages, fail, errorCode is " + errorCode);
                if (callback != null) {
                    mCore.getCallbackHandler().post(() -> callback.onError(errorCode));
                }
            }
        });
    }

    public void getRemoteMessages(Conversation conversation,
                                  int count,
                                  long startTime,
                                  JIMConst.PullDirection direction,
                                  List<String> contentTypes,
                                  IGetMessagesWithFinishCallback callback) {
        if (count > 100) {
            count = 100;
        }
        internalGetRemoteMessages(conversation, count, startTime, direction, contentTypes, callback);
    }

    @Override
    public void getLocalAndRemoteMessages(Conversation conversation, int count, long startTime, JIMConst.PullDirection direction, IGetLocalAndRemoteMessagesCallback callback) {
        if (count <= 0) {
            if (callback != null) {
                mCore.getCallbackHandler().post(() -> callback.onGetLocalList(new ArrayList<>(), false));
            }
            return;
        }
        if (count > 100) {
            count = 100;
        }
        List<Message> localMessages = getMessages(conversation, count, startTime, direction);
        //如果本地消息为空，需要获取远端消息
        boolean needRemote = localMessages == null || localMessages.isEmpty();
        if (!needRemote) {
            //获取本地消息列表中首条消息
            long firstMessageSeqNo = ((ConcreteMessage) localMessages.get(0)).getSeqNo();
            //判断是否需要获取远端消息
            needRemote = isRemoteMessagesNeeded(localMessages, count, firstMessageSeqNo);
        }
        if (callback != null) {
            boolean finalNeedRemote = needRemote;
            mCore.getCallbackHandler().post(() -> callback.onGetLocalList(localMessages, finalNeedRemote));
        }
        if (needRemote) {
            getRemoteMessages(conversation, count, startTime, direction, new IGetMessagesWithFinishCallback() {
                @Override
                public void onSuccess(List<Message> messages, boolean isFinished) {
                    JLogger.i("MSG-Get", "getLocalAndRemoteMessages, success");
                    //合并去重
                    List<Message> mergeList = mergeLocalAndRemoteMessages(localMessages == null ? new ArrayList<>() : localMessages, messages);
                    //消息排序
                    Collections.sort(mergeList, (o1, o2) -> Long.compare(o1.getTimestamp(), o2.getTimestamp()));
                    //返回合并后的消息列表
                    if (callback != null) {
                        mCore.getCallbackHandler().post(() -> callback.onGetRemoteList(mergeList));
                    }
                }

                @Override
                public void onError(int errorCode) {
                    JLogger.e("MSG-Get", "getLocalAndRemoteMessages, fail, errorCode is " + errorCode);
                    if (callback != null) {
                        mCore.getCallbackHandler().post(() -> callback.onGetRemoteListError(errorCode));
                    }
                }
            });
        }
    }

    @Override
    public void getMessages(Conversation conversation, JIMConst.PullDirection direction, GetMessageOptions options, IGetMessagesCallbackV2 callback) {
        if (conversation == null || conversation.getConversationId().isEmpty()) {
            if (callback != null) {
                mCore.getCallbackHandler().post(() -> {
                    callback.onGetLocalMessages(new ArrayList<>(), JErrorCode.INVALID_PARAM);
                    callback.onGetRemoteMessages(new ArrayList<>(), 0, false, JErrorCode.INVALID_PARAM);
                });
            }
            return;
        }
        if (options == null) {
            options = new GetMessageOptions();
        }
        if (options.getCount() <= 0 || options.getCount() > 100) {
            options.setCount(100);
        }
        List<Message> localMessages = getMessages(conversation, options.getCount(), options.getStartTime(), direction, options.getContentTypes());

        if (callback != null) {
            mCore.getCallbackHandler().post(() -> callback.onGetLocalMessages(localMessages, JErrorCode.NONE));
        }

        getRemoteMessages(conversation, options.getCount(), options.getStartTime(), direction, options.getContentTypes(), new IGetMessagesWithFinishCallback() {
            @Override
            public void onSuccess(List<Message> messages, boolean isFinished) {
                JLogger.i("MSG-Get", "get messages, success");
                long timestamp;
                if (messages != null && !messages.isEmpty()) {
                    Message m;
                    if (direction == JIMConst.PullDirection.NEWER) {
                        m = messages.get(messages.size() - 1);
                    } else {
                        m = messages.get(0);
                    }
                    timestamp = m.getTimestamp();
                } else {
                    timestamp = 0;
                }
                if (callback != null) {
                    mCore.getCallbackHandler().post(() -> callback.onGetRemoteMessages(messages, timestamp, !isFinished, JErrorCode.NONE));
                }
            }

            @Override
            public void onError(int errorCode) {
                JLogger.e("MSG-Get", "get messages fail, errorCode is " + errorCode);
                if (callback != null) {
                    mCore.getCallbackHandler().post(() -> callback.onGetRemoteMessages(new ArrayList<>(), 0, false, errorCode));
                }
            }
        });
    }

    @Override
    public void getMessages(Conversation conversation, JIMConst.PullDirection direction, GetMessageOptions options, IGetMessagesCallbackV3 callback) {
        if (conversation == null || conversation.getConversationId().isEmpty()) {
            if (callback != null) {
                mCore.getCallbackHandler().post(() -> {
                    callback.onGetMessages(new ArrayList<>(), 0, false, JErrorCode.INVALID_PARAM);
                });
            }
            return;
        }
        if (options == null) {
            options = new GetMessageOptions();
        }
        final int count;
        if (options.getCount() <= 0 || options.getCount() > 100) {
            count = 100;
        } else {
            count = options.getCount();
        }
        List<Message> localMessages = mCore.getDbManager().getMessages(
                count+1,
                options.getStartTime(),
                direction,
                null,
                null,
                options.getContentTypes(),
                null,
                null,
                Collections.singletonList(conversation),
                null,
                mCore.getCurrentTime());

        boolean needRemote = false;
        if (localMessages.size() < count+1) {
            needRemote = true;
        } else {
            long seqNo = -1;
            for (int i = 0; i < localMessages.size(); i++) {
                ConcreteMessage m = (ConcreteMessage) localMessages.get(i);
                if (m.getSeqNo() < 0) {
                    continue;
                }
                if (m.getState() == Message.MessageState.SENT && m.getSeqNo() > 0) {
                    if (seqNo < 0) {
                        seqNo = m.getSeqNo();
                    } else {
                        if (m.getSeqNo() != ++seqNo) {
                            needRemote = true;
                            break;
                        }
                    }
                }
            }
            if (!needRemote && options.getStartTime() == 0 && direction == JIMConst.PullDirection.OLDER) {
                ConversationInfo conversationInfo = mCore.getDbManager().getConversationInfo(conversation);
                if (conversationInfo != null) {
                    ConcreteMessage conversationLastMessage = (ConcreteMessage) conversationInfo.getLastMessage();
                    ConcreteMessage localListLastMessage = (ConcreteMessage) localMessages.get(localMessages.size()-1);
                    if (conversationLastMessage != null && conversationLastMessage.getSeqNo() > localListLastMessage.getSeqNo()) {
                        needRemote = true;
                    }
                }
            }
        }

        long startTime = options.getStartTime();
        if (needRemote) {
            internalGetRemoteMessages(conversation, count + 1, startTime, direction, options.getContentTypes(), new IGetMessagesWithFinishCallback() {
                @Override
                public void onSuccess(List<Message> messages, boolean isFinished) {
                    //合并去重
                    List<Message> mergeList = mergeLocalAndRemoteMessages(localMessages, messages);
                    //消息排序
                    Collections.sort(mergeList, (o1, o2) -> Long.compare(o1.getTimestamp(), o2.getTimestamp()));
                    completeCallbackForGetMessages(mergeList, count, direction, !isFinished, JErrorCode.NONE, startTime, callback);
                }

                @Override
                public void onError(int errorCode) {
                    boolean hasMore = localMessages.size() >= count + 1;
                    completeCallbackForGetMessages(localMessages, count, direction, hasMore, errorCode, startTime, callback);
                }
            });
        } else {
            boolean hasMore = localMessages.size() >= count + 1;
            completeCallbackForGetMessages(localMessages, count, direction, hasMore, JErrorCode.NONE, startTime, callback);
        }

    }

    private void completeCallbackForGetMessages(List<Message> messages, int count, JIMConst.PullDirection direction, boolean hasMore, int code, long getMessageTime, IGetMessagesCallbackV3 callback) {
        if (messages.size() > count) {
            if (direction == JIMConst.PullDirection.NEWER) {
                messages = messages.subList(0, count);
            } else {
                messages = messages.subList(messages.size() - count, messages.size());
            }
        }
        List<Message> finalMessages = messages;
        if (finalMessages.isEmpty()) {
            if (callback != null) {
                mCore.getCallbackHandler().post(() -> {
                    callback.onGetMessages(finalMessages, getMessageTime, hasMore, code);
                });
            }
            return;
        }
        Message baseMessage;
        if (direction == JIMConst.PullDirection.NEWER) {
            baseMessage = messages.get(messages.size() - 1);
        } else {
            baseMessage = messages.get(0);
        }
        long timestamp = baseMessage.getTimestamp();
        if (callback != null) {
            mCore.getCallbackHandler().post(() -> {
                callback.onGetMessages(finalMessages, timestamp, hasMore, code);
            });
        }
    }

    //判断是否需要同步远端数据
    private boolean isRemoteMessagesNeeded(List<Message> localMessages, int count, long firstMessageSeqNo) {
        //如果本地消息数量不满足分页需求数量，需要获取远端消息
        if (localMessages.size() < count) {
            return true;
        }
        //判断本地列表中的消息是否连续，不连续时需要获取远端消息
        long expectedSeqNo = firstMessageSeqNo;
        for (int i = 0; i < localMessages.size(); i++) {
            if (i == 0) continue;
            ConcreteMessage m = (ConcreteMessage) localMessages.get(i);
            if (Message.MessageState.SENT == m.getState() && m.getSeqNo() != 0) {
                if (m.getSeqNo() > ++expectedSeqNo) {
                    return true;
                }
            }
        }
        return false;
    }

    //合并localList和remoteList并去重
    private List<Message> mergeLocalAndRemoteMessages(List<Message> localList, List<Message> remoteList) {
        List<Message> mergedList = new ArrayList<>(remoteList);
        for (Message localMessage : localList) {
            boolean isContain = false;
            for (Message remoteMessage : mergedList) {
                if (localMessage.getClientMsgNo() == remoteMessage.getClientMsgNo()) {
                    if (localMessage.getContent().getClass() == remoteMessage.getContent().getClass()) {
                        remoteMessage.setLocalAttribute(localMessage.getLocalAttribute());
                    }
                    if (localMessage.getContent() instanceof ImageMessage
                    && remoteMessage.getContent() instanceof  ImageMessage) {
                        ImageMessage localImage = (ImageMessage) localMessage.getContent();
                        ImageMessage remoteImage = (ImageMessage) remoteMessage.getContent();
                        remoteImage.setThumbnailLocalPath(localImage.getThumbnailLocalPath());
                        remoteImage.setLocalPath(localImage.getLocalPath());
                    } else if (localMessage.getContent() instanceof VideoMessage
                    && remoteMessage.getContent() instanceof VideoMessage) {
                        VideoMessage localVideo = (VideoMessage) localMessage.getContent();
                        VideoMessage remoteVideo = (VideoMessage) remoteMessage.getContent();
                        remoteVideo.setLocalPath(localVideo.getLocalPath());
                        remoteVideo.setSnapshotLocalPath(localVideo.getSnapshotLocalPath());
                    } else if (localMessage.getContent() instanceof MediaMessageContent
                    && remoteMessage.getContent() instanceof MediaMessageContent) {
                        MediaMessageContent localContent = (MediaMessageContent) localMessage.getContent();
                        MediaMessageContent remoteContent = (MediaMessageContent) remoteMessage.getContent();
                        remoteContent.setLocalPath(localContent.getLocalPath());
                    }
                    isContain = true;
                    break;
                }
            }
            if (!isContain) {
                mergedList.add(localMessage);
            }
        }
        return mergedList;
    }

    @Override
    public void sendReadReceipt(Conversation conversation, List<String> messageIds, ISendReadReceiptCallback callback) {
        if (mCore.getWebSocket() == null) {
            int errorCode = JErrorCode.CONNECTION_UNAVAILABLE;
            JLogger.e("MSG-ReadReceipt", "sendReadReceipt, fail, errorCode is " + errorCode);
            if (callback != null) {
                mCore.getCallbackHandler().post(() -> callback.onError(errorCode));
            }
            return;
        }
        mCore.getWebSocket().sendReadReceipt(conversation, messageIds, new WebSocketTimestampCallback() {
            @Override
            public void onSuccess(long timestamp) {
                JLogger.i("MSG-ReadReceipt", "sendReadReceipt, success");
                updateMessageSendSyncTime(timestamp);
                mCore.getDbManager().setMessagesRead(messageIds);
                if (callback != null) {
                    mCore.getCallbackHandler().post(callback::onSuccess);
                }
                if (mSendReceiveListener != null) {
                    mSendReceiveListener.onMessagesRead(conversation, messageIds);
                }
                List<Message> readMessages = mCore.getDbManager().getMessagesByMessageIds(messageIds);
                for (Message readMessage : readMessages) {
                    checkAndNotifyDestroyTime(readMessage, timestamp);
                }
            }

            @Override
            public void onError(int errorCode) {
                JLogger.e("MSG-ReadReceipt", "sendReadReceipt, fail, errorCode is " + errorCode);
                if (callback != null) {
                    mCore.getCallbackHandler().post(() -> callback.onError(errorCode));
                }
            }
        });
    }

    @Override
    public void getGroupMessageReadDetail(Conversation conversation, String messageId, IGetGroupMessageReadDetailCallback callback) {
        if (mCore.getWebSocket() == null) {
            int errorCode = JErrorCode.CONNECTION_UNAVAILABLE;
            JLogger.e("MSG-GroupReadDetail", "fail, errorCode is " + errorCode);
            if (callback != null) {
                mCore.getCallbackHandler().post(() -> callback.onError(errorCode));
            }
            return;
        }
        mCore.getWebSocket().getGroupMessageReadDetail(conversation, messageId, new QryReadDetailCallback() {
            @Override
            public void onSuccess(List<UserInfo> readMembers, List<UserInfo> unreadMembers) {
                JLogger.i("MSG-GroupReadDetail", "success");
                GroupMessageReadInfo info = new GroupMessageReadInfo();
                info.setReadCount(readMembers.size());
                info.setMemberCount(readMembers.size() + unreadMembers.size());
                mCore.getDbManager().setGroupMessageReadInfo(new HashMap<String, GroupMessageReadInfo>() {
                    {
                        put(messageId, info);
                    }
                });
                if (callback != null) {
                    mCore.getCallbackHandler().post(() -> callback.onSuccess(readMembers, unreadMembers));
                }
            }

            @Override
            public void onError(int errorCode) {
                JLogger.e("MSG-GroupReadDetail", "fail, errorCode is " + errorCode);
                if (callback != null) {
                    mCore.getCallbackHandler().post(() -> callback.onError(errorCode));
                }
            }
        });
    }

    @Override
    public void getMergedMessageList(String containerMsgId, IGetMessagesCallback callback) {
        if (mCore.getWebSocket() == null) {
            int errorCode = JErrorCode.CONNECTION_UNAVAILABLE;
            JLogger.e("MSG-GetMerge", "fail, code is " + errorCode);
            if (callback != null) {
                mCore.getCallbackHandler().post(() -> callback.onError(errorCode));
            }
            return;
        }
        mCore.getWebSocket().getMergedMessageList(containerMsgId, 0, 100, JIMConst.PullDirection.OLDER, new QryHisMsgCallback() {
            @Override
            public void onSuccess(List<ConcreteMessage> messages, boolean isFinished) {
                JLogger.i("MSG-GetMerge", "success");
                insertRemoteMessages(messages);
                if (callback != null) {
                    List<Message> result = new ArrayList<>(messages);
                    mCore.getCallbackHandler().post(() -> callback.onSuccess(result));
                }
            }

            @Override
            public void onError(int errorCode) {
                JLogger.e("MSG-GetMerge", "fail, code is " + errorCode);
                if (callback != null) {
                    mCore.getCallbackHandler().post(() -> callback.onError(errorCode));
                }
            }
        });
    }

    @Override
    public void getMentionMessageList(Conversation conversation, int count, long time, JIMConst.PullDirection direction, IGetMessagesWithFinishCallback callback) {
        if (mCore.getWebSocket() == null) {
            int errorCode = JErrorCode.CONNECTION_UNAVAILABLE;
            JLogger.e("MSG-GetMention", "fail, code is " + errorCode);
            if (callback != null) {
                mCore.getCallbackHandler().post(() -> callback.onError(errorCode));
            }
            return;
        }
        ConcreteConversationInfo conversationInfo = mCore.getDbManager().getConversationInfo(conversation);
        mCore.getWebSocket().getMentionMessageList(conversation, time, count, direction, conversationInfo.getLastReadMessageIndex(), new QryHisMsgCallback() {
            @Override
            public void onSuccess(List<ConcreteMessage> messages, boolean isFinished) {
                JLogger.i("MSG-GetMention", "success");
                insertRemoteMessages(messages);
                if (callback != null) {
                    List<Message> result = new ArrayList<>(messages);
                    mCore.getCallbackHandler().post(() -> callback.onSuccess(result, isFinished));
                }
            }

            @Override
            public void onError(int errorCode) {
                JLogger.e("MSG-GetMention", "fail, code is " + errorCode);
                if (callback != null) {
                    mCore.getCallbackHandler().post(() -> callback.onError(errorCode));
                }
            }
        });
    }

    @Override
    public void setLocalAttribute(String messageId, String attribute) {
        mCore.getDbManager().updateLocalAttribute(messageId, attribute);
    }

    @Override
    public String getLocalAttribute(String messageId) {
        return mCore.getDbManager().getLocalAttribute(messageId);
    }

    @Override
    public void setLocalAttribute(long clientMsgNo, String attribute) {
        mCore.getDbManager().updateLocalAttribute(clientMsgNo, attribute);
    }

    @Override
    public String getLocalAttribute(long clientMsgNo) {
        return mCore.getDbManager().getLocalAttribute(clientMsgNo);
    }

    @Override
    public void broadcastMessage(MessageContent content, List<Conversation> conversations, IBroadcastMessageCallback callback) {
        if (conversations.isEmpty()) {
            if (callback != null) {
                mCore.getCallbackHandler().post(callback::onComplete);
            }
            return;
        }
        loopBroadcastMessage(content, conversations, 0, conversations.size(), callback);
    }

    @Override
    public void addMessageReaction(String messageId, Conversation conversation, String reactionId, ISimpleCallback callback) {
        if (messageId == null || messageId.isEmpty()
        || conversation == null || conversation.getConversationId().isEmpty()
        || reactionId == null || reactionId.isEmpty()) {
            JLogger.e("MSG-ReactionAdd", "invalid parameter");
            if (callback != null) {
                mCore.getCallbackHandler().post(() -> callback.onError(JErrorCode.INVALID_PARAM));
            }
            return;
        }
        if (mCore.getWebSocket() == null) {
            int errorCode = JErrorCode.CONNECTION_UNAVAILABLE;
            JLogger.e("MSG-ReactionAdd", "fail, code is " + errorCode);
            if (callback != null) {
                mCore.getCallbackHandler().post(() -> callback.onError(errorCode));
            }
            return;
        }
        mCore.getWebSocket().addMessageReaction(messageId, conversation, reactionId, mCore.getUserId(), new WebSocketTimestampCallback() {
            @Override
            public void onSuccess(long timestamp) {
                JLogger.i("MSG-ReactionAdd", "success");
                mCore.getCallbackHandler().post(() -> {
                    if (callback != null) {
                        callback.onSuccess();
                    }
                    UserInfo currentUser = mUserInfoManager.getUserInfo(mCore.getUserId());
                    if (currentUser == null) {
                        currentUser = new UserInfo();
                        currentUser.setUserId(mCore.getUserId());
                    }

                    //callback delegate
                    //callback 只有新增的，不用本地做合并，因为本地不全（特别是收到别的用户的 reaction 时，不能返回不全的数据）
                    MessageReaction reaction = new MessageReaction();
                    reaction.setMessageId(messageId);
                    MessageReactionItem it = new MessageReactionItem();
                    it.setReactionId(reactionId);
                    it.setUserInfoList(Collections.singletonList(currentUser));
                    reaction.setItemList(Collections.singletonList(it));

                    //update reaction db
                    List<MessageReaction> dbReactions = mCore.getDbManager().getMessageReactions(Collections.singletonList(messageId));
                    if (!dbReactions.isEmpty()) {
                        MessageReaction dbReaction = dbReactions.get(0);
                        for (MessageReactionItem item : dbReaction.getItemList()) {
                            if (reactionId.equals(item.getReactionId())) {
                                List<UserInfo> userInfoList = item.getUserInfoList();
                                userInfoList.add(currentUser);
                                item.setUserInfoList(userInfoList);
                                break;
                            }
                        }
                        mCore.getDbManager().setMessageReactions(Collections.singletonList(dbReaction));
                    } else {
                        mCore.getDbManager().setMessageReactions(Collections.singletonList(reaction));
                    }

                    if (mListenerMap != null) {
                        for (Map.Entry<String, IMessageListener> entry : mListenerMap.entrySet()) {
                            entry.getValue().onMessageReactionAdd(conversation, reaction);
                        }
                    }
                });
            }

            @Override
            public void onError(int errorCode) {
                JLogger.e("MSG-ReactionAdd", "error, code is " + errorCode);
                if (callback != null) {
                    mCore.getCallbackHandler().post(() -> callback.onError(errorCode));
                }
            }
        });
    }

    @Override
    public void removeMessageReaction(String messageId, Conversation conversation, String reactionId, ISimpleCallback callback) {
        if (messageId == null || messageId.isEmpty()
                || conversation == null || conversation.getConversationId().isEmpty()
                || reactionId == null || reactionId.isEmpty()) {
            JLogger.e("MSG-ReactionRemove", "invalid parameter");
            if (callback != null) {
                mCore.getCallbackHandler().post(() -> callback.onError(JErrorCode.INVALID_PARAM));
            }
            return;
        }
        if (mCore.getWebSocket() == null) {
            int errorCode = JErrorCode.CONNECTION_UNAVAILABLE;
            JLogger.e("MSG-ReactionRemove", "fail, code is " + errorCode);
            if (callback != null) {
                mCore.getCallbackHandler().post(() -> callback.onError(errorCode));
            }
            return;
        }
        mCore.getWebSocket().removeMessageReaction(messageId, conversation, reactionId, mCore.getUserId(), new WebSocketTimestampCallback() {
            @Override
            public void onSuccess(long timestamp) {
                JLogger.i("MSG-ReactionRemove", "success");
                mCore.getCallbackHandler().post(() -> {
                    if (callback != null) {
                        callback.onSuccess();
                    }

                    // update reaction db
                    List<MessageReaction> dbReactions = mCore.getDbManager().getMessageReactions(Collections.singletonList(messageId));
                    if (!dbReactions.isEmpty()) {
                        MessageReaction dbReaction = dbReactions.get(0);
                        for (MessageReactionItem item : dbReaction.getItemList()) {
                            if (reactionId.equals(item.getReactionId())) {
                                List<UserInfo> userInfoList = new ArrayList<>();
                                for (UserInfo userInfo : item.getUserInfoList()) {
                                    if (!mCore.getUserId().equals(userInfo.getUserId())) {
                                        userInfoList.add(userInfo);
                                    }
                                }
                                if (userInfoList.isEmpty()) {
                                    dbReaction.getItemList().remove(item);
                                } else {
                                    item.setUserInfoList(userInfoList);
                                }
                                break;
                            }
                        }
                        mCore.getDbManager().setMessageReactions(Collections.singletonList(dbReaction));
                    }

                    //callback delegate
                    //callback 只有新增的，不用本地做合并，因为本地不全（特别是收到别的用户的 reaction 时，不能返回不全的数据）
                    MessageReaction reaction = new MessageReaction();
                    reaction.setMessageId(messageId);
                    MessageReactionItem item = new MessageReactionItem();
                    item.setReactionId(reactionId);
                    UserInfo currentUser = mUserInfoManager.getUserInfo(mCore.getUserId());
                    if (currentUser == null) {
                        currentUser = new UserInfo();
                        currentUser.setUserId(mCore.getUserId());
                    }
                    item.setUserInfoList(Collections.singletonList(currentUser));
                    reaction.setItemList(Collections.singletonList(item));
                    if (mListenerMap != null) {
                        for (Map.Entry<String, IMessageListener> entry : mListenerMap.entrySet()) {
                            entry.getValue().onMessageReactionRemove(conversation, reaction);
                        }
                    }
                });
            }

            @Override
            public void onError(int errorCode) {
                JLogger.e("MSG-ReactionRemove", "error, code is " + errorCode);
                if (callback != null) {
                    mCore.getCallbackHandler().post(() -> callback.onError(errorCode));
                }
            }
        });
    }

    @Override
    public void getMessagesReaction(List<String> messageIdList, Conversation conversation, IMessageReactionListCallback callback) {
        if (messageIdList == null || messageIdList.isEmpty()
        || conversation == null || conversation.getConversationId().isEmpty()) {
            JLogger.e("MSG-ReactionGet", "invalid parameter");
            if (callback != null) {
                mCore.getCallbackHandler().post(() -> callback.onError(JErrorCode.INVALID_PARAM));
            }
            return;
        }
        if (mCore.getWebSocket() == null) {
            int errorCode = JErrorCode.CONNECTION_UNAVAILABLE;
            JLogger.e("MSG-ReactionGet", "fail, code is " + errorCode);
            if (callback != null) {
                mCore.getCallbackHandler().post(() -> callback.onError(errorCode));
            }
            return;
        }
        mCore.getWebSocket().getMessagesReaction(messageIdList, conversation, new MessageReactionListCallback() {
            @Override
            public void onSuccess(List<MessageReaction> reactionList) {
                JLogger.i("MSG-ReactionGet", "success");
                mCore.getDbManager().setMessageReactions(reactionList);
                if (callback != null) {
                    mCore.getCallbackHandler().post(() -> callback.onSuccess(reactionList));
                }
            }

            @Override
            public void onError(int errorCode) {
                JLogger.e("MSG-ReactionGet", "error, code is " + errorCode);
                if (callback != null) {
                    mCore.getCallbackHandler().post(() -> callback.onError(errorCode));
                }
            }
        });

    }

    @Override
    public List<MessageReaction> getCachedMessagesReaction(List<String> messageIdList) {
        return mCore.getDbManager().getMessageReactions(messageIdList);
    }

    @Override
    public void uploadImage(String localPath, JIMConst.IResultCallback<String> callback) {
        UploadManager uploadManager = new UploadManager(mCore);
        uploadManager.uploadImage(localPath, new JIMConst.IResultCallback<String>() {
            @Override
            public void onSuccess(String data) {
                if (callback != null) {
                    mCore.getCallbackHandler().post(() -> callback.onSuccess(data));
                }
            }

            @Override
            public void onError(int errorCode) {
                if (callback != null) {
                    mCore.getCallbackHandler().post(() -> callback.onError(errorCode));
                }
            }
        });
    }

    @Override
    public void setMute(boolean isMute, List<TimePeriod> periods, ISimpleCallback callback) {
        if (mCore.getWebSocket() == null) {
            int errorCode = JErrorCode.CONNECTION_UNAVAILABLE;
            JLogger.e("MSG-Mute", "fail, code is " + errorCode);
            if (callback != null) {
                mCore.getCallbackHandler().post(() -> callback.onError(errorCode));
            }
            return;
        }
        TimeZone timezone = TimeZone.getDefault();
        String zoneName = timezone.getID();
        if (zoneName == null) {
            zoneName = "";
        }
        mCore.getWebSocket().setGlobalMute(isMute, mCore.getUserId(), zoneName, periods, new WebSocketTimestampCallback() {
            @Override
            public void onSuccess(long timestamp) {
                JLogger.i("MSG-Mute", "success");
                if (callback != null) {
                    mCore.getCallbackHandler().post(callback::onSuccess);
                }
            }

            @Override
            public void onError(int errorCode) {
                JLogger.e("MSG-Mute", "code is " + errorCode);
                if (callback != null) {
                    mCore.getCallbackHandler().post(() -> callback.onError(errorCode));
                }
            }
        });
    }

    @Override
    public void getMuteStatus(IGetMuteStatusCallback callback) {
        mCore.getWebSocket().getGlobalMute(mCore.getUserId(), new GetGlobalMuteCallback() {
            @Override
            public void onSuccess(boolean isMute, String timezone, List<TimePeriod> periods) {
                JLogger.i("MSG-GetMute", "success");
                if (callback != null) {
                    mCore.getCallbackHandler().post(() -> callback.onSuccess(isMute, timezone, periods));
                }
            }

            @Override
            public void onError(int errorCode) {
                JLogger.e("MSG-GetMute", "code is " + errorCode);
                if (callback != null) {
                    mCore.getCallbackHandler().post(() -> callback.onError(errorCode));
                }
            }
        });
    }

    private void loopBroadcastMessage(MessageContent content,
                                      List<Conversation> conversations,
                                      int processCount,
                                      int totalCount,
                                      IBroadcastMessageCallback callback) {
        if (conversations.isEmpty()) {
            if (callback != null) {
                mCore.getCallbackHandler().post(callback::onComplete);
            }
            return;
        }
        sendMessage(content, conversations.get(0), null, true, new ISendMessageCallback() {
            @Override
            public void onSuccess(Message message) {
                broadcastCallbackAndLoopNext(message, JErrorCode.NONE, conversations, processCount, totalCount, callback);
            }

            @Override
            public void onError(Message message, int errorCode) {
                broadcastCallbackAndLoopNext(message, errorCode, conversations, processCount, totalCount, callback);
            }
        });
    }

    private void broadcastCallbackAndLoopNext(Message message,
                                              int errorCode,
                                              List<Conversation> conversations,
                                              int processCount,
                                              int totalCount,
                                              IBroadcastMessageCallback callback) {
        if (callback != null) {
            mCore.getCallbackHandler().post(() -> callback.onProgress(message, errorCode, processCount, totalCount));
        }
        if (conversations.size() <= 1) {
            if (callback != null) {
                mCore.getCallbackHandler().post(callback::onComplete);
            }
        } else {
            conversations.remove(0);
            mCore.getSendHandler().postDelayed(() -> loopBroadcastMessage(message.getContent(), conversations, processCount + 1, totalCount, callback), 50);
        }
    }



    @Override
    public void setMessageState(long clientMsgNo, Message.MessageState state) {
        //更新消息状态
        mCore.getDbManager().setMessageState(clientMsgNo, state);
        //查询消息
        List<Message> messages = getMessagesByClientMsgNos(new long[]{clientMsgNo});
        //通知会话更新
        if (messages == null || messages.isEmpty()) {
            return;
        }
        Message m = messages.get(0);
        if (m.getConversation().getConversationType() == Conversation.ConversationType.CHATROOM) {
            return;
        }
        if (mSendReceiveListener != null) {
            mSendReceiveListener.onMessagesSetState(m.getConversation(), clientMsgNo, state);
        }
    }

    @Override
    public void setTop(String messageId, Conversation conversation, boolean isTop, ISimpleCallback callback) {
        if (mCore.getWebSocket() == null) {
            int errorCode = JErrorCode.CONNECTION_UNAVAILABLE;
            JLogger.e("MSG-SetTop", "fail, code is " + errorCode);
            if (callback != null) {
                mCore.getCallbackHandler().post(() -> callback.onError(errorCode));
            }
            return;
        }
        if (TextUtils.isEmpty(messageId)
                || conversation == null
                || TextUtils.isEmpty(conversation.getConversationId())) {
            JLogger.e("MSG-SetTop", "invalid parameter");
            if (callback != null) {
                mCore.getCallbackHandler().post(() -> callback.onError(JErrorCode.INVALID_PARAM));
            }
            return;
        }
        mCore.getWebSocket().setMessageTop(messageId, conversation, isTop, new WebSocketTimestampCallback() {
            @Override
            public void onSuccess(long timestamp) {
                JLogger.i("MSG-SetTop", "success");
                updateMessageSendSyncTime(timestamp);
                long now = mCore.getCurrentTime();
                Message message = mCore.getDbManager().getMessageWithMessageId(messageId, now);
                UserInfo user = mUserInfoManager.getUserInfo(mCore.getUserId());
                if (user == null) {
                    user = new UserInfo();
                    user.setUserId(mCore.getUserId());
                }
                UserInfo finalUser = user;
                mCore.getCallbackHandler().post(() -> {
                    if (callback != null) {
                        callback.onSuccess();
                    }
                    if (message != null && mListenerMap != null) {
                        for (Map.Entry<String, IMessageListener> entry : mListenerMap.entrySet()) {
                            entry.getValue().onMessageSetTop(message, finalUser, isTop);
                        }
                    }
                });
            }

            @Override
            public void onError(int errorCode) {
                JLogger.e("MSG-SetTop", "error code is " + errorCode);
                mCore.getCallbackHandler().post(() -> {
                    if (callback != null) {
                        callback.onError(errorCode);
                    }
                });
            }
        });
    }

    @Override
    public void getTopMessage(Conversation conversation, IGetTopMessageCallback callback) {
        if (mCore.getWebSocket() == null) {
            int errorCode = JErrorCode.CONNECTION_UNAVAILABLE;
            JLogger.e("MSG-GetTop", "fail, code is " + errorCode);
            if (callback != null) {
                mCore.getCallbackHandler().post(() -> callback.onError(errorCode));
            }
            return;
        }
        if (conversation == null
                || TextUtils.isEmpty(conversation.getConversationId())) {
            JLogger.e("MSG-GetTop", "invalid parameter");
            if (callback != null) {
                mCore.getCallbackHandler().post(() -> callback.onError(JErrorCode.INVALID_PARAM));
            }
            return;
        }
        mCore.getWebSocket().getTopMessage(conversation, new GetTopMsgCallback() {
            @Override
            public void onSuccess(ConcreteMessage message, UserInfo userInfo, long timestamp) {
                JLogger.i("MSG-GetTop", "success");
                insertRemoteMessages(Collections.singletonList(message));
                mUserInfoManager.insertUserInfoList(Collections.singletonList(userInfo));
                mCore.getCallbackHandler().post(() -> {
                    if (callback != null) {
                        callback.onSuccess(message, userInfo, timestamp);
                    }
                });
            }

            @Override
            public void onError(int errorCode) {
                JLogger.e("MSG-GetTop", "error code is " + errorCode);
                mCore.getCallbackHandler().post(() -> {
                    if (callback != null) {
                        callback.onError(errorCode);
                    }
                });
            }
        });
    }

    @Override
    public void addFavorite(List<String> messageIdList, ISimpleCallback callback) {
        if (mCore.getWebSocket() == null) {
            int errorCode = JErrorCode.CONNECTION_UNAVAILABLE;
            JLogger.e("MSG-AddFvr", "fail, code is " + errorCode);
            if (callback != null) {
                mCore.getCallbackHandler().post(() -> callback.onError(errorCode));
            }
            return;
        }
        if (messageIdList == null
                || messageIdList.isEmpty()) {
            JLogger.e("MSG-AddFvr", "invalid parameter");
            if (callback != null) {
                mCore.getCallbackHandler().post(() -> callback.onError(JErrorCode.INVALID_PARAM));
            }
            return;
        }
        List<Message> messageList = getMessagesByMessageIds(messageIdList);
        mCore.getWebSocket().addFavoriteMessages(messageList, mCore.getUserId(), new WebSocketTimestampCallback() {
            @Override
            public void onSuccess(long timestamp) {
                JLogger.i("MSG-AddFvr", "success");
                if (callback != null) {
                    mCore.getCallbackHandler().post(callback::onSuccess);
                }
            }

            @Override
            public void onError(int errorCode) {
                JLogger.e("MSG-AddFvr", "error, code is " + errorCode);
                if (callback != null) {
                    mCore.getCallbackHandler().post(() -> callback.onError(errorCode));
                }
            }
        });
    }

    @Override
    public void removeFavorite(List<String> messageIdList, ISimpleCallback callback) {
        if (mCore.getWebSocket() == null) {
            int errorCode = JErrorCode.CONNECTION_UNAVAILABLE;
            JLogger.e("MSG-RmFvr", "fail, code is " + errorCode);
            if (callback != null) {
                mCore.getCallbackHandler().post(() -> callback.onError(errorCode));
            }
            return;
        }
        if (messageIdList == null
                || messageIdList.isEmpty()) {
            JLogger.e("MSG-RmFvr", "invalid parameter");
            if (callback != null) {
                mCore.getCallbackHandler().post(() -> callback.onError(JErrorCode.INVALID_PARAM));
            }
            return;
        }
        List<Message> messageList = getMessagesByMessageIds(messageIdList);
        mCore.getWebSocket().removeFavoriteMessages(messageList, mCore.getUserId(), new WebSocketTimestampCallback() {
            @Override
            public void onSuccess(long timestamp) {
                JLogger.i("MSG-RmFvr", "success");
                if (callback != null) {
                    mCore.getCallbackHandler().post(callback::onSuccess);
                }
            }

            @Override
            public void onError(int errorCode) {
                JLogger.e("MSG-RmFvr", "error, code is " + errorCode);
                if (callback != null) {
                    mCore.getCallbackHandler().post(() -> callback.onError(errorCode));
                }
            }
        });
    }

    @Override
    public void getFavorite(GetFavoriteMessageOption option, IGetFavoriteMessageCallback callback) {
        if (mCore.getWebSocket() == null) {
            int errorCode = JErrorCode.CONNECTION_UNAVAILABLE;
            JLogger.e("MSG-GetFvr", "fail, code is " + errorCode);
            if (callback != null) {
                mCore.getCallbackHandler().post(() -> callback.onError(errorCode));
            }
            return;
        }
        if (option == null || option.getCount() <= 0) {
            JLogger.e("MSG-GetFvr", "invalid parameter");
            if (callback != null) {
                mCore.getCallbackHandler().post(() -> callback.onError(JErrorCode.INVALID_PARAM));
            }
            return;
        }
        mCore.getWebSocket().getFavoriteMessages(option.getOffset(), option.getCount(), mCore.getUserId(), new GetFavoriteMsgCallback() {
            @Override
            public void onSuccess(List<FavoriteMessage> messageList, String offset) {
                JLogger.i("MSG-GetFvr", "success");
                List<ConcreteMessage> messages = new ArrayList<>();
                for (FavoriteMessage favoriteMessage : messageList) {
                    if (favoriteMessage.getMessage() instanceof ConcreteMessage) {
                        ConcreteMessage cm = (ConcreteMessage) favoriteMessage.getMessage();
                        messages.add(cm);
                    }
                }
                updateUserInfo(messages);
                if (callback != null) {
                    mCore.getCallbackHandler().post(() -> callback.onSuccess(messageList, offset));
                }
            }

            @Override
            public void onError(int errorCode) {
                JLogger.e("MSG-GetFvr", "error, code is " + errorCode);
                if (callback != null) {
                    mCore.getCallbackHandler().post(() -> callback.onError(errorCode));
                }
            }
        });
    }

    @Override
    public void registerContentType(Class<? extends MessageContent> messageContentClass) {
        JLogger.i("MSG-Register", "class is " + messageContentClass);
        ContentTypeCenter.getInstance().registerContentType(messageContentClass);
    }

    @Override
    public void addListener(String key, IMessageListener listener) {
        if (listener == null || TextUtils.isEmpty(key)) {
            return;
        }
        if (mListenerMap == null) {
            mListenerMap = new ConcurrentHashMap<>();
        }
        mListenerMap.put(key, listener);
    }

    @Override
    public void removeListener(String key) {
        if (!TextUtils.isEmpty(key) && mListenerMap != null) {
            mListenerMap.remove(key);
        }
    }

    @Override
    public void addSyncListener(String key, IMessageSyncListener listener) {
        if (listener == null || TextUtils.isEmpty(key)) {
            return;
        }
        if (mSyncListenerMap == null) {
            mSyncListenerMap = new ConcurrentHashMap<>();
        }
        mSyncListenerMap.put(key, listener);
    }

    @Override
    public void removeSyncListener(String key) {
        if (!TextUtils.isEmpty(key) && mSyncListenerMap != null) {
            mSyncListenerMap.remove(key);
        }
    }

    @Override
    public void addReadReceiptListener(String key, IMessageReadReceiptListener listener) {
        if (listener == null || TextUtils.isEmpty(key)) {
            return;
        }
        if (mReadReceiptListenerMap == null) {
            mReadReceiptListenerMap = new ConcurrentHashMap<>();
        }
        mReadReceiptListenerMap.put(key, listener);
    }

    @Override
    public void removeReadReceiptListener(String key) {
        if (!TextUtils.isEmpty(key) && mReadReceiptListenerMap != null) {
            mReadReceiptListenerMap.remove(key);
        }
    }

    @Override
    public void addDestroyListener(String key, IMessageDestroyListener listener) {
        if (listener == null || TextUtils.isEmpty(key)) {
            return;
        }
        if (mDestroyListenerMap == null) {
            mDestroyListenerMap = new ConcurrentHashMap<>();
        }
        mDestroyListenerMap.put(key, listener);
    }

    @Override
    public void removeDestroyListener(String key) {
        if (!TextUtils.isEmpty(key) && mDestroyListenerMap != null) {
            mDestroyListenerMap.remove(key);
        }
    }

    @Override
    public void setPreprocessor(IMessagePreprocessor preprocessor) {
        mCore.getWebSocket().setMessagePreprocessor(preprocessor);
    }

    @Override
    public void setMessageUploadProvider(IMessageUploadProvider uploadProvider) {
        this.mMessageUploadProvider = uploadProvider;
    }

    public void setDefaultMessageUploadProvider(IMessageUploadProvider uploadProvider) {
        this.mDefaultMessageUploadProvider = uploadProvider;
    }

    @Override
    public boolean onMessageReceive(ConcreteMessage message) {
        JLogger.i("MSG-Rcv", "direct message id is " + message.getMessageId());
        // 只处理发件箱的消息，收件箱的消息直接抛弃（状态消息直接漏过）
        boolean isStatusMessage = ((message.getFlags() & MessageContent.MessageFlag.IS_STATUS.getValue()) != 0);
        if (mSyncProcessing && !isStatusMessage) {
            if (message.getDirection() == Message.MessageDirection.SEND) {
                mSyncNotifyTime = message.getTimestamp();
            }
            return false;
        }
        List<ConcreteMessage> list = new ArrayList<>();
        list.add(message);
        handleReceiveMessages(list, false);
        return true;
    }

    @Override
    public void onSyncNotify(long syncTime) {
        if (mSyncProcessing) {
            mSyncNotifyTime = syncTime;
            return;
        }
        if (syncTime > mCore.getMessageReceiveTime()) {
            mSyncProcessing = true;
            sync();
        }
    }

    @Override
    public void onChatroomSyncNotify(String chatroomId, long syncTime) {
        if (isChatroomSyncProcessing()) {
            setTimeForChatroomSyncMap(chatroomId, syncTime);
            return;
        }
        syncChatroomMessages(chatroomId, syncTime);
    }

    @Override
    public void onMessageSend(String messageId, long timestamp, long seqNo, String clientUid, String contentType, MessageContent content, int groupMemberCount) {
        if (clientUid == null || TextUtils.isEmpty(clientUid)
        || messageId == null || TextUtils.isEmpty(messageId)) {
            return;
        }
        mCore.getDbManager().updateMessageAfterSendWithClientUid(clientUid, messageId, timestamp, seqNo, groupMemberCount);
        if (contentType != null && content != null) {
            mCore.getDbManager().updateMessageContentWithMessageId(content, contentType, messageId);
        }
    }

    @Override
    public void onChatroomJoin(String chatroomId) {
        // 确保后面会走 sync 逻辑
        long time = mChatroomManager.getSyncTimeForChatroom(chatroomId) + 1;
        onChatroomSyncNotify(chatroomId, time);
    }

    @Override
    public void onChatroomQuit(String chatroomId) {

    }

    @Override
    public void onChatroomJoinFail(String chatroomId, int errorCode) {

    }

    @Override
    public void onChatroomQuitFail(String chatroomId, int errorCode) {

    }

    @Override
    public void onChatroomKick(String chatroomId) {

    }

    @Override
    public void onChatroomDestroy(String chatroomId) {

    }

    public interface ISendReceiveListener {
        void onMessageSave(ConcreteMessage message);
        void onMessageSend(ConcreteMessage message);
        void onMessageReceive(List<ConcreteMessage> messages);
        void onMessagesRead(Conversation conversation, List<String> messageIds);
        void onMessagesSetState(Conversation conversation, long clientMsgNo, Message.MessageState state);
        void onMessageRemove(Conversation conversation, List<ConcreteMessage> removedMessages, ConcreteMessage lastMessage);
        void onMessageClear(Conversation conversation, long startTime, String sendUserId, ConcreteMessage lastMessage);
        void onConversationsAdd(ConcreteConversationInfo conversations);
        void onConversationsDelete(List<Conversation> conversations);
        void onConversationsUpdate(String updateType, List<ConcreteConversationInfo> conversations);
        void onConversationsClearTotalUnread(long clearTime);
        void onConversationSetUnread(Conversation conversation);
        void onMessageUpdate(ConcreteMessage message);
        void onConversationsAddToTag(String tagId, List<Conversation> conversations);
        void onConversationsRemoveFromTag(String tagId, List<Conversation> conversations);
    }

    public void setSendReceiveListener(ISendReceiveListener sendReceiveListener) {
        mSendReceiveListener = sendReceiveListener;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        mSendReceiveListener = null;
    }

    public void syncMessage() {
        mSyncProcessing = true;
        sync();
    }

    void updateMessageSendSyncTime(long timestamp) {
        if (mSyncProcessing) {
            if (timestamp > mCachedSendTime) {
                mCachedSendTime = timestamp;
            }
        } else {
            mCore.setMessageSendSyncTime(timestamp);
        }
    }

    void updateMessageReceiveTime(long timestamp) {
        if (mSyncProcessing) {
            if (timestamp > mCachedReceiveTime) {
                mCachedReceiveTime = timestamp;
            }
        } else {
            mCore.setMessageReceiveTime(timestamp);
        }
    }

    public void connectSuccess() {
        mSyncProcessing = true;
        mSyncNotifyTime = 0;
        setChatroomSyncProcessing(false);
        clearChatroomSyncMap();
    }

    private void onMessageReceive(List<ConcreteMessage> messages, boolean isFinished) {
        JLogger.i("MSG-Rcv", "messages count is " + messages.size() + ", isFinish is " + isFinished);
        handleReceiveMessages(messages, true);

        if (!isFinished) {
            sync();
        } else if (mSyncNotifyTime > mCore.getMessageSendSyncTime()) {
            sync();
            mSyncNotifyTime = -1;
        } else {
            mSyncProcessing = false;
            if (mCachedSendTime > 0) {
                mCore.setMessageSendSyncTime(mCachedSendTime);
                mCachedSendTime = -1;
            }
            if (mCachedReceiveTime > 0) {
                mCore.setMessageReceiveTime(mCachedReceiveTime);
                mCachedReceiveTime = -1;
            }
            if (mSyncListenerMap != null) {
                for (Map.Entry<String, IMessageSyncListener> entry : mSyncListenerMap.entrySet()) {
                    mCore.getCallbackHandler().post(() -> entry.getValue().onMessageSyncComplete());
                }
            }
        }
    }

    private void onChatroomMessageReceive(List<ConcreteMessage> messages) {
        JLogger.i("MSG-Rcv", "chatroom messages count is " + messages.size());
        if (messages == null || messages.isEmpty()) {
            checkChatroomSyncMap();
            return;
        }
        List<ConcreteMessage> messagesToSave = messagesToSave(messages);
        insertRemoteMessages(messagesToSave);

        ConcreteMessage lastMessage = messages.get(messages.size()-1);
        mChatroomManager.setSyncTime(lastMessage.getConversation().getConversationId(), lastMessage.getTimestamp());

        for (ConcreteMessage message : messages) {
            //recall message
            if (message.getContentType().equals(RecallCmdMessage.CONTENT_TYPE)) {
                RecallCmdMessage cmd = (RecallCmdMessage) message.getContent();
                Message recallMessage = handleRecallCmdMessage(message.getConversation(), cmd.getOriginalMessageId(), cmd.getExtra());
                //recallMessage 为空表示被撤回的消息本地不存在，不需要回调
                if (recallMessage != null) {
                    if (mListenerMap != null) {
                        for (Map.Entry<String, IMessageListener> entry : mListenerMap.entrySet()) {
                            mCore.getCallbackHandler().post(() -> entry.getValue().onMessageRecall(recallMessage));
                        }
                    }
                }
                continue;
            }

            //cmd消息不回调
            if ((message.getFlags() & MessageContent.MessageFlag.IS_CMD.getValue()) != 0) {
                continue;
            }

            //已存在的消息不回调
            if (message.isExisted()) {
                continue;
            }

            //执行回调
            if (mListenerMap != null) {
                for (Map.Entry<String, IMessageListener> entry : mListenerMap.entrySet()) {
                    mCore.getCallbackHandler().post(() -> entry.getValue().onMessageReceive(message));
                }
            }
        }
        checkChatroomSyncMap();
    }

    private List<ConcreteMessage> messagesToSave(List<ConcreteMessage> messages) {
        List<ConcreteMessage> list = new ArrayList<>();
        for (ConcreteMessage message : messages) {
            if ((message.getFlags() & MessageContent.MessageFlag.IS_SAVE.getValue()) != 0) {
                list.add(message);
            }
            saveReferMessages(message);
        }
        return list;
    }

    private void saveReferMessages(ConcreteMessage message) {
        if (message.getReferredMessage() == null) return;
        //查询本地数据库是否已保存该引用消息
        ConcreteMessage localReferMsg = mCore.getDbManager().getMessageWithMessageId(message.getReferredMessage().getMessageId(), mCore.getCurrentTime());
        //如果本地数据库已保存该引用消息，直接将消息中原来的引用消息替换为本地保存的引用消息
        if (localReferMsg != null) {
            message.setReferredMessage(localReferMsg);
            return;
        }
        //如果本地数据库未保存该引用消息，将该引用消息保存到数据库中
        List<ConcreteMessage> list = new ArrayList<>();
        list.add((ConcreteMessage) message.getReferredMessage());
        List<ConcreteMessage> messagesToSave = messagesToSave(list);
        insertRemoteMessages(messagesToSave);
    }

    private Message handleModifyMessage(String messageId, String msgType, MessageContent content) {
        if (messageId == null || messageId.isEmpty()) {
            return null;
        }
        mCore.getDbManager().updateMessageContentWithMessageId(content, msgType, messageId);
        List<String> ids = new ArrayList<>(1);
        ids.add(messageId);
        List<ConcreteMessage> messages = mCore.getDbManager().getConcreteMessagesByMessageIds(ids);
        if (!messages.isEmpty()) {
            ConcreteMessage message = messages.get(0);
            int flags = message.getFlags() | MessageContent.MessageFlag.IS_MODIFIED.getValue();
            message.setFlags(flags);
            message.setEdit(true);
            mCore.getDbManager().setMessageFlags(message.getMessageId(), flags);
            if (mSendReceiveListener != null) {
                mSendReceiveListener.onMessageUpdate(message);
            }
            return message;
        }
        return null;
    }

    private Message handleRecallCmdMessage(Conversation conversation, String messageId, Map<String, String> extra) {
        RecallInfoMessage recallInfoMessage = new RecallInfoMessage();
        recallInfoMessage.setExtra(extra);
        mCore.getDbManager().updateMessageContentWithMessageId(recallInfoMessage, RecallInfoMessage.CONTENT_TYPE, messageId);
        List<String> ids = new ArrayList<>(1);
        ids.add(messageId);
        List<ConcreteMessage> messages = mCore.getDbManager().getConcreteMessagesByMessageIds(ids);
        if (!messages.isEmpty()) {
            //通知会话更新
            notifyMessageRemoved(conversation, messages);
            return messages.get(0);
        }
        return null;
    }

    private void handleReceiveMessages(List<ConcreteMessage> messages, boolean isSync) {
        List<ConcreteMessage> messagesToSave = messagesToSave(messages);
        insertRemoteMessages(messagesToSave);

        //合并同一类型不同会话的cmd消息列表
        Map<String, Map<Conversation, List<ConcreteMessage>>> mergeSameTypeMessages = new HashMap<>();
        long sendTime = 0;
        long receiveTime = 0;
        for (ConcreteMessage message : messages) {
            //获取消息时间
            boolean isStatusMessage = ((message.getFlags() & MessageContent.MessageFlag.IS_STATUS.getValue()) != 0);
            if (message.getDirection() == Message.MessageDirection.SEND && !isStatusMessage) {
                sendTime = message.getTimestamp();
            } else if (message.getDirection() == Message.MessageDirection.RECEIVE && !isStatusMessage) {
                receiveTime = message.getTimestamp();
            }

            //call related
            if (message.getContentType().equals(CallActiveCallMessage.CONTENT_TYPE)) {
                mCallManager.handleActiveCallMessage(message);
                continue;
            }

            //tag add conversation
            if (message.getContentType().equals(TagAddConvMessage.CONTENT_TYPE)) {
                if (message.getTimestamp() <= mCore.getConversationSyncTime()) {
                    continue;
                }
                TagAddConvMessage cmd = (TagAddConvMessage) message.getContent();
                if (cmd.getConversations() == null
                || cmd.getConversations().isEmpty()
                || TextUtils.isEmpty(cmd.getTagId())) {
                    continue;
                }
                mCore.getDbManager().addConversationsToTag(cmd.getConversations(), cmd.getTagId());
                mSendReceiveListener.onConversationsAddToTag(cmd.getTagId(), cmd.getConversations());
                continue;
            }

            //tag remove conversation
            if (message.getContentType().equals(TagDelConvMessage.CONTENT_TYPE)) {
                if (message.getTimestamp() <= mCore.getConversationSyncTime()) {
                    continue;
                }
                TagDelConvMessage cmd = (TagDelConvMessage) message.getContent();
                if (cmd.getConversations() == null
                        || cmd.getConversations().isEmpty()
                        || TextUtils.isEmpty(cmd.getTagId())) {
                    continue;
                }
                mCore.getDbManager().removeConversationsFromTag(cmd.getConversations(), cmd.getTagId());
                mSendReceiveListener.onConversationsRemoveFromTag(cmd.getTagId(), cmd.getConversations());
                continue;
            }

            //reaction
            if (message.getContentType().equals(MsgExSetMessage.CONTENT_TYPE)) {
                MsgExSetMessage cmd = (MsgExSetMessage) message.getContent();
                if (!cmd.getAddItemList().isEmpty()) {
                    MessageReaction reaction = new MessageReaction();
                    reaction.setMessageId(cmd.getOriginalMessageId());
                    reaction.setItemList(cmd.getAddItemList());
                    if (mListenerMap != null) {
                        for (Map.Entry<String, IMessageListener> entry : mListenerMap.entrySet()) {
                            mCore.getCallbackHandler().post(() -> entry.getValue().onMessageReactionAdd(message.getConversation(), reaction));
                        }
                    }
                }
                if (!cmd.getRemoveItemList().isEmpty()) {
                    MessageReaction reaction = new MessageReaction();
                    reaction.setMessageId(cmd.getOriginalMessageId());
                    reaction.setItemList(cmd.getRemoveItemList());
                    if (mListenerMap != null) {
                        for (Map.Entry<String, IMessageListener> entry : mListenerMap.entrySet()) {
                            mCore.getCallbackHandler().post(() -> entry.getValue().onMessageReactionRemove(message.getConversation(), reaction));
                        }
                    }
                }
                continue;
            }

            //modify message
            if (message.getContentType().equals(MsgModifyMessage.CONTENT_TYPE)) {
                MsgModifyMessage cmd = (MsgModifyMessage) message.getContent();
                Message updatedMessage = handleModifyMessage(cmd.getOriginalMessageId(), cmd.getMessageType(), cmd.getMessageContent());
                //updatedMessage 为空表示被修改的消息本地不存在，不需要回调
                if (updatedMessage != null) {
                    if (mListenerMap != null) {
                        for (Map.Entry<String, IMessageListener> entry : mListenerMap.entrySet()) {
                            mCore.getCallbackHandler().post(() -> entry.getValue().onMessageUpdate(updatedMessage));
                        }
                    }
                }
                continue;
            }

            //recall message
            if (message.getContentType().equals(RecallCmdMessage.CONTENT_TYPE)) {
                RecallCmdMessage cmd = (RecallCmdMessage) message.getContent();
                Message recallMessage = handleRecallCmdMessage(message.getConversation(), cmd.getOriginalMessageId(), cmd.getExtra());
                //recallMessage 为空表示被撤回的消息本地不存在，不需要回调
                if (recallMessage != null) {
                    if (mListenerMap != null) {
                        for (Map.Entry<String, IMessageListener> entry : mListenerMap.entrySet()) {
                            mCore.getCallbackHandler().post(() -> entry.getValue().onMessageRecall(recallMessage));
                        }
                    }
                }
                continue;
            }

            //delete conversation
            if (message.getContentType().equals(DeleteConvMessage.CONTENT_TYPE)) {
                DeleteConvMessage deleteConvMessage = (DeleteConvMessage) message.getContent();

                List<Conversation> deletedList = new ArrayList<>();
                for (Conversation deleteConv : deleteConvMessage.getConversations()) {
                    //从消息表中获取指定会话的最新一条消息
                    ConcreteConversationInfo conversationInfo = mCore.getDbManager().getConversationInfo(deleteConv);
                    if (conversationInfo == null) {
                        continue;
                    }
                    Message lastMessage = conversationInfo.getLastMessage();
                    //当DeleteConvMessage的时间戳小于它指定的会话的最后一条消息的时间戳时，进行抛弃处理
                    if (lastMessage != null && message.getTimestamp() <= lastMessage.getTimestamp())
                        continue;
                    deletedList.add(deleteConv);
                }
                //进行删除操作
                mCore.getDbManager().deleteConversationInfo(deletedList);
                if (!deletedList.isEmpty() && mSendReceiveListener != null) {
                    mSendReceiveListener.onConversationsDelete(deletedList);
                }
                continue;
            }

            //read ntf
            if (message.getContentType().equals(ReadNtfMessage.CONTENT_TYPE)) {
                ReadNtfMessage readNtfMessage = (ReadNtfMessage) message.getContent();
                mCore.getDbManager().setMessagesRead(readNtfMessage.getMessageIds());
                if (mReadReceiptListenerMap != null) {
                    for (Map.Entry<String, IMessageReadReceiptListener> entry : mReadReceiptListenerMap.entrySet()) {
                        mCore.getCallbackHandler().post(() -> entry.getValue().onMessagesRead(message.getConversation(), readNtfMessage.getMessageIds()));
                    }
                }
                if (mSendReceiveListener != null) {
                    mSendReceiveListener.onMessagesRead(message.getConversation(), readNtfMessage.getMessageIds());
                }
                List<Message> readMessages = mCore.getDbManager().getMessagesByMessageIds(readNtfMessage.getMessageIds());
                for (Message readMessage : readMessages) {
                    checkAndNotifyDestroyTime(readMessage, message.getTimestamp());
                }
                continue;
            }

            //group read ntf
            if (message.getContentType().equals(GroupReadNtfMessage.CONTENT_TYPE)) {
                GroupReadNtfMessage groupReadNtfMessage = (GroupReadNtfMessage) message.getContent();
                mCore.getDbManager().setGroupMessageReadInfo(groupReadNtfMessage.getMessages());
                if (mReadReceiptListenerMap != null) {
                    for (Map.Entry<String, IMessageReadReceiptListener> entry : mReadReceiptListenerMap.entrySet()) {
                        mCore.getCallbackHandler().post(() -> entry.getValue().onGroupMessagesRead(message.getConversation(), groupReadNtfMessage.getMessages()));
                    }
                }
                for (Map.Entry<String, GroupMessageReadInfo> entry : groupReadNtfMessage.getMessages().entrySet()) {
                    String messageId = entry.getKey();
                    GroupMessageReadInfo readInfo = entry.getValue();
                    if (readInfo.getReadCount() >= readInfo.getMemberCount()) {
                        List<Message> readMessages = mCore.getDbManager().getMessagesByMessageIds(Collections.singletonList(messageId));
                        if (!readMessages.isEmpty()) {
                            Message readMessage = readMessages.get(0);
                            checkAndNotifyDestroyTime(readMessage, message.getTimestamp());
                        }
                    }
                }
                continue;
            }

            //clear history message
            if (message.getContentType().equals(CleanMsgMessage.CONTENT_TYPE)) {
                CleanMsgMessage cleanMsgMessage = (CleanMsgMessage) message.getContent();
                handleClearHistoryMessageCmdMessage(message.getConversation(), cleanMsgMessage.getCleanTime(), cleanMsgMessage.getSenderId());
                continue;
            }

            //delete msg message
            if (message.getContentType().equals(DeleteMsgMessage.CONTENT_TYPE)) {
                DeleteMsgMessage deleteMsgMessage = (DeleteMsgMessage) message.getContent();
                handleDeleteMsgMessageCmdMessage(message.getConversation(), deleteMsgMessage.getMsgIdList());
                continue;
            }

            //clear unread message
            if (message.getContentType().equals(ClearUnreadMessage.CONTENT_TYPE)) {
                Map<Conversation, List<ConcreteMessage>> conversationEntry = mergeSameTypeMessages.get(message.getContentType());
                if (conversationEntry == null) {
                    conversationEntry = new HashMap<>();
                    mergeSameTypeMessages.put(message.getContentType(), conversationEntry);
                }
                List<ConcreteMessage> messageListEntry = conversationEntry.get(message.getConversation());
                if (messageListEntry == null) {
                    messageListEntry = new ArrayList<>();
                    conversationEntry.put(message.getConversation(), messageListEntry);
                }
                //只保存本次循环中最新的一条消息
                if (messageListEntry.isEmpty() || messageListEntry.get(messageListEntry.size() - 1).getTimestamp() < message.getTimestamp()) {
                    messageListEntry.clear();
                    messageListEntry.add(message);
                }
                continue;
            }

            //top message
            if (message.getContentType().equals(TopMsgMessage.CONTENT_TYPE)) {
                handleTopMsgMessage(message);
                continue;
            }

            //clear total unread message
            if (message.getContentType().equals(ClearTotalUnreadMessage.CONTENT_TYPE)) {
                ClearTotalUnreadMessage clearTotalUnreadMessage = (ClearTotalUnreadMessage) message.getContent();
                handleClearTotalUnreadMessageCmdMessage(clearTotalUnreadMessage.getClearTime());
                continue;
            }

            //top conversation
            if (message.getContentType().equals(TopConvMessage.CONTENT_TYPE)) {
                TopConvMessage topConvMessage = (TopConvMessage) message.getContent();
                handleTopConversationCmdMessage(topConvMessage.getConversations());
                continue;
            }

            //unDisturb conversation
            if (message.getContentType().equals(UnDisturbConvMessage.CONTENT_TYPE)) {
                UnDisturbConvMessage unDisturbConvMessage = (UnDisturbConvMessage) message.getContent();
                handleUnDisturbConversationCmdMessage(unDisturbConvMessage.getConversations());
                continue;
            }

            //log command
            if (message.getContentType().equals(LogCommandMessage.CONTENT_TYPE)) {
                handleLogCommandCmdMessage(message);
                continue;
            }

            //add conversation
            if (message.getContentType().equals(AddConvMessage.CONTENT_TYPE)) {
                AddConvMessage addConvMessage = (AddConvMessage) message.getContent();
                handleAddConvMessage(addConvMessage.getConversationInfo());
                continue;
            }

            //mark unread
            if (message.getContentType().equals(MarkUnreadMessage.CONTENT_TYPE)) {
                MarkUnreadMessage markUnreadMessage = (MarkUnreadMessage) message.getContent();
                handleMarkUnreadMessage(markUnreadMessage);
                continue;
            }

            //cmd消息不回调
            if ((message.getFlags() & MessageContent.MessageFlag.IS_CMD.getValue()) != 0) {
                continue;
            }

            //已存在的消息不回调
            if (message.isExisted()) {
                continue;
            }

            //执行回调
            if (mListenerMap != null) {
                for (Map.Entry<String, IMessageListener> entry : mListenerMap.entrySet()) {
                    mCore.getCallbackHandler().post(() -> entry.getValue().onMessageReceive(message));
                }
            }
        }
        //处理合并的普通消息
        if (mSendReceiveListener != null) {
            mSendReceiveListener.onMessageReceive(messagesToSave);
        }
        //处理合并的cmd消息
        for (Map.Entry<String, Map<Conversation, List<ConcreteMessage>>> conversationEntry : mergeSameTypeMessages.entrySet()) {
            String contentType = conversationEntry.getKey();
            Map<Conversation, List<ConcreteMessage>> conversationsMap = conversationEntry.getValue();
            if (conversationsMap == null || conversationsMap.isEmpty()) {
                continue;
            }
            switch (contentType) {
                case ClearUnreadMessage.CONTENT_TYPE:
                    for (List<ConcreteMessage> messageList : conversationsMap.values()) {
                        if (messageList == null || messageList.isEmpty()) {
                            continue;
                        }
                        ClearUnreadMessage clearUnreadMessage = (ClearUnreadMessage) messageList.get(messageList.size() - 1).getContent();
                        handleClearUnreadMessageCmdMessage(clearUnreadMessage.getConversations());
                    }
                    break;
                case RecallCmdMessage.CONTENT_TYPE:
                case DeleteConvMessage.CONTENT_TYPE:
                case ReadNtfMessage.CONTENT_TYPE:
                case GroupReadNtfMessage.CONTENT_TYPE:
                case CleanMsgMessage.CONTENT_TYPE:
                case DeleteMsgMessage.CONTENT_TYPE:
                case ClearTotalUnreadMessage.CONTENT_TYPE:
                case TopConvMessage.CONTENT_TYPE:
                case UnDisturbConvMessage.CONTENT_TYPE:
                case LogCommandMessage.CONTENT_TYPE:
                case AddConvMessage.CONTENT_TYPE:
                default:
                    break;
            }
        }
        //直发的消息，而且正在同步中，不直接更新 sync time
        if (!isSync && mSyncProcessing) {
            if (sendTime > 0) {
                mCachedSendTime = sendTime;
            }
            if (receiveTime > 0) {
                mCachedReceiveTime = receiveTime;
            }
        } else {
            if (sendTime > 0) {
                mCore.setMessageSendSyncTime(sendTime);
            }
            if (receiveTime > 0) {
                mCore.setMessageReceiveTime(receiveTime);
            }
        }
    }

    private void handleTopMsgMessage(ConcreteMessage message) {
        TopMsgMessage topMsg = (TopMsgMessage) message.getContent();
        //延时操作有可能导致乱序，但是针对这个业务，回调延迟也不会造成太大影响
        getMessagesByMessageIds(message.getConversation(), Collections.singletonList(topMsg.getMessageId()), new IGetMessagesCallback() {
            @Override
            public void onSuccess(List<Message> messages) {
                if (!messages.isEmpty()) {
                    Message m = messages.get(0);
                    UserInfo userInfo = mUserInfoManager.getUserInfo(message.getSenderUserId());
                    if (userInfo == null) {
                        userInfo = new UserInfo();
                        userInfo.setUserId(message.getSenderUserId());
                    }
                    UserInfo finalUserInfo = userInfo;
                    mCore.getCallbackHandler().post(() -> {
                        if (mListenerMap != null) {
                            for (Map.Entry<String, IMessageListener> entry : mListenerMap.entrySet()) {
                                entry.getValue().onMessageSetTop(m, finalUserInfo, topMsg.isTop());
                            }
                        }
                    });
                }
            }

            @Override
            public void onError(int errorCode) {
            }
        });
    }

    private void handleClearHistoryMessageCmdMessage(Conversation conversation, long startTime, String senderId) {
        if (startTime <= 0) startTime = System.currentTimeMillis();
        //清空消息
        mCore.getDbManager().clearMessages(conversation, startTime, senderId);
        //通知消息回调
        if (mListenerMap != null) {
            long finalStartTime = startTime;
            for (Map.Entry<String, IMessageListener> entry : mListenerMap.entrySet()) {
                mCore.getCallbackHandler().post(() -> entry.getValue().onMessageClear(conversation, finalStartTime, senderId));
            }
        }
        //通知会话更新
        notifyMessageCleared(conversation, startTime, senderId);
    }

    private void handleDeleteMsgMessageCmdMessage(Conversation conversation, List<String> msgIds) {
        //查询消息
        List<ConcreteMessage> messages = mCore.getDbManager().getConcreteMessagesByMessageIds(msgIds);
        if (messages.isEmpty()) return;
        //删除消息
        mCore.getDbManager().deleteMessagesByMessageIds(msgIds);
        //通知消息回调
        if (mListenerMap != null) {
            List<Long> messageClientMsgNos = new ArrayList<>();
            for (int i = 0; i < messages.size(); i++) {
                messageClientMsgNos.add(messages.get(i).getClientMsgNo());
            }
            for (Map.Entry<String, IMessageListener> entry : mListenerMap.entrySet()) {
                mCore.getCallbackHandler().post(() -> entry.getValue().onMessageDelete(conversation, messageClientMsgNos));
            }
        }
        //通知会话更新
        notifyMessageRemoved(conversation, messages);
    }

    private void handleClearUnreadMessageCmdMessage(List<ConcreteConversationInfo> conversations) {
        if (mSendReceiveListener != null) {
            mSendReceiveListener.onConversationsUpdate(ClearUnreadMessage.CONTENT_TYPE, conversations);
        }
    }

    private void handleClearTotalUnreadMessageCmdMessage(long clearTime) {
        if (mSendReceiveListener != null) {
            mSendReceiveListener.onConversationsClearTotalUnread(clearTime);
        }
    }

    private void handleTopConversationCmdMessage(List<ConcreteConversationInfo> conversations) {
        if (mSendReceiveListener != null) {
            mSendReceiveListener.onConversationsUpdate(TopConvMessage.CONTENT_TYPE, conversations);
        }
    }

    private void handleUnDisturbConversationCmdMessage(List<ConcreteConversationInfo> conversations) {
        if (mSendReceiveListener != null) {
            mSendReceiveListener.onConversationsUpdate(UnDisturbConvMessage.CONTENT_TYPE, conversations);
        }
    }

    private void handleLogCommandCmdMessage(ConcreteMessage message) {
        LogCommandMessage content = (LogCommandMessage) message.getContent();

        if (!content.getPlatform().equals("Android")) {
            return;
        }
        JLogger.getInstance().uploadLog(message.getMessageId(), content.getStartTime(), content.getEndTime(), new IJLog.Callback() {
            @Override
            public void onSuccess() {
                JLogger.i("J-Logger", "uploadLogger success, startTime is " + content.getStartTime() + ", endTime is " + content.getEndTime());
            }

            @Override
            public void onError(int code, String msg) {
                JLogger.e("J-Logger", "uploadLogger error, code is " + code + ", msg is " + msg);
            }
        });
    }

    private void handleAddConvMessage(ConcreteConversationInfo conversationInfo) {
        if (mSendReceiveListener != null) {
            mSendReceiveListener.onConversationsAdd(conversationInfo);
        }
    }

    private void handleMarkUnreadMessage(MarkUnreadMessage markUnreadMessage) {
        for (Conversation conversation : markUnreadMessage.getConversations()) {
            if (mSendReceiveListener != null) {
                mSendReceiveListener.onConversationSetUnread(conversation);
            }
        }
    }

    //通知会话更新最新信息
    private void notifyMessageRemoved(Conversation conversation, List<ConcreteMessage> removedMessages) {
        if (mSendReceiveListener != null) {
            long now = mCore.getCurrentTime();
            //从消息表中获取当前会话最新一条消息
            Message lastMessage = mCore.getDbManager().getLastMessage(conversation, now);
            mSendReceiveListener.onMessageRemove(conversation, removedMessages, lastMessage == null ? null : (ConcreteMessage) lastMessage);
        }
    }

    //通知会话更新最新信息
    private void notifyMessageCleared(Conversation conversation, long startTime, String sendUserId) {
        if (mSendReceiveListener != null) {
            long now = mCore.getCurrentTime();
            //获取当前会话最新一条消息
            Message lastMessage = mCore.getDbManager().getLastMessage(conversation, now);
            mSendReceiveListener.onMessageClear(conversation, startTime, sendUserId, lastMessage == null ? null : (ConcreteMessage) lastMessage);
        }
    }

    private void sync() {
        JLogger.i("MSG-Sync", "receive time is " + mCore.getMessageReceiveTime() + ", send time is " + mCore.getMessageSendSyncTime());
        if (mCore.getWebSocket() != null) {
            mCore.getWebSocket().syncMessages(mCore.getMessageReceiveTime(), mCore.getMessageSendSyncTime(), mCore.getUserId(), new QryHisMsgCallback() {
                @Override
                public void onSuccess(List<ConcreteMessage> messages, boolean isFinished) {
                    JLogger.i("MSG-Sync", "success");
                    onMessageReceive(messages, isFinished);
                }

                @Override
                public void onError(int errorCode) {
                    JLogger.e("MSG-Sync", "error, code is " + errorCode);
                    onMessageReceive(new ArrayList<>(), true);
                }
            });
        }
    }

    private void checkChatroomSyncMap() {
        Map.Entry<String, Long> entry = popChatroomSyncMap();
        if (entry != null) {
            syncChatroomMessages(entry.getKey(), entry.getValue());
        } else {
            setChatroomSyncProcessing(false);
        }
    }

    private void syncChatroomMessages(String chatroomId, long time) {
        if (!mChatroomManager.isChatroomAvailable(chatroomId)) {
            checkChatroomSyncMap();
            return;
        }
        long cachedSyncTime = mChatroomManager.getSyncTimeForChatroom(chatroomId);
        if (time > cachedSyncTime) {
            int prevMessageCount = mChatroomManager.getPrevMessageCount(chatroomId);
            webSocketSyncChatroomMessage(chatroomId, cachedSyncTime, prevMessageCount);
        } else {
            checkChatroomSyncMap();
        }
    }

    private void webSocketSyncChatroomMessage(String chatroomId, long syncTime, int prevMessageCount) {
        JLogger.i("MSG-ChrmSync", "id is " + chatroomId + ", time is " + syncTime + ", count is " + prevMessageCount);
        setChatroomSyncProcessing(true);
        mCore.getWebSocket().syncChatroomMessages(chatroomId, mCore.getUserId(), syncTime, prevMessageCount, new QryHisMsgCallback() {
            @Override
            public void onSuccess(List<ConcreteMessage> messages, boolean isFinished) {
                JLogger.i("MSG-ChrmSync", "success");
                onChatroomMessageReceive(messages);
            }

            @Override
            public void onError(int errorCode) {
                JLogger.e("MSG-ChrmSync", "error, code is " + errorCode);
                onChatroomMessageReceive(new ArrayList<>());
            }
        });
    }

    private void updateUserInfo(List<ConcreteMessage> messages) {
        Map<String, GroupInfo> groupInfoMap = new HashMap<>();
        Map<String, UserInfo> userInfoMap = new HashMap<>();
        Map<String, GroupMember> groupMemberMap = new HashMap<>();
        for (ConcreteMessage message : messages) {
            if (message.getGroupInfo() != null && !TextUtils.isEmpty(message.getGroupInfo().getGroupId())) {
                groupInfoMap.put(message.getGroupInfo().getGroupId(), message.getGroupInfo());
            }
            if (message.getTargetUserInfo() != null && !TextUtils.isEmpty(message.getTargetUserInfo().getUserId())) {
                userInfoMap.put(message.getTargetUserInfo().getUserId(), message.getTargetUserInfo());
            }
            if (message.getGroupMemberInfo() != null && !TextUtils.isEmpty(message.getGroupMemberInfo().getGroupId()) && !TextUtils
                    .isEmpty(message.getGroupMemberInfo().getUserId())) {
                String key = message.getGroupMemberInfo().getGroupId() + "xxx" + message.getGroupMemberInfo().getUserId();
                groupMemberMap.put(key, message.getGroupMemberInfo());
            }
            if (message.hasMentionInfo() && message.getMentionInfo().getTargetUsers() != null) {
                for (UserInfo userInfo : message.getMentionInfo().getTargetUsers()) {
                    if (!TextUtils.isEmpty(userInfo.getUserId())) {
                        userInfoMap.put(userInfo.getUserId(), userInfo);
                    }
                }
            }
        }
        mUserInfoManager.insertUserInfoList(new ArrayList<>(userInfoMap.values()));
        mUserInfoManager.insertGroupInfoList(new ArrayList<>(groupInfoMap.values()));
        mUserInfoManager.insertGroupMemberList(new ArrayList<>(groupMemberMap.values()));
    }

    private void insertRemoteMessages(List<ConcreteMessage> messages) {
        mCore.getDbManager().insertMessages(messages);
        updateUserInfo(messages);
    }

    private String createClientUid() {
        return java.util.UUID.randomUUID().toString().replace("-", "");
    }

    private void checkAndNotifyDestroyTime(Message readMessage, long timestamp) {
        if (readMessage == null) {
            return;
        }
        if (readMessage.getLifeTimeAfterRead() > 0 && !readMessage.isDelete()) {
            long destroyTime = timestamp + readMessage.getLifeTimeAfterRead();
            if (destroyTime < readMessage.getDestroyTime()) {
                readMessage.setDestroyTime(destroyTime);
                mCore.getDbManager().updateDestroyTimeWithMessageId(readMessage.getMessageId(), destroyTime);
                mCore.getCallbackHandler().post(() -> {
                    if (mDestroyListenerMap != null) {
                        for (Map.Entry<String, IMessageDestroyListener> entry : mDestroyListenerMap.entrySet()) {
                            entry.getValue().onMessageDestroyTimeUpdate(readMessage.getMessageId(), readMessage.getConversation(), destroyTime);
                        }
                    }
                });
            }
        }
    }

    private synchronized void setTimeForChatroomSyncMap(String chatroomId, long timestamp) {
        mChatroomSyncMap.put(chatroomId, timestamp);
    }

    private synchronized Map.Entry<String, Long> popChatroomSyncMap() {
        Iterator<Map.Entry<String, Long>> iterator = mChatroomSyncMap.entrySet().iterator();
        if (iterator.hasNext()) {
            Map.Entry<String, Long> firstEntry = iterator.next();
            mChatroomSyncMap.remove(firstEntry.getKey());
            return firstEntry;
        } else {
            return null;
        }
    }

    private synchronized void clearChatroomSyncMap() {
        mChatroomSyncMap.clear();
    }

    private synchronized boolean isChatroomSyncProcessing() {
        return mChatroomSyncProcessing;
    }

    private synchronized void setChatroomSyncProcessing(boolean isProcessing) {
        mChatroomSyncProcessing = isProcessing;
    }

    private final JIMCore mCore;
    private final UserInfoManager mUserInfoManager;
    private final ChatroomManager mChatroomManager;
    private final CallManager mCallManager;
    private boolean mSyncProcessing = true;
    private long mCachedReceiveTime = -1;
    private long mCachedSendTime = -1;
    private long mSyncNotifyTime;//发件箱
    private boolean mChatroomSyncProcessing;
    private final ConcurrentHashMap<String, Long> mChatroomSyncMap;
    private ConcurrentHashMap<String, IMessageListener> mListenerMap;
    private ConcurrentHashMap<String, IMessageSyncListener> mSyncListenerMap;
    private ConcurrentHashMap<String, IMessageReadReceiptListener> mReadReceiptListenerMap;
    private ConcurrentHashMap<String, IMessageDestroyListener> mDestroyListenerMap;
    private IMessageUploadProvider mMessageUploadProvider;
    private IMessageUploadProvider mDefaultMessageUploadProvider;
    private ISendReceiveListener mSendReceiveListener;
}
