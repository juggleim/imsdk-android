package com.jet.im.kit.activities.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import java.util.List;

class ChannelDiffCallback extends DiffUtil.Callback {
    private final List<ChannelListAdapter.ChannelInfo> oldChannelList;
    private final List<ChannelListAdapter.ChannelInfo> newChannelList;

    ChannelDiffCallback(@NonNull List<ChannelListAdapter.ChannelInfo> oldChannelList, @NonNull List<ChannelListAdapter.ChannelInfo> newChannelList) {
        this.oldChannelList = oldChannelList;
        this.newChannelList = newChannelList;
    }

    @Override
    public int getOldListSize() {
        return oldChannelList.size();
    }

    @Override
    public int getNewListSize() {
        return newChannelList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        ChannelListAdapter.ChannelInfo oldChannel = oldChannelList.get(oldItemPosition);
        ChannelListAdapter.ChannelInfo newChannel = newChannelList.get(newItemPosition);
        if (!newChannel.getChannelUrl().equals(oldChannel.getChannelUrl())) {
            return false;
        }

        return newChannel.getCreatedAt() == oldChannel.getCreatedAt();
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        final ChannelListAdapter.ChannelInfo oldChannel = oldChannelList.get(oldItemPosition);
        final ChannelListAdapter.ChannelInfo newChannel = newChannelList.get(newItemPosition);

        return oldChannel.equals(newChannel);
    }
}
