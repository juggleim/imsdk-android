package com.jet.im.kit.call;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.jet.im.kit.R;
import com.juggle.im.JIM;
import com.juggle.im.call.CallConst;
import com.juggle.im.call.ICallSession;

public class SingleCallActivity extends BaseCallActivity implements ICallSession.ICallSessionListener {
    private static final int LOSS_RATE_ALARM = 20;
    private LayoutInflater inflater;
    private ICallSession mCallSession;
    private FrameLayout mLPreviewContainer;
    private FrameLayout mSPreviewContainer;
    private FrameLayout mButtonContainer;
    private LinearLayout mUserInfoContainer;
    private TextView mConnectionStateTextView;
    private Boolean isInformationShow = false;
    private SurfaceView mLocalVideo = null;
    private boolean muted = false;
    private boolean handFree = false;
    private boolean startForCheckPermissions = false;
    private boolean isReceiveLost = false;
    private boolean isSendLost = false;
    private int EVENT_FULL_SCREEN = 1;
    private String targetId = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rc_voip_activity_single_call);
        mLPreviewContainer = findViewById(R.id.rc_voip_call_large_preview);
        mSPreviewContainer = findViewById(R.id.rc_voip_call_small_preview);
        mButtonContainer = findViewById(R.id.rc_voip_btn);
        mUserInfoContainer = findViewById(R.id.rc_voip_user_info);
        mConnectionStateTextView = findViewById(R.id.rc_tv_connection_state);

        Intent intent = getIntent();
        String callId = intent.getStringExtra("callId");
        mCallSession = JIM.getInstance().getCallManager().getCallSession(callId);
        if (mCallSession == null) {
            finish();
            return;
        }
        mCallSession.addListener("SingleCallActivity", this);

        inflater = LayoutInflater.from(this);
        initView();
    }

    private void initView() {
        RelativeLayout buttonLayout = (RelativeLayout) inflater.inflate(R.layout.rc_voip_call_bottom_connected_button_layout, null);
        RelativeLayout userInfoLayout = null;
        userInfoLayout = (RelativeLayout) inflater.inflate(R.layout.rc_voip_audio_call_user_info_incoming, null);
        userInfoLayout.findViewById(R.id.iv_large_preview_Mask).setVisibility(View.VISIBLE);

        if (mCallSession.getCallStatus() == CallConst.CallStatus.OUTGOING) {
            RelativeLayout layout = buttonLayout.findViewById(R.id.rc_voip_call_mute);
            layout.setVisibility(View.VISIBLE);
            ImageView button = buttonLayout.findViewById(R.id.rc_voip_call_mute_btn);
            button.setEnabled(false);
            buttonLayout.findViewById(R.id.rc_voip_handfree).setVisibility(View.VISIBLE);
        }

//        if (mediaType.equals(RongCallCommon.CallMediaType.AUDIO)) {
            findViewById(R.id.rc_voip_call_information).setBackgroundColor(getResources().getColor(R.color.rc_voip_background_color));
            mLPreviewContainer.setVisibility(View.GONE);
            mSPreviewContainer.setVisibility(View.GONE);

            if (mCallSession.getCallStatus() == CallConst.CallStatus.INCOMING) {
                buttonLayout = (RelativeLayout) inflater.inflate(R.layout.rc_voip_call_bottom_incoming_button_layout, null);
                ImageView iv_answerBtn =  buttonLayout.findViewById(R.id.rc_voip_call_answer_btn);
//                iv_answerBtn.setBackground(CallKitUtils.BackgroundDrawable(R.drawable.rc_voip_audio_answer_selector_new, SingleCallActivity.this));

                TextView callInfo =  userInfoLayout.findViewById(R.id.rc_voip_call_remind_info);
                textViewShadowLayer(callInfo, SingleCallActivity.this);
                callInfo.setText(R.string.rc_voip_audio_call_inviting);
//                onIncomingCallRinging();
            }
//        }
//        else if (mediaType.equals(RongCallCommon.CallMediaType.VIDEO)) {
//            if (callAction.equals(RongCallAction.ACTION_INCOMING_CALL)) {
//                findViewById(R.id.rc_voip_call_information).setBackgroundColor(getResources().getColor(R.color.rc_voip_background_color));
//                buttonLayout = (RelativeLayout) inflater.inflate(R.layout.rc_voip_call_bottom_incoming_button_layout, null);
//                ImageView iv_answerBtn = (ImageView) buttonLayout.findViewById(R.id.rc_voip_call_answer_btn);
//                iv_answerBtn.setBackground(CallKitUtils.BackgroundDrawable(R.drawable.rc_voip_vedio_answer_selector_new, SingleCallActivity.this));
//
//                TextView callInfo = (TextView) userInfoLayout.findViewById(R.id.rc_voip_call_remind_info);
//                CallKitUtils.textViewShadowLayer(callInfo, SingleCallActivity.this);
//                callInfo.setText(R.string.rc_voip_video_call_inviting);
//                onIncomingCallRinging();
//            }
//        }
        mButtonContainer.removeAllViews();
        mButtonContainer.addView(buttonLayout);
        mUserInfoContainer.removeAllViews();
        mUserInfoContainer.addView(userInfoLayout);
    }

    private void textViewShadowLayer(TextView text, Context context) {
        if (null == text) {
            return;
        }
        text.setShadowLayer(16F, 0F, 2F, context.getApplicationContext().getResources().getColor(R.color.callkit_shadowcolor));
    }

    @Override
    public void onCallConnect() {
//        if (callSession.getMediaType().equals(RongCallCommon.CallMediaType.AUDIO)) {
            findViewById(R.id.rc_voip_call_minimize).setVisibility(View.VISIBLE);
            RelativeLayout btnLayout = (RelativeLayout) inflater.inflate(R.layout.rc_voip_call_bottom_connected_button_layout, null);
            ImageView button = btnLayout.findViewById(R.id.rc_voip_call_mute_btn);
            button.setEnabled(true);
            mButtonContainer.removeAllViews();
            mButtonContainer.addView(btnLayout);
//        }
//        else {
//            mConnectionStateTextView.setVisibility(View.VISIBLE);
//            mConnectionStateTextView.setText(R.string.rc_voip_connecting);
//            // 二人视频通话接通后 mUserInfoContainer 中更换为无头像的布局
//            mUserInfoContainer.removeAllViews();
//            inflater.inflate(R.layout.rc_voip_video_call_user_info, mUserInfoContainer);
//            UserInfo userInfo = RongContext.getInstance().getUserInfoFromCache(targetId);
//            if (userInfo != null) {
//                TextView userName = mUserInfoContainer.findViewById(R.id.rc_voip_user_name);
//                userName.setText(userInfo.getName());
////                userName.setShadowLayer(16F, 0F, 2F, getResources().getColor(R.color.rc_voip_reminder_shadow));//callkit_shadowcolor
//                CallKitUtils.textViewShadowLayer(userName, SingleCallActivity.this);
//            }
//            mLocalVideo = localVideo;
//            mLocalVideo.setTag(callSession.getSelfUserId());
//        }
        TextView tv_rc_voip_call_remind_info =  mUserInfoContainer.findViewById(R.id.rc_voip_call_remind_info);
//        CallKitUtils.textViewShadowLayer(tv_rc_voip_call_remind_info, SingleCallActivity.this);
        tv_rc_voip_call_remind_info.setVisibility(View.GONE);
        TextView remindInfo;
//        if (callSession.getMediaType().equals(RongCallCommon.CallMediaType.AUDIO)) {
            remindInfo = mUserInfoContainer.findViewById(R.id.tv_setupTime);
//        } else {
//            remindInfo = mUserInfoContainer.findViewById(R.id.tv_setupTime_video);
//        }
        if (remindInfo == null) {
            remindInfo = tv_rc_voip_call_remind_info;
        }
//        setupTime(remindInfo);

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
}
