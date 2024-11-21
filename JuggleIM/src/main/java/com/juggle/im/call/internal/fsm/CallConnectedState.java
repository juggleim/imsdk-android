package com.juggle.im.call.internal.fsm;

import android.os.Message;

import com.juggle.im.call.CallConst;
import com.juggle.im.call.internal.CallSessionImpl;
import com.juggle.im.internal.util.JLogger;

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
        return super.processMessage(msg);
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
