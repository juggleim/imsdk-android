package com.jet.im.kit.internal.model.notifications

import android.graphics.Color
import com.jet.im.kit.SendbirdUIKit
import com.jet.im.kit.internal.model.template_messages.KeySet
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException

@Serializable
internal enum class NotificationThemeMode(val value: Int) {

    @SerialName(KeySet.light)
    Light(0),

    @SerialName(KeySet.dark)
    Dark(1),

    @SerialName(KeySet.default)
    Default(2)
}

@Serializable
internal data class CSVColor(
    private val color: String
) {
    fun getColor(themeMode: NotificationThemeMode): Int {
        return Color.parseColor(getColorHexString(themeMode))
    }

    @JvmOverloads
    fun getColorHexString(themeMode: NotificationThemeMode? = null): String {
        return themeMode?.let {
            val values = color.split(",")
            if (values.isEmpty()) {
                throw SerializationException("color value must have value")
            }
            if (values.size == 1) {
                return values[0]
            }
            var currentTheme = themeMode
            if (themeMode == NotificationThemeMode.Default) {
                // follow UIKit theme
                currentTheme = when (SendbirdUIKit.getDefaultThemeMode()) {
                    SendbirdUIKit.ThemeMode.Light -> NotificationThemeMode.Light
                    SendbirdUIKit.ThemeMode.Dark -> NotificationThemeMode.Dark
                }
            }
            return values[currentTheme.value]
        } ?: color
    }
}
