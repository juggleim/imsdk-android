package com.jet.im.kit.model

import android.content.Context
import android.content.res.Resources.NotFoundException
import android.text.TextPaint
import android.text.style.MetricAffectingSpan
import androidx.core.content.res.ResourcesCompat
import com.sendbird.android.message.MentionType
import com.sendbird.android.user.User

/**
 * The text with a MentionSpan attached will be bold and clickable.
 * MentionSpan provides the User data that is relevant to the marked-up text.
 *
 * @since 3.0.0
 */
class MentionSpan
/**
 * Constructor
 *
 * @param context The `Context` this spannable is currently associated with
 * @param mentionType The type of mention to be applied for this mention-spanned text
 * @param trigger The text to trigger mention
 * @param value The text to be mentioned
 * @param mentionedUser The User relevant to this mention-spanned text
 * @param uiConfig The mention ui config.
 * @param mentionedCurrentUserUIConfig The mention ui config if current user is mentioned
 * @since 3.0.0
 */
@JvmOverloads constructor(
    private val context: Context,
    private val mentionType: MentionType = MentionType.USERS,
    /**
     * Returns the trigger text.
     *
     * @return The trigger text of this spanned text
     * @since 3.0.0
     */
    val trigger: String,
    /**
     * Returns the mentioned text.
     *
     * @return The mentioned text of this spanned text
     * @since 3.0.0
     */
    val value: String,
    /**
     * Returns the User relevant to this spanned text
     *
     * @return The User data relevant to this markup object
     * @since 3.0.0
     */
    val mentionedUser: User,
    private val uiConfig: TextUIConfig,
    private val mentionedCurrentUserUIConfig: TextUIConfig? = null
) : MetricAffectingSpan() {

    /**
     * Constructor
     *
     * @param context The `Context` this spannable is currently associated with
     * @param trigger The text to trigger mention
     * @param value The text to be mentioned
     * @param mentionedUser The User relevant to this mention-spanned text
     * @param uiConfig The mention ui config.
     * @param mentionedCurrentUserUIConfig The mention ui config if current user is mentioned
     * @since 3.0.0
     */
    constructor(
        context: Context,
        trigger: String,
        value: String,
        mentionedUser: User,
        uiConfig: TextUIConfig,
        mentionedCurrentUserUIConfig: TextUIConfig?
    ) : this(context, MentionType.USERS, trigger, value, mentionedUser, uiConfig, mentionedCurrentUserUIConfig)

    override fun updateDrawState(paint: TextPaint) {
        applyMentionTextPaint(context, uiConfig, mentionedCurrentUserUIConfig, paint)
    }

    override fun updateMeasureState(paint: TextPaint) {
        applyMentionTextPaint(context, uiConfig, mentionedCurrentUserUIConfig, paint)
    }

    val displayText: String
        /**
         * Returns the text to be displayed by combining trigger and value.
         *
         * @return The text to be displayed from this markup object
         * @since 3.0.0
         */
        get() = trigger + value

    val templateText: String
        /**
         * Returns the template text to be used as a mention data.
         *
         * @return The text to be used as a mention data from this markup object
         * @since 3.0.0
         */
        get() = trigger + "{" + mentionedUser.userId + "}"

    /**
     * Returns the length of the text to be displayed.
     *
     * @return The length of the displayed text of this markup object
     * @since 3.0.0
     */
    val length: Int
        get() = displayText.length

    companion object {
        private fun applyMentionTextPaint(
            context: Context,
            uiConfig: TextUIConfig,
            mentionedCurrentUserUIConfig: TextUIConfig?,
            to: TextPaint
        ) {
            apply(context, uiConfig, to)
            if (mentionedCurrentUserUIConfig != null) {
                // if mentioned current user exists, this color has priority.
                apply(context, mentionedCurrentUserUIConfig, to)
            }
            to.isUnderlineText = false
        }

        private fun apply(context: Context, from: TextUIConfig, to: TextPaint) {
            if (from.textColor != TextUIConfig.UNDEFINED_RESOURCE_ID) {
                to.color = from.textColor
            }
            if (from.textStyle != TextUIConfig.UNDEFINED_RESOURCE_ID) {
                to.typeface = from.generateTypeface()
            }
            if (from.textSize != TextUIConfig.UNDEFINED_RESOURCE_ID) {
                to.textSize = from.textSize.toFloat()
            }
            if (from.textBackgroundColor != TextUIConfig.UNDEFINED_RESOURCE_ID) {
                to.bgColor = from.textBackgroundColor
            }
            if (from.customFontRes != TextUIConfig.UNDEFINED_RESOURCE_ID) {
                try {
                    val font = ResourcesCompat.getFont(context, from.customFontRes)
                    if (font != null) {
                        to.typeface = font
                    }
                } catch (ignore: NotFoundException) {
                }
            }
        }
    }
}
