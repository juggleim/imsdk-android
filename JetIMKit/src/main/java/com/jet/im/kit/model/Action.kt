package com.jet.im.kit.model

import com.jet.im.kit.internal.model.template_messages.ActionData
import java.util.Locale

/**
 * Custom action data to be linked to a custom message.
 * This action data is delivered and used as it is.
 *
 * @since 3.5.0
 */
data class Action
/**
 * Constructor that is used only internally.
 *
 * @param type an action data type.
 * @param data a data delivered and used as it is
 * @param alterData an alternative data that can be used if data is not available
 * @since 3.5.0
 */
internal constructor(
    /**
     * Returns the type of Action.
     * "web", "custom", and "uikit" are available.
     *
     * @return the type of Action.
     * @since 3.5.0
     */
    @JvmField val type: String,
    /**
     * Returns action data that associated with the view.
     *
     * @return the action data associated with the view.
     * @since 3.5.0
     */
    @JvmField val data: String,
    /**
     * Alternative data that can be used if data is not available
     *
     * @return the alternative data that can be used if data is not available
     * @since 3.5.0
     */
    @JvmField val alterData: String?
) {

    companion object {
        /**
         * Convert ActionData to Action class. This is used only for internal.
         *
         * @param actionData The data from the given custom data filed.
         * @return Action data.
         * @since 3.5.0
         */
        @JvmStatic
        internal fun from(actionData: ActionData): Action {
            return Action(actionData.type.name.lowercase(Locale.getDefault()), actionData.data, actionData.alterData)
        }
    }
}
