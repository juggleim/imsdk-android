package com.juggle.im.call.internal.fsm;

import android.os.Message;

import com.juggle.im.call.CallConst;
import com.juggle.im.call.internal.CallEvent;
import com.juggle.im.call.internal.CallSessionImpl;
import com.juggle.im.internal.util.JLogger;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class CallConnectedState extends CallState {

    @Override
    public void enter() {
        super.enter();
        CallSessionImpl callSession = getCallSessionImpl();
        if (callSession != null) {
            callSession.setCallStatus(CallConst.CallStatus.CONNECTED);
            startPing();
        }
    }

    @Override
    public void exit() {
        super.exit();
        stopPing();
    }

    @Override
    public boolean processMessage(Message msg) {
        super.processMessage(msg);

        CallSessionImpl callSession = getCallSessionImpl();
        if (callSession == null) {
            JLogger.e("FSM-Sm", "callSession is null");
            return true;
        }

        List<String> userIdList;

        switch (msg.what) {
            case CallEvent.INVITE:
                userIdList = (List<String>) msg.obj;
                callSession.signalInvite(userIdList);
                break;

            case CallEvent.INVITE_DONE:
                userIdList = (List<String>) msg.obj;
                callSession.membersInviteBySelf(userIdList);
                break;

            case CallEvent.INVITE_FAIL:
                callSession.error(CallConst.CallErrorCode.INVITE_FAIL);
                break;

            default:
                return false;
        }

        return true;
    }

    private void startPing() {
        mPingTimer = new Timer();
        JLogger.i("Call-Ping", "start");
        mPingTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                CallSessionImpl callSession = getCallSessionImpl();
                if (callSession != null) {
                    callSession.ping();
                }

            }
        }, PING_INTERVAL, PING_INTERVAL);
    }

    private void stopPing() {
        JLogger.i("Call-Ping", "stop");
        if (mPingTimer != null) {
            mPingTimer.cancel();
            mPingTimer = null;
        }
    }

    private Timer mPingTimer;
    private static final int PING_INTERVAL = 5*1000;
}
