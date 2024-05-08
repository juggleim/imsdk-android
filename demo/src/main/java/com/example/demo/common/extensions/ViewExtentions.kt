package com.example.demo.common.extensions

import android.content.Context
import android.os.Build
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorRes

@Suppress("DEPRECATION")
internal fun TextView.setAppearance(context: Context, res: Int) {
    if (Build.VERSION.SDK_INT < 23) {
        setTextAppearance(context, res)
    } else {
        setTextAppearance(res)
    }
}

internal fun TextView.setTextColorResource(@ColorRes id: Int) {
    setTextColor(context.getColorResource(id))
}

internal fun View.toggleVisibility() {
    visibility = if (visibility == View.VISIBLE) View.GONE else View.VISIBLE
}
