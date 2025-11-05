package com.jet.im.kit.call;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MultiVoiceCallActivity extends BaseCallActivity implements ICallSession.ICallSessionListener {
    private static final String TAG = "MultiVoiceCallActivity";
    LinearLayout audioContainer;
    ICallScrollView memberContainer;

    RelativeLayout incomingLayout;
    RelativeLayout outgoingLayout;
    RelativeLayout outgoingController;
    RelativeLayout incomingController;
    String groupId;

//    boolean shouldShowFloat = true;
//    boolean startForCheckPermissions = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rc_voip_ac_muti_audio);

        groupId = getIntent().getStringExtra("groupId");
        audioContainer = findViewById(R.id.rc_voip_container);
        incomingLayout = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.rc_voip_item_incoming_maudio, null);
        TextView tv_invite_incoming_audio = incomingLayout.findViewById(R.id.tv_invite_incoming_audio);
        CallUtils.textViewShadowLayer(tv_invite_incoming_audio, MultiVoiceCallActivity.this);

        outgoingLayout = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.rc_voip_item_outgoing_maudio, null);
        TextView rc_voip_remind = incomingLayout.findViewById(R.id.rc_voip_remind);
        CallUtils.textViewShadowLayer(rc_voip_remind, MultiVoiceCallActivity.this);

        outgoingController = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.rc_voip_call_bottom_connected_button_layout, null);
        ImageView button = outgoingController.findViewById(R.id.rc_voip_call_mute_btn);
        button.setEnabled(false);
        incomingController = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.rc_voip_call_bottom_incoming_button_layout, null);

