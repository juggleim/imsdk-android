package com.jet.im.kit.internal.ui.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import com.jet.im.kit.R
import com.jet.im.kit.internal.extensions.addRipple
import com.jet.im.kit.internal.extensions.intToDp
import com.jet.im.kit.internal.extensions.setAppearance
import com.jet.im.kit.internal.model.template_messages.BoxViewParams
import com.jet.im.kit.internal.model.template_messages.ButtonViewParams
import com.jet.im.kit.internal.model.template_messages.ImageButtonViewParams
import com.jet.im.kit.internal.model.template_messages.ImageViewParams
import com.jet.im.kit.internal.model.template_messages.Orientation
import com.jet.im.kit.internal.model.template_messages.TextViewParams

@SuppressLint("ViewConstructor")
internal open class Text @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RoundCornerLayout(context, attrs, defStyleAttr) {
    private val textView: TextView

    init {
        layoutParams = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        )
        textView = AppCompatTextView(context).apply {
            ellipsize = TextUtils.TruncateAt.END
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
            )
        }
        this.addView(textView)
    }

    fun apply(params: TextViewParams, orientation: Orientation) {
        params.applyLayoutParams(context, layoutParams, orientation)
        params.viewStyle.apply(this)
        params.textStyle.apply(textView)

        textView.gravity = params.align.gravity
        params.maxTextLines?.let { textView.maxLines = it }
        textView.text = params.text
    }

    fun setTextAppearance(textAppearance: Int) {
        textView.setAppearance(context, textAppearance)
    }
}

@SuppressLint("ViewConstructor")
internal open class Image @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MessageTemplateImageView(context, attrs, defStyleAttr) {
    init {
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        scaleType = ScaleType.FIT_CENTER
    }

    fun apply(params: ImageViewParams, orientation: Orientation) {
        this.viewParams = params
        params.applyLayoutParams(context, layoutParams, orientation)
        params.metaData?.let {
            setSize(it.pixelWidth, it.pixelHeight)
        }
        params.imageStyle.apply(this)
        params.viewStyle.apply(this)
        load(params.imageUrl)
    }
}

@SuppressLint("ViewConstructor")
internal open class TextButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RoundCornerLayout(context, attrs, defStyleAttr) {
    private val textView: TextView

    init {
        // Even if action doesn't exist click ripple effect should show. (UIKit spec)
        layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT
        )
        isClickable = true
        gravity = Gravity.CENTER
        // default button padding.
        val padding = resources.intToDp(10)
        this.setPadding(padding, padding, padding, padding)
        this.setBackgroundResource(R.drawable.sb_shape_round_rect_background_200)
        setRadiusIntSize(6)
        addRipple(background)

        textView = AppCompatTextView(context).apply {
            ellipsize = TextUtils.TruncateAt.END
            maxLines = 1
            layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            )
        }
        this.addView(textView)
    }

    fun apply(params: ButtonViewParams, orientation: Orientation) {
        params.applyLayoutParams(context, layoutParams, orientation)
        params.textStyle.apply(textView)
        params.viewStyle.apply(this, true)
        textView.maxLines = params.maxTextLines
        textView.text = params.text
        addRipple(background)
    }

    fun setTextAppearance(textAppearance: Int) {
        textView.setAppearance(context, textAppearance)
    }
}

@SuppressLint("ViewConstructor")
internal open class ImageButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MessageTemplateImageView(context, attrs, defStyleAttr) {
    init {
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        scaleType = ScaleType.FIT_CENTER
        setRadiusIntSize(6)
        addRipple(background)
    }

    fun apply(params: ImageButtonViewParams, orientation: Orientation) {
        this.viewParams = params
        params.applyLayoutParams(context, layoutParams, orientation)
        params.metaData?.let {
            setSize(it.pixelWidth, it.pixelHeight)
        }
        params.imageStyle.apply(this)
        params.viewStyle.apply(this, true)
        load(params.imageUrl)
        addRipple(background)
    }
}

@SuppressLint("ViewConstructor")
internal open class Box @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RoundCornerLayout(context, attrs, defStyleAttr) {
    init {
        layoutParams = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        )
    }

    fun apply(params: BoxViewParams, orientation: Orientation) {
        this.orientation = params.orientation.value
        params.applyLayoutParams(context, layoutParams, orientation)
        gravity = params.align.gravity
        params.viewStyle.apply(this)
    }
}
