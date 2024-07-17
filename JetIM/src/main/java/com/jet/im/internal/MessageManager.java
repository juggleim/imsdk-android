package com.jet.im.internal;

import android.content.Context;
import android.text.TextUtils;

import com.jet.im.JErrorCode;
import com.jet.im.JetIMConst;
import com.jet.im.interfaces.IMessageManager;
import com.jet.im.interfaces.IMessageUploadProvider;
import com.jet.im.internal.core.JetIMCore;
import com.jet.im.internal.core.network.JWebSocket;
import com.jet.im.internal.core.network.QryHisMsgCallback;
import com.jet.im.internal.core.network.QryReadDetailCallback;
import com.jet.im.internal.core.network.SendMessageCallback;
import com.jet.im.internal.core.network.WebSocketTimestampCallback;
import com.jet.im.internal.downloader.MediaDownloadEngine;
import com.jet.im.internal.logger.IJLog;
import com.jet.im.internal.model.ConcreteConversationInfo;
import com.jet.im.internal.model.ConcreteMessage;
import com.jet.im.internal.model.MergeInfo;
import com.jet.im.internal.model.messages.AddConvMessage;
import com.jet.im.internal.model.messages.CleanMsgMessage;
import com.jet.im.internal.model.messages.ClearTotalUnreadMessage;
import com.jet.im.internal.model.messages.ClearUnreadMessage;
import com.jet.im.internal.model.messages.DeleteConvMessage;
import com.jet.im.internal.model.messages.DeleteMsgMessage;
import com.jet.im.internal.model.messages.GroupReadNtfMessage;
import com.jet.im.internal.model.messages.LogCommandMessage;
import com.jet.im.internal.model.messages.ReadNtfMessage;
import com.jet.im.internal.model.messages.RecallCmdMessage;
import com.jet.im.internal.model.messages.TopConvMessage;
import com.jet.im.internal.model.messages.UnDisturbConvMessage;
import com.jet.im.internal.util.FileUtils;
import com.jet.im.internal.util.JLogger;
import com.jet.im.model.Conversation;
import com.jet.im.model.GroupInfo;
import com.jet.im.model.GroupMessageReadInfo;
import com.jet.im.model.MediaMessageContent;
import com.jet.im.model.Message;
import com.jet.im.model.MessageContent;
import com.jet.im.model.MessageOptions;
import com.jet.im.model.MessageQueryOptions;
import com.jet.im.model.UserInfo;
import com.jet.im.model.messages.FileMessage;
import com.jet.im.model.messages.ImageMessage;
import com.jet.im.model.messages.MergeMessage;
import com.jet.im.model.messages.RecallInfoMessage;
import com.jet.im.model.messages.SnapshotPackedVideoMessage;
import com.jet.im.model.messages.TextMessage;
import com.jet.im.model.messages.ThumbnailPackedImageMessage;
import com.jet.im.model.messages.VideoMessage;
import com.jet.im.model.messages.VoiceMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MessageManager implements IMessageManager, JWebSocket.IWebSocketMessageListener {
    public MessageManager(JetIMCore core, UserInfoManager userInfoManager) {
        this.mCore = core;
        this.mCore.getWebSocket().setMessageListener(this);
        this.mUserInfoManager = userInfoManager;
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
        message.setContentType(content.getContentType());
        message.setDirection(direction);
        message.setState(state);
        message.setSenderUserId(mCore.getUserId());
        message.setClientUid(createClientUid());
        message.setTimestamp(System.currentTimeMillis());
        int flags = content.getFlags();
        if (isBroadcast) {
            flags |= MessageContent.MessageFlag.IS_BROADCAST.getValue();
        }
        message.setFlags(flags);
        if (options != null && options.getMentionInfo() != null) {
            message.setMentionInfo(options.getMentionInfo());
        }
        if (options != null && !TextUtils.isEmpty(options.getReferredMessageId())) {
            ConcreteMessage referMsg = mCore.getDbManager().getMessageWithMessageId(options.getReferredMessageId());
            if (referMsg != null) {
                message.setReferredMessage(referMsg);
            }
        }
        //保存消息
        List<ConcreteMessage> list = new ArrayList<>(1);
        list.add(message);
        mCore.getDbManager().insertMessages(list);
        //回调通知
        if (mSendReceiveListener != null) {
            mSendReceiveListener.onMessageSave(message);
        }
        //返回消息
        return message;
    }

    private void updateMessageWithContent(ConcreteMessage message) {
        if (message.getContent() != null) {
            message.setContentType(message.getContent().getContentType());
        }
        if (message.hasReferredInfo()) {
            ConcreteMessage referMsg = mCore.getDbManager().getMessageWithMessageId(message.getReferredMessage().getMessageId());
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
        ConcreteMessage message = saveMessageWithContent(content, conversation, options, Message.MessageState.SENDING, Message.MessageDirection.SEND, isBroadcast);
        sendWebSocketMessage(message, isBroadcast, callback);
        return message;
    }

    private void sendWebSocketMessage(ConcreteMessage message, boolean isBroadcast, ISendMessageCallback callback) {
        MergeInfo mergeInfo = null;
        if (message.getContent() instanceof MergeMessage) {
            MergeMessage mergeMessage = (MergeMessage) message.getContent();
            mergeInfo = new MergeInfo();
            mergeInfo.setConversation(mergeMessage.getConversation());
            mergeInfo.setContainerMsgId(mergeMessage.getContainerMsgId());
            mergeInfo.setMessages(mCore.getDbManager().getConcreteMessagesByMessageIds(mergeMessage.getMessageIdList()));
        }
        SendMessageCallback messageCallback = new SendMessageCallback(message.getClientMsgNo()) {
            @Override
            public void onSuccess(long clientMsgNo, String msgId, long timestamp, long seqNo) {
                JLogger.i("MSG-Send", "success, clientMsgNo is " + clientMsgNo);
                updateMessageSendSyncTime(timestamp);
                mCore.getDbManager().updateMessageAfterSend(clientMsgNo, msgId, timestamp, seqNo);
                message.setClientMsgNo(clientMsgNo);
                message.setMessageId(msgId);
                message.setTimestamp(timestamp);
                message.setSeqNo(seqNo);
                message.setState(Message.MessageState.SENT);

                if (message.getContent() instanceof MergeMessage) {
                    MergeMessage mergeMessage = (MergeMessage) message.getContent();
                    if (TextUtils.isEmpty(mergeMessage.getContainerMsgId())) {
                        mergeMessage.setContainerMsgId(message.getMessageId());
                    }
                    mCore.getDbManager().updateMessageContentWithMessageId(message.getContent(), message.getContentType(), message.getMessageId());
                }

                if (mSendReceiveListener != null) {
                    mSendReceiveListener.onMessageSend(message);
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
                message.getContent(),
                message.getConversation(),
                message.getClientUid(),
                mergeInfo,
                message.hasMentionInfo() ? message.getMentionInfo() : null,
                (ConcreteMessage) message.getReferredMessage(),
                isBroadcast,
                mCore.getUserId(),
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

    private Message sendMediaMessage(Message message, ISendMediaMessageCallback callback) {
        IMessageUploadProvider.UploadCallback uploadCallback = new IMessageUploadProvider.UploadCallback() {
            @Override
            public void onProgress(int progress) {
                if (callback != null) {
                    mCore.getCallbackHandler().post(() -> callback.onProgress(progress, message));
                }
            }

            @Override
            public void onSuccess(Message uploadMessage) {
                if (!(uploadMessage instanceof ConcreteMessage)) {
                    uploadMessage.setState(Message.MessageState.FAIL);
                    setMessageState(uploadMessage.getClientMsgNo(), Message.MessageState.FAIL);
                    if (callback != null) {
                        mCore.getCallbackHandler().post(() -> callback.onError(message, JErrorCode.MESSAGE_UPLOAD_ERROR));
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
                message.setState(Message.MessageState.FAIL);
                setMessageState(message.getClientMsgNo(), Message.MessageState.FAIL);
                if (callback != null) {
                    mCore.getCallbackHandler().post(() -> callback.onError(message, JErrorCode.MESSAGE_UPLOAD_ERROR));
                }
            }

            @Override
            public void onCancel() {
                message.setState(Message.MessageState.FAIL);
                setMessageState(message.getClientMsgNo(), Message.MessageState.FAIL);
                if (callback != null) {
                    mCore.getCallbackHandler().post(() -> callback.onCancel(message));
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
            return sendMessage(message.getContent(), message.getConversation(), options, callback);
        }
    }

    @Override
    public Message resendMediaMessage(Message message,
                                      ISendMediaMessageCallback callback) {
        if (message.getClientMsgNo() <= 0
                || !TextUtils.isEmpty(message.getMessageId())//已发送的消息不允许重发
                || message.getContent() == null
                || !(message.getContent() instanceof MediaMessageContent)
                || message.getConversation() == null
                || message.getConversation().getConversationId() == null
                || !(message instanceof ConcreteMessage)) {
            if (callback != null) {
                mCore.getCallbackHandler().post(() -> callback.onError(message, ConstInternal.ErrorCode.INVALID_PARAM));
            }
            return message;
        }
        if (message.getClientMsgNo() > 0) {
            if (message.getState() != Message.MessageState.SENDING) {
                message.setState(Message.MessageState.SENDING);
                setMessageState(message.getClientMsgNo(), Message.MessageState.SENDING);
            }
            updateMessageWithContent((ConcreteMessage) message);
            return sendMediaMessage(message, callback);
        } else {
            MessageOptions options = new MessageOptions();
            options.setMentionInfo(message.getMentionInfo());
            options.setReferredMessageId(message.getReferredMessage() == null ? null : message.getReferredMessage().getMessageId());
            return sendMediaMessage((MediaMessageContent) message.getContent(), message.getConversation(), options, callback);
        }
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
    public List<Message> getMessages(Conversation conversation, int count, long timestamp, JetIMConst.PullDirection direction) {
        return getMessages(
                count, timestamp, direction,
                new MessageQueryOptions
                        .Builder()
                        .setConversations(Collections.singletonList(conversation))
                        .build());
    }

    @Override
    public List<Message> getMessages(Conversation conversation, int count, long timestamp, JetIMConst.PullDirection direction, List<String> contentTypes) {
        return getMessages(
                count, timestamp, direction,
                new MessageQueryOptions
                        .Builder()
                        .setConversations(Collections.singletonList(conversation))
                        .setContentTypes(contentTypes)
                        .build());
    }

    @Override
    public List<Message> getMessages(int count, long timestamp, JetIMConst.PullDirection pullDirection, MessageQueryOptions messageQueryOptions) {
        return mCore.getDbManager().getMessages(
                count,
                timestamp,
                pullDirection,
                messageQueryOptions != null ? messageQueryOptions.getSearchContent() : null,
                messageQueryOptions != null ? messageQueryOptions.getDirection() : null,
                messageQueryOptions != null ? messageQueryOptions.getContentTypes() : null,
                messageQueryOptions != null ? messageQueryOptions.getSenderUserIds() : null,
                messageQueryOptions != null ? messageQueryOptions.getStates() : null,
                messageQueryOptions != null ? messageQueryOptions.getConversations() : null);
    }

    @Override
    public List<Message> getMessagesByMessageIds(List<String> messageIdList) {
        return mCore.getDbManager().getMessagesByMessageIds(messageIdList);
    }

    @Override
    public void getMessagesByMessageIds(Conversation conversation, List<String> messageIds, IGetMessagesCallback callback) {
        if (messageIds.size() == 0) {
            if (callback != null) {
                mCore.getCallbackHandler().post(() -> callback.onError(JErrorCode.INVALID_PARAM));
            }
            return;
        }
        List<Message> localMessages = mCore.getDbManager().getMessagesByMessageIds(messageIds);
        List<String> notExistList = new ArrayList<>();
        if (localMessages.size() == 0) {
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
        if (notExistList.size() == 0) {
            if (callback != null) {
                mCore.getCallbackHandler().post(() -> callback.onSuccess(localMessages));
            }
            return;
        }
        if (mCore.getWebSocket() == null) {
            int errorCode = JErrorCode.CONNECTION_UNAVAILABLE;
            JLogger.e("MSG-Get", "by id, fail, errorCode is " + errorCode);
            if (localMessages.size() > 0) {
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
                if (localMessages.size() > 0) {
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
    public List<Message> searchMessage(String searchContent, int count, long timestamp, JetIMConst.PullDirection direction) {
        return getMessages(
                count, timestamp, direction,
                new MessageQueryOptions
                        .Builder()
                        .setSearchContent(searchContent)
                        .build());
    }

    @Override
    public List<Message> searchMessage(String searchContent, int count, long timestamp, JetIMConst.PullDirection direction, List<String> contentTypes) {
        return getMessages(
                count, timestamp, direction,
                new MessageQueryOptions
                        .Builder()
                        .setSearchContent(searchContent)
                        .setContentTypes(contentTypes)
                        .build());
    }

    @Override
    public List<Message> searchMessageInConversation(Conversation conversation, String searchContent, int count, long timestamp, JetIMConst.PullDirection direction) {
        return getMessages(
                count, timestamp, direction,
                new MessageQueryOptions
                        .Builder()
                        .setSearchContent(searchContent)
                        .setConversations(Collections.singletonList(conversation))
                        .build());
    }

    @Override
    public void downloadMediaMessage(String messageId, IDownloadMediaMessageCallback callback) {
        mCore.getSendHandler().post(new Runnable() {
            @Override
            public void run() {
                ConcreteMessage message = mCore.getDbManager().getMessageWithMessageId(messageId);
                if (!(message.getContent() instanceof MediaMessageContent)) {
                    mCore.getCallbackHandler().post(() -> {
                        callback.onError(message, JErrorCode.MESSAGE_DOWNLOAD_ERROR_NOT_MEDIA_MESSAGE);
                    });

                    return;
                }
                MediaMessageContent content = (MediaMessageContent) message.getContent();
                if (TextUtils.isEmpty(content.getUrl())) {
                    mCore.getCallbackHandler().post(() -> {
                        callback.onError(message, JErrorCode.MESSAGE_DOWNLOAD_ERROR_URL_EMPTY);
                    });
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
                    mCore.getCallbackHandler().post(() -> {
                        callback.onError(message, JErrorCode.MESSAGE_DOWNLOAD_ERROR_APPKEY_OR_USERID_EMPTY);
                    });
                    return;
                }
                String dir = appKey + "/" + userId + "/" + media;
                String savePath = FileUtils.getMediaDownloadDir(context, dir, name);
                if (TextUtils.isEmpty(savePath)) {
                    mCore.getCallbackHandler().post(() -> {
                        callback.onError(message, JErrorCode.MESSAGE_DOWNLOAD_ERROR_SAVE_PATH_EMPTY);
                    });
                    return;

                }
                MediaDownloadEngine.getInstance().download(message.getMessageId(), content.getUrl(), savePath, new MediaDownloadEngine.DownloadEngineCallback() {

                    @Override
                    public void onError(int errorCode) {
                        mCore.getCallbackHandler().post(() -> {
                            callback.onError(message, errorCode);
                        });

                    }

                    @Override
                    public void onComplete(String savePath) {
                        content.setLocalPath(savePath);
                        mCore.getDbManager().updateMessageContentWithMessageId(message.getContent(), message.getContentType(), message.getMessageId());
                        mCore.getCallbackHandler().post(() -> {
                            callback.onSuccess(message);
                        });
                    }

                    @Override
                    public void onProgress(int progress) {
                        mCore.getCallbackHandler().post(() -> {
                            callback.onProgress(progress, message);
                        });
                    }

                    @Override
                    public void onCanceled(String tag) {
                        mCore.getCallbackHandler().post(() -> {
                            callback.onCancel(message);
                        });

                    }
                });
            }
        });

    }

    public void cancelDownloadMediaMessage(String messageId) {
        MediaDownloadEngine.getInstance().cancel(messageId);
    }

    @Override
    public List<Message> searchMessageInConversation(Conversation conversation, String searchContent, int count, long timestamp, JetIMConst.PullDirection direction, List<String> contentTypes) {
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
    public void deleteMessagesByMessageIdList(Conversation conversation, List<String> messageIds, ISimpleCallback callback) {
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
        for (int i = messages.size() - 1; i >= 0; i--) {
            Message temp = messages.get(i);
            if (temp.getConversation().equals(conversation)) {
                deleteIdList.add(temp.getMessageId());
                deleteList.add((ConcreteMessage) temp);
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
        mCore.getWebSocket().deleteMessage(conversation, deleteList, new WebSocketTimestampCallback() {
            @Override
            public void onSuccess(long timestamp) {
                JLogger.i("MSG-Delete", "by messageId, success");
                updateMessageSendSyncTime(timestamp);
                //删除消息
                mCore.getDbManager().deleteMessagesByMessageIds(deleteIdList);
                //通知会话更新
                notifyMessageRemoved(conversation, deleteList);
                //执行回调
                if (callback != null) {
                    mCore.getCallbackHandler().post(callback::onSuccess);
                }
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
    public void deleteMessagesByClientMsgNoList(Conversation conversation, List<Long> clientMsgNos, ISimpleCallback callback) {
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
            if (callback != null) {
                mCore.getCallbackHandler().post(callback::onSuccess);
            }
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
        mCore.getWebSocket().deleteMessage(conversation, deleteRemoteList, new WebSocketTimestampCallback() {
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
                if (callback != null) {
                    mCore.getCallbackHandler().post(callback::onSuccess);
                }
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
    public void clearMessages(Conversation conversation, long startTime, ISimpleCallback callback) {
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
        mCore.getWebSocket().clearHistoryMessage(conversation, finalStartTime, new WebSocketTimestampCallback() {
            @Override
            public void onSuccess(long timestamp) {
                JLogger.i("MSG-Clear", "success");
                updateMessageSendSyncTime(timestamp);
                //清空消息
                mCore.getDbManager().clearMessages(conversation, finalStartTime, null);
                //通知会话更新
                notifyMessageCleared(conversation, finalStartTime, null);
                //执行回调
                if (callback != null) {
                    mCore.getCallbackHandler().post(callback::onSuccess);
                }
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
    public void recallMessage(String messageId, Map<String, String> extras, IRecallMessageCallback callback) {
        List<String> idList = new ArrayList<>(1);
        idList.add(messageId);
        List<Message> messages = getMessagesByMessageIds(idList);
        if (messages.size() == 0) {
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
                if (callback != null) {
                    mCore.getCallbackHandler().post(() -> callback.onSuccess(m));
                }
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
    public void getRemoteMessages(Conversation conversation, int count, long startTime, JetIMConst.PullDirection direction, IGetMessagesCallback callback) {
        if (mCore.getWebSocket() == null) {
            int errorCode = JErrorCode.CONNECTION_UNAVAILABLE;
            JLogger.e("MSG-Get", "getRemoteMessages, fail, errorCode is " + errorCode);
            if (callback != null) {
                mCore.getCallbackHandler().post(() -> callback.onError(errorCode));
            }
            return;
        }
        if (count > 100) {
            count = 100;
        }
        mCore.getWebSocket().queryHisMsg(conversation, startTime, count, direction, new QryHisMsgCallback() {
            @Override
            public void onSuccess(List<ConcreteMessage> messages, boolean isFinished) {
                JLogger.i("MSG-Get", "getRemoteMessages, success");
                List<ConcreteMessage> messagesToSave = messagesToSave(messages);
                mCore.getDbManager().insertMessages(messagesToSave);
                updateUserInfo(messagesToSave);
                if (callback != null) {
                    List<Message> result = new ArrayList<>(messages);
                    mCore.getCallbackHandler().post(() -> callback.onSuccess(result));
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

    @Override
    public void getLocalAndRemoteMessages(Conversation conversation, int count, long startTime, JetIMConst.PullDirection direction, IGetLocalAndRemoteMessagesCallback callback) {
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
            getRemoteMessages(conversation, count, startTime, direction, new IGetMessagesCallback() {
                @Override
                public void onSuccess(List<Message> messages) {
                    JLogger.i("MSG-Get", "getLocalAndRemoteMessages, success");
                    //合并去重
                    List<Message> mergeList = mergeLocalAndRemoteMessages(localMessages == null ? new ArrayList<>() : localMessages, messages);
                    //消息排序
                    Collections.sort(mergeList, new Comparator<Message>() {
                        @Override
                        public int compare(Message o1, Message o2) {
                            return Long.compare(o1.getTimestamp(), o2.getTimestamp());
                        }
                    });
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

    //判断是否需要同步远端数据
    private boolean isRemoteMessagesNeeded(List<Message> localMessages, int count, long firstMessageSeqNo) {
        //如果本地消息数量不满足分页需求数量，且本地首条消息不是该会话的首条消息，需要获取远端消息
        if (localMessages.size() < count && firstMessageSeqNo != 1) {
            return true;
        }
        //判断本地列表中的消息是否连续，不连续时需要获取远端消息
        long expectedSeqNo = firstMessageSeqNo;
        for (int i = 0; i < localMessages.size(); i++) {
            if (i == 0) continue;
            ConcreteMessage m = (ConcreteMessage) localMessages.get(i);
            if (Message.MessageState.SENT == m.getState() && m.getSeqNo() > 0) {
                if (m.getSeqNo() > ++expectedSeqNo) {
                    return true;
                }
            }
        }
        return false;
    }

    //合并localList和remoteList并去重
    private List<Message> mergeLocalAndRemoteMessages(List<Message> localList, List<Message> remoteList) {
        Set<Long> seqNoSet = new HashSet<>();
        List<Message> mergedList = new ArrayList<>();
        for (Message message : remoteList) {
            if (seqNoSet.add((message).getClientMsgNo())) {
                mergedList.add(message);
            }
        }
        for (Message message : localList) {
            if (seqNoSet.add((message).getClientMsgNo())) {
                mergedList.add(message);
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
        mCore.getWebSocket().getMergedMessageList(containerMsgId, 0, 100, JetIMConst.PullDirection.OLDER, new QryHisMsgCallback() {
            @Override
            public void onSuccess(List<ConcreteMessage> messages, boolean isFinished) {
                JLogger.i("MSG-GetMerge", "success");
                mCore.getDbManager().insertMessages(messages);
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
    public void getMentionMessageList(Conversation conversation, int count, long time, JetIMConst.PullDirection direction, IGetMessagesCallback callback) {
        if (mCore.getWebSocket() == null) {
            int errorCode = JErrorCode.CONNECTION_UNAVAILABLE;
            JLogger.e("MSG-GetMention", "fail, code is " + errorCode);
            if (callback != null) {
                mCore.getCallbackHandler().post(() -> callback.onError(errorCode));
            }
            return;
        }
        mCore.getWebSocket().getMentionMessageList(conversation, time, count, direction, new QryHisMsgCallback() {
            @Override
            public void onSuccess(List<ConcreteMessage> messages, boolean isFinished) {
                JLogger.i("MSG-GetMention", "success");
                mCore.getDbManager().insertMessages(messages);
                if (callback != null) {
                    List<Message> result = new ArrayList<>(messages);
                    mCore.getCallbackHandler().post(() -> callback.onSuccess(result));
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
        if (conversations.size() == 0) {
            if (callback != null) {
                mCore.getCallbackHandler().post(callback::onComplete);
            }
            return;
        }
        loopBroadcastMessage(content, conversations, 0, conversations.size(), callback);
    }

    private void loopBroadcastMessage(MessageContent content,
                                      List<Conversation> conversations,
                                      int processCount,
                                      int totalCount,
                                      IBroadcastMessageCallback callback) {
        if (conversations.size() == 0) {
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
        if (mSendReceiveListener != null && messages != null && !messages.isEmpty()) {
            mSendReceiveListener.onMessagesSetState(messages.get(0).getConversation(), clientMsgNo, state);
        }
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
    public void setMessageUploadProvider(IMessageUploadProvider uploadProvider) {
        this.mMessageUploadProvider = uploadProvider;
    }

    public void setDefaultMessageUploadProvider(IMessageUploadProvider uploadProvider) {
        this.mDefaultMessageUploadProvider = uploadProvider;
    }

    @Override
    public boolean onMessageReceive(ConcreteMessage message) {
        if (mSyncProcessing) {
            return false;
        }
        List<ConcreteMessage> list = new ArrayList<>();
        list.add(message);
        handleReceiveMessages(list, false);
        return true;
    }

    @Override
    public void onMessageReceive(List<ConcreteMessage> messages, boolean isFinished) {
        handleReceiveMessages(messages, true);

        if (!isFinished) {
            sync();
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

    @Override
    public void onSyncNotify(long syncTime) {
        if (mSyncProcessing) {
            return;
        }
        if (syncTime > mCore.getMessageReceiveTime()) {
            mSyncProcessing = true;
            sync();
        }
    }

    interface ISendReceiveListener {
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
    }

    public void setSendReceiveListener(ISendReceiveListener sendReceiveListener) {
        mSendReceiveListener = sendReceiveListener;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        mSendReceiveListener = null;
    }

    void syncMessage() {
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

    void connectSuccess() {
        mSyncProcessing = true;
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
        ConcreteMessage localReferMsg = mCore.getDbManager().getMessageWithMessageId(message.getReferredMessage().getMessageId());
        //如果本地数据库已保存该引用消息，直接将消息中原来的引用消息替换为本地保存的引用消息
        if (localReferMsg != null) {
            message.setReferredMessage(localReferMsg);
            return;
        }
        //如果本地数据库未保存该引用消息，将该引用消息保存到数据库中
        List<ConcreteMessage> list = new ArrayList<>();
        list.add((ConcreteMessage) message.getReferredMessage());
        List<ConcreteMessage> messagesToSave = messagesToSave(list);
        mCore.getDbManager().insertMessages(messagesToSave);
        updateUserInfo(messagesToSave);
    }

    private Message handleRecallCmdMessage(Conversation conversation, String messageId, Map<String, String> extra) {
        RecallInfoMessage recallInfoMessage = new RecallInfoMessage();
        recallInfoMessage.setExtra(extra);
        mCore.getDbManager().updateMessageContentWithMessageId(recallInfoMessage, RecallInfoMessage.CONTENT_TYPE, messageId);
        List<String> ids = new ArrayList<>(1);
        ids.add(messageId);
        List<ConcreteMessage> messages = mCore.getDbManager().getConcreteMessagesByMessageIds(ids);
        if (messages.size() > 0) {
            //通知会话更新
            notifyMessageRemoved(conversation, messages);
            return messages.get(0);
        }
        return null;
    }

    private void handleReceiveMessages(List<ConcreteMessage> messages, boolean isSync) {
        List<ConcreteMessage> messagesToSave = messagesToSave(messages);
        mCore.getDbManager().insertMessages(messagesToSave);
        updateUserInfo(messagesToSave);

        //合并普通消息
        List<ConcreteMessage> messagesToUpdateConversation = new ArrayList<>();
        //合并同一类型不同会话的cmd消息列表
        Map<String, Map<Conversation, List<ConcreteMessage>>> mergeSameTypeMessages = new HashMap<>();
        long sendTime = 0;
        long receiveTime = 0;
        for (ConcreteMessage message : messages) {
            //获取消息时间
            if (message.getDirection() == Message.MessageDirection.SEND) {
                sendTime = message.getTimestamp();
            } else if (message.getDirection() == Message.MessageDirection.RECEIVE) {
                receiveTime = message.getTimestamp();
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
                    Message lastMessage = mCore.getDbManager().getLastMessage(deleteConv);
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
                LogCommandMessage logCommandMessage = (LogCommandMessage) message.getContent();
                handleLogCommandCmdMessage(logCommandMessage.getStartTime(), logCommandMessage.getEndTime());
                continue;
            }

            //add conversation
            if (message.getContentType().equals(AddConvMessage.CONTENT_TYPE)) {
                AddConvMessage addConvMessage = (AddConvMessage) message.getContent();
                handleAddConvMessage(addConvMessage.getConversationInfo());
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

            //合并普通消息
            messagesToUpdateConversation.add(message);

            //执行回调
            if (mListenerMap != null) {
                for (Map.Entry<String, IMessageListener> entry : mListenerMap.entrySet()) {
                    mCore.getCallbackHandler().post(() -> entry.getValue().onMessageReceive(message));
                }
            }
        }
        //处理合并的普通消息
        if (mSendReceiveListener != null) {
            mSendReceiveListener.onMessageReceive(messagesToUpdateConversation);
        }
        //处理合并的cmd消息
        for (Map.Entry<String, Map<Conversation, List<ConcreteMessage>>> conversationEntry : mergeSameTypeMessages.entrySet()) {
            String contentType = conversationEntry.getKey();
            Map<Conversation, List<ConcreteMessage>> conversationsMap = conversationEntry.getValue();
            if (conversationsMap == null || conversationsMap.values().isEmpty()) {
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

    private void handleLogCommandCmdMessage(long startTime, long endTime) {
        JLogger.getInstance().uploadLog(startTime, endTime, mCore.getAppKey(), mCore.getToken(), new IJLog.Callback() {
            @Override
            public void onSuccess() {
                JLogger.i("J-Logger", "uploadLogger success, startTime is " + startTime + ", endTime is " + endTime);
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

    //通知会话更新最新信息
    private void notifyMessageRemoved(Conversation conversation, List<ConcreteMessage> removedMessages) {
        if (mSendReceiveListener != null) {
            //从消息表中获取当前会话最新一条消息
            Message lastMessage = mCore.getDbManager().getLastMessage(conversation);
            mSendReceiveListener.onMessageRemove(conversation, removedMessages, lastMessage == null ? null : (ConcreteMessage) lastMessage);
        }
    }

    //通知会话更新最新信息
    private void notifyMessageCleared(Conversation conversation, long startTime, String sendUserId) {
        if (mSendReceiveListener != null) {
            //获取当前会话最新一条消息
            Message lastMessage = mCore.getDbManager().getLastMessage(conversation);
            mSendReceiveListener.onMessageClear(conversation, startTime, sendUserId, lastMessage == null ? null : (ConcreteMessage) lastMessage);
        }
    }

    private void sync() {
        JLogger.i("MSG-Sync", "receive time is " + mCore.getMessageReceiveTime() + ", send time is " + mCore.getMessageSendSyncTime());
        if (mCore.getWebSocket() != null) {
            mCore.getWebSocket().syncMessages(mCore.getMessageReceiveTime(), mCore.getMessageSendSyncTime(), mCore.getUserId());
        }
    }

    private void updateUserInfo(List<ConcreteMessage> messages) {
        Map<String, GroupInfo> groupInfoMap = new HashMap<>();
        Map<String, UserInfo> userInfoMap = new HashMap<>();
        for (ConcreteMessage message : messages) {
            if (message.getGroupInfo() != null && !TextUtils.isEmpty(message.getGroupInfo().getGroupId())) {
                groupInfoMap.put(message.getGroupInfo().getGroupId(), message.getGroupInfo());
            }
            if (message.getTargetUserInfo() != null && !TextUtils.isEmpty(message.getTargetUserInfo().getUserId())) {
                userInfoMap.put(message.getTargetUserInfo().getUserId(), message.getTargetUserInfo());
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
    }

    private String createClientUid() {
        long result = System.currentTimeMillis();
        result = result % 1000000;
        result = result * 1000 + mIncreaseId++;
        return Long.toString(result);
    }

    private final JetIMCore mCore;
    private final UserInfoManager mUserInfoManager;
    private int mIncreaseId = 0;
    private boolean mSyncProcessing = true;
    private long mCachedReceiveTime = -1;
    private long mCachedSendTime = -1;
    private ConcurrentHashMap<String, IMessageListener> mListenerMap;
    private ConcurrentHashMap<String, IMessageSyncListener> mSyncListenerMap;
    private ConcurrentHashMap<String, IMessageReadReceiptListener> mReadReceiptListenerMap;
    private IMessageUploadProvider mMessageUploadProvider;
    private IMessageUploadProvider mDefaultMessageUploadProvider;
    private ISendReceiveListener mSendReceiveListener;
}
