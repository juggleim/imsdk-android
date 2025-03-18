package com.juggle.chat.contacts.group;

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
import com.jet.im.kit.utils.DrawableUtils;
import com.jet.im.kit.utils.TextUtils;
import com.juggle.chat.R;
import com.juggle.chat.bean.GroupBean;
import com.juggle.chat.bean.HttpResult;
import com.juggle.chat.bean.ListResult;
import com.juggle.chat.common.adapter.CommonAdapter;
import com.juggle.chat.common.adapter.MultiItemTypeAdapter;
import com.juggle.chat.common.adapter.ViewHolder;
import com.juggle.chat.contacts.group.select.SelectGroupMemberActivity;
import com.juggle.chat.databinding.FragmentGroupsBinding;
import com.juggle.chat.http.CustomCallback;
import com.juggle.chat.http.ServiceManager;
import com.jet.im.kit.activities.ChannelActivity;
import com.juggle.im.model.Conversation;

/**
 * Fragment displaying the member list in the channel.
 */
public class GroupListFragment extends Fragment {
    private FragmentGroupsBinding binding;
    private CommonAdapter<GroupBean> adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentGroupsBinding.inflate(inflater, container, false);

        binding.headerView.getTitleTextView().setText(getString(R.string.text_tab_groups));
        binding.headerView.setLeftButtonImageResource(R.drawable.icon_back);
        binding.headerView.setLeftButtonTint(SendbirdUIKit.getDefaultThemeMode().getPrimaryTintColorStateList(requireContext()));
        binding.headerView.setOnLeftButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    getActivity().finish();
                }
            }
        });
        binding.headerView.setRightButtonImageResource(com.jet.im.kit.R.drawable.icon_create);
        binding.headerView.setRightButtonTint(SendbirdUIKit.getDefaultThemeMode().getPrimaryTintColorStateList(getContext()));
        binding.headerView.setOnRightButtonClickListener(v -> {
            startActivity(SelectGroupMemberActivity.newIntent(getContext()));
        });

        adapter = new CommonAdapter<GroupBean>(R.layout.sb_view_member_list_item) {
            @Override
            public void bindData(ViewHolder viewHolder, GroupBean item, int position) {
                if (TextUtils.isNotEmpty(item.getGroup_portrait())) {
                    Glide.with(viewHolder.itemView.getContext())
                            .load(item.getGroup_portrait())
                            .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                            .into(viewHolder.<ImageView>getView(R.id.ivProfile));
                } else {
                    viewHolder.<ImageView>getView(R.id.ivProfile).setImageDrawable(DrawableUtils.getDefaultDrawable(viewHolder.itemView.getContext()));
                }
                viewHolder.setText(R.id.tvNickname, item.getGroup_name());
            }
        };
        adapter.setOnItemClickListener(new MultiItemTypeAdapter.OnItemClickListener<GroupBean>() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder holder, GroupBean groupBean, int position) {
                startActivity(ChannelActivity.newIntent(requireContext(), Conversation.ConversationType.GROUP.getValue(), groupBean.getGroup_id()));
            }

            @Override
            public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, GroupBean groupBean, int position) {
                return false;
            }
        });
        binding.rvList.setAdapter(adapter);
        binding.rvList.setLayoutManager(new LinearLayoutManager(getContext()));
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    protected void refresh() {
        ServiceManager.getGroupsService().getGroupList("0", 100).enqueue(new CustomCallback<HttpResult<ListResult<GroupBean>>, ListResult<GroupBean>>() {
            @Override
            public void onSuccess(ListResult<GroupBean> listResult) {
                if (listResult.getItems() != null && !listResult.getItems().isEmpty()) {
                    adapter.setData(listResult.getItems());
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }
}
