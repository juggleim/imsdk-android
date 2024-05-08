package com.jet.im.kit.utils;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;
import androidx.appcompat.view.ContextThemeWrapper;

import com.jet.im.kit.R;
import com.jet.im.kit.SendbirdUIKit;
import com.jet.im.kit.internal.ui.widgets.ToastView;

public class ContextUtils {
    @NonNull
    public static String getApplicationName(@NonNull Context context) {
        return context.getApplicationInfo().loadLabel(context.getPackageManager()).toString();
    }

    public static void toastSuccess(@Nullable Context context, @StringRes int text) {
        toastSuccess(context, text, false);
    }

    public static void toastSuccess(@Nullable Context context, @StringRes int text, boolean useOverlay) {
        if (context == null) {
            return;
        }
        ToastView toastView = createToastView(context, useOverlay);
        toastView.setStatus(ToastView.ToastStatus.SUCCESS);
        toastView.setText(text);
        showToast(context, toastView);
    }

    public static void toastSuccess(@Nullable Context context, @NonNull CharSequence text) {
        toastSuccess(context, text, false);
    }

    public static void toastSuccess(@Nullable Context context, @NonNull CharSequence text, boolean useOverlay) {
        if (context == null) {
            return;
        }
        ToastView toastView = createToastView(context, useOverlay);
        toastView.setStatus(ToastView.ToastStatus.SUCCESS);
        toastView.setText(text);
        showToast(context, toastView);
    }

    public static void toastError(@Nullable Context context, @StringRes int resId) {
        toastError(context, resId, false);
    }

    public static void toastError(@Nullable Context context, @StringRes int resId, boolean useOverlay) {
        if (context == null) {
            return;
        }
        ToastView toastView = createToastView(context, useOverlay);
        toastView.setStatus(ToastView.ToastStatus.ERROR);
        toastView.setText(resId);
        showToast(context, toastView);
    }

    public static void toastError(@Nullable Context context, @NonNull String message) {
        toastError(context, message, false);
    }

    public static void toastError(@Nullable Context context, @NonNull String message, boolean useOverlay) {
        if (context == null) {
            return;
        }
        ToastView toastView = createToastView(context, useOverlay);
        toastView.setStatus(ToastView.ToastStatus.ERROR);
        toastView.setText(message);
        showToast(context, toastView);
    }

    private static void showToast(@Nullable Context context, @NonNull View toastView) {
        Toast toast = new Toast(context);
        toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, 0, 200);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(toastView);
        toast.show();
    }

    @NonNull
    public static Context extractModuleThemeContext(@NonNull Context activityContext, @StyleRes int themeResId, @AttrRes int moduleAttrResId) {
        final Context appThemeContext = new ContextThemeWrapper(activityContext, themeResId);
        final TypedValue values = new TypedValue();
        appThemeContext.getTheme().resolveAttribute(moduleAttrResId, values, true);
        return new ContextThemeWrapper(appThemeContext, values.resourceId);
    }

    @NonNull
    private static ToastView createToastView(@NonNull Context context, boolean useOverlay) {
        int themeResId;
        if (useOverlay) {
            themeResId = R.style.Widget_Sendbird_Overlay_ToastView;
        } else {
            themeResId = SendbirdUIKit.isDarkMode() ? R.style.Widget_Sendbird_Dark_ToastView : R.style.Widget_Sendbird_ToastView;
        }
        final Context themeWrapperContext = new ContextThemeWrapper(context, themeResId);
        return new ToastView(themeWrapperContext);
    }

    @Nullable
    public static Window getWindow(@NonNull Context context) {
        try {
            while (!(context instanceof Activity) && context instanceof ContextWrapper) {
                context = ((ContextWrapper) context).getBaseContext();
            }
            if (!(context instanceof Activity)) throw new Exception();
            return ((Activity) context).getWindow();
        } catch (Throwable ignore) {}
        return null;
    }
}
