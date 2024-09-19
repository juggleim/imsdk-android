package com.jet.im.kit.utils;

import androidx.annotation.NonNull;

import com.sendbird.android.AppInfo;
import com.sendbird.android.NotificationInfo;
import com.sendbird.android.SendbirdChat;
import com.jet.im.kit.consts.StringSet;

@SuppressWarnings("unused")
public class Available {
    private static boolean isAvailable(@NonNull String key) {
        return false;
    }

    /**
     * Checks if the application support super group channel.
     *
     * @return <code>true</code> if super group channel can be usable, <code>false</code> otherwise.
     * since 1.2.0
     */
    public static boolean isSupportSuper() {
        return isAvailable(StringSet.allow_super_group_channel);
    }

    /**
     * Checks if the application support broadcast channel.
     *
     * @return <code>true</code> if broadcast channel can be usable, <code>false</code> otherwise.
     * since 1.2.0
     */
    public static boolean isSupportBroadcast() {
        return isAvailable(StringSet.allow_broadcast_channel);
    }

    /**
     * Checks if the application support reaction.
     *
     * @return <code>true</code> if the react operation can be usable, <code>false</code> otherwise.
     * since 1.2.0
     */
    public static boolean isSupportReaction() {
        return isAvailable(StringSet.reactions);
    }

    /**
     * Checks if the application support og metadata..
     *
     * @return <code>true</code> if the og metadata can be usable, <code>false</code> otherwise.
     * since 1.2.0
     */
    public static boolean isSupportOgTag() {
        return isAvailable(StringSet.enable_og_tag);
    }

    /**
     * Checks if the application support message search.
     *
     * @return <code>true</code> if the message search operation can be usable, <code>false</code> otherwise.
     * since 2.1.0
     */
    public static boolean isSupportMessageSearch() {
        return isAvailable(StringSet.message_search_v3);
    }

    /**
     * Checks if the application support chat notification.
     *
     * @return <code>true</code> if the chat notification is available, <code>false</code> otherwise.
     * since 3.5.0
     */
    public static boolean isSupportChatNotification() {
        boolean includeChatNotification = false;
        final AppInfo appInfo = SendbirdChat.getAppInfo();
        if (appInfo != null) {
            final NotificationInfo notificationInfo = appInfo.getNotificationInfo();
            if (notificationInfo != null) {
                includeChatNotification = notificationInfo.isEnabled();
            }
        }
        return includeChatNotification;
    }
}
