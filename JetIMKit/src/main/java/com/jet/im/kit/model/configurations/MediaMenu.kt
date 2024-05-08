package com.jet.im.kit.model.configurations

import android.content.Intent
import android.os.Parcelable
import androidx.activity.result.contract.ActivityResultContracts
import com.jet.im.kit.internal.model.template_messages.KeySet
import com.jet.im.kit.utils.IntentUtils
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.jetbrains.annotations.TestOnly

/**
 * This class is used to determine whether to show the menu for selecting media files in the channel.
 *
 * @since 3.6.0
 */
@Serializable
@Parcelize
data class MediaMenu internal constructor(
    @SerialName(KeySet.enable_photo)
    private var _enablePhoto: Boolean = true,
    @SerialName(KeySet.enable_video)
    private var _enableVideo: Boolean = true,

    @Transient
    private var enablePhotoMutable: Boolean? = null,
    @Transient
    private var enableVideoMutable: Boolean? = null
) : Parcelable {
    var enablePhoto: Boolean
        /**
         * Returns whether the menu for selecting photos is shown in the channel.
         *
         * @return `true` if the menu for selecting photos is shown in the channel, `false` otherwise.
         * @since 3.6.0
         */
        get() = enablePhotoMutable ?: _enablePhoto
        /**
         * Sets whether the menu for selecting photos is shown in the channel.
         *
         * @param value `true` if the menu for selecting photos is shown in the channel, `false` otherwise.
         * @since 3.6.0
         */
        set(value) {
            this.enablePhotoMutable = value
        }

    var enableVideo: Boolean
        /**
         * Returns whether the menu for selecting videos is shown in the channel.
         *
         * @return `true` if the menu for selecting videos is shown in the channel, `false` otherwise.
         * @since 3.6.0
         */
        get() = enableVideoMutable ?: _enableVideo
        /**
         * Sets whether the menu for selecting videos is shown in the channel.
         *
         * @param value `true` if the menu for selecting videos is shown in the channel, `false` otherwise.
         * @since 3.6.0
         */
        set(value) {
            this.enableVideoMutable = value
        }

    /**
     * Returns the intent for selecting media files.
     *
     * @return the intent for selecting media files.
     * @since 3.6.0
     * @deprecated 3.9.0 Use [getPickVisualMediaType] instead.
     */
    @Deprecated(message = "Use Android Photo Picker", replaceWith = ReplaceWith("getPickVisualMediaType()"))
    fun getGalleryIntent(): Intent {
        return if (enablePhoto && !enableVideo) {
            IntentUtils.getImageGalleryIntent()
        } else if (!enablePhoto && enableVideo) {
            IntentUtils.getVideoGalleryIntent()
        } else {
            IntentUtils.getGalleryIntent()
        }
    }

    /**
     * Returns the [ActivityResultContracts.PickVisualMedia.VisualMediaType] for selecting media files.
     *
     * @return the [ActivityResultContracts.PickVisualMedia.VisualMediaType] for selecting media files.
     * @since 3.9.0
     */
    fun getPickVisualMediaType(): ActivityResultContracts.PickVisualMedia.VisualMediaType? {
        return if (enablePhoto && !enableVideo) {
            ActivityResultContracts.PickVisualMedia.ImageOnly
        } else if (!enablePhoto && enableVideo) {
            ActivityResultContracts.PickVisualMedia.VideoOnly
        } else if (enablePhoto && enableVideo) {
            ActivityResultContracts.PickVisualMedia.ImageAndVideo
        } else {
            null
        }
    }

    @JvmSynthetic
    internal fun merge(config: MediaMenu): MediaMenu {
        this._enablePhoto = config._enablePhoto
        this._enableVideo = config._enableVideo
        return this
    }

    @TestOnly
    @JvmSynthetic
    internal fun clear() {
        this.enablePhotoMutable = null
        this.enableVideoMutable = null
    }
}
