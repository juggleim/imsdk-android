package com.jet.im.kit.vm;

import androidx.annotation.NonNull;

import com.sendbird.android.channel.ChannelType;
import com.sendbird.android.user.User;
import com.jet.im.kit.interfaces.PagedQueryHandler;
import com.jet.im.kit.internal.queries.BannedUserListQuery;

/**
 * ViewModel preparing and managing data related with the list of banned users
 *
 * since 3.1.0
 */
public class OpenChannelBannedUserListViewModel extends OpenChannelUserViewModel<User> {
    /**
     * Constructor
     *
     * @param channelUrl The URL of a channel this view model is currently associated with
     * since 3.1.0
     */
    public OpenChannelBannedUserListViewModel(@NonNull String channelUrl) {
        super(channelUrl);
    }

    /**
     * Creates banned user list query.
     *
     * @param channelUrl The url of {@code OpenChannel} with banned users to be fetched by the query
     * @return {@code PagedQueryHandler<User>} to retrieve the list of users who are banned
     * since 3.1.0
     */
    @NonNull
    @Override
    protected PagedQueryHandler<User> createQueryHandler(@NonNull String channelUrl) {
        return new BannedUserListQuery(ChannelType.OPEN, channelUrl);
    }
}
