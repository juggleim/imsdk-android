package com.juggle.chat.settings;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.PopupWindow;

import com.juggle.chat.R;

public class MorePopWindow extends PopupWindow implements PopupWindow.OnDismissListener {
    private Activity context;
    private OnPopWindowItemClickListener listener;
    private View contentView;
    private static final float ALPHA_TRANSPARENT_COMPLETE = 1.0f;

    public interface OnPopWindowItemClickListener {
        void onCreateGroupClick();

        void onAddFriendClick();

        void onScanClick();
    }

    @SuppressLint("InflateParams")
    public MorePopWindow(final Activity context, OnPopWindowItemClickListener listener) {
        this.listener = listener;
        this.context = context;
        LayoutInflater inflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        contentView = inflater.inflate(R.layout.main_popup_title_more, null);

        // Popup window view
        this.setContentView(contentView);
        // Popup window width
        this.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        // Popup window height
        this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        // Popup window is clickable
        this.setFocusable(true);
        this.setOutsideTouchable(true);
        // Refresh state
        this.update();
        // Create a semi-transparent ColorDrawable.
        ColorDrawable dw = new ColorDrawable(0000000000);
        // Dismiss when back is pressed or the user taps outside; this also enables the dismiss listener.
        this.setBackgroundDrawable(dw);

        setOnDismissListener(this);

        // Popup window animation
        this.setAnimationStyle(R.style.AnimationMainTitleMore);
        contentView
                .findViewById(R.id.btn_create_group)
                .setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (listener != null) {
                                    listener.onCreateGroupClick();
                                }
                                dismiss();
                            }
                        });
        contentView
                .findViewById(R.id.btn_add_friends)
                .setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (listener != null) {
                                    listener.onAddFriendClick();
                                }
                                dismiss();
                            }
                        });
        contentView
                .findViewById(R.id.btn_scan)
                .setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (listener != null) {
                                    listener.onScanClick();
                                }
                                dismiss();
                            }
                        });
    }

    /**
     * Show the popup window.
     *
     * @param parent the anchor view
     */
    public void showPopupWindow(View parent) {
        if (!this.isShowing()) {
            // Show the popup window as a drop-down.
            this.showAsDropDown(parent, 0, 0);
        } else {
            this.dismiss();
        }
    }

    /**
     * @param parent the anchor view
     * @param alpha the background alpha
     */
    public void showPopupWindow(View parent, float alpha, int xoff, int yoff) {
        if (!this.isShowing()) {
            // Show the popup window as a drop-down.
            this.showAsDropDown(parent, xoff, yoff);
            setAlpha(alpha);
        } else {
            this.dismiss();
            setAlpha(ALPHA_TRANSPARENT_COMPLETE);
        }
    }

    private void setAlpha(float bgAlpha) {
        if (context == null || context.getWindow() == null) {
            return;
        }
        Window window = context.getWindow();
        WindowManager.LayoutParams lp = context.getWindow().getAttributes();
        // 0.0-1.0
        lp.alpha = bgAlpha;
        window.setAttributes(lp);
        // Everything behind this window will be dimmed.
        // This method sets the dimmed overlay so it works on devices where the default dimming fails.
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }

    @Override
    public void onDismiss() {
        super.dismiss();
        setAlpha(ALPHA_TRANSPARENT_COMPLETE);
    }
}
