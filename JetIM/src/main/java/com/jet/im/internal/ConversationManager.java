package com.jet.im.internal;

import android.text.TextUtils;

import com.jet.im.JetIMConst;
import com.jet.im.interfaces.IConversationManager;
import com.jet.im.internal.core.JetIMCore;
import com.jet.im.internal.core.network.SyncConversationsCallback;
import com.jet.im.internal.core.network.WebSocketSimpleCallback;
import com.jet.im.internal.core.network.WebSocketTimestampCallback;
import com.jet.im.internal.model.ConcreteConversationInfo;
import com.jet.im.internal.model.ConcreteMessage;
import com.jet.im.model.Conversation;
import com.jet.im.model.ConversationInfo;
import com.jet.im.model.ConversationMentionInfo;
import com.jet.im.model.GroupInfo;
import com.jet.im.model.Message;
import com.jet.im.model.MessageContent;
import com.jet.im.model.MessageMentionInfo;
import com.jet.im.model.UserInfo;
import com.jet.im.utils.LoggerUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class ConversationManager implements IConversationManager, MessageManager.ISendReceiveListener {

    public ConversationManager(JetIMCore core) {
        this.mCore = core;
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
                LoggerUtils.i("delete conversation success");
            }

            @Override
            public void onError(int errorCode) {
                LoggerUtils.i("delete conversation fail, code is " + errorCode);
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
                mCore.getDbManager().setMute(conversation, isMute);
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
    public void setTop(Conversation conversation, boolean isTop) {
        mCore.getDbManager().setTop(conversation, isTop);
        mCore.getWebSocket().setTop(conversation, isTop, mCore.getUserId(), new WebSocketTimestampCallback() {
            @Override
            public void onSuccess(long timestamp) {
                mCore.getDbManager().setTopTime(conversation, timestamp);
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
    public void clearUnreadCount(Conversation conversation) {
        ConcreteConversationInfo info = mCore.getDbManager().getConversationInfo(conversation);
        if (info == null) {
            return;
        }
        mCore.getDbManager().clearUnreadCount(conversation, info.getLastMessageIndex());
        mCore.getDbManager().setMentionInfo(conversation, "");
        noticeTotalUnreadCountChange();
        mCore.getWebSocket().clearUnreadCount(conversation, mCore.getUserId(), info.getLastMessageIndex(), new WebSocketSimpleCallback() {
            @Override
            public void onSuccess() {
                LoggerUtils.i("clear unread success");
            }

            @Override
            public void onError(int errorCode) {
                LoggerUtils.i("clear unread error, code is " + errorCode);
            }
        });
    }

    @Override
    public void clearTotalUnreadCount() {
        mCore.getDbManager().clearTotalUnreadCount();
        mCore.getDbManager().clearMentionInfo();
        noticeTotalUnreadCountChange();
        long time = mCore.getDbManager().getNewestStatusSentMessageTimestamp();
        mCore.getWebSocket().clearTotalUnreadCount(mCore.getUserId(), time, new WebSocketSimpleCallback() {
            @Override
            public void onSuccess() {
                LoggerUtils.i("clear total unread success");
            }

            @Override
            public void onError(int errorCode) {
                LoggerUtils.i("clear total unread error, code is " + errorCode);
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
        mCore.getWebSocket().syncConversations(mCore.getConversationSyncTime(), CONVERSATION_SYNC_COUNT, mCore.getUserId(), new SyncConversationsCallback() {
            @Override
            public void onSuccess(List<ConcreteConversationInfo> conversationInfoList, List<ConcreteConversationInfo> deleteConversationInfoList, boolean isFinished) {
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
                LoggerUtils.e("sync conversation fail, code is " + errorCode);
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
    public void onMessageRemoved(Conversation conversation, List<ConcreteMessage> removedMessages, ConcreteMessage lastedMessage) {
        updateConversationLastedMessage(conversation, removedMessages, lastedMessage);
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
    public void onConversationsUnreadUpdate(List<ConcreteConversationInfo> conversations) {
        if (conversations == null) return;
        //更新会话未读数
        List<ConversationInfo> infoList = new ArrayList<>();
        for (int i = 0; i < conversations.size(); i++) {
            ConcreteConversationInfo conversation = conversations.get(i);
            mCore.getDbManager().clearUnreadCount(conversation.getConversation(), conversation.getLastReadMessageIndex());
            mCore.getDbManager().setMentionInfo(conversation.getConversation(), "");

            ConversationInfo info = mCore.getDbManager().getConversationInfo((conversation.getConversation()));
            if (info != null) {
                infoList.add(info);
            }
        }
        //通知更新会话
        if (mListenerMap != null) {
            for (Map.Entry<String, IConversationListener> entry : mListenerMap.entrySet()) {
                entry.getValue().onConversationInfoUpdate(infoList);
            }
        }
        //通知更新总未读数
        noticeTotalUnreadCountChange();
    }

    interface ICompleteCallback {
        void onComplete();
    }

    private void addOrUpdateConversationIfNeed(ConcreteMessage message) {
        ConversationMentionInfo mentionInfo = null;
        //接收到的消息才处理 mention
        if (Message.MessageDirection.RECEIVE == message.getDirection()
                && message.getContent() != null
                && message.getContent().getMentionInfo() != null) {
            if (MessageMentionInfo.MentionType.ALL == message.getContent().getMentionInfo().getType()
                    || MessageMentionInfo.MentionType.ALL_AND_SOMEONE == message.getContent().getMentionInfo().getType()) {
                List<ConversationMentionInfo.MentionMsg> mentionMsgs = new ArrayList<>();
                ConversationMentionInfo.MentionMsg mentionMsg = new ConversationMentionInfo.MentionMsg();
                mentionMsg.setSenderId(message.getSenderUserId());
                mentionMsg.setMsgId(message.getMessageId());
                mentionMsg.setMsgTime(message.getTimestamp());
                mentionMsgs.add(mentionMsg);
                mentionInfo = new ConversationMentionInfo();
                mentionInfo.setMentionMsgList(mentionMsgs);
            } else if (MessageMentionInfo.MentionType.SOMEONE == message.getContent().getMentionInfo().getType()
                    && message.getContent().getMentionInfo().getTargetUsers() != null) {
                for (UserInfo userInfo : message.getContent().getMentionInfo().getTargetUsers()) {
                    if (userInfo.getUserId() != null
                            && userInfo.getUserId().equals(mCore.getUserId())) {
                        List<ConversationMentionInfo.MentionMsg> mentionMsgs = new ArrayList<>();
                        ConversationMentionInfo.MentionMsg mentionMsg = new ConversationMentionInfo.MentionMsg();
                        mentionMsg.setSenderId(message.getSenderUserId());
                        mentionMsg.setMsgId(message.getMessageId());
                        mentionMsg.setMsgTime(message.getTimestamp());
                        mentionMsgs.add(mentionMsg);
                        mentionInfo = new ConversationMentionInfo();
                        mentionInfo.setMentionMsgList(mentionMsgs);
                        break;
                    }
                }
            }
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
            info.setLastMessageIndex(message.getMsgIndex());
            int unreadCount = (int) (info.getLastMessageIndex() - info.getLastReadMessageIndex());
            info.setUnreadCount(unreadCount);
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

    private void updateConversationLastedMessage(Conversation conversation, List<ConcreteMessage> removedMessages, ConcreteMessage lastedMessage) {
        if (conversation == null) return;
        //查询会话
        ConcreteConversationInfo info = (ConcreteConversationInfo) getConversationInfo(conversation);
        //会话不存在时不处理
        if (info == null) return;
        //会话原来的最新消息为空，且当前最新消息也为空，不处理
        if (info.getLastMessage() == null && lastedMessage == null) return;
        //会话原来的最新消息与且当前最新消息相同，不处理
        if (info.getLastMessage() != null && lastedMessage != null
                && info.getLastMessage().getClientMsgNo() == lastedMessage.getClientMsgNo()
                && Objects.equals(info.getLastMessage().getContentType(), lastedMessage.getContentType())
        ) {
            return;
        }
        //最新消息为空时将会话Mention置为false
        if (lastedMessage == null) {
            info.setLastMessageIndex(0);
            info.setLastReadMessageIndex(0);
            info.setSortTime(0);
            info.setUnreadCount(0);
            info.setLastMessage(null);
            info.setMentionInfo(null);
            mCore.getDbManager().setMentionInfo(conversation, "");
            mCore.getDbManager().clearLastMessage(conversation);
        } else {
            //更新未读数
            info.setLastMessageIndex(lastedMessage.getMsgIndex());
            int unreadCount = (int) (info.getLastMessageIndex() - info.getLastReadMessageIndex());
            info.setUnreadCount(unreadCount);
            info.setSortTime(lastedMessage.getTimestamp());
            //更新Mention
            if (removedMessages != null
                    && info.getMentionInfo() != null && info.getMentionInfo().getMentionMsgList() != null && !info.getMentionInfo().getMentionMsgList().isEmpty()) {
                for (ConcreteMessage removedMessage : removedMessages) {
                    if (TextUtils.isEmpty(removedMessage.getMessageId())) continue;
                    ConversationMentionInfo.MentionMsg temp = new ConversationMentionInfo.MentionMsg();
                    temp.setMsgId(removedMessage.getMessageId());
                    info.getMentionInfo().getMentionMsgList().remove(temp);
                }
                if (info.getMentionInfo().getMentionMsgList().isEmpty()) {
                    info.setMentionInfo(null);
                    mCore.getDbManager().setMentionInfo(conversation, "");
                } else {
                    mCore.getDbManager().setMentionInfo(conversation, info.getMentionInfo().encodeToJson());
                }
            }
            info.setLastMessage(lastedMessage);
            mCore.getDbManager().updateLastMessage(lastedMessage);
        }
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
        mCore.getDbManager().insertUserInfoList(new ArrayList<>(userInfoMap.values()));
        mCore.getDbManager().insertGroupInfoList(new ArrayList<>(groupInfoMap.values()));
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
    private ConcurrentHashMap<String, IConversationListener> mListenerMap;
    private ConcurrentHashMap<String, IConversationSyncListener> mSyncListenerMap;
    private boolean mSyncProcessing;
    private long mCachedSyncTime;
    private static final int CONVERSATION_SYNC_COUNT = 100;
}