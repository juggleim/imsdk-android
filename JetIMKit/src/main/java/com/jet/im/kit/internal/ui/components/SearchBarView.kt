package com.jet.im.kit.internal.ui.components

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import android.widget.TextView
import com.jet.im.kit.R
import com.jet.im.kit.databinding.SbViewSearchBarBinding
import com.jet.im.kit.interfaces.OnInputTextChangedListener
import com.jet.im.kit.interfaces.OnSearchEventListener
import com.jet.im.kit.internal.extensions.setAppearance
import com.jet.im.kit.internal.extensions.setCursorDrawable

internal class SearchBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {
    val binding: SbViewSearchBarBinding
    var onSearchEventListener: OnSearchEventListener? = null
    var onInputTextChangedListener: OnInputTextChangedListener? = null
    var onClearButtonClickListener: OnClickListener? = null
    val layout: View
        get() = this

    val searchButton: TextView
        get() = binding.tvSearch

    fun setText(text: CharSequence?) {
        binding.etInputText.setText(text)
    }

    init {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.SearchBar, defStyle, 0)
        try {
            binding = SbViewSearchBarBinding.inflate(LayoutInflater.from(getContext()))
            addView(binding.root, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            val itemBackground = a.getResourceId(R.styleable.SearchBar_sb_search_bar_background, R.color.background_50)
            val dividerColor = a.getResourceId(R.styleable.SearchBar_sb_search_divider_color, R.color.onlight_04)
            val inputBackground = a.getResourceId(
                R.styleable.SearchBar_sb_search_bar_text_input_background,
                R.drawable.sb_shape_search_background
            )
            val textAppearance =
                a.getResourceId(R.styleable.SearchBar_sb_search_bar_text_appearance, R.style.SendbirdBody3OnLight01)
            val hintText =
                a.getResourceId(R.styleable.SearchBar_sb_search_bar_hint_text, R.string.sb_text_button_search)
            val hintTextColor = a.getColorStateList(R.styleable.SearchBar_sb_search_bar_hint_text_color)
            val clearIcon = a.getResourceId(R.styleable.SearchBar_sb_search_bar_clear_icon, R.drawable.icon_remove)
            val clearIconTintColor = a.getColorStateList(R.styleable.SearchBar_sb_search_bar_clear_icon_tint_color)
            val searchButtonText =
                a.getResourceId(R.styleable.SearchBar_sb_search_bar_search_text, R.string.sb_text_button_search)
            val searchTextAppearance = a.getResourceId(
                R.styleable.SearchBar_sb_search_bar_search_text_appearance,
                R.style.SendbirdButtonPrimary300
            )
            val searchTextColor = a.getColorStateList(R.styleable.SearchBar_sb_search_bar_search_text_color)
            val searchTextBackground = a.getResourceId(
                R.styleable.SearchBar_sb_search_bar_search_text_background,
                R.drawable.sb_button_uncontained_background_light
            )
            val cursorDrawable = a.getResourceId(
                R.styleable.SearchBar_sb_search_bar_cursor_drawable,
                R.drawable.sb_message_input_cursor_light
            )
            val searchIcon = a.getResourceId(R.styleable.SearchBar_sb_search_bar_search_icon, R.drawable.icon_search)
            val searchIconTintColor = a.getColorStateList(R.styleable.SearchBar_sb_search_bar_search_icon_tint_color)
            binding.searchBar.setBackgroundResource(itemBackground)
            binding.ivDivider.setBackgroundResource(dividerColor)
            binding.searchEditBox.setBackgroundResource(inputBackground)
            binding.etInputText.setAppearance(context, textAppearance)
            binding.etInputText.setHint(hintText)
            binding.etInputText.setHintTextColor(hintTextColor)
            binding.ivClear.setImageResource(clearIcon)
            binding.ivClear.imageTintList = clearIconTintColor
            binding.tvSearch.setText(searchButtonText)
            binding.tvSearch.setAppearance(context, searchTextAppearance)
            if (searchTextColor != null) {
                binding.tvSearch.setTextColor(searchTextColor)
            }
            binding.tvSearch.setBackgroundResource(searchTextBackground)
            binding.ivSearchIcon.setImageResource(searchIcon)
            binding.ivSearchIcon.imageTintList = searchIconTintColor
            binding.etInputText.setCursorDrawable(context, cursorDrawable)
            binding.etInputText.setOnEditorActionListener { _: TextView?, actionId: Int, _: KeyEvent? ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if (binding.etInputText.text != null) {
                        onSearchEventListener?.onSearchRequested(binding.etInputText.text?.toString() ?: "")
                        return@setOnEditorActionListener true
                    }
                }
                false
            }
            binding.etInputText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    binding.ivClear.visibility = if (count > 0) VISIBLE else GONE
                    onInputTextChangedListener?.onInputTextChanged(s, start, before, count)
                }

                override fun afterTextChanged(s: Editable) {}
            })
            binding.ivClear.setOnClickListener {
                onClearButtonClickListener?.onClick(it)
            }
            binding.tvSearch.setOnClickListener {
                val text = binding.etInputText.text
                onSearchEventListener?.onSearchRequested(text.toString())
            }
        } finally {
            a.recycle()
        }
    }
}
