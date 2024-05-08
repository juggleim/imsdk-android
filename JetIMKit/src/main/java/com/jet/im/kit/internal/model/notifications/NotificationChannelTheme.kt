@file:UseSerializers(CSVColorIntAsStringSerializer::class)

package com.jet.im.kit.internal.model.notifications

import com.jet.im.kit.internal.model.serializer.CSVColorIntAsStringSerializer
import com.jet.im.kit.internal.model.template_messages.KeySet
import com.jet.im.kit.internal.model.template_messages.Weight
import com.jet.im.kit.internal.singleton.JsonParser
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
internal data class NotificationChannelSettings(
    @SerialName(KeySet.updated_at)
    val updatedAt: Long,
    @SerialName(KeySet.theme_mode)
    val themeMode: NotificationThemeMode,
    val themes: List<NotificationChannelTheme>
) {
    companion object {
        @JvmStatic
        fun fromJson(jsonStr: String): NotificationChannelSettings {
            return JsonParser.fromJson(jsonStr)
        }
    }

    override fun toString(): String {
        return JsonParser.toJsonString(this)
    }

    fun getThemeOrNull(): NotificationChannelTheme? {
        return themes.firstOrNull()
    }
}

@Serializable
internal data class NotificationChannelTheme(
    val key: String,
    @SerialName(KeySet.created_at)
    val createdAt: Long,
    @SerialName(KeySet.updated_at)
    val updatedAt: Long,
    @SerialName(KeySet.notification)
    val notificationTheme: NotificationTheme,
    @SerialName(KeySet.list)
    val listTheme: NotificationListTheme,
    @SerialName(KeySet.header)
    val headerTheme: NotificationHeaderTheme
)

@Serializable
internal data class NotificationTheme(
    val radius: Int = 0,
    val backgroundColor: CSVColor,
    val unreadIndicatorColor: CSVColor,

    /**
     * This value is deprecated from version v1.2 of the Notification.
     * Replaced by [label].
     */
    @Deprecated("This value is deprecated from version v1.2 of the Notification. Replaced by label.")
    val category: FontStyle,
    val sentAt: FontStyle,
    val pressedColor: CSVColor,
    val label: FontStyle? = null,
)

@Serializable
internal data class NotificationListTheme(
    val backgroundColor: CSVColor,
    val tooltip: TooltipStyle,
    val timeline: TimelineStyle,
    /**
     * Added from uikit version `3.8.0` of the Notification.
     * Since the previously saved values did not include this field, a default value needs to be set.
     * affected field : category
     */
    val category: CategoryFilterStyle? = null
)

@Serializable
internal data class NotificationHeaderTheme(
    val textSize: Int,
    val textColor: CSVColor,
    val buttonIconTintColor: CSVColor,
    val backgroundColor: CSVColor,
    val lineColor: CSVColor,
    /**
     * Added from version v1.2 of the Notification.
     * Since the previously saved values did not include this field, a default value needs to be set.
     * affected field : fontWeight
     */
    val fontWeight: Weight = Weight.Normal
)

@Serializable
internal data class FontStyle(
    val textSize: Int,
    val textColor: CSVColor,
    /**
     * Added from version v1.2 of the Notification.
     * Since the previously saved values did not include this field, a default value needs to be set.
     * affected field : fontWeight
     */
    val fontWeight: Weight = Weight.Normal
)

@Serializable
internal data class TooltipStyle(
    val backgroundColor: CSVColor,
    val textColor: CSVColor,
    /**
     * Added from version v1.2 of the Notification.
     * Since the previously saved values did not include this field, a default value needs to be set.
     * affected field : textSize, fontWeight
     */
    val textSize: Int = 14,
    val fontWeight: Weight = Weight.Normal
)

@Serializable
internal data class TimelineStyle(
    val backgroundColor: CSVColor,
    val textColor: CSVColor,
    /**
     * Added from version v1.2 of the Notification.
     * Since the previously saved values did not include this field, a default value needs to be set.
     * affected field : textSize, fontWeight
     */
    val textSize: Int = 12,
    val fontWeight: Weight = Weight.Normal
)

@Serializable
internal data class CategoryFilterStyle(
    val backgroundColor: CSVColor,
    val fontWeight: Weight = Weight.Normal,
    val radius: Int = 15,
    val selectedBackgroundColor: CSVColor,
    val selectedTextColor: CSVColor,
    val textColor: CSVColor,
    val textSize: Int,
)
