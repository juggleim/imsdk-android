package com.jet.im.kit.model

import android.content.Context
import android.content.res.Resources.NotFoundException
import android.graphics.Typeface
import android.os.Parcelable
import android.text.Spannable
import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.TextAppearanceSpan
import android.text.style.TypefaceSpan
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.FontRes
import androidx.annotation.StyleRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.jet.im.kit.internal.model.TypefaceSpanEx
import kotlinx.parcelize.Parcelize

/**
 * A configurations of text.
 * This provides a way of applying [com.google.android.material.resources.TextAppearance] into this class's properties.
 * A background color property is not applied automatically because background attributes is not a property of [com.google.android.material.resources.TextAppearance].
 */
@Parcelize
open class TextUIConfig private constructor(
    /**
     * Returns a value of text color int.
     *
     * @return A text color int value.
     * @since 3.0.0
     */
    @ColorInt
    var textColor: Int,
    /**
     * Returns a value of background color int.
     *
     * @return A background color int value.
     * @since 3.0.0
     */
    @ColorInt
    var textBackgroundColor: Int,
    /**
     * Returns a value of [android.graphics.Typeface].
     *  * Typeface.NORMAL
     *  * Typeface.BOLD
     *  * Typeface.ITALIC
     *  * Typeface.BOLD_ITALIC
     *
     * @return A text style of text.
     * @since 3.0.0
     */
    var textStyle: Int,
    /**
     * Returns a value of text size int.
     *
     * @return A text size int value.
     * @since 3.1.1
     */
    // pixel size
    var textSize: Int,
    /**
     * Returns a value of text typeface family.
     *
     * @return A typeface family name value.
     * @since 3.1.1
     */
    var familyName: String?,
    /**
     * Returns a custom font res ID.
     *
     * @return A custom font resource ID.
     * @since 3.2.1
     */
    @FontRes
    var customFontRes: Int
) : Parcelable {

    /**
     * Apply values in the given [TextUIConfig] into this.
     *
     * @param config A [TextUIConfig] to apply.
     * @return This TextUIConfig object that applied with given data.
     * @since 3.0.0
     */
    fun apply(config: TextUIConfig): TextUIConfig {
        if (config.textBackgroundColor != UNDEFINED_RESOURCE_ID) {
            textBackgroundColor = config.textBackgroundColor
        }
        if (config.textColor != UNDEFINED_RESOURCE_ID) {
            textColor = config.textColor
        }
        if (config.textStyle != UNDEFINED_RESOURCE_ID) {
            textStyle = config.textStyle
        }
        if (config.textSize != UNDEFINED_RESOURCE_ID) {
            textSize = config.textSize
        }
        if (config.familyName != null) {
            familyName = config.familyName
        }
        if (config.customFontRes != UNDEFINED_RESOURCE_ID) {
            customFontRes = config.customFontRes
        }
        return this
    }

    /**
     * Apply values into given whole text.
     *
     * @param text A text to apply.
     * @since 3.2.1
     */
    fun apply(context: Context, text: String): SpannableString {
        val spannableString = SpannableString(text)
        bind(context, spannableString, 0, text.length)
        return spannableString
    }

    /**
     * Apply values into Spannable text.
     *
     * @param spannable A spannable text to apply.
     * @param start A starting position to apply value.
     * @param end An end position to apply value.
     * @since 3.0.0
     */
    fun bind(context: Context, spannable: Spannable, start: Int, end: Int) {
        if (textBackgroundColor != UNDEFINED_RESOURCE_ID) {
            spannable.setSpan(BackgroundColorSpan(textBackgroundColor), start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        }
        if (textColor != UNDEFINED_RESOURCE_ID) {
            spannable.setSpan(ForegroundColorSpan(textColor), start, end, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        // typeface style starts from 0.(Typeface.Normal = 0)
        if (textStyle != UNDEFINED_RESOURCE_ID) {
            spannable.setSpan(StyleSpan(textStyle), start, end, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        if (textSize != UNDEFINED_RESOURCE_ID) {
            spannable.setSpan(AbsoluteSizeSpan(textSize), start, end, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        if (familyName != null) {
            spannable.setSpan(TypefaceSpan(familyName), start, end, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        if (customFontRes != UNDEFINED_RESOURCE_ID) {
            try {
                ResourcesCompat.getFont(context, customFontRes)?.let {
                    spannable.setSpan(
                        TypefaceSpanEx(familyName ?: "", it),
                        start,
                        end,
                        SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            } catch (ignore: NotFoundException) {
            }
        }
    }

    /**
     * Merge attributes into this instance's values by given [com.google.android.material.resources.TextAppearance] and background color.
     * @param context the context of view.
     * @param textAppearance A TextAppearance to apply.
     * @param textBackgroundColor A background color to apply.
     * @since 3.0.0
     */
    // A Background attribute of text is not included in TextAppearance.
    @JvmOverloads
    fun mergeFromTextAppearance(
        context: Context,
        @StyleRes
        textAppearance: Int,
        @ColorRes
        textBackgroundColor: Int = 0
    ) {
        if (this.textBackgroundColor == UNDEFINED_RESOURCE_ID && textBackgroundColor != 0) {
            this.textBackgroundColor = ContextCompat.getColor(context, textBackgroundColor)
        }
        if (textAppearance == 0) return
        val span = TextAppearanceSpan(context, textAppearance)
        if (textSize == UNDEFINED_RESOURCE_ID) {
            textSize = span.textSize
        }
        if (textStyle == UNDEFINED_RESOURCE_ID) {
            textStyle = span.textStyle
        }
        if (textColor == UNDEFINED_RESOURCE_ID) {
            textColor = if (span.textColor != null) span.textColor.defaultColor else UNDEFINED_RESOURCE_ID
        }
        if (familyName == null) {
            familyName = span.family
        }
    }

    /**
     * Generates typeface from text style.
     *
     * @return The typeface instance
     * @since 3.1.1
     */
    fun generateTypeface(): Typeface {
        val familyName = familyName ?: ""
        var typeface = Typeface.create(familyName, Typeface.NORMAL)
        if (textStyle >= 0) {
            when (textStyle) {
                Typeface.NORMAL -> typeface = Typeface.create(familyName, Typeface.NORMAL)
                Typeface.BOLD -> typeface = Typeface.create(familyName, Typeface.BOLD)
                Typeface.ITALIC -> typeface = Typeface.create(familyName, Typeface.ITALIC)
                Typeface.BOLD_ITALIC -> typeface = Typeface.create(familyName, Typeface.BOLD_ITALIC)
            }
        }
        return typeface
    }

    class Builder {
        @ColorInt
        private var textBackgroundColor = UNDEFINED_RESOURCE_ID

        @ColorInt
        private var textColor = UNDEFINED_RESOURCE_ID
        private var textStyle = UNDEFINED_RESOURCE_ID

        // pixel size
        private var textSize = UNDEFINED_RESOURCE_ID
        private var familyName: String? = null

        @FontRes
        private var customFontRes = UNDEFINED_RESOURCE_ID

        /**
         * Constructor
         *
         * @since 3.1.1
         */
        constructor()

        /**
         * Constructor
         *
         * @param context The context for text appearance
         * @param textAppearanceRes The value of text appearance resource
         * @since 3.1.1
         */
        constructor(context: Context, @StyleRes textAppearanceRes: Int) {
            if (textAppearanceRes != 0) {
                val span = TextAppearanceSpan(context, textAppearanceRes)
                textColor = if (span.textColor != null) span.textColor.defaultColor else UNDEFINED_RESOURCE_ID
                textStyle = if (span.textStyle != 0) span.textStyle else UNDEFINED_RESOURCE_ID
                textSize = span.textSize
                familyName = span.family
            }
        }

        /**
         * Sets the background color of text
         *
         * @param textBackgroundColor The value of background color int
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 3.1.1
         */
        fun setTextBackgroundColor(@ColorInt textBackgroundColor: Int): Builder {
            this.textBackgroundColor = textBackgroundColor
            return this
        }

        /**
         * Sets the color of text
         *
         * @param textColor The value of text color int
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 3.1.1
         */
        fun setTextColor(@ColorInt textColor: Int): Builder {
            this.textColor = textColor
            return this
        }

        /**
         * Sets a value of [android.graphics.Typeface].
         *  * Typeface.NORMAL
         *  * Typeface.BOLD
         *  * Typeface.ITALIC
         *  * Typeface.BOLD_ITALIC
         *
         * @param textStyle The value of text style
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 3.1.1
         */
        fun setTextStyle(textStyle: Int): Builder {
            this.textStyle = textStyle
            return this
        }

        /**
         * Sets the size of text
         *
         * @param textSize The value of pixel size
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 3.1.1
         */
        fun setTextSize(textSize: Int): Builder {
            this.textSize = textSize
            return this
        }

        /**
         * Sets the typeface family name of text
         *
         * @param familyName The value of typeface family name
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 3.1.1
         */
        fun setFamilyName(familyName: String?): Builder {
            this.familyName = familyName
            return this
        }

        /**
         * Sets the custom font resource id of text
         *
         * @param customFontRes The value of custom font
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 3.2.1
         */
        fun setCustomFontRes(@FontRes customFontRes: Int): Builder {
            this.customFontRes = customFontRes
            return this
        }

        /**
         * Builds an [TextUIConfig] with the properties supplied to this builder.
         *
         * @return The [TextUIConfig] from this builder instance.
         * @since 3.1.1
         */
        fun build(): TextUIConfig {
            return TextUIConfig(textColor, textBackgroundColor, textStyle, textSize, familyName, customFontRes)
        }
    }

    companion object {
        const val UNDEFINED_RESOURCE_ID = -1
    }
}
