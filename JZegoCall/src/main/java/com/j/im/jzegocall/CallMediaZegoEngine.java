package com.j.im.jzegocall;

import static im.zego.zegoexpress.constants.ZegoViewMode.ASPECT_FILL;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import com.juggle.im.JIM;
import com.juggle.im.call.internal.media.CallMediaRoom;
import com.juggle.im.call.internal.media.CallMediaRoomConfig;
import com.juggle.im.call.internal.media.CallMediaUser;
import com.juggle.im.call.internal.media.ICallCompleteCallback;
import com.juggle.im.call.internal.media.ICallMediaEngine;
import com.juggle.im.call.model.CallVideoDenoiseParams;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.callback.IZegoEventHandler;
import im.zego.zegoexpress.callback.IZegoRoomLoginCallback;
import im.zego.zegoexpress.constants.ZegoEngineState;
import im.zego.zegoexpress.constants.ZegoPlayerState;
import im.zego.zegoexpress.constants.ZegoPublishChannel;
import im.zego.zegoexpress.constants.ZegoPublisherState;
import im.zego.zegoexpress.constants.ZegoRemoteDeviceState;
import im.zego.zegoexpress.constants.ZegoRoomState;
import im.zego.zegoexpress.constants.ZegoRoomStateChangedReason;
import im.zego.zegoexpress.constants.ZegoScenario;
import im.zego.zegoexpress.constants.ZegoStreamQualityLevel;
import im.zego.zegoexpress.constants.ZegoUpdateType;
import im.zego.zegoexpress.constants.ZegoVideoDenoiseMode;
import im.zego.zegoexpress.constants.ZegoVideoDenoiseStrength;
import im.zego.zegoexpress.entity.ZegoBroadcastMessageInfo;
import im.zego.zegoexpress.entity.ZegoCanvas;
import im.zego.zegoexpress.entity.ZegoEngineProfile;
import im.zego.zegoexpress.entity.ZegoRoomConfig;
import im.zego.zegoexpress.entity.ZegoRoomExtraInfo;
import im.zego.zegoexpress.entity.ZegoStream;
import im.zego.zegoexpress.entity.ZegoUser;
import im.zego.zegoexpress.entity.ZegoVideoDenoiseParams;

public class CallMediaZegoEngine extends IZegoEventHandler implements ICallMediaEngine {
    public CallMediaZegoEngine(int appId, Context context) {
        ZegoEngineProfile profile = new ZegoEngineProfile();
        profile.appID = appId;
        profile.scenario = ZegoScenario.STANDARD_VIDEO_CALL;
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
        sEngine.startPreview(createCanvasWithView(view));
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
    public void enableAEC(boolean isEnable) {
        sEngine.enableAEC(isEnable);
    }

    @Override
    public void setVideoDenoiseParams(CallVideoDenoiseParams params) {
        ZegoVideoDenoiseParams zp = new ZegoVideoDenoiseParams();
        zp.mode = ZegoVideoDenoiseMode.getZegoVideoDenoiseMode(params.mode.value());
        zp.strength = ZegoVideoDenoiseStrength.getZegoVideoDenoiseStrength(params.strength.value());
        sEngine.setVideoDenoiseParams(zp, ZegoPublishChannel.MAIN);
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
        sEngine.startPlayingStream(streamId, createCanvasWithView(view));
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
            List <String> userIdList = new ArrayList<>();
            for (ZegoStream stream : streamList) {
                String streamId = stream.streamID;
                String userId = userIdWithStreamId(streamId);
                View view = mListener.viewForUserId(userId);
                ZegoCanvas canvas = null;
                if (view != null) {
                    canvas = createCanvasWithView(view);
                }
                sEngine.startPlayingStream(streamId, canvas);
                sEngine.startSoundLevelMonitor();
                userIdList.add(userId);
            }
            if (mListener != null) {
                mListener.onUsersConnect(userIdList);
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
        if (state == ZegoRemoteDeviceState.OPEN) {
            mListener.onUserCameraChange(userId, true);
        } else if (state == ZegoRemoteDeviceState.DISABLE) {
            mListener.onUserCameraChange(userId, false);
        }
        if (sHandler != null) {
            sHandler.onRemoteCameraStateUpdate(streamID, state);
        }
    }

    @Override
    public void onRemoteMicStateUpdate(String streamID, ZegoRemoteDeviceState state) {
        if (mListener == null) {
            return;
        }
        String userId = userIdWithStreamId(streamID);
        if (state == ZegoRemoteDeviceState.OPEN) {
            mListener.onUserMicStateUpdate(userId, true);
        } else if (state == ZegoRemoteDeviceState.MUTE) {
            mListener.onUserMicStateUpdate(userId, false);
        }
        if (sHandler != null) {
            sHandler.onRemoteMicStateUpdate(streamID, state);
        }
    }

    @Override
    public void onPlayerRenderVideoFirstFrame(String streamID) {
        String userId = userIdWithStreamId(streamID);
        if (mListener != null) {
            mListener.onVideoFirstFrameRender(userId);
        }
        if (sHandler != null) {
            sHandler.onPlayerRenderVideoFirstFrame(streamID);
        }
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
        String userId = JIM.getInstance().getCurrentUserId();
        if (!TextUtils.isEmpty(userId)) {
            if (mListener != null) {
                HashMap<String, Float> map = new HashMap<>();
                map.put(JIM.getInstance().getCurrentUserId(), soundLevel);
                mListener.onSoundLevelUpdate(map);
            }
        }
        if (sHandler != null) {
            sHandler.onCapturedSoundLevelUpdate(soundLevel);
        }
    }

    @Override
    public void onRemoteSoundLevelUpdate(HashMap<String, Float> soundLevels) {
        if (mListener != null) {
            HashMap<String, Float> map = new HashMap<>();
            for (Map.Entry<String, Float> entry : soundLevels.entrySet()) {
                String userId = userIdWithStreamId(entry.getKey());
                map.put(userId, entry.getValue());
            }
            mListener.onSoundLevelUpdate(map);
        }

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
//        if (updateType == ZegoUpdateType.ADD) {
//            List<String> userIdList = new ArrayList<>();
//            for (ZegoUser zegoUser : userList) {
//                userIdList.add(zegoUser.userID);
//            }
//            if (mListener != null) {
//                mListener.onUsersConnect(userIdList);
//            }
//        } else if (updateType == ZegoUpdateType.DELETE) {
//            // 暂不处理
//        }
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

    @Override
    public void onRoomTokenWillExpire(String roomID, int remainTimeInSecond) {
        if (sHandler != null) {
            sHandler.onRoomTokenWillExpire(roomID, remainTimeInSecond);
        }
    }

    private String createStreamId(String roomId, String userId) {
        return roomId + SEPARATOR + userId;
    }

    private String userIdWithStreamId(String streamId) {
        String[] parts = streamId.split(Pattern.quote(SEPARATOR));
        if (parts.length > 1) {
            return parts[1];
        }
        return "";
    }

    private ZegoCanvas createCanvasWithView(View view) {
        ZegoCanvas canvas = new ZegoCanvas(view);
        canvas.viewMode = ASPECT_FILL;
        return canvas;
    }

    public static void setEventHandler(IZegoEventHandler handler) {
        sHandler = handler;
    }

    private static ZegoExpressEngine sEngine;
    private static IZegoEventHandler sHandler;
    private static final String SEPARATOR = "+++";
    private ICallMediaEngineListener mListener;
}
