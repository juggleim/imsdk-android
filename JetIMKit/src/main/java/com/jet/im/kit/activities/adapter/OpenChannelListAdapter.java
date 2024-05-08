package com.jet.im.kit.activities.adapter;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.recyclerview.widget.DiffUtil;

import com.sendbird.android.channel.OpenChannel;
import com.jet.im.kit.R;
import com.jet.im.kit.activities.viewholder.BaseViewHolder;
import com.jet.im.kit.databinding.SbViewOpenChannelPreviewBinding;
import com.jet.im.kit.interfaces.OnItemClickListener;
import com.jet.im.kit.interfaces.OnItemLongClickListener;
import com.jet.im.kit.internal.model.OpenChannelDiffCallback;
import com.jet.im.kit.internal.model.OpenChannelInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * OpenChannelListAdapter provides a binding from a {@link OpenChannel} type data set to views that are displayed within a RecyclerView.
 * since 3.2.0
 */
public class OpenChannelListAdapter extends BaseAdapter<OpenChannel, BaseViewHolder<OpenChannel>> {
    @NonNull
    private final List<OpenChannel> openChannelList = new ArrayList<>();
    @NonNull
    private List<OpenChannelInfo> cachedChannelList = new ArrayList<>();
    @Nullable
    private OnItemClickListener<OpenChannel> listener;
    @Nullable
    private OnItemLongClickListener<OpenChannel> longClickListener;

    /**
     * Constructor
     */
    public OpenChannelListAdapter() {
    }

    /**
     * Constructor
     *
     * @param listener The listener performing when the {@link OpenChannelPreviewHolder} is clicked.
     */
    public OpenChannelListAdapter(@Nullable OnItemClickListener<OpenChannel> listener) {
        setOnItemClickListener(listener);
    }

    /**
     * Called when RecyclerView needs a new {@link BaseViewHolder<OpenChannel>} of the given type to represent
     * an item.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return A new {@link BaseViewHolder<OpenChannel>} that holds a View of the given view type.
     * @see #getItemViewType(int)
     * @see #onBindViewHolder(BaseViewHolder, int)
     * since 3.2.0
     */
    @NonNull
    @Override
    public BaseViewHolder<OpenChannel> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final TypedValue values = new TypedValue();
        parent.getContext().getTheme().resolveAttribute(R.attr.sb_component_list, values, true);
        final Context contextWrapper = new ContextThemeWrapper(parent.getContext(), values.resourceId);
        return new OpenChannelPreviewHolder(SbViewOpenChannelPreviewBinding.inflate(LayoutInflater.from(contextWrapper), parent, false));
    }

    /**
     * Called by RecyclerView to display the data at the specified position. This method should
     * update the contents of the {@link BaseViewHolder#itemView} to reflect the item at the given
     * position.
     *
     * @param holder   The {@link BaseViewHolder<OpenChannel>} which should be updated to represent
     *                 the contents of the item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     * since 3.2.0
     */
    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder<OpenChannel> holder, int position) {
        final OpenChannel channel = getItem(position);
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
     * since 3.2.0
     */
    public void setOnItemClickListener(@Nullable OnItemClickListener<OpenChannel> listener) {
        this.listener = listener;
    }

    /**
     * Returns a callback to be invoked when the {@link BaseViewHolder#itemView} is clicked.
     *
     * @return {@code OnItemClickListener<OpenChannel>} to be invoked when the {@link BaseViewHolder#itemView} is clicked.
     * since 3.2.0
     */
    @Nullable
    public OnItemClickListener<OpenChannel> getOnItemClickListener() {
        return listener;
    }

    /**
     * Register a callback to be invoked when the {@link BaseViewHolder#itemView} is clicked and held.
     *
     * @param listener The callback that will run
     * since 3.2.0
     */
    public void setOnItemLongClickListener(@Nullable OnItemLongClickListener<OpenChannel> listener) {
        this.longClickListener = listener;
    }

    /**
     * Returns a callback to be invoked when the {@link BaseViewHolder#itemView} is clicked and held.
     *
     * @return {@code OnItemLongClickListener<OpenChannel>} to be invoked when the {@link BaseViewHolder#itemView} is clicked and held.
     * since 3.2.0
     */
    @Nullable
    public OnItemLongClickListener<OpenChannel> getOnItemLongClickListener() {
        return longClickListener;
    }

    /**
     * Returns the {@link List<OpenChannel>} in the data set held by the adapter.
     *
     * @return The {@link List<OpenChannel>} in this adapter.
     * since 3.2.0
     */
    @SuppressLint("KotlinPropertyAccess")
    @Override
    @NonNull
    public List<OpenChannel> getItems() {
        return Collections.unmodifiableList(openChannelList);
    }

    /**
     * Returns the {@link OpenChannel} in the data set held by the adapter.
     *
     * @param position The position of the item within the adapter's data set.
     * @return The {@link OpenChannel} to retrieve the position of in this adapter.
     * since 3.2.0
     */
    @NonNull
    @Override
    public OpenChannel getItem(int position) {
        return openChannelList.get(position);
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     * since 3.2.0
     */
    @Override
    public int getItemCount() {
        return openChannelList.size();
    }

    /**
     * Sets the {@link List<OpenChannel>} to be displayed.
     *
     * @param openChannelList list to be displayed
     * since 3.2.0
     */
    public void setItems(@NonNull List<OpenChannel> openChannelList) {
        final List<OpenChannelInfo> newChannelInfo = OpenChannelInfo.toChannelInfo(openChannelList);
        final OpenChannelDiffCallback diffCallback = new OpenChannelDiffCallback(this.cachedChannelList, newChannelInfo);
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        this.openChannelList.clear();
        this.openChannelList.addAll(openChannelList);
        this.cachedChannelList = newChannelInfo;
        diffResult.dispatchUpdatesTo(this);
    }

    private static class OpenChannelPreviewHolder extends BaseViewHolder<OpenChannel> {
        @NonNull
        private final SbViewOpenChannelPreviewBinding binding;

        OpenChannelPreviewHolder(@NonNull SbViewOpenChannelPreviewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @Override
        public void bind(@NonNull OpenChannel channel) {
            binding.channelPreview.drawChannel(channel);
        }
    }
}
