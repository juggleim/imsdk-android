package com.juggle.im.push;

import android.content.Context;

import com.juggle.im.JIM;
import com.juggle.im.internal.util.JLogger;
import com.juggle.im.internal.util.JUtility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class PushManager implements IPush.Callback {
    private static final String TAG = "CON-Push";
    private final ThreadPoolExecutor pushExecutor;
    private PushConfig mConfig;
    private boolean mHasRegister = false;
    Map<PushChannel, IPush> iPushMap = new HashMap<>();

    public static PushManager getInstance() {
        return PushManager.SingletonHolder.sInstance;
    }

    private PushManager() {
        pushExecutor = new ThreadPoolExecutor(1, 1, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        pushExecutor.allowCoreThreadTimeOut(true);
    }

    public void init(PushConfig config) {
        pushExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mConfig = config;
                mHasRegister = false;
                init("com.juggle.im.push.hw.HWPush");
                init("com.juggle.im.push.xm.XMPush");
                init("com.juggle.im.push.vivo.VIVOPush");
                init("com.juggle.im.push.oppo.OPPOPush");
                init("com.juggle.im.push.jg.JGPush");
                init("com.juggle.im.push.google.GooglePush");
            }
        });
    }

    public void getToken(Context context) {
        pushExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (mConfig == null) return;
                if (mHasRegister) return;
                mHasRegister = true;

                List<IPush> pushList = getRegisterPush();
                for (IPush item : pushList) {
                    item.getToken(context, mConfig, PushManager.this);
                }
            }
        });
    }

    /**
     * 获取适合的推送类型 根据手机机型和用户 enable 适配的推送类型
     */
    private List<IPush> getRegisterPush() {
        List<IPush> result = new ArrayList<>();
        String os = JUtility.getDeviceManufacturer().toLowerCase();
        for (Map.Entry<PushChannel, IPush> item : iPushMap.entrySet()) {
            if (item.getKey() == PushChannel.JIGUANG || item.getKey() == PushChannel.GOOGLE) {
                result.add(item.getValue());
                continue;
            }
            String pushTypeOs = item.getKey().getOs();
            if (pushTypeOs.contains(os)) {
                result.add(item.getValue());
            }
        }
        return result;
    }

    private void init(String className) {
        try {
            Class<?> aClass = Class.forName(className);
            IPush push = (IPush) aClass.newInstance();
            iPushMap.put(push.getType(), push);
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            JLogger.w(TAG, "not register " + className);
        }
    }

    @Override
    public void onReceivedToken(PushChannel type, String token) {
        //todo 1.本地持久化，2.调用上报 token 接口
        pushExecutor.execute(new Runnable() {
            @Override
            public void run() {
                JLogger.i(TAG, "on push token received, channel= " + type.getName() + ", token= " + token);
                JIM.getInstance().getConnectionManager().registerPushToken(type, token);
            }
        });
    }

    @Override
    public void onError(PushChannel type, int code, String msg) {
        //todo 处理 onError
        pushExecutor.execute(new Runnable() {
            @Override
            public void run() {
                JLogger.e(TAG, "on push token error, channel= " + type.getName() + ", code= " + code + ", msg= " + msg);
                mHasRegister = false;
            }
        });
    }

    private static class SingletonHolder {
        static final PushManager sInstance = new PushManager();
    }
}
