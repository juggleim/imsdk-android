package com.juggle.chat.contacts.group;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jet.im.kit.SendbirdUIKit;
import com.jet.im.kit.activities.BaseActivity;
import com.jet.im.kit.widgets.StatusFrameView;
import com.juggle.chat.R;
import com.juggle.chat.bean.HttpResult;
import com.juggle.chat.databinding.ActivityGroupManageBinding;
import com.juggle.chat.http.CustomCallback;
import com.juggle.chat.http.ServiceManager;

import java.util.HashMap;

import okhttp3.RequestBody;


public class GroupManageActivity extends BaseActivity {
    private final static String GROUP_ID = "groupId";
    private final static String GROUP_MUTE = "groupMute";
    private final static String DISPLAY_HISTORY = "displayHistory";
    private String mGroupId;
    private boolean mGroupMute;
    private boolean mDisplayHistory;
    private StatusFrameView mStatusFrameView;
    private ActivityGroupManageBinding mBinding;

    public static Intent newIntent(@NonNull Context context, String groupId, boolean groupMute, boolean displayHistory) {
        Intent intent = new Intent(context, GroupManageActivity.class);
        intent.putExtra(GROUP_ID, groupId);
        intent.putExtra(GROUP_MUTE, groupMute);
        intent.putExtra(DISPLAY_HISTORY, displayHistory);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGroupId = getIntent().getStringExtra(GROUP_ID);
        mGroupMute = getIntent().getBooleanExtra(GROUP_MUTE, false);
        mDisplayHistory = getIntent().getBooleanExtra(DISPLAY_HISTORY, false);

        mBinding = ActivityGroupManageBinding.inflate(getLayoutInflater());
        mBinding.headerView.getTitleTextView().setText(getString(R.string.text_group_management));
        mBinding.headerView.setLeftButtonImageResource(com.jet.im.kit.R.drawable.icon_arrow_left);
        mBinding.headerView.setLeftButtonTint(SendbirdUIKit.getDefaultThemeMode().getPrimaryTintColorStateList(this));
        mBinding.headerView.setOnLeftButtonClickListener(v -> finish());
        mBinding.headerView.setUseRightButton(false);

        mBinding.sivChangeOwner.setOnClickListener(v -> {
            changeOwner();
        });
        mBinding.sivGroupMute.setChecked(mGroupMute);
        mBinding.sivGroupMute.setSwitchClickable(false);
        mBinding.sivGroupMute.setOnClickListener(v -> {
            setGroupMute();
        });
        mBinding.sivDisplayHistory.setChecked(mDisplayHistory);
        mBinding.sivDisplayHistory.setSwitchClickable(false);
        mBinding.sivDisplayHistory.setOnClickListener(v -> {
            setHistory();
        });

        final FrameLayout innerContainer = new FrameLayout(this);
        innerContainer.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mStatusFrameView = new StatusFrameView(this, null, com.jet.im.kit.R.attr.sb_component_status);
        mBinding.getRoot().addView(innerContainer);
        innerContainer.addView(mStatusFrameView);

        setContentView(mBinding.getRoot());
    }

    private void setGroupMute() {
        mStatusFrameView.setStatus(StatusFrameView.Status.LOADING);
        HashMap<String, Object> map = new HashMap<>();
        map.put("group_id", mGroupId);
        map.put("is_mute", mGroupMute ? 0 : 1);
        RequestBody body = ServiceManager.createJsonRequest(map);
        ServiceManager.getGroupsService().setMute(body).enqueue(new CustomCallback<HttpResult<Object>, Object>() {
            @Override
            public void onSuccess(Object o) {
                mStatusFrameView.setStatus(StatusFrameView.Status.NONE);
                mGroupMute = !mGroupMute;
                mBinding.sivGroupMute.setChecked(mGroupMute);
            }
        });
    }

    private void setHistory() {
        mStatusFrameView.setStatus(StatusFrameView.Status.LOADING);
        HashMap<String, Object> map = new HashMap<>();
        map.put("group_id", mGroupId);
        map.put("group_his_msg_visible", mDisplayHistory ? 0 : 1);
        RequestBody body = ServiceManager.createJsonRequest(map);
        ServiceManager.getGroupsService().setHistoryMessageVisible(body).enqueue(new CustomCallback<HttpResult<Object>, Object>() {
            @Override
            public void onSuccess(Object o) {
                mStatusFrameView.setStatus(StatusFrameView.Status.NONE);
                mDisplayHistory = !mDisplayHistory;
                mBinding.sivDisplayHistory.setChecked(mDisplayHistory);
            }
        });
    }

    private void changeOwner() {
        Intent intent = GroupMemberListActivity.newIntent(this, mGroupId, 1);
        startActivityForResult(intent, 444);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 444) {
            finish();
        }
    }
}
