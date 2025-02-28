package com.jet.im.kit.utils;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.juggle.im.JIM;
import com.jet.im.kit.R;
import com.jet.im.kit.interfaces.UserInfo;

public class UserUtils {
    @NonNull
    public static String getDisplayName(@NonNull Context context, @Nullable com.juggle.im.model.UserInfo user) {
        return getDisplayName(context, user, false);
    }

    @NonNull
    public static String getDisplayName(@NonNull Context context, @Nullable com.juggle.im.model.UserInfo user, boolean usePronouns) {
        return getDisplayName(context, user, usePronouns, Integer.MAX_VALUE);
    }



    @NonNull
    public static String getDisplayName(@NonNull Context context, @Nullable com.juggle.im.model.UserInfo userInfo, boolean usePronouns, int maxLength) {
        String nickname = context.getString(R.string.sb_text_channel_list_title_unknown);
        if (userInfo == null) return nickname;

        if (usePronouns && userInfo.getUserId() != null &&
                JIM.getInstance().getCurrentUserId() != null &&
                userInfo.getUserId().equals(JIM.getInstance().getCurrentUserId())) {
            nickname = context.getString(R.string.sb_text_you);
        } else if (!TextUtils.isEmpty(userInfo.getUserName())) {
            nickname = userInfo.getUserName();
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
}
