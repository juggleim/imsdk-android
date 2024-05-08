package com.jet.im.kit.activities.adapter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import com.sendbird.android.channel.Role;
import com.sendbird.android.user.User;

import java.util.List;

/**
 * BannedUserListAdapter provides a binding from a {@link User} set to views that are displayed within a RecyclerView.
 */
public class BannedUserListAdapter extends UserTypeListAdapter<User> {
    @NonNull
    private Role myRole = Role.NONE;

    /**
     * Sets the {@link List <User>} to be displayed.
     *
     * @param userList list to be displayed
     * @param myRole The role of the current user
     */
    public void setItems(@NonNull List<User> userList, @NonNull Role myRole) {
        final UserTypeDiffCallback<User> diffCallback = UserTypeDiffCallback.createFromGroupChannel(getItems(), userList, this.myRole, myRole);
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        setUsers(userList);
        this.myRole = myRole;
        diffResult.dispatchUpdatesTo(this);
    }

    @Override
    protected boolean isCurrentUserOperator() {
        return myRole == Role.OPERATOR;
    }

    @NonNull
    @Override
    protected String getItemViewDescription(@NonNull Context context, @NonNull User user) {
        return "";
    }
}
