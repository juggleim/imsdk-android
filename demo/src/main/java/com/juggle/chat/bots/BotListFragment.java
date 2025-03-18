package com.juggle.chat.bots;

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
import com.jet.im.kit.activities.ChannelActivity;
import com.jet.im.kit.modules.components.StateHeaderComponent;
import com.jet.im.kit.utils.DrawableUtils;
import com.jet.im.kit.utils.TextUtils;
import com.juggle.chat.R;
import com.juggle.chat.bean.BotBean;
import com.juggle.chat.bean.HttpResult;
import com.juggle.chat.bean.ListResult;
import com.juggle.chat.common.adapter.CommonAdapter;
import com.juggle.chat.common.adapter.MultiItemTypeAdapter;
import com.juggle.chat.common.adapter.ViewHolder;
import com.juggle.chat.databinding.FragmentBotListBinding;
import com.juggle.chat.http.CustomCallback;
import com.juggle.chat.http.ServiceManager;
import com.juggle.im.model.Conversation;

public class BotListFragment extends Fragment {
    private CommonAdapter<BotBean> mAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentBotListBinding binding = FragmentBotListBinding.inflate(inflater, container, false);
        StateHeaderComponent headerComponent = new StateHeaderComponent();
        headerComponent.getParams().setTitle(getString(R.string.text_bots));
        headerComponent.getParams().setUseLeftButton(false);
        headerComponent.getParams().setUseRightButton(false);
        View header = headerComponent.onCreateView(requireContext(), inflater, binding.headerComponent, savedInstanceState);
        binding.headerComponent.addView(header);

        mAdapter = new CommonAdapter<BotBean>(R.layout.sb_view_member_list_item) {
            @Override
            public void bindData(ViewHolder viewHolder, BotBean item, int position) {
                if (TextUtils.isNotEmpty(item.getAvatar())) {
                    Glide.with(viewHolder.itemView.getContext())
                            .load(item.getAvatar())
                            .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                            .into(viewHolder.<ImageView>getView(R.id.ivProfile));
                } else {
                    viewHolder.<ImageView>getView(R.id.ivProfile).setImageDrawable(DrawableUtils.getDefaultDrawable(viewHolder.itemView.getContext()));
                }
                viewHolder.setText(R.id.tvNickname, item.getNickname());
            }
        };
        mAdapter.setOnItemClickListener(new MultiItemTypeAdapter.OnItemClickListener<BotBean>() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder holder, BotBean botBean, int position) {
                startActivity(ChannelActivity.newIntent(requireContext(), Conversation.ConversationType.PRIVATE.getValue(), botBean.getBotId()));
            }

            @Override
            public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, BotBean botBean, int position) {
                return false;
            }
        });
        binding.rvList.setAdapter(mAdapter);
        binding.rvList.setLayoutManager(new LinearLayoutManager(getContext()));
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        ServiceManager.getBotService().getBotList("0", 100).enqueue(new CustomCallback<HttpResult<ListResult<BotBean>>, ListResult<BotBean>>() {
            @Override
            public void onSuccess(ListResult<BotBean> listResult) {
                if (listResult.getItems() != null && !listResult.getItems().isEmpty()) {
                    mAdapter.setData(listResult.getItems());
                }
            }
        });
    }
}
