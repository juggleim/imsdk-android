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
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.sendbird.android.channel.OpenChannel;
import com.sendbird.android.message.BaseMessage;
import com.jet.im.kit.R;
import com.jet.im.kit.SendbirdUIKit;
import com.jet.im.kit.activities.adapter.OpenChannelMessageDiffCallback;
import com.jet.im.kit.activities.viewholder.MessageType;
import com.jet.im.kit.activities.viewholder.MessageViewHolder;
import com.jet.im.kit.activities.viewholder.MessageViewHolderFactory;
import com.jet.im.kit.consts.ClickableViewIdentifier;
import com.jet.im.kit.interfaces.OnIdentifiableItemClickListener;
import com.jet.im.kit.interfaces.OnIdentifiableItemLongClickListener;
import com.jet.im.kit.interfaces.OnMessageListUpdateHandler;
import com.jet.im.kit.model.MessageListUIParams;
import com.jet.im.kit.model.MessageUIConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * OpenChannelMessageListAdapter provides a binding from a {@link BaseMessage} set to views that are displayed
 * within a {@link RecyclerView}. This adapter is used to be only {@link OpenChannel}.
 *
 * since 2.0.0
 */
public class OpenChannelMessageListAdapter extends BaseMessageAdapter<BaseMessage, MessageViewHolder> {
    @NonNull
    private List<BaseMessage> messageList = new ArrayList<>();
    @Nullable
    private OpenChannel channel;
    @Nullable
    private OnIdentifiableItemClickListener<BaseMessage> listItemClickListener;
    @Nullable
    private OnIdentifiableItemLongClickListener<BaseMessage> listItemLongClickListener;
    @NonNull
    private final MessageListUIParams messageListUIParams;
    @Nullable
    private MessageUIConfig messageUIConfig;

    @NonNull
    private final ExecutorService service = Executors.newSingleThreadExecutor();

    /**
     * Constructor
     *
     * @param useMessageGroupUI <code>true</code> if the message group UI is used, <code>false</code> otherwise.
     * since 3.0.0
     */
    public OpenChannelMessageListAdapter(boolean useMessageGroupUI) {
        this(null, useMessageGroupUI);
    }

    /**
     * Constructor
     *
     * @param channel The {@link OpenChannel} that contains the data needed for this adapter
     */
    public OpenChannelMessageListAdapter(@Nullable OpenChannel channel) {
        this(channel, true);
    }

    /**
     * Constructor
     *
     * @param channel           The {@link OpenChannel} that contains the data needed for this adapter
     * @param useMessageGroupUI <code>true</code> if the message group UI is used, <code>false</code> otherwise.
     * since 2.2.0
     */
    public OpenChannelMessageListAdapter(@Nullable OpenChannel channel, boolean useMessageGroupUI) {
        this(channel, useMessageGroupUI, true);
    }

    /**
     * Constructor
     *
     * @param channel           The {@link OpenChannel} that contains the data needed for this adapter
     * @param useMessageGroupUI <code>true</code> if the message group UI is used, <code>false</code> otherwise.
     * @param useReverseLayout  <code>true</code> if the message list is reversed, <code>false</code> otherwise.
     * since 3.2.2
     */
    public OpenChannelMessageListAdapter(@Nullable OpenChannel channel, boolean useMessageGroupUI, boolean useReverseLayout) {
        this(channel, new MessageListUIParams.Builder()
            .setUseMessageGroupUI(useMessageGroupUI)
            .setUseReverseLayout(useReverseLayout)
            .build());
    }

    /**
     * Constructor
     *
     * @param channel               The {@link OpenChannel} that contains the data needed for this adapter
     * @param messageListUIParams   The {@link MessageListUIParams} that contains the data needed for this adapter
     * since 3.6.0
     */
    public OpenChannelMessageListAdapter(@Nullable OpenChannel channel, @NonNull MessageListUIParams messageListUIParams) {
        if (channel != null) this.channel = OpenChannel.clone(channel);
        this.messageListUIParams = messageListUIParams;
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
        final MessageViewHolder viewHolder = MessageViewHolderFactory.createOpenChannelViewHolder(inflater,
            parent,
            MessageType.from(viewType),
            messageListUIParams);
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
                        return true;
                    }
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
                Animation animation = (Animation) lastPayload;
                final Map<String, View> clickableViewMap = holder.getClickableViewMap();
                if (!clickableViewMap.isEmpty()) {
                    final View view = clickableViewMap.get(ClickableViewIdentifier.Chat.name());
                    if (view != null) {
                        view.setAnimation(animation);
                    }
                }
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
        BaseMessage prev = null;
        BaseMessage next = null;
        BaseMessage current = getItem(position);
        int itemCount = getItemCount();
        if (position < itemCount - 1) {
            prev = getItem(position + 1);
        }

        if (position > 0) {
            next = getItem(position - 1);
        }

