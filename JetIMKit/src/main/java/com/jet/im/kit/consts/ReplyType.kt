package com.jet.im.kit.consts

/**
 * Represents how to display replies in message list.
 *
 * @since 2.2.0
 */
enum class ReplyType(val value: String) {
    /**
     * Do not display replies in the message list.
     *
     * @since 2.2.0
     */
    NONE(StringSet.none);
    companion object {
        /**
         * Convert to ReplyType that matches the given value.
         *
         * @param value the text value of the ReplyType.
         * @return the [ReplyType]
         * @since 3.6.0
         */
        @JvmStatic
        fun from(value: String): ReplyType {
            return values().firstOrNull { it.value == value } ?: NONE
        }
    }
}
