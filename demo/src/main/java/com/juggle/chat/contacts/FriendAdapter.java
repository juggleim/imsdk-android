package com.juggle.chat.contacts;

import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.jet.im.kit.interfaces.OnItemClickListener;
import com.juggle.chat.R;
import com.juggle.chat.bean.FriendBean;
import com.juggle.chat.common.adapter.ViewHolder;
import com.jet.im.kit.utils.DrawableUtils;

import java.util.ArrayList;
import java.util.List;

public class FriendAdapter extends RecyclerView.Adapter<ViewHolder> {
    private final List<FriendBean> mList = new ArrayList<>();
    private OnItemClickListener<FriendBean> mOnItemClickListener;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return ViewHolder.createViewHolder(parent.getContext(), parent, R.layout.sb_view_member_list_item);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FriendBean item = new FriendBean();
        if (position == 0) {
            holder.setText(R.id.tvNickname, holder.itemView.getContext().getString(R.string.text_new_friend));
            holder.<ImageView>getView(R.id.ivProfile).setImageResource(R.drawable.icon_new_friend);
        } else if (position == 1) {
            holder.setText(R.id.tvNickname, holder.itemView.getContext().getString(R.string.text_tab_groups));
            holder.<ImageView>getView(R.id.ivProfile).setImageResource(R.drawable.icon_default_group);
        } else if (position == 2) {
            holder.setText(R.id.tvNickname, holder.itemView.getContext().getString(R.string.text_tab_chatroom));
            holder.<ImageView>getView(R.id.ivProfile).setImageResource(R.drawable.icon_default_group);
        } else {
            item = mList.get(position-3);
            holder.setText(R.id.tvNickname, item.getNickname());
            if (item.getAvatar() == null) {
                holder.<ImageView>getView(R.id.ivProfile).setImageDrawable(DrawableUtils.getDefaultDrawable(holder.itemView.getContext()));
            } else {
                Glide.with(holder.itemView.getContext())
                        .load(item.getAvatar())
                        .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                        .into(holder.<ImageView>getView(R.id.ivProfile));
            }
        }
        if (mOnItemClickListener != null) {
            FriendBean finalItem = item;
            holder.itemView.setOnClickListener(v -> {
                int currentPosition = holder.getBindingAdapterPosition();
                mOnItemClickListener.onItemClick(v, currentPosition, finalItem);
            });
        }
    }

    @Override
    public int getItemCount() {
        if (mList == null || mList.isEmpty()) {
            return 3;
        } else {
            return mList.size() + 3;
        }
    }

    public void setList(List<FriendBean> list) {
        if (list == null) {
            return;
        }
        mList.clear();
        mList.addAll(list);
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener<FriendBean> listener) {
        mOnItemClickListener = listener;
    }
}
