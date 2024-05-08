package com.jet.im.kit.vm;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sendbird.android.channel.ChannelType;
import com.sendbird.android.user.User;
import com.jet.im.kit.interfaces.PagedQueryHandler;
import com.jet.im.kit.internal.queries.MutedUserListQuery;

/**
 * ViewModel preparing and managing data related with the list of muted participants
 *
 * since 3.1.0
 */
public class OpenChannelMutedParticipantListViewModel extends OpenChannelUserViewModel<User> {
    /**
     * Constructor
     *
     * @param channelUrl The URL of a channel this view model is currently associated with
     * @param queryHandler A callback to be invoked when a list of data is loaded.
     * since 3.1.0
     */
    public OpenChannelMutedParticipantListViewModel(@NonNull String channelUrl, @Nullable PagedQueryHandler<User> queryHandler) {
        super(channelUrl, queryHandler);
    }

    /**
     * Creates muted participant list query.
     *
     * @param channelUrl The url of {@code OpenChannel} with muted participants to be fetched by the query
     * @return {@code PagedQueryHandler<User>} to retrieve the list of participants who are muted
     * since 3.1.0
     */
    @NonNull
    @Override
    protected PagedQueryHandler<User> createQueryHandler(@NonNull String channelUrl) {
        return new MutedUserListQuery(ChannelType.OPEN, channelUrl);
    }
}
