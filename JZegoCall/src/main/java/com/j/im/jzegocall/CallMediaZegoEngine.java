package com.j.im.jzegocall;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import com.juggle.im.call.internal.media.CallMediaRoom;
import com.juggle.im.call.internal.media.CallMediaRoomConfig;
import com.juggle.im.call.internal.media.CallMediaUser;
import com.juggle.im.call.internal.media.ICallCompleteCallback;
import com.juggle.im.call.internal.media.ICallMediaEngine;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.callback.IZegoEventHandler;
import im.zego.zegoexpress.callback.IZegoRoomLoginCallback;
import im.zego.zegoexpress.constants.ZegoEngineState;
import im.zego.zegoexpress.constants.ZegoPlayerState;
import im.zego.zegoexpress.constants.ZegoPublisherState;
import im.zego.zegoexpress.constants.ZegoRemoteDeviceState;
import im.zego.zegoexpress.constants.ZegoRoomState;
import im.zego.zegoexpress.constants.ZegoRoomStateChangedReason;
import im.zego.zegoexpress.constants.ZegoScenario;
import im.zego.zegoexpress.constants.ZegoStreamQualityLevel;
import im.zego.zegoexpress.constants.ZegoUpdateType;
import im.zego.zegoexpress.entity.ZegoBroadcastMessageInfo;
import im.zego.zegoexpress.entity.ZegoCanvas;
import im.zego.zegoexpress.entity.ZegoEngineProfile;
import im.zego.zegoexpress.entity.ZegoRoomConfig;
import im.zego.zegoexpress.entity.ZegoRoomExtraInfo;
import im.zego.zegoexpress.entity.ZegoStream;
import im.zego.zegoexpress.entity.ZegoUser;

public class CallMediaZegoEngine extends IZegoEventHandler implements ICallMediaEngine {
    public CallMediaZegoEngine(int appId, Context context) {
        ZegoEngineProfile profile = new ZegoEngineProfile();
        profile.appID = appId;
        profile.scenario = ZegoScenario.STANDARD_VOICE_CALL;
        profile.application = (Application) context.getApplicationContext();
        sEngine = ZegoExpressEngine.createEngine(profile, this);
        sEngine.enableCamera(false);
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
        sEngine.loginRoom(room.getRoomId(), zegoUser, zegoRoomConfig, new IZegoRoomLoginCallback() {
            @Override
            public void onRoomLoginResult(int errorCode, JSONObject extendedData) {
                if (errorCode == 0) {
                    String streamId = createStreamId(room.getRoomId(), user.getUserId());
                    sEngine.startPublishingStream(streamId);
                }
                if (callback != null) {
                    callback.onComplete(errorCode, extendedData);
                }
            }
        });
    }

    @Override
    public void leaveRoom(String roomId) {
        sEngine.logoutRoom();
    }

    @Override
    public void startPreview(View view) {
        sEngine.startPreview(new ZegoCanvas(view));
    }

    @Override
    public void stopPreview() {
        sEngine.stopPreview();
    }

    @Override
    public void enableCamera(boolean isEnable) {
        sEngine.enableCamera(isEnable);
    }

    @Override
    public void useFrontCamera(boolean isEnable) {
        sEngine.useFrontCamera(isEnable);
    }

    @Override
    public void setListener(ICallMediaEngineListener listener) {
        mListener = listener;
    }

    @Override
    public void setVideoView(String roomId, String userId, View view) {
        if (TextUtils.isEmpty(roomId) || TextUtils.isEmpty(userId)) {
            return;
        }
        String streamId = createStreamId(roomId, userId);
        sEngine.startPlayingStream(streamId, new ZegoCanvas(view));
    }

    @Override
    public void muteMicrophone(boolean isMute) {
        sEngine.muteMicrophone(isMute);
    }

    @Override
    public void muteSpeaker(boolean isMute) {
        sEngine.muteSpeaker(isMute);
    }

    @Override
    public void setSpeakerEnable(boolean isEnable) {
        sEngine.setAudioRouteToSpeaker(isEnable);
    }

    @Override
    public void onRoomStreamUpdate(String roomID, ZegoUpdateType updateType, ArrayList<ZegoStream> streamList, JSONObject extendedData) {
        if (updateType == ZegoUpdateType.ADD) {
            for (ZegoStream stream : streamList) {
                String streamId = stream.streamID;
                sEngine.startPlayingStream(streamId);
            }
        }
        if (sHandler != null) {
            sHandler.onRoomStreamUpdate(roomID, updateType, streamList, extendedData);
        }
    }

