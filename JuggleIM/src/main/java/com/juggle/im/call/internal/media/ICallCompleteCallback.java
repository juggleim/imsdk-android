package com.juggle.im.call.internal.media;

import org.json.JSONObject;

public interface ICallCompleteCallback {
    void onComplete(int errorCode, JSONObject data);
}
