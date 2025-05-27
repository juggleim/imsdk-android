package com.juggle.chat.contacts;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.jet.im.kit.SendbirdUIKit;
import com.jet.im.kit.interfaces.OnItemClickListener;
import com.jet.im.kit.utils.PortraitGenerator;
import com.jet.im.kit.utils.TextUtils;
import com.juggle.chat.R;
import com.juggle.chat.bean.FriendBean;
import com.juggle.chat.common.adapter.ViewHolder;
import com.jet.im.kit.utils.DrawableUtils;
import com.juggle.im.JIM;
import com.juggle.im.model.Conversation;
import com.juggle.im.model.ConversationInfo;

import java.util.ArrayList;
import java.util.List;

public class FriendAdapter extends RecyclerView.Adapter<ViewHolder> {
    private final List<FriendBean> mList = new ArrayList<>();
    private OnItemClickListener<FriendBean> mOnItemClickListener;
    private Context mContext;

    public FriendAdapter(Context context) {
        mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return ViewHolder.createViewHolder(parent.getContext(), parent, R.layout.sb_view_member_list_item);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FriendBean item = new FriendBean();
        if (position != 0) {
            holder.getView(R.id.tvUnreadCount).setVisibility(View.GONE);
        }
        if (position == 0) {
            updateUnreadCount(holder);
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
            if (TextUtils.isEmpty(item.getAvatar())) {
                String path = PortraitGenerator.generateDefaultAvatar(holder.itemView.getContext(), item.getUser_id(), item.getNickname());
                Uri uri = Uri.parse(path);
                Glide.with(holder.itemView.getContext())
                        .load(uri)
                        .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                        .into(holder.<ImageView>getView(R.id.ivProfile));
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
        if (mList.isEmpty()) {
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

    private void updateUnreadCount(ViewHolder holder) {
        Conversation conversation = new Conversation(Conversation.ConversationType.SYSTEM, SendbirdUIKit.FRIEND_CONVERSATION_ID);
        ConversationInfo info = JIM.getInstance().getConversationManager().getConversationInfo(conversation);
        int unreadCount = 0;
        if (info != null) {
            unreadCount = info.getUnreadCount();
        }
        if (unreadCount == 0) {
            holder.getView(R.id.tvUnreadCount).setVisibility(View.GONE);
        } else {
            TextView tvUnreadCount = holder.getView(R.id.tvUnreadCount);
            tvUnreadCount.setTextAppearance(com.jet.im.kit.R.style.SendbirdCaption1OnDark01);
            tvUnreadCount.setBackgroundResource(com.jet.im.kit.R.drawable.sb_shape_unread_message_count);
            holder.getView(R.id.tvUnreadCount).setVisibility(View.VISIBLE);

            if (unreadCount > 99) {
                tvUnreadCount.setText(mContext.getString(com.jet.im.kit.R.string.sb_text_channel_list_unread_count_max));
            } else {
                tvUnreadCount.setText(String.valueOf(unreadCount));
            }
        }
    }
}
