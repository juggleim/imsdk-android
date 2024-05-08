package com.jet.im.kit.internal.ui.messages

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.content.res.AppCompatResources
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.sendbird.android.message.OGMetaData
import com.jet.im.kit.R
import com.jet.im.kit.SendbirdUIKit
import com.jet.im.kit.databinding.SbViewOpenChannelOgtagBinding
import com.jet.im.kit.internal.extensions.setAppearance
import com.jet.im.kit.utils.DrawableUtils
import com.jet.im.kit.utils.ImageUtils
import com.jet.im.kit.utils.TextUtils

internal class OpenChannelOgtagView private constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int = R.attr.sb_widget_ogtag,
    parent: ViewGroup?
) : FrameLayout(context, attrs, defStyleAttr) {
    private val binding: SbViewOpenChannelOgtagBinding

    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = R.attr.sb_widget_ogtag
    ) : this(
        context,
        attrs,
        defStyleAttr,
        null
    )

    fun drawOgtag(metaData: OGMetaData?) {
        visibility = if (metaData != null) VISIBLE else GONE
        if (metaData == null) {
            return
        }

        if (metaData.ogImage != null && (metaData.ogImage?.secureUrl != null || metaData.ogImage?.url != null)) {
            binding.ivOgImage.visibility = VISIBLE
            val ogImageUrl: String? = if (metaData.ogImage?.secureUrl != null) {
                metaData.ogImage?.secureUrl
            } else {
                metaData.ogImage?.url
            }
            val thumbnailIconTint = if (SendbirdUIKit.isDarkMode()) R.color.ondark_02 else R.color.onlight_02
            val builder = Glide.with(context)
                .asDrawable()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(
                    DrawableUtils.setTintList(
                        ImageUtils.resize(
                            context.resources,
                            AppCompatResources.getDrawable(context, R.drawable.icon_photo),
                            R.dimen.sb_size_48,
                            R.dimen.sb_size_48
                        ),
                        AppCompatResources.getColorStateList(context, thumbnailIconTint)
                    )
                )
                .error(
                    DrawableUtils.setTintList(
                        ImageUtils.resize(
                            context.resources,
                            AppCompatResources.getDrawable(context, R.drawable.icon_thumbnail_none),
                            R.dimen.sb_size_48,
                            R.dimen.sb_size_48
                        ),
                        AppCompatResources.getColorStateList(context, thumbnailIconTint)
                    )
                )
            binding.ivOgImage.radius = resources.getDimensionPixelSize(R.dimen.sb_size_8).toFloat()
            binding.ivOgImage.content.scaleType = ImageView.ScaleType.CENTER
            builder.load(ogImageUrl).centerCrop().listener(object : RequestListener<Drawable?> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any,
                    target: Target<Drawable?>,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any,
                    target: Target<Drawable?>,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.ivOgImage.content.scaleType = ImageView.ScaleType.CENTER_CROP
                    return false
                }
            }).into(binding.ivOgImage.content)
        } else {
            binding.ivOgImage.visibility = GONE
        }

        if (TextUtils.isNotEmpty(metaData.title)) {
            binding.tvOgTitle.visibility = VISIBLE
            binding.tvOgTitle.text = metaData.title
        } else {
            binding.tvOgTitle.visibility = GONE
        }
        if (TextUtils.isNotEmpty(metaData.description)) {
            binding.tvOgDescription.visibility = VISIBLE
            binding.tvOgDescription.text = metaData.description
        } else {
            binding.tvOgDescription.visibility = GONE
        }
        if (TextUtils.isNotEmpty(metaData.url)) {
            binding.tvOgUrl.visibility = VISIBLE
            binding.tvOgUrl.text = metaData.url
        } else {
            binding.tvOgUrl.visibility = GONE
        }
    }

    companion object {
        // TODO (Remove : after all codes are converted as kotlin this annotation doesn't need)
        @JvmStatic
        fun inflate(context: Context, parent: ViewGroup?): OpenChannelOgtagView {
            return OpenChannelOgtagView(context, null, R.attr.sb_widget_ogtag, parent)
        }
    }

    init {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.MessageView, defStyleAttr, 0)
        try {
            binding = SbViewOpenChannelOgtagBinding.inflate(LayoutInflater.from(getContext()), parent ?: this, true)
            val ogtagTitleAppearence = a.getResourceId(
                R.styleable.MessageView_sb_message_ogtag_title_appearance,
                R.style.SendbirdBody3OnLight01
            )
            val ogtagDescAppearence = a.getResourceId(
                R.styleable.MessageView_sb_message_ogtag_description_appearance,
                R.style.SendbirdCaption2OnLight01
            )
            val ogtagUrlAppearence = a.getResourceId(
                R.styleable.MessageView_sb_message_ogtag_url_appearance,
                R.style.SendbirdCaption2OnLight02
            )
            binding.tvOgTitle.setAppearance(context, ogtagTitleAppearence)
            binding.tvOgDescription.setAppearance(context, ogtagDescAppearence)
            binding.tvOgUrl.setAppearance(context, ogtagUrlAppearence)
        } finally {
            a.recycle()
        }
    }
}
