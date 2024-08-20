package com.example.demo.group;

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
import com.example.demo.R;
import com.example.demo.bean.FriendBean;
import com.example.demo.bean.GroupBean;
import com.example.demo.bean.HttpResult;
import com.example.demo.bean.ListResult;
import com.example.demo.common.adapter.CommonAdapter;
import com.example.demo.common.adapter.MultiItemTypeAdapter;
import com.example.demo.common.adapter.ViewHolder;
import com.example.demo.databinding.FragmentFriendsGroupsBinding;
import com.example.demo.http.CustomCallback;
import com.example.demo.http.ServiceManager;
import com.jet.im.kit.activities.ChannelActivity;
import com.juggle.im.model.Conversation;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment displaying the member list in the channel.
 */
public class GroupListFragment extends Fragment {
    private FragmentFriendsGroupsBinding binding;
    private CommonAdapter<GroupBean> adapter;

    private List<GroupBean> data;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFriendsGroupsBinding.inflate(inflater, container, false);
        data = new ArrayList<>();
        adapter = new CommonAdapter<GroupBean>(getContext(), R.layout.sb_view_member_list_item, data) {

            @Override
            protected void convert(ViewHolder viewHolder, GroupBean item, int position) {
                if (item.getGroup_portrait() != null) {
                    Glide.with(viewHolder.itemView.getContext())
                            .load(item.getGroup_portrait())
                            .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                            .into(viewHolder.<ImageView>getView(R.id.ivProfile));
                }
                viewHolder.setText(R.id.tvNickname, item.getGroup_name());
            }
        };
        adapter.setOnItemClickListener(new MultiItemTypeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
                if (data.size() > position) {
                    GroupBean groupBean = data.get(position);
                    startActivity(ChannelActivity.newIntent(requireContext(), Conversation.ConversationType.GROUP.getValue(), groupBean.getGroup_id()));
                }
            }

            @Override
            public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
                return false;
            }
        });
        binding.rvList.setAdapter(adapter);
        binding.rvList.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.tvTitle.setText("Groups");
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
                data.clear();
                if (listResult.getItems() != null && !listResult.getItems().isEmpty()) {
                    data.addAll(listResult.getItems());
                    adapter.notifyDataSetChanged();
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
