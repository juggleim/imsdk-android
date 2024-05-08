package com.jet.im.kit.internal.queries

import com.sendbird.android.SendbirdChat.createParticipantListQuery
import com.sendbird.android.exception.SendbirdException
import com.sendbird.android.params.ParticipantListQueryParams
import com.sendbird.android.user.User
import com.sendbird.android.user.query.ParticipantListQuery
import com.jet.im.kit.interfaces.OnListResultHandler
import com.jet.im.kit.interfaces.PagedQueryHandler

internal class ParticipantsListQuery(private val channelUrl: String) : PagedQueryHandler<User> {
    private var query: ParticipantListQuery? = null
    override fun loadInitial(handler: OnListResultHandler<User>) {
        query = createParticipantListQuery(ParticipantListQueryParams(channelUrl, 30))
        loadMore(handler)
    }

    override fun loadMore(handler: OnListResultHandler<User>) {
        if (query != null) {
            query?.next { list: List<User>?, e: SendbirdException? ->
                handler.onResult(if (list != null) ArrayList<User>(list) else null, e)
            }
        }
    }

    override fun hasMore(): Boolean {
        return query?.hasNext ?: false
    }
}
