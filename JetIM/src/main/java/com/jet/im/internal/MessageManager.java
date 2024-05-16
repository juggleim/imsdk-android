package com.jet.im.internal;

import android.text.TextUtils;
import android.util.ArrayMap;

import com.jet.im.JErrorCode;
import com.jet.im.JetIMConst;
import com.jet.im.interfaces.IMessageManager;
import com.jet.im.internal.core.JetIMCore;
import com.jet.im.internal.core.network.JWebSocket;
import com.jet.im.internal.core.network.QryHisMsgCallback;
import com.jet.im.internal.core.network.QryReadDetailCallback;
import com.jet.im.internal.core.network.SendMessageCallback;
import com.jet.im.internal.core.network.WebSocketSimpleCallback;
import com.jet.im.internal.core.network.WebSocketTimestampCallback;
import com.jet.im.internal.model.ConcreteConversationInfo;
import com.jet.im.internal.model.ConcreteMessage;
import com.jet.im.internal.model.messages.CleanMsgMessage;
import com.jet.im.internal.model.messages.ClearUnreadMessage;
import com.jet.im.internal.model.messages.DeleteConvMessage;
import com.jet.im.internal.model.messages.DeleteMsgMessage;
import com.jet.im.internal.model.messages.GroupReadNtfMessage;
import com.jet.im.internal.model.messages.ReadNtfMessage;
import com.jet.im.internal.model.messages.RecallCmdMessage;
import com.jet.im.model.Conversation;
import com.jet.im.model.GroupInfo;
import com.jet.im.model.GroupMessageReadInfo;
import com.jet.im.model.Message;
import com.jet.im.model.MessageContent;
import com.jet.im.model.UserInfo;
import com.jet.im.model.messages.FileMessage;
import com.jet.im.model.messages.ImageMessage;
import com.jet.im.model.messages.MergeMessage;
import com.jet.im.model.messages.RecallInfoMessage;
import com.jet.im.model.messages.TextMessage;
import com.jet.im.model.messages.VideoMessage;
import com.jet.im.model.messages.VoiceMessage;
import com.jet.im.utils.LoggerUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MessageManager implements IMessageManager {

    public MessageManager(JetIMCore core) {
        this.mCore = core;
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
    }

    private final JetIMCore mCore;

    private ConcreteMessage createSendMessage(MessageContent content,
                                              Conversation conversation,
                                              boolean isBroadcast) {
        ConcreteMessage message = new ConcreteMessage();
        message.setContent(content);
        message.setConversation(conversation);
        message.setContentType(content.getContentType());
        message.setDirection(Message.MessageDirection.SEND);
        message.setState(Message.MessageState.SENDING);
        message.setSenderUserId(mCore.getUserId());
        message.setClientUid(createClientUid());
        message.setTimestamp(System.currentTimeMillis());
        int flags = content.getFlags();
        if (isBroadcast) {
            flags |= MessageContent.MessageFlag.IS_BROADCAST.getValue();
        }
        message.setFlags(flags);
        return message;
    }

    private Message sendMessage(MessageContent content,
                                Conversation conversation,
                                List<ConcreteMessage> mergedMessages,
                                boolean isBroadcast,
                                ISendMessageCallback callback) {
        ConcreteMessage message = createSendMessage(content, conversation, isBroadcast);
        List<ConcreteMessage> list = new ArrayList<>(1);
        list.add(message);
        mCore.getDbManager().insertMessages(list);

        if (mSendReceiveListener != null) {
            mSendReceiveListener.onMessageSave(message);
        }

        SendMessageCallback messageCallback = new SendMessageCallback(message.getClientMsgNo()) {
            @Override
            public void onSuccess(long clientMsgNo, String msgId, long timestamp, long seqNo) {
                if (mSyncProcessing) {
                    mCachedSendTime = timestamp;
                } else {
                    mCore.setMessageSendSyncTime(timestamp);
                }
                mCore.getDbManager().updateMessageAfterSend(clientMsgNo, msgId, timestamp, seqNo);
                message.setClientMsgNo(clientMsgNo);
                message.setMessageId(msgId);
                message.setTimestamp(timestamp);
                message.setSeqNo(seqNo);
                message.setState(Message.MessageState.SENT);

                if (mSendReceiveListener != null) {
                    mSendReceiveListener.onMessageSend(message);
                }

                if (callback != null) {
                    callback.onSuccess(message);
                }
            }

            @Override
            public void onError(int errorCode, long clientMsgNo) {
                message.setState(Message.MessageState.FAIL);
                mCore.getDbManager().messageSendFail(clientMsgNo);
                if (callback != null) {
                    message.setClientMsgNo(clientMsgNo);
                    callback.onError(message, errorCode);
                }
            }
        };
        if (mCore.getWebSocket() != null) {
            mCore.getWebSocket().sendIMMessage(content, conversation, message.getClientUid(), mergedMessages, isBroadcast, mCore.getUserId(), messageCallback);
        }
        return message;
    }

    @Override
    public Message sendMessage(MessageContent content, Conversation conversation, ISendMessageCallback callback) {
        List mergedMessages = null;
        if (content instanceof MergeMessage) {
            MergeMessage mergeMessage = (MergeMessage) content;
            mergedMessages = mCore.getDbManager().getMessagesByMessageIds(mergeMessage.getMessageIdList());
        }
        return sendMessage(content, conversation, mergedMessages, false, callback);
    }

    @Override
    public Message resendMessage(Message message, ISendMessageCallback callback) {
        if (message.getClientMsgNo() <= 0
                || message.getContent() == null
                || message.getConversation() == null
                || message.getConversation().getConversationId() == null) {
            if (callback != null) {
                callback.onError(message, ConstInternal.ErrorCode.INVALID_PARAM);
            }
            return message;
        }
        deleteMessageByClientMsgNo(message.getClientMsgNo(), false);
        return sendMessage(message.getContent(), message.getConversation(), callback);
    }

    @Override
    public Message saveMessage(MessageContent content, Conversation conversation) {
        ConcreteMessage message = new ConcreteMessage();
        message.setContent(content);
        message.setConversation(conversation);
        message.setContentType(content.getContentType());
        message.setDirection(Message.MessageDirection.SEND);
        message.setState(Message.MessageState.UNKNOWN);
        message.setSenderUserId(mCore.getUserId());
        message.setClientUid(createClientUid());
        message.setTimestamp(System.currentTimeMillis());

        List<ConcreteMessage> list = new ArrayList<>(1);
        list.add(message);
        mCore.getDbManager().insertMessages(list);

        if (mSendReceiveListener != null) {
            mSendReceiveListener.onMessageSave(message);
        }
        return message;
    }

    @Override
    public List<Message> getMessages(Conversation conversation, int count, long timestamp, JetIMConst.PullDirection direction) {
        return getMessages(conversation, count, timestamp, direction, new ArrayList<>());
    }

    @Override
    public List<Message> getMessages(Conversation conversation, int count, long timestamp, JetIMConst.PullDirection direction, List<String> contentTypes) {
        return mCore.getDbManager().getMessages(conversation, count, timestamp, direction, contentTypes);
    }

    @Override
    public List<Message> getMessagesByMessageIds(List<String> messageIdList) {
        return mCore.getDbManager().getMessagesByMessageIds(messageIdList);
    }

    @Override
    public void getMessagesByMessageIds(Conversation conversation, List<String> messageIds, IGetMessagesCallback callback) {
        if (messageIds.size() == 0) {
            if (callback != null) {
                callback.onError(JErrorCode.INVALID_PARAM);
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
        if (notExistList.size() > 0) {
            mCore.getWebSocket().queryHisMsgByIds(conversation, notExistList, new QryHisMsgCallback() {
                @Override
                public void onSuccess(List<ConcreteMessage> remoteMessages, boolean isFinished) {
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
                        callback.onSuccess(result);
                    }
                }

                @Override
                public void onError(int errorCode) {
                    if (localMessages.size() > 0) {
                        if (callback != null) {
                            callback.onSuccess(localMessages);
                        }
                    } else {
                        if (callback != null) {
                            callback.onError(errorCode);
                        }
                    }

                }
            });
        } else {
            if (callback != null) {
                callback.onSuccess(localMessages);
            }
        }
    }

    @Override
    public List<Message> getMessagesByClientMsgNos(long[] clientMsgNos) {
        return mCore.getDbManager().getMessagesByClientMsgNos(clientMsgNos);
    }

    @Override
    public List<Message> searchMessage(String searchContent, int count, long timestamp, JetIMConst.PullDirection direction) {
        return searchMessage(searchContent, count, timestamp, direction, new ArrayList<>());
    }

    @Override
    public List<Message> searchMessage(String searchContent, int count, long timestamp, JetIMConst.PullDirection direction, List<String> contentTypes) {
        return searchMessageInConversation(null, searchContent, count, timestamp, direction, new ArrayList<>());
    }

    @Override
    public List<Message> searchMessageInConversation(Conversation conversation, String searchContent, int count, long timestamp, JetIMConst.PullDirection direction) {
        return searchMessageInConversation(conversation, searchContent, count, timestamp, direction, new ArrayList<>());
    }

    @Override
    public List<Message> searchMessageInConversation(Conversation conversation, String searchContent, int count, long timestamp, JetIMConst.PullDirection direction, List<String> contentTypes) {
        return mCore.getDbManager().searchMessage(conversation, searchContent, count, timestamp, direction, contentTypes);
    }

    @Override
    public void deleteMessageByMessageId(String messageId, boolean isBidirectional) {
        //查询消息
        List<String> idList = new ArrayList<>(1);
        idList.add(messageId);
        List<Message> messages = getMessagesByMessageIds(idList);
        if (messages.isEmpty()) return;
        //删除消息
        ConcreteMessage deleteMessage = (ConcreteMessage) messages.get(0);
        mCore.getDbManager().deleteMessageByMessageId(messageId);
        //通知会话更新
        notifyMessageRemoved(deleteMessage.getConversation(), deleteMessage);
        //调用接口
        List<ConcreteMessage> deleteList = new ArrayList<>();
        deleteList.add(deleteMessage);
        mCore.getWebSocket().deleteMessage(deleteMessage.getConversation(), deleteList, isBidirectional, new WebSocketSimpleCallback() {
            @Override
            public void onSuccess() {
                LoggerUtils.i("delete message by messageId success");
            }

            @Override
            public void onError(int errorCode) {
                LoggerUtils.i("delete message by messageId error, code is " + errorCode);
            }
        });
    }

    @Override
    public void deleteMessageByClientMsgNo(long clientMsgNo, boolean isBidirectional) {
        //查询消息
        List<Message> messages = getMessagesByClientMsgNos(new long[]{clientMsgNo});
        if (messages.isEmpty()) return;
        //删除消息
        ConcreteMessage deleteMessage = (ConcreteMessage) messages.get(0);
        mCore.getDbManager().deleteMessageByClientMsgNo(clientMsgNo);
        //通知会话更新
        notifyMessageRemoved(deleteMessage.getConversation(), deleteMessage);
        //没有消息Id，不需要调用接口
        if (TextUtils.isEmpty(deleteMessage.getMessageId())) return;
        //调用接口
        List<ConcreteMessage> deleteList = new ArrayList<>();
        deleteList.add(deleteMessage);
        mCore.getWebSocket().deleteMessage(deleteMessage.getConversation(), deleteList, isBidirectional, new WebSocketSimpleCallback() {
            @Override
            public void onSuccess() {
                LoggerUtils.i("delete message by clientMsgNo success");
            }

            @Override
            public void onError(int errorCode) {
                LoggerUtils.i("delete message by clientMsgNo error, code is " + errorCode);
            }
        });
    }

    @Override
    public void clearMessages(Conversation conversation, long startTime) {
        if (startTime <= 0) startTime = System.currentTimeMillis();
        //清空消息
        mCore.getDbManager().clearMessages(conversation, startTime, null);
        //通知会话更新
        notifyMessageRemoved(conversation, null);
        //调用接口
        mCore.getWebSocket().clearHistoryMessage(conversation, startTime, new WebSocketSimpleCallback() {
            @Override
            public void onSuccess() {
                LoggerUtils.i("clear message success");
            }

            @Override
            public void onError(int errorCode) {
                LoggerUtils.i("clear message error, code is " + errorCode);
            }
        });
    }

    @Override
    public void recallMessage(String messageId, IRecallMessageCallback callback) {
        List<String> idList = new ArrayList<>(1);
        idList.add(messageId);
        List<Message> messages = getMessagesByMessageIds(idList);
        if (messages.size() > 0) {
            Message m = messages.get(0);

            if (m.getContentType().equals(RecallInfoMessage.CONTENT_TYPE)) {
                if (callback != null) {
                    callback.onError(JErrorCode.MESSAGE_ALREADY_RECALLED);
                }
                return;
            }
            mCore.getWebSocket().recallMessage(messageId, m.getConversation(), m.getTimestamp(), new WebSocketTimestampCallback() {
                @Override
                public void onSuccess(long timestamp) {
                    if (mSyncProcessing) {
                        mCachedSendTime = timestamp;
                    } else {
                        mCore.setMessageSendSyncTime(timestamp);
                    }
                    m.setContentType(RecallInfoMessage.CONTENT_TYPE);
                    RecallInfoMessage recallInfoMessage = new RecallInfoMessage();
                    m.setContent(recallInfoMessage);
                    mCore.getDbManager().updateMessageContent(recallInfoMessage, m.getContentType(), messageId);
                    //通知会话更新
                    notifyMessageRemoved(m.getConversation(), (ConcreteMessage) m);
                    if (callback != null) {
                        callback.onSuccess(m);
                    }
                }

                @Override
                public void onError(int errorCode) {
                    if (callback != null) {
                        callback.onError(errorCode);
                    }
                }
            });
        } else {
            if (callback != null) {
                callback.onError(JErrorCode.MESSAGE_NOT_EXIST);
            }
        }
    }

    @Override
    public void getRemoteMessages(Conversation conversation, int count, long startTime, JetIMConst.PullDirection direction, IGetMessagesCallback callback) {
        if (count > 100) {
            count = 100;
        }
        mCore.getWebSocket().queryHisMsg(conversation, startTime, count, direction, new QryHisMsgCallback() {
            @Override
            public void onSuccess(List<ConcreteMessage> messages, boolean isFinished) {
                mCore.getDbManager().insertMessages(messages);
                if (callback != null) {
                    List<Message> result = new ArrayList<>(messages);
                    callback.onSuccess(result);
                }
            }

            @Override
            public void onError(int errorCode) {
                if (callback != null) {
                    callback.onError(errorCode);
                }
            }
        });
    }

    @Override
    public void getLocalAndRemoteMessages(Conversation conversation, int count, long startTime, JetIMConst.PullDirection direction, IGetLocalAndRemoteMessagesCallback callback) {
        if (count <= 0) {
            if (callback != null) {
                callback.onGetLocalList(new ArrayList<>(), false);
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
            callback.onGetLocalList(localMessages, needRemote);
        }
        if (needRemote) {
            getRemoteMessages(conversation, count, startTime, direction, new IGetMessagesCallback() {
                @Override
                public void onSuccess(List<Message> messages) {
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
                        callback.onGetRemoteList(mergeList);
                    }
                }

                @Override
                public void onError(int errorCode) {
                    if (callback != null) {
                        callback.onGetRemoteListError(errorCode);
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
        mCore.getWebSocket().sendReadReceipt(conversation, messageIds, new WebSocketSimpleCallback() {
            @Override
            public void onSuccess() {
                mCore.getDbManager().setMessagesRead(messageIds);
                if (callback != null) {
                    callback.onSuccess();
                }
            }

            @Override
            public void onError(int errorCode) {
                if (callback != null) {
                    callback.onError(errorCode);
                }
            }
        });
    }

    @Override
    public void getGroupMessageReadDetail(Conversation conversation, String messageId, IGetGroupMessageReadDetailCallback callback) {
        mCore.getWebSocket().getGroupMessageReadDetail(conversation, messageId, new QryReadDetailCallback() {
            @Override
            public void onSuccess(List<UserInfo> readMembers, List<UserInfo> unreadMembers) {
                GroupMessageReadInfo info = new GroupMessageReadInfo();
                info.setReadCount(readMembers.size());
                info.setMemberCount(readMembers.size() + unreadMembers.size());
                mCore.getDbManager().setGroupMessageReadInfo(new HashMap<String, GroupMessageReadInfo>() {
                    {
                        put(messageId, info);
                    }
                });
                if (callback != null) {
                    callback.onSuccess(readMembers, unreadMembers);
                }
            }

            @Override
            public void onError(int errorCode) {
                if (callback != null) {
                    callback.onError(errorCode);
                }
            }
        });
    }

    @Override
    public void getMergedMessageList(String messageId, IGetMessagesCallback callback) {
        mCore.getWebSocket().getMergedMessageList(messageId, 0, 100, JetIMConst.PullDirection.OLDER, new QryHisMsgCallback() {
            @Override
            public void onSuccess(List<ConcreteMessage> messages, boolean isFinished) {
                mCore.getDbManager().insertMessages(messages);
                if (callback != null) {
                    List<Message> result = new ArrayList<>(messages);
                    callback.onSuccess(result);
                }
            }

            @Override
            public void onError(int errorCode) {
                if (callback != null) {
                    callback.onError(errorCode);
                }
            }
        });
    }

    @Override
    public void getMentionMessageList(Conversation conversation, int count, long time, JetIMConst.PullDirection direction, IGetMessagesCallback callback) {
        mCore.getWebSocket().getMentionMessageList(conversation, time, count, direction, new QryHisMsgCallback() {
            @Override
            public void onSuccess(List<ConcreteMessage> messages, boolean isFinished) {
                mCore.getDbManager().insertMessages(messages);
                if (callback != null) {
                    List<Message> result = new ArrayList<>(messages);
                    callback.onSuccess(result);
                }
            }

            @Override
            public void onError(int errorCode) {
                if (callback != null) {
                    callback.onError(errorCode);
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
                callback.onComplete();
            }
            return;
        }
        List mergedMessages = null;
        if (content instanceof MergeMessage) {
            MergeMessage mergeMessage = (MergeMessage) content;
            mergedMessages = mCore.getDbManager().getMessagesByMessageIds(mergeMessage.getMessageIdList());
        }
        loopBroadcastMessage(content, conversations, mergedMessages, 0, conversations.size(), callback);
    }

    private void loopBroadcastMessage(MessageContent content,
                                      List<Conversation> conversations,
                                      List<ConcreteMessage> mergedMessages,
                                      int processCount,
                                      int totalCount,
                                      IBroadcastMessageCallback callback) {
        if (conversations.size() == 0) {
            if (callback != null) {
                callback.onComplete();
            }
            return;
        }
        sendMessage(content, conversations.get(0), mergedMessages, true, new ISendMessageCallback() {
            @Override
            public void onSuccess(Message message) {
                broadcastCallbackAndLoopNext(message, JErrorCode.NONE, conversations, mergedMessages, processCount, totalCount, callback);
            }

            @Override
            public void onError(Message message, int errorCode) {
                broadcastCallbackAndLoopNext(message, errorCode, conversations, mergedMessages, processCount, totalCount, callback);
            }
        });
    }

    private void broadcastCallbackAndLoopNext(Message message,
                                              int errorCode,
                                              List<Conversation> conversations,
                                              List<ConcreteMessage> mergedMessages,
                                              int processCount,
                                              int totalCount,
                                              IBroadcastMessageCallback callback) {
        if (callback != null) {
            callback.onProgress(message, errorCode, processCount, totalCount);
        }
        if (conversations.size() <= 1) {
            if (callback != null) {
                callback.onComplete();
            }
        } else {
            conversations.remove(0);
            mCore.getSendHandler().postDelayed(() -> loopBroadcastMessage(message.getContent(), conversations, mergedMessages, processCount + 1, totalCount, callback), 50);
        }
    }

    @Override
    public void setMessageState(long clientMsgNo, Message.MessageState state) {
        mCore.getDbManager().setMessageState(clientMsgNo, state);
    }

    @Override
    public void registerContentType(Class<? extends MessageContent> messageContentClass) {
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

    interface ISendReceiveListener {
        void onMessageSave(ConcreteMessage message);

        void onMessageSend(ConcreteMessage message);

        void onMessageReceive(ConcreteMessage message);

        void onMessageRemoved(Conversation conversation, List<ConcreteMessage> removedMessages, ConcreteMessage lastedMessage);

        void onConversationsDelete(List<Conversation> conversations);

        void onConversationsUnreadUpdate(List<ConcreteConversationInfo> conversations);
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
        if (!mHasSetMessageListener) {
            mHasSetMessageListener = true;
            if (mCore.getWebSocket() != null) {
                mCore.getWebSocket().setMessageListener(new JWebSocket.IWebSocketMessageListener() {
                    @Override
                    public void onMessageReceive(ConcreteMessage message) {
                        List<ConcreteMessage> list = new ArrayList<>();
                        list.add(message);
                        handleReceiveMessages(list, false);
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
                                    entry.getValue().onMessageSyncComplete();
                                }
                            }
                        }
                    }

                    @Override
                    public void onSyncNotify(long syncTime) {
                        LoggerUtils.d("onSyncNotify, syncTime is " + syncTime + ", receiveSyncTime is " + mCore.getMessageReceiveTime());
                        if (syncTime > mCore.getMessageReceiveTime()) {
                            mSyncProcessing = true;
                            sync();
                        }

                    }
                });
            }
        }
        sync();
    }

    private List<ConcreteMessage> messagesToSave(List<ConcreteMessage> messages) {
        List<ConcreteMessage> list = new ArrayList<>();
        for (ConcreteMessage message : messages) {
            if ((message.getFlags() & MessageContent.MessageFlag.IS_SAVE.getValue()) != 0) {
                list.add(message);
            }
        }
        return list;
    }

    private Message handleRecallCmdMessage(String messageId) {
        RecallInfoMessage recallInfoMessage = new RecallInfoMessage();
        mCore.getDbManager().updateMessageContent(recallInfoMessage, RecallInfoMessage.CONTENT_TYPE, messageId);
        List<String> ids = new ArrayList<>(1);
        ids.add(messageId);
        List<Message> messages = mCore.getDbManager().getMessagesByMessageIds(ids);
        if (messages.size() > 0) {
            return messages.get(0);
        }
        return null;
    }

    private void handleReceiveMessages(List<ConcreteMessage> messages, boolean isSync) {
        List<ConcreteMessage> messagesToSave = messagesToSave(messages);
        mCore.getDbManager().insertMessages(messagesToSave);
        updateUserInfo(messagesToSave);

        long sendTime = 0;
        long receiveTime = 0;
        Map<String, UserInfo> userInfoMap = new HashMap<>();
        for (ConcreteMessage message : messages) {
            if (message.getDirection() == Message.MessageDirection.SEND) {
                sendTime = message.getTimestamp();
            } else if (message.getDirection() == Message.MessageDirection.RECEIVE) {
                receiveTime = message.getTimestamp();
            }

            //recall message
            if (message.getContentType().equals(RecallCmdMessage.CONTENT_TYPE)) {
                RecallCmdMessage cmd = (RecallCmdMessage) message.getContent();
                Message recallMessage = handleRecallCmdMessage(cmd.getOriginalMessageId());
                //recallMessage 为空表示被撤回的消息本地不存在，不需要回调
                if (recallMessage != null) {
                    if (mListenerMap != null) {
                        for (Map.Entry<String, IMessageListener> entry : mListenerMap.entrySet()) {
                            entry.getValue().onMessageRecall(recallMessage);
                        }
                    }
                }
                continue;
            }

            //delete conversation
            if (message.getContentType().equals(DeleteConvMessage.CONTENT_TYPE)) {
                DeleteConvMessage deleteConvMessage = (DeleteConvMessage) message.getContent();
                for (Conversation deleteConv : deleteConvMessage.getConversations()) {
                    mCore.getDbManager().deleteConversationInfo(deleteConv);
                }
                if (mSendReceiveListener != null) {
                    mSendReceiveListener.onConversationsDelete(deleteConvMessage.getConversations());
                }
                continue;
            }

            //read ntf
            if (message.getContentType().equals(ReadNtfMessage.CONTENT_TYPE)) {
                ReadNtfMessage readNtfMessage = (ReadNtfMessage) message.getContent();
                mCore.getDbManager().setMessagesRead(readNtfMessage.getMessageIds());
                if (mReadReceiptListenerMap != null) {
                    for (Map.Entry<String, IMessageReadReceiptListener> entry : mReadReceiptListenerMap.entrySet()) {
                        entry.getValue().onMessagesRead(message.getConversation(), readNtfMessage.getMessageIds());
                    }
                }
                continue;
            }

            //group read ntf
            if (message.getContentType().equals(GroupReadNtfMessage.CONTENT_TYPE)) {
                GroupReadNtfMessage groupReadNtfMessage = (GroupReadNtfMessage) message.getContent();
                mCore.getDbManager().setGroupMessageReadInfo(groupReadNtfMessage.getMessages());
                if (mReadReceiptListenerMap != null) {
                    for (Map.Entry<String, IMessageReadReceiptListener> entry : mReadReceiptListenerMap.entrySet()) {
                        entry.getValue().onGroupMessagesRead(message.getConversation(), groupReadNtfMessage.getMessages());
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
                handleDeleteMsgMessageCmdMessage(deleteMsgMessage.getMsgIdList());
                continue;
            }

            //clear unread message
            if (message.getContentType().equals(ClearUnreadMessage.CONTENT_TYPE)) {
                ClearUnreadMessage clearUnreadMessage = (ClearUnreadMessage) message.getContent();
                handleClearUnreadMessageCmdMessage(clearUnreadMessage.getConversations());
                continue;
            }

            if ((message.getFlags() & MessageContent.MessageFlag.IS_CMD.getValue()) != 0) {
                continue;
            }
            if (message.isExisted()) {
                continue;
            }

            if (message.getContent() != null
                    && message.getContent().getMentionInfo() != null
                    && message.getContent().getMentionInfo().getTargetUsers() != null) {
                for (UserInfo userInfo : message.getContent().getMentionInfo().getTargetUsers()) {
                    if (userInfo.getUserId() != null) {
                        userInfoMap.put(userInfo.getUserId(), userInfo);
                    }
                }
            }

            if (mSendReceiveListener != null) {
                mSendReceiveListener.onMessageReceive(message);
            }

            if (mListenerMap != null) {
                for (Map.Entry<String, IMessageListener> entry : mListenerMap.entrySet()) {
                    entry.getValue().onMessageReceive(message);
                }
            }
        }
        mCore.getDbManager().insertUserInfoList(new ArrayList<>(userInfoMap.values()));
        ////直发的消息，而且正在同步中，不直接更新 sync time
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
            for (Map.Entry<String, IMessageListener> entry : mListenerMap.entrySet()) {
                entry.getValue().onMessageClear(conversation, startTime, senderId);
            }
        }
        //通知会话更新
        notifyMessageRemoved(conversation, null);
    }

    private void handleDeleteMsgMessageCmdMessage(List<String> msgIds) {
        //查询消息
        List<Message> messages = getMessagesByMessageIds(msgIds);
        if (messages.isEmpty()) return;
        //删除消息
        mCore.getDbManager().deleteMessageByMessageId(msgIds);
        //通知消息回调
        if (mListenerMap != null) {
            for (int i = 0; i < messages.size(); i++) {
                for (Map.Entry<String, IMessageListener> entry : mListenerMap.entrySet()) {
                    entry.getValue().onMessageDelete(messages.get(i).getClientMsgNo());
                }
            }
        }
        //通知会话更新
        if (mSendReceiveListener != null) {
            //合并同一个会话中的被删除消息
            ArrayMap<Conversation, List<ConcreteMessage>> conversationMap = new ArrayMap<>();
            for (int i = 0; i < messages.size(); i++) {
                Message deletedMessage = messages.get(i);
                List<ConcreteMessage> removedList = conversationMap.get(deletedMessage.getConversation());
                if (removedList == null) {
                    removedList = new ArrayList<>();
                    conversationMap.put(deletedMessage.getConversation(), removedList);
                }
                removedList.add((ConcreteMessage) deletedMessage);
            }
            //遍历会话通知会话更新最新信息
            for (Conversation conversation : conversationMap.keySet()) {
                Message lastedMessage = mCore.getDbManager().getLatestMessages(conversation);
                List<ConcreteMessage> removedList = conversationMap.get(conversation);
                mSendReceiveListener.onMessageRemoved(conversation, removedList, lastedMessage == null ? null : (ConcreteMessage) lastedMessage);
            }
        }
    }

    private void handleClearUnreadMessageCmdMessage(List<ConcreteConversationInfo> conversations) {
        if (mSendReceiveListener != null) {
            mSendReceiveListener.onConversationsUnreadUpdate(conversations);
        }
    }

    //通知会话更新最新信息
    private void notifyMessageRemoved(Conversation conversation, ConcreteMessage removedMessage) {
        if (mSendReceiveListener != null) {
            //获取当前会话最新一条消息
            Message lastedMessage = mCore.getDbManager().getLatestMessages(conversation);
            List<ConcreteMessage> removedList = new ArrayList<>();
            if (removedMessage != null) {
                removedList.add(removedMessage);
            }
            mSendReceiveListener.onMessageRemoved(conversation, removedList, lastedMessage == null ? null : (ConcreteMessage) lastedMessage);
        }
    }

    private void sync() {
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
        }
        mCore.getDbManager().insertUserInfoList(new ArrayList<>(userInfoMap.values()));
        mCore.getDbManager().insertGroupInfoList(new ArrayList<>(groupInfoMap.values()));
    }

    private String createClientUid() {
        long result = System.currentTimeMillis();
        result = result % 1000000;
        result = result * 1000 + mIncreaseId++;
        return Long.toString(result);
    }

    private int mIncreaseId = 0;
    private boolean mSyncProcessing = false;
    private long mCachedReceiveTime = -1;
    private long mCachedSendTime = -1;
    private boolean mHasSetMessageListener = false;
    private ConcurrentHashMap<String, IMessageListener> mListenerMap;
    private ConcurrentHashMap<String, IMessageSyncListener> mSyncListenerMap;
    private ConcurrentHashMap<String, IMessageReadReceiptListener> mReadReceiptListenerMap;
    private ISendReceiveListener mSendReceiveListener;
}
