package com.juggle.im.call.internal.media;

import android.content.Context;

import com.juggle.im.JIM;
import com.juggle.im.call.internal.CallSessionImpl;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class CallMediaManager {

    public static CallMediaManager getInstance() {
        return SingletonHolder.sInstance;
    }

    public void initZegoEngine(int appId, Context context) {
        try {
            Class clazz = Class.forName("com.j.im.jzegocall.CallMediaZegoEngine");
            Constructor constructor = clazz.getConstructor(int.class, Context.class);
            mEngine = (ICallMediaEngine) constructor.newInstance(appId, context);
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
        mEngine.joinRoom(room, user, config, callback);
    }

    public void leaveRoom(String roomId) {
        mEngine.leaveRoom(roomId);
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

    private static class SingletonHolder {
        static final CallMediaManager sInstance = new CallMediaManager();
    }

    private ICallMediaEngine mEngine;

}
