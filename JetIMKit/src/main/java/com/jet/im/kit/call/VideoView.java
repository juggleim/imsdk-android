package com.jet.im.kit.call;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;

import io.livekit.android.renderer.SurfaceViewRenderer;

public class VideoView extends SurfaceViewRenderer {
    public VideoView(@NonNull Context context) {
        super(context);
    }

    public VideoView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }
}
