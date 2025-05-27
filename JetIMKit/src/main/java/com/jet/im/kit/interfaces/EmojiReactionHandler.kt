package com.jet.im.kit.interfaces

import android.view.View
import com.juggle.im.model.MessageReactionItem

internal fun interface EmojiReactionHandler {
    fun setEmojiReaction(
        reactionList: List<MessageReactionItem>,
        totalEmojiList: List<String>,
        emojiReactionClickListener: OnItemClickListener<String>?,
        emojiReactionLongClickListener: OnItemLongClickListener<String>?,
        moreButtonClickListener: View.OnClickListener?
    )
}
