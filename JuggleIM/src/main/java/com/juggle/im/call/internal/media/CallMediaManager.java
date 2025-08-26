package com.juggle.im.call.internal.media;

import android.content.Context;
import android.view.View;

import com.juggle.im.JIM;
import com.juggle.im.call.internal.CallSessionImpl;
import com.juggle.im.call.model.CallVideoDenoiseParams;

import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;

public class CallMediaManager implements ICallMediaEngine.ICallMediaEngineListener {

    public static CallMediaManager getInstance() {
        return SingletonHolder.sInstance;
    }

    public void initZegoEngine(int appId, Context context) {
        try {
            Class clazz = Class.forName("com.j.im.jzegocall.CallMediaZegoEngine");
            Constructor constructor = clazz.getConstructor(int.class, Context.class);
            mEngine = (ICallMediaEngine) constructor.newInstance(appId, context);
            mEngine.setListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void joinRoom(CallSessionImpl callSession, ICallCompleteCallback callback) {
        CallMediaRoom room = new CallMediaRoom();
        room.setRoomId(callSession.getCallId());
        CallMediaUser user = new CallMediaUser();
        user.setUserId(JIM.getInstance().getCurrentUserId());
        CallMediaRoomConfig config = new CallMediaRoomConfig();
        config.setUserStatusNotify(true);
        config.setZegoToken(callSession.getZegoToken());
        mEngine.joinRoom(room, user, config, new ICallCompleteCallback() {
            @Override
            public void onComplete(int errorCode, JSONObject data) {
                if (errorCode == 0) {
                    mListener = callSession;
                }
                if (callback != null) {
                    callback.onComplete(errorCode, data);
                }
            }
        });
    }

    public void leaveRoom(String roomId) {
        mListener = null;
        mEngine.leaveRoom(roomId);
    }

    public void enableCamera(boolean isEnable) {
        mEngine.enableCamera(isEnable);
    }

    public void startPreview(View view) {
        mEngine.startPreview(view);
    }

    public void stopPreview() {
        mEngine.stopPreview();
    }

    public void setVideoView(String roomId, String userId, View view) {
        mEngine.setVideoView(roomId, userId, view);
    }

    public void muteMicrophone(boolean isMute) {
        mEngine.muteMicrophone(isMute);
    }
    
    public void muteSpeaker(boolean isMute) {
        mEngine.muteSpeaker(isMute);
    }

    public void setSpeakerEnable(boolean isEnable) {
        mEngine.setSpeakerEnable(isEnable);
    }

    public void useFrontCamera(boolean isEnable) {
        mEngine.useFrontCamera(isEnable);
    }

    public void enableAEC(boolean isEnable) {
        mEngine.enableAEC(isEnable);
    }

    public void setVideoDenoiseParams(CallVideoDenoiseParams params) {
        mEngine.setVideoDenoiseParams(params);
    }

    @Override
    public View viewForUserId(String userId) {
        if (mListener != null) {
            return mListener.viewForUserId(userId);
        }
        return null;
    }

    @Override
    public void onUsersJoin(List<String> userIdList) {
        if (mListener != null) {
            mListener.onUsersJoin(userIdList);
        }
    }

    @Override
    public void onUserCameraChange(String userId, boolean enable) {
        if (mListener != null) {
            mListener.onUserCameraChange(userId, enable);
        }
    }

    @Override
    public void onUserMicStateUpdate(String userId, boolean enable) {
        if (mListener != null) {
            mListener.onUserMicStateUpdate(userId, enable);
        }
    }

    @Override
    public void onUsersLeave(List<String> userIdList) {

    }

    @Override
    public void onSoundLevelUpdate(HashMap<String, Float> soundLevels) {
        if (mListener != null) {
            mListener.onSoundLevelUpdate(soundLevels);
        }
    }

    @Override
    public void onVideoFirstFrameRender(String userId) {
        if (mListener != null) {
            mListener.onVideoFirstFrameRender(userId);
        }
    }

    private static class SingletonHolder {
        static final CallMediaManager sInstance = new CallMediaManager();
    }

    private ICallMediaEngine mEngine;
    private ICallMediaListener mListener;

}
