package com.jet.im.push.xm;


import android.content.Context;

import com.jet.im.push.PushType;
import com.xiaomi.mipush.sdk.ErrorCode;
import com.xiaomi.mipush.sdk.MiPushClient;
import com.xiaomi.mipush.sdk.MiPushCommandMessage;
import com.xiaomi.mipush.sdk.MiPushMessage;
import com.xiaomi.mipush.sdk.PushMessageReceiver;

import java.util.List;

public class MiMessageReceiver extends PushMessageReceiver {
    @Override
    public void onNotificationMessageClicked(Context context, MiPushMessage message) {
        if (message == null) {
            return;
        }
    }

    @Override
    public void onReceiveRegisterResult(Context context, MiPushCommandMessage message) {
        if (message == null) {
            return;
        }
        String command = message.getCommand();
        List<String> arguments = message.getCommandArguments();
        String cmdArg1 = ((arguments != null && !arguments.isEmpty()) ? arguments.get(0) : null);
        String cmdArg2 = ((arguments != null && arguments.size() > 1) ? arguments.get(1) : null);
        if (MiPushClient.COMMAND_REGISTER.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                if (XMPush.sCallback != null) {
                    XMPush.sCallback.onReceivedToken(PushType.XIAOMI, cmdArg1);
                }
            } else {
                if (XMPush.sCallback != null) {
                    XMPush.sCallback.onError(PushType.XIAOMI, Long.valueOf(message.getResultCode()).intValue(), message.getReason());
                }
            }
        }
    }
}
