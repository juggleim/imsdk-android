package com.juggle.im.call.internal;

import android.os.Message;

import com.juggle.im.call.CallConst;
import com.juggle.im.call.internal.fsm.CallState;
import com.juggle.im.internal.util.JLogger;

public class CallJoinState extends CallState {

    @Override
    public void enter() {
        super.enter();
        CallSessionImpl callSession = getCallSessionImpl();
        if (callSession != null) {
            callSession.setCallStatus(CallConst.CallStatus.JOIN);
            callSession.signalJoin();
        }
    }

    @Override
    public boolean processMessage(Message msg) {
        super.processMessage(msg);

        CallSessionImpl callSession = getCallSessionImpl();
        if (callSession == null) {
            JLogger.e("FSM-Sm", "callSession is null");
            return true;
        }

        boolean result = true;
        switch (msg.what) {
            case CallEvent.JOIN_DONE:
                callSession.transitionToConnectingState();
                break;

            case CallEvent.JOIN_FAIL:
                callSession.error(CallConst.CallErrorCode.JOIN_ROOM_FAIL);
                callSession.transitionToIdleState();
                break;

            default:
                result = false;
                break;
        }
        return result;
    }
}
