package com.juggle.im.internal;

import android.text.TextUtils;

import com.juggle.im.JErrorCode;
import com.juggle.im.JIMConst;
import com.juggle.im.interfaces.IConversationManager;
import com.juggle.im.internal.core.JIMCore;
import com.juggle.im.internal.core.db.DBManager;
import com.juggle.im.internal.core.network.AddConversationCallback;
import com.juggle.im.internal.core.network.SyncConversationsCallback;
import com.juggle.im.internal.core.network.WebSocketTimestampCallback;
import com.juggle.im.internal.model.ConcreteConversationInfo;
import com.juggle.im.internal.model.ConcreteMessage;
import com.juggle.im.internal.model.messages.ClearUnreadMessage;
import com.juggle.im.internal.model.messages.TopConvMessage;
import com.juggle.im.internal.model.messages.UnDisturbConvMessage;
import com.juggle.im.internal.util.JLogger;
import com.juggle.im.model.Conversation;
import com.juggle.im.model.ConversationInfo;
import com.juggle.im.model.ConversationMentionInfo;
import com.juggle.im.model.GroupInfo;
import com.juggle.im.model.Message;
import com.juggle.im.model.MessageContent;
import com.juggle.im.model.MessageMentionInfo;
import com.juggle.im.model.UserInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class ConversationManager implements IConversationManager, MessageManager.ISendReceiveListener {

    public ConversationManager(JIMCore core, UserInfoManager userInfoManager, MessageManager messageManager) {
        this.mCore = core;
        this.mUserInfoManager = userInfoManager;
        this.mMessageManager = messageManager;
        this.mCachedSyncTime = -1;
    }

    @Override
    public void createConversationInfo(Conversation conversation, ICreateConversationInfoCallback callback) {
        if (mCore.getWebSocket() == null) {
            int errorCode = JErrorCode.CONNECTION_UNAVAILABLE;
            JLogger.e("CONV-Create", "fail, code is " + errorCode);
            if (callback != null) {
                mCore.getCallbackHandler().post(() -> callback.onError(errorCode));
            }
            return;
        }
        mCore.getWebSocket().addConversationInfo(conversation, mCore.getUserId(), new AddConversationCallback() {
            @Override
            public void onSuccess(long timestamp, ConcreteConversationInfo conversationInfo) {
                JLogger.i("CONV-Create", "success");
                mMessageManager.updateMessageSendSyncTime(timestamp);
                ConcreteConversationInfo added = doConversationsAdd(conversationInfo);
                if (callback != null) {
                    mCore.getCallbackHandler().post(() -> callback.onSuccess(added));
                }
            }

            @Override
            public void onError(int errorCode) {
                JLogger.e("CONV-Create", "fail, code is " + errorCode);
                if (callback != null) {
                    mCore.getCallbackHandler().post(() -> callback.onError(errorCode));
                }
            }
        });
    }

    @Override
    public List<ConversationInfo> getConversationInfoList() {
        return mCore.getDbManager().getConversationInfoList();
    }

    @Override
    public List<ConversationInfo> getConversationInfoList(int[] conversationTypes, int count, long timestamp, JIMConst.PullDirection direction) {
        return mCore.getDbManager().getConversationInfoList(conversationTypes, count, timestamp, direction);
    }

    @Override
    public List<ConversationInfo> getConversationInfoList(int count, long timestamp, JIMConst.PullDirection direction) {
        return getConversationInfoList(null, count, timestamp, direction);
    }

    @Override
    public ConversationInfo getConversationInfo(Conversation conversation) {
        return mCore.getDbManager().getConversationInfo(conversation);
    }

    @Override
    public void deleteConversationInfo(Conversation conversation, ISimpleCallback callback) {
        if (mCore.getWebSocket() == null) {
            int errorCode = JErrorCode.CONNECTION_UNAVAILABLE;
            JLogger.e("CONV-Delete", "fail, code is " + errorCode);
            if (callback != null) {
                mCore.getCallbackHandler().post(() -> callback.onError(errorCode));
            }
            return;
        }
        mCore.getWebSocket().deleteConversationInfo(conversation, mCore.getUserId(), new WebSocketTimestampCallback() {
            @Override
            public void onSuccess(long timestamp) {
                JLogger.i("CONV-Delete", "success");
                mMessageManager.updateMessageSendSyncTime(timestamp);
                ConversationInfo conversationInfo = mCore.getDbManager().getConversationInfo(conversation);
                List<Conversation> deleteList = new ArrayList<>();
                deleteList.add(conversation);
                mCore.getDbManager().deleteConversationInfo(deleteList);
                if (callback != null) {
                    mCore.getCallbackHandler().post(callback::onSuccess);
                }
                if (conversationInfo != null && mListenerMap != null) {
                    List<ConversationInfo> list = new ArrayList<>();
                    list.add(conversationInfo);
                    for (Map.Entry<String, IConversationListener> entry : mListenerMap.entrySet()) {
                        mCore.getCallbackHandler().post(() -> entry.getValue().onConversationInfoDelete(list));
                    }
                }
            }

            @Override
            public void onError(int errorCode) {
                JLogger.e("CONV-Delete", "fail, code is " + errorCode);
                if (callback != null) {
                    mCore.getCallbackHandler().post(() -> callback.onError(errorCode));
                }
            }
        });
    }

    @Override
    public void setDraft(Conversation conversation, String draft) {
        mCore.getDbManager().setDraft(conversation, draft);
        ConversationInfo info = mCore.getDbManager().getConversationInfo(conversation);
        if (mListenerMap != null && info != null) {
            List<ConversationInfo> l = new ArrayList<>();
            l.add(info);
            for (Map.Entry<String, IConversationListener> entry : mListenerMap.entrySet()) {
                mCore.getCallbackHandler().post(() -> entry.getValue().onConversationInfoUpdate(l));
            }
        }
    }

    @Override
    public void clearDraft(Conversation conversation) {
        setDraft(conversation, "");
    }

    @Override
    public void setMute(Conversation conversation, boolean isMute, ISimpleCallback callback) {
        if (mCore.getWebSocket() == null) {
            int errorCode = JErrorCode.CONNECTION_UNAVAILABLE;
            JLogger.e("CONV-Mute", "fail, code is " + errorCode);
            if (callback != null) {
                mCore.getCallbackHandler().post(() -> callback.onError(errorCode));
            }
            return;
        }
        mCore.getWebSocket().setMute(conversation, isMute, mCore.getUserId(), new WebSocketTimestampCallback() {
            @Override
            public void onSuccess(long timestamp) {
                JLogger.i("CONV-Mute", "success");
                mMessageManager.updateMessageSendSyncTime(timestamp);
                mCore.getDbManager().setMute(conversation, isMute);
                if (callback != null) {
                    mCore.getCallbackHandler().post(callback::onSuccess);
                }
                ConversationInfo conversationInfo = mCore.getDbManager().getConversationInfo(conversation);
                if (conversationInfo != null && mListenerMap != null) {
                    List<ConversationInfo> list = new ArrayList<>();
                    list.add(conversationInfo);
                    for (Map.Entry<String, IConversationListener> entry : mListenerMap.entrySet()) {
                        mCore.getCallbackHandler().post(() -> entry.getValue().onConversationInfoUpdate(list));
                    }
                }
                noticeTotalUnreadCountChange();
            }

            @Override
            public void onError(int errorCode) {
                JLogger.e("CONV-Mute", "fail, code is " + errorCode);
                if (callback != null) {
                    mCore.getCallbackHandler().post(() -> callback.onError(errorCode));
                }
            }
        });
    }

    @Override
    public void setTop(Conversation conversation, boolean isTop, ISimpleCallback callback) {
        if (mCore.getWebSocket() == null) {
            int errorCode = JErrorCode.CONNECTION_UNAVAILABLE;
            JLogger.e("CONV-Top", "fail, code is " + errorCode);
            if (callback != null) {
                mCore.getCallbackHandler().post(() -> callback.onError(errorCode));
            }
            return;
        }
        mCore.getWebSocket().setTop(conversation, isTop, mCore.getUserId(), new WebSocketTimestampCallback() {
            @Override
            public void onSuccess(long timestamp) {
                JLogger.i("CONV-Top", "success");
                mMessageManager.updateMessageSendSyncTime(timestamp);
                mCore.getDbManager().setTop(conversation, isTop, timestamp);
                if (callback != null) {
                    mCore.getCallbackHandler().post(callback::onSuccess);
                }
                ConversationInfo conversationInfo = mCore.getDbManager().getConversationInfo(conversation);
                if (conversationInfo != null && mListenerMap != null) {
                    List<ConversationInfo> list = new ArrayList<>();
                    list.add(conversationInfo);
                    for (Map.Entry<String, IConversationListener> entry : mListenerMap.entrySet()) {
                        mCore.getCallbackHandler().post(() -> entry.getValue().onConversationInfoUpdate(list));
                    }
                }
            }

            @Override
            public void onError(int errorCode) {
                JLogger.e("CONV-Top", "fail, code is " + errorCode);
                if (callback != null) {
                    mCore.getCallbackHandler().post(() -> callback.onError(errorCode));
                }
            }
        });
    }

    @Override
    public List<ConversationInfo> getTopConversationInfoList(int count, long timestamp, JIMConst.PullDirection direction) {
        return mCore.getDbManager().getTopConversationInfoList(null, count, timestamp, direction);
    }

    @Override
    public int getTotalUnreadCount() {
        return mCore.getDbManager().getTotalUnreadCount();
    }

    @Override
    public void clearUnreadCount(Conversation conversation, ISimpleCallback callback) {
        if (mCore.getWebSocket() == null) {
            int errorCode = JErrorCode.CONNECTION_UNAVAILABLE;
            JLogger.e("CONV-ClearUnread", "fail, code is " + errorCode);
            if (callback != null) {
                mCore.getCallbackHandler().post(() -> callback.onError(errorCode));
            }
            return;
        }
        ConcreteConversationInfo info = mCore.getDbManager().getConversationInfo(conversation);
        if (info == null) {
            int errorCode = JErrorCode.INVALID_PARAM;
            JLogger.e("CONV-ClearUnread", "fail, code is " + errorCode);
            if (callback != null) {
                mCore.getCallbackHandler().post(() -> callback.onError(errorCode));
            }
            return;
        }
        mCore.getWebSocket().clearUnreadCount(conversation, mCore.getUserId(), info.getLastMessageIndex(), new WebSocketTimestampCallback() {
            @Override
            public void onSuccess(long timestamp) {
                JLogger.i("CONV-ClearUnread", "success");
                mMessageManager.updateMessageSendSyncTime(timestamp);
                mCore.getDbManager().clearUnreadCount(conversation, info.getLastMessageIndex());
                mCore.getDbManager().setMentionInfo(conversation, "");
                if (callback != null) {
                    mCore.getCallbackHandler().post(callback::onSuccess);
                }
                noticeTotalUnreadCountChange();
                if (mListenerMap != null) {
                    info.setLastReadMessageIndex(info.getLastMessageIndex());
                    info.setUnreadCount(0);
                    info.setMentionInfo(null);

                    List<ConversationInfo> list = new ArrayList<>();
                    list.add(info);
                    for (Map.Entry<String, IConversationListener> entry : mListenerMap.entrySet()) {
                        mCore.getCallbackHandler().post(() -> entry.getValue().onConversationInfoUpdate(list));
                    }
                }
            }

            @Override
            public void onError(int errorCode) {
                JLogger.e("CONV-ClearUnread", "fail, code is " + errorCode);
                if (callback != null) {
                    mCore.getCallbackHandler().post(() -> callback.onError(errorCode));
                }
            }
        });
    }

    @Override
    public void clearTotalUnreadCount(ISimpleCallback callback) {
        if (mCore.getWebSocket() == null) {
            int errorCode = JErrorCode.CONNECTION_UNAVAILABLE;
            JLogger.e("CONV-ClearTotal", "fail, code is " + errorCode);
            if (callback != null) {
                mCore.getCallbackHandler().post(() -> callback.onError(errorCode));
            }
            return;
        }
        long time = Math.max(mCore.getMessageSendSyncTime(), mCore.getMessageReceiveTime());
        mCore.getWebSocket().clearTotalUnreadCount(mCore.getUserId(), time, new WebSocketTimestampCallback() {
            @Override
            public void onSuccess(long timestamp) {
                JLogger.i("CONV-ClearTotal", "success");
                mMessageManager.updateMessageSendSyncTime(timestamp);
                mCore.getDbManager().clearTotalUnreadCount();
                mCore.getDbManager().clearMentionInfo();
                noticeTotalUnreadCountChange();
                if (callback != null) {
                    mCore.getCallbackHandler().post(callback::onSuccess);
                }
            }

            @Override
            public void onError(int errorCode) {
                JLogger.e("CONV-ClearTotal", "fail, code is " + errorCode);
                if (callback != null) {
                    mCore.getCallbackHandler().post(() -> callback.onError(errorCode));
                }
            }
        });
    }

    @Override
    public void addListener(String key, IConversationListener listener) {
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
    public void addSyncListener(String key, IConversationSyncListener listener) {
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

    void connectSuccess() {
        mSyncProcessing = true;
    }

    void syncConversations(ICompleteCallback callback) {
        if (mCore.getWebSocket() == null) {
            int errorCode = JErrorCode.CONNECTION_UNAVAILABLE;
            JLogger.e("CONV-Sync", "fail, code is " + errorCode);
            if (callback != null) {
                callback.onComplete();
            }
            return;
        }
        mSyncProcessing = true;
        JLogger.i("CONV-Sync", "sync time is " + mCore.getConversationSyncTime());
        mCore.getWebSocket().syncConversations(mCore.getConversationSyncTime(), CONVERSATION_SYNC_COUNT, mCore.getUserId(), new SyncConversationsCallback() {
            @Override
            public void onSuccess(List<ConcreteConversationInfo> conversationInfoList, List<ConcreteConversationInfo> deleteConversationInfoList, boolean isFinished) {
                JLogger.i("CONV-Sync", "success, conversation count is " + (conversationInfoList == null ? 0 : conversationInfoList.size()) + ", delete count is " + (deleteConversationInfoList == null ? 0 : deleteConversationInfoList.size()));
                long syncTime = 0;
                if (conversationInfoList != null && conversationInfoList.size() > 0) {
                    updateUserInfo(conversationInfoList);
                    ConcreteConversationInfo last = conversationInfoList.get(conversationInfoList.size() - 1);
                    if (last.getSyncTime() > syncTime) {
                        syncTime = last.getSyncTime();
                    }
                    mCore.getDbManager().insertConversations(conversationInfoList, (insertList, updateList) -> {
                        if (insertList.size() > 0) {
                            if (mListenerMap != null) {
                                List<ConversationInfo> l = new ArrayList<>(insertList);
                                for (Map.Entry<String, IConversationListener> entry : mListenerMap.entrySet()) {
                                    mCore.getCallbackHandler().post(() -> entry.getValue().onConversationInfoAdd(l));
                                }
                            }
                        }
                        if (updateList.size() > 0) {
                            if (mListenerMap != null) {
                                List<ConversationInfo> l = new ArrayList<>(updateList);
                                for (Map.Entry<String, IConversationListener> entry : mListenerMap.entrySet()) {
                                    mCore.getCallbackHandler().post(() -> entry.getValue().onConversationInfoUpdate(l));

                                }
                            }
                        }
                    });
                    noticeTotalUnreadCountChange();
                }
                if (deleteConversationInfoList != null && deleteConversationInfoList.size() > 0) {
                    updateUserInfo(deleteConversationInfoList);
                    ConcreteConversationInfo last = deleteConversationInfoList.get(deleteConversationInfoList.size() - 1);
                    if (last.getSyncTime() > syncTime) {
                        syncTime = last.getSyncTime();
                    }
                    List<Conversation> deleteConversationList = new ArrayList<>();
                    for (ConcreteConversationInfo info : deleteConversationInfoList) {
                        deleteConversationList.add(info.getConversation());
                    }
                    mCore.getDbManager().deleteConversationInfo(deleteConversationList);
                    List<ConversationInfo> l = new ArrayList<>(deleteConversationInfoList);
                    if (mListenerMap != null) {
                        for (Map.Entry<String, IConversationListener> entry : mListenerMap.entrySet()) {
                            mCore.getCallbackHandler().post(() -> entry.getValue().onConversationInfoDelete(l));
                        }
                    }
                }
                if (syncTime > 0) {
                    mCore.setConversationSyncTime(syncTime);
                }
                if (!isFinished) {
                    syncConversations(callback);
                } else {
                    mSyncProcessing = false;
                    if (mCachedSyncTime > 0) {
                        mCore.setConversationSyncTime(mCachedSyncTime);
                        mCachedSyncTime = -1;
                    }
                    if (mSyncListenerMap != null) {
                        for (Map.Entry<String, IConversationSyncListener> entry : mSyncListenerMap.entrySet()) {
                            mCore.getCallbackHandler().post(() -> entry.getValue().onConversationSyncComplete());
                        }
                    }
                    if (callback != null) {
                        callback.onComplete();
                    }
                }
            }

            @Override
            public void onError(int errorCode) {
                JLogger.e("CONV-Sync", "fail, code is " + errorCode);
                if (callback != null) {
                    callback.onComplete();
                }
            }
        });
    }

    @Override
    public void onMessageSave(ConcreteMessage message) {
        addOrUpdateConversationIfNeed(message);
    }

    @Override
    public void onMessageSend(ConcreteMessage message) {
        addOrUpdateConversationIfNeed(message);
        updateSyncTime(message.getTimestamp());
    }

    @Override
    public void onMessageReceive(List<ConcreteMessage> messages) {
        if (messages.isEmpty()) return;

        addOrUpdateConversationIfNeed(messages);
        updateSyncTime(messages.get(messages.size() - 1).getTimestamp());
        noticeTotalUnreadCountChange();
    }

    @Override
    public void onMessagesRead(Conversation conversation, List<String> messageIds) {
        //判空
        if (conversation == null) return;
        if (messageIds == null || messageIds.isEmpty()) return;
        //查询会话
        ConversationInfo conversationInfo = getConversationInfo(conversation);
        if (conversationInfo == null || conversationInfo.getLastMessage() == null || TextUtils.isEmpty(conversationInfo.getLastMessage().getMessageId()))
            return;
        //如果已读消息列表中包含会话的最新一条消息，需要更新会话
        if (messageIds.contains(conversationInfo.getLastMessage().getMessageId())) {
            //更新conversationInfo
            conversationInfo.getLastMessage().setHasRead(true);
            //更新数据库
            mCore.getDbManager().updateConversationLastMessageHasRead(conversation, conversationInfo.getLastMessage().getMessageId(), true);
            //执行回调
            if (mListenerMap != null) {
                List<ConversationInfo> result = new ArrayList<>();
                result.add(conversationInfo);
                for (Map.Entry<String, IConversationListener> entry : mListenerMap.entrySet()) {
                    mCore.getCallbackHandler().post(() -> entry.getValue().onConversationInfoUpdate(result));
                }
            }
        }
    }

    @Override
    public void onMessagesSetState(Conversation conversation, long clientMsgNo, Message.MessageState state) {
        //判空
        if (conversation == null) return;
        if (clientMsgNo < 0 || state == null) return;
        //查询会话
        ConversationInfo conversationInfo = getConversationInfo(conversation);
        if (conversationInfo == null || conversationInfo.getLastMessage() == null || conversationInfo.getLastMessage().getClientMsgNo() < 0)
            return;
        //判断是否需要更新会话
        if (clientMsgNo == conversationInfo.getLastMessage().getClientMsgNo()) {
            //更新conversationInfo
            conversationInfo.getLastMessage().setState(state);
            //更新数据库
            mCore.getDbManager().updateConversationLastMessageState(conversation, conversationInfo.getLastMessage().getClientMsgNo(), state);
            //执行回调
            if (mListenerMap != null) {
                List<ConversationInfo> result = new ArrayList<>();
                result.add(conversationInfo);
                for (Map.Entry<String, IConversationListener> entry : mListenerMap.entrySet()) {
                    mCore.getCallbackHandler().post(() -> entry.getValue().onConversationInfoUpdate(result));
                }
            }
        }
    }

    @Override
    public void onMessageRemove(Conversation conversation, List<ConcreteMessage> removedMessages, ConcreteMessage lastMessage) {
        updateConversationAfterRemove(conversation, removedMessages, lastMessage);
    }

    @Override
    public void onMessageClear(Conversation conversation, long startTime, String sendUserId, ConcreteMessage lastMessage) {
        updateConversationAfterClear(conversation, startTime, sendUserId, lastMessage);
    }

    @Override
    public void onConversationsAdd(ConcreteConversationInfo conversationInfo) {
        doConversationsAdd(conversationInfo);
    }

    @Override
    public void onConversationsDelete(List<Conversation> conversations) {
        List<ConversationInfo> results = new ArrayList<>();
        for (Conversation conversation : conversations) {
            ConversationInfo info = new ConversationInfo();
            info.setConversation(conversation);
            results.add(info);
        }
        if (mListenerMap != null) {
            for (Map.Entry<String, IConversationListener> entry : mListenerMap.entrySet()) {
                mCore.getCallbackHandler().post(() -> entry.getValue().onConversationInfoDelete(results));
            }
        }
    }

    @Override
    public void onConversationsUpdate(String updateType, List<ConcreteConversationInfo> conversations) {
        if (updateType == null) return;
        if (conversations == null) return;
        //标记总未读数是否有变更
        boolean totalUnreadCountHasChanged = false;
        //声明一个列表来保存有更新的会话
        List<ConversationInfo> infoList = new ArrayList<>();
        //遍历需要更新的会话列表
        boolean hasUpdate; //标记当前会话是否有变更
        for (int i = 0; i < conversations.size(); i++) {
            //标记当前会话是否有变更
            hasUpdate = true;
            ConcreteConversationInfo conversation = conversations.get(i);
            switch (updateType) {
                case ClearUnreadMessage.CONTENT_TYPE:
                    if (!totalUnreadCountHasChanged) totalUnreadCountHasChanged = true;
                    mCore.getDbManager().clearUnreadCount(conversation.getConversation(), conversation.getLastReadMessageIndex());
                    mCore.getDbManager().setMentionInfo(conversation.getConversation(), "");
                    break;
                case TopConvMessage.CONTENT_TYPE:
                    mCore.getDbManager().setTop(conversation.getConversation(), conversation.isTop(), conversation.getTopTime());
                    break;
                case UnDisturbConvMessage.CONTENT_TYPE:
                    if (!totalUnreadCountHasChanged) totalUnreadCountHasChanged = true;
                    mCore.getDbManager().setMute(conversation.getConversation(), conversation.isMute());
                    break;
                default:
                    hasUpdate = false;
                    break;
            }
            if (hasUpdate) {
                ConversationInfo info = mCore.getDbManager().getConversationInfo((conversation.getConversation()));
                if (info != null) {
                    infoList.add(info);
                }
            }
        }
        //通知更新会话
        if (!infoList.isEmpty() && mListenerMap != null) {
            for (Map.Entry<String, IConversationListener> entry : mListenerMap.entrySet()) {
                mCore.getCallbackHandler().post(() -> entry.getValue().onConversationInfoUpdate(infoList));
            }
        }
        //通知更新总未读数
        if (totalUnreadCountHasChanged) noticeTotalUnreadCountChange();
    }

    @Override
    public void onConversationsClearTotalUnread(long clearTime) {
        mCore.getDbManager().clearTotalUnreadCount();
        mCore.getDbManager().clearMentionInfo();
        noticeTotalUnreadCountChange();
    }

    interface ICompleteCallback {
        void onComplete();
    }

    private void addOrUpdateConversationIfNeed(ConcreteMessage message) {
        List<ConcreteMessage> singleMessageList = new ArrayList<>();
        singleMessageList.add(message);
        addOrUpdateConversationIfNeed(singleMessageList);
    }

    private void addOrUpdateConversationIfNeed(List<ConcreteMessage> messages) {
        //逐条处理消息
        Map<Conversation, ConcreteConversationInfo> conversationInfoMap = new HashMap<>();
        for (ConcreteMessage message : messages) {
            processSingleMessage(message, conversationInfoMap);
        }
        if (conversationInfoMap.isEmpty()) return;
        //统一更新数据库
        mCore.getDbManager().insertConversations(new ArrayList<>(conversationInfoMap.values()), new DBManager.IDbInsertConversationsCallback() {
            @Override
            public void onComplete(List<ConcreteConversationInfo> insertList, List<ConcreteConversationInfo> updateList) {
                //通知回调
                if (mListenerMap == null) return;
                if (!insertList.isEmpty()) {
                    List<ConversationInfo> l = new ArrayList<>(insertList);
                    for (Map.Entry<String, IConversationListener> entry : mListenerMap.entrySet()) {
                        mCore.getCallbackHandler().post(() -> entry.getValue().onConversationInfoAdd(l));
                    }
                }
                if (!updateList.isEmpty()) {
                    List<ConversationInfo> l = new ArrayList<>(updateList);
                    for (Map.Entry<String, IConversationListener> entry : mListenerMap.entrySet()) {
                        mCore.getCallbackHandler().post(() -> entry.getValue().onConversationInfoUpdate(l));

                    }
                }
            }
        });
    }

    //公共的处理单个消息的方法
    private void processSingleMessage(ConcreteMessage message, Map<Conversation, ConcreteConversationInfo> conversationInfoMap) {
        //提取ConversationMentionInfo
        ConversationMentionInfo mentionInfo = getConversationMentionInfo(message);
        //判断是否是广播消息
        boolean isBroadcast = (message.getFlags() & MessageContent.MessageFlag.IS_BROADCAST.getValue()) != 0;
        //查询会话
        ConcreteConversationInfo info = conversationInfoMap.get(message.getConversation());
        if (info == null) {
            info = (ConcreteConversationInfo) getConversationInfo(message.getConversation());
            if (info != null) conversationInfoMap.put(message.getConversation(), info);
        }
        //如果会话不存在
        if (info == null) {
            info = new ConcreteConversationInfo();
            info.setConversation(message.getConversation());
            if (isBroadcast && message.getDirection() == Message.MessageDirection.SEND) {
                info.setSortTime(0);
            } else {
                info.setSortTime(message.getTimestamp());
            }
            info.setLastMessage(message);
            if (message.getMsgIndex() > 0) {
                info.setLastMessageIndex(message.getMsgIndex());
                info.setLastReadMessageIndex(message.getMsgIndex() - 1);
                info.setUnreadCount(1);
            }
            if (mentionInfo != null) {
                info.setMentionInfo(mentionInfo);
            }
            conversationInfoMap.put(message.getConversation(), info);
            return;
        }
        //如果会话存在
        //更新Mention
        if (mentionInfo != null && mentionInfo.getMentionMsgList() != null) {
            if (info.getMentionInfo() != null && info.getMentionInfo().getMentionMsgList() != null) {
                for (ConversationMentionInfo.MentionMsg existingMsg : info.getMentionInfo().getMentionMsgList()) {
                    if (!mentionInfo.getMentionMsgList().contains(existingMsg)) {
                        mentionInfo.getMentionMsgList().add(existingMsg);
                    }
                }
            }
            info.setMentionInfo(mentionInfo);
        }
        //更新未读数
        if (message.getMsgIndex() > 0) {
            info.setLastMessageIndex(message.getMsgIndex());
            int unreadCount = (int) (info.getLastMessageIndex() - info.getLastReadMessageIndex());
            info.setUnreadCount(unreadCount);
        }
        //更新排序
        if (!isBroadcast || message.getDirection() != Message.MessageDirection.SEND) {
            info.setSortTime(message.getTimestamp());
        }
        //更新最新消息
        info.setLastMessage(message);
    }

    //提取ConversationMentionInfo
    private ConversationMentionInfo getConversationMentionInfo(ConcreteMessage message) {
        boolean hasMention = false;
        //接收到的消息才处理mention
        if (Message.MessageDirection.RECEIVE == message.getDirection() && message.hasMentionInfo()) {
            if (MessageMentionInfo.MentionType.ALL == message.getMentionInfo().getType() || MessageMentionInfo.MentionType.ALL_AND_SOMEONE == message.getMentionInfo().getType()) {
                hasMention = true;
            } else if (MessageMentionInfo.MentionType.SOMEONE == message.getMentionInfo().getType() && message.getMentionInfo().getTargetUsers() != null) {
                for (UserInfo userInfo : message.getMentionInfo().getTargetUsers()) {
                    if (userInfo.getUserId() != null && userInfo.getUserId().equals(mCore.getUserId())) {
                        hasMention = true;
                        break;
                    }
                }
            }
        }
        if (!hasMention) return null;
        ConversationMentionInfo.MentionMsg mentionMsg = new ConversationMentionInfo.MentionMsg();
        mentionMsg.setSenderId(message.getSenderUserId());
        mentionMsg.setMsgId(message.getMessageId());
        mentionMsg.setMsgTime(message.getTimestamp());
        ConversationMentionInfo mentionInfo = new ConversationMentionInfo();
        mentionInfo.setMentionMsgList(new ArrayList<>());
        mentionInfo.getMentionMsgList().add(mentionMsg);
        return mentionInfo;
    }

    private void updateConversationAfterRemove(Conversation
                                                       conversation, List<ConcreteMessage> removedMessages, ConcreteMessage lastMessage) {
        //查询会话
        ConcreteConversationInfo info = getConversationAfterCommonResolved(conversation, lastMessage);
        //判空
        if (info == null) return;
        //处理Mention
        ConversationUpdater mentionUpdater = () -> {
            //判断是否需要更新会话
            boolean hasUpdate = false;
            //更新Mention
            if (removedMessages != null && info.getMentionInfo() != null && info.getMentionInfo().getMentionMsgList() != null && !info.getMentionInfo().getMentionMsgList().isEmpty()) {
                //遍历被移除消息列表进行过滤
                for (ConcreteMessage removedMessage : removedMessages) {
                    if (TextUtils.isEmpty(removedMessage.getMessageId())) continue;
                    //通过消息ID过滤mentionMsg
                    ConversationMentionInfo.MentionMsg temp = new ConversationMentionInfo.MentionMsg();
                    temp.setMsgId(removedMessage.getMessageId());
                    boolean removeSuccess = info.getMentionInfo().getMentionMsgList().remove(temp);
                    if (removeSuccess && !hasUpdate) hasUpdate = true;
                }
                //保存更新后的Mention信息
                if (hasUpdate) {
                    mCore.getDbManager().setMentionInfo(conversation, info.getMentionInfo().getMentionMsgList().isEmpty() ? "" : info.getMentionInfo().encodeToJson());
                    if (info.getMentionInfo().getMentionMsgList().isEmpty()) {
                        info.setMentionInfo(null);
                    }
                }
            }
            return hasUpdate;
        };
        //调用公共方法更新会话
        updateConversationLastMessage(info, lastMessage, mentionUpdater);
    }

    private void updateConversationAfterClear(Conversation conversation, long startTime, String
            sendUserId, ConcreteMessage lastMessage) {
        //查询会话
        ConcreteConversationInfo info = getConversationAfterCommonResolved(conversation, lastMessage);
        //判空
        if (info == null) return;
        //处理Mention
        ConversationUpdater mentionUpdater = () -> {
            //判断是否需要更新会话
            boolean hasUpdate = false;
            //更新Mention
            if (info.getMentionInfo() != null && info.getMentionInfo().getMentionMsgList() != null && !info.getMentionInfo().getMentionMsgList().isEmpty()) {
                //遍历Mention列表进行过滤
                for (int i = info.getMentionInfo().getMentionMsgList().size() - 1; i >= 0; i--) {
                    ConversationMentionInfo.MentionMsg mentionMsg = info.getMentionInfo().getMentionMsgList().get(i);
                    //通过消息发送者ID过滤mentionMsg
                    if (!TextUtils.isEmpty(sendUserId)) {
                        if (sendUserId.equals(mentionMsg.getSenderId()) && startTime > 0 && mentionMsg.getMsgTime() < startTime) {
                            if (!hasUpdate) hasUpdate = true;
                            info.getMentionInfo().getMentionMsgList().remove(i);
                        }
                        continue;
                    }
                    //通过消息时间过滤mentionMsg
                    if (startTime > 0 && mentionMsg.getMsgTime() < startTime) {
                        if (!hasUpdate) hasUpdate = true;
                        info.getMentionInfo().getMentionMsgList().remove(i);
                    }
                }
                //保存更新后的Mention信息
                if (hasUpdate) {
                    mCore.getDbManager().setMentionInfo(conversation, info.getMentionInfo().getMentionMsgList().isEmpty() ? "" : info.getMentionInfo().encodeToJson());
                    if (info.getMentionInfo().getMentionMsgList().isEmpty()) {
                        info.setMentionInfo(null);
                    }
                }
            }
            //返回结果
            return hasUpdate;
        };
        //调用公共方法更新会话
        updateConversationLastMessage(info, lastMessage, mentionUpdater);
    }

    //查询会话，在执行完部分通用判断后返回该会话
    private ConcreteConversationInfo getConversationAfterCommonResolved(Conversation
                                                                                conversation, ConcreteMessage lastMessage) {
        //判空
        if (conversation == null) return null;
        //查询会话
        ConcreteConversationInfo info = (ConcreteConversationInfo) getConversationInfo(conversation);
        //会话不存在时不处理
        if (info == null) return null;
        //如果最后一条消息不为空，返回会话
        if (lastMessage != null) {
            return info;
        }
        //如果最后一条消息为空，直接更新会话并执行回调
        clearConversationLastMessage(info);
        return null;
    }

    //清空会话最新消息
    private void clearConversationLastMessage(ConcreteConversationInfo info) {
        //更新数据库
        mCore.getDbManager().clearLastMessage(info.getConversation());
        //更新Mention
        info.setMentionInfo(null);
        //更新最新消息
        info.setLastMessage(null);
        //执行回调
        if (mListenerMap != null) {
            List<ConversationInfo> result = new ArrayList<>();
            result.add(info);
            for (Map.Entry<String, IConversationListener> entry : mListenerMap.entrySet()) {
                mCore.getCallbackHandler().post(() -> entry.getValue().onConversationInfoUpdate(result));
            }
        }
    }

    //更新会话的通用方法
    private void updateConversationLastMessage(ConcreteConversationInfo info, ConcreteMessage
            lastMessage, ConversationUpdater mentionUpdater) {
        //判断会话最新消息是否有变化
        boolean isLastMessageUpdate = info.getLastMessage() == null || info.getLastMessage().getClientMsgNo() != lastMessage.getClientMsgNo() || !Objects.equals(info.getLastMessage().getContentType(), lastMessage.getContentType());
        //会话最新消息有变化，更新会话最新消息
        if (isLastMessageUpdate) {
            mCore.getDbManager().updateLastMessageWithoutIndex(lastMessage);
            info.setLastMessage(lastMessage);
        }
        //判断是否需要更新会话
        boolean hasUpdate = isLastMessageUpdate;
        //更新Mention信息
        if (mentionUpdater != null) {
            hasUpdate = mentionUpdater.update() || hasUpdate;
        }
        //如果不需要更新会话，直接return
        if (!hasUpdate) return;
        //执行回调
        if (mListenerMap != null) {
            List<ConversationInfo> result = new ArrayList<>();
            result.add(info);
            for (Map.Entry<String, IConversationListener> entry : mListenerMap.entrySet()) {
                mCore.getCallbackHandler().post(() -> entry.getValue().onConversationInfoUpdate(result));
            }
        }
    }

    private void updateSyncTime(long timestamp) {
        if (mSyncProcessing) {
            if (timestamp > mCachedSyncTime) {
                mCachedSyncTime = timestamp;
            }
        } else {
            mCore.setConversationSyncTime(timestamp);
        }
    }

    private void updateUserInfo(List<ConcreteConversationInfo> conversationInfoList) {
        Map<String, GroupInfo> groupInfoMap = new HashMap<>();
        Map<String, UserInfo> userInfoMap = new HashMap<>();
        for (ConcreteConversationInfo info : conversationInfoList) {
            if (info.getGroupInfo() != null && !TextUtils.isEmpty(info.getGroupInfo().getGroupId())) {
                groupInfoMap.put(info.getGroupInfo().getGroupId(), info.getGroupInfo());
            }
            if (info.getTargetUserInfo() != null && !TextUtils.isEmpty(info.getTargetUserInfo().getUserId())) {
                userInfoMap.put(info.getTargetUserInfo().getUserId(), info.getTargetUserInfo());
            }
            if (info.getMentionUserList() != null) {
                for (UserInfo mentionUserInfo : info.getMentionUserList()) {
                    if (!TextUtils.isEmpty(mentionUserInfo.getUserId())) {
                        userInfoMap.put(mentionUserInfo.getUserId(), mentionUserInfo);
                    }
                }
            }
        }
        mUserInfoManager.insertUserInfoList(new ArrayList<>(userInfoMap.values()));
        mUserInfoManager.insertGroupInfoList(new ArrayList<>(groupInfoMap.values()));
    }

    private void noticeTotalUnreadCountChange() {
        int count = mCore.getDbManager().getTotalUnreadCount();
        if (count < 0) {
            return;
        }
        if (mListenerMap != null) {
            for (Map.Entry<String, IConversationListener> entry : mListenerMap.entrySet()) {
                mCore.getCallbackHandler().post(() -> entry.getValue().onTotalUnreadMessageCountUpdate(count));
            }
        }
    }

    private ConcreteConversationInfo doConversationsAdd(ConcreteConversationInfo
                                                                conversationInfo) {
        if (conversationInfo == null || conversationInfo.getConversation() == null) return null;

        List<ConcreteConversationInfo> convList = new ArrayList<>();
        convList.add(conversationInfo);

        updateSyncTime(conversationInfo.getSyncTime());
        updateUserInfo(convList);
        ConcreteConversationInfo old = mCore.getDbManager().getConversationInfo(conversationInfo.getConversation());
        if (old == null) {
            mCore.getDbManager().insertConversations(convList, (insertList, updateList) -> {
                if (insertList.size() > 0) {
                    if (mListenerMap != null) {
                        List<ConversationInfo> l = new ArrayList<>(insertList);
                        for (Map.Entry<String, IConversationListener> entry : mListenerMap.entrySet()) {
                            mCore.getCallbackHandler().post(() -> entry.getValue().onConversationInfoAdd(l));
                        }
                    }
                }
            });
            return conversationInfo;
        }
        if (conversationInfo.getSortTime() > old.getSortTime()) {
            mCore.getDbManager().updateSortTime(conversationInfo.getConversation(), conversationInfo.getSortTime());
            old.setSortTime(conversationInfo.getSortTime());
            old.setSyncTime(conversationInfo.getSyncTime());
            if (mListenerMap != null) {
                List<ConversationInfo> l = new ArrayList<>();
                l.add(old);
                for (Map.Entry<String, IConversationListener> entry : mListenerMap.entrySet()) {
                    mCore.getCallbackHandler().post(() -> entry.getValue().onConversationInfoUpdate(l));
                }
            }
        }
        return old;
    }

    private final JIMCore mCore;
    private final UserInfoManager mUserInfoManager;
    private final MessageManager mMessageManager;
    private ConcurrentHashMap<String, IConversationListener> mListenerMap;
    private ConcurrentHashMap<String, IConversationSyncListener> mSyncListenerMap;
    private boolean mSyncProcessing = true;
    private long mCachedSyncTime;
    private static final int CONVERSATION_SYNC_COUNT = 100;

    private interface ConversationUpdater {
        boolean update();
    }
}