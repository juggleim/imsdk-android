package com.juggle.chat.common.widgets.refresh.wrapper;

import android.content.Context;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.juggle.chat.R;
import com.juggle.chat.common.widgets.refresh.api.RefreshFooter;
import com.juggle.chat.common.widgets.refresh.api.RefreshHeader;
import com.juggle.chat.common.widgets.refresh.api.RefreshLayout;
import com.juggle.chat.common.widgets.refresh.simple.SimpleComponent;


public class RongRefreshHeader extends SimpleComponent implements RefreshHeader, RefreshFooter {

    protected ImageView mProgressView;

    public RongRefreshHeader(Context context) {
        this(context, null);
    }

    public RongRefreshHeader(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RongRefreshHeader(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View.inflate(context, R.layout.rc_refresh_header, this);
        mProgressView = findViewById(R.id.rc_refresh_progress);
        final Drawable drawable = mProgressView.getDrawable();
        if (drawable instanceof Animatable) {
            if (((Animatable) drawable).isRunning()) {
                ((Animatable) drawable).stop();
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        final Drawable drawable = mProgressView.getDrawable();
        if (drawable instanceof Animatable) {
            if (((Animatable) drawable).isRunning()) {
                ((Animatable) drawable).stop();
            }
        }
    }

    @Override
    public int onFinish(@NonNull RefreshLayout refreshLayout, boolean success) {
        Drawable drawable = mProgressView.getDrawable();
        if (drawable instanceof Animatable) {
            ((Animatable) drawable).stop();
        }
        return super.onFinish(refreshLayout, success);
    }

    @Override
    public void onStartAnimator(
            @NonNull RefreshLayout refreshLayout, int height, int maxDragHeight) {
        Drawable drawable = mProgressView.getDrawable();
        if (drawable instanceof Animatable) {
            ((Animatable) drawable).start();
        }
    }
}
