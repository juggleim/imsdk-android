package com.jet.im.kit.model.configurations

import android.os.Parcelable
import com.jet.im.kit.internal.model.template_messages.KeySet
import com.jet.im.kit.utils.Available
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.jetbrains.annotations.TestOnly

@Serializable
@Parcelize
data class ChannelSettingConfig internal constructor(
    @SerialName(KeySet.enable_message_search)
    private var _enableMessageSearch: Boolean = false,

    @Transient
    private var enableMessageSearchMutable: Boolean? = null
) : Parcelable {

    companion object {
        /**
         * Returns a value that determines whether to display the search menu or not.
         * true, if channel settings displays the search menu.
         * false, otherwise.
         *
         * This method is affected by the value of [Available.isSupportMessageSearch].
         * It is also affected by the enable search value set in the application.
         *
         * @param channelSettingConfig The channel setting configuration
         * @return true to display message search in the channel setting, false otherwise
         * @since 3.6.0
         */
        @JvmStatic
        fun getEnableMessageSearch(channelSettingConfig: ChannelSettingConfig): Boolean {
            return Available.isSupportMessageSearch() && channelSettingConfig.enableMessageSearch
        }
    }

    var enableMessageSearch: Boolean
        /**
         * Returns whether to display message search in the channel setting.
         *
         * @return true to display message search in the channel setting, false otherwise
         * @since 3.6.0
         */
        get() = enableMessageSearchMutable ?: _enableMessageSearch
        /**
         * Sets whether to display message search in the channel setting.
         * If this value is true, the message search is displayed in the channel setting.
         *
         * @param value true to display message search in the channel setting, false otherwise
         * @since 3.6.0
         */
        set(value) {
            this.enableMessageSearchMutable = value
        }

    @JvmSynthetic
    internal fun merge(config: ChannelSettingConfig): ChannelSettingConfig {
        this._enableMessageSearch = config._enableMessageSearch
        return this
    }

    @JvmSynthetic
    @TestOnly
    internal fun clear() {
        this.enableMessageSearchMutable = null
    }

    /**
     * Deeply copies the current instance.
     *
     * @return The new copied instance of [ChannelSettingConfig]
     * @since 3.9.0
     */
    fun clone() = copy()
}
