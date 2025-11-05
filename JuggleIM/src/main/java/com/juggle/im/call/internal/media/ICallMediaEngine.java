package com.juggle.im.call.internal.media;

import android.view.View;

import com.juggle.im.call.model.CallVideoDenoiseParams;

import java.util.HashMap;
import java.util.List;

public interface ICallMediaEngine {

    interface ICallMediaEngineListener {
        View viewForUserId(String userId);
        View viewForSelf();
        void onUsersConnect(List<String> userIdList);
        void onUserCameraChange(String userId, boolean enable);
        void onUserMicStateUpdate(String userId, boolean enable);
        void onUsersLeave(List<String> userIdList);
        void onSoundLevelUpdate(HashMap<String, Float> soundLevels);
        void onVideoFirstFrameRender(String userId);
    }

    void joinRoom(CallMediaRoom room, CallMediaUser user, CallMediaRoomConfig config, ICallCompleteCallback callback);

    void leaveRoom(String roomId);

    void enableCamera(boolean isEnable);

    void startPreview(View view);

    void stopPreview();

    void setVideoView(String roomId, String userId, View view);

    void muteMicrophone(boolean isMute);

    void muteSpeaker(boolean isMute);

    void setSpeakerEnable(boolean isEnable);

    void useFrontCamera(boolean isEnable);

    void enableAEC(boolean isEnable);

    void setVideoDenoiseParams(CallVideoDenoiseParams params);

    void setListener(ICallMediaEngineListener listener);
}
