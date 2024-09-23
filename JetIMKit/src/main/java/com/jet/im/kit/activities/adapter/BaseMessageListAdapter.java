package com.jet.im.kit.activities.adapter;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.recyclerview.widget.DiffUtil;

import com.juggle.im.model.ConversationInfo;
import com.juggle.im.model.Message;
import com.sendbird.android.channel.GroupChannel;
import com.sendbird.android.message.BaseMessage;
import com.sendbird.android.user.User;
import com.jet.im.kit.R;
import com.jet.im.kit.activities.viewholder.MessageType;
import com.jet.im.kit.activities.viewholder.MessageViewHolder;
import com.jet.im.kit.activities.viewholder.MessageViewHolderFactory;
import com.jet.im.kit.interfaces.OnIdentifiableItemClickListener;
import com.jet.im.kit.interfaces.OnIdentifiableItemLongClickListener;
import com.jet.im.kit.interfaces.OnItemClickListener;
import com.jet.im.kit.interfaces.OnMessageListUpdateHandler;
import com.jet.im.kit.internal.interfaces.OnFeedbackRatingClickListener;
import com.jet.im.kit.internal.ui.viewholders.MyUserMessageViewHolder;
import com.jet.im.kit.internal.ui.viewholders.OtherUserMessageViewHolder;
import com.jet.im.kit.internal.contracts.SendbirdUIKitImpl;
import com.jet.im.kit.internal.contracts.SendbirdUIKitContract;
import com.jet.im.kit.log.Logger;
import com.jet.im.kit.model.MessageListUIParams;
import com.jet.im.kit.model.MessageUIConfig;

import org.jetbrains.annotations.TestOnly;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

abstract public class BaseMessageListAdapter extends BaseMessageAdapter<Message, MessageViewHolder> {
    @NonNull
    private List<Message> messageList = new ArrayList<>();
    @Nullable
    private ConversationInfo channel;
    @Nullable
    private OnIdentifiableItemClickListener<Message> listItemClickListener;
    @Nullable
    private OnIdentifiableItemLongClickListener<Message> listItemLongClickListener;
    @Nullable
    protected OnItemClickListener<User> mentionClickListener;

    @Nullable
    protected OnFeedbackRatingClickListener feedbackRatingClickListener;

    @NonNull
    private final MessageListUIParams messageListUIParams;
    @Nullable
    private MessageUIConfig messageUIConfig;

    // the worker must be a single thread.
    @NonNull
    private final ExecutorService differWorker = Executors.newSingleThreadExecutor();

    @NonNull
    protected final SendbirdUIKitContract sendbirdUIKit;

    /**
     * Constructor
     *
     * @param useMessageGroupUI <code>true</code> if the message group UI is used, <code>false</code> otherwise.
     *                          since 3.0.0
     */
    public BaseMessageListAdapter(boolean useMessageGroupUI) {
        this(null, useMessageGroupUI);
    }

    /**
     * Constructor
     *
     * @param channel The {@link GroupChannel} that contains the data needed for this adapter
     */
    public BaseMessageListAdapter(@Nullable ConversationInfo channel) {
        this(channel, true);
    }

    /**
     * Constructor
     *
     * @param channel           The {@link GroupChannel} that contains the data needed for this adapter
     * @param useMessageGroupUI <code>true</code> if the message group UI is used, <code>false</code> otherwise.
     *                          since 2.2.0
     */
    public BaseMessageListAdapter(@Nullable ConversationInfo channel, boolean useMessageGroupUI) {
        this(channel, useMessageGroupUI, true);
    }

    /**
     * Constructor
     *
     * @param channel           The {@link GroupChannel} that contains the data needed for this adapter
     * @param useMessageGroupUI <code>true</code> if the message group UI is used, <code>false</code> otherwise.
     * @param useReverseLayout  <code>true</code> if the message list is reversed, <code>false</code> otherwise.
     *                          since 3.2.2
     */
    public BaseMessageListAdapter(@Nullable ConversationInfo channel, boolean useMessageGroupUI, boolean useReverseLayout) {
        this(channel, new MessageListUIParams.Builder()
                .setUseMessageGroupUI(useMessageGroupUI)
                .setUseReverseLayout(useReverseLayout)
                .build());
    }

