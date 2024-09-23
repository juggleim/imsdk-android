package com.jet.im.kit.activities.adapter;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.recyclerview.widget.DiffUtil;

import com.jet.im.kit.R;
import com.jet.im.kit.activities.viewholder.BaseViewHolder;
import com.jet.im.kit.databinding.SbViewChannelPreviewBinding;
import com.jet.im.kit.interfaces.MessageDisplayDataProvider;
import com.jet.im.kit.interfaces.OnItemClickListener;
import com.jet.im.kit.interfaces.OnItemLongClickListener;
import com.jet.im.kit.model.ChannelListUIParams;
import com.jet.im.kit.model.configurations.UIKitConfig;
import com.juggle.im.model.Conversation;
import com.juggle.im.model.ConversationInfo;
import com.juggle.im.model.Message;
import com.sendbird.android.channel.GroupChannel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ChannelListAdapter provides a binding from a {@link GroupChannel} type data set to views that are displayed within a RecyclerView.
 */
public class ChannelListAdapter extends BaseAdapter<ConversationInfo, BaseViewHolder<ConversationInfo>> {
    @NonNull
    private final List<ConversationInfo> channelList = new ArrayList<>();
    @NonNull
    private List<ChannelInfo> cachedChannelList = new ArrayList<>();
    @Nullable
    private OnItemClickListener<ConversationInfo> listener;
    @Nullable
    private OnItemLongClickListener<ConversationInfo> longClickListener;
    @Nullable
    private MessageDisplayDataProvider messageDisplayDataProvider;
    @NonNull
    private final ChannelListUIParams params;

    /**
     * Constructor
     */
    public ChannelListAdapter() {
        this(null);
    }

    /**
     * Constructor
     *
     * @param listener The listener performing when the {@link ChannelPreviewHolder} is clicked.
     */
    public ChannelListAdapter(@Nullable OnItemClickListener<ConversationInfo> listener) {
        this(listener, new ChannelListUIParams());
    }

    public ChannelListAdapter(@Nullable OnItemClickListener<ConversationInfo> listener, @NonNull ChannelListUIParams params) {
        setOnItemClickListener(listener);
        setOnItemLongClickListener(longClickListener);
        this.params = params;
    }

