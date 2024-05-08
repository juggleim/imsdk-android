package com.jet.im.kit.model

import com.sendbird.android.message.BaseMessage
import com.jet.im.kit.log.Logger
import com.jet.im.kit.utils.DateUtils
import java.util.TreeSet
import java.util.concurrent.ConcurrentHashMap

internal class MessageList @JvmOverloads constructor(private val order: Order = Order.DESC) {
    enum class Order {
        ASC, DESC
    }

    private val messages: TreeSet<BaseMessage> = TreeSet { o1: BaseMessage, o2: BaseMessage ->
        if (o1.createdAt > o2.createdAt) {
            return@TreeSet if (order == Order.DESC) -1 else 1
        } else if (o1.createdAt < o2.createdAt) {
            return@TreeSet if (order == Order.DESC) 1 else -1
        }
        0
    }

    private val timelineMap: MutableMap<String, BaseMessage> = ConcurrentHashMap()

    /**
     * @return the latest message.
     */
    val latestMessage: BaseMessage?
        get() {
            if (messages.isEmpty()) return null
            return if (order == Order.DESC) messages.first() else messages.last()
        }

    /**
     * @return the oldest message.
     */
    val oldestMessage: BaseMessage?
        get() {
            if (messages.isEmpty()) return null
            return if (order == Order.DESC) messages.last() else messages.first()
        }

    val size: Int
        @JvmName("size") // TODO : remove it if there is no place to use it on the java-side.
        get() = messages.size

    fun toList(): MutableList<BaseMessage> {
        return messages.toMutableList()
    }

    @Synchronized
    fun clear() {
        messages.clear()
        timelineMap.clear()
    }

    @Synchronized
    fun add(message: BaseMessage) {
        Logger.d(">> MessageList::addAll()")
        val createdAt = message.createdAt
        val dateStr = DateUtils.getDateString(createdAt)
        var timeline = timelineMap[dateStr]
        // create new timeline message if not exists
        if (timeline == null) {
            timeline = createTimelineMessage(message)
            messages.add(timeline)
            timelineMap[dateStr] = timeline
            messages.remove(message)
            BaseMessage.clone(message)?.let { messages.add(it) }
            return
        }

        // remove previous timeline message if it exists.
        val timelineCreatedAt = timeline.createdAt
        if (timelineCreatedAt > createdAt) {
            messages.remove(timeline)
            val newTimeline = createTimelineMessage(message)
            timelineMap[dateStr] = newTimeline
            messages.add(newTimeline)
        }
        messages.remove(message)
        BaseMessage.clone(message)?.let { messages.add(it) }
    }

    fun addAll(messages: List<BaseMessage>) {
        Logger.d(">> MessageList::addAll()")
        if (messages.isEmpty()) return
        messages.forEach { add(it) }
    }

    @Synchronized
    fun delete(message: BaseMessage): Boolean {
        Logger.d(">> MessageList::deleteMessage()")
        val removed = messages.remove(message)
        if (removed) {
            val createdAt = message.createdAt
            val dateStr = DateUtils.getDateString(createdAt)
            val timeline = timelineMap[dateStr] ?: return true

            // check below item.
            val lower = messages.lower(message)
            if (lower != null && DateUtils.hasSameDate(createdAt, lower.createdAt)) {
                return true
            }

            // check above item.
            val higher = messages.higher(message)
            if (higher != null && DateUtils.hasSameDate(createdAt, higher.createdAt)) {
                if (timeline != higher) {
                    return true
                }
            }
            if (timelineMap.remove(dateStr) != null) {
                messages.remove(timeline)
            }
        }
        return removed
    }

    fun deleteAll(messages: List<BaseMessage>) {
        Logger.d(">> MessageList::deleteAllMessages() size = %s", messages.size)
        messages.forEach { delete(it) }
    }

    @Synchronized
    fun deleteByMessageId(msgId: Long): BaseMessage? {
        return messages.find { it.messageId == msgId }?.also { delete(it) }
    }

    @Synchronized
    fun update(message: BaseMessage) {
        Logger.d(">> MessageList::updateMessage()")
        if (messages.remove(message)) {
            BaseMessage.clone(message)?.let { messages.add(it) }
        }
    }

    fun updateAll(messages: List<BaseMessage>) {
        Logger.d(">> MessageList::updateAllMessages() size=%s", messages.size)
        messages.forEach { update(it) }
    }

    @Synchronized
    fun getById(messageId: Long): BaseMessage? {
        return messages.find { it.messageId == messageId }
    }

    @Synchronized
    fun getByCreatedAt(createdAt: Long): List<BaseMessage> {
        if (createdAt == 0L) return emptyList()
        return messages.filter { it.createdAt == createdAt }
    }

    companion object {
        private fun createTimelineMessage(anchorMessage: BaseMessage): BaseMessage {
            return TimelineMessage(anchorMessage)
        }
    }
}
