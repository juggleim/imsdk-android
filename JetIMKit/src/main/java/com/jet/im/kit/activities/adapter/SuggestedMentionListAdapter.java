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

import com.jet.im.kit.R;
import com.jet.im.kit.activities.viewholder.BaseViewHolder;
import com.jet.im.kit.databinding.SbViewSuggestedUserPreviewBinding;
import com.jet.im.kit.interfaces.OnItemClickListener;
import com.jet.im.kit.interfaces.OnItemLongClickListener;
import com.juggle.im.model.UserInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * SuggestedMentionListAdapter provides a binding from a {@link UserInfo} type data set to views that are displayed within a RecyclerView.
 *
 * since 3.0.0
 */
public class SuggestedMentionListAdapter extends MutableBaseAdapter<UserInfo> {

    @NonNull
    final private List<UserInfo> users = new ArrayList<>();
    @NonNull
    private List<SuggestedUserInfo> cachedUsers = new ArrayList<>();
    @Nullable
    private OnItemClickListener<UserInfo> listener;
    @Nullable
    private OnItemLongClickListener<UserInfo> longClickListener;
    @Nullable
    private OnItemClickListener<UserInfo> profileClickListener;

    private final boolean showUserId;

    /**
     * Constructor
     *
     * since 3.0.0
     */
    public SuggestedMentionListAdapter() {
        this(true);
    }

    /**
     * Constructor
     *
     * @param showUserId Whether to show user id information on each item
     * since 3.0.0
     */
    public SuggestedMentionListAdapter(boolean showUserId) {
        this.showUserId = showUserId;
    }

