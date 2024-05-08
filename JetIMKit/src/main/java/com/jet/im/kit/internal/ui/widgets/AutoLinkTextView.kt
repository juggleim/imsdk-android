package com.jet.im.kit.internal.ui.widgets

import android.content.Context
import android.graphics.RectF
import android.text.Selection
import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.text.style.BackgroundColorSpan
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.util.Linkify
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import com.jet.im.kit.log.Logger

internal class AutoLinkTextView @JvmOverloads internal constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : AppCompatTextView(context, attrs) {
    var onLinkClickListener: OnLinkClickListener? = null
    var onLinkLongClickListener: OnLinkLongClickListener? = null
    var clickedLinkBackgroundColor = 0
    var clickedLinkTextColor = 0
    var linkifyMask = Linkify.WEB_URLS or Linkify.EMAIL_ADDRESSES or Linkify.PHONE_NUMBERS

    override fun setText(text: CharSequence?, type: BufferType?) {
        super.setText(text, type)
        try {
            Linkify.addLinks(this, linkifyMask)
            movementMethod = SBLinkMovementMethod(
                onLinkClickListener = onLinkClickListener,
                onLinkLongClickListener = onLinkLongClickListener,
                clickedLinkTextColor = clickedLinkBackgroundColor,
                clickedLinkBackgroundColor = clickedLinkBackgroundColor
            )
        } catch (e: Exception) {
            Logger.e(e)
        }
    }
}

internal interface OnLinkClickListener {
    /**
     * @param textView The TextView on which a click was registered.
     * @param link     The clicked link.
     * @return True if this click was handled. False to let Android handle the URL.
     */
    fun onClick(textView: TextView, link: String): Boolean
}

internal interface OnLinkLongClickListener {
    /**
     * @param textView The TextView on which a long-click was registered.
     * @param link     The long-clicked link.
     * @return True if this long-click was handled. False to let Android handle the URL (as a short-click).
     */
    fun onLongClick(textView: TextView, link: String): Boolean
}

internal class SBLinkMovementMethod constructor(
    private val onLinkClickListener: OnLinkClickListener? = null,
    private val onLinkLongClickListener: OnLinkLongClickListener? = null,
    clickedLinkTextColor: Int = 0,
    clickedLinkBackgroundColor: Int = 0
) : LinkMovementMethod() {

    private var longPressTimer: LongPressTimer? = null
    private val backgroundColorSpan: BackgroundColorSpan?
    private val foregroundColorSpan: ForegroundColorSpan?

    private var activateTextViewHashcode = 0
    private val touchedLineBounded = RectF()
    private var longPressedRegistered = false
    private var prevLink: ClickableSpan? = null

    init {
        this.backgroundColorSpan =
            if (clickedLinkBackgroundColor != 0) BackgroundColorSpan(clickedLinkBackgroundColor) else null
        this.foregroundColorSpan = if (clickedLinkTextColor != 0) ForegroundColorSpan(clickedLinkTextColor) else null
    }

    private class LongPressTimer constructor(val onTimerReachedListener: OnTimerReachedListener) : Runnable {
        interface OnTimerReachedListener {
            fun onTimerReached()
        }

        override fun run() {
            onTimerReachedListener.onTimerReached()
        }
    }

    override fun onTouchEvent(widget: TextView, buffer: Spannable, event: MotionEvent): Boolean {
        if (activateTextViewHashcode != widget.hashCode()) {
            activateTextViewHashcode = widget.hashCode()
            widget.autoLinkMask = 0
        }
        var touchX = event.x.toInt()
        var touchY = event.y.toInt()
        touchX -= widget.totalPaddingLeft
        touchY -= widget.totalPaddingTop
        touchX += widget.scrollX
        touchY += widget.scrollY
        val layout = widget.layout
        val touchedLine = layout.getLineForVertical(touchY)
        val touchOffset = layout.getOffsetForHorizontal(touchedLine, touchX.toFloat())
        touchedLineBounded.left = layout.getLineLeft(touchedLine)
        touchedLineBounded.top = layout.getLineTop(touchedLine).toFloat()
        touchedLineBounded.right = layout.getLineWidth(touchedLine) + touchedLineBounded.left
        touchedLineBounded.bottom = layout.getLineBottom(touchedLine).toFloat()
        if (!touchedLineBounded.contains(touchX.toFloat(), touchY.toFloat())) {
            clearTouchEvent(widget, buffer)
            return false
        }
        val links = buffer.getSpans(touchOffset, touchOffset, ClickableSpan::class.java)
        if (links.isEmpty() || links[0] == null) {
            clearTouchEvent(widget, buffer)
            return false
        }
        val link = links[0]
        if (event.action == MotionEvent.ACTION_DOWN) {
            prevLink = link
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                Logger.d("ACTION_DOWN for link")
                drawClickedLink(link, buffer)
                widget.cancelLongPress()
                longPressTimer = LongPressTimer(object : LongPressTimer.OnTimerReachedListener {
                    override fun onTimerReached() {
                        longPressedRegistered = true
                        widget.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                        eraseClickedLink(buffer)

                        onLinkLongClickListener?.onLongClick(
                            widget,
                            buffer.substring(buffer.getSpanStart(link), buffer.getSpanEnd(link))
                        ) ?: link.onClick(widget)
                    }
                })
                widget.postDelayed(longPressTimer, ViewConfiguration.getLongPressTimeout().toLong())
                return true
            }
            MotionEvent.ACTION_UP -> {
                Logger.d("ACTION_UP for link")
                if (!longPressedRegistered && link === prevLink) {
                    onLinkClickListener?.onClick(
                        widget,
                        buffer.substring(buffer.getSpanStart(link), buffer.getSpanEnd(link))
                    ) ?: link.onClick(widget)
                }
                clearTouchEvent(widget, buffer)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (!longPressedRegistered) {
                    drawClickedLink(link, buffer)
                }
                if (link !== prevLink) {
                    clearTouchEvent(widget, buffer)
                }
                return true
            }
        }
        clearTouchEvent(widget, buffer)
        return false
    }

    private fun drawClickedLink(clickableSpan: ClickableSpan, text: Spannable) {
        val spanStart = text.getSpanStart(clickableSpan)
        val spanEnd = text.getSpanEnd(clickableSpan)
        if (backgroundColorSpan != null) {
            text.setSpan(backgroundColorSpan, spanStart, spanEnd, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        }
        if (foregroundColorSpan != null) {
            text.setSpan(foregroundColorSpan, spanStart, spanEnd, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        }
        Selection.setSelection(text, spanStart, spanEnd)
    }

    private fun eraseClickedLink(text: Spannable) {
        text.removeSpan(backgroundColorSpan)
        text.removeSpan(foregroundColorSpan)
        Selection.removeSelection(text)
    }

    private fun clearTouchEvent(widget: TextView, buffer: Spannable) {
        eraseClickedLink(buffer)
        longPressedRegistered = false
        widget.removeCallbacks(longPressTimer)
        longPressTimer = null
    }
}