        if (channel != null) {
            holder.onBindViewHolder(channel, prev, current, next);
        }
    }

    /**
     * Return the view type of the {@link MessageViewHolder} at <code>position</code> for the purposes
     * of view recycling.
     *
     * @param position position to query
     * @return integer value identifying the type of the view needed to represent the item at <code>position</code>.
     * @see MessageViewHolderFactory#getViewType(BaseMessage)
     */
    @Override
    public int getItemViewType(int position) {
        BaseMessage message = getItem(position);
        return MessageViewHolderFactory.getViewType(message);
    }

    /**
     * Sets channel that related with a list of messages
     *
     * @param channel {@code OpenChannel} that related with a list of messages
     */
    public void setChannel(@NonNull OpenChannel channel) {
        this.channel = OpenChannel.clone(channel);
    }

    /**
     * Sets the {@link List<BaseMessage>} to be displayed.
     *
     * @param messageList list to be displayed
     * since 2.2.0
     */
    public void setItems(@NonNull final OpenChannel channel, @NonNull final List<BaseMessage> messageList, @Nullable OnMessageListUpdateHandler callback) {
        final OpenChannel copiedChannel = OpenChannel.clone(channel);
        final List<BaseMessage> copiedMessage = Collections.unmodifiableList(messageList);
        service.submit(() -> {
            final CountDownLatch lock = new CountDownLatch(1);
            final OpenChannelMessageDiffCallback diffCallback = new OpenChannelMessageDiffCallback(
                OpenChannelMessageListAdapter.this.channel,
                channel,
                OpenChannelMessageListAdapter.this.messageList,
                messageList,
                messageListUIParams.shouldUseMessageGroupUI(),
                messageListUIParams.shouldUseReverseLayout()
            );
            final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

            SendbirdUIKit.runOnUIThread(() -> {
                try {
                    OpenChannelMessageListAdapter.this.messageList = copiedMessage;
                    OpenChannelMessageListAdapter.this.channel = copiedChannel;
                    diffResult.dispatchUpdatesTo(OpenChannelMessageListAdapter.this);
                    if (callback != null) {
                        callback.onListUpdated(messageList);
                    }
                } finally {
                    lock.countDown();
                }
            });
            lock.await();
            return true;
        });
    }

    /**
     * Animates the view holder with the corresponding position.
     *
     * @param animation Animation you want to apply to the view holder
     * @param position  Position of the view holder to be applied
     */
    public void startAnimation(@NonNull Animation animation, int position) {
        notifyItemChanged(position, animation);
    }

    @Override
    public void onViewRecycled(@NonNull MessageViewHolder holder) {
        final Map<String, View> clickableViewMap = holder.getClickableViewMap();
        if (!clickableViewMap.isEmpty()) {
            final View view = clickableViewMap.get(ClickableViewIdentifier.Chat.name());
            if (view != null && view.getAnimation() != null) {
                view.getAnimation().cancel();
            }
        }
    }

    /**
     * Register a callback to be invoked when the {@link MessageViewHolder#itemView} is clicked.
     *
     * @param listener The callback that will run
     * since 2.2.0
     */
    public void setOnListItemClickListener(@Nullable OnIdentifiableItemClickListener<BaseMessage> listener) {
        this.listItemClickListener = listener;
    }

    /**
     * Returns a callback to be invoked when the {@link MessageViewHolder#itemView} is clicked.
     *
     * @return {@code OnIdentifiableItemClickListener<BaseMessage>} to be invoked when the {@link MessageViewHolder#itemView} is clicked.
     * since 3.0.0
     */
    @Nullable
    public OnIdentifiableItemClickListener<BaseMessage> getOnListItemClickListener() {
        return listItemClickListener;
    }

    /**
     * Register a callback to be invoked when the {@link MessageViewHolder#itemView} is long clicked and held.
     *
     * @param listener The callback that will run
     * since 2.2.0
     */
    public void setOnListItemLongClickListener(@Nullable OnIdentifiableItemLongClickListener<BaseMessage> listener) {
        this.listItemLongClickListener = listener;
    }

    /**
     * Returns a callback to be invoked when the {@link MessageViewHolder#itemView} is long clicked and held.
     *
     * @return OnIdentifiableItemLongClickListener<BaseMessage> to be invoked when the {@link MessageViewHolder#itemView} is long clicked and held.
     * since 3.0.0
     */
    @Nullable
    public OnIdentifiableItemLongClickListener<BaseMessage> getOnListItemLongClickListener() {
        return listItemLongClickListener;
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
     * @return The {@link BaseMessage} to retrieve the position of in this adapter.
     */
    @Override
    @NonNull
    public BaseMessage getItem(int position) {
        return messageList.get(position);
    }

    /**
     * Returns the {@link List<BaseMessage>} in the data set held by the adapter.
     *
     * @return The {@link List<BaseMessage>} in this adapter.
     */
    @Override
    @NonNull
    public List<BaseMessage> getItems() {
        return Collections.unmodifiableList(messageList);
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
}
