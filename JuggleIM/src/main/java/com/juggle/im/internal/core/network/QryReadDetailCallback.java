package com.juggle.im.internal.core.network;

import com.juggle.im.model.UserInfo;

import java.util.List;

public abstract class QryReadDetailCallback implements IWebSocketCallback {
    public abstract void onSuccess(List<UserInfo> readMembers, List<UserInfo> unreadMembers);
    public abstract void onError(int errorCode);
}
