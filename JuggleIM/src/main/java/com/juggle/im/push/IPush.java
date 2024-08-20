package com.juggle.im.push;

import android.content.Context;

public interface IPush {
    /**
     * 非主线程调用此方法
     */
    void getToken(Context context, PushConfig config, Callback callback);

    PushChannel getType();

    interface Callback {
        void onReceivedToken(PushChannel type, String token);

        void onError(PushChannel type, int code, String msg);
    }
}
