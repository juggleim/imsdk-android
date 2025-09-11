package com.juggle.im.jagoracall;

import static io.agora.rtc2.Constants.REMOTE_AUDIO_STATE_STOPPED;
import static io.agora.rtc2.Constants.REMOTE_VIDEO_STATE_STARTING;

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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import io.agora.rtc2.ChannelMediaOptions;
import io.agora.rtc2.Constants;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;
import io.agora.rtc2.UserInfo;
import io.agora.rtc2.video.VideoCanvas;

public class CallMediaAgoraEngine implements ICallMediaEngine {
    public CallMediaAgoraEngine(String appId, Context context) {
        mUserMap = new HashMap<>();
        RtcEngine.destroy();
        RtcEngineConfig config = new RtcEngineConfig();
        config.mContext = context.getApplicationContext();
        config.mAppId = appId;
        config.mEventHandler = new IRtcEngineEventHandler() {
            @Override
            public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
                mUserMap.clear();
                mEngine.enableAudioVolumeIndication(1000, 3, false);
                if (mCallCompleteCallback != null) {
                    mCallCompleteCallback.onComplete(0, null);
                    mCallCompleteCallback = null;
                }
            }

            @Override
            public void onUserInfoUpdated(int uid, UserInfo userInfo) {
                mUserMap.put(userInfo.userAccount, uid);
            }

            @Override
            public void onUserJoined(int uid, int elapsed) {
                for (Map.Entry<String, Integer> entry : mUserMap.entrySet()) {
                    if (uid == entry.getValue()) {
                        if (mListener != null) {
                            View view = mListener.viewForUserId(entry.getKey());
                            VideoCanvas canvas = new VideoCanvas(view);
                            canvas.uid = uid;
                            mEngine.setupRemoteVideo(canvas);
                            mListener.onUsersConnect(Collections.singletonList(entry.getKey()));
                        }
                        break;
                    }
                }
            }

            @Override
            public void onRemoteVideoStateChanged(int uid, int state, int reason, int elapsed) {
                for (Map.Entry<String, Integer> entry : mUserMap.entrySet()) {
                    if (uid == entry.getValue()) {
                        if (mListener != null) {
                            if (state == REMOTE_VIDEO_STATE_STARTING) {
                                mListener.onUserCameraChange(entry.getKey(), true);
                            } else if (state == REMOTE_AUDIO_STATE_STOPPED) {
                                mListener.onUserCameraChange(entry.getKey(), false);
                            }
                        }
                        break;
                    }
                }
            }

            @Override
            public void onUserMuteAudio(int uid, boolean muted) {
                for (Map.Entry<String, Integer> entry : mUserMap.entrySet()) {
                    if (uid == entry.getValue()) {
                        if (mListener != null) {
                            mListener.onUserMicStateUpdate(entry.getKey(), !muted);
                        }
                        break;
                    }
                }
            }

            @Override
            public void onAudioVolumeIndication(AudioVolumeInfo[] speakers, int totalVolume) {
                HashMap<String, Float> resultMap = new HashMap<>();
                for (AudioVolumeInfo info : speakers) {
                    float volume = (float) (info.volume * 100) / 255;
                    if (info.uid == 0) {
                        String userId = JIM.getInstance().getCurrentUserId();
                        if (!TextUtils.isEmpty(userId)) {
                            resultMap.put(userId, volume);
                        }
                    } else {
                        for (Map.Entry<String, Integer> entry : mUserMap.entrySet()) {
                            if (info.uid == entry.getValue()) {
                                resultMap.put(entry.getKey(), volume);
                                break;
                            }
                        }
                    }
                }
                if (!resultMap.isEmpty() && mListener != null) {
                    mListener.onSoundLevelUpdate(resultMap);
                }
            }

            @Override
            public void onFirstRemoteVideoFrame(int uid, int width, int height, int elapsed) {
                for (Map.Entry<String, Integer> entry : mUserMap.entrySet()) {
                    if (uid == entry.getValue()) {
                        if (mListener != null) {
                            mListener.onVideoFirstFrameRender(entry.getKey());
                        }
                        break;
                    }
                }
            }
        };
        try {
            mEngine = RtcEngine.create(config);
            mEngine.enableVideo();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void joinRoom(CallMediaRoom room, CallMediaUser user, CallMediaRoomConfig config, ICallCompleteCallback callback) {
        if (room == null || room.getRoomId() == null || room.getRoomId().isEmpty()
                || user == null || user.getUserId() == null || user.getUserId().isEmpty()
                || config == null) {
            return;
        }
        ChannelMediaOptions o = new ChannelMediaOptions();
        o.channelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING;
        o.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER;
        o.publishMicrophoneTrack = true;
        o.publishCameraTrack = mEnableCamera;
        o.autoSubscribeAudio = true;
        o.autoSubscribeVideo = true;

        int errorCode = mEngine.joinChannelWithUserAccount(config.getToken(), room.getRoomId(), user.getUserId(), o);
        if (errorCode != 0) {
            if (callback != null) {
                callback.onComplete(errorCode, null);
            }
        } else {
            mCallCompleteCallback = callback;
        }
    }

    @Override
    public void leaveRoom(String roomId) {
        mEngine.leaveChannel();
    }

    @Override
    public void enableCamera(boolean isEnable) {
        mEnableCamera = isEnable;
        mEngine.muteLocalVideoStream(!isEnable);
    }

    @Override
    public void startPreview(View view) {
        VideoCanvas canvas = new VideoCanvas(view);
        mEngine.setupLocalVideo(canvas);
        mEngine.startPreview();
    }

    @Override
    public void stopPreview() {
        mEngine.stopPreview();
    }

    @Override
    public void setVideoView(String roomId, String userId, View view) {
        if (TextUtils.isEmpty(roomId) || TextUtils.isEmpty(userId)) {
            return;
        }
        for (Map.Entry<String, Integer> entry : mUserMap.entrySet()) {
            if (entry.getKey().equals(userId)) {
                VideoCanvas canvas = new VideoCanvas(view);
                canvas.uid = entry.getValue();
                mEngine.setupRemoteVideo(canvas);
                break;
            }
        }
    }

    @Override
    public void muteMicrophone(boolean isMute) {
        mEngine.enableLocalAudio(!isMute);
    }

    @Override
    public void muteSpeaker(boolean isMute) {
        mEngine.muteAllRemoteAudioStreams(isMute);
    }

    @Override
    public void setSpeakerEnable(boolean isEnable) {
        mEngine.setEnableSpeakerphone(isEnable);
    }

    @Override
    public void useFrontCamera(boolean isEnable) {
        mEngine.switchCamera();
    }

    @Override
    public void enableAEC(boolean isEnable) {

    }

    @Override
    public void setVideoDenoiseParams(CallVideoDenoiseParams params) {

    }

    @Override
    public void setListener(ICallMediaEngineListener listener) {
        mListener = listener;
    }

    private RtcEngine mEngine;

    private ICallMediaEngineListener mListener;
    private final Map<String, Integer> mUserMap;
    private boolean mEnableCamera;
    private ICallCompleteCallback mCallCompleteCallback;
}
