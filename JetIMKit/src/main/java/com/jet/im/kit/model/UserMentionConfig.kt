package com.jet.im.kit.model

import com.jet.im.kit.consts.StringSet

/**
 * Describes a configuration of mention for user.
 *
 * @since 3.0.0
 */
class UserMentionConfig private constructor(
    /**
     * Returns trigger text for mention.
     *
     * @return A text of trigger.
     * @since 3.0.0
     */
    val trigger: String,
    /**
     * Returns max mention count.
     * Up to 10 users mentioned in the message will be notified.
     *
     * @return A mentioned user counts to be able to mention.
     * @since 3.0.0
     */
    val maxMentionCount: Int
) : MentionConfig() {

    /**
     * Returns maximum suggestion count.
     *
     * @return Maximum suggestion count.
     * @since 3.0.0
     */
    var maxSuggestionCount = 0
        private set

    /**
     * Returns the time from now to delay execution.
     *
     * @return The time from now to delay execution
     * @since 3.0.0
     */
    var debounceTime: Long = 0
        private set

    /**
     * Returns mention delimiter string.
     *
     * @return mention delimiter string.
     * @since 3.0.0
     */
    lateinit var delimiter: String
        private set

    class Builder {
        private var maxMentionCount = 10
        private var maxSuggestionCount = 15

        /**
         * Sets maximum mention count.
         *
         * @param maxMentionCount maximum mention count
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 3.0.0
         */
        fun setMaxMentionCount(maxMentionCount: Int): Builder {
            this.maxMentionCount = maxMentionCount
            return this
        }

        /**
         * Sets the maximum user suggestion count.
         * It can be set up to 15.
         *
         * @param maxSuggestionCount Maximum user suggestion count.
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 3.0.0
         */
        fun setMaxSuggestionCount(maxSuggestionCount: Int): Builder {
            this.maxSuggestionCount = maxSuggestionCount.coerceAtMost(15)
            return this
        }

        /**
         * Creates an [UserMentionConfig] with the arguments supplied to this builder.
         *
         * @return The [UserMentionConfig].
         * @since 3.0.0
         */
        fun build(): UserMentionConfig {
            val params = UserMentionConfig(StringSet._AT, maxMentionCount)
            params.delimiter = " "
            params.maxSuggestionCount = maxSuggestionCount
            params.debounceTime = 300L
            return params
        }
    }
}
