package com.jet.im.kit.model.configurations

import android.os.Parcelable
import com.sendbird.android.channel.BaseChannel
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.channel.Role
import com.jet.im.kit.consts.ReplyType
import com.jet.im.kit.consts.ThreadReplySelectType
import com.jet.im.kit.consts.TypingIndicatorType
import com.jet.im.kit.internal.model.serializer.ReplyTypeAsStringSerializer
import com.jet.im.kit.internal.model.serializer.ThreadReplySelectTypeAsStringSerializer
import com.jet.im.kit.internal.model.template_messages.KeySet
import com.jet.im.kit.utils.Available
import com.juggle.im.model.ConversationInfo
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.jetbrains.annotations.TestOnly

/**
 * This class is used to determine whether to display features in the channel.
 *
 * @since 3.6.0
 */
@Serializable
@Parcelize
data class ChannelConfig internal constructor(
    @SerialName(KeySet.enable_ogtag)
    private var _enableOgTag: Boolean = true,
    @SerialName(KeySet.enable_mention)
    private var _enableMention: Boolean = false,
    @SerialName(KeySet.enable_typing_indicator)
    private var _enableTypingIndicator: Boolean = true,
    @SerialName(KeySet.typing_indicator_types)
    private var _typingIndicatorTypes: Set<TypingIndicatorType> = setOf(TypingIndicatorType.TEXT),
    @SerialName(KeySet.enable_reactions)
    private var _enableReactions: Boolean = true,
    @SerialName(KeySet.enable_reactions_supergroup)
    private var _enableReactionsSupergroup: Boolean = false,
    @SerialName(KeySet.enable_voice_message)
    private var _enableVoiceMessage: Boolean = false,
    @SerialName(KeySet.enable_multiple_files_message)
    private var _enableMultipleFilesMessage: Boolean = false,
    @SerialName(KeySet.enable_suggested_replies)
    private var _enableSuggestedReplies: Boolean = false,
    @SerialName(KeySet.enable_form_type_message)
    private var _enableFormTypeMessage: Boolean = false,
    @SerialName(KeySet.enable_feedback)
    private var _enableFeedback: Boolean = false,
    @SerialName(KeySet.thread_reply_select_type)
    @Serializable(with = ThreadReplySelectTypeAsStringSerializer::class)
    private var _threadReplySelectType: ThreadReplySelectType = ThreadReplySelectType.THREAD,
    @SerialName(KeySet.reply_type)
    @Serializable(with = ReplyTypeAsStringSerializer::class)
    private var _replyType: ReplyType = ReplyType.NONE,
    /**
     * Returns <code>Input</code>, which is the configuration of the input area.
     *
     * @return The value of <code>Input</code>
     * @since 3.6.0
     */
    val input: Input = Input(),

    @Transient
    private var enableOgTagMutable: Boolean? = null,
    @Transient
    private var enableMentionMutable: Boolean? = null,
    @Transient
    private var enableTypingIndicatorMutable: Boolean? = null,
    @Transient
    private var typingIndicatorTypesMutable: Set<TypingIndicatorType>? = null,
    @Transient
    private var enableReactionsMutable: Boolean? = null,
    @Transient
    private var enableReactionsSupergroupMutable: Boolean? = null,
    @Transient
    private var enableVoiceMessageMutable: Boolean? = null,
    @Transient
    private var enableMultipleFilesMessageMutable: Boolean? = null,
    @Transient
    private var threadReplySelectTypeMutable: ThreadReplySelectType? = null,
    @Transient
    private var replyTypeMutable: ReplyType? = null,
    @Transient
    private var enableSuggestedRepliesMutable: Boolean? = null,
    @Transient
    private var enableFormTypeMessageMutable: Boolean? = null,
    @Transient
    private var enableFeedbackMutable: Boolean? = null
) : Parcelable {
    companion object {
        /**
         * Returns a value that determines whether to display the ogtag or not.
         * true, if channel displays the content within the ogtag in the message.
         * false, otherwise.
         *
         * This method is affected by the value of [Available.isSupportOgTag].
         * It is also affected by the ogtag value set in the application.
         *
         * @param channelConfig The channel configuration
         * @return true if the ogtag is enabled, false otherwise
         * @since 3.6.0
         */
        @JvmStatic
        fun getEnableOgTag(channelConfig: ChannelConfig): Boolean {
            return Available.isSupportOgTag() && channelConfig.enableOgTag
        }
    }

    var enableOgTag: Boolean
        /**
         * Returns a value that determines whether to display the ogtag or not.
         * true, if channel displays the content within the ogtag in the message.
         * false, otherwise.
         *
         * Only the values set in UIKit dashboard and UIKitConfig are affected.
         *
         * @return true if the ogtag is enabled, false otherwise
         * @since 3.6.0
         */
        get() = enableOgTagMutable ?: _enableOgTag
        /**
         * Sets whether to display the ogtag or not.
         *
         * @param value true if the ogtag is enabled, false otherwise
         * @since 3.6.0
         */
        set(value) {
            enableOgTagMutable = value
        }
    var enableMention: Boolean
        /**
         * Returns a value that determines whether to use the mention or not.
         * true, if channel uses the mention in the message.
         * false, otherwise.
         *
         * @return true if the mention is enabled, false otherwise
         * @since 3.6.0
         */
        get() = enableMentionMutable ?: _enableMention
        /**
         * Sets whether to use the mention or not.
         *
         * @param value true if the mention is enabled, false otherwise
         * @since 3.6.0
         */
        set(value) {
            enableMentionMutable = value
        }
    var enableTypingIndicator: Boolean
        /**
         * Returns a value that determines whether to display the typing indicator or not.
         * true, if channel displays the typing indicator.
         * false, otherwise.
         *
         * @return true if the typing indicator is enabled, false otherwise
         * @since 3.6.0
         */
        get() = enableTypingIndicatorMutable ?: _enableTypingIndicator
        /**
         * Sets whether to display the typing indicator or not.
         *
         * @param value true if the typing indicator is enabled, false otherwise
         * @since 3.6.0
         */
        set(value) {
            enableTypingIndicatorMutable = value
        }
    var typingIndicatorTypes: Set<TypingIndicatorType>
        /**
         * Returns <code>Set<TypingIndicatorType></code>, which is how typingIndicators are displayed in the channel fragment.
         *
         * @return The value of <code>Set<TypingIndicatorType></code>
         * @since 3.11.0
         */
        get() = typingIndicatorTypesMutable ?: _typingIndicatorTypes
        /**
         * Sets the type of the typing indicator.
         *
         * @param value of <code>Set<TypingIndicatorType></code>
         * @since 3.11.0
         */
        set(value) {
            typingIndicatorTypesMutable = value
        }
    var enableReactions: Boolean
        /**
         * Returns a value that determines whether to display the reactions or not.
         * true, if channel displays the reactions in the message.
         * false, otherwise.
         *
         * Only the values set in UIKit dashboard and UIKitConfig are affected.
         * If you want to get the value used when actually drawing in uikit, use [ChannelConfig.getEnableReactions(ChannelConfig, GroupChannel)].
         *
         * This works exclusively with [enableReactionsSupergroup]. In other words, it does not affect whether or not reactions are used in the supergroup channel.
         * If you want to use reactions in a supergroup channel, adjust the [enableReactionsSupergroup] property.
         *
         * @return true if the reactions is enabled, false otherwise
         * @since 3.6.0
         */
        get() = enableReactionsMutable ?: _enableReactions
        /**
         * Sets whether to display the reactions or not.
         *
         * @param value true if the reactions is enabled, false otherwise
         * @since 3.6.0
         */
        set(value) {
            enableReactionsMutable = value
        }
    var enableReactionsSupergroup: Boolean
        /**
         * Returns a value that determines whether to display the reactions or not in the supergroup channel.
         * true, if supergroup channel displays the reactions in the message.
         * false, otherwise.
         *
         * Only the values set in UIKit dashboard and UIKitConfig are affected.
         * If you want to get the value used when actually drawing in uikit, use [ChannelConfig.getEnableReactions(ChannelConfig, GroupChannel)].
         *
         * This works exclusively with [enableReactions]. In other words, it only affects whether or not reactions are used in the supergroup channel.
         * If you want to use reactions in a group channel, adjust the [enableReactions] property.
         *
         * @return true if the reactions is enabled in the supergroup channel, false otherwise
         * @since 3.15.0
         */
        get() = enableReactionsSupergroupMutable ?: _enableReactionsSupergroup
        /**
         * Sets whether to display the reactions or not in the supergroup channel.
         *
         * @param value true if the reactions is enabled in the supergroup channel, false otherwise
         * @since 3.15.0
         */
        set(value) {
            enableReactionsSupergroupMutable = value
        }
    var enableVoiceMessage: Boolean
        /**
         * Returns a value that determines whether to use the voice message or not.
         * true, if channel displays the voice message in the message.
         * false, otherwise.
         *
         * @return true if the voice message is enabled, false otherwise
         * @since 3.6.0
         */
        get() = enableVoiceMessageMutable ?: _enableVoiceMessage
        /**
         * Sets whether to use the voice message or not.
         *
         * @param value true if the voice message is enabled, false otherwise
         * @since 3.6.0
         */
        set(value) {
            enableVoiceMessageMutable = value
        }
    var enableMultipleFilesMessage: Boolean
        /**
         * Returns a value that determines whether to use the multiple files message or not.
         * true, if channel displays the multiple files message in the message list.
         * false, otherwise.
         *
         * @return true if the multiple files message is enabled, false otherwise
         * @since 3.9.0
         */
        get() = enableMultipleFilesMessageMutable ?: _enableMultipleFilesMessage
        /**
         * Sets whether to use the multiple files message or not.
         *
         * @param value true if the multiple files message is enabled, false otherwise
         * @since 3.9.0
         */
        set(value) {
            enableMultipleFilesMessageMutable = value
        }
    var threadReplySelectType: ThreadReplySelectType
        /**
         * Returns <code>ThreadReplySelectType</code>, which determines where to go when reply is selected.
         * <code>ThreadReplySelectType</code> can be applied when the reply type is <code>ReplyType.THREAD</code>.
         *
         * @return The value of <code>ThreadReplySelectType</code>
         * @since 3.6.0
         */
        get() = threadReplySelectTypeMutable ?: _threadReplySelectType
        /**
         * Sets <code>ThreadReplySelectType</code>, which determines where to go when reply is selected.
         * <code>ThreadReplySelectType</code> can be applied when the reply type is <code>ReplyType.THREAD</code>.
         *
         * @param value The value of <code>ThreadReplySelectType</code>
         * @since 3.6.0
         */
        set(value) {
            threadReplySelectTypeMutable = value
        }
    var replyType: ReplyType
        /**
         * Returns <code>ReplyType</code>, which is how replies are displayed in the message list.
         *
         * @return The value of <code>ReplyType</code>
         * @since 3.6.0
         */
        get() = replyTypeMutable ?: _replyType
        /**
         * Sets <code>ReplyType</code>, which is how replies are displayed in the message list.
         * @param value The value of <code>ReplyType</code>
         * @since 3.6.0
         */
        set(value) {
            replyTypeMutable = value
        }
    var enableSuggestedReplies: Boolean
        /**
         * Returns a value that determines whether to use the suggested replies or not.
         * true, if channel displays suggested replies in the message list.
         * false, otherwise.
         *
         * @return true if the suggested replies is enabled, false otherwise
         * @since 3.10.0
         */
        get() = enableSuggestedRepliesMutable ?: _enableSuggestedReplies
        /**
         * Sets whether to use the suggested replies or not.
         *
         * @param value true if the suggested replies is enabled, false otherwise
         * @since 3.10.0
         */
        set(value) {
            enableSuggestedRepliesMutable = value
        }

    var enableFormTypeMessage: Boolean
        /**
         * Returns a value that determines whether to use the form type message or not.
         * true, if channel displays form type message in the message list if it contains form type message data.
         * false, otherwise.
         *
         * @return true if the form type is enabled, false otherwise
         * @since 3.10.0
         */
        get() = enableFormTypeMessageMutable ?: _enableFormTypeMessage
        /**
         * Sets whether to use the form type message or not.
         *
         * @param value true if the form type message is enabled, false otherwise
         * @since 3.10.0
         */
        set(value) {
            enableFormTypeMessageMutable = value
        }

    var enableFeedback: Boolean
        /**
         * Returns a value that determines whether to use feedback of the message or not.
         * true, if channel displays feedback in the message list if it contains feedback data.
         * false, otherwise.
         *
         * @return true if the feedback is enabled, false otherwise
         * @since 3.13.0
         */
        get() = enableFeedbackMutable ?: _enableFeedback
        /**
         * Sets whether to use feedback or not.
         *
         * @param value true if the feedback is enabled, false otherwise
         * @since 3.13.0
         */
        set(value) {
            enableFeedbackMutable = value
        }

    @JvmSynthetic
    internal fun merge(config: ChannelConfig): ChannelConfig {
        this._enableOgTag = config._enableOgTag
        this._enableMention = config._enableMention
        this._enableTypingIndicator = config._enableTypingIndicator
        this._typingIndicatorTypes = config._typingIndicatorTypes
        this._enableReactions = config._enableReactions
        this._enableReactionsSupergroup = config._enableReactionsSupergroup
        this._enableVoiceMessage = config._enableVoiceMessage
        this._enableMultipleFilesMessage = config._enableMultipleFilesMessage
        this._enableSuggestedReplies = config._enableSuggestedReplies
        this._enableFormTypeMessage = config._enableFormTypeMessage
        this._enableFeedback = config._enableFeedback
        this._threadReplySelectType = config._threadReplySelectType
        this._replyType = config._replyType
        this.input.merge(config.input)
        return this
    }

    @TestOnly
    @JvmSynthetic
    internal fun clear() {
        this.enableOgTagMutable = null
        this.enableMentionMutable = null
        this.enableTypingIndicatorMutable = null
        this.typingIndicatorTypesMutable = null
        this.enableReactionsMutable = null
        this.enableReactionsSupergroupMutable = null
        this.enableVoiceMessageMutable = null
        this.enableMultipleFilesMessageMutable = null
        this.threadReplySelectTypeMutable = null
        this.replyTypeMutable = null
        this.enableSuggestedRepliesMutable = null
        this.input.clear()
    }

    /**
     * Deeply copies the current instance.
     *
     * @return The new copied instance of [ChannelConfig]
     * @since 3.9.0
     */
    fun clone(): ChannelConfig = copy(input = this.input.clone())

    @Serializable
    @Parcelize
    data class Input internal constructor(
        @SerialName(KeySet.enable_document)
        private var _enableDocument: Boolean = true,
        /**
         * Returns <code>MediaMenu</code>, which is the configuration of the camera.
         *
         * @return The value of <code>MediaMenu</code>
         * @since 3.6.0
         */
        val camera: MediaMenu = MediaMenu(),
        /**
         * Returns <code>MediaMenu</code>, which is the configuration of the gallery.
         *
         * @return The value of <code>MediaMenu</code>
         * @since 3.6.0
         */
        val gallery: MediaMenu = MediaMenu(),

        @Transient
        private var enableDocumentMutable: Boolean? = null
    ) : Parcelable {
        var enableDocument: Boolean
            /**
             * Returns a value that determines whether to use the document or not.
             *
             * @return true if the document is enabled, false otherwise
             * @since 3.6.0
             */
            get() = enableDocumentMutable ?: _enableDocument
            /**
             * Sets whether to use the document or not.
             *
             * @param value true if the document is enabled, false otherwise
             * @since 3.6.0
             */
            set(value) {
                enableDocumentMutable = value
            }

        @JvmSynthetic
        internal fun merge(config: Input): Input {
            this.camera.merge(config.camera)
            this.gallery.merge(config.gallery)
            this._enableDocument = config._enableDocument
            return this
        }

        @TestOnly
        @JvmSynthetic
        internal fun clear() {
            this.enableDocumentMutable = null
            this.camera.clear()
            this.gallery.clear()
        }

        @JvmSynthetic
        internal fun clone() = copy(camera = camera.copy(), gallery = gallery.copy())
    }
}
