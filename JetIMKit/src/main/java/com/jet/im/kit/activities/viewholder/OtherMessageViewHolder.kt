package com.jet.im.kit.activities.viewholder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import com.jet.im.kit.annotation.MessageViewHolderExperimental
import com.jet.im.kit.consts.ClickableViewIdentifier
import com.jet.im.kit.databinding.SbViewOtherMessageBinding
import com.jet.im.kit.interfaces.EmojiReactionHandler
import com.jet.im.kit.interfaces.OnItemClickListener
import com.jet.im.kit.interfaces.OnItemLongClickListener
import com.jet.im.kit.internal.extensions.toComponentListContextThemeWrapper
import com.jet.im.kit.model.MessageListUIParams
import com.juggle.im.model.ConversationInfo
import com.juggle.im.model.Message
import com.juggle.im.model.MessageReactionItem
import com.sendbird.android.message.Emoji
import com.sendbird.android.message.Reaction

/**
 * This ViewHolder has a basic message template for 'Other message.'
 * To use it, inherit from this ViewHolder, inflate the view corresponding to the content, and pass it to the constructor.
 *
 * @see [com.jet.im.kit.activities.adapter.MessageListAdapter]
 * @see [com.jet.im.kit.providers.AdapterProviders.messageList]
 * @since 3.12.0
 */
@MessageViewHolderExperimental
open class OtherMessageViewHolder(
    parent: ViewGroup,
    open val contentView: View,
    messageListUIParams: MessageListUIParams,
    private val binding: SbViewOtherMessageBinding = SbViewOtherMessageBinding.inflate(
        LayoutInflater.from(parent.context.toComponentListContextThemeWrapper())
    )
) : MessageViewHolder(binding.root, messageListUIParams), EmojiReactionHandler {

    init {
        binding.root.attachContentView(contentView)
    }

    @CallSuper
    override fun bind(channel: ConversationInfo, message: Message, reactionList: List<MessageReactionItem>, params: MessageListUIParams) {
        binding.root.drawMessage(channel, message, reactionList, params)
    }

    @CallSuper
    override fun getClickableViewMap(): Map<String, View> {
        return mapOf(
            ClickableViewIdentifier.Chat.name to binding.root.binding.contentPanel,
        )
    }

    /**
     * Sets message reaction data.
     *
     * @param reactionList List of reactions which the message has.
     * @param emojiReactionClickListener The callback to be invoked when the emoji reaction is clicked and held.
     * @param emojiReactionLongClickListener The callback to be invoked when the emoji reaction is long clicked and held.
     * @param moreButtonClickListener The callback to be invoked when the emoji reaction more button is clicked and held.
     * @since 3.12.0
     */
    fun setEmojiReaction(
        reactionList: List<MessageReactionItem>,
        emojiReactionClickListener: OnItemClickListener<String>?,
        emojiReactionLongClickListener: OnItemLongClickListener<String>?,
        moreButtonClickListener: View.OnClickListener?
    ) {
        binding.root.binding.rvEmojiReactionList.apply {
            setReactionList(reactionList)
            setClickListeners(emojiReactionClickListener, emojiReactionLongClickListener, moreButtonClickListener)
        }
    }

    /**
     * Sets message reaction data.
     *
     * @param reactionList List of reactions which the message has.
     * @param totalEmojiList The total list of emojis allowed for this message. This value is used to compare whether `add` button should be displayed from the reactions view. Defaults to [EmojiManager.allEmojis].
     * @param emojiReactionClickListener The callback to be invoked when the emoji reaction is clicked and held.
     * @param emojiReactionLongClickListener The callback to be invoked when the emoji reaction is long clicked and held.
     * @param moreButtonClickListener The callback to be invoked when the emoji reaction more button is clicked and held.
     * @since 3.20.0
     */
    final override fun setEmojiReaction(
        reactionList: List<MessageReactionItem>,
        totalEmojiList: List<String>,
        emojiReactionClickListener: OnItemClickListener<String>?,
        emojiReactionLongClickListener: OnItemLongClickListener<String>?,
        moreButtonClickListener: View.OnClickListener?
    ) {
        binding.root.binding.rvEmojiReactionList.apply {
            setReactionList(reactionList, totalEmojiList)
            setClickListeners(emojiReactionClickListener, emojiReactionLongClickListener, moreButtonClickListener)
        }
    }
}
