package com.jet.im.kit.internal.model.template_messages

import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import com.jet.im.kit.internal.extensions.intToDp
import com.jet.im.kit.internal.extensions.loadToBackground
import com.jet.im.kit.internal.extensions.setTypeface
import com.jet.im.kit.internal.interfaces.ViewRoundable
import com.jet.im.kit.internal.model.serializer.ColorIntAsStringSerializer
import kotlinx.serialization.Serializable

@Serializable
internal data class TextStyle(
    val size: Int? = null,
    @ColorInt
    @Serializable(with = ColorIntAsStringSerializer::class)
    val color: Int? = null,
    val weight: Weight? = null
) {
    fun apply(view: TextView): TextStyle {
        size?.let { view.setTextSize(TypedValue.COMPLEX_UNIT_SP, it.toFloat()) }
        color?.let { view.setTextColor(it) }
        weight?.let { view.setTypeface(it.value) }
        return this
    }
}

@Serializable
internal data class ImageStyle(
    val contentMode: ContentMode? = null,
    @Serializable(with = ColorIntAsStringSerializer::class)
    val tintColor: Int? = null,
) {
    fun apply(view: ImageView): ImageStyle {
        contentMode?.let { view.scaleType = it.scaleType }
        tintColor?.let { view.setColorFilter(it, PorterDuff.Mode.SRC_ATOP) }
        return this
    }
}

@Serializable
internal data class ViewStyle(
    @ColorInt
    @Serializable(with = ColorIntAsStringSerializer::class)
    val backgroundColor: Int? = null,
    val backgroundImageUrl: String? = null,
    val borderWidth: Int? = null,
    @ColorInt
    @Serializable(with = ColorIntAsStringSerializer::class)
    val borderColor: Int? = null,
    val radius: Int? = null,
    val margin: Margin? = null,
    val padding: Padding? = null
) {
    fun apply(view: View, useRipple: Boolean = false): ViewStyle {
        val resources = view.context.resources
        backgroundImageUrl?.let { view.loadToBackground(it, radius ?: 0, useRipple) }
        if (backgroundColor != null || (borderWidth != null && borderWidth > 0)) {
            view.background = GradientDrawable().apply {
                backgroundColor?.let { setColor(it) }
                cornerRadius = resources.intToDp(radius ?: 0).toFloat()
            }
        }

        margin?.apply(view)
        padding?.apply(view)

        if (view is ViewRoundable) {
            radius?.let { view.setRadiusIntSize(it) }
            borderWidth?.let { view.setBorder(borderWidth, borderColor ?: Color.TRANSPARENT) }
        }
        return this
    }
}

@Serializable
internal data class Margin constructor(
    val top: Int = 0,
    val bottom: Int = 0,
    val left: Int = 0,
    val right: Int = 0
) {
    fun apply(view: View) {
        val resources = view.context.resources
        val layoutParams = view.layoutParams as LinearLayout.LayoutParams
        layoutParams.setMargins(
            resources.intToDp(left),
            resources.intToDp(top),
            resources.intToDp(right),
            resources.intToDp(bottom)
        )
        view.layoutParams = layoutParams
    }
}

@Serializable
internal data class Padding constructor(
    val top: Int = 0,
    val bottom: Int = 0,
    val left: Int = 0,
    val right: Int = 0
) {
    fun apply(view: View) {
        val resources = view.context.resources
        view.setPadding(
            resources.intToDp(left),
            resources.intToDp(top),
            resources.intToDp(right),
            resources.intToDp(bottom)
        )
    }
}
