package com.jet.im.kit.call;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.jet.im.kit.R;
import com.jet.im.kit.utils.ViewUtils;
import com.juggle.im.JIM;
import com.juggle.im.call.CallConst;
import com.juggle.im.call.ICallSession;
import com.juggle.im.call.model.CallMember;
import com.juggle.im.model.UserInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SingleCallActivity extends BaseCallActivity implements ICallSession.ICallSessionListener {
    private static final int LOSS_RATE_ALARM = 20;
    private LayoutInflater inflater;
    private VideoView mLPreviewContainer;
    private VideoView mSPreviewContainer;
    private FrameLayout mButtonContainer;
    private LinearLayout mUserInfoContainer;
    private TextView mConnectionStateTextView;
    private Boolean isInformationShow = false;
    private boolean muted = false;
    private boolean handFree = false;
    private boolean startForCheckPermissions = false;
    private boolean isReceiveLost = false;
    private boolean isSendLost = false;
    private int EVENT_FULL_SCREEN = 1;
    private String targetId = null;
    /// 是否切换了自己和对方的视频 view（默认对方为 LView，自己为 SView）
    private boolean mSwitchMainSubVideo;
    private boolean mUseFrontCamera = true;
    private boolean mEnableCamera = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rc_voip_activity_single_call);
        mLPreviewContainer = findViewById(R.id.rc_voip_call_large_preview);
        mSPreviewContainer = findViewById(R.id.rc_voip_call_small_preview);
        mButtonContainer = findViewById(R.id.rc_voip_btn);
        mUserInfoContainer = findViewById(R.id.rc_voip_user_info);
        mConnectionStateTextView = findViewById(R.id.rc_tv_connection_state);

        inflater = LayoutInflater.from(this);
        mCallSession.addListener("SingleCallActivity", this);
        initView();
    }

    private void initView() {
        RelativeLayout buttonLayout = (RelativeLayout) inflater.inflate(R.layout.rc_voip_call_bottom_connected_button_layout, null);
        RelativeLayout userInfoLayout;
        if (mCallSession.getMediaType() == CallConst.CallMediaType.VOICE || mCallSession.getCallStatus() == CallConst.CallStatus.INCOMING) {
            userInfoLayout = (RelativeLayout) inflater.inflate(R.layout.rc_voip_audio_call_user_info_incoming, null);
            userInfoLayout.findViewById(R.id.iv_large_preview_Mask).setVisibility(View.VISIBLE);
        } else {
            userInfoLayout = (RelativeLayout) inflater.inflate(R.layout.rc_voip_audio_call_user_info, null);
            TextView callInfo = userInfoLayout.findViewById(R.id.rc_voip_call_remind_info);
            CallUtils.textViewShadowLayer(callInfo, SingleCallActivity.this);
        }

        if (mCallSession.getCallStatus() == CallConst.CallStatus.OUTGOING) {
            RelativeLayout layout = buttonLayout.findViewById(R.id.rc_voip_call_mute);
            layout.setVisibility(View.GONE);
            ImageView button = buttonLayout.findViewById(R.id.rc_voip_call_mute_btn);
            button.setEnabled(false);
            buttonLayout.findViewById(R.id.rc_voip_handfree).setVisibility(View.GONE);
        }

        if (mCallSession.getMediaType() == CallConst.CallMediaType.VOICE) {
            findViewById(R.id.rc_voip_call_information).setBackgroundColor(getResources().getColor(R.color.rc_voip_background_color));
            mLPreviewContainer.setVisibility(View.GONE);
            mSPreviewContainer.setVisibility(View.GONE);

            if (mCallSession.getCallStatus() == CallConst.CallStatus.INCOMING) {
                buttonLayout = (RelativeLayout) inflater.inflate(R.layout.rc_voip_call_bottom_incoming_button_layout, null);
                ImageView iv_answerBtn =  buttonLayout.findViewById(R.id.rc_voip_call_answer_btn);
                iv_answerBtn.setBackground(CallUtils.BackgroundDrawable(R.drawable.rc_voip_audio_answer_selector_new, SingleCallActivity.this));

                TextView callInfo =  userInfoLayout.findViewById(R.id.rc_voip_call_remind_info);
                CallUtils.textViewShadowLayer(callInfo, SingleCallActivity.this);
                callInfo.setText(R.string.rc_voip_audio_call_inviting);
//                onIncomingCallRinging();
            }
        } else if (mCallSession.getMediaType() == CallConst.CallMediaType.VIDEO) {
            if (mCallSession.getCallStatus() == CallConst.CallStatus.INCOMING) {
                findViewById(R.id.rc_voip_call_information).setBackgroundColor(getResources().getColor(R.color.rc_voip_background_color));
                buttonLayout = (RelativeLayout) inflater.inflate(R.layout.rc_voip_call_bottom_incoming_button_layout, null);
                ImageView iv_answerBtn = buttonLayout.findViewById(R.id.rc_voip_call_answer_btn);
                iv_answerBtn.setBackground(CallUtils.BackgroundDrawable(R.drawable.rc_voip_vedio_answer_selector_new, SingleCallActivity.this));

                TextView callInfo = userInfoLayout.findViewById(R.id.rc_voip_call_remind_info);
                CallUtils.textViewShadowLayer(callInfo, SingleCallActivity.this);
                callInfo.setText(R.string.rc_voip_video_call_inviting);
//                onIncomingCallRinging();
            }
            mLPreviewContainer.setVisibility(View.VISIBLE);
            mCallSession.startPreview(mLPreviewContainer);
        }
        mButtonContainer.removeAllViews();
        mButtonContainer.addView(buttonLayout);
        mUserInfoContainer.removeAllViews();
        mUserInfoContainer.addView(userInfoLayout);
        List<CallMember> members = mCallSession.getMembers();
        com.juggle.im.model.UserInfo userInfo = members.get(0).getUserInfo();
        if (userInfo != null) {
            if (mCallSession.getMediaType() == CallConst.CallMediaType.VOICE || mCallSession.getCallStatus() == CallConst.CallStatus.INCOMING) {
                ImageView userPortrait = mUserInfoContainer.findViewById(R.id.rc_voip_user_portrait);
                if (userPortrait != null && userInfo.getPortrait() != null) {
                    ViewUtils.drawProfile(userPortrait, userInfo.getPortrait(), userInfo.getPortrait());
                }
                TextView userName = mUserInfoContainer.findViewById(R.id.rc_voip_user_name);
                userName.setText(userInfo.getUserName());
            }
        }
    }

    @Override
    public void onCallConnect() {
        if (mCallSession.getMediaType() == CallConst.CallMediaType.VOICE) {
//            findViewById(R.id.rc_voip_call_minimize).setVisibility(View.VISIBLE);
            RelativeLayout btnLayout = (RelativeLayout) inflater.inflate(R.layout.rc_voip_call_bottom_connected_button_layout, null);
            RelativeLayout layout = btnLayout.findViewById(R.id.rc_voip_call_mute);
            layout.setVisibility(View.VISIBLE);
            btnLayout.findViewById(R.id.rc_voip_handfree).setVisibility(View.VISIBLE);
            ImageView button = btnLayout.findViewById(R.id.rc_voip_call_mute_btn);
            button.setEnabled(true);
            mButtonContainer.removeAllViews();
            mButtonContainer.addView(btnLayout);
        } else {
            mConnectionStateTextView.setVisibility(View.VISIBLE);
            mConnectionStateTextView.setText(R.string.rc_voip_connecting);
            // 二人视频通话接通后 mUserInfoContainer 中更换为无头像的布局
            mUserInfoContainer.removeAllViews();
            inflater.inflate(R.layout.rc_voip_video_call_user_info, mUserInfoContainer);


            List<CallMember> members = mCallSession.getMembers();
            com.juggle.im.model.UserInfo userInfo = members.get(0).getUserInfo();
            if (userInfo != null) {
                TextView userName = mUserInfoContainer.findViewById(R.id.rc_voip_user_name);
                userName.setText(userInfo.getUserName());
//                userName.setShadowLayer(16F, 0F, 2F, getResources().getColor(R.color.rc_voip_reminder_shadow));//callkit_shadowcolor
                CallUtils.textViewShadowLayer(userName, SingleCallActivity.this);
            }
//            mCallSession.setVideoView(mCallSession.getMembers().get(0).getUserInfo().getUserId(), mLPreviewContainer);
//            mCallSession.setVideoView(JIM.getInstance().getCurrentUserId(), mSPreviewContainer);
        }
        TextView tv_rc_voip_call_remind_info =  mUserInfoContainer.findViewById(R.id.rc_voip_call_remind_info);
        CallUtils.textViewShadowLayer(tv_rc_voip_call_remind_info, SingleCallActivity.this);
        tv_rc_voip_call_remind_info.setVisibility(View.GONE);
        TextView remindInfo;
        if (mCallSession.getMediaType() == CallConst.CallMediaType.VOICE) {
            remindInfo = mUserInfoContainer.findViewById(R.id.tv_setupTime);
        } else {
            remindInfo = mUserInfoContainer.findViewById(R.id.tv_setupTime_video);
        }
        if (remindInfo == null) {
            remindInfo = tv_rc_voip_call_remind_info;
        }
        setupTime(remindInfo);

//        RongCallClient.getInstance().setEnableLocalAudio(!muted);
        View muteV = mButtonContainer.findViewById(R.id.rc_voip_call_mute);
        if (muteV != null) {
            muteV.setSelected(muted);
        }

        View handFreeV = mButtonContainer.findViewById(R.id.rc_voip_handfree);
        if (handFreeV != null) {
            handFreeV.setSelected(true);
        }

//        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
//        if (audioManager.isWiredHeadsetOn() || BluetoothUtil.hasBluetoothA2dpConnected()) {
//            handFree = false;
//            RongCallClient.getInstance().setEnableSpeakerphone(false);
//            ImageView handFreeV = null;
//            if (null != mButtonContainer) {
//                handFreeV = mButtonContainer.findViewById(R.id.rc_voip_handfree_btn);
//            }
//            if (handFreeV != null) {
//                handFreeV.setSelected(false);
//                handFreeV.setEnabled(false);
//                handFreeV.setClickable(false);
//            }
//        } else {
//            RongCallClient.getInstance().setEnableSpeakerphone(handFree);
//            View handFreeV = mButtonContainer.findViewById(R.id.rc_voip_handfree);
//            if (handFreeV != null) {
//                handFreeV.setSelected(handFree);
//            }
//        }
//        stopRing();
    }

    public void hideVideoCallInformation() {
        isInformationShow = false;
        mUserInfoContainer.setVisibility(View.GONE);
        mButtonContainer.setVisibility(View.GONE);
        findViewById(R.id.rc_voip_audio_chat).setVisibility(View.GONE);
    }

    public void showVideoCallInformation() {
        isInformationShow = true;
        mUserInfoContainer.setVisibility(View.VISIBLE);

//        mUserInfoContainer.findViewById(R.id.rc_voip_call_minimize).setVisibility(View.VISIBLE);
        mButtonContainer.setVisibility(View.VISIBLE);
        RelativeLayout btnLayout = (RelativeLayout) inflater.inflate(R.layout.rc_voip_call_bottom_connected_button_layout, null);
        btnLayout.findViewById(R.id.rc_voip_call_mute).setSelected(muted);
        btnLayout.findViewById(R.id.rc_voip_handfree).setVisibility(View.GONE);
        btnLayout.findViewById(R.id.rc_voip_camera).setVisibility(View.VISIBLE);
        mButtonContainer.removeAllViews();
        mButtonContainer.addView(btnLayout);
        View view = findViewById(R.id.rc_voip_audio_chat);
        view.setVisibility(View.VISIBLE);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEnableCamera = !mEnableCamera;
                mCallSession.enableCamera(mEnableCamera);
            }
        });
    }

    public void onHangupBtnClick(View view) {
        mCallSession.hangup();
    }

    public void onReceiveBtnClick(View view) {
        mCallSession.accept();
    }

    public void onHandFreeButtonClick(View view) {
        mCallSession.setSpeakerEnable(!view.isSelected());
//        RongCallClient.getInstance().setEnableSpeakerphone(!view.isSelected());//true:打开免提 false:关闭免提
        view.setSelected(!view.isSelected());
//        handFree = view.isSelected();
    }

    public void onMuteButtonClick(View view) {
        mCallSession.muteMicrophone(!view.isSelected());
//        RongCallClient.getInstance().setEnableLocalAudio(view.isSelected());
        view.setSelected(!view.isSelected());
//        muted = view.isSelected();
    }

    public void onMinimizeClick(View view) {
        return;
    }

    @Override
    public void onCallFinish(CallConst.CallFinishReason finishReason) {

        String senderId;
        String extra = "";

//        if (callSession == null) {
//            RLog.e(TAG, "onCallDisconnected. callSession is null!");
//            postRunnableDelay(new Runnable() {
//                @Override
//                public void run() {
//                    finish();
//                }
//            });
//            return;
//        }
//        senderId = mCallSession.getInviter();
//        senderId = callSession.getInviterUserId();
//        switch (reason) {
//            case HANGUP:
//            case REMOTE_HANGUP:
//                long time = getTime();
//                if (time > 0) {
//                    if (time >= 3600) {
//                        extra = String.format("%d:%02d:%02d", time / 3600, (time % 3600) / 60, (time % 60));
//                    } else {
//                        extra = String.format("%02d:%02d", (time % 3600) / 60, (time % 60));
//                    }
//                } else {
//                    extra = reason == HANGUP ? getResources().getString(R.string.rc_voip_mo_reject) : getResources().getString(R.string.rc_voip_mt_reject);
//                }
//                break;
//            case OTHER_DEVICE_HAD_ACCEPTED:
//                extra = getString(R.string.rc_voip_call_other);
//                break;
//            default:
//                break;
//        }
//        cancelTime();
//
//        if (!TextUtils.isEmpty(senderId)) {
//            CallSTerminateMessage message = new CallSTerminateMessage();
//            message.setReason(reason);
//            message.setMediaType(callSession.getMediaType());
//            message.setExtra(extra);
//            long serverTime = System.currentTimeMillis() - RongIMClient.getInstance().getDeltaTime();
//            if (senderId.equals(callSession.getSelfUserId())) {
//                message.setDirection("MO");
//                RongIM.getInstance().insertOutgoingMessage(Conversation.ConversationType.PRIVATE, callSession.getTargetId(), io.rong.imlib.model.Message.SentStatus.SENT, message, serverTime, null);
//            } else {
//                message.setDirection("MT");
//                io.rong.imlib.model.Message.ReceivedStatus receivedStatus = new io.rong.imlib.model.Message.ReceivedStatus(0);
//                RongIM.getInstance().insertIncomingMessage(Conversation.ConversationType.PRIVATE, callSession.getTargetId(), senderId, receivedStatus, message, serverTime, null);
//            }
//        }
        Handler mainHandler = new Handler(Looper.getMainLooper());
        mainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 200);
    }

    @Override
    public void onErrorOccur(CallConst.CallErrorCode errorCode) {

    }

    @Override
    public void onUsersInvite(String inviterId, List<String> userIdList) {

    }

    @Override
    public void onUsersConnect(List<String> userIdList) {
        mConnectionStateTextView.setVisibility(View.GONE);
        if (mCallSession.getMediaType() == CallConst.CallMediaType.VIDEO) {
            findViewById(R.id.rc_voip_call_information).setBackgroundColor(getResources().getColor(android.R.color.transparent));
            mLPreviewContainer.setVisibility(View.VISIBLE);
            mCallSession.setVideoView(mCallSession.getMembers().get(0).getUserInfo().getUserId(), mLPreviewContainer);
            mLPreviewContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isInformationShow) {
                        hideVideoCallInformation();
                    } else {
                        showVideoCallInformation();
                        handler.sendEmptyMessageDelayed(EVENT_FULL_SCREEN, 5 * 1000);
                    }
                }
            });
            mSPreviewContainer.setVisibility(View.VISIBLE);
            mCallSession.setVideoView(JIM.getInstance().getCurrentUserId(), mSPreviewContainer);

            /** 小窗口点击事件 **/
            mSPreviewContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String currentId = JIM.getInstance().getCurrentUserId();
                    UserInfo currentUserInfo = JIM.getInstance().getUserInfoManager().getUserInfo(currentId);
                    CallMember member = mCallSession.getMembers().get(0);
                    UserInfo remoteUserInfo = member.getUserInfo();
                    UserInfo mainUser;
                    UserInfo subUser;
                    if (mSwitchMainSubVideo) {
                        mainUser = remoteUserInfo;
                        subUser = currentUserInfo;
                    } else {
                        mainUser = currentUserInfo;
                        subUser = remoteUserInfo;
                    }
                    mSwitchMainSubVideo = !mSwitchMainSubVideo;
                    String name = "";
                    if (mainUser != null) {
                        if (!TextUtils.isEmpty(mainUser.getUserName())) {
                            name = mainUser.getUserName();
                        } else {
                            name = mainUser.getUserId();
                        }
                    }

                    TextView userName = mUserInfoContainer.findViewById(R.id.rc_voip_user_name);
                    userName.setLines(1);
                    userName.setEllipsize(TextUtils.TruncateAt.END);
                    userName.setText(name);
                    assert mainUser != null;
                    mCallSession.setVideoView(mainUser.getUserId(), mLPreviewContainer);
                    mCallSession.setVideoView(subUser.getUserId(), mSPreviewContainer);
                }
            });
            mButtonContainer.setVisibility(View.GONE);
            mUserInfoContainer.setVisibility(View.GONE);
        }
    }

    @Override
    public void onUsersLeave(List<String> userIdList) {

    }

    @Override
    public void onUserCameraEnable(String userId, boolean enable) {

    }

    @Override
    public void onUserMicrophoneEnable(String userId, boolean enable) {
        Log.i("SingleCallActivity", "onUserMicrophoneEnable");
    }

    @Override
    public void onSoundLevelUpdate(HashMap<String, Float> soundLevels) {
        Log.i("SingleCallActivity", "onSoundLevelUpdate start");
        for (Map.Entry<String, Float> entry : soundLevels.entrySet()) {
            Log.i("SingleCallActivity", "onSoundLevelUpdate userId is " + entry.getKey() + ", value is " + entry.getValue());
        }
        Log.i("SingleCallActivity", "onSoundLevelUpdate end");
    }

    @Override
    public void onVideoFirstFrameRender(String userId) {
        Log.i("SingleCallActivity", "onVideoFirstFrameRender userId is " + userId);
    }

    public void onSwitchCameraClick(View view) {
        mUseFrontCamera = !mUseFrontCamera;
        mCallSession.useFrontCamera(mUseFrontCamera);
    }
}
