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
 * since 3.0.0
 */
public class OperatorListViewModel extends UserViewModel<User> {
    private final ChannelType channelType;

    /**
     * Constructor
     *
     * @param channelUrl The URL of a channel this view model is currently associated with
     * @param channelType Type of a channel this view model is currently associated with
     * @param queryHandler A callback to be invoked when a list of data is loaded.
     * since 3.0.0
     */
    public OperatorListViewModel(@NonNull String channelUrl, @Nullable ChannelType channelType, @Nullable PagedQueryHandler<User> queryHandler) {
        super(channelUrl, queryHandler);
        this.channelType = channelType == null ? ChannelType.GROUP : channelType;
    }

    /**
     * Creates operator list query.
     *
     * @param channelUrl The url of {@code GroupChannel} with operators to be fetched by the query
     * @return {@code PagedQueryHandler<User>} to retrieve the list of operators
     * since 3.0.0
     */
    @NonNull
    @Override
    protected PagedQueryHandler<User> createQueryHandler(@NonNull String channelUrl) {
        return new OperatorListQuery(channelType, channelUrl);
    }
}
