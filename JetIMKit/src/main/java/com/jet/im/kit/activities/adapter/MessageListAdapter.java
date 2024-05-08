package com.jet.im.kit.activities.adapter;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.sendbird.android.channel.GroupChannel;
import com.sendbird.android.message.BaseMessage;
import com.jet.im.kit.activities.adapter.BaseMessageListAdapter;
import com.jet.im.kit.activities.viewholder.MessageViewHolder;
import com.jet.im.kit.interfaces.OnItemClickListener;
import com.jet.im.kit.interfaces.FormSubmitButtonClickListener;
import com.jet.im.kit.internal.ui.viewholders.FormMessageViewHolder;
import com.jet.im.kit.internal.ui.viewholders.SuggestedRepliesViewHolder;
import com.jet.im.kit.internal.contracts.SendbirdUIKitImpl;
import com.jet.im.kit.internal.contracts.SendbirdUIKitContract;
import com.jet.im.kit.model.MessageListUIParams;

/**
 * MessageListAdapter provides a binding from a {@link BaseMessage} type data set to views that are displayed within a RecyclerView.
 */
public class MessageListAdapter extends BaseMessageListAdapter {
    @Nullable
    protected OnItemClickListener<String> suggestedRepliesClickListener;

    @Nullable
    protected FormSubmitButtonClickListener formSubmitButtonClickListener;

    public MessageListAdapter(boolean useMessageGroupUI) {
        this(null, useMessageGroupUI);
    }

    public MessageListAdapter(@Nullable GroupChannel channel) {
        this(channel, true);
    }

    public MessageListAdapter(@Nullable GroupChannel channel, boolean useMessageGroupUI) {
        this(channel, new MessageListUIParams.Builder()
            .setUseMessageGroupUI(useMessageGroupUI)
            .build());
    }

    public MessageListAdapter(@Nullable GroupChannel channel, @NonNull MessageListUIParams messageListUIParams) {
        this(channel, messageListUIParams, new SendbirdUIKitImpl());
    }

    @VisibleForTesting
    MessageListAdapter(@Nullable GroupChannel channel, @NonNull MessageListUIParams messageListUIParams, @NonNull SendbirdUIKitContract sendbirdUIKit) {
        super(channel,
            new MessageListUIParams.Builder(messageListUIParams)
                .setUseQuotedView(true)
                .build(),
            sendbirdUIKit);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if (holder instanceof SuggestedRepliesViewHolder) {
            SuggestedRepliesViewHolder suggestedRepliesViewHolder = (SuggestedRepliesViewHolder) holder;
            suggestedRepliesViewHolder.setSuggestedRepliesClickedListener((view, pos, data) -> {
                int messagePosition = holder.getBindingAdapterPosition();
                if (messagePosition != NO_POSITION && suggestedRepliesClickListener != null) {
                    suggestedRepliesClickListener.onItemClick(view, pos, data);
                }
            });
        }

        if (holder instanceof FormMessageViewHolder) {
            FormMessageViewHolder formMessageViewHolder = (FormMessageViewHolder) holder;
            formMessageViewHolder.setOnSubmitClickListener((message, form) -> {
                final FormSubmitButtonClickListener finalListener = this.formSubmitButtonClickListener;
                if (finalListener != null) {
                    finalListener.onClicked(message, form);
                }
            });
        }
    }

    /**
     * Returns a callback to be invoked when the suggested replies is clicked.
     *
     * @return {OnItemClickListener<String>} to be invoked when the suggested replies is clicked.
     * since 3.10.0
     */
    @Nullable
    public OnItemClickListener<String> getSuggestedRepliesClickListener() {
        return suggestedRepliesClickListener;
    }

    /**
     * Register a callback to be invoked when the suggested replies is clicked.
     *
     * @param suggestedRepliesClickListener The callback to be registered.
     * since 3.10.0
     */
    public void setSuggestedRepliesClickListener(@Nullable OnItemClickListener<String> suggestedRepliesClickListener) {
        this.suggestedRepliesClickListener = suggestedRepliesClickListener;
    }

    /**
     * Returns a callback to be invoked when the Form submit button is clicked.
     *
     * @return {@link FormSubmitButtonClickListener} to be invoked when the Form submit button is clicked.
     * since 3.12.1
     */
    @Nullable
    public FormSubmitButtonClickListener getFormSubmitButtonClickListener() {
        return formSubmitButtonClickListener;
    }

    /**
     * Register a callback to be invoked when the Form submit button is clicked.
     *
     * @param formSubmitButtonClickListener The callback that will run when the Form submit button is clicked.
     * since 3.12.1
     */
    public void setFormSubmitButtonClickListener(@Nullable FormSubmitButtonClickListener formSubmitButtonClickListener) {
        this.formSubmitButtonClickListener = formSubmitButtonClickListener;
    }
}
