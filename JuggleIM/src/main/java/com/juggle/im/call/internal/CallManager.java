package com.juggle.im.call.internal;

import android.content.Context;
import android.text.TextUtils;

import com.juggle.im.JErrorCode;
import com.juggle.im.JIM;
import com.juggle.im.JIMConst;
import com.juggle.im.call.CallConst;
import com.juggle.im.call.ICallManager;
import com.juggle.im.call.ICallSession;
import com.juggle.im.call.internal.media.CallMediaManager;
import com.juggle.im.call.internal.model.CallActiveCallMessage;
import com.juggle.im.call.internal.model.RtcRoom;
import com.juggle.im.call.model.CallInfo;
import com.juggle.im.call.model.CallMember;
import com.juggle.im.internal.UserInfoManager;
import com.juggle.im.internal.core.JIMCore;
import com.juggle.im.internal.core.network.JWebSocket;
import com.juggle.im.internal.core.network.wscallback.RtcRoomListCallback;
import com.juggle.im.internal.core.network.wscallback.WebSocketDataCallback;
import com.juggle.im.internal.util.JLogger;
import com.juggle.im.internal.util.JUtility;
import com.juggle.im.model.Conversation;
import com.juggle.im.model.Message;
import com.juggle.im.model.UserInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CallManager implements ICallManager, JWebSocket.IWebSocketCallListener, ICallSessionLifeCycleListener {
    @Override
    public void initZegoEngine(int appId, Context context) {
        if (context == null) {
            JLogger.e("Call-Init", "context is null");
        }
        CallMediaManager.getInstance().initZegoEngine(appId, context);
        mEngineType = CallInternalConst.CallEngineType.ZEGO;
    }

    @Override
    public void initLiveKitEngine(Context context) {
        if (context == null) {
            JLogger.e("Call-Init", "context is null");
        }
        CallMediaManager.getInstance().initLiveKitEngine(context);
        mEngineType = CallInternalConst.CallEngineType.LIVEKIT;
    }

    @Override
    public void initAgoraEngine(String appId, Context context) {
        if (context == null) {
            JLogger.e("Call-Init", "context is null");
        }
        CallMediaManager.getInstance().initAgoraEngine(appId, context);
        mEngineType = CallInternalConst.CallEngineType.AGORA;
    }

    @Override
    public ICallSession startSingleCall(String userId, ICallSession.ICallSessionListener listener) {
        return startSingleCall(userId, CallConst.CallMediaType.VOICE, listener);
    }

    @Override
    public ICallSession startSingleCall(String userId, CallConst.CallMediaType mediaType, ICallSession.ICallSessionListener listener) {
        return startSingleCall(userId, mediaType, "", listener);
    }

    @Override
    public ICallSession startSingleCall(String userId, CallConst.CallMediaType mediaType, String extra, ICallSession.ICallSessionListener listener) {
        if (TextUtils.isEmpty(userId)) {
            if (listener != null) {
                mCore.getCallbackHandler().post(() -> {
                    listener.onErrorOccur(CallConst.CallErrorCode.INVALID_PARAMETER);
                });
            }
            return null;
        }
        return startCall(Collections.singletonList(userId), false, mediaType, null, extra, listener);
    }

    @Override
    public ICallSession startMultiCall(List<String> userIdList, CallConst.CallMediaType mediaType, ICallSession.ICallSessionListener listener) {
        return startCall(userIdList, true, mediaType, null, "", listener);
    }

    @Override
    public ICallSession startMultiCall(List<String> userIdList, CallConst.CallMediaType mediaType, String extra, ICallSession.ICallSessionListener listener) {
        return startCall(userIdList, true, mediaType, null, extra, listener);
    }

    @Override
    public ICallSession startMultiCall(List<String> userIdList, CallConst.CallMediaType mediaType, Conversation conversation, String extra, ICallSession.ICallSessionListener listener) {
        return startCall(userIdList, true, mediaType, conversation, extra, listener);
    }

    @Override
    public void addReceiveListener(String key, ICallReceiveListener listener) {
        if (listener == null || TextUtils.isEmpty(key)) {
            return;
        }
        mListenerMap.put(key, listener);
    }

    @Override
    public void removeReceiveListener(String key) {
        if (!TextUtils.isEmpty(key)) {
            mListenerMap.remove(key);
        }
    }

    @Override
    public ICallSession joinCall(String callId, ICallSession.ICallSessionListener listener) {
        synchronized (this) {
            if (!mCallSessionList.isEmpty()) {
                if (listener != null) {
                    mCore.getCallbackHandler().post(() -> {
                        listener.onErrorOccur(CallConst.CallErrorCode.CALL_EXIST);
                    });
                }
                return null;
            }
        }
        if (TextUtils.isEmpty(callId)) {
            if (listener != null) {
                mCore.getCallbackHandler().post(() -> {
                    listener.onErrorOccur(CallConst.CallErrorCode.INVALID_PARAMETER);
                });
            }
            return null;
        }
        CallSessionImpl callSession = createCallSessionImpl(callId, true);
        callSession.addListener(callId+"-JIM", listener);
        callSession.sendMessage(CallEvent.JOIN);
        addCallSession(callSession);
        return callSession;
    }

    @Override
    public void getConversationCallInfo(Conversation conversation, JIMConst.IResultCallback<CallInfo> callback) {
        if (conversation == null || TextUtils.isEmpty(conversation.getConversationId())) {
            JLogger.e("Call-GetConvCall", "error, invalid param");
            if (callback != null) {
                mCore.getCallbackHandler().post(() -> {
                    callback.onError(CallConst.CallErrorCode.INVALID_PARAMETER.getValue());
                });
            }
            return;
        }
        if (mCore.getWebSocket() == null) {
            int errorCode = JErrorCode.CONNECTION_UNAVAILABLE;
            JLogger.e("Call-GetConvCall", "error, connection unavailable");
            if (callback != null) {
                mCore.getCallbackHandler().post(() -> {
                    callback.onError(errorCode);
                });
            }
            return;
        }
        mCore.getWebSocket().getConversationCallInfo(conversation, mCore.getUserId(), new WebSocketDataCallback<CallInfo>() {
            @Override
            public void onSuccess(CallInfo data) {
                JLogger.i("Call-GetConvCall", "success");
                if (callback != null) {
                    mCore.getCallbackHandler().post(() -> {
                        callback.onSuccess(data);
                    });
                }
            }

            @Override
            public void onError(int errorCode) {
                JLogger.e("Call-GetConvCall", "error, code is " + errorCode);
                if (callback != null) {
                    mCore.getCallbackHandler().post(() -> {
                        callback.onError(errorCode);
                    });
                }
            }
        });
    }

    @Override
    public void addConversationCallListener(String key, IConversationCallListener listener) {
        if (listener == null || TextUtils.isEmpty(key)) {
            return;
        }
        mConversationCallListenerMap.put(key, listener);
    }

    @Override
    public void removeConversationCallListener(String key) {
        if (!TextUtils.isEmpty(key)) {
            mConversationCallListenerMap.remove(key);
        }
    }

    public CallManager(JIMCore core, UserInfoManager userInfoManager) {
        this.mCore = core;
        this.mUserInfoManager = userInfoManager;
        mCore.getWebSocket().setCallListener(this);
    }

    public void connectSuccess() {
        mCore.getWebSocket().queryCallRooms(mCore.getUserId(), new RtcRoomListCallback() {
            @Override
            public void onSuccess(List<RtcRoom> rooms) {
                JLogger.i("Call-Qry", "query call rooms, count is " + rooms.size());
                for (RtcRoom room : rooms) {
                    if (room.getDeviceId() == null
                    || room.getDeviceId().isEmpty()
                    || room.getDeviceId().equals(JUtility.getDeviceId(mCore.getContext()))) {
                        CallConst.CallStatus callStatus = room.getCallStatus();
                        mCore.getWebSocket().queryCallRoom(room.getRoomId(), new RtcRoomListCallback() {
                            @Override
                            public void onSuccess(List<RtcRoom> singleRooms) {
                                JLogger.i("Call-Qry", "query call room success");
                                if (singleRooms == null || singleRooms.isEmpty()) {
                                    JLogger.w("Call-Qry", "query call room count is 0");
                                    return;
                                }
                                RtcRoom singleRoom = singleRooms.get(0);
                                CallSessionImpl callSession = createCallSessionImpl(singleRoom.getRoomId(), singleRoom.isMultiCall());
                                callSession.setOwner(singleRoom.getOwner().getUserId());
                                callSession.setExtra(singleRoom.getExtra());
                                Map<String, UserInfo> userInfoMap = new HashMap<>();
                                for (CallMember member : singleRoom.getMembers()) {
                                    userInfoMap.put(member.getUserInfo().getUserId(), member.getUserInfo());
                                    if (!member.getUserInfo().getUserId().equals(mCore.getUserId())) {
                                        callSession.addMember(member);
                                    }
                                }
                                mUserInfoManager.insertUserInfoList(new ArrayList<>(userInfoMap.values()));
                                initCallSessionWithCallStatus(callSession, callStatus);
                            }

                            @Override
                            public void onError(int errorCode) {
                                JLogger.e("Call-Qry", "query call room error, code is " + errorCode);
                            }
                        });
                    }
                }
            }

            @Override
            public void onError(int errorCode) {
                JLogger.e("Call-Qry", "query call rooms error, code is " + errorCode);
            }
        });
    }

    public void imKick() {
        synchronized (this) {
            for (CallSessionImpl loopSession : mCallSessionList) {
                loopSession.sendMessage(CallEvent.HANGUP);
            }
        }
    }

    public void handleActiveCallMessage(Message message) {
        mCore.getCallbackHandler().post(() -> {
            CallActiveCallMessage activeCallMessage = (CallActiveCallMessage) message.getContent();
            for (Map.Entry<String, IConversationCallListener> entry : mConversationCallListenerMap.entrySet()) {
                entry.getValue().onCallInfoUpdate(activeCallMessage.getCallInfo(), message.getConversation(), activeCallMessage.isFinished());
            }
        });
    }

    @Override
    public void onCallInvite(RtcRoom room, UserInfo inviter, List<UserInfo> targetUsers) {
        if (room == null || room.getRoomId() == null) {
            return;
        }
        Map<String, UserInfo> userMap = new HashMap<>();
        for (CallMember member : room.getMembers()) {
            userMap.put(member.getUserInfo().getUserId(), member.getUserInfo());
        }
        mUserInfoManager.insertUserInfoList(new ArrayList<>(userMap.values()));
        CallSessionImpl callSession = getCallSessionImpl(room.getRoomId());
        if (callSession != null) {
            Map<String, Object> m = new HashMap<>();
            m.put("inviter", inviter);
            m.put("targetUsers", targetUsers);
            callSession.sendMessage(CallEvent.RECEIVE_INVITE_OTHERS, m);
        } else {
            boolean isInvite = false;
            for (UserInfo userInfo : targetUsers) {
                if (userInfo.getUserId().equals(mCore.getUserId())) {
                    isInvite = true;
                    break;
                }
            }
            if (isInvite) {
                callSession = createCallSessionImpl(room.getRoomId(), room.isMultiCall());
                callSession.setOwner(room.getOwner().getUserId());
                callSession.setInviter(inviter.getUserId());
                callSession.setMediaType(room.getMediaType());
                callSession.setExtra(room.getExtra());
                CallMediaManager.getInstance().enableCamera(callSession.getMediaType() == CallConst.CallMediaType.VIDEO);
                for (CallMember member : room.getMembers()) {
                    if (!member.getUserInfo().getUserId().equals(mCore.getUserId())) {
                        callSession.addMember(member);
                    }
                }
                addCallSession(callSession);
                callSession.sendMessage(CallEvent.RECEIVE_INVITE);
            }
        }
    }

    @Override
    public void onCallHangup(RtcRoom room, UserInfo user) {
        if (room == null || user == null || user.getUserId() == null) {
            return;
        }
        Map<String, UserInfo> userInfoMap = new HashMap<>();
        userInfoMap.put(user.getUserId(), user);
        mUserInfoManager.insertUserInfoList(new ArrayList<>(userInfoMap.values()));

        CallSessionImpl callSession = getCallSessionImpl(room.getRoomId());
        if (callSession == null) {
            return;
        }
        callSession.sendMessage(CallEvent.RECEIVE_HANGUP, user.getUserId());
    }

    @Override
    public void onCallQuit(RtcRoom room, List<CallMember> members) {
        if (room == null || members == null || members.isEmpty()) {
            return;
        }
        Map<String, UserInfo> userMap = new HashMap<>();
        boolean includeCurrent = false;
        for (CallMember member : members) {
            userMap.put(member.getUserInfo().getUserId(), member.getUserInfo());
            if (mCore.getUserId().equals(member.getUserInfo().getUserId())) {
                includeCurrent = true;
            }
        }
        mUserInfoManager.insertUserInfoList(new ArrayList<>(userMap.values()));

        CallSessionImpl callSession = getCallSessionImpl(room.getRoomId());
        if (callSession == null) {
            return;
        }
        if (includeCurrent) {
            callSession.sendMessage(CallEvent.RECEIVE_SELF_QUIT);
        } else {
            callSession.sendMessage(CallEvent.RECEIVE_QUIT, new ArrayList<>(userMap.keySet()));
        }
    }

    @Override
    public void onCallAccept(RtcRoom room, UserInfo user) {
        if (room == null || user == null || user.getUserId() == null) {
            return;
        }
        Map<String, UserInfo> userInfoMap = new HashMap<>();
        userInfoMap.put(user.getUserId(), user);
        mUserInfoManager.insertUserInfoList(new ArrayList<>(userInfoMap.values()));

        CallSessionImpl callSession = getCallSessionImpl(room.getRoomId());
        if (callSession == null) {
            return;
        }
        callSession.sendMessage(CallEvent.RECEIVE_ACCEPT, user.getUserId());
    }

    @Override
    public void onRoomDestroy(RtcRoom room) {
        if (room == null) {
            return;
        }
        CallSessionImpl callSession = getCallSessionImpl(room.getRoomId());
        if (callSession == null) {
            return;
        }
        callSession.sendMessage(CallEvent.ROOM_DESTROY);
    }

    @Override
    public void onUserJoin(List<CallMember> users, RtcRoom room) {
        List<UserInfo> userInfoList = new ArrayList<>();
        for (CallMember member : users) {
            userInfoList.add(member.getUserInfo());
        }
        mUserInfoManager.insertUserInfoList(userInfoList);
        CallSessionImpl callSession = getCallSessionImpl(room.getRoomId());
        if (callSession == null) {
            return;
        }
        callSession.sendMessage(CallEvent.RECEIVE_JOIN, userInfoList);
    }

    @Override
    public void onSessionFinish(CallSessionImpl session) {
        synchronized (this) {
            if (session != null) {
                mCallSessionList.remove(session);
            }
        }
    }

    @Override
    public void onCallReceive(CallSessionImpl session) {
        mCore.getCallbackHandler().post(() -> {
            for (Map.Entry<String, ICallReceiveListener> entry : mListenerMap.entrySet()) {
                entry.getValue().onCallReceive(session);
            }
        });
    }

    @Override
    public boolean onCallAccept(CallSessionImpl session) {
        boolean needHangupOther = false;
        synchronized (this) {
            for (CallSessionImpl loopSession : mCallSessionList) {
                if (loopSession.getCallId() == null) {
                    continue;
                }
                if (!loopSession.getCallId().equals(session.getCallId())) {
                    loopSession.sendMessage(CallEvent.HANGUP);
                    needHangupOther = true;
                }
            }
        }
        return needHangupOther;
    }

    private ICallSession startCall(List<String> userIdList, boolean isMulti, CallConst.CallMediaType mediaType, Conversation conversation, String extra, ICallSession.ICallSessionListener listener) {
        synchronized (this) {
            if (!mCallSessionList.isEmpty()) {
                if (listener != null) {
                    mCore.getCallbackHandler().post(() -> {
                        listener.onErrorOccur(CallConst.CallErrorCode.CALL_EXIST);
                    });
                }
                return null;
            }
        }
        if (userIdList == null || userIdList.isEmpty()) {
            if (listener != null) {
                mCore.getCallbackHandler().post(() -> {
                    listener.onErrorOccur(CallConst.CallErrorCode.INVALID_PARAMETER);
                });
            }
            return null;
        }
        String callId = JUtility.getUUID();
        CallSessionImpl callSession = createCallSessionImpl(callId, isMulti);
        callSession.setCallStatus(CallConst.CallStatus.OUTGOING);
        callSession.setOwner(JIM.getInstance().getCurrentUserId());
        callSession.setMediaType(mediaType);
        callSession.setExtra(extra);
        callSession.setConversation(conversation);
        CallMediaManager.getInstance().enableCamera(mediaType == CallConst.CallMediaType.VIDEO);
        for (String userId : userIdList) {
            CallMember member = new CallMember();
            UserInfo userInfo = getUserInfo(userId);
            member.setUserInfo(userInfo);
            member.setCallStatus(CallConst.CallStatus.INCOMING);
            member.setStartTime(System.currentTimeMillis());
            UserInfo inviter = getUserInfo(mCore.getUserId());
            member.setInviter(inviter);
            callSession.addMember(member);
        }
        callSession.addListener(callId+"-JIM", listener);
        callSession.sendMessage(CallEvent.INVITE);
        addCallSession(callSession);
        return callSession;
    }

    private void initCallSessionWithCallStatus(CallSessionImpl callSession, CallConst.CallStatus callStatus) {
        if (callStatus == CallConst.CallStatus.INCOMING) {
            addCallSession(callSession);
            callSession.sendMessage(CallEvent.RECEIVE_INVITE);
        }
    }

    private CallSessionImpl createCallSessionImpl(String callId, boolean isMultiCall) {
        CallSessionImpl callSession = new CallSessionImpl(callId);
        callSession.setCallId(callId);
        callSession.setMultiCall(isMultiCall);
        callSession.setEngineType(mEngineType);
        callSession.setStartTime(System.currentTimeMillis());
        callSession.setCore(mCore);
        callSession.setSessionLifeCycleListener(this);
        return callSession;
    }

    private CallSessionImpl getCallSessionImpl(String callId) {
        if (callId == null || callId.isEmpty()) {
            return null;
        }
        synchronized (this) {
            for (CallSessionImpl session : mCallSessionList) {
                if (session.getCallId() != null && session.getCallId().equals(callId)) {
                    return session;
                }
            }
        }
        return null;
    }

    private UserInfo getUserInfo(String userId) {
        UserInfo userInfo = JIM.getInstance().getUserInfoManager().getUserInfo(userId);
        if (userInfo == null) {
            userInfo = new UserInfo();
            userInfo.setUserId(userId);
        }
        return userInfo;
    }

    public ICallSession getCallSession(String callId) {
        return getCallSessionImpl(callId);
    }

    private void addCallSession(CallSessionImpl callSession) {
        synchronized (this) {
            mCallSessionList.add(callSession);
        }
    }

    private final ConcurrentHashMap<String, ICallReceiveListener> mListenerMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, IConversationCallListener> mConversationCallListenerMap = new ConcurrentHashMap<>();
    private final JIMCore mCore;
    private final UserInfoManager mUserInfoManager;
    private final List<CallSessionImpl> mCallSessionList = new ArrayList<>();
    private CallInternalConst.CallEngineType mEngineType;
}
