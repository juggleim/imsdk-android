package com.jet.im.kit.internal.model

import android.graphics.Paint
import android.graphics.Typeface
import android.text.TextPaint
import android.text.style.TypefaceSpan

internal class TypefaceSpanEx constructor(family: String, val tf: Typeface) : TypefaceSpan(family) {
    override fun updateDrawState(ds: TextPaint) {
        applyCustomTypeFace(ds, tf)
    }

    override fun updateMeasureState(paint: TextPaint) {
        applyCustomTypeFace(paint, tf)
    }

    private fun applyCustomTypeFace(paint: Paint, typeface: Typeface) {
        val oldStyle: Int
        val old = paint.typeface
        oldStyle = old?.style ?: 0
        val fake = oldStyle and typeface.style.inv()
        if (fake and Typeface.BOLD != 0) {
            paint.isFakeBoldText = true
        }
        if (fake and Typeface.ITALIC != 0) {
            paint.textSkewX = -0.25f
        }
        paint.typeface = typeface
    }
}
