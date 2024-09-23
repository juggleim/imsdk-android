package com.jet.im.kit.internal.ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.widget.ProgressBar
import com.jet.im.kit.R
import com.jet.im.kit.SendbirdUIKit
import com.jet.im.kit.utils.DrawableUtils

internal class MessageProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ProgressBar(context, attrs) {
    init {
        val loadingTint = SendbirdUIKit.getDefaultThemeMode().primaryTintResId
        val loading = DrawableUtils.setTintList(context, R.drawable.sb_message_progress, loadingTint)
        this.indeterminateDrawable = loading
    }
}
