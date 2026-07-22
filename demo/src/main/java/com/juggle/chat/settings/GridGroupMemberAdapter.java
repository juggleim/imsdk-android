package com.juggle.chat.settings;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.jet.im.kit.R;
import com.jet.im.kit.utils.PortraitGenerator;
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

        // Last item
        if (position == getCount() - 1 && (isAllowDelete || isAllowAdd)) {
            // Allow removal
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
        } else { // Regular member
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
                String path = PortraitGenerator.generateDefaultAvatar(context, groupMember.getUserId(), groupMember.getNickname());
                Uri uri = Uri.parse(path);
                Glide.with(context).load(uri)
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
        // When deletion is enabled, show both add and delete buttons at the end.
        // Otherwise, show only the add button.
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
     * Enable or disable member deletion.
     *
     * @param isAllowDelete whether member deletion is allowed
     */
    public void setAllowDeleteMember(boolean isAllowDelete) {
        this.isAllowDelete = isAllowDelete;
        notifyDataSetChanged();
    }

    /**
     * Enable or disable member addition.
     *
     * @param isAllowAdd
     */
    public void setAllowAddMember(boolean isAllowAdd) {
        this.isAllowAdd = isAllowAdd;
    }

    /** Update the data and refresh the UI. */
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
     * Set the grid item click listener.
     *
     * @param onItemClickedListener the click listener
     */
    public void setOnItemClickedListener(OnItemClickedListener onItemClickedListener) {
        this.onItemClickedListener = onItemClickedListener;
    }

    public interface OnItemClickedListener {
        /**
         * Callback when add or delete is clicked.
         *
         * @param isAdd true to add a member, false to remove one
         */
        void onAddOrDeleteMemberClicked(boolean isAdd);

        /**
         * Callback when a member is clicked.
         *
         * @param groupMember the clicked member
         */
        void onMemberClicked(GroupMemberBean groupMember);
    }

    private class ViewHolder {
        SelectableRoundedImageView avatarView;
        String avatarUrl;
        TextView usernameTv;
    }
}
