package com.juggle.chat.component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Component 的基类
 *
 * @author rongcloud
 * @since 5.10.4
 */
public abstract class BaseComponent extends FrameLayout {
    public BaseComponent(@NonNull Context context) {
        super(context);
    }

    public BaseComponent(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseComponent(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        addView(onCreateView(context, LayoutInflater.from(context), this, attrs));
    }

    /**
     * 创建视图
     *
     * @param context 上下文
     * @param from 布局加载器
     * @param parent 父布局
     * @param attrs Bundle
     * @return The created View
     */
    protected abstract View onCreateView(
            Context context, LayoutInflater from, @NonNull ViewGroup parent, AttributeSet attrs);
}
