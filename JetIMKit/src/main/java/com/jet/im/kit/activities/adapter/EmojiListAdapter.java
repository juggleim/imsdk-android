package com.jet.im.kit.activities.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.juggle.im.JIM;
import com.juggle.im.model.MessageReactionItem;
import com.juggle.im.model.UserInfo;
import com.sendbird.android.message.Emoji;
import com.sendbird.android.message.Reaction;
import com.jet.im.kit.activities.viewholder.BaseViewHolder;
import com.jet.im.kit.databinding.SbViewEmojiBinding;
import com.jet.im.kit.interfaces.OnItemClickListener;
import com.jet.im.kit.internal.ui.reactions.EmojiView;
import com.jet.im.kit.internal.ui.viewholders.EmojiMoreViewHolder;
import com.jet.im.kit.internal.ui.viewholders.EmojiViewHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adapters provides a binding from an {@link Emoji} data set to views that are displayed within a RecyclerView.
 *
 * since 1.1.0
 */
public class EmojiListAdapter extends BaseAdapter<String, BaseViewHolder<String>> {
    private static final int VIEW_EMOJI = 0;
    private static final int VIEW_EMOJI_MORE = 1;

    @NonNull
    private final List<String> emojiList;
    @NonNull
    private final Map<String, List<String>> reactionUserMap = new HashMap<>();
    @Nullable
    private OnItemClickListener<String> emojiClickListener;
    @Nullable
    private View.OnClickListener moreButtonClickListener;
    private final boolean showMoreButton;

    /**
     * Constructor
     *
     * @param emojiList The {@link List<Emoji>} that contains the data needed for this adapter
     * @param reactionList The {@link List<Reaction>} that contains the data needed for this adapter
     * @param showMoreButton <code>true</code> if the more button is showed,
     *                       <code>false</code> otherwise.
     * since 1.1.0
     */
    public EmojiListAdapter(@NonNull List<String> emojiList,
                            @Nullable List<MessageReactionItem> reactionList,
                            boolean showMoreButton) {
        this.emojiList = emojiList;
        if (reactionList != null) {
            for (MessageReactionItem reaction : reactionList) {
                List<String> userIdList = new ArrayList<>();
                for (UserInfo userInfo : reaction.getUserInfoList()) {
                    userIdList.add(userInfo.getUserId());
                }
                reactionUserMap.put(reaction.getReactionId(), userIdList);
            }
        }
        this.showMoreButton = showMoreButton;
    }

    /**
     * Called when RecyclerView needs a new {@link BaseViewHolder<Emoji>} of the given type to represent
     * an item.
     *
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     *               an adapter position.
     * @param viewType The view type of the new View.
     *
     * @return A new {@link BaseViewHolder<Emoji>} that holds a View of the given view type.
     * @see #getItemViewType(int)
     * @see #onBindViewHolder(BaseViewHolder, int)
     * since 1.1.0
     */
    @NonNull
    @Override
    public BaseViewHolder<String> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_EMOJI_MORE) {
            return new EmojiMoreViewHolder(new EmojiView(parent.getContext()));
        } else {
            return new EmojiViewHolder(SbViewEmojiBinding.inflate(inflater, parent, false));
        }
    }

    /**
     * Called by RecyclerView to display the data at the specified position. This method should
     * update the contents of the {@link BaseViewHolder#itemView} to reflect the item at the given
     * position.
     *
     * @param holder The {@link BaseViewHolder<Emoji>} which should be updated to represent
     *               the contents of the item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     * since 1.1.0
     */
    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder<String> holder, int position) {
        String current = getItem(position);
        int type = getItemViewType(position);

        if (type == VIEW_EMOJI_MORE) {
            holder.itemView.setOnClickListener(v -> {
                if (moreButtonClickListener != null) {
                    moreButtonClickListener.onClick(v);
                }
            });
        } else {
            if (!reactionUserMap.isEmpty() && current != null) {
                List<String> userIds = reactionUserMap.get(current);
                holder.itemView.setSelected(userIds != null && JIM.getInstance().getCurrentUserId() != null && userIds.contains(JIM.getInstance().getCurrentUserId()));
            }

            holder.itemView.setOnClickListener(v -> {
                String emoji = getItem(holder.getBindingAdapterPosition());
                if (emojiClickListener != null && emoji != null) {
                    emojiClickListener.onItemClick(v,
                        holder.getBindingAdapterPosition(),
                        emoji);
                }
            });
        }

        if (current == null) return;
        holder.bind(current);
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     * since 1.1.0
     */
    @Override
    public int getItemCount() {
        if (showMoreButton) {
            return emojiList.size() + 1;
        } else {
            return emojiList.size();
        }
    }

    /**
     * Returns the {@link Emoji} in the data set held by the adapter.
     *
     * @param position The position of the item within the adapter's data set.
     * @return The {@link Emoji} to retrieve the position of in this adapter.
     * since 1.1.0
     */
    @Override
    @Nullable
    public String getItem(int position) {
        if (position >= emojiList.size()) {
            return null;
        }
        return emojiList.get(position);
    }

    /**
     * Returns the {@link List<Emoji>} in the data set held by the adapter.
     *
     * @return The {@link List<Emoji>} in this adapter.
     * since 1.1.0
     */
    @Override
    @NonNull
    public List<String> getItems() {
        return emojiList;
    }

    /**
     * Return the view type of the {@link BaseViewHolder<Emoji>} at <code>position</code> for the purposes
     * of view recycling.
     *
     * @param position position to query
     * @return integer value identifying the type of the view needed to represent the item at <code>position</code>.
     * since 1.1.0
     */
    @Override
    public int getItemViewType(int position) {
        if (showMoreButton && position >= emojiList.size()) {
            return VIEW_EMOJI_MORE;
        } else {
            return VIEW_EMOJI;
        }
    }

    /**
     * Register a callback to be invoked when the emoji is clicked and held.
     *
     * @param emojiClickListener The callback that will run
     * since 1.1.0
     */
    public void setEmojiClickListener(@Nullable OnItemClickListener<String> emojiClickListener) {
        this.emojiClickListener = emojiClickListener;
    }

    /**
     * Register a callback to be invoked when the emoji more button is clicked and held.
     *
     * @param moreButtonClickListener The callback that will run
     * since 1.1.0
     */
    public void setMoreButtonClickListener(@Nullable View.OnClickListener moreButtonClickListener) {
        this.moreButtonClickListener = moreButtonClickListener;
    }
}
