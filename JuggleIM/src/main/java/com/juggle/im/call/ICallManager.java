package com.juggle.im.call;

import android.content.Context;

import java.util.List;

public interface ICallManager {
    interface ICallReceiveListener {
        void onCallReceive(ICallSession callSession);
    }

    void initZegoEngine(int appId, Context context);
    ICallSession startSingleCall(String userId, ICallSession.ICallSessionListener listener);
    ICallSession startSingleCall(String userId, CallConst.CallMediaType mediaType, ICallSession.ICallSessionListener listener);
    ICallSession startSingleCall(String userId, CallConst.CallMediaType mediaType, String extra, ICallSession.ICallSessionListener listener);
    ICallSession startMultiCall(List<String> userIdList, CallConst.CallMediaType mediaType, ICallSession.ICallSessionListener listener);
    ICallSession startMultiCall(List<String> userIdList, CallConst.CallMediaType mediaType, String extra, ICallSession.ICallSessionListener listener);
    void addReceiveListener(String key, ICallReceiveListener listener);
    void removeReceiveListener(String key);
    ICallSession getCallSession(String callId);
}
