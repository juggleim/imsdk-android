package com.jet.im.kit.internal.ui.widgets

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.target.CustomViewTarget
import com.bumptech.glide.request.transition.Transition
import com.jet.im.kit.internal.extensions.intToDp
import com.jet.im.kit.internal.interfaces.ViewRoundable
import com.jet.im.kit.internal.model.template_messages.SizeType
import com.jet.im.kit.internal.model.template_messages.ViewParams
import com.jet.im.kit.log.Logger
import com.jet.im.kit.utils.MetricsUtils

internal open class MessageTemplateImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr), ViewRoundable {
    private lateinit var rectF: RectF
    private val path: Path = Path()
    private var strokePaint: Paint? = null
    override var radius: Float = 0F
    private var imageRatio: Float = 0F
    private var targetWidth: Int = 0
    private var targetHeight: Int = 0
    var viewParams: ViewParams? = null

    fun setSize(width: Int, height: Int) {
        if (width > 0 && height > 0) {
            this.targetWidth = width
            this.targetHeight = height
            this.imageRatio = width.toFloat() / height.toFloat()
            requestLayout()
        }
    }

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

        rectF = RectF(0f, 0f, w.toFloat(), h.toFloat())
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

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val width = MeasureSpec.getSize(widthMeasureSpec)
        if (width == 0) {
            return
        }

        // auto image resolution applies only the height value is flexible
        viewParams?.let {
            if (it.height.type == SizeType.Fixed) return
            if (it.height.type == SizeType.Flex && it.height.value == ViewGroup.LayoutParams.MATCH_PARENT) return
        } ?: return

        // if imageRatio doesn't know it should skip
        if (imageRatio == 0f) {
            return
        }

        val layoutParams = layoutParams as ViewGroup.MarginLayoutParams
        val newHeight = (width / imageRatio).toInt()
        layoutParams.height = newHeight
        setMeasuredDimension(width, newHeight)
    }

    fun load(url: String) {
        var glide = Glide.with(context).load(url)
        if (targetWidth > 0 && targetHeight > 0) {
            val deviceWidth = MetricsUtils.getDeviceWidth(context)
            if (targetWidth > deviceWidth) {
                val height = (deviceWidth / imageRatio).toInt()
                glide = glide.override(deviceWidth, height)
                Logger.i("++ override width=$deviceWidth, height=$height, url=$url")
            }
        }

        if (imageRatio > 0) {
            // if the ratio exist no need to get the original image size to resize image.
            glide.into(this)
        } else {
            // if the image ratio not exist it need to find the size of image to make imageview size.
            // After obtaining the proportion of the image, onMeasure is used to determine the size of the view to match the proportion.
            glide.into(object : CustomViewTarget<ImageView, Drawable>(this) {
                override fun onLoadFailed(errorDrawable: Drawable?) {}
                override fun onResourceCleared(placeholder: Drawable?) {}

                override fun onResourceReady(
                    resource: Drawable,
                    transition: Transition<in Drawable>?
                ) {
                    val width = when (resource) {
                        is BitmapDrawable -> resource.bitmap.width
                        is GifDrawable -> resource.intrinsicWidth
                        else -> 0
                    }

                    val height = when (resource) {
                        is BitmapDrawable -> resource.bitmap.height
                        is GifDrawable -> resource.intrinsicHeight
                        else -> 0
                    }

                    Logger.i("++ width=$width, height=$height, url=$url")
                    setSize(width, height)
                    setImageDrawable(resource)
                }
            })
        }
    }
}
