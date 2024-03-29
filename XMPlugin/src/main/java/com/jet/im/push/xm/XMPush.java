package com.jet.im.push.xm;

import android.content.Context;

import com.jet.im.push.IPush;
import com.jet.im.push.PushConfig;
import com.jet.im.push.PushType;
import com.xiaomi.mipush.sdk.MiPushClient;

public class XMPush implements IPush {
    static IPush.Callback sCallback;

    @Override
    public void getToken(Context context, PushConfig config, Callback callback) {
        sCallback = callback;
        if (config.getXmConfig() != null) {
            MiPushClient.registerPush(context, config.getXmConfig().getAppId(), config.getXmConfig().getAppKey());
        }

    }

    @Override
    public PushType getType() {
        return PushType.XIAOMI;
    }
}
