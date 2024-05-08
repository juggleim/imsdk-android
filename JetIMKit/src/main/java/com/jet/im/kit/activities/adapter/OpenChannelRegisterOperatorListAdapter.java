package com.jet.im.kit.activities.adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sendbird.android.channel.OpenChannel;
import com.sendbird.android.user.User;
import com.jet.im.kit.interfaces.OnItemClickListener;
import com.jet.im.kit.interfaces.UserInfo;
import com.jet.im.kit.utils.UserUtils;

/**
 * OpenChannelRegisterOperatorListAdapter provides a binding from a {@link User} type data set to views that are displayed within a RecyclerView.
 *
 * since 3.1.0
 */
public class OpenChannelRegisterOperatorListAdapter extends SelectUserListAdapter<User> {
    @Nullable
    private final OpenChannel openChannel;

    public OpenChannelRegisterOperatorListAdapter(@Nullable OpenChannel openChannel) {
        this(openChannel, null);
    }

    public OpenChannelRegisterOperatorListAdapter(@Nullable OpenChannel openChannel, @Nullable OnItemClickListener<User> listener) {
        super(listener);
        this.openChannel = openChannel;
    }

    @Override
    protected boolean isDisabled(@NonNull User user) {
        return openChannel != null && openChannel.isOperator(user);
    }

    @Override
    protected boolean isSelected(@NonNull User user) {
        return selectedUserIdList.contains(user.getUserId());
    }

    @NonNull
    @Override
    protected UserInfo toUserInfo(@NonNull User user) {
        return UserUtils.toUserInfo(user);
    }
}
