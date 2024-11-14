package com.juggle.im.call.internal;

import android.text.TextUtils;

import com.juggle.im.call.ICallManager;
import com.juggle.im.call.ICallSession;
import com.juggle.im.call.internal.model.RtcRoom;
import com.juggle.im.internal.core.JIMCore;
import com.juggle.im.internal.core.network.JWebSocket;
import com.juggle.im.model.UserInfo;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class CallManager implements ICallManager, JWebSocket.IWebSocketCallListener {
    @Override
    public void initZegoEngine(int appId) {

    }

    @Override
    public ICallSession startSingleCall(String userId) {

        return null;
    }

    @Override
    public void addReceiveListener(String key, ICallReceiveListener listener) {
        if (listener == null || TextUtils.isEmpty(key)) {
            return;
        }
        if (mListenerMap == null) {
            mListenerMap = new ConcurrentHashMap<>();
        }
        mListenerMap.put(key, listener);
    }

    @Override
    public void removeReceiveListener(String key) {
        if (!TextUtils.isEmpty(key) && mListenerMap != null) {
            mListenerMap.remove(key);
        }
    }

    public CallManager(JIMCore core) {
        this.mCore = core;
        mCore.getWebSocket().setCallListener(this);
    }

    @Override
    public void onCallInvite(RtcRoom room, UserInfo inviter, List<UserInfo> targetUsers) {

    }

    @Override
    public void onCallHangup(RtcRoom room, UserInfo user) {

    }

    @Override
    public void onCallAccept(RtcRoom room, UserInfo user) {

    }

    @Override
    public void onRoomDestroy(RtcRoom room) {

    }

    private ConcurrentHashMap<String, ICallReceiveListener> mListenerMap;
    private final JIMCore mCore;


}
