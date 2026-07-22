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
 * Base class for components
 *
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
     * Create the view
     *
     * @param context the context
     * @param from the layout inflater
     * @param parent the parent view
     * @param attrs Bundle
     * @return The created View
     */
    protected abstract View onCreateView(
            Context context, LayoutInflater from, @NonNull ViewGroup parent, AttributeSet attrs);
}
