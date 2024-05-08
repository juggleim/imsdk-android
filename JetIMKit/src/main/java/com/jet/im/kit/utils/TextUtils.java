package com.jet.im.kit.utils;

import android.text.SpannableStringBuilder;
import android.text.Spanned;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

@SuppressWarnings("unused")
public class TextUtils {

    public static boolean isEmpty(@Nullable CharSequence text) {
        return text == null || text.length() == 0;
    }

    public static boolean isNotEmpty(@Nullable CharSequence text) {
        return !isEmpty(text);
    }

    @NonNull
    public static String capitalize(@NonNull String text) {
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }

    @NonNull
    public static CharSequence replace(@NonNull CharSequence template,
                                       @NonNull String[] sources,
                                       @NonNull CharSequence[] destinations) {
        final SpannableStringBuilder tb = new SpannableStringBuilder(template);

        int from = 0;
        for (String source : sources) {
            int where = android.text.TextUtils.indexOf(tb, source, from);

            if (where >= 0) {
                from = where + source.length();
                tb.setSpan(source, where, where + source.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }

        for (int i = 0; i < sources.length; i++) {
            int start = tb.getSpanStart(sources[i]);
            int end = tb.getSpanEnd(sources[i]);

            if (start >= 0) {
                tb.replace(start, end, destinations[i]);
            }
        }

        return tb;
    }

}
