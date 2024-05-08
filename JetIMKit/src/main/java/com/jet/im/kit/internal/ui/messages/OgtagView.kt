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
import com.jet.im.kit.databinding.SbViewOgtagBinding
import com.jet.im.kit.internal.extensions.setAppearance
import com.jet.im.kit.utils.DrawableUtils
import com.jet.im.kit.utils.ImageUtils
import com.jet.im.kit.utils.TextUtils

internal class OgtagView private constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int = R.attr.sb_widget_ogtag,
    parent: ViewGroup?
) : FrameLayout(context, attrs, defStyleAttr) {
    private val binding: SbViewOgtagBinding

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

    fun drawOgtag(ogMetaData: OGMetaData?) {
        if (ogMetaData == null) {
            return
        }
        if (ogMetaData.ogImage != null && (ogMetaData.ogImage?.secureUrl != null || ogMetaData.ogImage?.url != null)) {
            binding.ivOgImage.visibility = VISIBLE
            val ogImageUrl: String? = if (ogMetaData.ogImage?.secureUrl != null) {
                ogMetaData.ogImage?.secureUrl
            } else {
                ogMetaData.ogImage?.url
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
            binding.ivOgImage.scaleType = ImageView.ScaleType.CENTER
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
                    binding.ivOgImage.scaleType = ImageView.ScaleType.CENTER_CROP
                    return false
                }
            }).into(binding.ivOgImage)
        } else {
            binding.ivOgImage.visibility = GONE
        }

        if (TextUtils.isNotEmpty(ogMetaData.title)) {
            binding.tvOgTitle.visibility = VISIBLE
            binding.tvOgTitle.text = ogMetaData.title
        } else {
            binding.tvOgTitle.visibility = GONE
        }
        if (TextUtils.isNotEmpty(ogMetaData.description)) {
            binding.tvOgDescription.visibility = VISIBLE
            binding.tvOgDescription.text = ogMetaData.description
        } else {
            binding.tvOgDescription.visibility = GONE
        }
        if (TextUtils.isNotEmpty(ogMetaData.url)) {
            binding.tvOgUrl.visibility = VISIBLE
            binding.tvOgUrl.text = ogMetaData.url
        } else {
            binding.tvOgUrl.visibility = GONE
        }
    }

    companion object {
        // TODO (Remove : after all codes are converted as kotlin this annotation doesn't need)
        @JvmStatic
        fun inflate(context: Context, parent: ViewGroup?): OgtagView {
            return OgtagView(context, null, R.attr.sb_widget_ogtag, parent)
        }
    }

    init {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.MessageView_User, defStyleAttr, 0)
        try {
            binding = SbViewOgtagBinding.inflate(LayoutInflater.from(getContext()), parent ?: this, true)
            val ogtagTitleAppearance = a.getResourceId(
                R.styleable.MessageView_User_sb_message_ogtag_title_appearance,
                R.style.SendbirdBody2OnLight01
            )
            val ogtagDescriptionAppearance = a.getResourceId(
                R.styleable.MessageView_User_sb_message_ogtag_description_appearance,
                R.style.SendbirdCaption2OnLight01
            )
            val ogtagUrlAppearance = a.getResourceId(
                R.styleable.MessageView_User_sb_message_ogtag_url_appearance,
                R.style.SendbirdCaption2OnLight02
            )
            binding.tvOgTitle.setAppearance(context, ogtagTitleAppearance)
            binding.tvOgDescription.setAppearance(context, ogtagDescriptionAppearance)
            binding.tvOgUrl.setAppearance(context, ogtagUrlAppearance)
        } finally {
            a.recycle()
        }
    }
}