    /**
     * Called when RecyclerView needs a new {@link BaseViewHolder <UserInfo>} of the given type to represent
     * an item.
     *
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     *               an adapter position.
     * @param viewType The view type of the new View.
     *
     * @return A new {@link BaseViewHolder<UserInfo>} that holds a View of the given view type.
     * @see #getItemViewType(int)
     * @see #onBindViewHolder(BaseViewHolder, int)
     * since 3.0.0
     */
    @NonNull
    @Override
    public BaseViewHolder<UserInfo> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final TypedValue values = new TypedValue();
        parent.getContext().getTheme().resolveAttribute(R.attr.sb_component_list, values, true);
        final Context contextWrapper = new ContextThemeWrapper(parent.getContext(), values.resourceId);
        return new SuggestionPreviewHolder(SbViewSuggestedUserPreviewBinding.inflate(LayoutInflater.from(contextWrapper), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder<UserInfo> holder, int position) {
        holder.bind(getItem(position));
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     * since 3.0.0
     */
    @Override
    public int getItemCount() {
        return users.size();
    }

    /**
     * Returns the {@link UserInfo} in the data set held by the adapter.
     *
     * @param position The position of the item within the adapter's data set.
     * @return The {@link UserInfo} to retrieve the position of in this adapter.
     * since 3.0.0
     */
    @NonNull
    public UserInfo getItem(int position) {
        return users.get(position);
    }

    /**
     * Returns the {@link List<UserInfo>} in the data set held by the adapter.
     *
     * @return The {@link List<UserInfo>} in this adapter.
     * since 3.0.0
     */
    @NonNull
    public List<UserInfo> getItems() {
        return Collections.unmodifiableList(users);
    }

    /**
     * Register a callback to be invoked when the {@link BaseViewHolder#itemView} is clicked.
     *
     * @param listener The callback that will run
     * since 3.0.0
     */
    public void setOnItemClickListener(@Nullable OnItemClickListener<UserInfo> listener) {
        this.listener = listener;
    }

    /**
     * Returns a callback to be invoked when the {@link BaseViewHolder#itemView} is clicked.
     *
     * @return {@code OnItemClickListener} to be invoked when the {@link BaseViewHolder#itemView} is clicked.
     * since 3.0.0
     */
    @Nullable
    public OnItemClickListener<UserInfo> getOnItemClickListener() {
        return listener;
    }

    /**
     * Register a callback to be invoked when the {@link BaseViewHolder#itemView} is clicked and held.
     *
     * @param listener The callback that will run
     * since 3.0.0
     */
    public void setOnItemLongClickListener(@Nullable OnItemLongClickListener<UserInfo> listener) {
        this.longClickListener = listener;
    }

    /**
     * Returns a callback to be invoked when the {@link BaseViewHolder#itemView} is clicked and held.
     *
     * @return {@code OnItemLongClickListener} to be invoked when the {@link BaseViewHolder#itemView} is clicked and held.
     * since 3.0.0
     */
    @Nullable
    public OnItemLongClickListener<UserInfo> getOnItemLongClickListener() {
        return longClickListener;
    }

    /**
     * Register a callback to be invoked when the profile view is clicked.
     *
     * @param profileClickListener The callback that will run
     * since 3.0.0
     */
    public void setOnProfileClickListener(@Nullable OnItemClickListener<UserInfo> profileClickListener) {
        this.profileClickListener = profileClickListener;
    }

    /**
     * Returns a callback to be invoked when the profile view is clicked.
     *
     * @return {@code OnItemClickListener} to be invoked when the profile view is clicked.
     * since 3.0.0
     */
    @Nullable
    public OnItemClickListener<UserInfo> getOnProfileClickListener() {
        return profileClickListener;
    }

    /**
     * Sets the {@link List<UserInfo>} to be displayed.
     *
     * @param userList list to be displayed
     * since 3.0.0
     */
    @Override
    public void setItems(@NonNull List<UserInfo> userList) {
        final List<SuggestedUserInfo> newUserList = SuggestedUserInfo.toUserInfoList(userList);
        final UserTypeDiffCallback<SuggestedUserInfo> diffCallback = new UserTypeDiffCallback<>(this.cachedUsers, newUserList);
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        this.users.clear();
        this.users.addAll(userList);
        this.cachedUsers = newUserList;
        diffResult.dispatchUpdatesTo(this);
    }

    private class SuggestionPreviewHolder extends BaseViewHolder<UserInfo> {
        @NonNull
        private final SbViewSuggestedUserPreviewBinding binding;

        SuggestionPreviewHolder(@NonNull SbViewSuggestedUserPreviewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.suggestedMentionPreview.setOnClickListener(v -> {
                int userPosition = getBindingAdapterPosition();
                if (userPosition != NO_POSITION && listener != null) {
                    listener.onItemClick(v, userPosition, getItem(userPosition));
                }
            });

            binding.suggestedMentionPreview.setOnLongClickListener(v -> {
                int userPosition = getBindingAdapterPosition();
                if (userPosition != NO_POSITION && longClickListener != null) {
                    longClickListener.onItemLongClick(v, userPosition, getItem(userPosition));
                    return true;
                }
                return false;
            });

            binding.suggestedMentionPreview.setOnProfileClickListener(v -> {
                int userPosition = getBindingAdapterPosition();
                if (userPosition != NO_POSITION && profileClickListener != null) {
                    profileClickListener.onItemClick(v, userPosition, getItem(userPosition));
                }
            });
        }

        @Override
        public void bind(@NonNull UserInfo user) {
            binding.suggestedMentionPreview.drawUser(user, showUserId);
        }
    }

    private static class UserTypeDiffCallback<T extends SuggestedUserInfo> extends DiffUtil.Callback {
        @NonNull
        private final List<T> oldUserList;
        @NonNull
        private final List<T> newUserList;

        UserTypeDiffCallback(@NonNull List<T> oldUserList, @NonNull List<T> newUserList) {
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
            final T oldUser = oldUserList.get(oldItemPosition);
            final T newUser = newUserList.get(newItemPosition);

            return oldUser.equals(newUser);
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            final T oldUser = oldUserList.get(oldItemPosition);
            final T newUser = newUserList.get(newItemPosition);

            if (!areItemsTheSame(oldItemPosition, newItemPosition)) {
                return false;
            }

            final String oldId = oldUser.getUserId();
            final String newId = newUser.getUserId();
            if (!newId.equals(oldId)) {
                return false;
            }

            final String oldNickname = oldUser.getUserNickname();
            final String newNickname = newUser.getUserNickname();
            if (!newNickname.equals(oldNickname)) {
                return false;
            }

            final String oldProfileUrl = oldUser.getProfileUrl();
            final String newProfileUrl = newUser.getProfileUrl();

            return newProfileUrl.equals(oldProfileUrl);
        }
    }

    private static class SuggestedUserInfo {
        @NonNull
        private final String userId;
        @NonNull
        private final String userNickname;
        @NonNull
        private final String profileUrl;

        SuggestedUserInfo(@NonNull UserInfo user) {
            this.userId = user.getUserId();
            this.userNickname = user.getUserName();
            this.profileUrl = user.getPortrait();
        }

        @NonNull
        String getUserId() {
            return userId;
        }

        @NonNull
        String getUserNickname() {
            return userNickname;
        }

        @NonNull
        String getProfileUrl() {
            return profileUrl;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SuggestedUserInfo that = (SuggestedUserInfo) o;

            if (!userId.equals(that.userId)) return false;
            if (!userNickname.equals(that.userNickname)) return false;
            return Objects.equals(profileUrl, that.profileUrl);
        }

        @Override
        public int hashCode() {
            int result = userId.hashCode();
            result = 31 * result + userNickname.hashCode();
            result = 31 * result + profileUrl.hashCode();
            return result;
        }

        @NonNull
        @Override
        public String toString() {
            return "UserInfo{" +
                "userId='" + userId + '\'' +
                ", userNickname='" + userNickname + '\'' +
                ", profileUrl='" + profileUrl + '\'' +
                '}';
        }

        @NonNull
        static List<SuggestedUserInfo> toUserInfoList(@NonNull List<UserInfo> userList) {
            List<SuggestedUserInfo> results = new ArrayList<>();
            for (UserInfo user : userList) {
                results.add(new SuggestedUserInfo(user));
            }
            return results;
        }
    }
}
