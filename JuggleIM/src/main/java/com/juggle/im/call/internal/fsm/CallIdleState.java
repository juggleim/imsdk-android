package com.juggle.im.call.internal.fsm;

import android.os.Message;

import com.juggle.im.call.CallConst;
import com.juggle.im.call.internal.CallEvent;
import com.juggle.im.call.internal.CallSessionImpl;
import com.juggle.im.internal.util.JLogger;

public class CallIdleState extends CallState {
    @Override
    public void enter() {
        super.enter();
        CallSessionImpl callSession = getCallSessionImpl();
        if (callSession != null) {
            callSession.setCallStatus(CallConst.CallStatus.IDLE);
        }
    }

    @Override
    public void exit() {
        super.exit();
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
            case CallEvent.INVITE:
                callSession.transitionToOutgoingState();
                break;

            case CallEvent.RECEIVE_INVITE:
                callSession.transitionToIncomingState();
                break;

            default:
                return false;
        }

        return true;
    }
}
