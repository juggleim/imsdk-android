package com.jet.im.kit.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class DialogListItem

/**
 * A Single item of selectable dialog list.
 *
 * @param key the resource identifier of the string resource to be displayed.
 * @param icon Resource identifier of the icon Drawable.
 * @param isAlert Determine whether the item text uses an error color. If it sets <code>true</code>, the text color will be shown as an error color.
 * @param isDisabled Determine whether to disable the item.
 */
@JvmOverloads constructor(
    /**
     * Returns a key of item.
     *
     * @return String resource id.
     */
    @StringRes
    val key: Int,

    /**
     * Returns an icon of item.
     *
     * @return Drawable resource id.
     */
    @DrawableRes
    val icon: Int = 0,

    /**
     * Returns the item text uses error color.
     *
     * @return `true` if the text color uses error color, `false` otherwise.
     */
    val isAlert: Boolean = false,

    /**
     * Returns the item is disabled.
     *
     * @return `true` if the item is disabled, `false` otherwise.
     */
    val isDisabled: Boolean = false
)
