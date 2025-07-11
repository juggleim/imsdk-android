package com.juggle.im.call;

import android.view.View;

import com.juggle.im.call.model.CallMember;

import java.util.HashMap;
import java.util.List;

public interface ICallSession {
    interface ICallSessionListener {
        // 通话已接通
        void onCallConnect();

        // 通话已结束
        void onCallFinish(CallConst.CallFinishReason finishReason);

        // 通话中的错误回调
        void onErrorOccur(CallConst.CallErrorCode errorCode);

        // 用户被邀请（多人通话中使用）
        void onUsersInvite(String inviterId, List<String> userIdList);

        // 用户加入通话（多人通话中使用）
        void onUsersConnect(List<String> userIdList);

        // 用户退出通话（多人通话中使用）
        void onUsersLeave(List<String> userIdList);

        // 用户开启/关闭摄像头
        void onUserCameraEnable(String userId, boolean enable);

        // 用户开启/关闭麦克风
        void onUserMicrophoneEnable(String userId, boolean enable);

        // 用户声音大小变化
        // userId 为 key，声音大小为 value
        void onSoundLevelUpdate(HashMap<String, Float> soundLevels);
    }

    void addListener(String key, ICallSessionListener listener);
    void removeListener(String key);

    // 接听来电
    void accept();
    // 挂断来电
    void hangup();
    // 开启摄像头
    void enableCamera(boolean isEnable);
    // 设置用户的视频 view
    void setVideoView(String userId, View view);
    // 开始预览
    void startPreview(View view);
    // 设置麦克风静音
    void muteMicrophone(boolean isMute);
    // 设置扬声器静音
    void muteSpeaker(boolean isMute);
    // 设置外放声音
    // true 使用外放扬声器；false 使用听筒
    void setSpeakerEnable(boolean isEnable);
    // 切换摄像头，默认 true 使用前置摄像头
    void useFrontCamera(boolean isEnable);
    // 呼叫用户加入通话（isMultiCall 为 false 时不支持该功能）
    void inviteUsers(List<String> userIdList);

    // 通话 id
    String getCallId();
    // 是否多人通话，false 表示一对一通话
    boolean isMultiCall();
    // 媒体类型（语音/视频）
    CallConst.CallMediaType getMediaType();
    // 通话状态
    CallConst.CallStatus getCallStatus();
    // 呼叫开始时间（多人会话中当前用户被呼叫的时间，不一定等于整个通话开始的时间）
    long getStartTime();
    // 当前用户加入通话的时间
    long getConnectTime();
    // 当前用户结束通话的时间
    long getFinishTime();
    // 通话的发起人 id
    String getOwner();
    // 邀请当前用户加入通话的用户 id
    String getInviter();
    // 通话结束原因
    CallConst.CallFinishReason getFinishReason();
    // 通话参与者（除当前用户外的其他参与者）
    List<CallMember> getMembers();
    // 当前用户
    CallMember getCurrentCallMember();
}
