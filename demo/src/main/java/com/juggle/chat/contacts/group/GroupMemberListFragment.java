package com.juggle.chat.contacts.group;

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

import java.util.HashMap;

import okhttp3.RequestBody;

public class GroupMemberListFragment extends Fragment {
    private String mGroupId;
    private int mType; // 0: Group member list; 1: change group owner
    private CommonAdapter<GroupMemberBean> mAdapter;
    private FragmentGroupMemberListBinding mBinding;
    private StatusFrameView mStatusFrameView;

    public GroupMemberListFragment(String groupId, int type) {
        mGroupId = groupId;
        mType = type;
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
                if (mType == 0) {
                    Intent intent = UserDetailActivity.newIntent(getContext(), groupMemberBean);
                    startActivity(intent);
                } else if (mType == 1) {
                    mStatusFrameView.setStatus(StatusFrameView.Status.LOADING);
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("group_id", mGroupId);
                    map.put("owner_id", groupMemberBean.getUserId());
                    RequestBody body = ServiceManager.createJsonRequest(map);
                    ServiceManager.getGroupsService().changeOwner(body).enqueue(new CustomCallback<HttpResult<Object>, Object>() {
                        @Override
                        public void onSuccess(Object o) {
                            mStatusFrameView.setStatus(StatusFrameView.Status.NONE);
                            if (getActivity() != null) {
                                Intent intent = new Intent();
                                getActivity().setResult(Activity.RESULT_OK, intent);
                                getActivity().finish();
                            }
                        }
                    });
                }
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

        final FrameLayout innerContainer = new FrameLayout(getContext());
        innerContainer.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mStatusFrameView = new StatusFrameView(getContext(), null, com.jet.im.kit.R.attr.sb_component_status);
        mBinding.getRoot().addView(innerContainer);
        innerContainer.addView(mStatusFrameView);

        loadData();
        return mBinding.getRoot();
    }

    private void loadData() {
        ServiceManager.getGroupsService().getGroupMembers(mGroupId).enqueue(new CustomCallback<HttpResult<ListResult<GroupMemberBean>>, ListResult<GroupMemberBean>>() {
            @Override
            public void onSuccess(ListResult<GroupMemberBean> listResult) {
                if (mType == 1) {
                    for (GroupMemberBean item : listResult.getItems()) {
                        if (item.getUserId().equals(SendbirdUIKit.userId)) {
                            listResult.getItems().remove(item);
                            break;
                        }
                    }
                }
                mAdapter.setData(listResult.getItems());
            }
        });
    }
}
