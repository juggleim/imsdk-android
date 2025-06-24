package com.juggle.im.call.internal.media;

import android.view.View;

import java.util.List;

public interface ICallMediaEngine {

    interface ICallMediaEngineListener {
        View viewForUserId(String userId);
        void onUsersJoin(List<String> userIdList);
        void onUserCameraChange(String userId, boolean enable);
        void onUsersLeave(List<String> userIdList);
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

    void setListener(ICallMediaEngineListener listener);
}
