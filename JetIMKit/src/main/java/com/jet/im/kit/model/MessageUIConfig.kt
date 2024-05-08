package com.jet.im.kit.model

import android.content.res.ColorStateList
import android.graphics.drawable.Drawable

/**
 * Configurations of Message.
 */
open class MessageUIConfig {
    /**
     * Returns UI configuration of edited message that I sent.
     * If the message is edited, UIKit add an extra text, like, "(Edited)".
     * This extra text's UI configurations will be returned.
     *
     * @return the UI configuration of edited message mark.
     * @since 3.0.0
     */
    val myEditedTextMarkUIConfig: TextUIConfig = TextUIConfig.Builder().build()

    /**
     * Returns UI configuration of edited message that the others sent.
     * If the message is edited, UIKit add an extra text, like, "(Edited)".
     * This extra text's UI configurations will be returned.
     *
     * @return the UI configuration of edited message mark.
     * @since 3.0.0
     */
    val otherEditedTextMarkUIConfig: TextUIConfig = TextUIConfig.Builder().build()

    /**
     * Returns UI configuration of mentioned message that I sent.
     *
     * @return the UI configuration of mentioned message.
     * @since 3.0.0
     */
    val myMentionUIConfig: TextUIConfig = TextUIConfig.Builder().build()

    /**
     * Returns UI configuration of mentioned message that the others sent.
     *
     * @return the UI configuration of mentioned message.
     * @since 3.0.0
     */
    val otherMentionUIConfig: TextUIConfig = TextUIConfig.Builder().build()

    /**
     * Returns UI configuration of message text that I sent.
     *
     * @return the UI configuration of message text.
     * @since 3.1.1
     */
    val myMessageTextUIConfig: TextUIConfig = TextUIConfig.Builder().build()

    /**
     * Returns UI configuration of message text that the others sent.
     *
     * @return the UI configuration of message text.
     * @since 3.1.1
     */
    val otherMessageTextUIConfig: TextUIConfig = TextUIConfig.Builder().build()

    /**
     * Returns UI configuration of message sentAt text that the I sent.
     *
     * @return the UI configuration of message sentAt text.
     * @since 3.1.1
     */
    val mySentAtTextUIConfig: TextUIConfig = TextUIConfig.Builder().build()

    /**
     * Returns UI configuration of message sentAt text that the others sent.
     *
     * @return the UI configuration of message sentAt text.
     * @since 3.1.1
     */
    val otherSentAtTextUIConfig: TextUIConfig = TextUIConfig.Builder().build()

    /**
     * Returns UI configuration of sender nickname that the I sent.
     *
     * @return the UI configuration of sender nickname.
     * @since 3.1.1
     */
    val myNicknameTextUIConfig: TextUIConfig = TextUIConfig.Builder().build()

    /**
     * Returns UI configuration of sender nickname that the others sent.
     *
     * @return the UI configuration of sender nickname.
     * @since 3.1.1
     */
    val otherNicknameTextUIConfig: TextUIConfig = TextUIConfig.Builder().build()

    /**
     * Returns UI configuration of sender nickname that the operator sent.
     *
     * @return the UI configuration of sender nickname.
     * @since 3.1.1
     */
    val operatorNicknameTextUIConfig: TextUIConfig = TextUIConfig.Builder().build()

    /**
     * Returns UI configuration of replied parent message.
     *
     * @return the UI configuration of replied parent message.
     * @since 3.2.1
     */
    val repliedMessageTextUIConfig: TextUIConfig = TextUIConfig.Builder().build()

    /**
     * Returns the UI configuration of the linked text color in the message text.
     *
     * @return the UI configuration of the linked text color.
     * @since 3.1.1
     */
    var linkedTextColor: ColorStateList? = null

    /**
     * Returns UI configuration of message background that the I sent.
     *
     * @return the UI configuration of message background.
     * @since 3.1.1
     */
    var myMessageBackground: Drawable? = null

    /**
     * Returns UI configuration of message background that the others sent.
     *
     * @return the UI configuration of message background.
     * @since 3.1.1
     */
    var otherMessageBackground: Drawable? = null

    /**
     * Returns UI configuration of message reaction list background that the I sent.
     *
     * @return the UI configuration of message reaction list background.
     * @since 3.1.1
     */
    var myReactionListBackground: Drawable? = null

    /**
     * Returns UI configuration of message reaction list background that the others sent.
     *
     * @return the UI configuration of message reaction list background.
     * @since 3.1.1
     */
    var otherReactionListBackground: Drawable? = null

    /**
     * Returns UI configuration of ogtag message background that the I sent.
     *
     * @return the UI configuration of ogtag message background.
     * @since 3.1.1
     */
    var myOgtagBackground: Drawable? = null

    /**
     * Returns UI configuration of ogtag message background that the others sent.
     *
     * @return the UI configuration of ogtag message background.
     * @since 3.1.1
     */
    var otherOgtagBackground: Drawable? = null
}
