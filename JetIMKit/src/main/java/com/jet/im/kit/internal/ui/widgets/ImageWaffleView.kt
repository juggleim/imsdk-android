package com.jet.im.kit.internal.ui.widgets

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import com.jet.im.kit.R
import com.jet.im.kit.SendbirdUIKit
import java.util.Collections
import java.util.LinkedList
import java.util.Queue

internal open class ImageWaffleView @JvmOverloads internal constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : ViewGroup(context, attrs) {

    companion object {
        private const val ROUND_BORDER = 1
        private const val DIVIDER_WIDTH = 1
    }

    private val roundingPaint = Paint()
    private val borderPaint = Paint()
    private val canvasBounds = RectF()
    private val tempCanvas = Canvas()
    private var tempBitmap: Bitmap? = null

    private class KillerWaffleChildImageView constructor(val imageWaffleView: ImageWaffleView) :
        AppCompatImageView(imageWaffleView.context) {
        override fun requestLayout() {
            // suppress layout for performance
            super.requestLayout()
            imageWaffleView.forceLayout()
        }

        override fun invalidate() {
            super.invalidate()
            imageWaffleView.invalidate()
        }

        init {
            scaleType = ScaleType.CENTER_CROP
        }
    }

    init {
        roundingPaint.isAntiAlias = true
        roundingPaint.isFilterBitmap = true
        borderPaint.isAntiAlias = true
    }

    open fun prepareSingleImageView(): ImageView {
        return prepareImageViews(1)[0]
    }

    open fun prepareImageViews(length: Int): List<ImageView> {
        require(!(length > 4 || length < 0)) { "Invalid length : $length" }
        val prevImageViews: Queue<ImageView> = LinkedList()
        if (childCount == length) {
            for (i in 0 until childCount) {
                prevImageViews.add(getChildAt(i) as ImageView)
            }
        } else {
            removeAllViews()
        }
        val prepared: MutableList<ImageView> = ArrayList(length)
        for (i in 0 until length) {
            prepared.add(pollOrNewImageView(prevImageViews))
        }
        var toBeRemoved: ImageView?
        while (prevImageViews.poll().also { toBeRemoved = it } != null) {
            removeView(toBeRemoved)
        }
        return Collections.unmodifiableList(prepared)
    }

    private fun pollOrNewImageView(prevImageViews: Queue<ImageView>): ImageView {
        val polled = prevImageViews.poll()
        if (polled != null) {
            return polled
        }
        val imageView: ImageView = KillerWaffleChildImageView(this)
        addView(imageView)
        return imageView
    }

