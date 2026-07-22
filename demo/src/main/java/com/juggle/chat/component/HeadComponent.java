package com.juggle.chat.component;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.juggle.chat.R;


/**
 * Description:
 *
 * @author haogaohui
 * @since 5.10.4
 */
public class HeadComponent extends BaseComponent {

    private LinearLayout rightContainer;
    private TextView leftTextView;
    private TextView titleTextView;
    private TextView rightTextView;

    private View.OnClickListener onLeftClickListener;
    private View.OnClickListener onTitleClickListener;
    private View.OnClickListener onRightClickListener;
    private int rightTextColorDefault;
    private int rightTextColorDisable;

    public HeadComponent(Context context) {
        super(context);
    }

    public HeadComponent(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public HeadComponent(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected View onCreateView(
            Context context, LayoutInflater from, @NonNull ViewGroup parent, AttributeSet attrs) {
        View view = from.inflate(R.layout.rc_head_component, parent, false);
        rightContainer = view.findViewById(R.id.right_container);
        leftTextView = view.findViewById(R.id.left_text);
        titleTextView = view.findViewById(R.id.title_text);
        rightTextView = view.findViewById(R.id.right_text);

        if (attrs != null) {
            TypedArray typedArray = null;
            try {
                typedArray = context.obtainStyledAttributes(attrs, R.styleable.HeadComponent);

                String title = typedArray.getString(R.styleable.HeadComponent_head_title_text);
                String leftText = typedArray.getString(R.styleable.HeadComponent_head_left_text);
                String rightText = typedArray.getString(R.styleable.HeadComponent_head_right_text);
                rightTextColorDefault =
                        typedArray.getColor(
                                R.styleable.HeadComponent_head_right_text_color_default, -1);
                rightTextColorDisable =
                        typedArray.getColor(
                                R.styleable.HeadComponent_head_right_text_color_disable, -1);
                int leftDrawable =
                        typedArray.getResourceId(
                                R.styleable.HeadComponent_head_left_text_drawable, -1);
                int titleDrawable =
                        typedArray.getResourceId(
                                R.styleable.HeadComponent_head_title_text_drawable, -1);
                int rightDrawable =
                        typedArray.getResourceId(
                                R.styleable.HeadComponent_head_right_text_drawable, -1);

                if (title != null) {
                    titleTextView.setText(title);
                }

                if (leftText != null) {
                    leftTextView.setText(leftText);
                }

                if (rightText != null) {
                    rightTextView.setText(rightText);
                    rightTextView.setVisibility(VISIBLE);
                }

                if (leftDrawable != -1) {
                    setLeftTextDrawable(leftDrawable);
                }

                if (titleDrawable != -1) {
                    setTitleTextDrawable(titleDrawable);
                }

                if (rightDrawable != -1) {
                    setRightTextDrawable(rightDrawable);
                }
                if (rightTextColorDefault != -1) {
                    rightTextView.setTextColor(rightTextColorDefault);
                }
            } finally {
                if (typedArray != null) {
                    typedArray.recycle();
                }
            }
        }

        // Set click listeners
        leftTextView.setOnClickListener(
                v -> {
                    if (onLeftClickListener != null) {
                        onLeftClickListener.onClick(v);
                    } else {
                        if (getContext() instanceof Activity) {
                            ((Activity) getContext()).finish();
                        }
                    }
                });

        titleTextView.setOnClickListener(
                v -> {
                    if (onTitleClickListener != null) {
                        onTitleClickListener.onClick(v);
                    }
                });

        rightTextView.setOnClickListener(
                v -> {
                    if (onRightClickListener != null) {
                        onRightClickListener.onClick(v);
                    }
                });

        return view;
    }

    /**
     * Set the title.
     *
     * @param title the title
     */
    public void setTitleText(String title) {
        titleTextView.setText(title);
    }

    /**
     * Set the left text.
     *
     * @param text the text
     */
    public void setLeftText(String text) {
        leftTextView.setText(text);
    }

    /**
     * Set the right text.
     *
     * @param text the text
     */
    public void setRightText(String text) {
        rightTextView.setText(text);
        rightTextView.setVisibility(VISIBLE);
    }

    /**
     * Set the left text drawable.
     *
     * @param resId the drawable resource id
     */
    public void setLeftTextDrawable(int resId) {
        Drawable drawable = getResources().getDrawable(resId);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        leftTextView.setCompoundDrawables(drawable, null, null, null);
        leftTextView.setVisibility(VISIBLE);
    }

    /**
     * Set the title drawable.
     *
     * @param resId the drawable resource id
     */
    public void setTitleTextDrawable(int resId) {
        Drawable drawable = getResources().getDrawable(resId);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        titleTextView.setCompoundDrawables(drawable, null, null, null);
        titleTextView.setVisibility(VISIBLE);
    }

    /**
     * Set the right text drawable.
     *
     * @param resId the drawable resource id
     */
    public void setRightTextDrawable(int resId) {
        Drawable drawable = getResources().getDrawable(resId);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        rightTextView.setCompoundDrawables(null, null, drawable, null);
        rightTextView.setVisibility(VISIBLE);
    }

    /**
     * Enable or disable the right text view.
     *
     * @param enable whether it is enabled
     */
    public void setRightTextViewEnable(boolean enable) {
        if (rightTextView != null) {
            rightTextColorDefault =
                    rightTextColorDefault != -1
                            ? rightTextColorDefault
                            : rightTextView.getResources().getColor(com.jet.im.kit.R.color.primary_300);
            rightTextColorDisable =
                    rightTextColorDisable != -1
                            ? rightTextColorDisable
                            : rightTextView.getResources().getColor(com.jet.im.kit.R.color.primary_400);
            rightTextView.setTextColor(enable ? rightTextColorDefault : rightTextColorDisable);
            rightTextView.setEnabled(enable);
            rightTextView.setClickable(enable);
        }
    }

    /**
     * Add a view to the right side.
     *
     * @param view the view
     */
    public void addRightView(View view) {
        if (rightContainer != null) {
            rightContainer.addView(view);
        }
    }

    /**
     * GetLeft
     *
     * @return Left
     */
    public TextView getLeftTextView() {
        return leftTextView;
    }

    /**
     * GetTitle
     *
     * @return Title
     */
    public TextView getTitleTextView() {
        return titleTextView;
    }

    /**
     * GetRight
     *
     * @return Right
     */
    public TextView getRightTextView() {
        return rightTextView;
    }

    /**
     * SetLeft
     *
     * @param listener
     */
    public void setLeftClickListener(View.OnClickListener listener) {
        this.onLeftClickListener = listener;
    }

    /**
     * SetTitle
     *
     * @param listener
     */
    public void setTitleClickListener(View.OnClickListener listener) {
        this.onTitleClickListener = listener;
    }

    /**
     * SetRight
     *
     * @param listener
     */
    public void setRightClickListener(View.OnClickListener listener) {
        this.onRightClickListener = listener;
    }
}
