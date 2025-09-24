package com.juggle.im.call.internal.fsm;

import android.os.Message;

import com.juggle.im.call.CallConst;
import com.juggle.im.call.internal.CallEvent;
import com.juggle.im.call.internal.CallSessionImpl;
import com.juggle.im.internal.util.JLogger;
import com.juggle.im.model.UserInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CallSuperState extends CallState {
    @Override
    public boolean processMessage(Message msg) {
        super.processMessage(msg);

        CallSessionImpl callSession = getCallSessionImpl();
        if (callSession == null) {
            JLogger.e("FSM-Sm", "callSession is null");
            return true;
        }
        String userId;
        List<String> userIdList;
        Map<?, ?> map;
        boolean enable;

        switch (msg.what) {
            case CallEvent.INVITE:
                // do nothing
                // idle 状态处理
                // connected 状态处理
                // 其它状态下 invite 两次不会是同一个 callSession
                break;

            case CallEvent.INVITE_DONE:
                // do nothing
                // outgoing 状态处理
                // connected 状态处理
                // 其它状态下忽略
                break;

            case CallEvent.INVITE_FAIL:
                // do nothing
                // outgoing 状态处理
                // connected 状态处理
                // 其它状态下忽略
                break;

            case CallEvent.RECEIVE_INVITE:
                // do nothing
                // idle 状态处理
                // 其它状态下忽略（服务端不会给已在房间内的用户发送同一个 callId 的 invite）
                break;

            case CallEvent.RECEIVE_INVITE_OTHERS:
                // idle 状态 do nothing
                // 其它状态统一由这里处理
                map = (Map<?, ?>) msg.obj;
                UserInfo inviter = (UserInfo) map.get("inviter");
                List<UserInfo> targetUsers = (List<UserInfo>) map.get("targetUsers");
                callSession.addInviteMembers(inviter, targetUsers);
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
                if (callSession.getCallStatus() == CallConst.CallStatus.INCOMING) {
                    callSession.transitionToIdleStateWithoutMediaQuit();
                } else {
                    callSession.transitionToIdleState();
                }
                break;

            case CallEvent.RECEIVE_SELF_QUIT:
                if (callSession.getCallStatus() == CallConst.CallStatus.CONNECTED) {
                    callSession.setFinishReason(CallConst.CallFinishReason.NETWORK_ERROR);
                } else if (callSession.getCallStatus() == CallConst.CallStatus.INCOMING) {
                    callSession.setFinishReason(CallConst.CallFinishReason.NO_RESPONSE);
                }
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
                // outgoing 状态处理（别的用户 accept）
                // incoming 状态处理（当前用户在其它端 accept）
                userId = (String) msg.obj;
                callSession.memberAccept(userId);
                break;

            case CallEvent.RECEIVE_HANGUP:
                // incoming 状态处理（当前用户在其它端 hangup）
                userId = (String) msg.obj;
                callSession.memberHangup(userId);
                if (!callSession.isMultiCall()) {
                    callSession.transitionToIdleState();
                }
                break;

            case CallEvent.RECEIVE_QUIT:
                // 跟 JCallEventReceiveHangup 不同，incoming 状态不会收到当前用户在其它端的 quit 事件
                userIdList = (List<String>) msg.obj;
                callSession.membersQuit(userIdList);
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

            case CallEvent.PARTICIPANT_JOIN_CHANNEL:
                userIdList = (List<String>) msg.obj;
                callSession.membersConnected(userIdList);
                break;

            case CallEvent.PARTICIPANT_LEAVE_CHANNEL:
                break;

            case CallEvent.PARTICIPANT_ENABLE_CAMERA:
                map = (Map<?, ?>) msg.obj;
                enable = (boolean) map.get("enable");
                userId = (String) map.get("userId");
                callSession.cameraEnable(userId, enable);
                break;

            case CallEvent.PARTICIPANT_ENABLE_MIC:
                map = (Map<?, ?>) msg.obj;
                enable = (boolean) map.get("enable");
                userId = (String) map.get("userId");
                callSession.micEnable(userId, enable);
                break;

            case CallEvent.SOUND_LEVEL_UPDATE:
                HashMap<String, Float> soundLevels = (HashMap<String, Float>) msg.obj;
                callSession.soundLevelUpdate(soundLevels);
                break;

            case CallEvent.VIDEO_FIRST_FRAME_RENDER:
                userId = (String) msg.obj;
                callSession.videoFirstFrameRender(userId);
                break;

            case CallEvent.JOIN:
                // do nothing
                // idle 状态处理
                // 其它状态下忽略
                break;

            case CallEvent.JOIN_DONE:
                // do nothing
                // join 状态处理
                // 其它状态下忽略
                break;

            case CallEvent.JOIN_FAIL:
                // do nothing
                // join 状态处理
                // 其它状态下忽略
                break;

            case CallEvent.RECEIVE_JOIN:
                List<UserInfo> userInfoList = (List<UserInfo>) msg.obj;
                callSession.membersJoin(userInfoList);
                break;

            default:
                return false;
        }

        return true;
    }
}
