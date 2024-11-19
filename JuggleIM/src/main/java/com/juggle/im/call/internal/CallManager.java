package com.juggle.im.call.internal;

import android.os.Message;
import android.text.TextUtils;

import com.juggle.im.JIM;
import com.juggle.im.call.CallConst;
import com.juggle.im.call.ICallManager;
import com.juggle.im.call.ICallSession;
import com.juggle.im.call.internal.model.RtcRoom;
import com.juggle.im.call.model.CallMember;
import com.juggle.im.internal.core.JIMCore;
import com.juggle.im.internal.core.network.JWebSocket;
import com.juggle.im.internal.core.network.RtcRoomListCallback;
import com.juggle.im.internal.util.JLogger;
import com.juggle.im.internal.util.JUtility;
import com.juggle.im.model.UserInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CallManager implements ICallManager, JWebSocket.IWebSocketCallListener, ICallSessionLifeCycleListener {
    @Override
    public void initZegoEngine(int appId) {

        //todo
    }

    @Override
    public ICallSession startSingleCall(String userId, ICallSession.ICallSessionListener listener) {
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
        String callId = JUtility.getUUID();
        CallSessionImpl callSession = createCallSessionImpl(callId, false);
        callSession.setOwner(JIM.getInstance().getCurrentUserId());
        CallMember member = new CallMember();
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(userId);
        member.setUserInfo(userInfo);
        member.setCallStatus(CallConst.CallStatus.INCOMING);
        callSession.addMember(member);
        callSession.addListener(callId+"-JIM", listener);
        callSession.sendMessage(CallEvent.INVITE);
        addCallSession(callSession);
        return callSession;
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
        if (!TextUtils.isEmpty(key) && mListenerMap != null) {
            mListenerMap.remove(key);
        }
    }

    public CallManager(JIMCore core) {
        this.mCore = core;
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
                                Map<String, UserInfo> userInfoMap = new HashMap<>();
                                for (CallMember member : singleRoom.getMembers()) {
                                    userInfoMap.put(member.getUserInfo().getUserId(), member.getUserInfo());
                                    if (!member.getUserInfo().getUserId().equals(mCore.getUserId())) {
                                        callSession.addMember(member);
                                    }
                                }
                                mCore.getDbManager().insertUserInfoList(new ArrayList<>(userInfoMap.values()));
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

    @Override
    public void onCallInvite(RtcRoom room, UserInfo inviter, List<UserInfo> targetUsers) {
        if (room == null || room.getRoomId() == null) {
            return;
        }
        Map<String, UserInfo> userMap = new HashMap<>();
        if (inviter != null && inviter.getUserId() != null) {
            userMap.put(inviter.getUserId(), inviter);
        }
        if (room.getOwner() != null && room.getOwner().getUserId() != null) {
            userMap.put(room.getOwner().getUserId(), room.getOwner());
        }
        boolean isInvite = false;
        for (UserInfo userInfo : targetUsers) {
            if (userInfo.getUserId() != null) {
                userMap.put(userInfo.getUserId(), userInfo);
                if (userInfo.getUserId().equals(mCore.getUserId())) {
                    isInvite = true;
                }
            }
        }
        mCore.getDbManager().insertUserInfoList(new ArrayList<>(userMap.values()));
        if (isInvite) {
            CallSessionImpl callSession = getCallSessionImpl(room.getRoomId());
            if (callSession == null) {
                callSession = createCallSessionImpl(room.getRoomId(), room.isMultiCall());
                if (room.getOwner() != null) {
                    callSession.setOwner(room.getOwner().getUserId());
                }
                if (inviter != null) {
                    callSession.setInviter(inviter.getUserId());
                }
                CallMember member = new CallMember();
                member.setUserInfo(inviter);
                member.setCallStatus(CallConst.CallStatus.OUTGOING);
                callSession.addMember(member);
                addCallSession(callSession);
            }
            callSession.sendMessage(CallEvent.RECEIVE_INVITE);
        }
    }

    @Override
    public void onCallHangup(RtcRoom room, UserInfo user) {
        if (room == null || user == null || user.getUserId() == null) {
            return;
        }
        Map<String, UserInfo> userInfoMap = new HashMap<>();
        userInfoMap.put(user.getUserId(), user);
        mCore.getDbManager().insertUserInfoList(new ArrayList<>(userInfoMap.values()));

        CallSessionImpl callSession = getCallSessionImpl(room.getRoomId());
        if (callSession == null) {
            return;
        }
        callSession.sendMessage(CallEvent.RECEIVE_HANGUP, user.getUserId());
    }

    @Override
    public void onCallAccept(RtcRoom room, UserInfo user) {
        if (room == null || user == null || user.getUserId() == null) {
            return;
        }
        Map<String, UserInfo> userInfoMap = new HashMap<>();
        userInfoMap.put(user.getUserId(), user);
        mCore.getDbManager().insertUserInfoList(new ArrayList<>(userInfoMap.values()));

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
            if (mListenerMap != null) {
                for (Map.Entry<String, ICallReceiveListener> entry : mListenerMap.entrySet()) {
                    entry.getValue().onCallReceive(session);
                }
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

    private void addCallSession(CallSessionImpl callSession) {
        synchronized (this) {
            mCallSessionList.add(callSession);
        }
    }

    private ConcurrentHashMap<String, ICallReceiveListener> mListenerMap = new ConcurrentHashMap<>();;
    private final JIMCore mCore;
    private final List<CallSessionImpl> mCallSessionList = new ArrayList<>();
    private CallInternalConst.CallEngineType mEngineType;
}
