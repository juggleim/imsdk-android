package com.jet.im.kit.widgets

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.jet.im.kit.R
import com.jet.im.kit.SendbirdUIKit
import com.jet.im.kit.consts.StringSet
import com.jet.im.kit.databinding.SbViewMessageInputBinding
import com.jet.im.kit.interfaces.OnInputModeChangedListener
import com.jet.im.kit.interfaces.OnInputTextChangedListener
import com.jet.im.kit.internal.extensions.getCacheKey
import com.jet.im.kit.internal.extensions.setAppearance
import com.jet.im.kit.internal.extensions.setCursorDrawable
import com.jet.im.kit.internal.extensions.toDisplayText
import com.jet.im.kit.model.TextUIConfig
import com.jet.im.kit.utils.MessageUtils
import com.jet.im.kit.utils.SoftInputUtils
import com.jet.im.kit.utils.TextUtils
import com.jet.im.kit.utils.ViewUtils
import com.juggle.im.model.Message
import com.sendbird.android.message.FileMessage
import com.sendbird.android.message.MultipleFilesMessage

class MessageInputView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {
    val textAppearance: Int
    val binding: SbViewMessageInputBinding
    val layout: View
        get() = this
    var onSendClickListener: OnClickListener? = null
        set(value) {
            field = value
            binding.ibtnSend.setOnClickListener(value)
        }
    var onVoiceRecorderButtonClickListener: OnClickListener? = null
        set(value) {
            field = value
            binding.ibtnVoiceRecorder.setOnClickListener(value)
        }
    var onAddClickListener: OnClickListener? = null
        set(value) {
            field = value
            binding.ibtnAdd.setOnClickListener(value)
        }
    var onEditCancelClickListener: OnClickListener? = null
        set(value) {
            field = value
            binding.btnCancel.setOnClickListener(value)
        }
    var onEditSaveClickListener: OnClickListener? = null
        set(value) {
            field = value
            binding.btnSave.setOnClickListener(value)
        }
    var onReplyCloseClickListener: OnClickListener? = null
        set(value) {
            field = value
            binding.ivQuoteReplyClose.setOnClickListener(value)
        }
    var onInputTextChangedListener: OnInputTextChangedListener? = null
    var onEditModeTextChangedListener: OnInputTextChangedListener? = null
    var onInputModeChangedListener: OnInputModeChangedListener? = null
    var mode = Mode.DEFAULT
    var addButtonVisibility = VISIBLE
        set(value) {
            field = value
            binding.ibtnAdd.visibility = value
        }
    var showSendButtonAlways = false
    var useVoiceButton = false
        set(value) {
            field = value
            setVoiceRecorderButtonVisibility(if (value) VISIBLE else GONE)
        }
    var useOverlay = false
    var inputText: CharSequence?
        get() = binding.etInputText.text?.trim { it <= ' ' }
        set(text) {
            binding.etInputText.setText(text)
            text?.let { binding.etInputText.setSelection(text.length) }
        }

    val inputEditText: EditText
        get() = binding.etInputText

    var inputMode: Mode
        get() = mode
        set(mode) {
            val before = this.mode
            this.mode = mode
            when (mode) {
                Mode.EDIT -> {
                    setQuoteReplyPanelVisibility(GONE)
                    setEditPanelVisibility(VISIBLE)
                    setVoiceRecorderButtonVisibility(GONE)
                    binding.ibtnAdd.visibility = GONE
                }

                else -> {
                    setQuoteReplyPanelVisibility(GONE)
                    setEditPanelVisibility(GONE)
                    addButtonVisibility = this@MessageInputView.addButtonVisibility
                }
            }
            onInputModeChangedListener?.onInputModeChanged(before, mode)
        }

    enum class Mode {
        /**
         * A mode to be able to send a message normally.
         */
        DEFAULT,

        /**
         * A mode to edit current message.
         */
        EDIT
    }

