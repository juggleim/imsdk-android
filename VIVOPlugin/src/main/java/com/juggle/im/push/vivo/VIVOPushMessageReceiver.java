package com.juggle.im.push.vivo;

import android.content.Context;

import com.juggle.im.push.PushChannel;
import com.vivo.push.model.UPSNotificationMessage;
import com.vivo.push.sdk.OpenClientPushMessageReceiver;

public class VIVOPushMessageReceiver extends OpenClientPushMessageReceiver {

    @Override
    public void onNotificationMessageClicked(Context context, UPSNotificationMessage msg) {
    }

    @Override
    public void onReceiveRegId(Context context, String regId) {
        if (VIVOPush.sCallback != null) {
            VIVOPush.sCallback.onReceivedToken(PushChannel.VIVO, regId);
        }
    }
}
