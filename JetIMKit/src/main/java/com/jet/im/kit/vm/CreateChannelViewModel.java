package com.jet.im.kit.vm;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sendbird.android.channel.GroupChannel;
import com.sendbird.android.handler.GroupChannelCallbackHandler;
import com.sendbird.android.params.GroupChannelCreateParams;
import com.jet.im.kit.SendbirdUIKit;
import com.jet.im.kit.interfaces.PagedQueryHandler;
import com.jet.im.kit.interfaces.UserInfo;
import com.jet.im.kit.internal.queries.DefaultUserListQuery;
import com.jet.im.kit.log.Logger;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * ViewModel preparing and managing data related with the list of users when creating a channel
 *
 * since 3.0.0
 */
public class CreateChannelViewModel extends UserViewModel<UserInfo> {
    private final AtomicBoolean isCreatingChannel = new AtomicBoolean();

    public CreateChannelViewModel(@Nullable PagedQueryHandler<UserInfo> queryHandler) {
        super("", queryHandler);
    }

    /**
     * Creates a channel.
     *
     * @param params Params to be applied when creating a channel
     * @param handler Callback notifying the result of authentication
     */
    public void createChannel(@NonNull GroupChannelCreateParams params, @Nullable GroupChannelCallbackHandler handler) {
        Logger.dev("++ createGroupChannel isCreatingChannel : " + isCreatingChannel.get());
        if (isCreatingChannel.compareAndSet(false, true)) {
            GroupChannel.createChannel(params, (channel, e) -> {
                isCreatingChannel.set(false);
                if (handler != null) handler.onResult(channel, e);
            });
        }
    }

    /**
     * Creates user list query.
     *
     * @return {@code PagedQueryHandler<UserInfo>} to retrieve the list of users to create {@code GroupChannel}
     * since 3.0.0
     */
    @NonNull
    @Override
    protected PagedQueryHandler<UserInfo> createQueryHandler(@Nullable String channelUrl) {
        final PagedQueryHandler<UserInfo> customHandler = SendbirdUIKit.getCustomUserListQueryHandler();
        return customHandler != null ? customHandler : new DefaultUserListQuery();
    }
}
