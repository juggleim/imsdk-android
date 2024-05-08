package com.jet.im.kit.internal.model

import com.sendbird.android.channel.OpenChannel

internal data class OpenChannelInfo(val channel: OpenChannel) {
    val channelUrl: String = channel.url
    val createdAt: Long = channel.createdAt
    val participantCount: Int = channel.participantCount
    val isFrozen: Boolean = channel.isFrozen
    val name: String = channel.name
    val coverUrl: String = channel.coverUrl

    companion object {
        @JvmStatic
        fun toChannelInfo(channelList: List<OpenChannel>): List<OpenChannelInfo> {
            return channelList.map { OpenChannelInfo(it) }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OpenChannelInfo

        if (channelUrl != other.channelUrl) return false
        if (createdAt != other.createdAt) return false
        if (participantCount != other.participantCount) return false
        if (isFrozen != other.isFrozen) return false
        if (name != other.name) return false
        if (coverUrl != other.coverUrl) return false

        return true
    }

    override fun hashCode(): Int {
        var result = channelUrl.hashCode()
        result = 31 * result + createdAt.hashCode()
        result = 31 * result + participantCount
        result = 31 * result + isFrozen.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + coverUrl.hashCode()
        return result
    }
}
