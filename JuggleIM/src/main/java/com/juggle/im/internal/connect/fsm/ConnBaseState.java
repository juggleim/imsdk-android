package com.juggle.im.internal.connect.fsm;

import android.os.Message;

import com.juggle.im.internal.connect.ConnectionManager;
import com.juggle.im.internal.util.JLogger;
import com.juggle.im.internal.util.statemachine.BaseState;

import java.lang.ref.WeakReference;

public class ConnBaseState extends BaseState {
    @Override
    public boolean processMessage(Message msg) {
        JLogger.i("FSM-Sm", "[" + getName() + "] processMessage : " + ConnEvent.nameOfEvent(msg.what));
        return super.processMessage(msg);
    }

    public ConnectionManager getConnectionManager() {
        return mConnectionManager.get();
    }

    public void setConnectionManager(ConnectionManager connectionManager) {
        mConnectionManager = new WeakReference<>(connectionManager);
    }

    protected WeakReference<ConnectionManager> mConnectionManager;
}
