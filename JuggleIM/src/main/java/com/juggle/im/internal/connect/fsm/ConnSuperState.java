package com.juggle.im.internal.connect.fsm;

import android.os.Message;

public class ConnSuperState extends ConnBaseState {

    @Override
    public boolean processMessage(Message msg) {
        super.processMessage(msg);

        switch (msg.what) {
            case ConnEvent.USER_CONNECT:
                //各状态自行处理
                break;

            case ConnEvent.USER_DISCONNECT:
                //各状态自行处理
                break;

            case ConnEvent.CONNECT_DONE:
                // do nothing
                // connecting 状态处理
                // 其它状态下忽略
                break;

            case ConnEvent.NETWORK_AVAILABLE:
                // do nothing
                // waiting 和 connecting 状态处理
                // 其它状态忽略（connected 状态会自动触发 websocketFail）
                break;
        }
        return true;
    }
}
