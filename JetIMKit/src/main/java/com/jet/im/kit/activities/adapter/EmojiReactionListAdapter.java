package com.jet.im.kit.activities.adapter;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import com.jet.im.kit.internal.extensions.EmojiExtensionsKt;
import com.jet.im.kit.model.EmojiManager2;
import com.juggle.im.JIM;
import com.juggle.im.model.MessageReactionItem;
import com.juggle.im.model.UserInfo;
import com.sendbird.android.SendbirdChat;
import com.sendbird.android.message.Reaction;
import com.jet.im.kit.activities.viewholder.BaseViewHolder;
import com.jet.im.kit.databinding.SbViewEmojiReactionBinding;
import com.jet.im.kit.interfaces.OnItemClickListener;
import com.jet.im.kit.interfaces.OnItemLongClickListener;
import com.jet.im.kit.internal.ui.reactions.EmojiReactionView;
import com.jet.im.kit.internal.ui.viewholders.EmojiReactionMoreViewHolder;
import com.jet.im.kit.internal.ui.viewholders.EmojiReactionViewHolder;
import com.jet.im.kit.log.Logger;
import com.jet.im.kit.model.EmojiManager;

import java.util.ArrayList;
import java.util.List;

/**
 * EmojiReactionListAdapter provides a binding from a {@link Reaction} set to views that are displayed within a RecyclerView.
 *
 * since 1.1.0
 */
public class EmojiReactionListAdapter extends BaseAdapter<MessageReactionItem, BaseViewHolder<MessageReactionItem>> {
    private static final int VIEW_EMOJI_REACTION = 0;
    private static final int VIEW_EMOJI_REACTION_MORE = 1;

    @NonNull
    private final List<MessageReactionItem> reactionList = new ArrayList<>();
    @Nullable
    private OnItemClickListener<String> emojiReactionClickListener;
    @Nullable
    private OnItemLongClickListener<String> emojiReactionLongClickListener;
    @Nullable
    private View.OnClickListener moreButtonClickListener;
    private boolean useMoreButton = true;
    private boolean clickable = true;
    private boolean longClickable = true;
    @NonNull
    private List<String> totalEmojiList = EmojiManager2.INSTANCE.getEmojiList();

