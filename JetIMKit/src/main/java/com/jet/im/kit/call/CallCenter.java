package com.jet.im.kit.call;

import android.content.Context;
import android.content.Intent;

import com.juggle.im.JIM;
import com.juggle.im.call.ICallManager;
import com.juggle.im.call.ICallSession;

public class CallCenter implements ICallManager.ICallReceiveListener {
    public static CallCenter getInstance() {
        return SingletonHolder.sInstance;
    }

    public void initZegoEngine(int appId, Context context) {
        JIM.getInstance().getCallManager().initZegoEngine(appId, context);
        mContext = context;
    }

    public void startSingleCall(Context context, String userId) {
        if (context == null) return;
        ICallSession callSession = JIM.getInstance().getCallManager().startSingleCall(userId, null);
        Intent intent = new Intent("com.jet.im.intent.action.SINGLE_AUDIO");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("callId", callSession.getCallId());
        context.startActivity(intent);
    }

    @Override
    public void onCallReceive(ICallSession callSession) {
        if (mContext == null) {
            return;
        }
        Intent intent = new Intent("com.jet.im.intent.action.SINGLE_AUDIO");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("callId", callSession.getCallId());
        mContext.startActivity(intent);
    }

    private CallCenter() {
        JIM.getInstance().getCallManager().addReceiveListener("CallCenter", this);
    }

    private static class SingletonHolder {
        static final CallCenter sInstance = new CallCenter();
    }

    private Context mContext;

}
