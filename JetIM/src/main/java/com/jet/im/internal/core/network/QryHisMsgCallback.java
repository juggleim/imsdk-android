package com.jet.im.internal.core.network;

import com.jet.im.internal.model.ConcreteMessage;
import com.jet.im.model.Message;

import java.util.List;

public abstract class QryHisMsgCallback implements IWebSocketCallback {
    public abstract void onSuccess(List<ConcreteMessage> messages, boolean isFinished);
    public abstract void onError(int errorCode);
}
