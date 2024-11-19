package com.juggle.im.call.internal;

public interface ICallSessionLifeCycleListener {
    void onSessionFinish(CallSessionImpl session);
    void onCallReceive(CallSessionImpl session);
    boolean onCallAccept(CallSessionImpl session);
}
