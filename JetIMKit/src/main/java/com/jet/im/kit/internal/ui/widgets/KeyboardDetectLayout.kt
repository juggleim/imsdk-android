package com.jet.im.kit.internal.ui.widgets

import android.content.Context
import android.content.res.Configuration
import android.graphics.Rect
import android.util.AttributeSet
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import com.jet.im.kit.log.Logger
import java.util.concurrent.atomic.AtomicBoolean

internal open class KeyboardDetectLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle), ViewTreeObserver.OnGlobalLayoutListener {

    private var screenOrientation: Int
    private val orientationChanged: AtomicBoolean = AtomicBoolean(false)
    private val keyboardShowing: AtomicBoolean = AtomicBoolean(false)
    var listener: OnKeyboardDetectListener? = null

    init {
        screenOrientation = resources.configuration.orientation
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        orientationChanged.set(screenOrientation != newConfig.orientation)
        screenOrientation = newConfig.orientation
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        viewTreeObserver.addOnGlobalLayoutListener(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        viewTreeObserver.removeOnGlobalLayoutListener(this)
    }

    override fun onGlobalLayout() {
        Logger.d("++ layoutChanged orientation changed=${orientationChanged.get()}")
        if (orientationChanged.getAndSet(false)) return
        val rect = Rect()
        getWindowVisibleDisplayFrame(rect)
        val screenHeight = rootView.height
        val keyboardHeight = screenHeight - rect.bottom

        listener?.let {
            if (keyboardHeight > screenHeight * 0.15) {
                if (!keyboardShowing.getAndSet(true)) {
                    it.onKeyboardShown()
                }
            } else {
                if (keyboardShowing.getAndSet(false)) {
                    it.onKeyboardHidden()
                }
            }
        }
    }
}

internal interface OnKeyboardDetectListener {
    fun onKeyboardShown()
    fun onKeyboardHidden()
}