    /**
     * Called when RecyclerView needs a new {@link BaseViewHolder<ConversationInfo>} of the given type to represent
     * an item.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return A new {@link BaseViewHolder<ConversationInfo>} that holds a View of the given view type.
     * @see #getItemViewType(int)
     * @see #onBindViewHolder(BaseViewHolder, int)
     */
    @NonNull
    @Override
    public BaseViewHolder<ConversationInfo> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final TypedValue values = new TypedValue();
        parent.getContext().getTheme().resolveAttribute(R.attr.sb_component_list, values, true);
        final Context contextWrapper = new ContextThemeWrapper(parent.getContext(), values.resourceId);
        return new ChannelPreviewHolder(SbViewChannelPreviewBinding.inflate(LayoutInflater.from(contextWrapper), parent, false), params);
    }

    /**
     * Called by RecyclerView to display the data at the specified position. This method should
     * update the contents of the {@link BaseViewHolder#itemView} to reflect the item at the given
     * position.
     *
     * @param holder   The {@link BaseViewHolder<ConversationInfo>} which should be updated to represent
     *                 the contents of the item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder<ConversationInfo> holder, int position) {
        final ConversationInfo channel = getItem(position);
        holder.bind(channel);

        holder.itemView.setOnClickListener(v -> {
            int channelPosition = holder.getBindingAdapterPosition();
            if (channelPosition != NO_POSITION && listener != null) {
                listener.onItemClick(v, channelPosition, getItem(channelPosition));
            }
        });
        holder.itemView.setOnLongClickListener(v -> {
            int channelPosition = holder.getBindingAdapterPosition();
            if (channelPosition != NO_POSITION && longClickListener != null) {
                longClickListener.onItemLongClick(v, channelPosition, getItem(channelPosition));
                return true;
            }
            return false;
        });
    }

    /**
     * Register a callback to be invoked when the {@link BaseViewHolder#itemView} is clicked.
     *
     * @param listener The callback that will run
     */
    public void setOnItemClickListener(@Nullable OnItemClickListener<ConversationInfo> listener) {
        this.listener = listener;
    }

    /**
     * Returns a callback to be invoked when the {@link BaseViewHolder#itemView} is clicked.
     *
     * @return {@code OnItemClickListener<ConversationInfo>} to be invoked when the {@link BaseViewHolder#itemView} is clicked.
     * since 3.0.0
     */
    @Nullable
    public OnItemClickListener<ConversationInfo> getOnItemClickListener() {
        return listener;
    }

    /**
     * Register a callback to be invoked when the {@link BaseViewHolder#itemView} is clicked and held.
     *
     * @param listener The callback that will run
     */
    public void setOnItemLongClickListener(@Nullable OnItemLongClickListener<ConversationInfo> listener) {
        this.longClickListener = listener;
    }

    /**
     * Returns a callback to be invoked when the {@link BaseViewHolder#itemView} is clicked and held.
     *
     * @return {@code OnItemLongClickListener<ConversationInfo>} to be invoked when the {@link BaseViewHolder#itemView} is clicked and held.
     * since 3.0.0
     */
    @Nullable
    public OnItemLongClickListener<ConversationInfo> getOnItemLongClickListener() {
        return longClickListener;
    }

    /**
     * Sets {@link MessageDisplayDataProvider}, which is used to generate data before they are sent or rendered.
     * The generated value is primarily used when the view is rendered.
     * The generated data will be applied to the last message of a channel in this adapter.
     * since 3.5.7
     */
    public void setMessageDisplayDataProvider(@Nullable MessageDisplayDataProvider messageDisplayDataProvider) {
        this.messageDisplayDataProvider = messageDisplayDataProvider;
    }

    /**
     * Returns the {@link List<ConversationInfo>} in the data set held by the adapter.
     *
     * @return The {@link List<ConversationInfo>} in this adapter.
     */
    @Override
    @NonNull
    public List<ConversationInfo> getItems() {
        return Collections.unmodifiableList(channelList);
    }

    /**
     * Returns the {@link ConversationInfo} in the data set held by the adapter.
     *
     * @param position The position of the item within the adapter's data set.
     * @return The {@link ConversationInfo} to retrieve the position of in this adapter.
     */
    @NonNull
    @Override
    public ConversationInfo getItem(int position) {
        return channelList.get(position);
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return channelList.size();
    }

    /**
     * Sets the {@link List<ConversationInfo>} to be displayed.
     *
     * @param channelList list to be displayed
     */
    public void setItems(@NonNull List<ConversationInfo> channelList) {
        if (messageDisplayDataProvider == null || messageDisplayDataProvider.shouldRunOnUIThread()) {
//            if (messageDisplayDataProvider != null)
//                MessageDisplayDataManager.checkAndGenerateDisplayDataFromChannelList(channelList, messageDisplayDataProvider);
            notifyChannelListChanged(channelList);
            return;
        }

        messageDisplayDataProvider.threadPool().submit(() -> {
//            MessageDisplayDataManager.checkAndGenerateDisplayDataFromChannelList(channelList, messageDisplayDataProvider);
            notifyChannelListChanged(channelList);
        });
    }

    private void notifyChannelListChanged(@NonNull List<ConversationInfo> channelList) {
        final List<ChannelInfo> newChannelInfo = ChannelInfo.toChannelInfoList(channelList, new ChannelListUIParams());
        final ChannelDiffCallback diffCallback = new ChannelDiffCallback(this.cachedChannelList, newChannelInfo);
        final DiffUtil.DiffResult diffResult = calculateDiff(diffCallback);

        this.channelList.clear();
        this.channelList.addAll(channelList);
        this.cachedChannelList = newChannelInfo;
        diffResult.dispatchUpdatesTo(this);
    }

    @VisibleForTesting
    @NonNull
    DiffUtil.DiffResult calculateDiff(ChannelDiffCallback diffCallback) {
        return DiffUtil.calculateDiff(diffCallback);
    }

    private static class ChannelPreviewHolder extends BaseViewHolder<ConversationInfo> {
        @NonNull
        private final SbViewChannelPreviewBinding binding;

        ChannelPreviewHolder(@NonNull SbViewChannelPreviewBinding binding, @NonNull ChannelListUIParams params) {
            super(binding.getRoot());
            this.binding = binding;
            this.binding.channelPreview.setUseTypingIndicator(false);
            this.binding.channelPreview.setUseMessageReceiptStatus(false);
            this.binding.channelPreview.setUseUnreadMentionCount(false);
        }

        @Override
        public void bind(@NonNull ConversationInfo channel) {
            binding.channelPreview.drawChannel(channel);
        }
    }

    static class ChannelInfo {
        private final Conversation.ConversationType conversationType;
        private final String conversationId;
        private final ChannelListUIParams params;
        private final int unreadCount;
        private final long updateTime;
        @Nullable
        private final Message lastMessage;
        private final boolean isTop;
        private final long topTime;
        private final boolean mute;
        private final String draft;

        ChannelInfo(@NonNull ConversationInfo channel, @NonNull ChannelListUIParams params) {
            this.params = params;
            conversationType = channel.getConversation().getConversationType();
            conversationId = channel.getConversation().getConversationId();
            unreadCount = channel.getUnreadCount();
            updateTime = channel.getSortTime();
            lastMessage = channel.getLastMessage();
            isTop = channel.isTop();
            topTime = channel.getTopTime();
            mute = channel.isMute();
            draft = channel.getDraft();
        }

        public Conversation.ConversationType getConversationType() {
            return conversationType;
        }

        public String getConversationId() {
            return conversationId;
        }

        public ChannelListUIParams getParams() {
            return params;
        }

        public int getUnreadCount() {
            return unreadCount;
        }

        public long getUpdateTime() {
            return updateTime;
        }

        @Nullable
        public Message getLastMessage() {
            return lastMessage;
        }

        public boolean isTop() {
            return isTop;
        }

        public long getTopTime() {
            return topTime;
        }

        public boolean isMute() {
            return mute;
        }

        public String getDraft() {
            return draft;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            return super.equals(obj);
            //todo 判断相等
        }

        @Override
        public int hashCode() {
            return super.hashCode();
            //todo 区别新旧
//            int result = channelUrl.hashCode();
//            result = 31 * result + (int) (createdAt ^ (createdAt >>> 32));
//            result = 31 * result + memberCount;
//            result = 31 * result + (lastMessage != null ? lastMessage.hashCode() : 0);
//            result = 31 * result + channelName.hashCode();
//            result = 31 * result + (coverImageUrl != null ? coverImageUrl.hashCode() : 0);
//            result = 31 * result + coverImageHash;
//            result = 31 * result + (pushTriggerOption != null ? pushTriggerOption.hashCode() : 0);
//            result = 31 * result + unreadMessageCount;
//            result = 31 * result + unreadMentionCount;
//            result = 31 * result + (isFrozen ? 1 : 0);
//
//            if (params.getEnableTypingIndicator()) {
//                result = 31 * result + typingMembers.hashCode();
//            }
//
//            if (params.getEnableMessageReceiptStatus()) {
//                result = 31 * result + unReadMemberCount;
//                result = 31 * result + unDeliveredMemberCount;
//            }
//            return result;
        }

        @NonNull
        static List<ChannelInfo> toChannelInfoList(@NonNull List<ConversationInfo> channelList, @NonNull ChannelListUIParams params) {
            List<ChannelInfo> results = new ArrayList<>();
            for (ConversationInfo channel : channelList) {
                results.add(new ChannelInfo(channel, params));
            }
            return results;
        }
    }
}
