package com.juggle.chat.contacts.group;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.jet.im.kit.SendbirdUIKit;
import com.jet.im.kit.widgets.StatusFrameView;
import com.juggle.chat.R;
import com.juggle.chat.databinding.ActivityGroupManageBinding;


public class GroupManageActivity extends AppCompatActivity {
    private final static String GROUP_ID = "groupId";
    private String mGroupId;
    private StatusFrameView mStatusFrameView;

    public static Intent newIntent(@NonNull Context context, String groupId) {
        Intent intent = new Intent(context, GroupManageActivity.class);
        intent.putExtra(GROUP_ID, groupId);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGroupId = getIntent().getStringExtra(GROUP_ID);

        ActivityGroupManageBinding binding = ActivityGroupManageBinding.inflate(getLayoutInflater());
        binding.headerView.getTitleTextView().setText(getString(R.string.text_group_management));
        binding.headerView.setLeftButtonImageResource(com.jet.im.kit.R.drawable.icon_arrow_left);
        binding.headerView.setLeftButtonTint(SendbirdUIKit.getDefaultThemeMode().getPrimaryTintColorStateList(this));
        binding.headerView.setOnLeftButtonClickListener(v -> finish());
        binding.headerView.setUseRightButton(false);

        binding.sivChangeOwner.setOnClickListener(v -> {

        });
        binding.sivGroupMute.setSwitchClickable(false);
        binding.sivGroupMute.setOnClickListener(v -> {

        });
        binding.sivDisplayHistory.setSwitchClickable(false);
        binding.sivDisplayHistory.setOnClickListener(v -> {

        });

        final FrameLayout innerContainer = new FrameLayout(this);
        innerContainer.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mStatusFrameView = new StatusFrameView(this, null, com.jet.im.kit.R.attr.sb_component_status);
        binding.getRoot().addView(innerContainer);
        innerContainer.addView(mStatusFrameView);

        setContentView(binding.getRoot());
    }
}
