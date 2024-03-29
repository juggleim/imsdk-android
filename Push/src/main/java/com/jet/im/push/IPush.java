package com.jet.im.push;

import android.content.Context;

public interface IPush {
    /**
     * 非主线程调用此方法
     */
    void getToken(Context context, PushConfig config, Callback callback);

    PushType getType();

    interface Callback {
        void onReceivedToken(PushType type,String token);

        void onError(PushType type,int code, String msg);
    }
}
