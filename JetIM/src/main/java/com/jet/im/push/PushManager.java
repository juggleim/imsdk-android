package com.jet.im.push;

import android.content.Context;
import android.util.Log;

import com.jet.im.JetIM;
import com.jet.im.JetIMConst;
import com.jet.im.internal.util.JUtility;
import com.jet.im.push.google.GooglePush;
import com.jet.im.push.hw.HWPush;
import com.jet.im.push.xm.XMPush;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class PushManager implements IPush.Callback {
    private static final String TAG = "PushManager";
    private ThreadPoolExecutor pushExecutor;
    Map<PushType, IPush> iPushMap = new HashMap<>();

    public static PushManager getInstance() {
        return PushManager.SingletonHolder.sInstance;
    }

    private PushManager() {
        pushExecutor = new ThreadPoolExecutor(1, 1, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        pushExecutor.allowCoreThreadTimeOut(true);
    }

    public void init(Context context, PushConfig config) {
        pushExecutor.execute(new Runnable() {
            @Override
            public void run() {
                initHW();
                initXM();
                initGoogle();
                List<IPush> pushList = getRegisterPush();
                for (IPush item : pushList) {
                    item.getToken(context, config, PushManager.this);
                }
            }
        });
    }

    /**
     * 获取适合的推送类型 根据手机机型和用户 enable 适配的推送类型
     */
    public List<IPush> getRegisterPush() {
        List<IPush> result = new ArrayList<>();
        String os = JUtility.getDeviceManufacturer().toLowerCase();
        for (Map.Entry<PushType, IPush> item : iPushMap.entrySet()) {
            if (item.getKey() == PushType.GOOGLE) {
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

    private void initHW() {
        try {
            Class.forName("com.jet.im.push.hw.HWPush");
            IPush push = new HWPush();
            iPushMap.put(push.getType(), push);
        } catch (ClassNotFoundException e) {
            Log.d(TAG, "not register hw");
        }
    }

    private void initXM() {
        try {
            Class.forName("com.jet.im.push.xm.XMPush");
            IPush push = new XMPush();
            iPushMap.put(push.getType(), push);
        } catch (ClassNotFoundException e) {
            Log.d(TAG, "not register xm");
        }
    }

    private void initGoogle() {
        try {
            Class.forName("com.jet.im.push.google.GooglePush");
            IPush push = new GooglePush();
            iPushMap.put(push.getType(), push);
        } catch (ClassNotFoundException e) {
            Log.d(TAG, "not register google");
        }
    }

    @Override
    public void onReceivedToken(PushType type, String token) {
        //todo 1.本地持久化，2.调用上报 token 接口
        pushExecutor.execute(new Runnable() {
            @Override
            public void run() {
                JetIM.getInstance().getConnectionManager().registerPushToken(JetIMConst.PushChannel.getPushChannel(type), token);
            }
        });
    }

    @Override
    public void onError(PushType type, int code, String msg) {
        //todo 处理 onError
    }

    private static class SingletonHolder {
        static final PushManager sInstance = new PushManager();
    }
}
