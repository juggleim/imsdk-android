package com.juggle.im.internal.connect.fsm;

import com.juggle.im.internal.connect.ConnectionManager;
import com.juggle.im.internal.core.JIMCore;

public class ConnIdleState extends ConnBaseState {
    @Override
    public void enter() {
        super.enter();
        ConnectionManager manager = getConnectionManager();
        if (manager != null) {
            manager.setConnectionStatus(JIMCore.ConnectionStatusInternal.IDLE);
        }
    }
}
