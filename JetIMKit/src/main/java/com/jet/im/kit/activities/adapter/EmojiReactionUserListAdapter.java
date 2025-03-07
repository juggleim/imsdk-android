package com.jet.im.kit.activities.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import com.juggle.im.model.UserInfo;
import com.jet.im.kit.activities.viewholder.BaseViewHolder;
import com.jet.im.kit.databinding.SbViewEmojiReactionUserBinding;
import com.jet.im.kit.interfaces.OnItemClickListener;
import com.jet.im.kit.internal.ui.reactions.EmojiReactionUserView;

import java.util.ArrayList;
import java.util.List;

/**
 * EmojiReactionUserListAdapter provides a binding from a {@link UserInfo} set to views that are displayed within a RecyclerView.
 *
 * since 1.1.0
 */
public class EmojiReactionUserListAdapter extends BaseAdapter<UserInfo, BaseViewHolder<UserInfo>> {
    @NonNull
    final private List<UserInfo> userList;
    @Nullable
    private OnItemClickListener<UserInfo> emojiReactionUserListProfileClickListener;

    /**
     * Constructor
     * since 1.1.0
     */
    public EmojiReactionUserListAdapter() {
        this(new ArrayList<>());
    }

    /**
     * Constructor
     *
     * @param userList list to be displayed.
     * since 1.1.0
     */
    public EmojiReactionUserListAdapter(@NonNull List<UserInfo> userList) {
        this.userList = userList;
    }

    /**
     * Called when RecyclerView needs a new {@link EmojiReactionUserViewHolder} of the given type to represent
     * an item.
     *
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     *               an adapter position.
     * @param viewType The view type of the new View.
     *
     * @return A new {@link BaseViewHolder<UserInfo>} that holds a View of the given view type.
     * @see #getItemViewType(int)
     * @see #onBindViewHolder(BaseViewHolder, int)
     * since 1.1.0
     */
    @NonNull
    @Override
    public BaseViewHolder<UserInfo> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final EmojiReactionUserViewHolder viewHolder = new EmojiReactionUserViewHolder(SbViewEmojiReactionUserBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        if (viewHolder.itemView instanceof EmojiReactionUserView) {
            final EmojiReactionUserView view = (EmojiReactionUserView) viewHolder.itemView;
            view.setOnProfileClickListener(v -> {
                if (emojiReactionUserListProfileClickListener != null) {
                    final int position = viewHolder.getAbsoluteAdapterPosition();
                    final UserInfo user = getItem(position);
                    if (user != null) {
                        emojiReactionUserListProfileClickListener.onItemClick(v, position, user);
                    }
                }
            });
        }
        return viewHolder;
    }

    /**
     * Called by RecyclerView to display the data at the specified position. This method should
     * update the contents of the {@link BaseViewHolder#itemView} to reflect the item at the given
     * position.
     *
     * @param holder The {@link BaseViewHolder<UserInfo>} which should be updated to represent
     *               the contents of the item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     * since 1.1.0
     */
    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder<UserInfo> holder, int position) {
        UserInfo userInfo = getItem(position);
        if (holder instanceof EmojiReactionUserViewHolder) {
            ((EmojiReactionUserViewHolder) holder).bind(userInfo);
        } else {
            if (userInfo != null) {
                holder.bind(userInfo);
            }
        }
    }

    /**
     * Returns the {@link UserInfo} in the data set held by the adapter.
     *
     * @param position The position of the item within the adapter's data set.
     * @return The {@link UserInfo} to retrieve the position of in this adapter.
     * since 1.1.0
     */
    @Override
    @Nullable
    public UserInfo getItem(int position) {
        return userList.get(position);
    }

    /**
     * Returns the {@link List<UserInfo>} in the data set held by the adapter.
     *
     * @return The {@link List<UserInfo>} in this adapter.
     * since 1.1.0
     */
    @Override
    @NonNull
    public List<UserInfo> getItems() {
        return userList;
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     * since 1.1.0
     */
    @Override
    public int getItemCount() {
        return userList.size();
    }

    /**
     * Sets the {@link List<UserInfo>} to be displayed.
     *
     * @param userList list to be displayed
     * since 1.1.0
     */
    public void setItems(@NonNull List<UserInfo> userList) {
        final EmojiReactionUserDiffCallback diffCallback = new EmojiReactionUserDiffCallback(this.userList, userList);
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        this.userList.clear();
        this.userList.addAll(userList);
        diffResult.dispatchUpdatesTo(this);
    }

    public void setOnEmojiReactionUserListProfileClickListener(@Nullable OnItemClickListener<UserInfo> emojiReactionUserListProfileClickListener) {
        this.emojiReactionUserListProfileClickListener = emojiReactionUserListProfileClickListener;
    }


    private static class EmojiReactionUserViewHolder extends BaseViewHolder<com.juggle.im.model.UserInfo> {
        private final SbViewEmojiReactionUserBinding binding;

        EmojiReactionUserViewHolder(@NonNull SbViewEmojiReactionUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @Override
        public void bind(@Nullable UserInfo user) {
            binding.userViewHolder.drawUser(user);
        }
    }

    private static class EmojiReactionUserDiffCallback extends DiffUtil.Callback {
        @NonNull
        private final List<UserInfo> oldUserList;
        @NonNull
        private final List<UserInfo> newUserList;

        EmojiReactionUserDiffCallback(@NonNull List<UserInfo> oldUserList, @NonNull List<UserInfo> newUserList) {
            this.oldUserList = oldUserList;
            this.newUserList = newUserList;
        }

        @Override
        public int getOldListSize() {
            return oldUserList.size();
        }

        @Override
        public int getNewListSize() {
            return newUserList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            final UserInfo oldUser = oldUserList.get(oldItemPosition);
            final UserInfo newUser = newUserList.get(newItemPosition);
            if (oldUser == null || newUser == null) return false;

            return oldUser.equals(newUser);
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            final UserInfo oldUser = oldUserList.get(oldItemPosition);
            final UserInfo newUser = newUserList.get(newItemPosition);

            if (!areItemsTheSame(oldItemPosition, newItemPosition)) {
                return false;
            }

            String oldNickname = oldUser.getUserName();
            String newNickname = newUser.getUserName();
            if (!newNickname.equals(oldNickname)) {
                return false;
            }

            String oldProfileUrl = oldUser.getPortrait();
            String newProfileUrl = newUser.getPortrait();
            return newProfileUrl.equals(oldProfileUrl);
        }
    }
}
