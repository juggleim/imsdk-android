package com.juggle.im.call;

import android.content.Context;

public interface ICallManager {
    interface ICallReceiveListener {
        void onCallReceive(ICallSession callSession);
    }

    void initZegoEngine(int appId, Context context);
    ICallSession startSingleCall(String userId, ICallSession.ICallSessionListener listener);
    void addReceiveListener(String key, ICallReceiveListener listener);
    void removeReceiveListener(String key);
    ICallSession getCallSession(String callId);
}
