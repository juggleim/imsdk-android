package com.juggle.chat.group.select;

import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.juggle.chat.R;
import com.juggle.chat.bean.SelectFriendBean;
import com.juggle.chat.common.adapter.CommonAdapter;
import com.juggle.chat.common.adapter.ViewHolder;
import com.jet.im.kit.utils.DrawableUtils;

public class SelectMemberAdapter extends CommonAdapter<SelectFriendBean> {
    public SelectMemberAdapter() {
        super(R.layout.sb_view_select_member_list_item);
    }

    @Override
    public void bindData(ViewHolder viewHolder, SelectFriendBean item, int position) {
        viewHolder.<CheckBox>getView(R.id.cb_select).setChecked(item.isSelected());
        if (item.getAvatar() == null) {
            viewHolder.<ImageView>getView(R.id.ivProfile).setImageDrawable(DrawableUtils.getDefaultDrawable(viewHolder.itemView.getContext()));
        } else {
            Glide.with(viewHolder.itemView.getContext())
                    .load(item.getAvatar())
                    .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                    .into(viewHolder.<ImageView>getView(R.id.ivProfile));
        }
        viewHolder.setText(R.id.tvNickname, item.getNickname());
        viewHolder.<CheckBox>getView(R.id.cb_select).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                item.setSelected(isChecked);
            }
        });
    }

}
