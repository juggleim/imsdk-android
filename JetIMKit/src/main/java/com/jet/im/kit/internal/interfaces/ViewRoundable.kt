package com.jet.im.kit.internal.interfaces

import android.graphics.Color
import androidx.annotation.ColorInt

internal interface ViewRoundable {
    var radius: Float
    fun setRadiusIntSize(radius: Int)
    fun setBorder(borderWidth: Int = 0, @ColorInt borderColor: Int = Color.TRANSPARENT)
}
