package com.juggle.im.push;

import android.content.Context;

public interface IPush {
    /**
     * Call this method from a non-main thread.
     */
    void getToken(Context context, PushConfig config, Callback callback);

    PushChannel getType();

    interface Callback {
        void onReceivedToken(PushChannel type, String token);

        void onError(PushChannel type, int code, String msg);
    }
}
