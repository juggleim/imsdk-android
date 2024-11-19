package com.juggle.im.call.internal.fsm;

import android.os.Message;

import com.juggle.im.call.CallConst;
import com.juggle.im.call.internal.CallEvent;
import com.juggle.im.call.internal.CallSessionImpl;
import com.juggle.im.call.model.CallMember;
import com.juggle.im.internal.util.JLogger;

import java.util.Timer;
import java.util.TimerTask;

public class CallOutgoingState extends CallState {
    @Override
    public void enter() {
        super.enter();
        CallSessionImpl callSession = getCallSessionImpl();
        if (callSession != null) {
            callSession.setCallStatus(CallConst.CallStatus.OUTGOING);
            startOutgoingTimer();
            callSession.signalSingleInvite();
        }
    }

    @Override
    public void exit() {
        super.exit();
        stopOutgoingTimer();
    }

    @Override
    public boolean processMessage(Message msg) {
        super.processMessage(msg);

        CallSessionImpl callSession = getCallSessionImpl();
        if (callSession == null) {
            JLogger.e("FSM-Sm", "callSession is null");
            return true;
        }

        switch (msg.what) {
            case CallEvent.INVITE_FAIL:
                inviteFail();
                callSession.transitionToIdleState();
                break;

            case CallEvent.INVITE_TIMEOUT:
                inviteTimeout();
                callSession.transitionToIdleState();
                break;

            case CallEvent.RECEIVE_ACCEPT:
                String userId = (String) msg.obj;
                memberAccept(userId);
                if (!callSession.isMultiCall()) {
                    callSession.transitionToConnectingState();
                }
                break;

            default:
                return false;
        }
        return true;
    }

    private void startOutgoingTimer() {
        JLogger.i("Call-Timer", "outgoing timer start");
        mOutgoingTimer = new Timer();
        mOutgoingTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                CallSessionImpl callSession = getCallSessionImpl();
                if (callSession != null) {
                    callSession.sendMessage(CallEvent.INVITE_TIMEOUT);
                }
            }
        }, OUTGOING_INTERVAL);
    }

    private void stopOutgoingTimer() {
        JLogger.i("Call-Timer", "outgoing timer stop");
        if (mOutgoingTimer != null) {
            mOutgoingTimer.cancel();
            mOutgoingTimer = null;
        }
    }

    private void inviteFail() {
        CallSessionImpl callSession = getCallSessionImpl();
        if (callSession == null) {
            return;
        }
        callSession.setFinishTime(System.currentTimeMillis());
        callSession.setFinishReason(CallConst.CallFinishReason.NETWORK_ERROR);
    }

    private void inviteTimeout() {
        CallSessionImpl callSession = getCallSessionImpl();
        if (callSession == null) {
            return;
        }
        callSession.setFinishTime(System.currentTimeMillis());
        callSession.setFinishReason(CallConst.CallFinishReason.OTHER_SIDE_NO_RESPONSE);
    }

    private void memberAccept(String userId) {
        CallSessionImpl callSession = getCallSessionImpl();
        if (callSession == null) {
            return;
        }
        if (!callSession.isMultiCall()) {
            if (callSession.getMembers() == null) {
                return;
            }
            for (CallMember member : callSession.getMembers()) {
                if (member.getUserInfo() != null
                && member.getUserInfo().getUserId() != null
                && member.getUserInfo().getUserId().equals(userId)) {
                    member.setCallStatus(CallConst.CallStatus.CONNECTING);
                }
            }
        } else {

        }
    }

    private Timer mOutgoingTimer;
    private static final int OUTGOING_INTERVAL = 60;
}
