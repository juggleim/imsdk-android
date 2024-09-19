package com.jet.im.kit.internal.ui.widgets

import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import kotlin.math.min

internal class VoiceProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    var cornerRadius: Float = 200F
    var progress: Int = 0
    var max: Int = 100 * 10
    var trackColor: ColorStateList? = null
        set(value) {
            field = value
            value?.let {
                trackPaint.color = it.getColorForState(drawableState, 0)
            } ?: run {
                trackPaint.color = 0
            }
            invalidate()
        }
    var progressColor: ColorStateList? = null
        set(value) {
            field = value
            value?.let {
                progressPaint.color = it.getColorForState(drawableState, 0)
            } ?: run {
                progressPaint.color = 0
            }
            invalidate()
        }
    var animationDuration: Long = 100
    private val trackPaint: Paint = Paint()
    private var trackRectF: RectF = RectF()
    private val trackRectPath = Path()
    private val progressPaint: Paint = Paint()
    private var progressRectF: RectF = RectF()
    private var animator: ValueAnimator? = null

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        trackRectF.set(0F, 0F, w.toFloat(), h.toFloat())
        trackRectPath.reset()
        trackRectPath.addRoundRect(trackRectF, cornerRadius, cornerRadius, Path.Direction.CW)
        progressRectF.set(0F, 0F, progress * w.toFloat() / max, h.toFloat())
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.apply {
            clipPath(trackRectPath)
            drawRect(trackRectF, trackPaint)
            drawRect(progressRectF, progressPaint)
        }
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        progressColor?.let {
            val progressColor: Int = it.getColorForState(drawableState, 0)
            progressPaint.color = progressColor
        }
        trackColor?.let {
            val trackColor: Int = it.getColorForState(drawableState, 0)
            trackPaint.color = trackColor
        }
    }

    fun drawProgressWithAnimation(progress: Int) {
        cancelAnimator()
        val valuesHolder = PropertyValuesHolder.ofInt(
            "percentage",
            this.progress,
            progress
        )
        animator = ValueAnimator().apply {
            setValues(valuesHolder)
            duration = animationDuration
            interpolator = LinearInterpolator()
            addUpdateListener {
                val percentage = it.getAnimatedValue("percentage") as Int
                this@VoiceProgressView.progress = percentage
                progressRectF.set(0F, 0F, calculateProgressWidth(), height.toFloat())
                postInvalidate()
            }
        }
        animator?.start()
    }

    fun drawProgress(progress: Int) {
        cancelAnimator()
        this.progress = progress
        progressRectF.set(0F, 0F, calculateProgressWidth(), height.toFloat())
        postInvalidate()
    }

    private fun cancelAnimator() {
        animator?.cancel()
        animator = null
    }

    private fun calculateProgressWidth(): Float {
        return min(progress, max) * width.toFloat() / max
    }
}
