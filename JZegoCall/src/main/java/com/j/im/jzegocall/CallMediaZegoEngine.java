package com.j.im.jzegocall;

import android.app.Application;
import android.content.Context;

import com.juggle.im.call.internal.media.CallMediaRoom;
import com.juggle.im.call.internal.media.CallMediaRoomConfig;
import com.juggle.im.call.internal.media.CallMediaUser;
import com.juggle.im.call.internal.media.ICallCompleteCallback;
import com.juggle.im.call.internal.media.ICallMediaEngine;

import org.json.JSONObject;

import java.util.ArrayList;

import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.callback.IZegoEventHandler;
import im.zego.zegoexpress.callback.IZegoRoomLoginCallback;
import im.zego.zegoexpress.constants.ZegoPlayerState;
import im.zego.zegoexpress.constants.ZegoScenario;
import im.zego.zegoexpress.constants.ZegoUpdateType;
import im.zego.zegoexpress.entity.ZegoEngineProfile;
import im.zego.zegoexpress.entity.ZegoRoomConfig;
import im.zego.zegoexpress.entity.ZegoStream;
import im.zego.zegoexpress.entity.ZegoUser;

public class CallMediaZegoEngine extends IZegoEventHandler implements ICallMediaEngine {
    public CallMediaZegoEngine(int appId, Context context) {
        ZegoEngineProfile profile = new ZegoEngineProfile();
        profile.appID = appId;
        profile.scenario = ZegoScenario.STANDARD_VOICE_CALL;
        profile.application = (Application) context.getApplicationContext();
        mEngine = ZegoExpressEngine.createEngine(profile, this);
        mEngine.enableCamera(false);
    }

    @Override
    public void joinRoom(CallMediaRoom room, CallMediaUser user, CallMediaRoomConfig config, ICallCompleteCallback callback) {
        if (room == null || room.getRoomId() == null || room.getRoomId().isEmpty()
        || user == null || user.getUserId() == null || user.getUserId().isEmpty()
        || config == null) {
            return;
        }

        ZegoUser zegoUser = new ZegoUser(user.getUserId());
        ZegoRoomConfig zegoRoomConfig = new ZegoRoomConfig();
        zegoRoomConfig.isUserStatusNotify = config.isUserStatusNotify();
        zegoRoomConfig.token = config.getZegoToken();
        mEngine.loginRoom(room.getRoomId(), zegoUser, zegoRoomConfig, new IZegoRoomLoginCallback() {
            @Override
            public void onRoomLoginResult(int errorCode, JSONObject extendedData) {
                if (errorCode == 0) {
                    String streamId = room.getRoomId() + "+++" + user.getUserId();
                    mEngine.startPublishingStream(streamId);
                }
                if (callback != null) {
                    callback.onComplete(errorCode, extendedData);
                }
            }
        });
    }

    @Override
    public void leaveRoom(String roomId) {
        mEngine.logoutRoom();
    }

    @Override
    public void muteMicrophone(boolean isMute) {
        mEngine.muteMicrophone(isMute);
    }

    @Override
    public void muteSpeaker(boolean isMute) {
        mEngine.muteSpeaker(isMute);
    }

    @Override
    public void setSpeakerEnable(boolean isEnable) {
        mEngine.setAudioRouteToSpeaker(isEnable);
    }

    @Override
    public void onRoomStreamUpdate(String roomID, ZegoUpdateType updateType, ArrayList<ZegoStream> streamList, JSONObject extendedData) {
        super.onRoomStreamUpdate(roomID, updateType, streamList, extendedData);
        if (updateType == ZegoUpdateType.ADD) {
            for (ZegoStream stream : streamList) {
                String streamId = stream.streamID;
                mEngine.startPlayingStream(streamId);
            }
        }
    }

    @Override
    public void onPlayerStateUpdate(String streamID, ZegoPlayerState state, int errorCode, JSONObject extendedData) {
        super.onPlayerStateUpdate(streamID, state, errorCode, extendedData);
    }

    @Override
    public void onDebugError(int errorCode, String funcName, String info) {
        super.onDebugError(errorCode, funcName, info);
    }

    private static ZegoExpressEngine mEngine;
}
