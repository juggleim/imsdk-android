package com.juggle.im.call.internal;

import android.os.Message;
import android.text.TextUtils;

import com.juggle.im.call.CallConst;
import com.juggle.im.call.ICallSession;
import com.juggle.im.call.internal.fsm.CallConnectedState;
import com.juggle.im.call.internal.fsm.CallConnectingState;
import com.juggle.im.call.internal.fsm.CallIdleState;
import com.juggle.im.call.internal.fsm.CallIncomingState;
import com.juggle.im.call.internal.fsm.CallOutgoingState;
import com.juggle.im.call.internal.fsm.CallSuperState;
import com.juggle.im.call.model.CallMember;
import com.juggle.im.internal.core.JIMCore;
import com.juggle.im.internal.core.network.CallAuthCallback;
import com.juggle.im.internal.core.network.WebSocketSimpleCallback;
import com.juggle.im.internal.util.JLogger;
import com.juggle.im.internal.util.statemachine.StateMachine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CallSessionImpl extends StateMachine implements ICallSession {
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
    public void muteMicrophone(boolean isMute) {

        //todo
    }

    @Override
    public void muteSpeaker(boolean isMute) {

        //todo
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

    public void signalSingleInvite() {
        List<String> targetIds = new ArrayList<>();
        for (CallMember member : mMembers) {
            if (member.getUserInfo() != null && member.getUserInfo().getUserId() != null) {
                targetIds.add(member.getUserInfo().getUserId());
            }
        }
        mCore.getWebSocket().callInvite(mCallId, false, targetIds, mEngineType.getValue(), new CallAuthCallback() {
            @Override
            public void onSuccess(String zegoToken) {
                JLogger.i("Call-Signal", "send invite success");
                mZegoToken = zegoToken;
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
        //todo
    }

    public void mediaJoin() {
        JLogger.i("Call-Media", "media join");
        //todo
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

    public void setSessionLifeCycleListener(ICallSessionLifeCycleListener sessionLifeCycleListener) {
        mSessionLifeCycleListener = sessionLifeCycleListener;
    }

    private JIMCore mCore;
    private CallInternalConst.CallEngineType mEngineType;
    private ICallSessionLifeCycleListener mSessionLifeCycleListener;
    private ConcurrentHashMap<String, ICallSessionListener> mListeners;

    private String mZegoToken;
    private String mCallId;
    private boolean mIsMultiCall;
    private CallConst.CallStatus mCallStatus;
    private long mStartTime;
    private long mConnectTime;
    private long mFinishTime;
    private String mOwner;
    private String mInviter;
    private CallConst.CallFinishReason mFinishReason;
    private List<CallMember> mMembers = new ArrayList<>();

    private CallSuperState mSuperState;
    private CallIdleState mIdleState;
    private CallConnectingState mConnectingState;
    private CallIncomingState mIncomingState;
    private CallOutgoingState mOutgoingState;
    private CallConnectedState mConnectedState;
}
