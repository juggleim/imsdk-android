package com.juggle.im.call.internal.media;

import android.view.View;

import java.util.HashMap;
import java.util.List;

public interface ICallMediaListener {
    View viewForUserId(String userId);
    View viewForSelf();
    void onUsersConnect(List<String> userIdList);
    void onUserCameraChange(String userId, boolean enable);
    void onUserMicStateUpdate(String userId, boolean enable);
    void onSoundLevelUpdate(HashMap<String, Float> soundLevels);
    void onVideoFirstFrameRender(String userId);
}
