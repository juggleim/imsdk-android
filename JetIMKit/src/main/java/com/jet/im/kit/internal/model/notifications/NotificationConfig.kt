package com.jet.im.kit.internal.model.notifications

internal class NotificationConfig(
    val themeMode: NotificationThemeMode,
    val theme: NotificationChannelTheme
) {
    companion object {
        @JvmStatic
        fun from(notificationChannelSettings: NotificationChannelSettings?): NotificationConfig? {
            return notificationChannelSettings?.let {
                NotificationConfig(
                    notificationChannelSettings.themeMode,
                    notificationChannelSettings.themes.first().copy()
                )
            }
        }
    }
}
