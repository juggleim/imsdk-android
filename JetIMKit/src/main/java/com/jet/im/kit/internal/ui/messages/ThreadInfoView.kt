package com.jet.im.kit.internal.ui.messages

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.sendbird.android.message.ThreadInfo
import com.sendbird.android.user.User
import com.jet.im.kit.R
import com.jet.im.kit.SendbirdUIKit
import com.jet.im.kit.databinding.SbViewThreadInfoBinding
import com.jet.im.kit.internal.extensions.setAppearance
import com.jet.im.kit.internal.model.GlideCachedUrlLoader
import com.jet.im.kit.utils.DrawableUtils
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import java.security.MessageDigest
import kotlin.math.min

internal class ThreadInfoView @JvmOverloads internal constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.sb_widget_thread_info,
) : FrameLayout(context, attrs, defStyleAttr) {
    private val binding: SbViewThreadInfoBinding
    private val profileViews: MutableList<ImageView> = mutableListOf()
    private val moreDrawable: Drawable

    companion object {
        private const val MAX_PROFILE_SIZE = 5
    }

    fun drawThreadInfo(threadInfo: ThreadInfo) {
        if (threadInfo.replyCount <= 0) {
            visibility = GONE
            return
        }
        visibility = VISIBLE

        val userSize = threadInfo.mostRepliedUsers.size
        val profileSize = min(MAX_PROFILE_SIZE, userSize)
        for (index in 0 until MAX_PROFILE_SIZE) {
            profileViews[index].visibility = GONE
            if (index >= profileSize) continue
            profileViews[index].visibility = VISIBLE
            val user: User = threadInfo.mostRepliedUsers[index]
            val overrideSize = resources
                .getDimensionPixelSize(R.dimen.sb_size_20)

            val transformation = if (index == MAX_PROFILE_SIZE - 1) MultiTransformation(
                CircleCrop(),
                MoreIcon(resources, moreDrawable)
            ) else MultiTransformation(CircleCrop())

            GlideCachedUrlLoader.load(
                Glide.with(context),
                user.profileUrl,
                user.plainProfileImageUrl.hashCode().toString()
            )
                .override(overrideSize, overrideSize)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(
                    DrawableUtils.createOvalIcon(
                        context,
                        R.color.background_300,
                        R.drawable.icon_user,
                        if (SendbirdUIKit.isDarkMode()) R.color.onlight_01 else R.color.ondark_01
                    )
                )
                .transform(transformation)
                .into(profileViews[index])
        }

        binding.tvReplyCount.text = if (threadInfo.replyCount < 100) String.format(
            if (threadInfo.replyCount == 1)
                context.getString(R.string.sb_text_number_of_reply)
            else
                context.getString(R.string.sb_text_number_of_replies),
            threadInfo.replyCount
        ) else {
            context.getString(R.string.sb_text_max_number_of_replies)
        }
    }

    init {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.ThreadInfoView, defStyleAttr, 0)
        try {
            binding = SbViewThreadInfoBinding.inflate(LayoutInflater.from(context), this, true)
            val replyCountTextAppearance = a.getResourceId(
                R.styleable.ThreadInfoView_sb_thread_info_reply_count_text_appearance,
                R.style.SendbirdCaption3Primary300
            )
            val moreIconRes = a.getResourceId(
                R.styleable.ThreadInfoView_sb_thread_info_more_icon,
                R.drawable.icon_plus
            )
            val moreIconTint = a.getResourceId(
                R.styleable.ThreadInfoView_sb_thread_info_more_icon_tint,
                R.color.ondark_01
            )
            binding.tvReplyCount.setAppearance(context, replyCountTextAppearance)

            moreDrawable = DrawableUtils.createOvalIcon(
                context,
                R.color.overlay_01,
                140,
                moreIconRes,
                moreIconTint
            )

            for (idx in 0 until MAX_PROFILE_SIZE) {
                val profileView = createProfileView()
                profileView.visibility = GONE
                profileViews.add(profileView)
                binding.profileViewPanel.addView(profileView)
            }
        } finally {
            a.recycle()
        }
    }

    private fun createProfileView(): ImageView {
        val imageView = ImageView(context)
        val width = context.resources.getDimensionPixelSize(R.dimen.sb_size_20)
        val height = context.resources.getDimensionPixelSize(R.dimen.sb_size_20)
        val layoutParams = MarginLayoutParams(width, height)
        layoutParams.marginEnd = context.resources.getDimensionPixelSize(R.dimen.sb_size_5)
        imageView.layoutParams = layoutParams
        return imageView
    }

    private class MoreIcon(val resources: Resources, val moreDrawable: Drawable) : BitmapTransformation() {
        private val ID = "com.jet.im.kit.internal.ui.messages.MoreIcon"
        private val ID_BYTES: ByteArray = ID.toByteArray(Charset.forName("UTF-8"))

        override fun transform(pool: BitmapPool, toTransform: Bitmap, outWidth: Int, outHeight: Int): Bitmap {
            val toTransformDrawable = BitmapDrawable(resources, toTransform)
            val layer = arrayOf(toTransformDrawable, moreDrawable)
            val layerDrawable = LayerDrawable(layer)
            return DrawableUtils.toBitmap(layerDrawable) ?: toTransform
        }

        override fun updateDiskCacheKey(messageDigest: MessageDigest) {
            messageDigest.update(ID_BYTES)

            val bitmap = DrawableUtils.toBitmap(moreDrawable)
            val stream = ByteArrayOutputStream()
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            messageDigest.update(stream.toByteArray())
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is MoreIcon) return false

            if (moreDrawable != other.moreDrawable) return false
            if (ID != other.ID) return false

            return true
        }

        override fun hashCode(): Int {
            var result = moreDrawable.hashCode()
            result = 31 * result + ID.hashCode()
            return result
        }
    }
}
