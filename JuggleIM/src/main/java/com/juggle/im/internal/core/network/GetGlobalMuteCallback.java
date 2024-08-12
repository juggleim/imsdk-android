package com.juggle.im.internal.core.network;

import com.juggle.im.model.TimePeriod;

import java.util.List;

public abstract class GetGlobalMuteCallback implements IWebSocketCallback {
    public abstract void onSuccess(boolean isMute, String timezone, List<TimePeriod> periods);
    public abstract void onError(int errorCode);
}