    @Override
    public void onRemoteCameraStateUpdate(String streamID, ZegoRemoteDeviceState state) {
        if (mListener == null) {
            return;
        }
        String userId = userIdWithStreamId(streamID);
        mListener.onUserCameraChange(userId, state == ZegoRemoteDeviceState.OPEN);
    }

    @Override
    public void onPlayerStateUpdate(String streamID, ZegoPlayerState state, int errorCode, JSONObject extendedData) {
        if (sHandler != null) {
            sHandler.onPlayerStateUpdate(streamID, state, errorCode, extendedData);
        }
    }

    @Override
    public void onDebugError(int errorCode, String funcName, String info) {
        super.onDebugError(errorCode, funcName, info);
    }

    @Override
    public void onEngineStateUpdate(ZegoEngineState state) {
        if (sHandler != null) {
            sHandler.onEngineStateUpdate(state);
        }
    }

    @Override
    public void onIMRecvCustomCommand(String roomID, ZegoUser fromUser, String command) {
        if (sHandler != null) {
            sHandler.onIMRecvCustomCommand(roomID, fromUser, command);
        }
    }

    @Override
    public void onPublisherStateUpdate(String streamID, ZegoPublisherState state, int errorCode, JSONObject extendedData) {
        if (sHandler != null) {
            sHandler.onPublisherStateUpdate(streamID, state, errorCode, extendedData);
        }
    }

    @Override
    public void onCapturedSoundLevelUpdate(float soundLevel) {
        if (sHandler != null) {
            sHandler.onCapturedSoundLevelUpdate(soundLevel);
        }
    }

    @Override
    public void onRemoteSoundLevelUpdate(HashMap<String, Float> soundLevels) {
        if (sHandler != null) {
            sHandler.onRemoteSoundLevelUpdate(soundLevels);
        }
    }

    @Override
    public void onRoomStateChanged(String roomID, ZegoRoomStateChangedReason reason, int errorCode, JSONObject extendedData) {
        if (sHandler != null) {
            sHandler.onRoomStateChanged(roomID, reason, errorCode, extendedData);
        }
    }

    @Override
    public void onRoomStateUpdate(String roomID, ZegoRoomState state, int errorCode, JSONObject extendedData) {
        if (sHandler != null) {
            sHandler.onRoomStateUpdate(roomID, state, errorCode, extendedData);
        }
    }

    @Override
    public void onRoomUserUpdate(String roomID, ZegoUpdateType updateType, ArrayList<ZegoUser> userList) {
        if (updateType == ZegoUpdateType.ADD) {
            List<String> userIdList = new ArrayList<>();
            for (ZegoUser zegoUser : userList) {
                userIdList.add(zegoUser.userID);
            }
            if (mListener != null) {
                mListener.onUsersJoin(userIdList);
            }
        } else if (updateType == ZegoUpdateType.DELETE) {
            // 暂不处理
        }
        if (sHandler != null) {
            sHandler.onRoomUserUpdate(roomID, updateType, userList);
        }
    }

    @Override
    public void onRoomOnlineUserCountUpdate(String roomID, int count) {
        if (sHandler != null) {
            sHandler.onRoomOnlineUserCountUpdate(roomID, count);
        }
    }

    @Override
    public void onRoomExtraInfoUpdate(String roomID, ArrayList<ZegoRoomExtraInfo> roomExtraInfoList) {
        if (sHandler != null) {
            sHandler.onRoomExtraInfoUpdate(roomID, roomExtraInfoList);
        }
    }

    @Override
    public void onNetworkQuality(String userID, ZegoStreamQualityLevel upstreamQuality, ZegoStreamQualityLevel downstreamQuality) {
        if (sHandler != null) {
            sHandler.onNetworkQuality(userID, upstreamQuality, downstreamQuality);
        }
    }

    @Override
    public void onIMRecvBroadcastMessage(String roomID, ArrayList<ZegoBroadcastMessageInfo> messageList) {
        if (sHandler != null) {
            sHandler.onIMRecvBroadcastMessage(roomID, messageList);
        }
    }

    private String createStreamId(String roomId, String userId) {
        return roomId + SEPARATOR + userId;
    }

    private String userIdWithStreamId(String streamId) {
        String[] parts = streamId.split(SEPARATOR);
        if (parts.length > 1) {
            return parts[1];
        }
        return "";
    }

    public static void setEventHandler(IZegoEventHandler handler) {
        sHandler = handler;
    }

    private static ZegoExpressEngine sEngine;
    private static IZegoEventHandler sHandler;
    private static final String SEPARATOR = "+++";
    private ICallMediaEngineListener mListener;
}
