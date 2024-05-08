package com.jet.im.kit.vm;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sendbird.android.user.Member;
import com.jet.im.kit.interfaces.PagedQueryHandler;
import com.jet.im.kit.internal.queries.MutedMemberListQuery;

/**
 * ViewModel preparing and managing data related with the list of muted members
 *
 * since 3.0.0
 */
public class MutedMemberListViewModel extends UserViewModel<Member> {
    /**
     * Constructor
     *
     * @param channelUrl The URL of a channel this view model is currently associated with
     * @param queryHandler A callback to be invoked when a list of data is loaded.
     * since 3.0.0
     */
    public MutedMemberListViewModel(@NonNull String channelUrl, @Nullable PagedQueryHandler<Member> queryHandler) {
        super(channelUrl, queryHandler);
    }

    /**
     * Creates muted member list query.
     *
     * @param channelUrl The url of {@code GroupChannel} with muted members to be fetched by the query
     * @return {@code PagedQueryHandler<UserInfo>} to retrieve the list of members who are muted
     * since 3.0.0
     */
    @NonNull
    @Override
    protected PagedQueryHandler<Member> createQueryHandler(@NonNull String channelUrl) {
        return new MutedMemberListQuery(channelUrl);
    }
}
