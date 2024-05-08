package com.jet.im.kit.activities.adapter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import com.sendbird.android.SendbirdChat;
import com.sendbird.android.channel.OpenChannel;
import com.sendbird.android.user.User;
import com.jet.im.kit.R;

import java.util.List;

/**
 * ParticipantsListAdapter provides a binding from a {@link User} type data set to views that are displayed within a RecyclerView.
 *
 * since 3.0.0
 */
public class ParticipantListAdapter extends UserTypeListAdapter<User> {
    @Nullable
    private OpenChannel openChannel;

    /**
     * Sets the {@link List <User>} to be displayed.
     *
     * @param userList list to be displayed
     * @param openChannel The latest open channel
     * since 3.1.0
     */
    public void setItems(@NonNull List<User> userList, @NonNull OpenChannel openChannel) {
        final UserTypeDiffCallback<User> diffCallback = UserTypeDiffCallback.createFromOpenChannel(getItems(), userList, this.openChannel, openChannel);
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        setUsers(userList);
        this.openChannel = OpenChannel.clone(openChannel);
        diffResult.dispatchUpdatesTo(this);
    }

    @Override
    protected boolean isCurrentUserOperator() {
        if (this.openChannel == null) return false;
        return this.openChannel.isOperator(SendbirdChat.getCurrentUser());
    }

    @NonNull
    @Override
    protected String getItemViewDescription(@NonNull Context context, @NonNull User user) {
        if (this.openChannel == null) return "";
        return this.openChannel.isOperator(user) ? context.getString(R.string.sb_text_operator) : "";
    }
}
