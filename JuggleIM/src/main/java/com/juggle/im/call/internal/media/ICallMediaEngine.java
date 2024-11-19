package com.juggle.im.call.internal.media;

public interface ICallMediaEngine {

    void joinRoom(CallMediaRoom room, CallMediaUser user, CallMediaRoomConfig config, ICallCompleteCallback callback);

    void leaveRoom(String roomId);

    void muteMicrophone(boolean isMute);

    void muteSpeaker(boolean isMute);
}
