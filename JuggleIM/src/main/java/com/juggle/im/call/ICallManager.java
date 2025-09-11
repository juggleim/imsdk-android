package com.juggle.im.call;

import android.content.Context;

import com.juggle.im.JIMConst;
import com.juggle.im.call.model.CallInfo;
import com.juggle.im.model.Conversation;

import java.util.List;

public interface ICallManager {
    interface ICallReceiveListener {
        void onCallReceive(ICallSession callSession);
    }

    interface IConversationCallListener {
        void onCallInfoUpdate(CallInfo callInfo, Conversation conversation, boolean isFinished);
    }

    void initZegoEngine(int appId, Context context);
    void initAgoraEngine(String appId, Context context);
    ICallSession startSingleCall(String userId, ICallSession.ICallSessionListener listener);
    ICallSession startSingleCall(String userId, CallConst.CallMediaType mediaType, ICallSession.ICallSessionListener listener);
    ICallSession startSingleCall(String userId, CallConst.CallMediaType mediaType, String extra, ICallSession.ICallSessionListener listener);
    ICallSession startMultiCall(List<String> userIdList, CallConst.CallMediaType mediaType, ICallSession.ICallSessionListener listener);
    ICallSession startMultiCall(List<String> userIdList, CallConst.CallMediaType mediaType, String extra, ICallSession.ICallSessionListener listener);
    ICallSession startMultiCall(List<String> userIdList, CallConst.CallMediaType mediaType, Conversation conversation, String extra, ICallSession.ICallSessionListener listener);
    ICallSession joinCall(String callId, ICallSession.ICallSessionListener listener);
    void addReceiveListener(String key, ICallReceiveListener listener);
    void removeReceiveListener(String key);
    ICallSession getCallSession(String callId);
    void getConversationCallInfo(Conversation conversation, JIMConst.IResultCallback<CallInfo> callback);
    void addConversationCallListener(String key, IConversationCallListener listener);
    void removeConversationCallListener(String key);
}