    fun showKeyboard() {
        SoftInputUtils.showSoftKeyboard(
            binding.etInputText
        )
    }


    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        binding.ibtnAdd.isEnabled = enabled
        binding.etInputText.isEnabled = enabled
        binding.ibtnSend.isEnabled = enabled
        binding.ibtnVoiceRecorder.isEnabled = enabled
    }

    fun setSendButtonVisibility(visibility: Int) {
        binding.ibtnSend.visibility = visibility
    }

    fun setVoiceRecorderButtonVisibility(visibility: Int) {
        binding.ibtnVoiceRecorder.visibility = visibility
    }

    fun setSendImageResource(@DrawableRes sendImageResource: Int) {
        binding.ibtnSend.setImageResource(sendImageResource)
    }

    fun setSendImageDrawable(drawable: Drawable?) {
        binding.ibtnSend.setImageDrawable(drawable)
    }

    fun setSendImageButtonTint(tint: ColorStateList?) {
        binding.ibtnSend.imageTintList = tint
    }

    fun setAddImageResource(@DrawableRes addImageResource: Int) {
        binding.ibtnAdd.setImageResource(addImageResource)
    }

    fun setAddImageDrawable(drawable: Drawable?) {
        binding.ibtnAdd.setImageDrawable(drawable)
    }

    fun setAddImageButtonTint(tint: ColorStateList?) {
        binding.ibtnAdd.imageTintList = tint
    }

    fun setEditPanelVisibility(visibility: Int) {
        binding.editPanel.visibility = visibility
    }

    fun setQuoteReplyPanelVisibility(visibility: Int) {
        binding.quoteReplyPanel.visibility = visibility
        binding.ivReplyDivider.visibility = visibility
    }

    fun setInputTextHint(hint: CharSequence?) {
        binding.etInputText.hint = hint
    }

    fun applyTextUIConfig(textUIConfig: TextUIConfig) {
        binding.etInputText.applyTextUIConfig(textUIConfig)
    }

    init {
        val a = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.MessageInputComponent,
            defStyle,
            0
        )
        try {
            binding =
                SbViewMessageInputBinding.inflate(LayoutInflater.from(getContext()), this, true)
            val backgroundId =
                a.getResourceId(
                    R.styleable.MessageInputComponent_sb_message_input_background,
                    R.color.background_50
                )
            val textBackgroundId = a.getResourceId(
                R.styleable.MessageInputComponent_sb_message_input_text_background,
                R.drawable.sb_message_input_text_background_light
            )
            textAppearance = a.getResourceId(
                R.styleable.MessageInputComponent_sb_message_input_text_appearance,
                R.style.SendbirdBody3OnLight01
            )
            val hint = a.getString(R.styleable.MessageInputComponent_sb_message_input_text_hint)
            val hintColor =
                a.getColorStateList(R.styleable.MessageInputComponent_sb_message_input_text_hint_color)
            val textCursorDrawable = a.getResourceId(
                R.styleable.MessageInputComponent_sb_message_input_text_cursor_drawable,
                R.drawable.sb_message_input_cursor_light
            )
            val leftButtonIcon = a.getResourceId(
                R.styleable.MessageInputComponent_sb_message_input_left_button_icon,
                R.drawable.icon_add
            )
            val leftButtonTint =
                a.getColorStateList(R.styleable.MessageInputComponent_sb_message_input_left_button_tint)
            val leftButtonBackground = a.getResourceId(
                R.styleable.MessageInputComponent_sb_message_input_left_button_background,
                R.drawable.sb_button_uncontained_background_light
            )
            val rightButtonIcon = a.getResourceId(
                R.styleable.MessageInputComponent_sb_message_input_right_button_icon,
                R.drawable.icon_send
            )
            val rightButtonTint =
                a.getColorStateList(R.styleable.MessageInputComponent_sb_message_input_right_button_tint)
            val rightButtonBackground = a.getResourceId(
                R.styleable.MessageInputComponent_sb_message_input_right_button_background,
                R.drawable.sb_button_uncontained_background_light
            )
            val micButtonIcon = a.getResourceId(
                R.styleable.MessageInputComponent_sb_message_input_voice_recorder_button_icon,
                R.drawable.icon_send
            )
            val micButtonTint =
                a.getColorStateList(R.styleable.MessageInputComponent_sb_message_input_voice_recorder_button_tint)
            val micButtonBackground = a.getResourceId(
                R.styleable.MessageInputComponent_sb_message_input_voice_recorder_button_background,
                R.drawable.sb_button_uncontained_background_light
            )
            val editSaveButtonTextAppearance = a.getResourceId(
                R.styleable.MessageInputComponent_sb_message_input_edit_save_button_text_appearance,
                R.style.SendbirdButtonOnDark01
            )
            val editSaveButtonTextColor =
                a.getColorStateList(R.styleable.MessageInputComponent_sb_message_input_edit_save_button_text_color)
            val editSaveButtonBackground = a.getResourceId(
                R.styleable.MessageInputComponent_sb_message_input_edit_save_button_background,
                R.drawable.sb_button_contained_background_light
            )
            val editCancelButtonTextAppearance = a.getResourceId(
                R.styleable.MessageInputComponent_sb_message_input_edit_cancel_button_text_appearance,
                R.style.SendbirdButtonPrimary300
            )
            val editCancelButtonTextColor =
                a.getColorStateList(R.styleable.MessageInputComponent_sb_message_input_edit_cancel_button_text_color)
            val editCancelButtonBackground = a.getResourceId(
                R.styleable.MessageInputComponent_sb_message_input_edit_cancel_button_background,
                R.drawable.sb_button_uncontained_background_light
            )
            val replyTitleAppearance = a.getResourceId(
                R.styleable.MessageInputComponent_sb_message_input_quote_reply_title_text_appearance,
                R.style.SendbirdCaption1OnLight01
            )
            val replyMessageAppearance = a.getResourceId(
                R.styleable.MessageInputComponent_sb_message_input_quoted_message_text_appearance,
                R.style.SendbirdCaption2OnLight03
            )
            val replyRightButtonIcon = a.getResourceId(
                R.styleable.MessageInputComponent_sb_message_input_quote_reply_right_icon,
                R.drawable.icon_close
            )
            val replyRightButtonTint =
                a.getColorStateList(R.styleable.MessageInputComponent_sb_message_input_quote_reply_right_icon_tint)
            val replyRightButtonBackground = a.getResourceId(
                R.styleable.MessageInputComponent_sb_message_input_quote_reply_right_icon_background,
                R.drawable.sb_button_uncontained_background_light
            )
            binding.messageInputParent.setBackgroundResource(backgroundId)
            binding.etInputText.setBackgroundResource(textBackgroundId)
            binding.etInputText.setAppearance(context, textAppearance)
            hint?.let { setInputTextHint(it) }
            if (hintColor != null) {
                binding.etInputText.setHintTextColor(hintColor)
            }
            binding.etInputText.setCursorDrawable(context, textCursorDrawable)
            isEnabled = true
            binding.ibtnAdd.setBackgroundResource(leftButtonBackground)
            setAddImageResource(leftButtonIcon)
            binding.ibtnAdd.imageTintList = leftButtonTint
            binding.ibtnSend.setBackgroundResource(rightButtonBackground)
            setSendImageResource(rightButtonIcon)
            binding.ibtnSend.imageTintList = rightButtonTint
            binding.ibtnVoiceRecorder.setBackgroundResource(micButtonBackground)
            binding.ibtnVoiceRecorder.setImageResource(micButtonIcon)
            binding.ibtnVoiceRecorder.imageTintList = micButtonTint
            setVoiceRecorderButtonVisibility(if (useVoiceButton) VISIBLE else GONE)
            binding.btnSave.setAppearance(context, editSaveButtonTextAppearance)
            if (editSaveButtonTextColor != null) {
                binding.btnSave.setTextColor(editSaveButtonTextColor)
            }
            binding.btnSave.setBackgroundResource(editSaveButtonBackground)
            binding.btnCancel.setAppearance(context, editCancelButtonTextAppearance)
            if (editCancelButtonTextColor != null) {
                binding.btnCancel.setTextColor(editCancelButtonTextColor)
            }
            binding.btnCancel.setBackgroundResource(editCancelButtonBackground)
            binding.ivQuoteReplyMessageImage.radius =
                resources.getDimensionPixelSize(R.dimen.sb_size_8).toFloat()
            binding.tvQuoteReplyTitle.setAppearance(context, replyTitleAppearance)
            binding.tvQuoteReplyMessage.setAppearance(context, replyMessageAppearance)
            binding.ivQuoteReplyClose.setImageResource(replyRightButtonIcon)
            binding.ivQuoteReplyClose.imageTintList = replyRightButtonTint
            binding.ivQuoteReplyClose.setBackgroundResource(replyRightButtonBackground)
            val dividerColor =
                if (SendbirdUIKit.isDarkMode()) R.color.ondark_04 else R.color.onlight_04
            binding.ivReplyDivider.setBackgroundColor(ContextCompat.getColor(context, dividerColor))
            binding.etInputText.setOnClickListener { showKeyboard() }
            binding.etInputText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                    if (!TextUtils.isEmpty(s?.trim()) && Mode.EDIT != inputMode || showSendButtonAlways) {
                        setSendButtonVisibility(VISIBLE)
                        if (useVoiceButton) {
                            setVoiceRecorderButtonVisibility(GONE)
                        }
                    } else {
                        setSendButtonVisibility(GONE)
                        if (useVoiceButton && Mode.EDIT != inputMode) {
                            setVoiceRecorderButtonVisibility(VISIBLE)
                        }
                    }
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (Mode.EDIT == inputMode) {
                        onEditModeTextChangedListener?.onInputTextChanged(
                            s ?: "",
                            start,
                            before,
                            count
                        )
                    } else {
                        onInputTextChangedListener?.onInputTextChanged(
                            s ?: "",
                            start,
                            before,
                            count
                        )
                    }
                }

                override fun afterTextChanged(s: Editable?) {
                    if (!TextUtils.isEmpty(s?.trim()) && Mode.EDIT != inputMode || showSendButtonAlways) {
                        setSendButtonVisibility(VISIBLE)
                        if (useVoiceButton) {
                            setVoiceRecorderButtonVisibility(GONE)
                        }
                    } else {
                        setSendButtonVisibility(GONE)
                        if (useVoiceButton && Mode.EDIT != inputMode) {
                            setVoiceRecorderButtonVisibility(VISIBLE)
                        }
                    }
                }
            })
            binding.etInputText.inputType = (
                    InputType.TYPE_CLASS_TEXT
                            or InputType.TYPE_TEXT_FLAG_MULTI_LINE
                            or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                    )
        } finally {
            a.recycle()
        }
    }
}
