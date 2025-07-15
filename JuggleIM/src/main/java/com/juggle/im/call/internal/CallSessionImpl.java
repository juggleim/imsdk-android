package com.juggle.im.call.internal;

import android.os.Message;
import android.text.TextUtils;
import android.view.View;

import com.juggle.im.JIM;
import com.juggle.im.call.CallConst;
import com.juggle.im.call.ICallSession;
import com.juggle.im.call.internal.fsm.CallConnectedState;
import com.juggle.im.call.internal.fsm.CallConnectingState;
import com.juggle.im.call.internal.fsm.CallIdleState;
import com.juggle.im.call.internal.fsm.CallIncomingState;
import com.juggle.im.call.internal.fsm.CallOutgoingState;
import com.juggle.im.call.internal.fsm.CallSuperState;
import com.juggle.im.call.internal.media.CallMediaManager;
import com.juggle.im.call.internal.media.ICallCompleteCallback;
import com.juggle.im.call.internal.media.ICallMediaListener;
import com.juggle.im.call.model.CallMember;
import com.juggle.im.internal.core.JIMCore;
import com.juggle.im.internal.core.network.wscallback.CallAuthCallback;
import com.juggle.im.internal.core.network.wscallback.WebSocketSimpleCallback;
import com.juggle.im.internal.util.JLogger;
import com.juggle.im.internal.util.statemachine.StateMachine;
import com.juggle.im.model.UserInfo;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CallSessionImpl extends StateMachine implements ICallSession, ICallMediaListener {
    protected CallSessionImpl(String name) {
        super(name);

        mSuperState = new CallSuperState();
        mSuperState.setCallSessionImpl(this);
        mIdleState = new CallIdleState();
        mIdleState.setCallSessionImpl(this);
        mOutgoingState = new CallOutgoingState();
        mOutgoingState.setCallSessionImpl(this);
        mIncomingState = new CallIncomingState();
        mIncomingState.setCallSessionImpl(this);
        mConnectingState = new CallConnectingState();
        mConnectingState.setCallSessionImpl(this);
        mConnectedState = new CallConnectedState();
        mConnectedState.setCallSessionImpl(this);

        addState(mSuperState, null);
        addState(mIdleState, mSuperState);
        addState(mOutgoingState, mSuperState);
        addState(mIncomingState, mSuperState);
        addState(mConnectingState, mSuperState);
        addState(mConnectedState, mSuperState);

        setInitialState(mIdleState);
        start();
    }

    @Override
    public void addListener(String key, ICallSessionListener listener) {
        if (listener == null || TextUtils.isEmpty(key)) {
            return;
        }
        if (mListeners == null) {
            mListeners = new ConcurrentHashMap<>();
        }
        mListeners.put(key, listener);
    }

    @Override
    public void removeListener(String key) {
        if (!TextUtils.isEmpty(key) && mListeners != null) {
            mListeners.remove(key);
        }
    }

    @Override
    public void accept() {
        sendMessage(CallEvent.ACCEPT);
    }

    @Override
    public void hangup() {
        Message msg = Message.obtain();
        msg.what = CallEvent.HANGUP;
        sendMessage(msg);
    }

    @Override
    public void inviteUsers(List<String> userIdList) {
        sendMessage(CallEvent.INVITE, userIdList);
    }

    @Override
    public void enableCamera(boolean isEnable) {
        CallMediaManager.getInstance().enableCamera(isEnable);
    }

    @Override
    public void setVideoView(String userId, View view) {
        if (TextUtils.isEmpty(userId)) {
            return;
        }
        if (userId.equals(JIM.getInstance().getCurrentUserId())) {
            CallMediaManager.getInstance().startPreview(view);
        } else {
            if (view != null) {
                mViewMap.put(userId, view);
            } else {
                mViewMap.remove(userId);
            }
            if (mCallStatus == CallConst.CallStatus.CONNECTED) {
                CallMediaManager.getInstance().setVideoView(mCallId, userId, view);
            }
        }
    }

    @Override
    public void startPreview(View view) {
        CallMediaManager.getInstance().startPreview(view);
    }

    @Override
    public void muteMicrophone(boolean isMute) {
        CallMediaManager.getInstance().muteMicrophone(isMute);
    }

    @Override
    public void muteSpeaker(boolean isMute) {
        CallMediaManager.getInstance().muteSpeaker(isMute);
    }

    @Override
    public void setSpeakerEnable(boolean isEnable) {
        CallMediaManager.getInstance().setSpeakerEnable(isEnable);
    }

    @Override
    public void useFrontCamera(boolean isEnable) {
        CallMediaManager.getInstance().useFrontCamera(isEnable);
    }

    @Override
    public String getCallId() {
        return mCallId;
    }

    @Override
    public boolean isMultiCall() {
        return mIsMultiCall;
    }

    @Override
    public CallConst.CallMediaType getMediaType() {
        return mMediaType;
    }

    @Override
    public CallConst.CallStatus getCallStatus() {
        return mCallStatus;
    }

    @Override
    public long getStartTime() {
        return mStartTime;
    }

    @Override
    public long getConnectTime() {
        return mConnectTime;
    }

    @Override
    public long getFinishTime() {
        return mFinishTime;
    }

    @Override
    public String getOwner() {
        return mOwner;
    }

    @Override
    public String getInviter() {
        return mInviter;
    }

    @Override
    public CallConst.CallFinishReason getFinishReason() {
        return mFinishReason;
    }

    @Override
    public List<CallMember> getMembers() {
        return mMembers;
    }

    public void error(CallConst.CallErrorCode code) {
        if (mListeners != null) {
            for (Map.Entry<String, ICallSessionListener> entry : mListeners.entrySet()) {
                mCore.getCallbackHandler().post(() -> {
                    entry.getValue().onErrorOccur(code);
                });
            }
        }
    }

    public void notifyReceiveCall() {
        if (mSessionLifeCycleListener != null) {
            mSessionLifeCycleListener.onCallReceive(this);
        }
    }

    public boolean notifyAcceptCall() {
        boolean result = false;
        if (mSessionLifeCycleListener != null) {
            result = mSessionLifeCycleListener.onCallAccept(this);
        }
        return result;
    }

    public void memberHangup(String userId) {
        removeMember(userId);
        if (!mIsMultiCall) {
            mFinishTime = System.currentTimeMillis();
            if (mCallStatus == CallConst.CallStatus.OUTGOING) {
                mFinishReason = CallConst.CallFinishReason.OTHER_SIDE_DECLINE;
            } else if (mCallStatus == CallConst.CallStatus.INCOMING) {
                mFinishReason = CallConst.CallFinishReason.OTHER_SIDE_CANCEL;
            } else {
                mFinishReason = CallConst.CallFinishReason.OTHER_SIDE_HANGUP;
            }
        } else {
            if (mListeners != null) {
                for (Map.Entry<String, ICallSessionListener> entry : mListeners.entrySet()) {
                    mCore.getCallbackHandler().post(() -> {
                        entry.getValue().onUsersLeave(Collections.singletonList(userId));
                    });
                }
            }
        }
    }

    public void membersQuit(List<String> userIdList) {
        for (String userId : userIdList) {
            removeMember(userId);
        }
        if (!mIsMultiCall) {
            mFinishTime = System.currentTimeMillis();
            mFinishReason = CallConst.CallFinishReason.OTHER_SIDE_NO_RESPONSE;
        } else {
            if (mListeners != null) {
                for (Map.Entry<String, ICallSessionListener> entry : mListeners.entrySet()) {
                    mCore.getCallbackHandler().post(() -> {
                        entry.getValue().onUsersLeave(userIdList);
                    });
                }
            }
        }
    }

    public void memberAccept(String userId) {
        for (CallMember member : mMembers) {
            if (member.getUserInfo().getUserId().equals(userId)) {
                member.setCallStatus(CallConst.CallStatus.CONNECTING);
            }
        }
    }

    public void membersInviteBySelf(List<String> userIdList) {
        List<String> resultList = new ArrayList<>();
        for (String userId : userIdList) {
            boolean isExist = false;
            for (CallMember member : mMembers) {
                if (userId.equals(member.getUserInfo().getUserId())) {
                    isExist = true;
                    break;
                }
            }
            if (!isExist) {
                CallMember newMember = new CallMember();
                UserInfo userInfo = JIM.getInstance().getUserInfoManager().getUserInfo(userId);
                if (userInfo == null) {
                    userInfo = new UserInfo();
                    userInfo.setUserId(userId);
                }
                newMember.setUserInfo(userInfo);
                newMember.setCallStatus(CallConst.CallStatus.INCOMING);
                newMember.setStartTime(System.currentTimeMillis());
                newMember.setInviter(JIM.getInstance().getUserInfoManager().getUserInfo(mCore.getUserId()));
                mMembers.add(newMember);
                resultList.add(userId);
            }
        }
        if (!resultList.isEmpty()) {
            if (mListeners != null) {
                for (Map.Entry<String, ICallSessionListener> entry : mListeners.entrySet()) {
                    mCore.getCallbackHandler().post(() -> {
                        entry.getValue().onUsersInvite(mCore.getUserId(), resultList);
                    });
                }
            }
        }
    }

    public void addInviteMembers(UserInfo inviter, List<UserInfo> targetUsers) {
        if (inviter == null || targetUsers == null) {
            return;
        }
        List<String> userIdList = new ArrayList<>();
        for (UserInfo userInfo : targetUsers) {
            if (userInfo.getUserId().equals(mCore.getUserId())) {
                continue;
            }
            boolean isExist = false;
            for (CallMember member : mMembers) {
                if (userInfo.getUserId().equals(member.getUserInfo().getUserId())) {
                    isExist = true;
                    break;
                }
            }
            if (!isExist) {
                CallMember newMember = new CallMember();
                newMember.setUserInfo(userInfo);
                newMember.setCallStatus(CallConst.CallStatus.INCOMING);
                newMember.setStartTime(System.currentTimeMillis());
                newMember.setInviter(inviter);
                mMembers.add(newMember);
                userIdList.add(userInfo.getUserId());
            }
        }
        if (!userIdList.isEmpty()) {
            if (mListeners != null) {
                for (Map.Entry<String, ICallSessionListener> entry : mListeners.entrySet()) {
                    mCore.getCallbackHandler().post(() -> {
                        entry.getValue().onUsersInvite(inviter.getUserId(), userIdList);
                    });
                }
            }
        }
    }

    public void addMember(CallMember member) {
        mMembers.add(member);
    }

    public void removeMember(String userId) {
        for (CallMember member : mMembers) {
            if (member.getUserInfo() != null
            && member.getUserInfo().getUserId() != null
            && member.getUserInfo().getUserId().equals(userId)) {
                mMembers.remove(member);
                return;
            }
        }
    }

    public void membersConnected(List<String> userIdList) {
        for (String userId : userIdList) {
            for (CallMember member : mMembers) {
                if (member.getUserInfo().getUserId().equals(userId)) {
                    member.setCallStatus(CallConst.CallStatus.CONNECTED);
                    break;
                }
            }
        }
        if (mListeners != null) {
            for (Map.Entry<String, ICallSessionListener> entry : mListeners.entrySet()) {
                mCore.getCallbackHandler().post(() -> {
                    entry.getValue().onUsersConnect(userIdList);
                });
            }
        }
    }

    public void cameraEnable(String userId, boolean enable) {
        if (mListeners != null) {
            for (Map.Entry<String, ICallSessionListener> entry : mListeners.entrySet()) {
                mCore.getCallbackHandler().post(() -> {
                    entry.getValue().onUserCameraEnable(userId, enable);
                });
            }
        }
    }

    public void soundLevelUpdate(HashMap<String, Float> soundLevels) {
        if (mListeners != null) {
            for (Map.Entry<String, ICallSessionListener> entry : mListeners.entrySet()) {
                mCore.getCallbackHandler().post(() -> {
                   entry.getValue().onSoundLevelUpdate(soundLevels);
                });
            }
        }
    }

    public void signalInvite() {
        List<String> targetIds = new ArrayList<>();
        for (CallMember member : mMembers) {
            targetIds.add(member.getUserInfo().getUserId());
        }
        signalInvite(targetIds);
    }

    public void signalInvite(List<String> userIdList) {
        mCore.getWebSocket().callInvite(mCallId, mIsMultiCall, mMediaType, userIdList, mEngineType.getValue(), new CallAuthCallback() {
            @Override
            public void onSuccess(String zegoToken) {
                JLogger.i("Call-Signal", "send invite success");
                mZegoToken = zegoToken;
                sendMessage(CallEvent.INVITE_DONE, userIdList);
            }

            @Override
            public void onError(int errorCode) {
                JLogger.e("Call-Signal", "send invite error, code is " + errorCode);
                sendMessage(CallEvent.INVITE_FAIL);
            }
        });
    }

    public void signalHangup() {
        mCore.getWebSocket().callHangup(mCallId, new WebSocketSimpleCallback() {
            @Override
            public void onSuccess() {
                JLogger.i("Call-Signal", "send hangup success");
            }

            @Override
            public void onError(int errorCode) {
                JLogger.e("Call-Signal", "send hangup error, code is " + errorCode);
            }
        });
    }

    public void signalAccept() {
        mCore.getWebSocket().callAccept(mCallId, new CallAuthCallback() {
            @Override
            public void onSuccess(String zegoToken) {
                JLogger.i("Call-Signal", "send accept success");
                mZegoToken = zegoToken;
                sendMessage(CallEvent.ACCEPT_DONE);
            }

            @Override
            public void onError(int errorCode) {
                JLogger.e("Call-Signal", "send accept error, code is " + errorCode);
                sendMessage(CallEvent.ACCEPT_FAIL);

            }
        });
    }

    public void signalConnected() {
        mCore.getWebSocket().callConnected(mCallId, new WebSocketSimpleCallback() {
            @Override
            public void onSuccess() {
                JLogger.i("Call-Signal", "call connected success");
            }

            @Override
            public void onError(int errorCode) {
                JLogger.e("Call-Signal", "call connected error, code is " + errorCode);
            }
        });
    }

    public void ping() {
        mCore.getWebSocket().rtcPing(mCallId);
    }

    public void mediaQuit() {
        JLogger.i("Call-Media", "media quit");
        CallMediaManager.getInstance().stopPreview();
        CallMediaManager.getInstance().leaveRoom(mCallId);
    }

    public void mediaJoin() {
        JLogger.i("Call-Media", "media join");
        CallMediaManager.getInstance().joinRoom(this, new ICallCompleteCallback() {
            @Override
            public void onComplete(int errorCode, JSONObject data) {
                if (errorCode == 0) {
                    JLogger.i("Call-Media", "join room success");
                    sendMessage(CallEvent.JOIN_CHANNEL_DONE);
                } else {
                    JLogger.e("Call-Media", "join room error, code is " + errorCode);
                    sendMessage(CallEvent.JOIN_CHANNEL_FAIL, errorCode);
                }
            }
        });
    }

    public void transitionToConnectedState() {
        transitionTo(mConnectedState);
        signalConnected();
        mCore.getCallbackHandler().post(() -> {
            if (mListeners != null) {
                for (Map.Entry<String, ICallSessionListener> entry : mListeners.entrySet()) {
                    entry.getValue().onCallConnect();
                }
            }
        });
    }

    public void transitionToConnectingState() {
        transitionTo(mConnectingState);
    }

    public void transitionToIdleState() {
        transitionTo(mIdleState);
        mediaQuit();
        mCore.getCallbackHandler().post(() -> {
            if (mListeners != null) {
                for (Map.Entry<String, ICallSessionListener> entry : mListeners.entrySet()) {
                    entry.getValue().onCallFinish(mFinishReason);
                }
            }
            destroy();
        });
    }

    public void transitionToIncomingState() {
        transitionTo(mIncomingState);
    }

    public void transitionToOutgoingState() {
        transitionTo(mOutgoingState);
    }

    @Override
    public View viewForUserId(String userId) {
        return mViewMap.get(userId);
    }

    @Override
    public void onUsersJoin(List<String> userIdList) {
        sendMessage(CallEvent.PARTICIPANT_JOIN_CHANNEL, userIdList);
    }

    @Override
    public void onUserCameraChange(String userId, boolean enable) {
        Map<String, Object> map = new HashMap<>();
        map.put("enable", enable);
        map.put("userId", userId);
        sendMessage(CallEvent.PARTICIPANT_ENABLE_CAMERA, map);
    }

    @Override
    public void onSoundLevelUpdate(HashMap<String, Float> soundLevels) {
        sendMessage(CallEvent.SOUND_LEVEL_UPDATE, soundLevels);
    }

    private void destroy() {
        if (mSessionLifeCycleListener != null) {
            mSessionLifeCycleListener.onSessionFinish(this);
        }
    }

    public void setCallId(String callId) {
        mCallId = callId;
    }

    public void setMultiCall(boolean multiCall) {
        mIsMultiCall = multiCall;
    }

    public void setMediaType(CallConst.CallMediaType mediaType) {
        mMediaType = mediaType;
    }

    public void setCallStatus(CallConst.CallStatus callStatus) {
        mCallStatus = callStatus;
    }

    public void setStartTime(long startTime) {
        mStartTime = startTime;
    }

    public void setConnectTime(long connectTime) {
        mConnectTime = connectTime;
    }

    public void setFinishTime(long finishTime) {
        mFinishTime = finishTime;
    }

    public void setOwner(String owner) {
        mOwner = owner;
    }

    public void setInviter(String inviter) {
        mInviter = inviter;
    }

    public void setFinishReason(CallConst.CallFinishReason finishReason) {
        mFinishReason = finishReason;
    }

    public void setEngineType(CallInternalConst.CallEngineType engineType) {
        mEngineType = engineType;
    }

    public void setCore(JIMCore core) {
        mCore = core;
    }

    public JIMCore getCore() {
        return mCore;
    }

    public void setSessionLifeCycleListener(ICallSessionLifeCycleListener sessionLifeCycleListener) {
        mSessionLifeCycleListener = sessionLifeCycleListener;
    }

    public String getZegoToken() {
        return mZegoToken;
    }

    public void setZegoToken(String zegoToken) {
        mZegoToken = zegoToken;
    }

    @Override
    public CallMember getCurrentCallMember() {
        CallMember current = new CallMember();
        UserInfo userInfo = JIM.getInstance().getUserInfoManager().getUserInfo(JIM.getInstance().getCurrentUserId());
        current.setUserInfo(userInfo);
        current.setCallStatus(mCallStatus);
        current.setStartTime(mStartTime);
        current.setConnectTime(mConnectTime);
        current.setFinishTime(mFinishTime);
        current.setInviter(JIM.getInstance().getUserInfoManager().getUserInfo(mInviter));
        return current;
    }

    private JIMCore mCore;
    private CallInternalConst.CallEngineType mEngineType;
    private ICallSessionLifeCycleListener mSessionLifeCycleListener;
    private ConcurrentHashMap<String, ICallSessionListener> mListeners;

    private String mZegoToken;
    private String mCallId;
    private boolean mIsMultiCall;
    private CallConst.CallMediaType mMediaType;
    private CallConst.CallStatus mCallStatus;
    private long mStartTime;
    private long mConnectTime;
    private long mFinishTime;
    private String mOwner;
    private String mInviter;
    private CallConst.CallFinishReason mFinishReason;
    private final List<CallMember> mMembers = new ArrayList<>();
    private final Map<String, View> mViewMap = new HashMap<>();

    private final CallSuperState mSuperState;
    private final CallIdleState mIdleState;
    private final CallConnectingState mConnectingState;
    private final CallIncomingState mIncomingState;
    private final CallOutgoingState mOutgoingState;
    private final CallConnectedState mConnectedState;
}
