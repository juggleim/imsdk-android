package com.jet.im.kit.model

import com.jet.im.kit.consts.MessageGroupType
import com.jet.im.kit.model.configurations.ChannelConfig
import com.jet.im.kit.model.configurations.OpenChannelConfig
import com.jet.im.kit.model.configurations.UIKitConfig
import com.jet.im.kit.model.configurations.UIKitConfig.groupChannelConfig

/**
 * Describes a configuration of a message item view
 * @since 3.3.0
 */
data class MessageListUIParams internal constructor(
    /**
     * Returns the type of message group UI.
     *
     * @return The value of [MessageGroupType]
     * @since 3.3.0
     */
    val messageGroupType: MessageGroupType,
    private val useMessageGroupUI: Boolean,
    private val useReverseLayout: Boolean,
    private val useQuotedView: Boolean,
    private val useMessageReceipt: Boolean,
    /**
     * Returns [ChannelConfig] that contains the configuration of the channel.
     *
     * @return The value of [ChannelConfig]
     * @since 3.6.0
     * @see ChannelConfig
     */
    val channelConfig: ChannelConfig,
    /**
     * Returns [OpenChannelConfig] that contains the configuration of the open channel.
     *
     * @return The value of [OpenChannelConfig]
     * @since 3.6.0
     * @see OpenChannelConfig
     */
    val openChannelConfig: OpenChannelConfig
) {

    /**
     * Returns whether the message grouping is used.
     *
     * @return `true` if the message grouping is used, `false` otherwise
     * @since 3.3.0
     */
    fun shouldUseMessageGroupUI(): Boolean {
        return useMessageGroupUI
    }

    /**
     * Returns whether the message list is reversed.
     *
     * @return `true` if the message list is reversed, `false` otherwise
     * @since 3.3.0
     */
    fun shouldUseReverseLayout(): Boolean {
        return useReverseLayout
    }

    /**
     * Returns whether the status (read receipt, delivery receipt) of messages is shown.
     *
     * @return `true` if the message receipt is shown, `false` otherwise
     * @since 3.3.0
     */
    fun shouldUseMessageReceipt(): Boolean {
        return useMessageReceipt
    }

    class Builder {
        private var messageGroupType = MessageGroupType.GROUPING_TYPE_SINGLE
        private var useMessageGroupUI = true
        private var useReverseLayout = true
        private var useQuotedView = false
        private var useMessageReceipt = true
        private var channelConfig = groupChannelConfig
        private var openChannelConfig = UIKitConfig.openChannelConfig

        /**
         * Constructor
         * @since 3.3.0
         */
        constructor()

        /**
         * Constructor
         *
         * @param params The message draw parameter to be used as the base
         * @since 3.3.0
         */
        constructor(params: MessageListUIParams) {
            messageGroupType = params.messageGroupType
            useMessageGroupUI = params.useMessageGroupUI
            useReverseLayout = params.useReverseLayout
            useQuotedView = params.useQuotedView
            useMessageReceipt = params.useMessageReceipt
            channelConfig = params.channelConfig
            this.openChannelConfig = params.openChannelConfig
        }

        /**
         * Sets the type of message group UI.
         *
         * @param messageGroupType The value of [MessageGroupType]
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 3.3.0
         */
        fun setMessageGroupType(messageGroupType: MessageGroupType): Builder {
            this.messageGroupType = messageGroupType
            return this
        }

        /**
         * Sets whether the quoted view is used.
         *
         * @param useQuotedView `true` if the quoted view is used, `false` otherwise
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 3.3.0
         */
        fun setUseQuotedView(useQuotedView: Boolean): Builder {
            this.useQuotedView = useQuotedView
            return this
        }

        /**
         * Sets whether the message grouping is used.
         *
         * @param useMessageGroupUI `true` if the message grouping is used, `false` otherwise
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 3.3.0
         */
        fun setUseMessageGroupUI(useMessageGroupUI: Boolean): Builder {
            this.useMessageGroupUI = useMessageGroupUI
            return this
        }

        /**
         * Sets whether the message list is reversed.
         *
         * @param useReverseLayout `true` if the message list is reversed, `false` otherwise
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 3.3.0
         */
        fun setUseReverseLayout(useReverseLayout: Boolean): Builder {
            this.useReverseLayout = useReverseLayout
            return this
        }

        /**
         * Sets whether the status (read receipt, delivery receipt) of messages is shown.
         *
         * @param useMessageReceipt `true` if the message receipt is shown, `false` otherwise
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 3.3.0
         */
        fun setUseMessageReceipt(useMessageReceipt: Boolean): Builder {
            this.useMessageReceipt = useMessageReceipt
            return this
        }

        /**
         * Sets [ChannelConfig] that contains the configuration of the channel.
         * Use `UIKitConfig.groupChannelConfig.clone()` for the default value.
         * Example usage:
         *
         * ```kotlin
         * val params = MessageListUIParams.Builder()
         *                 .setChannelConfig(UIKitConfig.groupChannelConfig.clone().apply {
         *                     this.enableMention = true
         *                 }).build()
         * ```
         * @param channelConfig The value of [ChannelConfig]
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 3.6.0
         * @see ChannelConfig
         */
        fun setChannelConfig(channelConfig: ChannelConfig): Builder {
            this.channelConfig = channelConfig
            return this
        }

        /**
         * Sets [OpenChannelConfig] that contains the configuration of the open channel.
         * Use `UIKitConfig.openChannelConfig.clone()` for the default value.
         * Example usage:
         *
         *```kotlin
         * val params = MessageListUIParams.Builder()
         *             .setOpenChannelConfig(UIKitConfig.openChannelConfig.clone().apply {
         *                  this.enableOgTag = false
         *             }).build()
         * ```
         *
         *
         * @param openChannelConfig The value of [OpenChannelConfig]
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 3.6.0
         * @see OpenChannelConfig
         */
        fun setOpenChannelConfig(openChannelConfig: OpenChannelConfig): Builder {
            this.openChannelConfig = openChannelConfig
            return this
        }

        /**
         * Builds an [MessageListUIParams] with the properties supplied to this builder.
         *
         * @return The [MessageListUIParams] from this builder instance.
         * @since 3.3.0
         */
        fun build(): MessageListUIParams {
            return MessageListUIParams(
                messageGroupType,
                useMessageGroupUI,
                useReverseLayout,
                useQuotedView,
                useMessageReceipt,
                channelConfig,
                this.openChannelConfig
            )
        }
    }
}