    /**
     * Constructor
     *
     * @param channel             The {@link GroupChannel} that contains the data needed for this adapter
     * @param messageListUIParams The {@link MessageListUIParams} that contains drawing parameters
     *                            since 3.3.0
     */
    public BaseMessageListAdapter(@Nullable ConversationInfo channel, @NonNull MessageListUIParams messageListUIParams) {
        this(channel, messageListUIParams, new SendbirdUIKitImpl());
    }

    @VisibleForTesting
    BaseMessageListAdapter(@Nullable ConversationInfo channel, @NonNull MessageListUIParams messageListUIParams, @NonNull SendbirdUIKitContract sendbirdUIKit) {
        if (channel != null) this.channel = channel;
        this.messageListUIParams = messageListUIParams;
        this.sendbirdUIKit = sendbirdUIKit;
    }

    /**
     * Called when RecyclerView needs a new {@link MessageViewHolder} of the given type to represent
     * an item.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return A new {@link MessageViewHolder} that holds a View of the given view type.
     * @see #getItemViewType(int)
     * @see #onBindViewHolder(MessageViewHolder, int)
     */
    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final TypedValue values = new TypedValue();
        parent.getContext().getTheme().resolveAttribute(R.attr.sb_component_list, values, true);
        final Context contextWrapper = new ContextThemeWrapper(parent.getContext(), values.resourceId);
        LayoutInflater inflater = LayoutInflater.from(contextWrapper);
        MessageViewHolder viewHolder = createViewHolder(parent, viewType, inflater);

        viewHolder.setMessageUIConfig(messageUIConfig);

        final Map<String, View> views = viewHolder.getClickableViewMap();
        for (Map.Entry<String, View> entry : views.entrySet()) {
            final String identifier = entry.getKey();
            entry.getValue().setOnClickListener(v -> {
                int messagePosition = viewHolder.getBindingAdapterPosition();
                if (messagePosition != NO_POSITION) {
                    if (listItemClickListener != null) {
                        listItemClickListener.onIdentifiableItemClick(v, identifier, messagePosition, getItem(messagePosition));
                    }
                }
            });

            entry.getValue().setOnLongClickListener(v -> {
                int messagePosition = viewHolder.getBindingAdapterPosition();
                if (messagePosition != NO_POSITION) {
                    if (listItemLongClickListener != null) {
                        listItemLongClickListener.onIdentifiableItemLongClick(v, identifier, messagePosition, getItem(messagePosition));
                    }

                    return true;
                }
                return false;
            });
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (!payloads.isEmpty()) {
            final Object lastPayload = payloads.get(payloads.size() - 1);
            if (lastPayload instanceof Animation) {
                final Animation animation = (Animation) lastPayload;
                holder.itemView.startAnimation(animation);
            }
        }
        super.onBindViewHolder(holder, position, payloads);
    }

    /**
     * Called by RecyclerView to display the data at the specified position. This method should
     * update the contents of the {@link MessageViewHolder#itemView} to reflect the item at the given
     * position.
     *
     * @param holder   The {@link MessageViewHolder} which should be updated to represent
     *                 the contents of the item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, final int position) {
        Message prev = null;
        Message next = null;
        Message current = getItem(position);

        int itemCount = getItemCount();
        if (position < itemCount - 1) {
            prev = getItem(position + 1);
        }

        if (position > 0) {
            next = getItem(position - 1);
        }
        if (holder instanceof MyUserMessageViewHolder) {
            MyUserMessageViewHolder myUserMessageViewHolder = (MyUserMessageViewHolder) holder;
            myUserMessageViewHolder.setOnMentionClickListener((view, pos, mentionedUser) -> {
                int messagePosition = holder.getBindingAdapterPosition();
                if (messagePosition != NO_POSITION && mentionClickListener != null) {
                    mentionClickListener.onItemClick(view, messagePosition, mentionedUser);
                }
            });
        }

        if (holder instanceof OtherUserMessageViewHolder) {
            OtherUserMessageViewHolder otherUserMessageViewHolder = (OtherUserMessageViewHolder) holder;
            otherUserMessageViewHolder.setOnMentionClickListener((view, pos, mentionedUser) -> {
                int messagePosition = holder.getBindingAdapterPosition();
                if (messagePosition != NO_POSITION && mentionClickListener != null) {
                    mentionClickListener.onItemClick(view, messagePosition, mentionedUser);
                }
            });

            otherUserMessageViewHolder.setOnFeedbackRatingClickListener((message, rating) -> {
                if (feedbackRatingClickListener != null) {
                    feedbackRatingClickListener.onFeedbackClicked(message, rating);
                }
            });
        }

        if (channel != null) {
            holder.onBindViewHolder(channel, prev, current, next);
        }
    }

    /**
     * Sets the configurations of the message's properties to highlight text.
     *
     * @param messageUIConfig the configurations of the message's properties to highlight text.
     * @see com.jet.im.kit.model.TextUIConfig
     * since 3.0.0
     */
    public void setMessageUIConfig(@Nullable MessageUIConfig messageUIConfig) {
        this.messageUIConfig = messageUIConfig;
    }

