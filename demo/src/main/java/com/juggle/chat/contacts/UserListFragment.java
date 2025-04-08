package com.juggle.chat.contacts;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.jet.im.kit.SendbirdUIKit;
import com.jet.im.kit.utils.TextUtils;
import com.jet.im.kit.widgets.StatusFrameView;
import com.juggle.chat.R;
import com.juggle.chat.bean.FriendBean;
import com.juggle.chat.bean.HttpResult;
import com.juggle.chat.bean.ListResult;
import com.juggle.chat.common.adapter.CommonAdapter;
import com.juggle.chat.common.adapter.MultiItemTypeAdapter;
import com.juggle.chat.common.adapter.ViewHolder;
import com.juggle.chat.databinding.FragmentUserListBinding;
import com.juggle.chat.http.CustomCallback;
import com.juggle.chat.http.ServiceManager;
import com.juggle.im.model.UserInfo;

import java.util.ArrayList;
import java.util.List;

public class UserListFragment extends Fragment {
    private int mType; // 0: Friend
    private CommonAdapter<UserInfo> mAdapter;
    private FragmentUserListBinding mBinding;
    private StatusFrameView mStatusFrameView;

    public UserListFragment(int type) {
        mType = type;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mAdapter = new CommonAdapter<UserInfo>(R.layout.view_group_member_item) {
            @Override
            public void bindData(ViewHolder holder, UserInfo userInfo, int position) {
                if (TextUtils.isNotEmpty(userInfo.getPortrait())) {
                    Glide.with(holder.itemView.getContext())
                            .load(userInfo.getPortrait())
                            .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                            .into(holder.<ImageView>getView(R.id.ivProfile));
                } else {
                    Glide.with(holder.itemView.getContext())
                            .load(R.drawable.icon_person)
                            .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                            .into(holder.<ImageView>getView(R.id.ivProfile));
                }
                holder.setText(R.id.tvNickname, userInfo.getUserName());
            }
        };
        mAdapter.setOnItemClickListener(new MultiItemTypeAdapter.OnItemClickListener<UserInfo>() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder holder, UserInfo userInfo, int position) {
                if (mType == 0) {
                    if (getActivity() != null) {
                        Intent intent = new Intent();
                        intent.putExtra("user_id", userInfo.getUserId());
                        intent.putExtra("name", userInfo.getUserName());
                        intent.putExtra("portrait", userInfo.getPortrait());
                        getActivity().setResult(Activity.RESULT_OK, intent);
                        getActivity().finish();
                    }
                }
            }

            @Override
            public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, UserInfo userInfo, int position) {
                return false;
            }
        });

        mBinding = FragmentUserListBinding.inflate(inflater);

        mBinding.headerView.getTitleTextView().setText("");
        mBinding.headerView.setLeftButtonImageResource(com.jet.im.kit.R.drawable.icon_arrow_left);
        mBinding.headerView.setLeftButtonTint(SendbirdUIKit.getDefaultThemeMode().getPrimaryTintColorStateList(getContext()));
        mBinding.headerView.setOnLeftButtonClickListener(v -> getActivity().finish());
        mBinding.headerView.setUseRightButton(false);

        mBinding.rvList.setAdapter(mAdapter);
        mBinding.rvList.setLayoutManager(new LinearLayoutManager(getContext()));

        final FrameLayout innerContainer = new FrameLayout(getContext());
        innerContainer.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mStatusFrameView = new StatusFrameView(getContext(), null, com.jet.im.kit.R.attr.sb_component_status);
        mBinding.getRoot().addView(innerContainer);
        innerContainer.addView(mStatusFrameView);

        loadData();
        return mBinding.getRoot();
    }

    private void loadData() {
        if (mType == 0) {
            ServiceManager.getFriendsService().getFriendList(SendbirdUIKit.userId, "0", 200).enqueue(new CustomCallback<HttpResult<ListResult<FriendBean>>, ListResult<FriendBean>>() {
                @Override
                public void onSuccess(ListResult<FriendBean> friendBeanListResult) {
                    List<UserInfo> userInfoList = new ArrayList<>();
                    for (FriendBean friend : friendBeanListResult.getItems()) {
                        UserInfo userInfo = new UserInfo();
                        userInfo.setUserId(friend.getUser_id());
                        userInfo.setUserName(friend.getNickname());
                        userInfo.setPortrait(friend.getAvatar());
                        userInfoList.add(userInfo);
                    }
                    mAdapter.setData(userInfoList);
                }
            });
        }
    }

}
