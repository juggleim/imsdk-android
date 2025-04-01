package com.juggle.chat.contacts.group;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.jet.im.kit.SendbirdUIKit;
import com.jet.im.kit.activities.ChannelActivity;
import com.jet.im.kit.utils.TextUtils;
import com.juggle.chat.R;
import com.juggle.chat.bean.HttpResult;
import com.juggle.chat.databinding.ActivityJoinGroupBinding;
import com.juggle.chat.http.CustomCallback;
import com.juggle.chat.http.ServiceManager;
import com.juggle.chat.utils.ToastUtils;
import com.juggle.im.model.Conversation;

import java.util.HashMap;

import okhttp3.RequestBody;

public class JoinGroupActivity extends AppCompatActivity {
    private final static String GROUP_ID = "groupId";
    private final static String PORTRAIT = "portrait";
    private final static String NAME = "name";
    private final static String NUMBER = "number";

    private String mGroupId;
    private String mPortrait;
    private String mName;
    private int mNumber;

    public static Intent newIntent(@NonNull Context context, String groupId, String portrait, String name, int number) {
        Intent intent = new Intent(context, JoinGroupActivity.class);
        intent.putExtra(GROUP_ID, groupId);
        intent.putExtra(PORTRAIT, portrait);
        intent.putExtra(NAME, name);
        intent.putExtra(NUMBER, number);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityJoinGroupBinding binding = ActivityJoinGroupBinding.inflate(getLayoutInflater());
        binding.headerView.getTitleTextView().setText(getString(com.jet.im.kit.R.string.text_group_detail));
        binding.headerView.setLeftButtonImageResource(com.jet.im.kit.R.drawable.icon_arrow_left);
        binding.headerView.setLeftButtonTint(SendbirdUIKit.getDefaultThemeMode().getPrimaryTintColorStateList(this));
        binding.headerView.setOnLeftButtonClickListener(v -> finish());
        binding.headerView.setUseRightButton(false);

        mGroupId = getIntent().getStringExtra(GROUP_ID);
        mPortrait = getIntent().getStringExtra(PORTRAIT);
        mName = getIntent().getStringExtra(NAME);
        mNumber = getIntent().getIntExtra(NUMBER, 0);

        if (TextUtils.isNotEmpty(mPortrait)) {
            Glide.with(this)
                    .load(mPortrait)
                    .into(binding.profileIvJoinGroupPortrait);
        } else {
            Glide.with(this)
                    .load(R.drawable.icon_default_group)
                    .into(binding.profileIvJoinGroupPortrait);
        }
        binding.profileTvJoinGroupName.setText(mName);
        binding.profileTvJoinGroupMember.setText(getString(R.string.text_group_has_members, mNumber));

        binding.profileBtnJoinGroupConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("group_id", mGroupId);
                RequestBody body = ServiceManager.createJsonRequest(map);
                ServiceManager.getGroupsService().applyGroup(body).enqueue(new CustomCallback<HttpResult<Void>, Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        startActivity(ChannelActivity.newIntent(JoinGroupActivity.this, Conversation.ConversationType.GROUP.getValue(), mGroupId));
                        finish();
                    }

                    @Override
                    public void onError(Throwable t) {
                        super.onError(t);
                        ToastUtils.show("加入失败");
                    }
                });
            }
        });

        setContentView(binding.getRoot());
    }
}
