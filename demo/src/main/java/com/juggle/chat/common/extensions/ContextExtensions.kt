package com.juggle.chat.common.extensions

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.jet.im.kit.R

internal fun Context.getDrawable(resId: Int, colorRes: Int): Drawable? {
    return if (colorRes == 0) {
        AppCompatResources.getDrawable(this, resId)
    } else setTintList(
        AppCompatResources.getDrawable(this, resId) ?: return null,
        AppCompatResources.getColorStateList(this, colorRes)
    )
}

private fun setTintList(drawable: Drawable, colorStateList: ColorStateList?): Drawable {
    return DrawableCompat.wrap(drawable).also {
        DrawableCompat.setTintList(it, colorStateList)
        it.mutate()
    }
}

internal fun Context.getColorResource(@ColorRes colorResId: Int): Int {
    return ContextCompat.getColor(this, colorResId)
}

internal fun Context.createOvalIcon(
    @ColorRes backgroundColor: Int,
    @DrawableRes iconRes: Int,
    @ColorRes iconTint: Int
): Drawable {
    return createOvalIcon(this, backgroundColor, 255, iconRes, iconTint)
}

private fun createOvalIcon(
    context: Context, @ColorRes backgroundColor: Int, backgroundAlpha: Int,
    @DrawableRes iconRes: Int, @ColorRes iconTint: Int
): Drawable {
    val ovalBackground = ShapeDrawable(OvalShape())
    ovalBackground.paint.color = context.getColorResource(backgroundColor)
    ovalBackground.paint.alpha = backgroundAlpha
    val icon = context.getDrawable(iconRes, iconTint)
    val inset = context.resources.getDimension(R.dimen.sb_size_24).toInt()
    return createLayerIcon(ovalBackground, icon, inset)
}

private fun createLayerIcon(background: Drawable, icon: Drawable?, inset: Int): Drawable {
    val layer = arrayOf(background, icon)
    return LayerDrawable(layer).apply {
        setLayerInset(1, inset, inset, inset, inset)
    }
}
