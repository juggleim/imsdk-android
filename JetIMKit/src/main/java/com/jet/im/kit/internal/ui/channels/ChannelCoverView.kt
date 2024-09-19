package com.jet.im.kit.internal.ui.channels

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.ImageView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.jet.im.kit.R
import com.jet.im.kit.SendbirdUIKit
import com.jet.im.kit.internal.ui.widgets.ImageWaffleView
import com.jet.im.kit.utils.DrawableUtils
import com.jet.im.kit.utils.TextUtils

internal class ChannelCoverView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : ImageWaffleView(context, attrs) {
    @DrawableRes
    var defaultImageResId = 0

    fun loadImage(url: String) {
        val imageView = prepareSingleImageView()
        drawImageFromUrl(imageView, url)
    }

    fun loadImages(imageUrlList: List<String>) {
        if (imageUrlList.isEmpty()) {
            prepareSingleImageView().setImageDrawable(getDefaultDrawable())
            return
        }
        val maxImageUrlSize = 4
        val profileImages: List<ImageView> = prepareImageViews(imageUrlList.size)
        val size = maxImageUrlSize.coerceAtMost(imageUrlList.size)
        for (i in 0 until size) {
            val imageView: ImageView = profileImages[i]
            val url = imageUrlList[i]
            drawImageFromUrl(imageView, url)
        }
    }

    private fun drawImageFromUrl(imageView: ImageView, url: String) {
        if (TextUtils.isEmpty(url)) {
            imageView.setImageDrawable(getDefaultDrawable())
            return
        }
        val overrideSize = resources
            .getDimensionPixelSize(R.dimen.sb_size_64)
        Glide.with(imageView.context)
            .load(url)
            .override(overrideSize, overrideSize)
            .centerCrop()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .error(getDefaultDrawable())
            .into(imageView)
    }

    private  fun getDefaultDrawable(): Drawable? {
        return if (defaultImageResId != 0) {
            AppCompatResources.getDrawable(context, defaultImageResId)
        } else {
            @ColorRes
            val iconTint: Int =
                if (SendbirdUIKit.isDarkMode()) R.color.onlight_01 else R.color.ondark_01

            @ColorRes
            val backgroundTint: Int = R.color.background_300
            DrawableUtils.createOvalIcon(
                context,
                backgroundTint, R.drawable.icon_user, iconTint
            )
        }
    }

    fun drawBroadcastChannelCover() {
        val imageView: ImageView = prepareSingleImageView()

        @ColorRes
        val iconTint: Int = if (SendbirdUIKit.isDarkMode()) R.color.onlight_01 else R.color.ondark_01

        @ColorRes
        val backgroundTint = SendbirdUIKit.getDefaultThemeMode().secondaryTintResId
        imageView.setImageDrawable(
            DrawableUtils.createOvalIcon(
                context,
                backgroundTint,
                R.drawable.icon_broadcast,
                iconTint
            )
        )
    }
}
