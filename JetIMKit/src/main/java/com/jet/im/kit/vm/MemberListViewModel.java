package com.jet.im.kit.vm;

import androidx.annotation.NonNull;

import com.sendbird.android.user.Member;
import com.jet.im.kit.interfaces.PagedQueryHandler;
import com.jet.im.kit.internal.queries.ChannelMemberListQuery;

/**
 * ViewModel preparing and managing data related with the list of members
 *
 * since 3.0.0
 */
public class MemberListViewModel extends UserViewModel<Member> {
    /**
     * Constructor
     *
     * @param channelUrl The URL of a channel this view model is currently associated with
     * since 3.0.0
     */
    public MemberListViewModel(@NonNull String channelUrl) {
        super(channelUrl);
    }

    /**
     * Creates member list query.
     *
     * @param channelUrl The url of {@code GroupChannel} with members to be fetched by the query
     * @return {@code PagedQueryHandler<UserInfo>} to retrieve the list of members
     * since 3.0.0
     */
    @NonNull
    @Override
    protected PagedQueryHandler<Member> createQueryHandler(@NonNull String channelUrl) {
        return new ChannelMemberListQuery(channelUrl);
    }
}
