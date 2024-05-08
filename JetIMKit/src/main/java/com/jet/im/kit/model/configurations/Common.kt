package com.jet.im.kit.model.configurations

import android.annotation.SuppressLint
import com.jet.im.kit.internal.model.template_messages.KeySet
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.jetbrains.annotations.NotNull

/**
 * This class is a data class that has common configurations for the UIKit.
 * @since 3.6.0
 */
@Serializable
data class Common internal constructor(
    @SerialName(KeySet.enable_using_default_user_profile)
    private var _enableUsingDefaultUserProfile: Boolean = false
) {
    @Transient
    var enableUsingDefaultUserProfile: Boolean? = null
        /**
         * Returns a value that determines whether to use the default user profile or not.
         * true, if uikit displays the user profile,
         * false, otherwise.
         *
         * @return true if uikit displays the user profile, false otherwise.
         * @since 3.6.0
         */
        @SuppressLint("KotlinNullnessAnnotation")
        @NotNull
        get() = field ?: _enableUsingDefaultUserProfile

    @JvmSynthetic
    internal fun merge(config: Common): Common {
        this._enableUsingDefaultUserProfile = config._enableUsingDefaultUserProfile
        return this
    }
}
