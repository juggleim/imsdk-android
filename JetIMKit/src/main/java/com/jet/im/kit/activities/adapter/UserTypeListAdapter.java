package com.jet.im.kit.activities.adapter;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;

import com.sendbird.android.user.Member;
import com.sendbird.android.user.RestrictedUser;
import com.sendbird.android.user.RestrictionType;
import com.sendbird.android.user.User;
import com.jet.im.kit.R;
import com.jet.im.kit.activities.viewholder.BaseViewHolder;
import com.jet.im.kit.databinding.SbViewUserPreviewBinding;
import com.jet.im.kit.interfaces.OnItemClickListener;
import com.jet.im.kit.interfaces.OnItemLongClickListener;
import com.jet.im.kit.internal.ui.widgets.UserPreview;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Adapters provides a binding from a {@link User} set to views that are displayed within a RecyclerView.
 */
abstract public class UserTypeListAdapter<T extends User> extends BaseAdapter<T, BaseViewHolder<T>> {
    @NonNull
    final private List<T> users = new ArrayList<>();
    @Nullable
    private OnItemClickListener<T> listener;
    @Nullable
    private OnItemLongClickListener<T> longClickListener;
    @Nullable
    private OnItemClickListener<T> actionItemClickListener;
    @Nullable
    private OnItemClickListener<T> profileClickListener;

