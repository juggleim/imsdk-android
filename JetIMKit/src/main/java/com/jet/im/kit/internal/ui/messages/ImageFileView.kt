package com.jet.im.kit.internal.ui.messages

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.sendbird.android.message.Thumbnail
import com.jet.im.kit.R
import com.jet.im.kit.databinding.SbViewImageFileComponentBinding
import com.jet.im.kit.internal.extensions.setBackgroundColorAndRadii
import com.jet.im.kit.utils.ViewUtils

internal class ImageFileView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {
    val binding: SbViewImageFileComponentBinding
    private var imageBackgroundColor: ColorStateList?
    var cornerRadii: FloatArray = floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
        set(value) {
            field = value
            binding.ivThumbnail.cornerRadii = value
            val overlayColor = ContextCompat.getColorStateList(context, R.color.sb_selector_thumbnail_overlay)
            binding.ivThumbnailOverlay.setBackgroundColorAndRadii(overlayColor, value)
            binding.ivThumbnail.setBackgroundColorAndRadii(imageBackgroundColor, value)
        }

    init {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.MessageView_MultipleFiles, defStyle, 0)
        try {
            binding = SbViewImageFileComponentBinding.inflate(LayoutInflater.from(getContext()), this, true)
            imageBackgroundColor =
                a.getColorStateList(R.styleable.MessageView_MultipleFiles_sb_multiple_files_message_item_background_color)
            binding.ivThumbnail.radius = 0f
        } finally {
            a.recycle()
        }
    }

    fun draw(
        url: String,
        plainUrl: String,
        fileType: String,
        cacheKey: String
    ) {
        binding.ivThumbnailOverlay.setOnClickListener { this.performClick() }
        binding.ivThumbnailOverlay.setOnLongClickListener { this.performLongClick() }
        ViewUtils.drawThumbnailIcon(binding.ivThumbnailIcon, fileType)
        ViewUtils.drawThumbnail(
            binding.ivThumbnail,
            cacheKey,
            url,
            plainUrl,
            fileType,
            null,
            R.dimen.sb_size_32
        )
    }
}
