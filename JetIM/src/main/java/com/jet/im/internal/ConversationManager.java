package com.jet.im.internal;

import android.text.TextUtils;

import com.jet.im.JetIMConst;
import com.jet.im.internal.core.JetIMCore;
import com.jet.im.interfaces.IConversationManager;
import com.jet.im.internal.core.db.DBManager;
import com.jet.im.internal.core.network.SyncConversationsCallback;
import com.jet.im.internal.core.network.WebSocketSimpleCallback;
import com.jet.im.internal.model.ConcreteConversationInfo;
import com.jet.im.internal.model.ConcreteMessage;
import com.jet.im.model.Conversation;
import com.jet.im.model.ConversationInfo;
import com.jet.im.model.GroupInfo;
import com.jet.im.model.UserInfo;
import com.jet.im.utils.LoggerUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        List<ConversationInfo> l = new ArrayList<>();
        l.add(info);
        for (Map.Entry<String, IConversationListener> entry : mListenerMap.entrySet()) {
            entry.getValue().onConversationInfoUpdate(l);
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
                    mCore.getDbManager().insertConversations(conversationInfoList, new DBManager.IDbInsertConversationsCallback() {
                        @Override
                        public void onComplete(List<ConcreteConversationInfo> insertList, List<ConcreteConversationInfo> updateList) {
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
        mCore.getDbManager().updateLastMessage(message);
        noticeConversationAddOrUpdate(message);
    }

    @Override
    public void onMessageSend(ConcreteMessage message) {
        mCore.getDbManager().updateLastMessage(message);
        updateSyncTime(message.getTimestamp());
        noticeConversationAddOrUpdate(message);
    }

    @Override
    public void onMessageReceive(ConcreteMessage message) {
        mCore.getDbManager().updateLastMessage(message);
        updateSyncTime(message.getTimestamp());
        noticeConversationAddOrUpdate(message);
        noticeTotalUnreadCountChange();
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

    interface ICompleteCallback {
        void onComplete();
    }

    private void noticeConversationAddOrUpdate(ConcreteMessage message) {
        ConversationInfo info = getConversationInfo(message.getConversation());
        if (info == null) {
            ConcreteConversationInfo addInfo = new ConcreteConversationInfo();
            addInfo.setConversation(message.getConversation());
            addInfo.setUpdateTime(message.getTimestamp());
            addInfo.setLastMessage(message);
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
            if (mListenerMap != null) {
                List<ConversationInfo> result = new ArrayList<>();
                result.add(info);
                for (Map.Entry<String, IConversationListener> entry : mListenerMap.entrySet()) {
                    entry.getValue().onConversationInfoUpdate(result);
                }
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