    /**
     * Called when RecyclerView needs a new {@link BaseViewHolder <T>} of the given type to represent
     * an item.
     *
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     *               an adapter position.
     * @param viewType The view type of the new View.
     *
     * @return A new {@link BaseViewHolder<T>} that holds a View of the given view type.
     * @see #getItemViewType(int)
     * @see #onBindViewHolder(BaseViewHolder, int)
     */
    @NonNull
    @Override
    public BaseViewHolder<T> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final TypedValue values = new TypedValue();
        parent.getContext().getTheme().resolveAttribute(R.attr.sb_component_list, values, true);
        final Context contextWrapper = new ContextThemeWrapper(parent.getContext(), values.resourceId);
        return new UserPreviewHolder(SbViewUserPreviewBinding.inflate(LayoutInflater.from(contextWrapper), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder<T> holder, int position) {
        holder.bind(getItem(position));
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return users.size();
    }

    /**
     * Returns the {@link User} in the data set held by the adapter.
     *
     * @param position The position of the item within the adapter's data set.
     * @return The {@link User} to retrieve the position of in this adapter.
     */
    @Override
    @NonNull
    public T getItem(int position) {
        return users.get(position);
    }

    /**
     * Returns the {@link List<T>} in the data set held by the adapter.
     *
     * @return The {@link List<T>} in this adapter.
     */
    @Override
    @NonNull
    public List<T> getItems() {
        return Collections.unmodifiableList(users);
    }

    /**
     * Register a callback to be invoked when the {@link BaseViewHolder#itemView} is clicked.
     *
     * @param listener The callback that will run
     */
    public void setOnItemClickListener(@Nullable OnItemClickListener<T> listener) {
        this.listener = listener;
    }

    /**
     * Returns a callback to be invoked when the {@link BaseViewHolder#itemView} is clicked.
     *
     * @return {@code OnItemClickListener} to be invoked when the {@link BaseViewHolder#itemView} is clicked.
     * since 3.0.0
     */
    @Nullable
    public OnItemClickListener<T> getOnItemClickListener() {
        return listener;
    }

    /**
     * Register a callback to be invoked when the {@link BaseViewHolder#itemView} is clicked and held.
     *
     * @param listener The callback that will run
     */
    public void setOnItemLongClickListener(@Nullable OnItemLongClickListener<T> listener) {
        this.longClickListener = listener;
    }

    /**
     * Returns a callback to be invoked when the {@link BaseViewHolder#itemView} is clicked and held.
     *
     * @return {@code OnItemLongClickListener} to be invoked when the {@link BaseViewHolder#itemView} is clicked and held.
     * since 3.0.0
     */
    @Nullable
    public OnItemLongClickListener<T> getOnItemLongClickListener() {
        return longClickListener;
    }

    /**
     * Register a callback to be invoked when the action view is clicked.
     *
     * @param listener The callback that will run
     */
    public void setOnActionItemClickListener(@Nullable OnItemClickListener<T> listener) {
        this.actionItemClickListener = listener;
    }

    /**
     * Returns a callback to be invoked when the action view is clicked.
     *
     * @return {@code OnItemClickListener} to be invoked when the action view is clicked.
     * since 3.0.0
     */
    @Nullable
    public OnItemClickListener<T> getOnActionItemClickListener() {
        return actionItemClickListener;
    }

    /**
     * Register a callback to be invoked when the profile view is clicked.
     *
     * @param profileClickListener The callback that will run
     * since 1.2.2
     */
    public void setOnProfileClickListener(@Nullable OnItemClickListener<T> profileClickListener) {
        this.profileClickListener = profileClickListener;
    }

    /**
     * Returns a callback to be invoked when the profile view is clicked.
     *
     * @return {@code OnItemClickListener} to be invoked when the profile view is clicked.
     * since 3.0.0
     */
    @Nullable
    public OnItemClickListener<T> getOnProfileClickListener() {
        return profileClickListener;
    }

    /**
     * Sets the {@link List<T>} to be displayed.
     *
     * @param userList list to be displayed
     * since 3.1.0
     */
    protected void setUsers(@NonNull List<T> userList) {
        this.users.clear();
        this.users.addAll(userList);
    }

    /**
     * Returns whether the current user is operator or not.
     *
     * @return {@code true} if the current user is operator, {@code false} otherwise
     * since 3.1.0
     */
    abstract protected boolean isCurrentUserOperator();

    /**
     * Returns the description to be shown in item view.
     *
     * @param context Context for item view
     * @param user The user to be checked if showing operator badge
     * @return Text to be shown as description in item view
     * since 3.1.0
     */
    @NonNull
    abstract protected String getItemViewDescription(@NonNull Context context, @NonNull T user);

    private class UserPreviewHolder extends BaseViewHolder<T> {
        @NonNull
        private final SbViewUserPreviewBinding binding;

        UserPreviewHolder(@NonNull SbViewUserPreviewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.userViewHolder.setOnClickListener(v -> {
                int userPosition = getBindingAdapterPosition();
                if (userPosition != NO_POSITION && listener != null) {
                    listener.onItemClick(v, userPosition, getItem(userPosition));
                }
            });

            binding.userViewHolder.setOnLongClickListener(v -> {
                int userPosition = getBindingAdapterPosition();
                if (userPosition != NO_POSITION && longClickListener != null) {
                    longClickListener.onItemLongClick(v, userPosition, getItem(userPosition));
                    return true;
                }
                return false;
            });

            binding.userViewHolder.setOnActionMenuClickListener(v -> {
                int userPosition = getBindingAdapterPosition();
                if (userPosition != NO_POSITION && actionItemClickListener != null) {
                    actionItemClickListener.onItemClick(v, userPosition, getItem(userPosition));
                }
            });

            binding.userViewHolder.setOnProfileClickListener(v -> {
                int userPosition = getBindingAdapterPosition();
                if (userPosition != NO_POSITION && profileClickListener != null) {
                    profileClickListener.onItemClick(v, userPosition, getItem(userPosition));
                }
            });
        }

        @Override
        public void bind(@NonNull T user) {
            boolean isMuted = false;
            if (user instanceof Member) {
                isMuted = ((Member) user).isMuted();
            } else if (user instanceof RestrictedUser) {
                isMuted = ((RestrictedUser) user).getRestrictionInfo().getRestrictionType().equals(RestrictionType.MUTED);
            }
            binding.userViewHolder.useActionMenu(isCurrentUserOperator() && actionItemClickListener != null);
            UserPreview.drawUser(binding.userViewHolder, user, getItemViewDescription(binding.getRoot().getContext(), user), isMuted);
        }
    }
}
