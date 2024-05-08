package com.jet.im.kit.internal.queries

import com.sendbird.android.SendbirdChat.createBannedUserListQuery
import com.sendbird.android.channel.ChannelType
import com.sendbird.android.exception.SendbirdException
import com.sendbird.android.params.BannedUserListQueryParams
import com.sendbird.android.user.RestrictedUser
import com.sendbird.android.user.User
import com.sendbird.android.user.query.BannedUserListQuery
import com.jet.im.kit.interfaces.OnListResultHandler
import com.jet.im.kit.interfaces.PagedQueryHandler

internal class BannedUserListQuery(
    private val channelType: ChannelType,
    private val channelUrl: String
) : PagedQueryHandler<User> {
    private var query: BannedUserListQuery? = null
    override fun loadInitial(handler: OnListResultHandler<User>) {
        query = createBannedUserListQuery(
            BannedUserListQueryParams(channelType, channelUrl).apply {
                limit = 30
            }
        )
        loadMore(handler)
    }

    override fun loadMore(handler: OnListResultHandler<User>) {
        query?.next { list: List<RestrictedUser>?, e: SendbirdException? ->
            handler.onResult(
                if (list != null) ArrayList<User>(list) else null, e
            )
        } ?: handler.onResult(null, SendbirdException("loadInitial must be called first."))
    }

    override fun hasMore(): Boolean {
        return query?.hasNext ?: false
    }
}
