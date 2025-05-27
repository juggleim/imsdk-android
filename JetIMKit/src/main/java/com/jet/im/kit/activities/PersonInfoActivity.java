package com.jet.im.kit.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.jet.im.kit.R;
import com.jet.im.kit.SendbirdUIKit;
import com.jet.im.kit.databinding.ActivityPersonInfoBinding;
import com.jet.im.kit.utils.DialogUtils;
import com.jet.im.kit.utils.PortraitGenerator;
import com.jet.im.kit.utils.TextUtils;
import com.jet.im.kit.widgets.StatusFrameView;
import com.juggle.im.JIM;
import com.juggle.im.interfaces.IConversationManager;
import com.juggle.im.interfaces.IMessageManager;
import com.juggle.im.model.Conversation;
import com.juggle.im.model.ConversationInfo;
import com.juggle.im.model.UserInfo;

public class PersonInfoActivity extends BaseActivity {
    private final static String USER_ID = "userId";
    private String mUserId;
    private ConversationInfo mConversationInfo;
    private ActivityPersonInfoBinding mBinding;
    private StatusFrameView mStatusFrameView;

    public static Intent newIntent(@NonNull Context context, String userId) {
        Intent intent = new Intent(context, PersonInfoActivity.class);
        intent.putExtra(USER_ID, userId);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserId = getIntent().getStringExtra(USER_ID);

        mBinding = ActivityPersonInfoBinding.inflate(getLayoutInflater());
        mBinding.headerView.getTitleTextView().setText(getString(R.string.text_chat_detail));
        mBinding.headerView.setLeftButtonImageResource(R.drawable.icon_arrow_left);
        mBinding.headerView.setLeftButtonTint(SendbirdUIKit.getDefaultThemeMode().getPrimaryTintColorStateList(this));
        mBinding.headerView.setOnLeftButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mBinding.headerView.setUseRightButton(false);

        UserInfo userInfo = JIM.getInstance().getUserInfoManager().getUserInfo(mUserId);
        mBinding.tvNickname.setText(userInfo.getUserName());
        if (TextUtils.isEmpty(userInfo.getPortrait())) {
            String path = PortraitGenerator.generateDefaultAvatar(this, mUserId, userInfo.getUserName());
            Uri uri = Uri.parse(path);
            Glide.with(this)
                    .load(uri)
                    .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                    .into(mBinding.ivProfileView);
        } else {
            Glide.with(this)
                    .load(userInfo.getPortrait())
                    .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                    .into(mBinding.ivProfileView);
        }

        mBinding.scNotification.setTrackTintList(AppCompatResources.getColorStateList(this, R.color.sb_switch_track_light));
        mBinding.scNotification.setThumbTintList(AppCompatResources.getColorStateList(this, R.color.sb_switch_thumb_light));

        mBinding.scSetTop.setTrackTintList(AppCompatResources.getColorStateList(this, R.color.sb_switch_track_light));
        mBinding.scSetTop.setThumbTintList(AppCompatResources.getColorStateList(this, R.color.sb_switch_thumb_light));

        mBinding.itemNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStatusFrameView.setStatus(StatusFrameView.Status.LOADING);
                boolean isMute = mConversationInfo.isMute();
                JIM.getInstance().getConversationManager().setMute(mConversationInfo.getConversation(), !isMute, new IConversationManager.ISimpleCallback() {
                    @Override
                    public void onSuccess() {
                        mStatusFrameView.setStatus(StatusFrameView.Status.NONE);
                        mConversationInfo.setMute(!isMute);
                        mBinding.scNotification.setChecked(!isMute);
                    }

                    @Override
                    public void onError(int errorCode) {
                        mStatusFrameView.setStatus(StatusFrameView.Status.NONE);
                    }
                });
            }
        });

        mBinding.itemSetTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStatusFrameView.setStatus(StatusFrameView.Status.LOADING);
                boolean isTop = mConversationInfo.isTop();
                JIM.getInstance().getConversationManager().setTop(mConversationInfo.getConversation(), !isTop, new IConversationManager.ISimpleCallback() {
                    @Override
                    public void onSuccess() {
                        mStatusFrameView.setStatus(StatusFrameView.Status.NONE);
                        mConversationInfo.setTop(!isTop);
                        mBinding.scSetTop.setChecked(!isTop);
                    }

                    @Override
                    public void onError(int errorCode) {
                        mStatusFrameView.setStatus(StatusFrameView.Status.NONE);
                    }
                });
            }
        });

        mBinding.itemClearMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogUtils.showWarningDialog(
                        PersonInfoActivity.this,
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
                                    Toast.makeText(PersonInfoActivity.this, "Clear error", Toast.LENGTH_SHORT).show();
                                }
                            });
                        },
                        getString(R.string.j_cancel),
                        cancel -> {

                        }
                );

            }
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
        Conversation conversation = new Conversation(Conversation.ConversationType.PRIVATE, mUserId);
        mConversationInfo = JIM.getInstance().getConversationManager().getConversationInfo(conversation);
        mBinding.scNotification.setChecked(mConversationInfo.isMute());
        mBinding.scSetTop.setChecked(mConversationInfo.isTop());
    }
}
