package com.juggle.im.call.internal.fsm;

import android.os.Message;

import com.juggle.im.call.CallConst;
import com.juggle.im.call.internal.CallEvent;
import com.juggle.im.call.internal.CallSessionImpl;
import com.juggle.im.internal.util.JLogger;

import java.util.Timer;
import java.util.TimerTask;

public class CallIncomingState extends CallState {

    @Override
    public void enter() {
        super.enter();
        CallSessionImpl callSession = getCallSessionImpl();
        if (callSession != null) {
            callSession.setCallStatus(CallConst.CallStatus.INCOMING);
            startIncomingTimer();
            callSession.notifyReceiveCall();
        }
    }

    @Override
    public void exit() {
        super.exit();
        stopIncomingTimer();
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
            case CallEvent.ACCEPT:
                boolean needHangupOther = callSession.notifyAcceptCall();
                if (needHangupOther) {
                    startHangupTimer();
                } else {
                    callSession.signalAccept();
                }
                break;

            case CallEvent.INCOMING_TIMEOUT:
                incomingTimeout();
                callSession.transitionToIdleState();
                break;

            case CallEvent.ACCEPT_AFTER_HANGUP_OTHER:
                callSession.signalAccept();
                break;

            case CallEvent.ACCEPT_DONE:
                callSession.transitionToConnectingState();
                break;

            case CallEvent.ACCEPT_FAIL:
                callSession.error(CallConst.CallErrorCode.ACCEPT_FAIL);
                callSession.transitionToIdleState();
                break;

            default:
                return false;
        }
        return true;
    }

    private void startHangupTimer() {
        Timer hangupTimer = new Timer();
        hangupTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                JLogger.i("Call-Accept", "hangupTimerFire");
                CallSessionImpl callSession = getCallSessionImpl();
                if (callSession != null) {
                    callSession.sendMessage(CallEvent.ACCEPT_AFTER_HANGUP_OTHER);
                }

            }
        }, HANGUP_INTERVAL);
    }

    private void startIncomingTimer() {
        JLogger.i("Call-Timer", "incoming timer start");
        mIncomingTimer = new Timer();
        mIncomingTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                CallSessionImpl callSession = getCallSessionImpl();
                if (callSession != null) {
                    callSession.sendMessage(CallEvent.INCOMING_TIMEOUT);
                }
            }
        }, INCOMING_INTERVAL);
    }

    private void stopIncomingTimer() {
        JLogger.i("Call-Timer", "incoming timer stop");
        if (mIncomingTimer != null) {
            mIncomingTimer.cancel();
            mIncomingTimer = null;
        }
    }

    private void incomingTimeout() {
        CallSessionImpl callSession = getCallSessionImpl();
        if (callSession != null) {
            callSession.setFinishTime(System.currentTimeMillis());
            callSession.setFinishReason(CallConst.CallFinishReason.NO_RESPONSE);
        }
    }

    private Timer mIncomingTimer;
    private static final int HANGUP_INTERVAL = 500;
    private static final int INCOMING_INTERVAL = 60*1000;
}
