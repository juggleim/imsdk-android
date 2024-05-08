package com.jet.im.kit.activities.adapter;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jet.im.kit.activities.viewholder.BaseViewHolder;
import com.jet.im.kit.databinding.SbViewSelectUserBinding;
import com.jet.im.kit.interfaces.OnItemClickListener;
import com.jet.im.kit.interfaces.OnItemLongClickListener;
import com.jet.im.kit.interfaces.OnUserSelectChangedListener;
import com.jet.im.kit.interfaces.UserInfo;
import com.jet.im.kit.log.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class SelectUserListAdapter<T> extends BaseAdapter<T, BaseViewHolder<T>> {
    @NonNull
    protected List<T> userList = new ArrayList<>();
    @NonNull
    protected List<String> disabledUserList = new ArrayList<>();
    @NonNull
    protected final List<String> selectedUserIdList = new ArrayList<>();
    @Nullable
    protected OnItemClickListener<T> listener;
    @Nullable
    protected OnItemLongClickListener<T> longClickListener;
    @Nullable
    protected OnUserSelectChangedListener userSelectChangedListener;

    @NonNull
    @Override
    public BaseViewHolder<T> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SelectUserViewHolder(SbViewSelectUserBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    /**
     * Constructor
     */
    public SelectUserListAdapter() {
        this(null);
    }

    /**
     * Constructor
     *
     * @param listener The listener performing when the {@link BaseViewHolder<T>} is clicked.
     */
    public SelectUserListAdapter(@Nullable OnItemClickListener<T> listener) {
        setHasStableIds(true);
        this.listener = listener;
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder<T> holder, int position) {
        holder.bind(getItem(position));
    }

    @Override
    @NonNull
    public T getItem(int position) {
        return userList.get(position);
    }

    @Override
    @NonNull
    public List<T> getItems() {
        return Collections.unmodifiableList(userList);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).hashCode();
    }

    public void setItems(@NonNull List<T> userList) {
        this.userList = userList;
        notifyDataSetChanged();
    }

    /**
     * Register a callback to be invoked when the {@link BaseViewHolder#itemView} is clicked.
     * If this view is not clickable, it becomes clickable.
     *
     * @param listener The callback that will run
     * since 3.0.0
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
     * If this view is not clickable, it becomes clickable.
     *
     * @param listener The callback that will run
     * since 3.0.0
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
     * Register a callback to be invoked when the {@link BaseViewHolder#itemView} is clicked.
     * If this view is not clickable, it becomes clickable.
     *
     * @param userSelectChangedListener The callback that will run
     * since 3.0.0
     */
    public void setOnUserSelectChangedListener(@Nullable OnUserSelectChangedListener userSelectChangedListener) {
        this.userSelectChangedListener = userSelectChangedListener;
    }

    /**
     * Returns a callback to be invoked when the {@link BaseViewHolder#itemView} is clicked.
     *
     * @return {@code OnUserSelectChangedListener} to be invoked when the {@link BaseViewHolder#itemView} is clicked.
     * since 3.0.0
     */
    @Nullable
    public OnUserSelectChangedListener getOnUserSelectChangedListener() {
        return userSelectChangedListener;
    }

    /**
     * Sets the user's id which should disable in the list.
     *
     * @param disabledUserList User's ids which should not display.
     * since 3.0.0
     */
    public void setDisabledUserIdList(@NonNull List<String> disabledUserList) {
        this.disabledUserList = disabledUserList;
    }

    /**
     * Returns the list of selected user's id.
     *
     * @return {@code List<String>} to be selected user ids.
     * since 3.0.0
     */
    @NonNull
    public List<String> getSelectedUserIdList() {
        return selectedUserIdList;
    }

    /**
     * Determines whether the item is disabled or not.
     *
     * @param item A specific-item to be determined
     * @return {@code true} if the item is disabled, {@code false} otherwise.
     * since 3.0.0
     */
    abstract protected boolean isDisabled(@NonNull T item);

    /**
     * Determines whether the item is selected or not.
     *
     * @param item A specific-item to be determined
     * @return {@code true} if the use is selected, {@code false} otherwise.
     * since 3.0.0
     */
    abstract protected boolean isSelected(@NonNull T item);

    /**
     * Converts the given item to {@link UserInfo}.
     *
     * @param item A specific-item to be converted.
     * @return A converted {@link UserInfo}.
     * since 3.0.0
     */
    @NonNull
    abstract protected UserInfo toUserInfo(@NonNull T item);

    private class SelectUserViewHolder extends BaseViewHolder<T> {
        @NonNull
        private final SbViewSelectUserBinding binding;

        SelectUserViewHolder(@NonNull SbViewSelectUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.selectUserViewHolder.setOnItemClickListener(v -> {
                int userPosition = getBindingAdapterPosition();
                if (userPosition != NO_POSITION) {
                    final T item = getItem(userPosition);
                    boolean isSelected = isSelected(item);
                    final UserInfo userInfo = toUserInfo(item);
                    Logger.d("++ isSelected : %s, userName : %s", isSelected, userInfo.getNickname());
                    if (!isSelected) {
                        selectedUserIdList.add(userInfo.getUserId());
                    } else {
                        selectedUserIdList.remove(userInfo.getUserId());
                    }

                    if (listener != null) {
                        listener.onItemClick(v, userPosition, item);
                    }
                }
            });

            binding.selectUserViewHolder.setOnItemLongClickListener(v -> {
                int userPosition = getBindingAdapterPosition();
                if (userPosition != NO_POSITION && longClickListener != null) {
                    longClickListener.onItemLongClick(v, userPosition, getItem(userPosition));
                    return true;
                }
                return false;
            });

            binding.selectUserViewHolder.setOnSelectedStateChangedListener((buttonView, isSelected) -> {
                int userPosition = getBindingAdapterPosition();
                if (userPosition != NO_POSITION) {
                    if (userSelectChangedListener != null) {
                        userSelectChangedListener.onUserSelectChanged(selectedUserIdList, !isSelected);
                    }
                }
            });
        }

        @Override
        public void bind(@NonNull T item) {
            binding.selectUserViewHolder.drawUser(toUserInfo(item), isSelected(item) || isDisabled(item), !isDisabled(item));
        }
    }
}
