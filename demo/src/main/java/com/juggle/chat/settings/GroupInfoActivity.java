package com.juggle.chat.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.jet.im.kit.R;
import com.jet.im.kit.SendbirdUIKit;
import com.jet.im.kit.widgets.StatusFrameView;
import com.jet.im.kit.widgets.WrapHeightGridView;
import com.juggle.chat.bean.GroupDetailBean;
import com.juggle.chat.bean.GroupMemberBean;
import com.juggle.chat.bean.HttpResult;
import com.juggle.chat.databinding.ActivityGroupInfoBinding;
import com.juggle.chat.http.CustomCallback;
import com.juggle.chat.http.ServiceManager;
import com.juggle.im.JIM;
import com.juggle.im.interfaces.IConversationManager;
import com.juggle.im.model.Conversation;
import com.juggle.im.model.ConversationInfo;
import com.juggle.im.model.GroupInfo;

public class GroupInfoActivity extends AppCompatActivity {
    private final static String GROUP_ID = "groupId";
    private final int SHOW_GROUP_MEMBER_LIMIT = 30;
    private String mGroupId;
    private ConversationInfo mConversationInfo;
    private GroupDetailBean mGroupDetailBean;
    private ActivityGroupInfoBinding mBinding;
    private StatusFrameView mStatusFrameView;
    private GridGroupMemberAdapter mMemberAdapter;

    public static Intent newIntent(@NonNull Context context, String groupId) {
        Intent intent = new Intent(context, GroupInfoActivity.class);
        intent.putExtra(GROUP_ID, groupId);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGroupId = getIntent().getStringExtra(GROUP_ID);

        mBinding = ActivityGroupInfoBinding.inflate(getLayoutInflater());
        mBinding.headerView.getTitleTextView().setText(getString(R.string.text_group_detail));
        mBinding.headerView.setLeftButtonImageResource(R.drawable.icon_arrow_left);
        mBinding.headerView.setLeftButtonTint(SendbirdUIKit.getDefaultThemeMode().getPrimaryTintColorStateList(this));
        mBinding.headerView.setOnLeftButtonClickListener(v -> finish());

        WrapHeightGridView groupMemberGv = mBinding.profileGvGroupMember;
        mMemberAdapter = new GridGroupMemberAdapter(this, SHOW_GROUP_MEMBER_LIMIT);
        mMemberAdapter.setAllowAddMember(true);
        groupMemberGv.setAdapter(mMemberAdapter);
        mMemberAdapter.setOnItemClickedListener(new GridGroupMemberAdapter.OnItemClickedListener() {
            @Override
            public void onAddOrDeleteMemberClicked(boolean isAdd) {
                //todo: GroupInfo
//                memberManage(isAdd);
            }

            @Override
            public void onMemberClicked(GroupMemberBean groupMember) {
                showMemberInfo(groupMember);
            }
        });

        mBinding.profileSivMessageNotice.setSwitchClickable(false);
        mBinding.profileSivMessageNotice.setOnClickListener(v -> {
            mute(!mBinding.profileSivMessageNotice.isChecked());
        });
        mBinding.profileSivSetTop.setSwitchClickable(false);
        mBinding.profileSivSetTop.setOnClickListener(v -> {
            setTop(!mBinding.profileSivSetTop.isChecked());
        });

        final FrameLayout innerContainer = new FrameLayout(this);
        innerContainer.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mStatusFrameView = new StatusFrameView(this, null, com.jet.im.kit.R.attr.sb_component_status);
        mBinding.getRoot().addView(innerContainer);
        innerContainer.addView(mStatusFrameView);

        setContentView(mBinding.getRoot());
    }

    @Override
    protected void onResume() {
        super.onResume();
        Conversation conversation = new Conversation(Conversation.ConversationType.GROUP, mGroupId);
        mConversationInfo = JIM.getInstance().getConversationManager().getConversationInfo(conversation);
        loadGroupInfo();
    }

    private void loadGroupInfo() {
        ServiceManager.getGroupsService().getGroupDetail(mGroupId).enqueue(new CustomCallback<HttpResult<GroupDetailBean>, GroupDetailBean>() {
            @Override
            public void onSuccess(GroupDetailBean groupDetailBean) {
                mGroupDetailBean = groupDetailBean;
                updateView();
            }
        });
    }

    private void updateView() {
        mMemberAdapter.updateListView(mGroupDetailBean.getMembers());

        mBinding.profileSivAllGroupMember.setValue(String.valueOf(mGroupDetailBean.getMemberCount()));

//        Glide.with(this)
//                .load(mGroupDetailBean.getPortrait())
//                .apply(RequestOptions.bitmapTransform(new CircleCrop()))
//                .into(mBinding.profileSivGroupPortraitContainer.getRightImageView());

        mBinding.profileSivGroupNameContainer.setValue(mGroupDetailBean.getGroupName());
        mBinding.profileSivGroupNickname.setValue(mGroupDetailBean.getGroupDisplayName());
        mBinding.profileSivMessageNotice.setChecked(mConversationInfo.isMute());
        mBinding.profileSivSetTop.setChecked(mConversationInfo.isTop());


    }

    private void mute(boolean isMute) {
        mStatusFrameView.setStatus(StatusFrameView.Status.LOADING);
        JIM.getInstance().getConversationManager().setMute(mConversationInfo.getConversation(), isMute, new IConversationManager.ISimpleCallback() {
            @Override
            public void onSuccess() {
                mStatusFrameView.setStatus(StatusFrameView.Status.NONE);
                mBinding.profileSivMessageNotice.setChecked(isMute);
                mConversationInfo.setMute(isMute);
            }

            @Override
            public void onError(int errorCode) {
                mStatusFrameView.setStatus(StatusFrameView.Status.NONE);
                Toast.makeText(GroupInfoActivity.this, com.juggle.chat.R.string.text_operation_fail, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setTop(boolean isTop) {
        mStatusFrameView.setStatus(StatusFrameView.Status.LOADING);
        JIM.getInstance().getConversationManager().setTop(mConversationInfo.getConversation(), isTop, new IConversationManager.ISimpleCallback() {
            @Override
            public void onSuccess() {
                mStatusFrameView.setStatus(StatusFrameView.Status.NONE);
                mBinding.profileSivSetTop.setChecked(isTop);
                mConversationInfo.setTop(isTop);
            }

            @Override
            public void onError(int errorCode) {
                mStatusFrameView.setStatus(StatusFrameView.Status.NONE);
                Toast.makeText(GroupInfoActivity.this, com.juggle.chat.R.string.text_operation_fail, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showMemberInfo(GroupMemberBean member) {
        Intent intent = UserDetailActivity.newIntent(this, member);
        startActivity(intent);
    }
}
