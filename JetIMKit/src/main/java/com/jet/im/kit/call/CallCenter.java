package com.jet.im.kit.call;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.juggle.im.JIM;
import com.juggle.im.call.CallConst;
import com.juggle.im.call.ICallManager;
import com.juggle.im.call.ICallSession;
import com.juggle.im.call.model.CallInfo;
import com.juggle.im.model.Conversation;

import java.util.List;

public class CallCenter implements ICallManager.ICallReceiveListener, ICallManager.IConversationCallListener {
    public static CallCenter getInstance() {
        return SingletonHolder.sInstance;
    }

    public void initZegoEngine(int appId, Context context) {
        JIM.getInstance().getCallManager().initZegoEngine(appId, context);
        mContext = context;
        JIM.getInstance().getCallManager().addConversationCallListener("CallCenter", this);
    }

    public void startSingleCall(Context context, String userId, CallConst.CallMediaType mediaType, String extra) {
        if (context == null) return;
        ICallSession callSession = JIM.getInstance().getCallManager().startSingleCall(userId, mediaType, extra, null);
        Intent intent = buildIntent(false, callSession.getMediaType(), callSession.getCallId());
        context.startActivity(intent);
    }

    public void startMultiCall(Context context, List<String> userIdList, CallConst.CallMediaType mediaType, String extra, String groupId) {
        if (context == null) return;
        Conversation conversation = new Conversation(Conversation.ConversationType.GROUP, groupId);
        ICallSession callSession = JIM.getInstance().getCallManager().startMultiCall(userIdList, mediaType, conversation, extra,null);
        Intent intent = buildIntent(true, mediaType, callSession.getCallId());
        intent.putExtra("groupId", groupId);
        context.startActivity(intent);
    }

    public void joinCall(Context context, String callId, String groupId) {
        if (context == null) return;
        ICallSession callSession = JIM.getInstance().getCallManager().joinCall(callId, null);
        int i = 1;
//        Intent intent = buildIntent(true, CallConst.CallMediaType.VOICE, callId);
//        intent.putExtra("groupId", groupId);
//        context.startActivity(intent);
    }

    @Override
    public void onCallReceive(ICallSession callSession) {
        if (mContext == null) {
            return;
        }
        Intent intent = buildIntent(callSession.isMultiCall(), callSession.getMediaType(), callSession.getCallId());
        mContext.startActivity(intent);
    }

    private Intent buildIntent(boolean isMultiCall, CallConst.CallMediaType mediaType, String callId) {
        Intent intent;
        if (!isMultiCall) {
            intent = new Intent("com.jet.im.intent.action.SINGLE_CALL");
        } else {
            if (mediaType == CallConst.CallMediaType.VOICE) {
                intent = new Intent("com.jet.im.intent.action.MULTI_VOICE_CALL");
            } else {
                intent = new Intent("com.jet.im.intent.action.MULTI_VIDEO_CALL");
            }
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("callId", callId);
        return intent;
    }

    private CallCenter() {
        JIM.getInstance().getCallManager().addReceiveListener("CallCenter", this);
    }

    @Override
    public void onCallInfoUpdate(CallInfo callInfo, Conversation conversation, boolean isFinished) {
        Log.i("CallCenter", "onCallInfoUpdate");
    }

    private static class SingletonHolder {
        static final CallCenter sInstance = new CallCenter();
    }

    private Context mContext;

}
