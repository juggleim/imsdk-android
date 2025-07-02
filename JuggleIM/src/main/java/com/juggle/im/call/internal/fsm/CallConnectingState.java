package com.juggle.im.call.internal.fsm;

import android.os.Message;

import com.juggle.im.call.CallConst;
import com.juggle.im.call.internal.CallSessionImpl;
import com.juggle.im.internal.util.JLogger;
import com.juggle.im.call.internal.CallEvent;

public class CallConnectingState extends CallState {
    @Override
    public void enter() {
        super.enter();
        CallSessionImpl callSession = getCallSessionImpl();
        if (callSession != null) {
            callSession.setCallStatus(CallConst.CallStatus.CONNECTING);
            if (callSession.getMediaType() == CallConst.CallMediaType.VIDEO) {
                callSession.enableCamera(true);
            } else {
                callSession.enableCamera(false);
            }
            callSession.mediaJoin();
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
            case CallEvent.JOIN_CHANNEL_DONE:
                callSession.setConnectTime(System.currentTimeMillis());
                callSession.transitionToConnectedState();
                break;

            case CallEvent.JOIN_CHANNEL_FAIL:
                callSession.setFinishTime(System.currentTimeMillis());
                callSession.setFinishReason(CallConst.CallFinishReason.NETWORK_ERROR);
                callSession.error(CallConst.CallErrorCode.JOIN_MEDIA_ROOM_FAIL);
                callSession.transitionToIdleState();
                break;
            default:
                return false;
        }
        return true;

    }
}
