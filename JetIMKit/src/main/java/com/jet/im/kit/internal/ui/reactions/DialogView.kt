package com.jet.im.kit.internal.ui.reactions

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.StyleRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.jet.im.kit.R
import com.jet.im.kit.activities.adapter.DialogListAdapter
import com.jet.im.kit.consts.DialogEditTextParams
import com.jet.im.kit.consts.StringSet
import com.jet.im.kit.databinding.SbViewDialogBinding
import com.jet.im.kit.interfaces.OnEditTextResultListener
import com.jet.im.kit.interfaces.OnItemClickListener
import com.jet.im.kit.internal.extensions.setAppearance
import com.jet.im.kit.model.DialogListItem
import com.jet.im.kit.utils.DrawableUtils
import com.jet.im.kit.utils.SoftInputUtils

internal class DialogView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {
    private val binding: SbViewDialogBinding
    private val backgroundBottomId: Int
    private val backgroundAnchorId: Int

    private var editTextResultListener: OnEditTextResultListener? = null
    private val editText: String
        get() = binding.etInputText.text?.toString() ?: ""

    init {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.DialogView, 0, 0)
        try {
            binding = SbViewDialogBinding.inflate(LayoutInflater.from(context), null, false)
            addView(binding.root, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            val backgroundId =
                a.getResourceId(R.styleable.DialogView_sb_dialog_view_background, R.drawable.sb_rounded_rectangle_light)
            backgroundBottomId = a.getResourceId(
                R.styleable.DialogView_sb_dialog_view_background_bottom,
                R.drawable.sb_top_rounded_rectangle_light
            )
            backgroundAnchorId = a.getResourceId(
                R.styleable.DialogView_sb_dialog_view_background_anchor,
                R.drawable.layer_dialog_anchor_background_light
            )
            val titleAppearance =
                a.getResourceId(R.styleable.DialogView_sb_dialog_view_title_appearance, R.style.SendbirdH1OnLight01)
            val messageAppearance = a.getResourceId(
                R.styleable.DialogView_sb_dialog_view_message_appearance,
                R.style.SendbirdBody3OnLight02
            )
            val editTextAppearance = a.getResourceId(
                R.styleable.DialogView_sb_dialog_view_edit_text_appearance,
                R.style.SendbirdSubtitle2OnLight01
            )
            val editTextTint = a.getColorStateList(R.styleable.DialogView_sb_dialog_view_edit_text_tint)
            val editTextCursorDrawable = a.getResourceId(
                R.styleable.DialogView_sb_dialog_view_edit_text_cursor_drawable,
                R.drawable.sb_message_input_cursor_light
            )
            val editTextHintColor = a.getColorStateList(R.styleable.DialogView_sb_dialog_view_edit_text_hint_color)
            val positiveButtonTextAppearance = a.getResourceId(
                R.styleable.DialogView_sb_dialog_view_positive_button_text_appearance,
                R.style.SendbirdButtonPrimary300
            )
            val positiveButtonTextColor =
                a.getColorStateList(R.styleable.DialogView_sb_dialog_view_positive_button_text_color)
            val positiveButtonBackground = a.getResourceId(
                R.styleable.DialogView_sb_dialog_view_positive_button_background,
                R.drawable.sb_button_uncontained_background_light
            )
            val negativeButtonTextAppearance = a.getResourceId(
                R.styleable.DialogView_sb_dialog_view_negative_button_text_appearance,
                R.style.SendbirdButtonPrimary300
            )
            val negativeButtonTextColor =
                a.getColorStateList(R.styleable.DialogView_sb_dialog_view_negative_button_text_color)
            val negativeButtonBackground = a.getResourceId(
                R.styleable.DialogView_sb_dialog_view_negative_button_background,
                R.drawable.sb_button_uncontained_background_light
            )
            val neutralButtonTextAppearance = a.getResourceId(
                R.styleable.DialogView_sb_dialog_view_neutral_button_text_appearance,
                R.style.SendbirdButtonPrimary300
            )
            val neutralButtonTextColor =
                a.getColorStateList(R.styleable.DialogView_sb_dialog_view_neutral_button_text_color)
            val neutralButtonBackground = a.getResourceId(
                R.styleable.DialogView_sb_dialog_view_neutral_button_background,
                R.drawable.sb_button_uncontained_background_light
            )
            binding.sbParentPanel.setBackgroundResource(backgroundId)
            binding.tvDialogTitle.setAppearance(context, titleAppearance)
            binding.tvDialogMessage.setAppearance(context, messageAppearance)
            binding.etInputText.setAppearance(context, editTextAppearance)
            binding.etInputText.background = DrawableUtils.setTintList(binding.etInputText.background, editTextTint)
            editTextHintColor?.let {
                binding.etInputText.setHintTextColor(editTextHintColor)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                binding.etInputText.setTextCursorDrawable(editTextCursorDrawable)
            } else {
                val f = TextView::class.java.getDeclaredField(StringSet.mCursorDrawableRes)
                f.isAccessible = true
                f[binding.etInputText] = editTextCursorDrawable
            }
            binding.btPositive.setAppearance(context, positiveButtonTextAppearance)
            positiveButtonTextColor?.let {
                binding.btPositive.setTextColor(positiveButtonTextColor)
            }
            binding.btPositive.setBackgroundResource(positiveButtonBackground)
            binding.btNegative.setAppearance(context, negativeButtonTextAppearance)
            negativeButtonTextColor?.let {
                binding.btNegative.setTextColor(negativeButtonTextColor)
            }
            binding.btNegative.setBackgroundResource(negativeButtonBackground)
            binding.btNeutral.setAppearance(context, neutralButtonTextAppearance)
            neutralButtonTextColor?.let {
                binding.btNeutral.setTextColor(neutralButtonTextColor)
            }
            binding.btNeutral.setBackgroundResource(neutralButtonBackground)
        } finally {
            a.recycle()
        }
    }