    override fun dispatchDraw(canvas: Canvas) {
        val width = width
        val height = height
        if (isInEditMode) {
            val debugPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            debugPaint.color = ContextCompat.getColor(context, R.color.background_400)
            val ROUNDING_RADIUS = 1
            canvas.drawRoundRect(
                RectF(0F, 0F, width.toFloat(), height.toFloat()),
                ROUNDING_RADIUS.toFloat(),
                ROUNDING_RADIUS.toFloat(),
                debugPaint
            )
            return
        }
        if (width == 0 || height == 0) {
            super.dispatchDraw(canvas)
            return
        }

        tempBitmap?.let {
            if (it.isRecycled || it.width != width || it.height != height) {
                it.recycle()
                tempBitmap = try {
                    Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                } catch (e: OutOfMemoryError) {
                    Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
                }
            } else {
                it.eraseColor(Color.TRANSPARENT)
            }
        } ?: run {
            tempBitmap = try {
                Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            } catch (e: OutOfMemoryError) {
                Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            }
        }

        tempCanvas.setBitmap(tempBitmap)

        // draw below children
        // drawBackground(tempCanvas);

        // draw children
        super.dispatchDraw(tempCanvas)

        // draw above children
        // drawForeground(tempCanvas);

        // draw top-most layer
        // drawGlass(tempCanvas);
        tempBitmap?.let {
            roundingPaint.shader =
                BitmapShader(it, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        }

        val paddingLeft = paddingLeft
        val paddingRight = paddingRight
        val paddingTop = paddingTop
        val paddingBottom = paddingBottom
        canvasBounds.set(0f, 0f, width.toFloat(), height.toFloat())
        borderPaint.color =
            ContextCompat.getColor(context, if (SendbirdUIKit.isDarkMode()) R.color.ondark_04 else R.color.onlight_04)
        canvas.drawRoundRect(
            canvasBounds,
            (width / 2).toFloat(),
            (height / 2).toFloat(),
            borderPaint
        )
        canvasBounds.set(
            ROUND_BORDER + paddingLeft.toFloat(),
            ROUND_BORDER + paddingTop.toFloat(),
            (width - ROUND_BORDER - paddingRight).toFloat(),
            (height - ROUND_BORDER - paddingBottom).toFloat()
        )
        canvas.drawRoundRect(
            canvasBounds,
            (width / 2).toFloat(),
            (height / 2).toFloat(),
            roundingPaint
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val specWidth = MeasureSpec.getSize(widthMeasureSpec)
        val specHeight = MeasureSpec.getSize(heightMeasureSpec)
        val halfWidth = (specWidth - DIVIDER_WIDTH + ROUND_BORDER) / 2
        val halfHeight = (specHeight - DIVIDER_WIDTH + ROUND_BORDER) / 2
        when (childCount) {
            1 -> measureInGrid(getChildAt(0), specWidth, specHeight)
            2 -> {
                measureInGrid(getChildAt(0), halfWidth, specHeight)
                measureInGrid(getChildAt(1), halfWidth, specHeight)
            }
            3 -> {
                measureInGrid(getChildAt(0), specWidth, halfHeight)
                measureInGrid(getChildAt(1), halfWidth, halfHeight)
                measureInGrid(getChildAt(2), halfWidth, halfHeight)
            }
            4 -> {
                measureInGrid(getChildAt(0), halfWidth, halfHeight)
                measureInGrid(getChildAt(1), halfWidth, halfHeight)
                measureInGrid(getChildAt(2), halfWidth, halfHeight)
                measureInGrid(getChildAt(3), halfWidth, halfHeight)
            }
        }
        setMeasuredDimension(specWidth, specHeight)
    }

    private fun measureInGrid(view: View, width: Int, height: Int) {
        view.measure(
            MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        )
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val halfWidth = (measuredWidth - DIVIDER_WIDTH - ROUND_BORDER) / 2
        val halfHeight = (measuredHeight - DIVIDER_WIDTH - ROUND_BORDER) / 2
        when (childCount) {
            1 -> getChildAt(0).layout(0, 0, measuredWidth, measuredHeight)
            2 -> {
                getChildAt(0).layout(0, 0, halfWidth, measuredHeight)
                getChildAt(1).layout(measuredWidth - halfWidth, 0, measuredWidth, measuredHeight)
            }
            3 -> {
                getChildAt(0).layout(0, 0, measuredWidth, halfHeight)
                getChildAt(1).layout(0, measuredHeight - halfHeight, halfWidth, measuredHeight)
                getChildAt(2).layout(
                    measuredWidth - halfWidth, measuredHeight - halfHeight,
                    measuredWidth, measuredHeight
                )
            }
            4 -> {
                getChildAt(0).layout(0, 0, halfWidth, halfHeight)
                getChildAt(1).layout(measuredWidth - halfWidth, 0, measuredWidth, halfHeight)
                getChildAt(2).layout(0, measuredHeight - halfHeight, halfWidth, measuredHeight)
                getChildAt(3).layout(
                    measuredWidth - halfWidth, measuredHeight - halfHeight,
                    measuredWidth, measuredHeight
                )
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        tempBitmap?.recycle()
    }
}
