package com.jet.im.internal.core.network;

import com.jet.im.model.UserInfo;

import java.util.List;

public abstract class QryReadDetailCallback implements IWebSocketCallback {
    public abstract void onSuccess(List<UserInfo> readMembers, List<UserInfo> unreadMembers);
    public abstract void onError(int errorCode);
}
