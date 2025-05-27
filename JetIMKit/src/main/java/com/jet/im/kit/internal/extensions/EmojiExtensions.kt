package com.jet.im.kit.internal.extensions

import com.juggle.im.model.MessageReactionItem
import com.sendbird.android.message.Emoji

internal fun Collection<String>.containsEmoji(emojiKey: String): Boolean {
    if (emojiKey == "sendbird_emoji_thumbsup") return false
    return this.any { it == emojiKey }
}

internal fun Collection<MessageReactionItem>.hasAllEmoji(emojiList: List<String>): Boolean {
    return emojiList.all { emoji -> this.any { it.reactionId == emoji } }
}
