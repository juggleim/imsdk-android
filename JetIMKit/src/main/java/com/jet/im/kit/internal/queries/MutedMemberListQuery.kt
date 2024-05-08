package com.jet.im.kit.internal.queries

import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.params.MemberListQueryParams
import com.sendbird.android.user.Member
import com.sendbird.android.user.query.MemberListQuery
import com.sendbird.android.user.query.MutedMemberFilter
import com.jet.im.kit.interfaces.OnListResultHandler
import com.jet.im.kit.interfaces.PagedQueryHandler

internal class MutedMemberListQuery(private val channelUrl: String) : PagedQueryHandler<Member> {
    private var query: MemberListQuery? = null
    override fun loadInitial(handler: OnListResultHandler<Member>) {
        query = GroupChannel.createMemberListQuery(
            channelUrl,
            MemberListQueryParams().apply {
                limit = 30
                mutedMemberFilter = MutedMemberFilter.MUTED
            }
        )
        loadMore(handler)
    }

    override fun loadMore(handler: OnListResultHandler<Member>) {
        query?.next { result, e -> handler.onResult(result, e) }
    }

    override fun hasMore(): Boolean {
        return query?.hasNext ?: false
    }
}