    /**
     * Returns the configurations of the message's properties to highlight text.
     *
     * @return the configurations of the message's properties to highlight text.
     * since 3.0.0
     */
    @Nullable
    public MessageUIConfig getMessageUIConfig() {
        return this.messageUIConfig;
    }

    /**
     * Return the view type of the {@link MessageViewHolder} at <code>position</code> for the purposes
     * of view recycling.
     *
     * @param position position to query
     * @return integer value identifying the type of the view needed to represent the item at <code>position</code>.
     * @see MessageViewHolderFactory#getViewType(Message)
     */
    @Override
    public int getItemViewType(int position) {
        Message message = getItem(position);
        return MessageViewHolderFactory.getViewType(message);
    }

    /**
     * Sets channel that related with a list of messages
     *
     * @param channel {@code GroupChannel} that related with a list of messages
     */
    public void setChannel(@NonNull ConversationInfo channel) {
        this.channel = channel;
    }

    /**
     * Sets the {@link List<BaseMessage>} to be displayed.
     *
     * @param messageList list to be displayed
     *                    since 2.2.0
     */
    public void setItems(@NonNull final ConversationInfo channel, @NonNull final List<Message> messageList, @Nullable OnMessageListUpdateHandler callback) {
        notifyMessageListChanged(channel, messageList, callback);
    }

    private void notifyMessageListChanged(@NonNull ConversationInfo channel, @NonNull List<Message> messageList, @Nullable OnMessageListUpdateHandler callback) {
        BaseMessageListAdapter.this.messageList = messageList;
        BaseMessageListAdapter.this.channel = channel;
        notifyDataSetChanged();
        if (callback != null) {
            callback.onListUpdated(messageList);
        }
    }

    /**
     * Animates the view holder with the corresponding message id.
     *
     * @param animation Animation you want to apply to the view holder
     * @param messageId Message id of the view holder to be applied
     */
    public void startAnimation(@NonNull Animation animation, long messageId) {
        Message target = null;
        int position = -1;

        final List<Message> copied = new ArrayList<>(messageList);
        for (int i = 0; i < copied.size(); i++) {
            Message message = copied.get(i);
            if (message.getClientMsgNo() == messageId) {
                target = message;
                position = i;
                break;
            }
        }

        if (target != null) {
            startAnimation(animation, position);
        }
    }

    /**
     * Animates the view holder with the corresponding position.
     *
     * @param animation Animation you want to apply to the view holder
     * @param position  Position of the view holder to be applied
     */
    public void startAnimation(@NonNull Animation animation, int position) {
        Logger.d(">> MessageListAdapter::startAnimation(), position=%s", position);
        notifyItemChanged(position, animation);
    }

    @Override
    public void onViewRecycled(@NonNull MessageViewHolder holder) {
        final View view = holder.itemView;
        if (view.getAnimation() != null) {
            view.getAnimation().cancel();
        }
    }

