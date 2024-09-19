package com.jet.im.kit.consts

/**
 * Represents where to go when selecting a reply.
 * This type is only available for `ReplyType.THREAD` type.
 *
 * @since 3.3.0
 */
enum class ThreadReplySelectType(val value: String) {
    /**
     * Moves to parent message.
     *
     * @since 3.3.0
     */
    PARENT(StringSet.parent),

    /**
     * Moves to thread page.
     *
     * @since 3.3.0
     */
    THREAD(StringSet.thread);

    companion object {
        /**
         * Convert to ReplyType that matches the given value.
         *
         * @param value the text value of the ReplyType.
         * @return the [ReplyType]
         * @since 3.6.0
         */
        @JvmStatic
        fun from(value: String): ThreadReplySelectType {
            return values().firstOrNull { it.value == value } ?: THREAD
        }
    }
}
