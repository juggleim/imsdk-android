package com.juggle.im.call.internal.fsm;

import android.os.Message;

import com.juggle.im.call.CallConst;
import com.juggle.im.call.internal.CallEvent;
import com.juggle.im.call.internal.CallSessionImpl;
import com.juggle.im.internal.util.JLogger;

public class CallSuperState extends CallState {
    @Override
    public boolean processMessage(Message msg) {
        super.processMessage(msg);

        CallSessionImpl callSession = getCallSessionImpl();
        if (callSession == null) {
            JLogger.e("FSM-Sm", "callSession is null");
            return true;
        }

        switch (msg.what) {
            case CallEvent.INVITE:
                // do nothing
                // idle 状态处理
                // 其它状态下 invite 两次不会是同一个 callSession
                break;

            case CallEvent.INVITE_FAIL:
                // do nothing
                // outgoing 状态处理
                // 其它状态下忽略
                break;

            case CallEvent.RECEIVE_INVITE:
                // do nothing
                // idle 状态处理
                // 其它状态下忽略（服务端不会给已在房间内的用户发送同一个 callId 的 invite）
                break;

            case CallEvent.HANGUP:
                if (callSession.getCallStatus() == CallConst.CallStatus.INCOMING) {
                    callSession.setFinishReason(CallConst.CallFinishReason.DECLINE);
                } else if (callSession.getCallStatus() == CallConst.CallStatus.OUTGOING) {
                    callSession.setFinishReason(CallConst.CallFinishReason.CANCEL);
                } else {
                    callSession.setFinishReason(CallConst.CallFinishReason.HANGUP);
                }
                callSession.signalHangup();
                callSession.transitionToIdleState();
                break;

            case CallEvent.ROOM_DESTROY:
                callSession.setFinishReason(CallConst.CallFinishReason.ROOM_DESTROY);
                callSession.transitionToIdleState();
                break;

            case CallEvent.ACCEPT:
                callSession.error(CallConst.CallErrorCode.CANT_ACCEPT_WHILE_NOT_INVITED);
                break;

            case CallEvent.ACCEPT_DONE:
                // do nothing
                // incoming 状态处理
                // 其它状态忽略
                break;

            case CallEvent.ACCEPT_FAIL:
                // do nothing
                // incoming 状态处理
                // 其它状态忽略
                break;

            case CallEvent.RECEIVE_ACCEPT:
                // TODO: 更新 member 状态
                // outgoing 状态处理
                // 其它状态忽略
                break;

            case CallEvent.RECEIVE_HANGUP:
                String userId = (String) msg.obj;
                callSession.memberHangup(userId);
                if (!callSession.isMultiCall()) {
                    callSession.transitionToIdleState();
                }
                break;

            case CallEvent.JOIN_CHANNEL_DONE:
                // do nothing
                // connecting 状态处理
                // 其它状态忽略
                break;

            case CallEvent.JOIN_CHANNEL_FAIL:
                // do nothing
                // connecting 状态处理
                // 其它状态忽略
                break;

            default:
                return false;
        }

        return true;
    }
}
