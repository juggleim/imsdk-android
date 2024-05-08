package com.jet.im.kit.internal.ui.messages

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sendbird.android.message.BaseMessage
import com.jet.im.kit.R
import com.jet.im.kit.SendbirdUIKit
import com.jet.im.kit.activities.adapter.FormFieldAdapter
import com.jet.im.kit.databinding.SbViewFormMessageComponentBinding
import com.jet.im.kit.internal.extensions.setAppearance
import com.jet.im.kit.model.MessageListUIParams
import com.jet.im.kit.utils.DrawableUtils
import com.jet.im.kit.utils.ViewUtils

internal class FormMessageView @JvmOverloads internal constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = R.attr.sb_widget_other_user_message
) : BaseMessageView(context, attrs, defStyle) {
    override val binding: SbViewFormMessageComponentBinding
    private val editedAppearance: Int
    private val sentAtAppearance: Int
    private val nicknameAppearance: Int
    private val messageAppearance: Int
    private val formFieldAdapter: FormFieldAdapter = FormFieldAdapter()

    override val layout: View
        get() = binding.root

    init {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.MessageView_User, defStyle, 0)
        try {
            binding = SbViewFormMessageComponentBinding.inflate(LayoutInflater.from(getContext()), this, true)
            val isDarkMode = SendbirdUIKit.isDarkMode()
            sentAtAppearance = a.getResourceId(
                R.styleable.MessageView_User_sb_message_time_text_appearance,
                R.style.SendbirdCaption4OnLight03
            )
            nicknameAppearance = a.getResourceId(
                R.styleable.MessageView_User_sb_message_sender_name_text_appearance,
                R.style.SendbirdCaption1OnLight02
            )
            editedAppearance = a.getResourceId(
                R.styleable.MessageView_User_sb_message_other_edited_mark_text_appearance,
                R.style.SendbirdBody3OnLight02
            )
            messageAppearance = a.getResourceId(
                R.styleable.MessageView_User_sb_message_other_text_appearance,
                R.style.SendbirdBody3OnLight01
            )
            val messageBackground = a.getResourceId(
                R.styleable.MessageView_User_sb_message_other_background,
                R.drawable.sb_shape_chat_bubble
            )
            val messageBackgroundTint =
                a.getColorStateList(R.styleable.MessageView_User_sb_message_other_background_tint)

            binding.contentPanel.background =
                DrawableUtils.setTintList(context, messageBackground, messageBackgroundTint)

            binding.rvFormFields.adapter = formFieldAdapter
            binding.rvFormFields.layoutManager = LinearLayoutManager(context)
            binding.rvFormFields.addItemDecoration(
                ItemSpacingDecoration(resources.getDimensionPixelSize(R.dimen.sb_size_8))
            )

            binding.buttonSubmit.background = if (isDarkMode) {
                ResourcesCompat.getDrawable(resources, R.drawable.sb_shape_submit_button_dark, null)
            } else {
                ResourcesCompat.getDrawable(resources, R.drawable.sb_shape_submit_button_light, null)
            }

            binding.buttonSubmit.setAppearance(
                context,
                if (isDarkMode) R.style.SendbirdButtonOnLight01 else R.style.SendbirdButtonOnDark01
            )
            val linkTextColor = a.getColorStateList(R.styleable.MessageView_User_sb_message_other_link_text_color)
            val clickedLinkBackgroundColor = a.getResourceId(
                R.styleable.MessageView_User_sb_message_other_clicked_link_background_color,
                R.color.primary_100
            )
            binding.tvMessageFormDisabled.setLinkTextColor(linkTextColor)
            binding.tvMessageFormDisabled.clickedLinkBackgroundColor = ContextCompat.getColor(context, clickedLinkBackgroundColor)
        } finally {
            a.recycle()
        }
    }

    fun drawFormMessage(message: BaseMessage, messageListUIParams: MessageListUIParams) {
        val form = message.forms.firstOrNull() ?: return
        formFieldAdapter.setFormFields(form)
        messageUIConfig?.let {
            it.otherSentAtTextUIConfig.mergeFromTextAppearance(context, sentAtAppearance)
            it.otherNicknameTextUIConfig.mergeFromTextAppearance(context, nicknameAppearance)
            it.otherMessageBackground?.let { background -> binding.contentPanel.background = background }
            it.otherEditedTextMarkUIConfig.mergeFromTextAppearance(context, editedAppearance)
            it.otherMessageTextUIConfig.mergeFromTextAppearance(context, messageAppearance)
            it.linkedTextColor?.let { linkedTextColor -> binding.tvMessageFormDisabled.setLinkTextColor(linkedTextColor) }
        }

        if (messageListUIParams.channelConfig.enableFormTypeMessage) {
            binding.formEnabledLayout.visibility = VISIBLE
            binding.tvMessageFormDisabled.visibility = GONE
        } else {
            binding.formEnabledLayout.visibility = GONE
            binding.tvMessageFormDisabled.visibility = VISIBLE
        }

        ViewUtils.drawTextMessage(
            binding.tvMessageFormDisabled,
            message,
            messageUIConfig,
            false,
            null,
            null
        )

        if (form.isSubmitted) {
            setSubmitButtonVisibility(View.GONE)
        } else {
            setSubmitButtonVisibility(View.VISIBLE)
        }
        ViewUtils.drawNickname(binding.tvNickname, message, messageUIConfig, false)
        ViewUtils.drawProfile(binding.ivProfileView, message)
        ViewUtils.drawSentAt(binding.tvSentAt, message, messageUIConfig)
    }

    private fun setSubmitButtonVisibility(visibility: Int) {
        if (visibility !in setOf(View.VISIBLE, View.GONE)) return
        binding.buttonSubmit.visibility = visibility
    }

    fun setSubmitButtonClickListener(listener: OnClickListener?) {
        binding.buttonSubmit.setOnClickListener { view ->
            val isSubmittable = formFieldAdapter.isSubmittable()
            formFieldAdapter.updateValidation()
            if (!isSubmittable) {
                return@setOnClickListener
            }
            listener?.onClick(view)
        }
    }

    private class ItemSpacingDecoration(
        private val spacing: Int
    ) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            val position = parent.getChildAdapterPosition(view)
            val itemCount = state.itemCount
            val isLastPosition = position == (itemCount - 1)

            with(outRect) {
                left = 0
                top = 0
                right = 0
                bottom = if (isLastPosition) 0 else spacing
            }
        }
    }
}
