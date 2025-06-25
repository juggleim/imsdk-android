package com.jet.im.kit.call;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.jet.im.kit.R;

public class CallUtils {
    public static void textViewShadowLayer(TextView text, Context context) {
        if (null == text) {
            return;
        }
        text.setShadowLayer(16F, 0F, 2F, context.getApplicationContext().getResources().getColor(R.color.callkit_shadowcolor));
    }

    public static Drawable BackgroundDrawable(int drawable, Context context) {
        return ContextCompat.getDrawable(context, drawable);
    }
}
