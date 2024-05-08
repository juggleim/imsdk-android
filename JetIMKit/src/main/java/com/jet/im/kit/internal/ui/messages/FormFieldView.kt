package com.jet.im.kit.internal.ui.messages

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.sendbird.android.message.Answer
import com.sendbird.android.message.FormField
import com.sendbird.android.message.FormFieldInputType
import com.jet.im.kit.R
import com.jet.im.kit.SendbirdUIKit
import com.jet.im.kit.databinding.SbViewFormFieldComponentBinding
import com.jet.im.kit.internal.extensions.lastValidation
import com.jet.im.kit.internal.extensions.setAppearance

internal class FormFieldView @JvmOverloads internal constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : BaseMessageView(context, attrs, defStyle) {
    override val binding: SbViewFormFieldComponentBinding = SbViewFormFieldComponentBinding.inflate(
        LayoutInflater.from(getContext()),
        this,
        true
    )
    override val layout: View
        get() = binding.root

    private val etFormFieldBackground = if (SendbirdUIKit.isDarkMode()) {
        ResourcesCompat.getDrawable(resources, R.drawable.sb_shape_edit_text_form_field_normal_dark, null)
    } else {
        ResourcesCompat.getDrawable(resources, R.drawable.sb_shape_edit_text_form_field_normal_light, null)
    }

    private val etFormFieldBackgroundError = if (SendbirdUIKit.isDarkMode()) {
        ResourcesCompat.getDrawable(resources, R.drawable.sb_shape_edit_text_form_field_invalid_dark, null)
    } else {
        ResourcesCompat.getDrawable(resources, R.drawable.sb_shape_edit_text_form_field_invalid_light, null)
    }

    private var textWatcher: FormFieldTextWatcher? = null

    init {
        val isDarkMode = SendbirdUIKit.isDarkMode()
        binding.tvFormFieldTitle.setAppearance(
            context,
            if (isDarkMode) R.style.SendbirdCaption3OnDark02
            else R.style.SendbirdCaption3OnLight02
        )

        binding.tvFormFieldTitleOptional.setAppearance(
            context,
            if (isDarkMode) R.style.SendbirdCaption3OnDark03
            else R.style.SendbirdCaption3OnLight03
        )

        binding.etFormField.background = etFormFieldBackground

        binding.etFormField.setAppearance(
            context,
            if (isDarkMode) R.style.SendbirdBody3OnDark01
            else R.style.SendbirdBody3OnLight01
        )

        binding.etFormField.setHintTextColor(
            ContextCompat.getColor(context, if (isDarkMode) R.color.ondark_03 else R.color.onlight_03)
        )

        binding.tvFormFieldError.setAppearance(
            context,
            if (isDarkMode) R.style.SendbirdCaption4Error200
            else R.style.SendbirdCaption4Error300
        )

        binding.answeredLayout.background = if (isDarkMode) {
            ResourcesCompat.getDrawable(resources, R.drawable.sb_shape_round_rect_background_onlight_04, null)
        } else {
            ResourcesCompat.getDrawable(resources, R.drawable.sb_shape_round_rect_background_ondark_02, null)
        }
        binding.iconDone.setColorFilter(
            ContextCompat.getColor(context, if (isDarkMode) R.color.secondary_300 else R.color.secondary_200)
        )

        binding.tvAnswer.setAppearance(
            context,
            if (isDarkMode) R.style.SendbirdBody3OnDark01
            else R.style.SendbirdBody3OnLight01
        )
    }

    fun drawFormField(formField: FormField) {
        textWatcher?.let { binding.etFormField.removeTextChangedListener(it) }
        binding.tvFormFieldTitle.text = formField.title
        binding.tvFormFieldTitleOptional.visibility = if (formField.required) GONE else VISIBLE

        when (formField.lastValidation) {
            true, null -> showValidFormField()
            false -> showInvalidFormField()
        }

        val answer = formField.answer
        if (answer == null) {
            binding.unansweredLayout.visibility = VISIBLE
            binding.answeredLayout.visibility = GONE
            if (formField.inputType == FormFieldInputType.PASSWORD) {
                binding.etFormField.transformationMethod = PasswordTransformationMethod()
            } else {
                binding.etFormField.transformationMethod = null
            }
            binding.etFormField.setText(formField.temporaryAnswer?.value ?: "")
            textWatcher = FormFieldTextWatcher(formField).also {
                binding.etFormField.addTextChangedListener(it)
            }
            formField.placeholder?.let { binding.etFormField.hint = it }
        } else {
            binding.unansweredLayout.visibility = GONE
            binding.answeredLayout.visibility = VISIBLE
            binding.tvAnswer.text = answer.value
        }
    }

    fun showValidFormField() {
        binding.etFormField.background = etFormFieldBackground
        binding.tvFormFieldError.visibility = GONE
    }

    fun showInvalidFormField() {
        binding.etFormField.background = etFormFieldBackgroundError
        binding.tvFormFieldError.visibility = VISIBLE
    }

    private inner class FormFieldTextWatcher(
        private val formField: FormField
    ) : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun afterTextChanged(s: Editable?) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            if (s.isEmpty()) {
                formField.temporaryAnswer = null
                formField.lastValidation = null
                showValidFormField()
                return
            }

            if (!formField.isValid(s.toString())) {
                formField.lastValidation = false
                showInvalidFormField()
            } else {
                formField.lastValidation = true
                showValidFormField()
            }

            formField.temporaryAnswer = Answer(formField.key, s.toString())
        }
    }
}
