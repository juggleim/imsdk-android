package com.jet.im.kit.internal.ui.widgets

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import com.jet.im.kit.internal.extensions.intToDp
import com.jet.im.kit.internal.interfaces.ViewRoundable

internal open class RoundCornerLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), ViewRoundable {
    private val rectF: RectF = RectF()
    private val path: Path = Path()
    private var strokePaint: Paint? = null
    override var radius: Float = 0F

    init {
        setBorder(0, Color.TRANSPARENT)
    }

    override fun setRadiusIntSize(radius: Int) {
        this.radius = context.resources.intToDp(radius).toFloat()
    }

    final override fun setBorder(borderWidth: Int, @ColorInt borderColor: Int) {
        if (borderWidth <= 0)
            strokePaint = null
        else {
            strokePaint = Paint().apply {
                style = Paint.Style.STROKE
                isAntiAlias = true
                strokeWidth = context.resources.intToDp(borderWidth).toFloat()
                color = borderColor
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // In the Android platform, even if a view is not drawn on the screen due to left and right views, its height value exists.
        // In the template message syntax, the views that are not drawn have to hide.
        // onSizeChanged() and onLayout() do not update the view even if the visibility changes, so the status of the view must be updated once again.
        // Logger.i("-- parent view's width=${(parent as View).width}, x=$x, measureWidth=$width, visible=$visibility")
        val visibility = if (x <= -width || x >= (parent as View).width) GONE else VISIBLE
        post {
            this.visibility = visibility
        }
        rectF.set(0f, 0f, w.toFloat(), h.toFloat())
        resetPath()
    }

    override fun draw(canvas: Canvas) {
        val save = canvas.save()
        canvas.clipPath(path)
        super.draw(canvas)
        strokePaint?.let {
            val inlineWidth = it.strokeWidth
            rectF.set(inlineWidth / 2, inlineWidth / 2, width - inlineWidth / 2, height - inlineWidth / 2)
            canvas.drawRoundRect(rectF, radius, radius, it)
        }
        canvas.restoreToCount(save)
    }

    private fun resetPath() {
        path.reset()
        path.addRoundRect(rectF, radius, radius, Path.Direction.CW)
        path.close()
    }
}
