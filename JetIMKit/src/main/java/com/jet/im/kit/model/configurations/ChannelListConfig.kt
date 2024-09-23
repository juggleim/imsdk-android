package com.jet.im.kit.model.configurations

import android.os.Parcelable
import com.jet.im.kit.internal.model.template_messages.KeySet
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.jetbrains.annotations.TestOnly

/**
 * This class is used to determine whether to display features in the channel list.
 *
 * @since 3.6.0
 */
@Serializable
@Parcelize
data class ChannelListConfig internal constructor(
    @SerialName(KeySet.enable_typing_indicator)
    private var _enableTypingIndicator: Boolean = false,
    @SerialName(KeySet.enable_message_receipt_status)
    private var _enableMessageReceiptStatus: Boolean = false,

    @Transient
    private var enableTypingIndicatorMutable: Boolean? = null,
    @Transient
    private var enableMessageReceiptStatusMutable: Boolean? = null
) : Parcelable {
    var enableTypingIndicator: Boolean
        /**
         * Returns whether to display typing indicator in the channel list.
         *
         * @return true to display typing indicator in the channel list, false otherwise
         * @since 3.6.0
         */
        get() = enableTypingIndicatorMutable ?: _enableTypingIndicator
        /**
         * Sets whether to display typing indicator in the channel list.
         * If this value is true, the typing indicator is displayed in the channel list.
         *
         * @param value true to display typing indicator in the channel list, false otherwise
         * @since 3.6.0
         */
        set(value) {
            this.enableTypingIndicatorMutable = value
        }
    var enableMessageReceiptStatus: Boolean
        /**
         * Returns whether to display message receipt status in the channel list.
         *
         * @return true to display message receipt status in the channel list, false otherwise
         * @since 3.6.0
         */
        get() = enableMessageReceiptStatusMutable ?: _enableMessageReceiptStatus
        /**
         * Sets whether to display message receipt status in the channel list.
         * If this value is true, the message receipt status is displayed in the channel list.
         *
         * @param value true to display message receipt status in the channel list, false otherwise
         * @since 3.6.0
         */
        set(value) {
            this.enableMessageReceiptStatusMutable = value
        }

    @JvmSynthetic
    internal fun merge(config: ChannelListConfig): ChannelListConfig {
        this._enableTypingIndicator = config._enableTypingIndicator
        this._enableMessageReceiptStatus = config._enableMessageReceiptStatus
        return this
    }

    @JvmSynthetic
    @TestOnly
    internal fun clear() {
        this.enableTypingIndicatorMutable = null
        this.enableMessageReceiptStatusMutable = null
    }

    /**
     * Deeply copies the current instance.
     *
     * @return The new copied instance of [ChannelListConfig]
     * @since 3.9.0
     */
    fun clone() = copy()
}
