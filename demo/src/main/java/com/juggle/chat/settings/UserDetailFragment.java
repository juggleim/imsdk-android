package com.juggle.chat.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.jet.im.kit.SendbirdUIKit;
import com.jet.im.kit.activities.ChannelActivity;
import com.jet.im.kit.call.CallCenter;
import com.jet.im.kit.fragments.PermissionFragment;
import com.jet.im.kit.utils.PermissionUtils;
import com.jet.im.kit.utils.TextUtils;
import com.juggle.chat.R;
import com.juggle.chat.bean.HttpResult;
import com.juggle.chat.bean.UserInfoBean;
import com.juggle.chat.common.widgets.CommonDialog;
import com.juggle.chat.databinding.FragmentUserDetailBinding;
import com.juggle.chat.http.CustomCallback;
import com.juggle.chat.http.ServiceManager;
import com.juggle.im.model.Conversation;

import java.util.HashMap;

import okhttp3.RequestBody;

public class UserDetailFragment extends PermissionFragment {
    private final String mUserId;
    private final String mName;
    private final String mPortrait;
    private UserInfoBean mUserInfoBean;
    private FragmentUserDetailBinding mBinding;

    public UserDetailFragment(String userId, String name, String portrait) {
        mUserId = userId;
        mName = name;
        mPortrait = portrait;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentUserDetailBinding.inflate(inflater, container, false);

        mBinding.headerView.getTitleTextView().setText(getString(R.string.text_user_information));
        mBinding.headerView.setLeftButtonImageResource(com.jet.im.kit.R.drawable.icon_arrow_left);
        mBinding.headerView.setLeftButtonTint(SendbirdUIKit.getDefaultThemeMode().getPrimaryTintColorStateList(getActivity()));
        mBinding.headerView.setOnLeftButtonClickListener(v -> {
            if (getActivity() == null) {
                return;
            }
            getActivity().finish();
        });
        mBinding.headerView.setUseRightButton(false);

        if (TextUtils.isNotEmpty(mPortrait)) {
            Glide.with(this).load(mPortrait)
                    .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                    .into(mBinding.profileIvDetailUserPortrait);
        } else {
            Glide.with(this).load(R.drawable.icon_person)
                    .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                    .into(mBinding.profileIvDetailUserPortrait);
        }
        mBinding.profileTvDetailDisplayName.setText(mName);

        mBinding.profileBtnDetailStartChat.setOnClickListener(v -> {
            startActivity(ChannelActivity.newIntent(getActivity(), Conversation.ConversationType.PRIVATE.getValue(), mUserId));
        });
        mBinding.profileBtnDetailStartVoice.setOnClickListener(v -> {
            requestPermission(PermissionUtils.RECORD_AUDIO_PERMISSION, () -> {
                if (getContext() == null) return;

                CallCenter.getInstance().startSingleCall(getContext(), mUserId);
            });
        });
        mBinding.profileBtnDetailStartVideo.setOnClickListener(v -> {
            //todo
        });
        mBinding.profileBtnDetailAddFriend.setOnClickListener(v -> {
            addFriend();
        });

        loadData();

        return mBinding.getRoot();
    }

    private void loadData() {
        ServiceManager.getUserService().getUserInfo(mUserId).enqueue(new CustomCallback<HttpResult<UserInfoBean>, UserInfoBean>() {
            @Override
            public void onSuccess(UserInfoBean userInfoBean) {
                mUserInfoBean = userInfoBean;
                updateView();
            }
        });
    }

    private void updateView() {
        boolean isMe = mUserId.equals(SendbirdUIKit.userId);
        boolean isFriend = mUserInfoBean.isFriend();
        if (isMe) {
            mBinding.profileBtnDetailStartChat.setVisibility(View.GONE);
            mBinding.profileBtnDetailStartVideo.setVisibility(View.GONE);
            mBinding.profileBtnDetailStartVoice.setVisibility(View.GONE);
            mBinding.profileBtnDetailAddFriend.setVisibility(View.GONE);
        } else if (isFriend) {
            mBinding.profileBtnDetailStartChat.setVisibility(View.VISIBLE);
            mBinding.profileBtnDetailStartVideo.setVisibility(View.GONE);
//            mBinding.profileBtnDetailStartVideo.setVisibility(View.VISIBLE);
            mBinding.profileBtnDetailStartVoice.setVisibility(View.VISIBLE);
            mBinding.profileBtnDetailAddFriend.setVisibility(View.GONE);
        } else {
            mBinding.profileBtnDetailStartChat.setVisibility(View.GONE);
            mBinding.profileBtnDetailStartVideo.setVisibility(View.GONE);
            mBinding.profileBtnDetailStartVoice.setVisibility(View.GONE);
            mBinding.profileBtnDetailAddFriend.setVisibility(View.VISIBLE);
        }
    }

    private void addFriend() {
        HashMap<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("user_id", SendbirdUIKit.userId);
        paramsMap.put("friend_id", mUserId);
        RequestBody body = ServiceManager.createJsonRequest(paramsMap);
        ServiceManager.getFriendsService().addFriend(body).enqueue(new CustomCallback<HttpResult<Object>, Object>() {
            @Override
            public void onSuccess(Object o) {
                CommonDialog dialog = new CommonDialog.Builder()
                        .setContentMessage(getString(R.string.text_friend_application_sent))
                        .setIsOnlyConfirm(true)
                        .build();
                dialog.show(getParentFragmentManager(), null);
            }
        });
    }
}
