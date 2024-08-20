package com.jet.im.kit.activities.adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.jet.im.kit.activities.viewholder.MessageViewHolder;
import com.jet.im.kit.internal.contracts.SendbirdUIKitContract;
import com.jet.im.kit.internal.contracts.SendbirdUIKitImpl;
import com.jet.im.kit.model.MessageListUIParams;
import com.juggle.im.model.ConversationInfo;
import com.sendbird.android.message.BaseMessage;

/**
 * MessageListAdapter provides a binding from a {@link BaseMessage} type data set to views that are displayed within a RecyclerView.
 */
public class MessageListAdapter extends BaseMessageListAdapter {

    public MessageListAdapter(boolean useMessageGroupUI) {
        this(null, useMessageGroupUI);
    }

    public MessageListAdapter(@Nullable ConversationInfo channel) {
        this(channel, true);
    }

    public MessageListAdapter(@Nullable ConversationInfo channel, boolean useMessageGroupUI) {
        this(channel, new MessageListUIParams.Builder()
            .setUseMessageGroupUI(useMessageGroupUI)
            .build());
    }

    public MessageListAdapter(@Nullable ConversationInfo channel, @NonNull MessageListUIParams messageListUIParams) {
        this(channel, messageListUIParams, new SendbirdUIKitImpl());
    }

    @VisibleForTesting
    MessageListAdapter(@Nullable ConversationInfo channel, @NonNull MessageListUIParams messageListUIParams, @NonNull SendbirdUIKitContract sendbirdUIKit) {
        super(channel,
            new MessageListUIParams.Builder(messageListUIParams)
                .setUseQuotedView(true)
                .build(),
            sendbirdUIKit);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
    }

}
