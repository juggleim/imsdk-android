package com.jet.im.kit.activities.adapter;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sendbird.android.channel.GroupChannel;
import com.sendbird.android.message.BaseFileMessage;
import com.sendbird.android.message.BaseMessage;
import com.sendbird.android.message.UserMessage;
import com.jet.im.kit.activities.adapter.BaseMessageListAdapter;
import com.jet.im.kit.activities.viewholder.MessageType;
import com.jet.im.kit.activities.viewholder.MessageViewHolder;
import com.jet.im.kit.internal.ui.viewholders.ParentMessageInfoViewHolder;
import com.jet.im.kit.model.MessageListUIParams;
import com.jet.im.kit.utils.MessageUtils;

/**
 * ThreadListAdapter provides a binding from a thread message type data set to views that are displayed within a RecyclerView.
 *
 * since 3.3.0
 */
public class ThreadListAdapter extends BaseMessageListAdapter {

    public ThreadListAdapter(@Nullable GroupChannel channel, @NonNull MessageListUIParams messageListUIParams) {
        super(channel, messageListUIParams);
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return super.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        if (holder instanceof ParentMessageInfoViewHolder) {
            ParentMessageInfoViewHolder parentMessageInfoViewHolder = (ParentMessageInfoViewHolder) holder;
            parentMessageInfoViewHolder.setOnMentionClickListener((view, pos, mentionedUser) -> {
                int messagePosition = holder.getBindingAdapterPosition();
                if (messagePosition != NO_POSITION && mentionClickListener != null) {
                    mentionClickListener.onItemClick(view, messagePosition, mentionedUser);
                }
            });
        }

        super.onBindViewHolder(holder, position);
    }

    @Override
    public int getItemViewType(int position) {
        final BaseMessage message = getItem(position);
        if (position == getItemCount() - 1 &&
            !MessageUtils.hasParentMessage(message) &&
            (message instanceof UserMessage || message instanceof BaseFileMessage)) {
            return MessageType.VIEW_TYPE_PARENT_MESSAGE_INFO.getValue();
        }
        return super.getItemViewType(position);
    }
}
