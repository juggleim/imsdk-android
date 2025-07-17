package com.jet.im.kit.call;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.jet.im.kit.R;
import com.juggle.im.call.CallConst;
import com.juggle.im.call.ICallSession;

import java.util.HashMap;
import java.util.List;

public class MultiVideoCallActivity extends BaseCallActivity implements ICallSession.ICallSessionListener {
//    private static final String TAG = "MultiVideoCallActivity";
//    private static final String REMOTE_FURFACEVIEW_TAG = "surfaceview";//
//    private static final String REMOTE_VIEW_TAG = "remoteview";//rc_voip_viewlet_remote_user tag
//    private static final String VOIP_USERNAME_TAG = "username";//topContainer.findViewById(R.id.rc_voip_user_name);
//    private static final String VOIP_PARTICIPANT_PORTAIT_CONTAINER_TAG = "participantPortraitView";//被叫方显示头像容器tag
//    SurfaceView localView;
//    ContainerLayout localViewContainer;
//    LinearLayout remoteViewContainer;
//    LinearLayout remoteViewContainer2;
//    LinearLayout topContainer;
//    LinearLayout waitingContainer;
//    LinearLayout bottomButtonContainer;
//    LinearLayout participantPortraitContainer;
//    LinearLayout portraitContainer1;
//    LayoutInflater inflater;
//    //通话中的最小化按钮、呼叫中的最小化按钮
//    ImageView minimizeButton, rc_voip_multiVideoCall_minimize;
//    ImageView moreButton;
//    ImageView switchCameraButton;
//    ImageView userPortrait;
//    LinearLayout infoLayout;
//    ImageView signalView;
//    TextView userNameView;
//    private RelativeLayout mRelativeWebView;
//    private int remoteUserViewWidth;
//    //    private int  remoteUserViewHeight;
//    //主叫、通话中 远端View
//    private float remoteUserViewMarginsRight = 10;
//    private float remoteUserViewMarginsLeft = 20;
//
//    boolean isFullScreen = false;
//    boolean isMuteMIC = false;
//    boolean isMuteCamera = false;
//    boolean startForCheckPermissions = false;
//
//    String localViewUserId;
//    private CallOptionMenu optionMenu;
//    ImageView muteButtion;
//    ImageView disableCameraButtion;
//    CallPromptDialog dialog = null;
//    RelativeLayout observerLayout;
//    private ImageView iv_large_preview_mutilvideo, iv_large_preview_Mask;
//    private String topUserName ="" ,topUserNameTag ="";
//    String groupId;
//
//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.rc_voip_multi_video_call);
//
//        groupId = getIntent().getStringExtra("groupId");
//        initViews();
//        setupIntent();
//    }
//
//    protected void initViews() {
//        inflater = LayoutInflater.from(this);
//        localViewContainer = (ContainerLayout) findViewById(R.id.rc_local_user_view);
//        remoteViewContainer = (LinearLayout) findViewById(R.id.rc_remote_user_container);
//        remoteViewContainer2 = (LinearLayout) findViewById(R.id.rc_remote_user_container_2);
//        topContainer = (LinearLayout) findViewById(R.id.rc_top_container);
//        topContainer.setVisibility(View.VISIBLE);
//        waitingContainer = (LinearLayout) findViewById(R.id.rc_waiting_container);
//        bottomButtonContainer = (LinearLayout) findViewById(R.id.rc_bottom_button_container);
//        participantPortraitContainer = (LinearLayout) findViewById(R.id.rc_participant_portait_container);
//        minimizeButton = (ImageView) findViewById(R.id.rc_voip_call_minimize);
//        rc_voip_multiVideoCall_minimize = (ImageView) findViewById(R.id.rc_voip_multiVideoCall_minimize);
//        userPortrait = (AsyncImageView) findViewById(R.id.rc_voip_user_portrait);
//        moreButton = (ImageView) findViewById(R.id.rc_voip_call_more);
//        switchCameraButton = (ImageView) findViewById(R.id.rc_voip_switch_camera);
//        progressDialog = new ProgressDialog(this);
//        progressDialog.setCancelable(false);
//        progressDialog.setMessage("白板加载中...");
//        mRelativeWebView = (RelativeLayout) findViewById(R.id.rc_whiteboard);
//        whiteboardView = new WebView(getApplicationContext());
//        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//        whiteboardView.setLayoutParams(params);
//        mRelativeWebView.addView(whiteboardView);
//        iv_large_preview_mutilvideo = (ImageView) findViewById(R.id.iv_large_preview_mutilvideo);
//        iv_large_preview_Mask = (ImageView) findViewById(R.id.iv_large_preview_Mask);
//        WebSettings settings = whiteboardView.getSettings();
//        settings.setJavaScriptEnabled(true);
//        settings.setUseWideViewPort(true);
//        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
//        settings.setLoadWithOverviewMode(true);
//        settings.setBlockNetworkImage(false);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
//        }
//
//        DisplayMetrics metrics = new DisplayMetrics();
//        getWindowManager().getDefaultDisplay().getMetrics(metrics);
//        remoteUserViewWidth = (metrics.widthPixels - 50) / 4;
//
//        localView = null;
//        localViewContainer.removeAllViews();
//        remoteViewContainer2.removeAllViews();
//
//        minimizeButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                MultiVideoCallActivity.super.onMinimizeClick(v);
//            }
//        });
//        rc_voip_multiVideoCall_minimize.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                MultiVideoCallActivity.super.onMinimizeClick(v);
//            }
//        });
//    }
//
    @Override
    public void onCallConnect() {

    }

    @Override
    public void onCallFinish(CallConst.CallFinishReason finishReason) {

    }

    @Override
    public void onErrorOccur(CallConst.CallErrorCode errorCode) {

    }

    @Override
    public void onUsersInvite(String inviterId, List<String> userIdList) {

    }

    @Override
    public void onUsersConnect(List<String> userIdList) {

    }

    @Override
    public void onUsersLeave(List<String> userIdList) {

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
}
