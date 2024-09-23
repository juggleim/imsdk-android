package com.juggle.chat.friends;

import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.juggle.chat.R;
import com.juggle.chat.bean.FriendBean;
import com.juggle.chat.common.adapter.CommonAdapter;
import com.juggle.chat.common.adapter.ViewHolder;
import com.jet.im.kit.utils.DrawableUtils;

public class FriendAdapter extends CommonAdapter<FriendBean> {
    public FriendAdapter() {
        super(R.layout.sb_view_member_list_item);
    }

    @Override
    public void bindData(ViewHolder viewHolder, FriendBean item, int position) {
        viewHolder.setText(R.id.tvNickname, item.getNickname());
        if (item.getAvatar() == null) {
            viewHolder.<ImageView>getView(R.id.ivProfile).setImageDrawable(DrawableUtils.getDefaultDrawable(viewHolder.itemView.getContext()));
        } else {
            Glide.with(viewHolder.itemView.getContext())
                    .load(item.getAvatar())
                    .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                    .into(viewHolder.<ImageView>getView(R.id.ivProfile));
        }
    }
}
