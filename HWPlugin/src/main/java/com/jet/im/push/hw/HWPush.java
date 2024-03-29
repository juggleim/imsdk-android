package com.jet.im.push.hw;

import android.content.Context;
import android.text.TextUtils;

import com.huawei.agconnect.config.AGConnectServicesConfig;
import com.huawei.hms.aaid.HmsInstanceId;
import com.huawei.hms.common.ApiException;
import com.jet.im.push.IPush;
import com.jet.im.push.PushConfig;
import com.jet.im.push.PushType;

public class HWPush implements IPush {
    static IPush.Callback sCallback;

    @Override
    public void getToken(Context context, PushConfig config, IPush.Callback callback) {
        sCallback = callback;
        try {
            String appId = AGConnectServicesConfig.fromContext(context).getString("client/app_id");
            String token = HmsInstanceId.getInstance(context).getToken(appId, "HCM");
            if (!TextUtils.isEmpty(token)) {
                callback.onReceivedToken(getType(), token);
            }
        } catch (Throwable e) {
            int errorCode = e instanceof ApiException ? ((ApiException) e).getStatusCode() : -1;
            callback.onError(getType(), errorCode, e.getMessage());
        }
    }

    @Override
    public PushType getType() {
        return PushType.HUAWEI;
    }
}
