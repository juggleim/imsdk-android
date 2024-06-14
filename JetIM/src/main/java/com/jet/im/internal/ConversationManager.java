package com.jet.im.internal;

import android.text.TextUtils;

import com.jet.im.JErrorCode;
import com.jet.im.JetIMConst;
import com.jet.im.interfaces.IConversationManager;
import com.jet.im.internal.core.JetIMCore;
import com.jet.im.internal.core.network.SyncConversationsCallback;
import com.jet.im.internal.core.network.WebSocketSimpleCallback;
import com.jet.im.internal.core.network.WebSocketTimestampCallback;
import com.jet.im.internal.model.ConcreteConversationInfo;
import com.jet.im.internal.model.ConcreteMessage;
import com.jet.im.internal.model.messages.ClearUnreadMessage;
import com.jet.im.internal.model.messages.TopConvMessage;
import com.jet.im.internal.model.messages.UnDisturbConvMessage;
import com.jet.im.internal.util.JLogger;
import com.jet.im.model.Conversation;
import com.jet.im.model.ConversationInfo;
import com.jet.im.model.ConversationMentionInfo;
import com.jet.im.model.GroupInfo;
import com.jet.im.model.Message;
import com.jet.im.model.MessageContent;
import com.jet.im.model.MessageMentionInfo;
import com.jet.im.model.UserInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class ConversationManager implements IConversationManager, MessageManager.ISendReceiveListener {

    public ConversationManager(JetIMCore core, UserInfoManager userInfoManager) {
        this.mCore = core;
        this.mUserInfoManager = userInfoManager;
        this.mCachedSyncTime = -1;
    }

    @Override
    public List<ConversationInfo> getConversationInfoList() {
        return mCore.getDbManager().getConversationInfoList();
    }

    @Override
    public List<ConversationInfo> getConversationInfoList(int[] conversationTypes, int count, long timestamp, JetIMConst.PullDirection direction) {
        return mCore.getDbManager().getConversationInfoList(conversationTypes, count, timestamp, direction);
    }

    @Override
    public List<ConversationInfo> getConversationInfoList(int count, long timestamp, JetIMConst.PullDirection direction) {
        return getConversationInfoList(null, count, timestamp, direction);
    }

    @Override
    public ConversationInfo getConversationInfo(Conversation conversation) {
        return mCore.getDbManager().getConversationInfo(conversation);
    }

    @Override
    public void deleteConversationInfo(Conversation conversation) {
        mCore.getDbManager().deleteConversationInfo(conversation);
        //手动删除不给回调
        if (mCore.getWebSocket() == null) {
            return;
        }
        mCore.getWebSocket().deleteConversationInfo(conversation, mCore.getUserId(), new WebSocketSimpleCallback() {
            @Override
            public void onSuccess() {
                JLogger.i("CONV-Delete", "success");
            }

            @Override
            public void onError(int errorCode) {
                JLogger.e("CONV-Delete", "fail, code is " + errorCode);
            }
        });
    }

    @Override
    public void setDraft(Conversation conversation, String draft) {
        mCore.getDbManager().setDraft(conversation, draft);
        ConversationInfo info = mCore.getDbManager().getConversationInfo(conversation);
        if (mListenerMap != null) {
            List<ConversationInfo> l = new ArrayList<>();
            l.add(info);
            for (Map.Entry<String, IConversationListener> entry : mListenerMap.entrySet()) {
                entry.getValue().onConversationInfoUpdate(l);
            }
        }
    }

    @Override
    public void clearDraft(Conversation conversation) {
        setDraft(conversation, "");
    }

    @Override
    public void setMute(Conversation conversation, boolean isMute, ISimpleCallback callback) {
        mCore.getWebSocket().setMute(conversation, isMute, mCore.getUserId(), new WebSocketSimpleCallback() {
            @Override
            public void onSuccess() {
                JLogger.i("CONV-Mute", "success");
                mCore.getDbManager().setMute(conversation, isMute);
                if (callback != null) {
                    callback.onSuccess();
                }
                ConversationInfo conversationInfo = mCore.getDbManager().getConversationInfo(conversation);
                if (conversationInfo != null && mListenerMap != null) {
                    List<ConversationInfo> list = new ArrayList<>();
                    list.add(conversationInfo);
                    for (Map.Entry<String, IConversationListener> entry : mListenerMap.entrySet()) {
                        entry.getValue().onConversationInfoUpdate(list);
                    }
                }
            }

            @Override
            public void onError(int errorCode) {
                JLogger.e("CONV-Mute", "fail, code is " + errorCode);
                if (callback != null) {
                    callback.onError(errorCode);
                }
            }
        });
    }

    @Override
    public void setTop(Conversation conversation, boolean isTop, ISimpleCallback callback) {
        mCore.getWebSocket().setTop(conversation, isTop, mCore.getUserId(), new WebSocketTimestampCallback() {
            @Override
            public void onSuccess(long timestamp) {
                JLogger.i("CONV-Top", "success");
                mCore.getDbManager().setTop(conversation, isTop, timestamp);
                if (callback != null) {
                    callback.onSuccess();
                }
                ConversationInfo conversationInfo = mCore.getDbManager().getConversationInfo(conversation);
                if (conversationInfo != null && mListenerMap != null) {
                    List<ConversationInfo> list = new ArrayList<>();
                    list.add(conversationInfo);
                    for (Map.Entry<String, IConversationListener> entry : mListenerMap.entrySet()) {
                        entry.getValue().onConversationInfoUpdate(list);
                    }
                }
            }

            @Override
            public void onError(int errorCode) {
                JLogger.e("CONV-Top", "fail, code is " + errorCode);
                if (callback != null) {
                    callback.onError(errorCode);
                }
            }
        });
    }

    @Override
    public List<ConversationInfo> getTopConversationInfoList(int count, long timestamp, JetIMConst.PullDirection direction) {
        return mCore.getDbManager().getTopConversationInfoList(null, count, timestamp, direction);
    }

    @Override
    public int getTotalUnreadCount() {
        return mCore.getDbManager().getTotalUnreadCount();
    }

    @Override
    public void clearUnreadCount(Conversation conversation, ISimpleCallback callback) {
        ConcreteConversationInfo info = mCore.getDbManager().getConversationInfo(conversation);
        if (info == null) {
            if (callback != null) {
                callback.onError(JErrorCode.INVALID_PARAM);
            }
            return;
        }
        mCore.getWebSocket().clearUnreadCount(conversation, mCore.getUserId(), info.getLastMessageIndex(), new WebSocketSimpleCallback() {
            @Override
            public void onSuccess() {
                JLogger.i("CONV-ClearUnread", "success");
                mCore.getDbManager().clearUnreadCount(conversation, info.getLastMessageIndex());
                mCore.getDbManager().setMentionInfo(conversation, "");
                noticeTotalUnreadCountChange();
                if (callback != null) {
                    callback.onSuccess();
                }
            }

            @Override
            public void onError(int errorCode) {
                JLogger.e("CONV-ClearUnread", "fail, code is " + errorCode);
                if (callback != null) {
                    callback.onError(errorCode);
                }
            }
        });
    }

    @Override
    public void clearTotalUnreadCount(ISimpleCallback callback) {
        long time = Math.max(mCore.getMessageSendSyncTime(), mCore.getMessageReceiveTime());
        mCore.getWebSocket().clearTotalUnreadCount(mCore.getUserId(), time, new WebSocketSimpleCallback() {
            @Override
            public void onSuccess() {
                JLogger.i("CONV-ClearTotal", "success");
                mCore.getDbManager().clearTotalUnreadCount();
                mCore.getDbManager().clearMentionInfo();
                noticeTotalUnreadCountChange();
                if (callback != null) {
                    callback.onSuccess();
                }
            }

            @Override
            public void onError(int errorCode) {
                JLogger.e("CONV-ClearTotal", "fail, code is " + errorCode);
                if (callback != null) {
                    callback.onError(errorCode);
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

    void syncConversations(ICompleteCallback callback) {
        if (mCore.getWebSocket() == null) {
            return;
        }
        mSyncProcessing = true;
        JLogger.i("CONV-Sync", "sync time is " + mCore.getConversationSyncTime());
        mCore.getWebSocket().syncConversations(mCore.getConversationSyncTime(), CONVERSATION_SYNC_COUNT, mCore.getUserId(), new SyncConversationsCallback() {
            @Override
            public void onSuccess(List<ConcreteConversationInfo> conversationInfoList, List<ConcreteConversationInfo> deleteConversationInfoList, boolean isFinished) {
                JLogger.i("CONV-Sync", "success, conversation count is " + (conversationInfoList == null ? 0 : conversationInfoList.size()) + ", delete count is " + (deleteConversationInfoList == null ? 0 : deleteConversationInfoList.size()));
                long syncTime = 0;
                if (conversationInfoList.size() > 0) {
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
                                    entry.getValue().onConversationInfoAdd(l);
                                }
                            }
                        }
                        if (updateList.size() > 0) {
                            if (mListenerMap != null) {
                                List<ConversationInfo> l = new ArrayList<>(updateList);
                                for (Map.Entry<String, IConversationListener> entry : mListenerMap.entrySet()) {
                                    entry.getValue().onConversationInfoUpdate(l);
                                }
                            }
                        }
                    });
                    noticeTotalUnreadCountChange();
                }
                if (deleteConversationInfoList.size() > 0) {
                    updateUserInfo(deleteConversationInfoList);
                    ConcreteConversationInfo last = deleteConversationInfoList.get(deleteConversationInfoList.size() - 1);
                    if (last.getSyncTime() > syncTime) {
                        syncTime = last.getSyncTime();
                    }
                    for (ConcreteConversationInfo info : deleteConversationInfoList) {
                        mCore.getDbManager().deleteConversationInfo(info.getConversation());
                    }
                    List<ConversationInfo> l = new ArrayList<>(deleteConversationInfoList);
                    if (mListenerMap != null) {
                        for (Map.Entry<String, IConversationListener> entry : mListenerMap.entrySet()) {
                            entry.getValue().onConversationInfoDelete(l);
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
                            entry.getValue().onConversationSyncComplete();
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
    public void onMessageReceive(ConcreteMessage message) {
        addOrUpdateConversationIfNeed(message);
        updateSyncTime(message.getTimestamp());
        noticeTotalUnreadCountChange();
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
    public void onConversationsDelete(List<Conversation> conversations) {
        List<ConversationInfo> results = new ArrayList<>();
        for (Conversation conversation : conversations) {
            ConversationInfo info = new ConversationInfo();
            info.setConversation(conversation);
            results.add(info);
        }
        if (mListenerMap != null) {
            for (Map.Entry<String, IConversationListener> entry : mListenerMap.entrySet()) {
                entry.getValue().onConversationInfoDelete(results);
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
                entry.getValue().onConversationInfoUpdate(infoList);
            }
        }
        //通知更新总未读数
        if (totalUnreadCountHasChanged) noticeTotalUnreadCountChange();
    }

    interface ICompleteCallback {
        void onComplete();
    }


    private void addOrUpdateConversationIfNeed(ConcreteMessage message) {
        boolean hasMention = false;
        //接收到的消息才处理 mention
        if (Message.MessageDirection.RECEIVE == message.getDirection()
                && message.hasMentionInfo()) {
            if (MessageMentionInfo.MentionType.ALL == message.getMessageOptions().getMentionInfo().getType()
                    || MessageMentionInfo.MentionType.ALL_AND_SOMEONE == message.getMessageOptions().getMentionInfo().getType()) {
                hasMention = true;
            } else if (MessageMentionInfo.MentionType.SOMEONE == message.getMessageOptions().getMentionInfo().getType()
                    && message.getMessageOptions().getMentionInfo().getTargetUsers() != null) {
                for (UserInfo userInfo : message.getMessageOptions().getMentionInfo().getTargetUsers()) {
                    if (userInfo.getUserId() != null && userInfo.getUserId().equals(mCore.getUserId())) {
                        hasMention = true;
                        break;
                    }
                }
            }
        }
        ConversationMentionInfo mentionInfo = null;
        if (hasMention) {
            List<ConversationMentionInfo.MentionMsg> mentionMessages = new ArrayList<>();
            ConversationMentionInfo.MentionMsg mentionMsg = new ConversationMentionInfo.MentionMsg();
            mentionMsg.setSenderId(message.getSenderUserId());
            mentionMsg.setMsgId(message.getMessageId());
            mentionMsg.setMsgTime(message.getTimestamp());
            mentionMessages.add(mentionMsg);
            mentionInfo = new ConversationMentionInfo();
            mentionInfo.setMentionMsgList(mentionMessages);
        }
        boolean isBroadcast = (message.getFlags() & MessageContent.MessageFlag.IS_BROADCAST.getValue()) != 0;

        ConcreteConversationInfo info = (ConcreteConversationInfo) getConversationInfo(message.getConversation());
        if (info == null) {
            ConcreteConversationInfo addInfo = new ConcreteConversationInfo();
            addInfo.setConversation(message.getConversation());
            if (isBroadcast && message.getDirection() == Message.MessageDirection.SEND) {
                addInfo.setSortTime(0);
            } else {
                addInfo.setSortTime(message.getTimestamp());
            }
            addInfo.setLastMessage(message);
            addInfo.setLastMessageIndex(message.getMsgIndex());
            addInfo.setLastReadMessageIndex(message.getMsgIndex() - 1);
            addInfo.setUnreadCount(1);
            if (mentionInfo != null) {
                addInfo.setMentionInfo(mentionInfo);
            }
            List<ConcreteConversationInfo> l = new ArrayList<>();
            l.add(addInfo);
            mCore.getDbManager().insertConversations(l, null);
            if (mListenerMap != null) {
                List<ConversationInfo> result = new ArrayList<>(l);
                for (Map.Entry<String, IConversationListener> entry : mListenerMap.entrySet()) {
                    entry.getValue().onConversationInfoAdd(result);
                }
            }
        } else {
            //更新Mention
            if (mentionInfo != null && mentionInfo.getMentionMsgList() != null) {
                if (info.getMentionInfo() != null && info.getMentionInfo().getMentionMsgList() != null) {
                    for (ConversationMentionInfo.MentionMsg mentionMsg : info.getMentionInfo().getMentionMsgList()) {
                        if (!mentionInfo.getMentionMsgList().contains(mentionMsg)) {
                            mentionInfo.getMentionMsgList().add(mentionMsg);
                        }
                    }
                }
                info.setMentionInfo(mentionInfo);
                mCore.getDbManager().setMentionInfo(message.getConversation(), mentionInfo.encodeToJson());
            }
            //更新未读数
            if (message.getMsgIndex() > 0) {
                info.setLastMessageIndex(message.getMsgIndex());
                int unreadCount = (int) (info.getLastMessageIndex() - info.getLastReadMessageIndex());
                info.setUnreadCount(unreadCount);
            }
            info.setSortTime(message.getTimestamp());
            //更新最新消息
            info.setLastMessage(message);
            mCore.getDbManager().updateLastMessage(message);
            if (mListenerMap != null) {
                List<ConversationInfo> result = new ArrayList<>();
                result.add(info);
                for (Map.Entry<String, IConversationListener> entry : mListenerMap.entrySet()) {
                    entry.getValue().onConversationInfoUpdate(result);
                }
            }
        }
    }

    private void updateConversationAfterRemove(Conversation conversation, List<ConcreteMessage> removedMessages, ConcreteMessage lastMessage) {
        //查询会话
        ConcreteConversationInfo info = getConversationAfterCommonResolved(conversation, lastMessage);
        //判空
        if (info == null) return;
        //处理Mention
        ConversationUpdater mentionUpdater = () -> {
            //判断是否需要更新会话
            boolean hasUpdate = false;
            //更新Mention
            if (removedMessages != null
                    && info.getMentionInfo() != null && info.getMentionInfo().getMentionMsgList() != null && !info.getMentionInfo().getMentionMsgList().isEmpty()) {
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

    private void updateConversationAfterClear(Conversation conversation, long startTime, String sendUserId, ConcreteMessage lastMessage) {
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
                    if (!TextUtils.isEmpty(sendUserId) && sendUserId.equals(mentionMsg.getSenderId())) {
                        if (!hasUpdate) hasUpdate = true;
                        info.getMentionInfo().getMentionMsgList().remove(i);
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
    private ConcreteConversationInfo getConversationAfterCommonResolved(Conversation conversation, ConcreteMessage lastMessage) {
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
                entry.getValue().onConversationInfoUpdate(result);
            }
        }
    }

    //更新会话的通用方法
    private void updateConversationLastMessage(ConcreteConversationInfo info, ConcreteMessage lastMessage, ConversationUpdater mentionUpdater) {
        //判断会话最新消息是否有变化
        boolean isLastMessageUpdate = info.getLastMessage() == null
                || info.getLastMessage().getClientMsgNo() != lastMessage.getClientMsgNo()
                || !Objects.equals(info.getLastMessage().getContentType(), lastMessage.getContentType());
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
                entry.getValue().onConversationInfoUpdate(result);
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
                entry.getValue().onTotalUnreadMessageCountUpdate(count);
            }
        }
    }

    private final JetIMCore mCore;
    private final UserInfoManager mUserInfoManager;
    private ConcurrentHashMap<String, IConversationListener> mListenerMap;
    private ConcurrentHashMap<String, IConversationSyncListener> mSyncListenerMap;
    private boolean mSyncProcessing;
    private long mCachedSyncTime;
    private static final int CONVERSATION_SYNC_COUNT = 100;

    private interface ConversationUpdater {
        boolean update();
    }
}