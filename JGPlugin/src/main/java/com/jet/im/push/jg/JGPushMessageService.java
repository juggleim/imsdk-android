package com.jet.im.push.jg;

import android.content.Context;
import android.content.Intent;

import com.jet.im.push.PushChannel;

import cn.jpush.android.api.CmdMessage;
import cn.jpush.android.api.CustomMessage;
import cn.jpush.android.api.JPushMessage;
import cn.jpush.android.api.NotificationMessage;
import cn.jpush.android.service.JPushMessageService;

public class JGPushMessageService extends JPushMessageService {
    private static final String TAG = "Push-JGPush";

    @Override
    public void onMessage(Context context, CustomMessage customMessage) {
    }

    @Override
    public void onNotifyMessageOpened(Context context, NotificationMessage message) {
    }

    @Override
    public void onMultiActionClicked(Context context, Intent intent) {
    }

    @Override
    public void onNotifyMessageArrived(Context context, NotificationMessage message) {
    }

    @Override
    public void onNotifyMessageDismiss(Context context, NotificationMessage message) {
    }

    @Override
    public void onRegister(Context context, String registrationId) {
        if (JGPush.sCallback != null) {
            JGPush.sCallback.onReceivedToken(PushChannel.JIGUANG, registrationId);
        }
    }

    @Override
    public void onConnected(Context context, boolean isConnected) {
    }

    @Override
    public void onCommandResult(Context context, CmdMessage cmdMessage) {
    }

    @Override
    public void onTagOperatorResult(Context context, JPushMessage jPushMessage) {
        super.onTagOperatorResult(context, jPushMessage);
    }

    @Override
    public void onCheckTagOperatorResult(Context context, JPushMessage jPushMessage) {
        super.onCheckTagOperatorResult(context, jPushMessage);
    }

    @Override
    public void onAliasOperatorResult(Context context, JPushMessage jPushMessage) {
        super.onAliasOperatorResult(context, jPushMessage);
    }

    @Override
    public void onMobileNumberOperatorResult(Context context, JPushMessage jPushMessage) {
        super.onMobileNumberOperatorResult(context, jPushMessage);
    }

    @Override
    public void onNotificationSettingsCheck(Context context, boolean isOn, int source) {
        super.onNotificationSettingsCheck(context, isOn, source);
    }
}
