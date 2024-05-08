package com.jet.im.kit.vm;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sendbird.android.channel.ChannelType;
import com.sendbird.android.user.User;
import com.jet.im.kit.interfaces.PagedQueryHandler;
import com.jet.im.kit.internal.queries.OperatorListQuery;

/**
 * ViewModel preparing and managing data related with the list of operators
 *
 * since 3.1.0
 */
public class OpenChannelOperatorListViewModel extends OpenChannelUserViewModel<User> {
    /**
     * Constructor
     *
     * @param channelUrl The URL of a channel this view model is currently associated with
     * @param queryHandler A callback to be invoked when a list of data is loaded.
     * since 3.1.0
     */
    public OpenChannelOperatorListViewModel(@NonNull String channelUrl, @Nullable PagedQueryHandler<User> queryHandler) {
        super(channelUrl, queryHandler);
    }

    /**
     * Creates operator list query.
     *
     * @param channelUrl The url of {@code OpenChannel} with operators to be fetched by the query
     * @return {@code PagedQueryHandler<User>} to retrieve the list of operators
     * since 3.1.0
     */
    @NonNull
    @Override
    protected PagedQueryHandler<User> createQueryHandler(@NonNull String channelUrl) {
        return new OperatorListQuery(ChannelType.OPEN, channelUrl);
    }
}
