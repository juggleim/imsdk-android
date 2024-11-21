package com.juggle.im.internal.core.network;

import com.juggle.im.call.internal.model.RtcRoom;

import java.util.List;

public abstract class RtcRoomListCallback implements IWebSocketCallback {
    public abstract void onSuccess(List<RtcRoom> rooms);
    public abstract void onError(int errorCode);
}
