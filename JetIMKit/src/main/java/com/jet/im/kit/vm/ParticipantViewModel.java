package com.jet.im.kit.vm;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sendbird.android.user.User;
import com.jet.im.kit.interfaces.PagedQueryHandler;
import com.jet.im.kit.internal.queries.ParticipantsListQuery;

/**
 * ViewModel preparing and managing data related with the list of participants
 *
 * since 3.0.0
 */
public class ParticipantViewModel extends OpenChannelUserViewModel<User> {
    /**
     * Constructor
     *
     * @param channelUrl The URL of a channel this view model is currently associated with
     * @param queryHandler A query to retrieve {@link User} list for the current <code>OpenChannel</code>
     * since 3.0.0
     */
    public ParticipantViewModel(@NonNull String channelUrl, @Nullable PagedQueryHandler<User> queryHandler) {
        super(channelUrl, queryHandler);
    }

    /**
     * Creates participants user list query.
     *
     * @param channelUrl The url of {@code OpenChannel} with participants to be fetched by the query
     * @return {@code PagedQueryHandler<User>} to retrieve the list of operators
     * since 3.0.0
     */
    @NonNull
    protected PagedQueryHandler<User> createQueryHandler(@NonNull String channelUrl) {
        return new ParticipantsListQuery(channelUrl);
    }
}
