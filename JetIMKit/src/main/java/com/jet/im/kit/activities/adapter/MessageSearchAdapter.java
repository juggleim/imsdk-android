package com.jet.im.kit.activities.adapter;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.recyclerview.widget.DiffUtil;

import com.sendbird.android.message.BaseMessage;
import com.sendbird.android.user.User;
import com.jet.im.kit.R;
import com.jet.im.kit.activities.viewholder.BaseViewHolder;
import com.jet.im.kit.databinding.SbViewSearchResultPreviewBinding;
import com.jet.im.kit.interfaces.MessageDisplayDataProvider;
import com.jet.im.kit.interfaces.OnItemClickListener;
import com.jet.im.kit.internal.singleton.MessageDisplayDataManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * MessageSearchAdapter provides a binding from a {@link BaseMessage} type data set to views that are displayed within a RecyclerView.
 *
 * since 3.0.0
 */
public class MessageSearchAdapter extends BaseAdapter<BaseMessage, BaseViewHolder<BaseMessage>> {
    @NonNull
    private final List<BaseMessage> items = new ArrayList<>();
    @Nullable
    private OnItemClickListener<BaseMessage> listener;
    @Nullable
    private MessageDisplayDataProvider messageDisplayDataProvider;

    /**
     * Called when RecyclerView needs a new {@code BaseViewHolder<BaseMessage>} of the given type to represent
     * an item.
     *
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     *               an adapter position.
     * @param viewType The view type of the new View.
     *
     * @return A new {@code BaseViewHolder<BaseMessage>} that holds a View of the given view type.
     * @see #getItemViewType(int)
     * @see #onBindViewHolder(BaseViewHolder, int)
     */
    @NonNull
    @Override
    public BaseViewHolder<BaseMessage> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final TypedValue values = new TypedValue();
        parent.getContext().getTheme().resolveAttribute(R.attr.sb_component_list, values, true);
        final Context contextWrapper = new ContextThemeWrapper(parent.getContext(), values.resourceId);
        return new SearchResultViewHolder(SbViewSearchResultPreviewBinding.inflate(LayoutInflater.from(contextWrapper), parent, false));
    }

    /**
     * Called by RecyclerView to display the data at the specified position. This method should
     * update the contents of the {@link BaseViewHolder#itemView} to reflect the item at the given
     * position.
     *
     * @param holder The {@link BaseViewHolder<BaseMessage>} which should be updated to represent
     *               the contents of the item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder<BaseMessage> holder, int position) {
        holder.bind(getItem(position));
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
        return this.items.get(position);
    }

    /**
     * Returns the {@link List<BaseMessage>} in the data set held by the adapter.
     *
     * @return The {@link List<BaseMessage>} in this adapter.
     */
    @Override
    @NonNull
    public List<BaseMessage> getItems() {
        return Collections.unmodifiableList(items);
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * Sets the {@link List<BaseMessage>} to be displayed.
     *
     * @param items list to be displayed
     * since 2.1.0
     */
    public void setItems(@NonNull List<BaseMessage> items) {
        if (messageDisplayDataProvider == null || messageDisplayDataProvider.shouldRunOnUIThread()) {
            if (messageDisplayDataProvider != null)
                MessageDisplayDataManager.checkAndGenerateDisplayData(items, messageDisplayDataProvider);
            notifyMessageListChanged(items);
            return;
        }

        messageDisplayDataProvider.threadPool().submit(() -> {
            MessageDisplayDataManager.checkAndGenerateDisplayData(items, messageDisplayDataProvider);
            notifyMessageListChanged(items);
        });
    }

    private void notifyMessageListChanged(@NonNull List<BaseMessage> items) {
        final MessageSearchDiffCallback diffCallback = new MessageSearchDiffCallback(this.items, items);
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        this.items.clear();
        this.items.addAll(items);
        diffResult.dispatchUpdatesTo(this);
    }

    /**
     * Register a callback to be invoked when the {@link BaseViewHolder#itemView} is clicked.
     *
     * @param listener The callback that will run
     * since 2.1.0
     */
    public void setOnItemClickListener(@Nullable OnItemClickListener<BaseMessage> listener) {
        this.listener = listener;
    }

    /**
     * Returns a callback to be invoked when the {@link BaseViewHolder#itemView} is clicked.
     *
     * @return {@code OnItemClickListener<BaseMessage>} to be invoked when the {@link BaseViewHolder#itemView} is clicked.
     * since 3.0.0
     */
    @Nullable
    public OnItemClickListener<BaseMessage> getOnItemClickListener() {
        return listener;
    }

    /**
     * Sets {@link MessageDisplayDataProvider}, which is used to generate data before they are sent or rendered.
     * The generated value is primarily used when the view is rendered.
     * since 3.5.7
     */
    public void setMessageDisplayDataProvider(@Nullable MessageDisplayDataProvider messageDisplayDataProvider) {
        this.messageDisplayDataProvider = messageDisplayDataProvider;
    }

    private class SearchResultViewHolder extends BaseViewHolder<BaseMessage> {
        @NonNull
        private final SbViewSearchResultPreviewBinding binding;

        public SearchResultViewHolder(@NonNull SbViewSearchResultPreviewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            this.binding.getRoot().setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != NO_POSITION && listener != null) {
                    BaseMessage message = getItem(position);
                    listener.onItemClick(v, position, message);
                }
            });
        }

        @Override
        public void bind(@NonNull BaseMessage message) {
            binding.messagePreview.drawMessage(message);
        }
    }

    private static class MessageSearchDiffCallback extends DiffUtil.Callback {
        @NonNull
        private final List<BaseMessage> oldMessageList;
        @NonNull
        private final List<BaseMessage> newMessageList;

        public MessageSearchDiffCallback(@NonNull List<BaseMessage> oldMessageList, @NonNull List<BaseMessage> newMessageList) {
            this.oldMessageList = oldMessageList;
            this.newMessageList = newMessageList;
        }

        @Override
        public int getOldListSize() {
            return oldMessageList.size();
        }

        @Override
        public int getNewListSize() {
            return newMessageList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            BaseMessage oldMessage = oldMessageList.get(oldItemPosition);
            BaseMessage newMessage = newMessageList.get(newItemPosition);
            return oldMessage.equals(newMessage);
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            BaseMessage oldMessage = oldMessageList.get(oldItemPosition);
            BaseMessage newMessage = newMessageList.get(newItemPosition);

            final User oldSender = oldMessage.getSender();
            final User newSender = newMessage.getSender();

            if (oldSender == null) {
                return false;
            }

            if (!oldSender.equals(newSender)) {
                return false;
            }

            String oldNickname = oldSender.getNickname();
            String newNickname = newSender.getNickname();
            if (!newNickname.equals(oldNickname)) {
                return false;
            }

            String oldProfileUrl = oldSender.getProfileUrl();
            String newProfileUrl = newSender.getProfileUrl();
            if (!newProfileUrl.equals(oldProfileUrl)) {
                return false;
            }

            String oldMessageText = oldMessage.getMessage();
            String newMessageText = newMessage.getMessage();
            return newMessageText.equals(oldMessageText);
        }
    }
}
