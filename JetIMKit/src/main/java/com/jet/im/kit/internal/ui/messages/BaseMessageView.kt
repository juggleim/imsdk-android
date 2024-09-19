package com.jet.im.kit.internal.ui.messages

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.viewbinding.ViewBinding
import com.jet.im.kit.model.MessageUIConfig

internal abstract class BaseMessageView @JvmOverloads internal constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {
    abstract val layout: View
    abstract val binding: ViewBinding
    var messageUIConfig: MessageUIConfig? = null
}
