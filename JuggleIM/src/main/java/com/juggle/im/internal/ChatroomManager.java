package com.juggle.im.internal;

import android.text.TextUtils;

import com.juggle.im.JErrorCode;
import com.juggle.im.interfaces.IChatroomManager;
import com.juggle.im.internal.core.JIMCore;
import com.juggle.im.internal.core.network.JWebSocket;
import com.juggle.im.internal.core.network.UpdateChatroomAttrCallback;
import com.juggle.im.internal.core.network.WebSocketTimestampCallback;
import com.juggle.im.internal.model.CachedChatroom;
import com.juggle.im.internal.model.ChatroomAttributeItem;
import com.juggle.im.internal.util.JLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChatroomManager implements IChatroomManager, JWebSocket.IWebSocketChatroomListener {
    public ChatroomManager(JIMCore core) {
        this.mCore = core;
        this.mCore.getWebSocket().setChatroomListener(this);
        this.mCachedChatroomMap = new ConcurrentHashMap<>();
    }

    @Override
    public void joinChatroom(String chatroomId) {
        if (chatroomId == null || chatroomId.isEmpty()) {
            JLogger.e("CHRM-Join", "error chatroomId is empty");
            return;
        }
        mCore.getWebSocket().joinChatroom(chatroomId, new WebSocketTimestampCallback() {
            @Override
            public void onSuccess(long timestamp) {
                JLogger.i("CHRM-Join", "success");
                changeStatus(chatroomId, CachedChatroom.ChatroomStatus.JOINED);
                mCore.getWebSocket().syncChatroomAttributes(chatroomId, getAttrSyncTimeForChatroom(chatroomId));
                if (mListenerMap != null) {
                    for (Map.Entry<String, IChatroomListener> entry : mListenerMap.entrySet()) {
                        mCore.getCallbackHandler().post(() -> entry.getValue().onChatroomJoin(chatroomId));
                    }
                }
            }

            @Override
            public void onError(int errorCode) {
                JLogger.e("CHRM-Join", "error code is " + errorCode);
                changeStatus(chatroomId, CachedChatroom.ChatroomStatus.FAILED);
                if (mListenerMap != null) {
                    for (Map.Entry<String, IChatroomListener> entry : mListenerMap.entrySet()) {
                        mCore.getCallbackHandler().post(() -> entry.getValue().onChatroomJoinFail(chatroomId, errorCode));
                    }
                }
            }
        });
        changeStatus(chatroomId, CachedChatroom.ChatroomStatus.JOINING);
    }

    @Override
    public void quitChatroom(String chatroomId) {
        if (chatroomId == null || chatroomId.isEmpty()) {
            JLogger.e("CHRM-Quit", "error chatroomId is empty");
            return;
        }
        mCore.getWebSocket().quitChatroom(chatroomId, new WebSocketTimestampCallback() {
            @Override
            public void onSuccess(long timestamp) {
                JLogger.i("CHRM-Quit", "success");
                changeStatus(chatroomId, CachedChatroom.ChatroomStatus.QUIT);
                mCore.getDbManager().clearChatroomMessage(chatroomId);
                if (mListenerMap != null) {
                    for (Map.Entry<String, IChatroomListener> entry : mListenerMap.entrySet()) {
                        mCore.getCallbackHandler().post(() -> entry.getValue().onChatroomQuit(chatroomId));
                    }
                }
            }

            @Override
            public void onError(int errorCode) {
                JLogger.e("CHRM-Quit", "error code is " + errorCode);
                if (mListenerMap != null) {
                    for (Map.Entry<String, IChatroomListener> entry : mListenerMap.entrySet()) {
                        mCore.getCallbackHandler().post(() -> entry.getValue().onChatroomQuitFail(chatroomId, errorCode));
                    }
                }
            }
        });
    }

    @Override
    public void setAttributes(String chatroomId, Map<String, String> attributes, IChatroomAttributesUpdateCallback callback) {
        if (chatroomId == null || chatroomId.isEmpty() || attributes == null || attributes.isEmpty()) {
            JLogger.e("CHRM-SetAttr", "error invalid parameters");
            if (callback != null) {
                mCore.getCallbackHandler().post(() -> callback.onComplete(JErrorCode.INVALID_PARAM, null));
            }
            return;
        }
        mCore.getWebSocket().setAttributes(chatroomId, attributes, new UpdateChatroomAttrCallback() {
            @Override
            public void onComplete(int code, List<ChatroomAttributeItem> items) {
                Map<String, Integer> resultMap = new HashMap<>();
                if (code == JErrorCode.NONE) {
                    for (ChatroomAttributeItem item : items) {
                        if (item.getCode() != 0) {
                            resultMap.put(item.getKey(), item.getCode());
                        }
                    }
                    if (resultMap.isEmpty()) {
                        JLogger.i("CHRM-SetAttr", "success");
                        if (callback != null) {
                            mCore.getCallbackHandler().post(() -> callback.onComplete(JErrorCode.NONE, null));
                        }
                    } else {
                        JLogger.e("CHRM-SetAttr", "partial fail");
                        if (callback != null) {
                            mCore.getCallbackHandler().post(() -> callback.onComplete(JErrorCode.CHATROOM_BATCH_SET_ATTRIBUTE_FAIL, resultMap));
                        }
                    }
                } else {
                    JLogger.e("CHRM-SetAttr", "fail, code is " + code);
                    for (Map.Entry<String, String> entry : attributes.entrySet()) {
                        resultMap.put(entry.getKey(), code);
                    }
                    if (callback != null) {
                        mCore.getCallbackHandler().post(() -> callback.onComplete(code, resultMap));
                    }
                }
            }
        });
    }

    @Override
    public void removeAttributes(String chatroomId, List<String> keys, IChatroomAttributesUpdateCallback callback) {
        if (chatroomId == null || chatroomId.isEmpty() || keys == null || keys.isEmpty()) {
            JLogger.e("CHRM-RmAttr", "error invalid parameters");
            if (callback != null) {
                mCore.getCallbackHandler().post(() -> callback.onComplete(JErrorCode.INVALID_PARAM, null));
            }
            return;
        }
        mCore.getWebSocket().removeAttributes(chatroomId, keys, new UpdateChatroomAttrCallback() {
            @Override
            public void onComplete(int code, List<ChatroomAttributeItem> items) {
                Map<String, Integer> resultMap = new HashMap<>();
                if (code == JErrorCode.NONE) {
                    for (ChatroomAttributeItem item : items) {
                        if (item.getCode() != 0) {
                            resultMap.put(item.getKey(), item.getCode());
                        }
                    }
                    if (resultMap.isEmpty()) {
                        JLogger.i("CHRM-RmAttr", "success");
                        if (callback != null) {
                            mCore.getCallbackHandler().post(() -> callback.onComplete(JErrorCode.NONE, null));
                        }
                    } else {
                        JLogger.e("CHRM-RmAttr", "partial fail");
                        if (callback != null) {
                            mCore.getCallbackHandler().post(() -> callback.onComplete(JErrorCode.CHATROOM_BATCH_SET_ATTRIBUTE_FAIL, resultMap));
                        }
                    }
                } else {
                    JLogger.e("CHRM-RmAttr", "fail, code is " + code);
                    for (String key : keys) {
                        resultMap.put(key, code);
                    }
                    if (callback != null) {
                        mCore.getCallbackHandler().post(() -> callback.onComplete(code, resultMap));
                    }
                }
            }
        });
    }

    @Override
    synchronized public void getAllAttributes(String chatroomId, IChatroomAttributesCallback callback) {
        CachedChatroom cachedChatroom = mCachedChatroomMap.get(chatroomId);
        if (cachedChatroom == null) {
            if (callback != null) {
                mCore.getCallbackHandler().post(() -> callback.onComplete(JErrorCode.NOT_CHATROOM_MEMBER, null));
            }
            return;
        }
        if (callback != null) {
            mCore.getCallbackHandler().post(() -> callback.onComplete(JErrorCode.NONE, cachedChatroom.getAttributes()));
        }
    }

    synchronized private void changeStatus(String chatroomId, CachedChatroom.ChatroomStatus status) {
        if (status == CachedChatroom.ChatroomStatus.QUIT
        || status == CachedChatroom.ChatroomStatus.FAILED) {
            mCachedChatroomMap.remove(chatroomId);
            return;
        }
        CachedChatroom cachedChatroom = mCachedChatroomMap.get(chatroomId);
        if (cachedChatroom == null) {
            cachedChatroom = new CachedChatroom();
        }
        cachedChatroom.setStatus(status);
        mCachedChatroomMap.put(chatroomId, cachedChatroom);
    }

    synchronized public long getSyncTimeForChatroom(String chatroomId) {
        CachedChatroom cachedChatroom = mCachedChatroomMap.get(chatroomId);
        if (cachedChatroom != null) {
            return cachedChatroom.getSyncTime();
        }
        return 0;
    }

    synchronized private long getAttrSyncTimeForChatroom(String chatroomId) {
        CachedChatroom cachedChatroom = mCachedChatroomMap.get(chatroomId);
        if (cachedChatroom != null) {
            return cachedChatroom.getAttrSyncTime();
        }
        return 0;
    }

    synchronized public void setSyncTime(String chatroomId, long syncTime) {
        CachedChatroom cachedChatroom = mCachedChatroomMap.get(chatroomId);
        if (cachedChatroom == null) {
            return;
        }
        if (syncTime > cachedChatroom.getSyncTime()) {
            cachedChatroom.setSyncTime(syncTime);
        }
    }

    synchronized private void setAttrSyncTime(String chatroomId, long syncTime) {
        CachedChatroom cachedChatroom = mCachedChatroomMap.get(chatroomId);
        if (cachedChatroom == null) {
            return;
        }
        if (syncTime > cachedChatroom.getAttrSyncTime()) {
            cachedChatroom.setAttrSyncTime(syncTime);
        }
    }

    synchronized public boolean isChatroomAvailable(String chatroomId) {
        CachedChatroom cachedChatroom = mCachedChatroomMap.get(chatroomId);
        return cachedChatroom != null;
    }

    synchronized private void updateAttributes(String chatroomId, Map<String, String> attributes, boolean isRemove) {
        CachedChatroom cachedChatroom = mCachedChatroomMap.get(chatroomId);
        if (cachedChatroom == null) {
            return;
        }
        Map<String, String> attrs = cachedChatroom.getAttributes();
        if (attrs == null) {
            attrs = new ConcurrentHashMap<>();
        }
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            if (isRemove) {
                attrs.remove(entry.getKey());
            } else {
                attrs.put(entry.getKey(), entry.getValue());
            }
        }
        cachedChatroom.setAttributes(attrs);
    }

    public void connectSuccess() {
        List<String> chatroomIds;
        synchronized (this) {
            if (mCachedChatroomMap.isEmpty()) {
                chatroomIds = new ArrayList<>();
            } else {
                chatroomIds = new ArrayList<>(mCachedChatroomMap.keySet());
            }
        }
        mCore.getDbManager().clearChatroomMessageExclude(chatroomIds);
        if (chatroomIds.isEmpty()) {
            return;
        }
        for (String chatroomId : chatroomIds) {
            joinChatroom(chatroomId);
        }
    }

    synchronized public void userDisconnect() {
        mCachedChatroomMap.clear();
    }

    @Override
    public void addListener(String key, IChatroomListener listener) {
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
    public void addAttributesListener(String key, IChatroomAttributesListener listener) {
        if (listener == null || TextUtils.isEmpty(key)) {
            return;
        }
        if (mAttributesListenerMap == null) {
            mAttributesListenerMap = new ConcurrentHashMap<>();
        }
        mAttributesListenerMap.put(key, listener);
    }

    @Override
    public void removeAttributesListener(String key) {
        if (!TextUtils.isEmpty(key) && mAttributesListenerMap != null) {
            mAttributesListenerMap.remove(key);
        }
    }

    @Override
    public void onSyncChatroomAttrNotify(String chatroomId, long syncTime) {
        if (!isChatroomAvailable(chatroomId)) {
            return;
        }
        long cachedSyncTime = getAttrSyncTimeForChatroom(chatroomId);
        if (syncTime > cachedSyncTime) {
            syncChatroomAttr(chatroomId, cachedSyncTime);
        }
    }

    @Override
    public void onAttributesSync(String chatroomId, List<ChatroomAttributeItem> items) {
        if (items.isEmpty()) {
            return;
        }
        Map<String, String> updateMap = new HashMap<>();
        Map<String, String> deleteMap = new HashMap<>();
        long syncTime = 0;
        for (ChatroomAttributeItem item : items) {
            if (item.getTimestamp() > syncTime) {
                syncTime = item.getTimestamp();
            }
            if (item.getType() == ChatroomAttributeItem.ChatroomAttrOptType.UPDATE) {
                updateMap.put(item.getKey(), item.getValue());
            } else if (item.getType() == ChatroomAttributeItem.ChatroomAttrOptType.DELETE) {
                deleteMap.put(item.getKey(), item.getValue());
            }
        }
        if (syncTime > 0) {
            setAttrSyncTime(chatroomId, syncTime);
        }
        if (!updateMap.isEmpty()) {
            updateAttributes(chatroomId, updateMap, false);
            if (mAttributesListenerMap != null) {
                for (Map.Entry<String, IChatroomAttributesListener> entry : mAttributesListenerMap.entrySet()) {
                    mCore.getCallbackHandler().post(() -> entry.getValue().onAttributesUpdate(chatroomId, updateMap));
                }
            }
        }
        if (!deleteMap.isEmpty()) {
            updateAttributes(chatroomId, deleteMap, true);
            if (mAttributesListenerMap != null) {
                for (Map.Entry<String, IChatroomAttributesListener> entry : mAttributesListenerMap.entrySet()) {
                    mCore.getCallbackHandler().post(() -> entry.getValue().onAttributesDelete(chatroomId, deleteMap));
                }
            }
        }
    }

    @Override
    public void onChatroomDestroy(String chatroomId) {
        changeStatus(chatroomId, CachedChatroom.ChatroomStatus.QUIT);
        if (mListenerMap != null) {
            for (Map.Entry<String, IChatroomListener> entry : mListenerMap.entrySet()) {
                mCore.getCallbackHandler().post(() -> entry.getValue().onChatroomDestroy(chatroomId));
            }
        }
    }

    @Override
    public void onChatroomQuit(String chatroomId) {
        changeStatus(chatroomId, CachedChatroom.ChatroomStatus.QUIT);
        if (mListenerMap != null) {
            for (Map.Entry<String, IChatroomListener> entry : mListenerMap.entrySet()) {
                mCore.getCallbackHandler().post(() -> entry.getValue().onChatroomQuit(chatroomId));
            }
        }
    }

    @Override
    public void onChatroomKick(String chatroomId) {
        changeStatus(chatroomId, CachedChatroom.ChatroomStatus.QUIT);
        if (mListenerMap != null) {
            for (Map.Entry<String, IChatroomListener> entry : mListenerMap.entrySet()) {
                mCore.getCallbackHandler().post(() -> entry.getValue().onChatroomKick(chatroomId));
            }
        }
    }

    void syncChatroomAttr(String chatroomId, long syncTime) {
        JLogger.i("CHRM-AttrSync", "id is " + chatroomId + ", time is " + syncTime);
        mCore.getWebSocket().syncChatroomAttributes(chatroomId, syncTime);
    }

    private final JIMCore mCore;
    private ConcurrentHashMap<String, IChatroomListener> mListenerMap;
    private ConcurrentHashMap<String, IChatroomAttributesListener> mAttributesListenerMap;
    private final ConcurrentHashMap<String, CachedChatroom> mCachedChatroomMap;
}
