package com.jet.im.internal.core.network;

public abstract class SendMessageCallback implements IWebSocketCallback {
    public SendMessageCallback(long clientMsgNo) {
        this.mClientMsgNo = clientMsgNo;
    }
    public abstract void onSuccess(long clientMsgNo, String msgId, long timestamp, long msgIndex);
    public abstract void onError(int errorCode, long clientMsgNo);

    public long getClientMsgNo() {
        return mClientMsgNo;
    }

    private final long mClientMsgNo;
}
