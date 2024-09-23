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
data class OpenChannelConfig internal constructor(
    @SerialName(KeySet.enable_ogtag)
    private var _enableOgTag: Boolean = true,
    /**
     * Returns <code>Input</code>, which is the configuration of the input area.
     *
     * @return The value of <code>Input</code>
     * @since 3.6.0
     */
    val input: Input = Input(),

    @Transient
    private var enableOgTagMutable: Boolean? = null
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
         * @param openChannelConfig The open channel configuration
         * @return true if the ogtag is enabled, false otherwise
         * @since 3.6.0
         */
        @JvmStatic
        fun getEnableOgTag(openChannelConfig: OpenChannelConfig): Boolean {
            return Available.isSupportOgTag() && openChannelConfig.enableOgTag
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
         * @param enableOgTag true if the ogtag is enabled, false otherwise
         * @since 3.6.0
         */
        set(enableOgTag) {
            this.enableOgTagMutable = enableOgTag
        }

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
        var enableDocumentMutable: Boolean? = null
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
        internal fun clone() = copy(camera = this.camera.copy(), gallery = this.gallery.copy())
    }

    @JvmSynthetic
    internal fun merge(config: OpenChannelConfig): OpenChannelConfig {
        this._enableOgTag = config._enableOgTag
        this.input.merge(config.input)
        return this
    }

    @JvmSynthetic
    @TestOnly
    internal fun clear() {
        this.enableOgTagMutable = null
        this.input.clear()
    }

    /**
     * Deeply copies the current instance.
     *
     * @return The new copied instance of [OpenChannelConfig]
     * @since 3.9.0
     */
    fun clone() = copy(input = this.input.clone())
}
