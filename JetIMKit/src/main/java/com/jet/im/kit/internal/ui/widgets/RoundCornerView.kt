package com.jet.im.kit.internal.ui.widgets

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import com.jet.im.kit.R

internal class RoundCornerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {
    private val roundingPaint = Paint()
    private val canvasBounds = RectF()
    private val tempCanvas = Canvas()
    private var tempBitmap: Bitmap? = null
    private val child: AppCompatImageView
    var radius: Float
    var cornerRadii: FloatArray? = null
    val content: ImageView
        get() = child

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        child.layout(0, 0, measuredWidth, measuredHeight)
    }

    override fun dispatchDraw(canvas: Canvas) {
        val width = width
        val height = height
        if (isInEditMode) {
            val debugPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            debugPaint.color = ContextCompat.getColor(context, R.color.background_400)
            canvas.drawRoundRect(RectF(0f, 0f, width.toFloat(), height.toFloat()), 0f, 0f, debugPaint)
            return
        }
        if (width <= 0 || height <= 0) {
            super.dispatchDraw(canvas)
            return
        }
        tempBitmap?.run {
            if (isRecycled || width != getWidth() || height != getHeight()) {
                tempBitmap?.recycle()
            } else {
                eraseColor(Color.TRANSPARENT)
            }
            tempBitmap = try {
                Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888)
            } catch (e: OutOfMemoryError) {
                Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.RGB_565)
            }
        } ?: run {
            tempBitmap = try {
                Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            } catch (e: OutOfMemoryError) {
                Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            }
        }
        tempCanvas.setBitmap(tempBitmap)
        super.dispatchDraw(tempCanvas) // draw children
        roundingPaint.shader = BitmapShader(tempBitmap!!, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        canvasBounds[0f, 0f, width.toFloat()] = height.toFloat()
        cornerRadii?.let {
            val path = Path()
            path.addRoundRect(canvasBounds, it, Path.Direction.CW)
            canvas.drawPath(path, roundingPaint)
        } ?: run {
            canvas.drawRoundRect(canvasBounds, radius, radius, roundingPaint)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val specWidth = MeasureSpec.getSize(widthMeasureSpec)
        val specHeight = MeasureSpec.getSize(heightMeasureSpec)
        child.measure(
            MeasureSpec.makeMeasureSpec(specWidth, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(specHeight, MeasureSpec.EXACTLY)
        )
        setMeasuredDimension(child.measuredWidth, child.measuredHeight)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        tempBitmap?.recycle()
    }

    init {
        roundingPaint.isAntiAlias = true
        roundingPaint.isFilterBitmap = true
        radius = getContext().resources.getDimension(R.dimen.sb_size_16)
        child = AppCompatImageView(getContext()).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            minimumWidth = getContext().resources.getDimension(R.dimen.sb_size_100).toInt()
            minimumHeight = getContext().resources.getDimension(R.dimen.sb_size_100).toInt()
            maxWidth = getContext().resources.getDimension(R.dimen.sb_message_max_width).toInt()
            maxHeight = getContext().resources.getDimension(R.dimen.sb_message_max_height).toInt()
        }
        addView(child)
    }
}
