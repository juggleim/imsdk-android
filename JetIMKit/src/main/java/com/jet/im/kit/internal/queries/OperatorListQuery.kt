package com.jet.im.kit.internal.queries

import com.sendbird.android.SendbirdChat.createOperatorListQuery
import com.sendbird.android.channel.ChannelType
import com.sendbird.android.exception.SendbirdException
import com.sendbird.android.params.OperatorListQueryParams
import com.sendbird.android.user.User
import com.sendbird.android.user.query.OperatorListQuery
import com.jet.im.kit.interfaces.OnListResultHandler
import com.jet.im.kit.interfaces.PagedQueryHandler

internal class OperatorListQuery(private val channelType: ChannelType, private val channelUrl: String) :
    PagedQueryHandler<User> {
    private var query: OperatorListQuery? = null
    override fun loadInitial(handler: OnListResultHandler<User>) {
        query = createOperatorListQuery(OperatorListQueryParams(channelType, channelUrl).apply { limit = 30 })
        loadMore(handler)
    }

    override fun loadMore(handler: OnListResultHandler<User>) {
        query?.next { list: List<User>?, e: SendbirdException? ->
            handler.onResult(if (list != null) ArrayList<User>(list) else null, e)
        }
    }

    override fun hasMore(): Boolean {
        return query?.hasNext ?: false
    }
}