    /**
     * Called when RecyclerView needs a new {@link BaseViewHolder<Reaction>} of the given type to represent
     * an item.
     *
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     *               an adapter position.
     * @param viewType The view type of the new View.
     *
     * @return A new {@link BaseViewHolder<Reaction>} that holds a View of the given view type.
     * @see #getItemViewType(int)
     * @see #onBindViewHolder(BaseViewHolder, int)
     * since 1.1.0
     */
    @NonNull
    @Override
    public BaseViewHolder<MessageReactionItem> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_EMOJI_REACTION) {
            return new EmojiReactionViewHolder(SbViewEmojiReactionBinding.inflate(inflater, parent, false));
        } else {
            return new EmojiReactionMoreViewHolder(new EmojiReactionView(parent.getContext()));
        }
    }

    /**
     * Called by RecyclerView to display the data at the specified position. This method should
     * update the contents of the {@link BaseViewHolder#itemView} to reflect the item at the given
     * position.
     *
     * @param holder The {@link BaseViewHolder<Reaction>} which should be updated to represent
     *               the contents of the item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     * since 1.1.0
     */
    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder<MessageReactionItem> holder, int position) {
        int type = getItemViewType(position);

        if (type == VIEW_EMOJI_REACTION_MORE) {
            holder.itemView.setOnClickListener(v -> {
                int reactionPosition = holder.getBindingAdapterPosition();
                if (reactionPosition != NO_POSITION && moreButtonClickListener != null) {
                    moreButtonClickListener.onClick(v);
                }
            });
        } else {
            MessageReactionItem current = getItem(position);

            if (current != null) {
                List<UserInfo> userInfoList = current.getUserInfoList();
                boolean isContain = false;
                for (UserInfo userInfo : userInfoList) {
                    if (userInfo.getUserId().equals(JIM.getInstance().getCurrentUserId())) {
                        isContain = true;
                        break;
                    }
                }
                holder.itemView.setSelected(isContain);
            }

            Logger.d("++ isClickable = %s, longClickable=%s", clickable, longClickable);
            if (this.clickable) {
                holder.itemView.setOnClickListener(v -> {
                    int reactionPosition = holder.getBindingAdapterPosition();
                    if (reactionPosition != NO_POSITION && emojiReactionClickListener != null) {
                        final MessageReactionItem reactionItem = getItem(reactionPosition);
                        emojiReactionClickListener.onItemClick(v,
                            reactionPosition,
                                reactionItem != null ? reactionItem.getReactionId() : "");
                    }
                });
            } else {
                holder.itemView.setOnClickListener(null);
            }

            if (this.longClickable) {
                holder.itemView.setOnLongClickListener(v -> {
                    int reactionPosition = holder.getBindingAdapterPosition();
                    if (reactionPosition != NO_POSITION && emojiReactionLongClickListener != null) {
                        final MessageReactionItem reactionItem = getItem(reactionPosition);
                        emojiReactionLongClickListener.onItemLongClick(v,
                            reactionPosition,
                                reactionItem != null ? reactionItem.getReactionId() : "");
                        return true;
                    }
                    return false;
                });
            } else {
                holder.itemView.setOnLongClickListener(null);
            }

            if (current == null) return;
            holder.bind(current);
        }
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     * since 1.1.0
     */
    @Override
    public int getItemCount() {
        if (EmojiExtensionsKt.hasAllEmoji(reactionList, totalEmojiList)) {
            return reactionList.size();
        } else {
            return reactionList.size() + (useMoreButton ? 1 : 0);
        }
    }

    /**
     * Returns the {@link Reaction} in the data set held by the adapter.
     *
     * @param position The position of the item within the adapter's data set.
     * @return The {@link Reaction} to retrieve the position of in this adapter.
     * since 1.1.0
     */
    @Override
    @Nullable
    public MessageReactionItem getItem(int position) {
        if (position >= reactionList.size()) {
            return null;
        }
        return reactionList.get(position);
    }

    /**
     * Returns the {@link List<Reaction>} in the data set held by the adapter.
     *
     * @return The {@link List<Reaction>} in this adapter.
     * since 1.1.0
     */
    @Override
    @NonNull
    public List<MessageReactionItem> getItems() {
        return reactionList;
    }

    /**
     * Return the view type of the {@link BaseViewHolder<Reaction>} at <code>position</code> for the purposes
     * of view recycling.
     *
     * @param position position to query
     * @return integer value identifying the type of the view needed to represent the item at <code>position</code>.
     * since 1.1.0
     */
    @Override
    public int getItemViewType(int position) {
        if (position >= reactionList.size()) {
            return VIEW_EMOJI_REACTION_MORE;
        }
        return VIEW_EMOJI_REACTION;
    }

    /**
     * Sets the total emoji list that can be added to the current message.
     * This value is used to compare whether `add` button should be displayed from the reactions view.
     * @param totalEmojiList The total emoji list that can be added to the current message. Defaults to {@link EmojiManager#getAllEmojis()}.
     * @since 3.20.0
     */
    public void setTotalEmojiList(@NonNull List<String> totalEmojiList) {
        this.totalEmojiList = totalEmojiList;
    }

    /**
     * Sets the {@link List<Reaction>} to be displayed.
     *
     * @param reactionList list to be displayed
     * since 1.1.0
     */
    public void setReactionList(@NonNull List<MessageReactionItem> reactionList) {
        this.reactionList.clear();
        this.reactionList.addAll(reactionList);
        notifyDataSetChanged();
        //todo diff
//        final EmojiReactionDiffCallback diffCallback = new EmojiReactionDiffCallback(this.reactionList, reactionList);
//        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);
//
//        this.reactionList.clear();
//        this.reactionList.addAll(reactionList);
//        diffResult.dispatchUpdatesTo(this);
    }

    /**
     * Register a callback to be invoked when the emoji reaction is clicked and held.
     *
     * @param emojiReactionClickListener The callback that will run
     * since 1.1.0
     */
    public void setEmojiReactionClickListener(@Nullable OnItemClickListener<String> emojiReactionClickListener) {
        this.emojiReactionClickListener = emojiReactionClickListener;
    }

    /**
     * Register a callback to be invoked when the emoji reaction is long clicked and held.
     *
     * @param emojiReactionLongClickListener The callback that will run
     * since 1.1.0
     */
    public void setEmojiReactionLongClickListener(@Nullable OnItemLongClickListener<String> emojiReactionLongClickListener) {
        this.emojiReactionLongClickListener = emojiReactionLongClickListener;
    }

    /**
     * Register a callback to be invoked when the emoji reaction more button is clicked and held.
     *
     * @param moreButtonClickListener The callback that will run
     * since 1.1.0
     */
    public void setMoreButtonClickListener(@Nullable View.OnClickListener moreButtonClickListener) {
        this.moreButtonClickListener = moreButtonClickListener;
    }

    /**
     * Sets a value if using the more button in the reaction view.
     *
     * @param useMoreButton true to make the view using more button, false otherwise
     * since 1.1.2
     */
    public void setUseMoreButton(boolean useMoreButton) {
        Logger.i("++ useMoreButton : %s", useMoreButton);
        this.useMoreButton = useMoreButton;
    }

    /**
     * Indicates whether this view using a more button or not.
     *
     * @return true if the view is using more button, false otherwise
     * since 1.1.2
     */
    public boolean useMoreButton() {
        return useMoreButton;
    }

    /**
     * Enables or disables click events for this view.
     *
     * @param clickable true to make the view clickable, false otherwise
     * since 1.1.2
     */
    public void setClickable(boolean clickable) {
        this.clickable = clickable;
    }

    /**
     * Enables or disables long click events for this view.
     *
     * @param longClickable true to make the view long clickable, false otherwise
     * since 1.1.2
     */
    public void setLongClickable(boolean longClickable) {
        this.longClickable = longClickable;
    }
}
