package com.jet.im.kit.utils;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sendbird.android.SendbirdChat;
import com.sendbird.android.user.User;
import com.jet.im.kit.R;
import com.jet.im.kit.interfaces.UserInfo;

public class UserUtils {
    @NonNull
    public static <T extends User> UserInfo toUserInfo(@NonNull T user) {
        return new UserInfo() {
            @NonNull
            @Override
            public String getUserId() {
                return user.getUserId();
            }

            @Nullable
            @Override
            public String getNickname() {
                return user.getNickname();
            }

            @Nullable
            @Override
            public String getProfileUrl() {
                return user.getProfileUrl();
            }
        };
    }

    @NonNull
    public static String getDisplayName(@NonNull Context context, @Nullable User user) {
        return getDisplayName(context, user, false);
    }

    @NonNull
    public static String getDisplayName(@NonNull Context context, @Nullable User user, boolean usePronouns) {
        return getDisplayName(context, user, usePronouns, Integer.MAX_VALUE);
    }

    @NonNull
    public static String getDisplayName(@NonNull Context context, @Nullable User user, boolean usePronouns, int maxLength) {
        String nickname = context.getString(R.string.sb_text_channel_list_title_unknown);
        if (user == null) return nickname;

        if (usePronouns && user.getUserId() != null &&
            SendbirdChat.getCurrentUser() != null &&
            user.getUserId().equals(SendbirdChat.getCurrentUser().getUserId())) {
            nickname = context.getString(R.string.sb_text_you);
        } else if (!TextUtils.isEmpty(user.getNickname())) {
            nickname = user.getNickname();
        }

        if (nickname.length() > maxLength) {
            nickname = nickname.substring(0, maxLength) + context.getString(R.string.sb_text_ellipsis);
        }
        return nickname;
    }

    @NonNull
    public static String getDisplayName(@NonNull Context context, @Nullable UserInfo userInfo) {
        String nickname = context.getString(R.string.sb_text_channel_list_title_unknown);
        if (userInfo == null) return nickname;

        if (userInfo.getNickname() != null && userInfo.getNickname().length() > 0) {
            nickname = userInfo.getNickname();
        }
        return nickname;
    }

    @NonNull
    public static String getDisplayName(@NonNull Context context, @Nullable com.juggle.im.model.UserInfo userInfo) {
        String nickname = context.getString(R.string.sb_text_channel_list_title_unknown);
        if (userInfo == null) return nickname;

        if (userInfo.getUserName() != null && !userInfo.getUserName().isEmpty()) {
            nickname = userInfo.getUserName();
        }
        return nickname;
    }
}
