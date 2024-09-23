package com.juggle.chat.common.widgets;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.Toolbar;

import com.juggle.chat.R;


/** 头部Toolbar封装 Created by lhz on 18/3/22. */
public class TitleBar extends Toolbar {

    private TextView mLeft;
    private TextView mMiddle;
    private TextView mTying;
    private TextView mRight;
    private OnBackClickListener mOnBackClickListener;
    private OnRightIconClickListener mOnRightIconClickListener;
    private Context mContext;
    private Drawable drawable;
    private ImageView search;

    public TitleBar(Context context) {
        super(context);
        mContext = context;
        init(null);
    }

    public TitleBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init(attrs);
    }

    public TitleBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init(attrs);
    }

    public void setOnBackClickListener(OnBackClickListener l) {
        this.mOnBackClickListener = l;
    }

    public void setOnRightIconClickListener(OnRightIconClickListener l) {
        this.mOnRightIconClickListener = l;
    }

    private void init(AttributeSet attrs) {
        setContentInsetsRelative(0, 0);
        TypedArray typedArray = mContext.obtainStyledAttributes(attrs, R.styleable.TitleBar);
        String title = typedArray.getString(R.styleable.TitleBar_bar_title);
        String rightText = typedArray.getString(R.styleable.TitleBar_right_text);
        int rightColor = typedArray.getColor(R.styleable.TitleBar_right_text_color, Color.BLACK);
        int leftColor = typedArray.getColor(R.styleable.TitleBar_left_text_color, Color.BLACK);
        String leftText = typedArray.getString(R.styleable.TitleBar_left_text);
        boolean isBackIconShow = typedArray.getBoolean(R.styleable.TitleBar_show_back_icon, true);
        boolean isShowMiddle = typedArray.getBoolean(R.styleable.TitleBar_show_middle, true);
        int rightIconResourceId = typedArray.getResourceId(R.styleable.TitleBar_right_icon, -1);
        typedArray.recycle();
        addView(LayoutInflater.from(mContext).inflate(R.layout.rc_title_bar, this, false));
        if (rightIconResourceId > 0) {
            drawable = getResources().getDrawable(rightIconResourceId);
            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
            TextView rightView = getRightView();
            rightView.setVisibility(VISIBLE);
            rightView.setCompoundDrawables(null, null, drawable, null);
        }
        if (null != title) {
            setTitle(title);
        }
        if (null != rightText) {
            setRightText(rightText);
        }
        setRightTextColor(rightColor);
        ((TextView) getLeftView()).setTextColor(leftColor);

        if (!TextUtils.isEmpty(leftText)) {
            setLeftText(leftText);
        }
        if (!isBackIconShow) {
            dismissBackIcon();
        }
        if (!isShowMiddle) {
            dismissMiddle();
        }
        getLeftView()
                .setOnClickListener(
                        new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (mOnBackClickListener != null) {
                                    mOnBackClickListener.onBackClick();
                                    return;
                                }
                                if (mContext instanceof Activity) {
                                    ((Activity) mContext).onBackPressed();
                                }
                            }
                        });
        getRightView()
                .setOnClickListener(
                        new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (mOnRightIconClickListener != null) {
                                    mOnRightIconClickListener.onRightIconClick(v);
                                }
                            }
                        });
    }

    private void dismissMiddle() {
        getMiddleView().setVisibility(GONE);
    }

    public void setRightTextColor(int color) {
        getRightView().setTextColor(color);
    }

    public TextView getLeftView() {
        if (mLeft == null) {
            mLeft = findViewById(R.id.tool_bar_left);
        }
        return mLeft;
    }

    public void setRightVisible(boolean visible) {
        if (visible) {
            getRightView().setVisibility(VISIBLE);
        } else {
            getRightView().setVisibility(GONE);
        }
    }

    public TextView getMiddleView() {
        if (mMiddle == null) {
            mMiddle = findViewById(R.id.tool_bar_middle);
        }
        return mMiddle;
    }

    public TextView getTypingView() {
        if (mTying == null) {
            mTying = findViewById(R.id.tool_bar_middle_typing);
        }
        return mTying;
    }

    public void setTyping(@StringRes int typing) {
        getTypingView().setText(typing);
    }

    public TextView getRightView() {
        if (mRight == null) {
            mRight = findViewById(R.id.tool_bar_right);
        }
        return mRight;
    }

    public void setRightText(CharSequence charSequence) {
        TextView rightView = getRightView();
        if (rightView.getVisibility() != VISIBLE) {
            rightView.setVisibility(VISIBLE);
        }
        rightView.setText(charSequence);
    }

    public void setLeftText(CharSequence charSequence) {
        TextView leftText = getLeftView();
        leftText.setText(charSequence);
    }

    public void setTitle(String title) {
        super.setTitle(title);
        getMiddleView().setText(title);
    }

    @Override
    public void setTitle(@StringRes int resId) {
        super.setTitle(resId);
        getMiddleView().setText(resId);
    }

    public void dismissBackIcon() {
        getLeftView().setVisibility(View.GONE);
    }

    public void setRightIconDrawableVisibility(boolean visible) {
        if (visible) {
            if (drawable == null) {
                return;
            }
            getRightView().setCompoundDrawables(null, null, drawable, null);
        } else {
            getRightView().setCompoundDrawables(null, null, null, null);
        }
    }

    public ImageView getSearchView() {
        if (search == null) {
            search = findViewById(R.id.rc_search);
        }
        return search;
    }

    public void setSearchViewVisibility(boolean visibility) {
        getSearchView().setVisibility(visibility ? View.VISIBLE : View.GONE);
    }

    public interface OnBackClickListener {
        void onBackClick();
    }

    public interface OnRightIconClickListener {
        void onRightIconClick(View v);
    }
}