    /**
     * Register a callback to be invoked when the {@link MessageViewHolder#itemView} is clicked.
     *
     * @param listener The callback that will run
     *                 since 2.2.0
     */
    public void setOnListItemClickListener(@Nullable OnIdentifiableItemClickListener<Message> listener) {
        this.listItemClickListener = listener;
    }

    /**
     * Returns a callback to be invoked when the {@link MessageViewHolder#itemView} is clicked
     *
     * @return {@code OnIdentifiableItemClickListener<BaseMessage>} to be invoked when the {@link MessageViewHolder#itemView} is clicked.
     * since 3.0.0
     */
    @Nullable
    public OnIdentifiableItemClickListener<Message> getOnListItemClickListener() {
        return listItemClickListener;
    }

    /**
     * Register a callback to be invoked when the {@link MessageViewHolder#itemView} is long clicked and held.
     *
     * @param listener The callback that will run
     *                 since 2.2.0
     */
    public void setOnListItemLongClickListener(@Nullable OnIdentifiableItemLongClickListener<Message> listener) {
        this.listItemLongClickListener = listener;
    }

    /**
     * Returns a callback to be invoked when the {@link MessageViewHolder#itemView} is long clicked and held.
     *
     * @return {@code OnIdentifiableItemLongClickListener<BaseMessage>} to be invoked when the {@link MessageViewHolder#itemView} is long clicked and held.
     * since 3.0.0
     */
    @Nullable
    public OnIdentifiableItemLongClickListener<Message> getOnListItemLongClickListener() {
        return listItemLongClickListener;
    }

    /**
     * Register a callback to be invoked when the mentioned user is clicked.
     *
     * @param listener The callback that will run
     *                 since 3.5.3
     */
    public void setMentionClickListener(@Nullable OnItemClickListener<User> listener) {
        this.mentionClickListener = listener;
    }

    /**
     * Returns a callback to be invoked when the mentioned user is clicked.
     *
     * @return {OnItemClickListener<User>} to be invoked when the mentioned user is clicked.
     * since 3.5.3
     */
    @Nullable
    public OnItemClickListener<User> getMentionClickListener() {
        return mentionClickListener;
    }

    /**
     * Returns a callback to be invoked when the feedback rating is clicked.
     *
     * @return {@link OnFeedbackRatingClickListener} to be invoked when the feedback rating is clicked.
     * since 3.13.0
     */
    @Nullable
    public OnFeedbackRatingClickListener getFeedbackRatingClickListener() {
        return feedbackRatingClickListener;
    }

    /**
     * Register a callback to be invoked when the feedback rating is clicked.
     *
     * @param feedbackRatingClickListener The callback that will run
     *                                    since 3.13.0
     */
    public void setFeedbackRatingClickListener(@Nullable OnFeedbackRatingClickListener feedbackRatingClickListener) {
        this.feedbackRatingClickListener = feedbackRatingClickListener;
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return messageList.size();
    }

    /**
     * Returns the {@link BaseMessage} in the data set held by the adapter.
     *
     * @param position The position of the item within the adapter's data set.
     * @return The {@link Message} to retrieve the position of in this adapter.
     */
    @NonNull
    @Override
    public Message getItem(int position) {
        return messageList.get(position);
    }

    /**
     * Returns the {@link List<BaseMessage>} in the data set held by the adapter.
     *
     * @return The {@link List<BaseMessage>} in this adapter.
     */
    @NonNull
    @Override
    public List<Message> getItems() {
        return Collections.unmodifiableList(messageList);
    }

    @TestOnly
    @NonNull
    MessageListUIParams getMessageListUIParams() {
        return messageListUIParams;
    }

    @NonNull
    MessageViewHolder createViewHolder(@NonNull ViewGroup parent, int viewType, LayoutInflater inflater) {
        return MessageViewHolderFactory.createViewHolder(inflater,
                parent,
                MessageType.from(viewType),
                messageListUIParams);
    }

    @TestOnly
    @Nullable
    ConversationInfo getChannel() {
        return channel;
    }

    @VisibleForTesting
    @NonNull
    DiffUtil.DiffResult calculateDiff(MessageDiffCallback diffCallback) {
        return DiffUtil.calculateDiff(diffCallback);
    }
}