//        startForCheckPermissions = getIntent().getBooleanExtra("checkPermissions", false);
        mCallSession.addListener(TAG, this);
        initView();
    }

    void initView() {
//        Intent intent = getIntent();
//        callAction = RongCallAction.valueOf(intent.getStringExtra("callAction"));
//        if (callAction == null || callAction.equals(RongCallAction.ACTION_RESUME_CALL)) {
//            RelativeLayout relativeLayout = (RelativeLayout) outgoingLayout.findViewById(R.id.reltive_voip_outgoing_audio_title);
//            relativeLayout.setVisibility(View.VISIBLE);
//            return;
//        }


        if (mCallSession.getCallStatus() == CallConst.CallStatus.INCOMING) {
            TextView name = incomingLayout.findViewById(R.id.rc_user_name);
            ImageView userPortrait = incomingLayout.findViewById(R.id.rc_voip_user_portrait);
            UserInfo userInfo = JIM.getInstance().getUserInfoManager().getUserInfo(mCallSession.getCallId());
            if (userInfo != null && userInfo.getUserName() != null)
                name.setText(userInfo.getUserName());
            else
                name.setText(mCallSession.getCallId());
            if (userInfo != null && userInfo.getPortrait() != null) {
                ViewUtils.drawProfile(userPortrait, userInfo.getPortrait(), userInfo.getPortrait());
                userPortrait.setVisibility(View.VISIBLE);
            }

            name.setTag(mCallSession.getCallId() + "callerName");
            audioContainer.addView(incomingLayout);
            memberContainer = audioContainer.findViewById(R.id.rc_voip_members_container_gridView);
//            SPUtils.put(MultiAudioCallActivity.this, "ICallScrollView", "CallUserGridView");

            memberContainer.setChildPortraitSize(memberContainer.dip2pix(55));
            List<CallMember> list = mCallSession.getMembers();
            for (CallMember member : list) {
                if (!member.getUserInfo().getUserId().equals(mCallSession.getInviter())) {
                    userInfo = member.getUserInfo();
                    memberContainer.addChild(userInfo.getUserId(), userInfo);
                }
            }
            FrameLayout controller = audioContainer.findViewById(R.id.rc_voip_control_layout);
            controller.addView(incomingController);

            ImageView iv_answerBtn = incomingController.findViewById(R.id.rc_voip_call_answer_btn);
            iv_answerBtn.setBackground(CallUtils.BackgroundDrawable(R.drawable.rc_voip_audio_answer_selector_new, MultiVoiceCallActivity.this));

//            onIncomingCallRinging();
        } else {
            List<String> userIds = new ArrayList<>();
            for (CallMember member : mCallSession.getMembers()) {
                userIds.add(member.getUserInfo().getUserId());
            }
            audioContainer.addView(outgoingLayout);


            LinearLayout linear_scrollviewTag = outgoingLayout.findViewById(R.id.linear_scrollviewTag);


            //多人语音主叫方顶部布局
            RelativeLayout relativeLayout = outgoingLayout.findViewById(R.id.reltive_voip_outgoing_audio_title);
            relativeLayout.setVisibility(View.VISIBLE);

            memberContainer = audioContainer.findViewById(R.id.rc_voip_members_container);
//            SPUtils.put(MultiAudioCallActivity.this, "ICallScrollView", "CallVerticalScrollView");
            memberContainer.enableShowState(true);
            FrameLayout controller = audioContainer.findViewById(R.id.rc_voip_control_layout);
            controller.addView(outgoingController);

            ImageView iv_answerBtn = incomingController.findViewById(R.id.rc_voip_call_answer_btn);
            iv_answerBtn.setBackground(CallUtils.BackgroundDrawable(R.drawable.rc_voip_audio_answer_selector_new, MultiVoiceCallActivity.this));

            ImageView button = outgoingController.findViewById(R.id.rc_voip_call_mute_btn);
            button.setEnabled(false);
            for (int i = 0; i < userIds.size(); i++) {
                if (!userIds.get(i).equals(JIM.getInstance().getCurrentUserId())) {
                    UserInfo userInfo = JIM.getInstance().getUserInfoManager().getUserInfo(userIds.get(i));
                    memberContainer.addChild(userIds.get(i), userInfo, getString(R.string.rc_voip_call_connecting));
                }
            }
            //
            if (userIds.size() > 4) {
                ViewGroup.LayoutParams params = linear_scrollviewTag.getLayoutParams();
                params.height = CallUtils.dp2px(200, MultiVoiceCallActivity.this);
                linear_scrollviewTag.setLayoutParams(params);
            }
        }
        memberContainer.setScrollViewOverScrollMode(View.OVER_SCROLL_NEVER);
//        createPickupDetector();

//        if (callAction.equals(RongCallAction.ACTION_INCOMING_CALL)) {
//            regisHeadsetPlugReceiver();
//            if (BluetoothUtil.hasBluetoothA2dpConnected() || BluetoothUtil.isWiredHeadsetOn(MultiAudioCallActivity.this)) {
//                HeadsetInfo headsetInfo = new HeadsetInfo(true, HeadsetInfo.HeadsetType.BluetoothA2dp);
//                onEventMainThread(headsetInfo);
//            }
//        }
    }

    @Override
    public void onCallConnect() {
        if (!mCallSession.getOwner().equals(JIM.getInstance().getCurrentUserId())) {
            audioContainer.removeAllViews();
            FrameLayout controller = (FrameLayout) outgoingLayout.findViewById(R.id.rc_voip_control_layout);
            controller.addView(outgoingController);
            audioContainer.addView(outgoingLayout);
            //多人语音通话中竖向滑动
            memberContainer = (CallVerticalScrollView) outgoingLayout.findViewById(R.id.rc_voip_members_container);
            memberContainer.enableShowState(true);
            LinearLayout linear_scrollviewTag = (LinearLayout) outgoingLayout.findViewById(R.id.linear_scrollviewTag);
            if (mCallSession.getMembers().size() > 4) {
                ViewGroup.LayoutParams params = linear_scrollviewTag.getLayoutParams();
                params.height = CallUtils.dp2px(200, MultiVoiceCallActivity.this);
                linear_scrollviewTag.setLayoutParams(params);
            }
            for (CallMember member : mCallSession.getMembers()) {
                if (!member.getUserInfo().getUserId().equals(JIM.getInstance().getCurrentUserId())) {
                    UserInfo userInfo = member.getUserInfo();
                    String state = member.getCallStatus() == CallConst.CallStatus.CONNECTED ? null : getString(R.string.rc_voip_call_connecting);
                    memberContainer.addChild(member.getUserInfo().getUserId(), userInfo, state);
                }
            }
        }

        outgoingLayout.findViewById(R.id.rc_voip_remind).setVisibility(View.GONE);
        outgoingLayout.findViewById(R.id.rc_voip_handfree).setVisibility(View.VISIBLE);
        ImageView button = outgoingController.findViewById(R.id.rc_voip_call_mute_btn);
        button.setEnabled(true);
        outgoingLayout.findViewById(R.id.rc_voip_call_mute).setVisibility(View.VISIBLE);
        //多人语音主叫方顶部布局
        RelativeLayout relativeLayout = (RelativeLayout) outgoingLayout.findViewById(R.id.reltive_voip_outgoing_audio_title);
        relativeLayout.setVisibility(View.GONE);

        View muteV = outgoingLayout.findViewById(R.id.rc_voip_call_mute_btn);
        muteV.setVisibility(View.VISIBLE);
        muteV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallSession.muteMicrophone(!v.isSelected());
                v.setSelected(!v.isSelected());
            }
        });

        View handfreeV = outgoingLayout.findViewById(R.id.rc_voip_handfree_btn);
        handfreeV.setSelected(true);
        handfreeV.setVisibility(View.VISIBLE);
        handfreeV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallSession.setSpeakerEnable(!v.isSelected());
                v.setSelected(!v.isSelected());
            }
        });

        outgoingLayout.findViewById(R.id.rc_voip_title).setVisibility(View.VISIBLE);
        TextView timeV = outgoingLayout.findViewById(R.id.rc_voip_time);
        setupTime(timeV);

        View imgvAdd = outgoingLayout.findViewById(R.id.rc_voip_add_btn);
        imgvAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("com.jet.im.action.select_group_member");
                intent.putExtra("groupId", groupId);
                intent.putExtra("type", 3);

                startActivityForResult(intent, 777);

