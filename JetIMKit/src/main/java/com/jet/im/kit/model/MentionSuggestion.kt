package com.jet.im.kit.model

import com.juggle.im.model.UserInfo
import com.sendbird.android.user.User

data class MentionSuggestion(val keyword: String) {
    private val suggestionList: MutableList<UserInfo> = ArrayList()
    fun getSuggestionList(): List<UserInfo> {
        return suggestionList.toList()
    }

    fun append(suggestionList: List<UserInfo>) {
        this.suggestionList.addAll(suggestionList)
    }

    fun clear() {
        suggestionList.clear()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as MentionSuggestion
        return if (keyword != that.keyword) false else suggestionList == that.suggestionList
    }

    override fun hashCode(): Int {
        var result = keyword.hashCode()
        result = 31 * result + suggestionList.hashCode()
        return result
    }
}
