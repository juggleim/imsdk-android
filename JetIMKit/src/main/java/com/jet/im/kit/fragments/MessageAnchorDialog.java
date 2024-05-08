package com.jet.im.kit.fragments;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.content.ContextCompat;

import com.jet.im.kit.R;
import com.jet.im.kit.SendbirdUIKit;
import com.jet.im.kit.interfaces.OnItemClickListener;
import com.jet.im.kit.internal.ui.reactions.DialogView;
import com.jet.im.kit.log.Logger;
import com.jet.im.kit.model.DialogListItem;

class MessageAnchorDialog {
    @NonNull
    private final static Handler mainHandler = new Handler(Looper.getMainLooper());
    @NonNull
    private final View anchorView;
    @NonNull
    private final DialogView contentView;
    @NonNull
    private final View parent;
    @Nullable
    private OnItemClickListener<DialogListItem> itemClickListener;
    @NonNull
    private final PopupWindow window;
    @NonNull
    private final Context context;
    @NonNull
    private final View.OnLayoutChangeListener layoutChangeListener;
    @Nullable
    private PopupWindow.OnDismissListener dismissListener;

    private MessageAnchorDialog(@NonNull View anchorView, @NonNull View parent, @NonNull DialogListItem[] items, boolean useOverlay) {
        this.context = anchorView.getContext();
        this.anchorView = anchorView;
        this.parent = parent;
        int width = (int) context.getResources().getDimension(R.dimen.sb_dialog_width_212);
        this.window = new PopupWindow(width, WindowManager.LayoutParams.WRAP_CONTENT);
        this.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        int themeResId;
        if (useOverlay) {
            themeResId = R.style.Widget_Sendbird_Overlay_DialogView;
        } else {
            themeResId = SendbirdUIKit.isDarkMode() ? R.style.Widget_Sendbird_Dark_DialogView : R.style.Widget_Sendbird_DialogView;
        }
        final Context themeWrapperContext = new ContextThemeWrapper(context, themeResId);
        contentView = new DialogView(themeWrapperContext);
        contentView.setItems(items, (view, position, key) -> {
            window.dismiss();
            if (itemClickListener != null) {
                itemClickListener.onItemClick(view, position, key);
            }
        }, false, R.dimen.sb_size_16);
        contentView.setBackgroundAnchor();

        layoutChangeListener = (v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            int x = getXoff(anchorView);
            int y = getYoff(parent, anchorView, contentView);
            window.update(x, y, -1, -1, true);
        };

        this.window.setOnDismissListener(() -> {
            anchorView.getRootView().removeOnLayoutChangeListener(layoutChangeListener);
            if (dismissListener != null) dismissListener.onDismiss();
        });
    }

    public void show() {
        mainHandler.post(() -> {
            Logger.d(">> MessageAnchorDialog::show()");
            showAnchorList();
        });
    }

    public void dismiss() {
        mainHandler.post(() -> {
            try {
                Logger.d(">> MessageAnchorDialog::dismiss()");
                window.dismiss();
            } catch (Exception e) {
                Logger.d(e);
            }
        });
    }

    public boolean isShowing() {
        return window.isShowing();
    }

    private void showAnchorList() {
        window.setContentView(contentView);
        window.setOutsideTouchable(true);
        window.setFocusable(true);
        window.setBackgroundDrawable(ContextCompat.getDrawable(context, android.R.color.transparent));

        int x = getXoff(anchorView);
        int y = getYoff(parent, anchorView, contentView);
        window.showAtLocation(anchorView, Gravity.START | Gravity.TOP, x, y);
        anchorView.getRootView().addOnLayoutChangeListener(layoutChangeListener);
    }

    private static int getXoff(View anchorView) {
        int[] loc = new int[2];
        anchorView.getLocationOnScreen(loc);
        return loc[0];
    }

    private static int getYoff(View parent, View anchorView, View contentView) {
        contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int contentViewMeasuredHeight = contentView.getMeasuredHeight();
        int[] loc = new int[2];
        anchorView.getLocationOnScreen(loc);

        if (isDropDown(parent, anchorView)) {
            return loc[1] + anchorView.getMeasuredHeight();
        }
        return loc[1] - (contentViewMeasuredHeight);
    }

    private static boolean isDropDown(View parent, View anchorView) {
        int[] loc = new int[2];
        int[] parentLoc = new int[2];
        anchorView.getLocationOnScreen(loc);
        parent.getLocationOnScreen(parentLoc);
        int parentHeight = parent.getMeasuredHeight();
        return (parentHeight / 2 > loc[1] - parentLoc[1]);
    }

    void setOnItemClickListener(OnItemClickListener<DialogListItem> itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    void setOnDismissListener(PopupWindow.OnDismissListener dismissListener) {
        this.dismissListener = dismissListener;
    }

    public static class Builder {
        @NonNull
        private final View anchorView;
        @NonNull
        private final View parent;
        @NonNull
        private final DialogListItem[] items;
        @Nullable
        private OnItemClickListener<DialogListItem> itemClickListener;
        @Nullable
        private PopupWindow.OnDismissListener dismissListener;
        private boolean useOverlay = false;

        public Builder(@NonNull View anchorView, @NonNull View parent, @NonNull DialogListItem[] items) {
            this.anchorView = anchorView;
            this.parent = parent;
            this.items = items;
        }

        @NonNull
        public Builder setOnItemClickListener(@Nullable OnItemClickListener<DialogListItem> itemClickListener) {
            this.itemClickListener = itemClickListener;
            return this;
        }

        @NonNull
        public Builder setOnDismissListener(@Nullable PopupWindow.OnDismissListener dismissListener) {
            this.dismissListener = dismissListener;
            return this;
        }

        @NonNull
        public Builder setUseOverlay(boolean useOverlay) {
            this.useOverlay = useOverlay;
            return this;
        }

        @NonNull
        public MessageAnchorDialog build() {
            MessageAnchorDialog dialog = new MessageAnchorDialog(anchorView, parent, items, useOverlay);
            dialog.setOnItemClickListener(itemClickListener);
            dialog.setOnDismissListener(dismissListener);
            return dialog;
        }
    }
}