//                 if (callSession.getConversationType().equals(Conversation.ConversationType.GROUP)) {
//                    Intent intent = new Intent(MultiAudioCallActivity.this, CallSelectMemberActivity.class);
//                    ArrayList<String> added = new ArrayList<>();
//                    List<CallUserProfile> list = RongCallClient.getInstance().getCallSession().getParticipantProfileList();
//                    for (CallUserProfile profile : list) {
//                        added.add(profile.getUserId());
//                    }
//                    ArrayList<String> allObserver = (ArrayList<String>) RongCallClient.getInstance().getCallSession().getObserverUserList();
//                    intent.putStringArrayListExtra("allObserver", allObserver);
//                    intent.putStringArrayListExtra("invitedMembers", added);
//                    intent.putExtra("conversationType", callSession.getConversationType().getValue());
//                    intent.putExtra("groupId", callSession.getTargetId());
//                    intent.putExtra("mediaType", RongCallCommon.CallMediaType.AUDIO.getValue());
//                    startActivityForResult(intent, REQUEST_CODE_ADD_MEMBER);
//                } else {
//                    ArrayList<String> added = new ArrayList<>();
//                    List<CallUserProfile> list = RongCallClient.getInstance().getCallSession().getParticipantProfileList();
//                    for (CallUserProfile profile : list) {
//                        added.add(profile.getUserId());
//                    }
//                    addMember(added);
//                }
            }
        });
        outgoingLayout.findViewById(R.id.rc_voip_minimize_outgoing).setVisibility(View.VISIBLE);
        View minimizeV = outgoingLayout.findViewById(R.id.rc_voip_minimize);
        minimizeV.setVisibility(View.GONE);
        minimizeV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("audioTag", "************ outgoingLayout.findViewById(R.id.rc_voip_minimize)*****************");
//                MultiAudioCallActivity.super.onMinimizeClick(v);
            }
        });
    }

    @Override
    public void onCallFinish(CallConst.CallFinishReason finishReason) {
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
        for (String userId : userIdList) {
            memberContainer.addChild(userId, JIM.getInstance().getUserInfoManager().getUserInfo(userId), getString(R.string.rc_voip_call_connecting));
        }
    }

    @Override
    public void onUsersConnect(List<String> userIdList) {
        for (String userId : userIdList) {
            View view = memberContainer.findChildById(userId);
            if (view != null) {
                memberContainer.updateChildState(userId, false);
            } else {
                memberContainer.addChild(userId, JIM.getInstance().getUserInfoManager().getUserInfo(userId));
            }
        }
    }

    @Override
    public void onUsersLeave(List<String> userIdList) {
        String text = "";
//        switch (reason) {
//            case REMOTE_BUSY_LINE:
//                text = getString(R.string.rc_voip_mt_busy);
//                break;
//            case REMOTE_CANCEL:
//                text = getString(R.string.rc_voip_mt_cancel);
//                break;
//            case REMOTE_REJECT:
//                text = getString(R.string.rc_voip_mt_reject);
//                break;
//            case NO_RESPONSE:
//                text = getString(R.string.rc_voip_mt_no_response);
//                break;
//            case NETWORK_ERROR:
//            case HANGUP:
//            case REMOTE_HANGUP:
//                break;
//        }
        for (String userId : userIdList) {
            if (memberContainer != null) {
                memberContainer.updateChildState(userId, text);
            }
            if (memberContainer != null)
                memberContainer.removeChild(userId);
        }
    }

    @Override
    public void onUserCameraEnable(String userId, boolean enable) {

    }

    @Override
    public void onUserMicrophoneEnable(String userId, boolean enable) {

    }

    @Override
    public void onSoundLevelUpdate(HashMap<String, Float> soundLevels) {

    }

    @Override
    public void onVideoFirstFrameRender(String userId) {

    }

    public void onHangupBtnClick(View view) {
        Log.i(TAG, "onHangupBtnClick");
        mCallSession.hangup();
    }

    public void onReceiveBtnClick(View view) {
        Log.i(TAG, "onReceiveBtnClick");
        mCallSession.accept();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 777) {
            if (data == null) {
                return;
            }
            List<String> userIdList = data.getStringArrayListExtra("userIdList");
            mCallSession.inviteUsers(userIdList);
        }
    }
}
