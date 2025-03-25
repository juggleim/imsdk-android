package com.juggle.chat.contacts.group;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.juggle.chat.R;
import com.juggle.chat.bean.GroupMemberBean;
import com.juggle.chat.bean.HttpResult;
import com.juggle.chat.bean.ListResult;
import com.juggle.chat.common.adapter.CommonAdapter;
import com.juggle.chat.common.adapter.MultiItemTypeAdapter;
import com.juggle.chat.common.adapter.ViewHolder;
import com.juggle.chat.databinding.FragmentGroupMemberListBinding;
import com.juggle.chat.http.CustomCallback;
import com.juggle.chat.http.ServiceManager;
import com.juggle.chat.settings.UserDetailActivity;

import java.util.ArrayList;
import java.util.List;

public class GroupMemberListFragment extends Fragment {
    private String mGroupId;
    private CommonAdapter<GroupMemberBean> mAdapter;
    private FragmentGroupMemberListBinding mBinding;

    public GroupMemberListFragment(String groupId) {
        mGroupId = groupId;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mAdapter = new CommonAdapter<GroupMemberBean>(R.layout.view_group_member_item) {
            @Override
            public void bindData(ViewHolder holder, GroupMemberBean item, int position) {
                if (TextUtils.isNotEmpty(item.getAvatar())) {
                    Glide.with(holder.itemView.getContext())
                            .load(item.getAvatar())
                            .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                            .into(holder.<ImageView>getView(R.id.ivProfile));
                } else {
                    Glide.with(holder.itemView.getContext())
                            .load(R.drawable.icon_person)
                            .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                            .into(holder.<ImageView>getView(R.id.ivProfile));
                }
                holder.setText(R.id.tvNickname, item.getNickname());
            }
        };
        mAdapter.setOnItemClickListener(new MultiItemTypeAdapter.OnItemClickListener<GroupMemberBean>() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder holder, GroupMemberBean groupMemberBean, int position) {
                if (getContext() == null) {
                    return;
                }
                Intent intent = UserDetailActivity.newIntent(getContext(), groupMemberBean);
                startActivity(intent);
            }

            @Override
            public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, GroupMemberBean groupMemberBean, int position) {
                return false;
            }
        });

        mBinding = FragmentGroupMemberListBinding.inflate(inflater);

        mBinding.headerView.getTitleTextView().setText(getString(R.string.text_group_member));
        mBinding.headerView.setLeftButtonImageResource(com.jet.im.kit.R.drawable.icon_arrow_left);
        mBinding.headerView.setLeftButtonTint(SendbirdUIKit.getDefaultThemeMode().getPrimaryTintColorStateList(getContext()));
        mBinding.headerView.setOnLeftButtonClickListener(v -> getActivity().finish());
        mBinding.headerView.setUseRightButton(false);

        mBinding.rvList.setAdapter(mAdapter);
        mBinding.rvList.setLayoutManager(new LinearLayoutManager(getContext()));

        loadData();
        return mBinding.getRoot();
    }

    private void loadData() {
        ServiceManager.getGroupsService().getGroupMembers(mGroupId).enqueue(new CustomCallback<HttpResult<ListResult<GroupMemberBean>>, ListResult<GroupMemberBean>>() {
            @Override
            public void onSuccess(ListResult<GroupMemberBean> listResult) {
                mAdapter.setData(listResult.getItems());
            }
        });
    }
}