    fun setTitle(title: Int) {
        if (title == 0) {
            return
        }
        binding.tvDialogTitle.setText(title)
        binding.tvDialogTitle.visibility = VISIBLE
    }

    fun setTitle(title: CharSequence?) {
        title?.isNotEmpty()?.let {
            binding.tvDialogTitle.text = title
            binding.tvDialogTitle.visibility = VISIBLE
        }
    }

    fun setTitleEmpty() {
        binding.tvDialogTitle.visibility = GONE
        binding.sbTopEmpty.visibility = VISIBLE
    }

    fun setMessageTextAppearance(@StyleRes resId: Int) {
        binding.tvDialogMessage.setAppearance(context, resId)
    }

    fun setMessage(message: CharSequence?) {
        if (message.isNullOrEmpty()) {
            binding.tvDialogMessage.visibility = GONE
        } else {
            binding.tvDialogMessage.text = message
            binding.tvDialogMessage.visibility = VISIBLE
        }
    }

    fun setEditText(params: DialogEditTextParams?, editTextResultListener: OnEditTextResultListener?) {
        params?.let {
            binding.etInputText.visibility = VISIBLE
            it.hintText.takeUnless { text -> text.isNullOrEmpty() }?.let { text ->
                binding.etInputText.hint = text
            }

            it.text.takeUnless { text -> text.isNullOrEmpty() }?.let { text ->
                binding.etInputText.setText(text)
            }

            binding.etInputText.isSingleLine = params.enableSingleLine
            it.ellipsis?.let {
                binding.etInputText.ellipsize = params.ellipsis
            }
            val data = binding.etInputText.text
            val selection = params.selection
            if (selection > 0 && data != null) {
                if (data.length > selection) {
                    binding.etInputText.setSelection(selection)
                }
            }
            SoftInputUtils.showSoftKeyboard(binding.etInputText)
            this.editTextResultListener = editTextResultListener
        }
    }

    fun setAdapter(adapter: RecyclerView.Adapter<*>) {
        binding.rvSelectView.adapter = adapter
        binding.rvSelectView.visibility = VISIBLE
    }

    fun setItems(
        items: Array<DialogListItem?>?,
        itemClickListener: OnItemClickListener<DialogListItem?>,
        useLeftIcon: Boolean
    ) {
        items?.let {
            binding.rvSelectView.adapter = DialogListAdapter(it, useLeftIcon, itemClickListener)
            binding.rvSelectView.visibility = VISIBLE
        }
    }

    fun setItems(
        items: Array<DialogListItem?>?,
        itemClickListener: OnItemClickListener<DialogListItem?>,
        useLeftIcon: Boolean,
        @DimenRes nameMarginLeft: Int
    ) {
        items?.let {
            val adapter = DialogListAdapter(it, useLeftIcon, itemClickListener)
            adapter.setNameMarginLeft(nameMarginLeft)
            binding.rvSelectView.adapter = adapter
            binding.rvSelectView.visibility = VISIBLE
        }
    }

    fun setPositiveButton(text: String?, @ColorRes textColor: Int, clickListener: OnClickListener) {
        text?.isNotEmpty()?.let {
            binding.btPositive.text = text
            if (textColor != 0) {
                binding.btPositive.setTextColor(AppCompatResources.getColorStateList(context, textColor))
            }
            binding.btPositive.setOnClickListener {
                editTextResultListener?.onResult(editText)
                clickListener.onClick(it)
            }
            binding.sbButtonPanel.visibility = VISIBLE
            binding.btPositive.visibility = VISIBLE
        }
    }

    fun setNegativeButton(text: String?, @ColorRes textColor: Int, clickListener: OnClickListener) {
        text?.isNotEmpty()?.let {
            binding.btNegative.text = text
            if (textColor != 0) {
                binding.btNegative.setTextColor(AppCompatResources.getColorStateList(context, textColor))
            }
            binding.btNegative.setOnClickListener(clickListener)
            binding.sbButtonPanel.visibility = VISIBLE
            binding.btNegative.visibility = VISIBLE
        }
    }

    fun setNeutralButton(text: String?, @ColorRes textColor: Int, clickListener: OnClickListener) {
        text?.isNotEmpty()?.let {
            binding.btNeutral.text = text
            if (textColor != 0) {
                binding.btNeutral.setTextColor(AppCompatResources.getColorStateList(context, textColor))
            }
            binding.btNeutral.setOnClickListener(clickListener)
            binding.sbButtonPanel.visibility = VISIBLE
            binding.btNeutral.visibility = VISIBLE
        }
    }

    fun setBackgroundBottom() {
        binding.sbParentPanel.setBackgroundResource(backgroundBottomId)
    }

    fun setBackgroundAnchor() {
        binding.sbParentPanel.setBackgroundResource(backgroundAnchorId)
    }

    fun setBackground(@DrawableRes background: Int) {
        binding.sbParentPanel.setBackgroundResource(background)
    }

    fun setContentView(view: View?) {
        view?.let {
            binding.sbContentViewPanel.addView(
                view,
                LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            )
        }
    }
}
