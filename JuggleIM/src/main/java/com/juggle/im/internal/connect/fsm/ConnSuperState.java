package com.juggle.im.internal.connect.fsm;

import android.os.Message;

public class ConnSuperState extends ConnBaseState {

    @Override
    public boolean processMessage(Message msg) {
        super.processMessage(msg);

        switch (msg.what) {
            case ConnEvent.USER_CONNECT:
                // Handled by each state
                break;

            case ConnEvent.USER_DISCONNECT:
                // Handled by each state
                break;

            case ConnEvent.CONNECT_DONE:
                // do nothing
                // Handled by the connecting state
                // Ignored in other states
                break;

            case ConnEvent.NETWORK_AVAILABLE:
                // do nothing
                // Handled by the waiting and connecting states
                // Ignored in other states; the connected state triggers websocketFail automatically
                break;
        }
        return true;
    }
}
