package com.jet.im.kit.interfaces

import com.sendbird.android.message.BaseMessage
import com.sendbird.android.message.UserMessage
import com.jet.im.kit.model.MessageDisplayData
import com.jet.im.kit.model.UserMessageDisplayData
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

interface MessageDisplayDataGenerator<O> {
    /**
     * Processes and generates the information of the message.
     * This method is executed in the adapter that renders the message list, before items are changed.
     *
     * @param messages the message to be displayed data
     * @return the displayed data
     * @since 3.5.7
     */
    fun generate(messages: List<BaseMessage>): O
}

interface UserMessageDisplayDataGenerator :
    MessageDisplayDataGenerator<Map<BaseMessage, UserMessageDisplayData>>

/**
 * This interface provides the functionality to process and generate message information
 * during the sending or rendering of messages in the UIKit.
 *
 * @since 3.5.7
 */
abstract class MessageDisplayDataProvider {
    /**
     * Returns the [MessageDisplayDataGenerator] to process and generate the information of the message when rendering a user message.
     * If the return value is null, do not use the generator.
     *
     * @return the [MessageDisplayDataGenerator] to process and generate the information of the message
     * @since 3.5.7
     */
    var userMessageDisplayDataGenerator: UserMessageDisplayDataGenerator? = null

    internal fun generate(messages: List<BaseMessage>): Map<BaseMessage, MessageDisplayData> {
        val messageDisplayDataMap = mutableMapOf<BaseMessage, MessageDisplayData>()
        userMessageDisplayDataGenerator?.let { generator ->
            messages.filterIsInstance<UserMessage>().apply {
                messageDisplayDataMap.putAll(generator.generate(this))
            }
        }

        return messageDisplayDataMap
    }

    /**
     * Returns whether the [generate] method should be run on a UI thread.
     *
     * @return true if the [generate] method should be run on a UI thread, false otherwise
     * @since 3.5.7
     */
    open fun shouldRunOnUIThread(): Boolean = true

    @JvmName("threadPool")
    internal fun threadPool(): ExecutorService = Executors.newSingleThreadExecutor()
}
