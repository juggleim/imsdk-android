package com.juggle.chat.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.jet.im.kit.utils.DialogUtils;
import com.jet.im.kit.widgets.StatusFrameView;
import com.jet.im.kit.widgets.WrapHeightGridView;
import com.juggle.chat.bean.GroupDetailBean;
import com.juggle.chat.bean.GroupMemberBean;
import com.juggle.chat.bean.HttpResult;
import com.juggle.chat.contacts.group.GroupAnnouncementActivity;
import com.juggle.chat.contacts.group.GroupMemberListActivity;
import com.juggle.chat.contacts.group.GroupNameActivity;
import com.juggle.chat.contacts.group.GroupNicknameActivity;
import com.juggle.chat.contacts.group.select.SelectGroupMemberActivity;
import com.juggle.chat.databinding.ActivityGroupInfoBinding;
import com.juggle.chat.http.CustomCallback;
import com.juggle.chat.http.ServiceManager;
import com.juggle.im.JIM;
import com.juggle.im.interfaces.IConversationManager;
import com.juggle.im.interfaces.IMessageManager;
import com.juggle.im.model.Conversation;
import com.juggle.im.model.ConversationInfo;

import java.util.HashMap;

import okhttp3.RequestBody;

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
        mBinding.headerView.setUseRightButton(false);

        WrapHeightGridView groupMemberGv = mBinding.profileGvGroupMember;
        mMemberAdapter = new GridGroupMemberAdapter(this, SHOW_GROUP_MEMBER_LIMIT);
        mMemberAdapter.setAllowAddMember(true);
        groupMemberGv.setAdapter(mMemberAdapter);
        mMemberAdapter.setOnItemClickedListener(new GridGroupMemberAdapter.OnItemClickedListener() {
            @Override
            public void onAddOrDeleteMemberClicked(boolean isAdd) {
                memberManage(isAdd);
            }

            @Override
            public void onMemberClicked(GroupMemberBean groupMember) {
                showMemberInfo(groupMember);
            }
        });

        mBinding.profileSivAllGroupMember.setOnClickListener(v -> {
            showMemberList();
        });
        mBinding.profileSivGroupNameContainer.setOnClickListener(v -> {
            updateGroupName();
        });
        mBinding.profileSivGroupBroadcast.setOnClickListener(v -> {
            updateGroupAnnouncement();
        });
        mBinding.profileSivGroupNickname.setOnClickListener(v -> {
            updateGroupNickname();
        });


        mBinding.profileSivMessageNotice.setSwitchClickable(false);
        mBinding.profileSivMessageNotice.setOnClickListener(v -> {
            mute(!mBinding.profileSivMessageNotice.isChecked());
        });
        mBinding.profileSivSetTop.setSwitchClickable(false);
        mBinding.profileSivSetTop.setOnClickListener(v -> {
            setTop(!mBinding.profileSivSetTop.isChecked());
        });
        mBinding.profileSivClearMessage.setOnClickListener(v -> {
            clearMessage();
        });
        mBinding.profileBtnGroupQuit.setOnClickListener(v -> {
            quitGroup();
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
        if (mGroupDetailBean.getMyRole() == 1 || mGroupDetailBean.getMyRole() == 2) {
            mMemberAdapter.setAllowDeleteMember(true);
            mBinding.profileSivGroupManagement.setVisibility(View.VISIBLE);
        } else {
            mMemberAdapter.setAllowDeleteMember(false);
            mBinding.profileSivGroupManagement.setVisibility(View.GONE);
        }
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

    private void showMemberList() {
        Intent intent = GroupMemberListActivity.newIntent(this, mGroupId);
        startActivity(intent);
    }

    private void memberManage(boolean isAdd) {
        int type = isAdd ? 1 : 2;
        Intent intent = SelectGroupMemberActivity.newIntent(this, mGroupId, type);
        startActivity(intent);
    }

    private void updateGroupName() {
        Intent intent = GroupNameActivity.newIntent(this, mGroupId, mGroupDetailBean.getGroupName(), mGroupDetailBean.getPortrait());
        startActivity(intent);
    }

    private void updateGroupAnnouncement() {
        Intent intent = GroupAnnouncementActivity.newIntent(this, mGroupId);
        startActivity(intent);
    }

    private void updateGroupNickname() {
        Intent intent = GroupNicknameActivity.newIntent(this, mGroupId, mGroupDetailBean.getGroupDisplayName());
        startActivity(intent);
    }

    private void clearMessage() {
        DialogUtils.showWarningDialog(
                this,
                getString(R.string.text_clear_message_confirm),
                getString(R.string.j_confirm),
                confirm -> {
                    mStatusFrameView.setStatus(StatusFrameView.Status.LOADING);
                    JIM.getInstance().getMessageManager().clearMessages(mConversationInfo.getConversation(), 0, new IMessageManager.ISimpleCallback() {
                        @Override
                        public void onSuccess() {
                            mStatusFrameView.setStatus(StatusFrameView.Status.NONE);
                        }

                        @Override
                        public void onError(int errorCode) {
                            mStatusFrameView.setStatus(StatusFrameView.Status.NONE);
                            Toast.makeText(GroupInfoActivity.this, "Clear error", Toast.LENGTH_SHORT).show();
                        }
                    });
                },
                getString(R.string.j_cancel),
                cancel -> {
                }
        );
    }

    private void quitGroup() {
        DialogUtils.showWarningDialog(
                this,
                getString(R.string.text_quit_group_confirm),
                getString(R.string.j_confirm),
                confirm -> {
                    mStatusFrameView.setStatus(StatusFrameView.Status.LOADING);
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("group_id", mGroupId);
                    RequestBody body = ServiceManager.createJsonRequest(map);
                    ServiceManager.getGroupsService().quitGroup(body).enqueue(new CustomCallback<HttpResult<Object>, Object>() {
                        @Override
                        public void onSuccess(Object o) {
                            Conversation c = new Conversation(Conversation.ConversationType.GROUP, mGroupId);
                            Handler handler = new Handler(Looper.getMainLooper());
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    JIM.getInstance().getConversationManager().deleteConversationInfo(c, new IConversationManager.ISimpleCallback() {
                                        @Override
                                        public void onSuccess() {
                                            mStatusFrameView.setStatus(StatusFrameView.Status.NONE);
                                            Intent intent = new Intent();
                                            setResult(RESULT_OK, intent);
                                            finish();
                                        }

                                        @Override
                                        public void onError(int errorCode) {
                                            mStatusFrameView.setStatus(StatusFrameView.Status.NONE);
                                            Toast.makeText(GroupInfoActivity.this, "操作失败", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }, 300);

                        }
                    });
                },
                getString(R.string.j_cancel),
                cancel -> {
                }
        );
    }
}
