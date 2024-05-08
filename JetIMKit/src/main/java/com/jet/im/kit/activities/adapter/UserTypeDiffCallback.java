package com.jet.im.kit.activities.adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import com.sendbird.android.SendbirdChat;
import com.sendbird.android.channel.OpenChannel;
import com.sendbird.android.channel.Role;
import com.sendbird.android.user.Member;
import com.sendbird.android.user.User;

import java.util.List;

class UserTypeDiffCallback<T extends User> extends DiffUtil.Callback {
    @NonNull
    private final List<T> oldUserList;
    @NonNull
    private final List<T> newUserList;
    @NonNull
    private final Role oldMyRole;
    @NonNull
    private final Role newMyRole;
    @Nullable
    private final OpenChannel oldOpenChannel;
    @Nullable
    private final OpenChannel newOpenChannel;

    static <U extends User> UserTypeDiffCallback<U> createFromOpenChannel(@NonNull List<U> oldUserList, @NonNull List<U> newUserList, @Nullable OpenChannel oldOpenChannel, @Nullable OpenChannel newOpenChannel) {
        return new UserTypeDiffCallback<>(oldUserList, newUserList,
            oldOpenChannel != null && oldOpenChannel.isOperator(SendbirdChat.getCurrentUser()) ? Role.OPERATOR : Role.NONE,
            newOpenChannel != null && newOpenChannel.isOperator(SendbirdChat.getCurrentUser()) ? Role.OPERATOR : Role.NONE,
            oldOpenChannel, newOpenChannel);
    }

    static <U extends User> UserTypeDiffCallback<U> createFromGroupChannel(@NonNull List<U> oldUserList, @NonNull List<U> newUserList, @NonNull Role oldMyRole, @NonNull Role newMyRole) {
        return new UserTypeDiffCallback<>(oldUserList, newUserList, oldMyRole, newMyRole, null, null);
    }

    private UserTypeDiffCallback(@NonNull List<T> oldUserList, @NonNull List<T> newUserList, @NonNull Role oldMyRole, @NonNull Role newMyRole, @Nullable OpenChannel oldOpenChannel, @Nullable OpenChannel newOpenChannel) {
        this.oldUserList = oldUserList;
        this.newUserList = newUserList;
        this.oldMyRole = oldMyRole;
        this.newMyRole = newMyRole;
        this.oldOpenChannel = oldOpenChannel;
        this.newOpenChannel = newOpenChannel;
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

        return oldUser.equals(newUser) && oldMyRole.equals(newMyRole);
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        final T oldUser = oldUserList.get(oldItemPosition);
        final T newUser = newUserList.get(newItemPosition);

        if (!areItemsTheSame(oldItemPosition, newItemPosition)) {
            return false;
        }

        final String oldNickname = oldUser.getNickname();
        final String newNickname = newUser.getNickname();
        if (!newNickname.equals(oldNickname)) {
            return false;
        }

        if (newUser instanceof Member && oldUser instanceof Member) {
            final Member oldMember = (Member) oldUser;
            final Member newMember = (Member) newUser;
            if (oldMember.isMuted() != newMember.isMuted()) {
                return false;
            }

            if (oldMember.getRole() != newMember.getRole()) {
                return false;
            }
        }

        if (oldOpenChannel != null && newOpenChannel != null) {
            final boolean oldIsOperator = oldOpenChannel.isOperator(oldUser);
            final boolean newIsOperator = newOpenChannel.isOperator(newUser);
            if (oldIsOperator != newIsOperator) {
                return false;
            }
        }

        final String oldProfileUrl = oldUser.getProfileUrl();
        final String newProfileUrl = newUser.getProfileUrl();
        return newProfileUrl.equals(oldProfileUrl);
    }
}
