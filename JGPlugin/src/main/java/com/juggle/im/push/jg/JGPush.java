package com.juggle.im.push.jg;

import android.content.Context;
import android.text.TextUtils;

import com.juggle.im.push.IPush;
import com.juggle.im.push.PushChannel;
import com.juggle.im.push.PushConfig;

import cn.jiguang.api.utils.JCollectionAuth;
import cn.jpush.android.api.JPushInterface;

public class JGPush implements IPush {
    static IPush.Callback sCallback;

    @Override
    public void getToken(Context context, PushConfig config, IPush.Callback callback) {
        sCallback = callback;
        if (config.getJGConfig() == null) {
            return;
        }
        try {
            JPushInterface.init(context);
            JCollectionAuth.setAuth(context, true);
            String token = JPushInterface.getRegistrationID(context);
            if (!TextUtils.isEmpty(token)) {
                callback.onReceivedToken(getType(), token);
            }
        } catch (Throwable e) {
            callback.onError(getType(), -1, e.getMessage());
        }
    }

    @Override
    public PushChannel getType() {
        return PushChannel.JIGUANG;
    }
}
