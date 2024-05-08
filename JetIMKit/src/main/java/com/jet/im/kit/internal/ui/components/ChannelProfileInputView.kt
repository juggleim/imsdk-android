package com.jet.im.kit.internal.ui.components

import android.content.Context
import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.jet.im.kit.R
import com.jet.im.kit.databinding.SbViewChannelProfileInputBinding
import com.jet.im.kit.interfaces.OnInputTextChangedListener
import com.jet.im.kit.internal.extensions.setAppearance
import com.jet.im.kit.internal.extensions.setCursorDrawable

internal class ChannelProfileInputView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {
    val binding: SbViewChannelProfileInputBinding
    var onInputTextChangedListener: OnInputTextChangedListener? = null
    var onClearButtonClickListener: OnClickListener? = null
    var onMediaSelectButtonClickListener: OnClickListener? = null

    private val mediaSelectIconBg: Int

    init {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.ChannelProfileInput, defStyle, 0)
        try {
            binding = SbViewChannelProfileInputBinding.inflate(LayoutInflater.from(getContext())).apply {
                val itemBackground = a.getResourceId(
                    R.styleable.ChannelProfileInput_sb_channel_profile_input_background,
                    R.color.background_50
                )
                val inputBackground = a.getResourceId(
                    R.styleable.ChannelProfileInput_sb_channel_profile_input_text_input_background,
                    R.drawable.sb_shape_search_background
                )
                val textAppearance =
                    a.getResourceId(
                        R.styleable.ChannelProfileInput_sb_channel_profile_input_text_appearance,
                        R.style.SendbirdBody3OnLight01
                    )
                val hintText =
                    a.getResourceId(
                        R.styleable.ChannelProfileInput_sb_channel_profile_input_hint_text,
                        R.string.text_input_channel_name_hint
                    )
                val hintTextColor =
                    a.getColorStateList(R.styleable.ChannelProfileInput_sb_channel_profile_input_hint_text_color)
                val clearIcon = a.getResourceId(
                    R.styleable.ChannelProfileInput_sb_channel_profile_input_clear_icon,
                    R.drawable.icon_remove
                )
                val clearIconTintColor =
                    a.getColorStateList(R.styleable.ChannelProfileInput_sb_channel_profile_input_clear_icon_tint_color)
                mediaSelectIconBg = a.getResourceId(
                    R.styleable.ChannelProfileInput_sb_channel_profile_input_media_select_icon_background,
                    R.drawable.sb_shape_circle_background_300
                )
                val cameraIcon = a.getResourceId(
                    R.styleable.ChannelProfileInput_sb_channel_profile_input_camera_icon,
                    R.drawable.icon_remove
                )
                val cameraIconTintColor =
                    a.getColorStateList(R.styleable.ChannelProfileInput_sb_channel_profile_input_camera_icon_tint)
                val cursorDrawable = a.getResourceId(
                    R.styleable.ChannelProfileInput_sb_channel_profile_input_cursor_drawable,
                    R.drawable.sb_message_input_cursor_light
                )
                root.setBackgroundResource(itemBackground)
                inputContainer.setBackgroundResource(inputBackground)
                ivClear.setImageResource(clearIcon)
                ivClear.imageTintList = clearIconTintColor
                ivMediaSelector.setBackgroundResource(mediaSelectIconBg)
                ivCameraIcon.setImageResource(cameraIcon)
                ivCameraIcon.imageTintList = cameraIconTintColor
                etChannelName.setAppearance(context, textAppearance)
                etChannelName.setHint(hintText)
                etChannelName.setHintTextColor(hintTextColor)
                etChannelName.setCursorDrawable(context, cursorDrawable)
                etChannelName.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                        ivClear.visibility = if (count > 0) VISIBLE else GONE
                        onInputTextChangedListener?.onInputTextChanged(s, start, before, count)
                    }

                    override fun afterTextChanged(s: Editable) {}
                })
                ivClear.setOnClickListener {
                    onClearButtonClickListener?.onClick(it)
                }
                ivMediaSelector.setOnClickListener {
                    onMediaSelectButtonClickListener?.onClick(it)
                }
            }
            addView(binding.root, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        } finally {
            a.recycle()
        }
    }

    fun drawCoverImage(uri: Uri?) {
        uri?.let {
            binding.ivCameraIcon.visibility = GONE
            Glide.with(context)
                .load(uri)
                .override(binding.ivMediaSelector.width, binding.ivMediaSelector.height)
                .circleCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.ivMediaSelector)
        } ?: run {
            binding.ivMediaSelector.setImageDrawable(null)
            binding.ivCameraIcon.visibility = VISIBLE
        }
    }

    fun setText(text: CharSequence) {
        binding.etChannelName.setText(text)
    }

    fun getText(): CharSequence? = binding.etChannelName.text
}
