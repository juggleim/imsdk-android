package com.juggle.chat.settings;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.jet.im.kit.R;
import com.jet.im.kit.utils.TextUtils;
import com.jet.im.kit.widgets.SelectableRoundedImageView;
import com.juggle.chat.bean.GroupMemberBean;
import com.juggle.im.model.UserInfo;

import java.util.List;

public class GridGroupMemberAdapter extends BaseAdapter {

    private List<GroupMemberBean> list;
    private Context context;
    private int showMemberLimit;
    private boolean isAllowDelete = false;
    private boolean isAllowAdd = false;
    private OnItemClickedListener onItemClickedListener;

    public GridGroupMemberAdapter(Context context, int showMemberLimit) {
        this.context = context;
        this.showMemberLimit = showMemberLimit;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView =
                    LayoutInflater.from(context)
                            .inflate(R.layout.profile_item_grid_group_member, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.avatarView = convertView.findViewById(R.id.profile_iv_grid_member_avatar);
            viewHolder.usernameTv = convertView.findViewById(R.id.profile_iv_grid_tv_username);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        SelectableRoundedImageView avatarView = viewHolder.avatarView;
        TextView usernameTv = viewHolder.usernameTv;

        // 最后一个item
        if (position == getCount() - 1 && (isAllowDelete || isAllowAdd)) {
            // 允许减员
            if (isAllowDelete) {
                usernameTv.setText("");
                avatarView.setImageDrawable(null);
                avatarView.setBackgroundResource(R.drawable.profile_ic_grid_member_delete);
                avatarView.setOnClickListener(
                        v -> {
                            if (onItemClickedListener != null) {
                                onItemClickedListener.onAddOrDeleteMemberClicked(false);
                            }
                        });
            } else if (isAllowAdd) {
                usernameTv.setText("");
                avatarView.setImageDrawable(null);
                avatarView.setBackgroundResource(R.drawable.profile_ic_grid_member_add);

                avatarView.setOnClickListener(
                        v -> {
                            if (onItemClickedListener != null) {
                                onItemClickedListener.onAddOrDeleteMemberClicked(true);
                            }
                        });
            }

            viewHolder.avatarUrl = null;
        } else if ((isAllowDelete && position == getCount() - 2) && isAllowAdd) {
            usernameTv.setText("");
            avatarView.setImageDrawable(null);
            avatarView.setBackgroundResource(R.drawable.profile_ic_grid_member_add);
            viewHolder.avatarUrl = null;

            avatarView.setOnClickListener(
                    v -> {
                        if (onItemClickedListener != null) {
                            onItemClickedListener.onAddOrDeleteMemberClicked(true);
                        }
                    });
        } else { // 普通成员
            final GroupMemberBean groupMember = list.get(position);
            String groupNickName = groupMember.getNickname();
            usernameTv.setText(groupNickName);

            avatarView.setBackgroundResource(android.R.color.transparent);
            String portraitUri = groupMember.getAvatar();
            if (TextUtils.isNotEmpty(portraitUri)) {
                if (!portraitUri.equals(viewHolder.avatarUrl)) {
                    Glide.with(context).load(groupMember.getAvatar())
                            .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                            .into(avatarView);
                    viewHolder.avatarUrl = portraitUri;
                }
            } else {
                Glide.with(context).load(com.juggle.chat.R.drawable.icon_person)
                        .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                        .into(avatarView);
                viewHolder.avatarUrl = portraitUri;
            }

            avatarView.setOnClickListener(
                    v -> {
                        if (onItemClickedListener != null) {
                            onItemClickedListener.onMemberClicked(groupMember);
                        }
                    });
        }

        return convertView;
    }

    @Override
    public int getCount() {
        // 判断是否允许删除成员时
        // 在允许删除成员时，在最后显示添加和删除按钮；当不运行删除成员时，仅显示添加按钮
        if (isAllowDelete && isAllowAdd) {
            return (list != null ? list.size() : 0) + 2;
        } else if (isAllowDelete || isAllowAdd) {
            return (list != null ? list.size() : 0) + 1;
        } else {
            return list != null ? list.size() : 0;
        }
    }

    @Override
    public GroupMemberBean getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * 设置是否允许删除成员
     *
     * @param isAllowDelete
     */
    public void setAllowDeleteMember(boolean isAllowDelete) {
        this.isAllowDelete = isAllowDelete;
        notifyDataSetChanged();
    }

    /**
     * 设置是否允许添加成员
     *
     * @param isAllowAdd
     */
    public void setAllowAddMember(boolean isAllowAdd) {
        this.isAllowAdd = isAllowAdd;
    }

    /** 传入新的数据 刷新UI的方法 */
    public void updateListView(List<GroupMemberBean> list) {
        if (showMemberLimit > 0) {
            if (list != null && list.size() > showMemberLimit) {
                list = list.subList(0, showMemberLimit);
            }
        }
        this.list = list;
        notifyDataSetChanged();
    }

    /**
     * 设置网格项点击事件
     *
     * @param onItemClickedListener
     */
    public void setOnItemClickedListener(OnItemClickedListener onItemClickedListener) {
        this.onItemClickedListener = onItemClickedListener;
    }

    public interface OnItemClickedListener {
        /**
         * 当点击添加或删除成员时回调
         *
         * @param isAdd true 为添加成员，false 为移除成员
         */
        void onAddOrDeleteMemberClicked(boolean isAdd);

        /**
         * 当成员点击时回调
         *
         * @param groupMember
         */
        void onMemberClicked(GroupMemberBean groupMember);
    }

    private class ViewHolder {
        SelectableRoundedImageView avatarView;
        String avatarUrl;
        TextView usernameTv;
    }
}
