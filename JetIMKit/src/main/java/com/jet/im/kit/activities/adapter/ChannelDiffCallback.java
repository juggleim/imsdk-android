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
        //todo 比较值
        ChannelListAdapter.ChannelInfo oldChannel = oldChannelList.get(oldItemPosition);
        ChannelListAdapter.ChannelInfo newChannel = newChannelList.get(newItemPosition);
        return newChannel.getConversationType() == oldChannel.getConversationType()
                && newChannel.getConversationId().equals(oldChannel.getConversationId());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        //todo 比较值
        ChannelListAdapter.ChannelInfo oldChannel = oldChannelList.get(oldItemPosition);
        ChannelListAdapter.ChannelInfo newChannel = newChannelList.get(newItemPosition);
        boolean digestEqual = true;
        if (newChannel.getLastMessage() != null
            && newChannel.getLastMessage().getContent() != null
            && newChannel.getLastMessage().getContent().conversationDigest() != null
            && oldChannel.getLastMessage() != null
            && oldChannel.getLastMessage().getContent() != null
            && oldChannel.getLastMessage().getContent().conversationDigest() != null
            && !newChannel.getLastMessage().getContent().conversationDigest().equals(oldChannel.getLastMessage().getContent().conversationDigest())) {
            digestEqual = false;
        }
        boolean draftEqual = true;
        if (newChannel.getDraft() != null
            && !newChannel.getDraft().equals(oldChannel.getDraft())) {
            draftEqual = false;
        }

        return newChannel.getUnreadCount() == oldChannel.getUnreadCount()
                && newChannel.getUpdateTime() == oldChannel.getUpdateTime()
                && digestEqual
                && newChannel.isTop() == oldChannel.isTop()
                && newChannel.isMute() == oldChannel.isMute()
                && draftEqual;
    }
}
