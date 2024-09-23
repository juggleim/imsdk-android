package com.juggle.chat.chatroom;

import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.juggle.chat.R;
import com.juggle.chat.bean.ChatRoomBean;
import com.juggle.chat.common.adapter.CommonAdapter;
import com.juggle.chat.common.adapter.ViewHolder;

public class ChatRoomAdapter extends CommonAdapter<ChatRoomBean> {
    public ChatRoomAdapter() {
        super(R.layout.sb_view_member_list_item);
    }

    @Override
    public void bindData(ViewHolder viewHolder, ChatRoomBean item, int position) {
        viewHolder.setText(R.id.tvNickname, item.getName());
        Glide.with(viewHolder.itemView.getContext())
                .load(item.getPoster())
                .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                .into(viewHolder.<ImageView>getView(R.id.ivProfile));
    }
}
