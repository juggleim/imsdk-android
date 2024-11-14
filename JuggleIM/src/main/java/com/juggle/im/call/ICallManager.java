package com.juggle.im.call;

public interface ICallManager {
    interface ICallReceiveListener {
        void onCallReceive(ICallSession callSession);
    }

    void initZegoEngine(int appId);
    ICallSession startSingleCall(String userId);
    void addReceiveListener(String key, ICallReceiveListener listener);
    void removeReceiveListener(String key);
}
