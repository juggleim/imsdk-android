package com.jet.im.kit.interfaces

import android.view.View
import com.sendbird.android.message.Reaction

internal fun interface EmojiReactionHandler {
    fun setEmojiReaction(
        reactionList: List<Reaction>,
        emojiReactionClickListener: OnItemClickListener<String>?,
        emojiReactionLongClickListener: OnItemLongClickListener<String>?,
        moreButtonClickListener: View.OnClickListener?
    )
}
