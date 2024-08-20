package com.jet.im.kit.model

import com.sendbird.android.message.BaseMessage
import com.jet.im.kit.log.Logger
import com.jet.im.kit.utils.DateUtils
import com.juggle.im.model.Message
import java.util.TreeSet
import java.util.concurrent.ConcurrentHashMap

internal class MessageList @JvmOverloads constructor(private val order: Order = Order.DESC) {
    enum class Order {
        ASC, DESC
    }

    private val messages: TreeSet<Message> = TreeSet { o1: Message, o2: Message ->
        if (o1.timestamp > o2.timestamp) {
            return@TreeSet if (order == Order.DESC) -1 else 1
        } else if (o1.timestamp < o2.timestamp) {
            return@TreeSet if (order == Order.DESC) 1 else -1
        }
        0
    }

    private val timelineMap: MutableMap<String, Message> = ConcurrentHashMap()

    /**
     * @return the latest message.
     */
    val latestMessage: Message?
        get() {
            if (messages.isEmpty()) return null
            return if (order == Order.DESC) messages.first() else messages.last()
        }

    /**
     * @return the oldest message.
     */
    val oldestMessage: Message?
        get() {
            if (messages.isEmpty()) return null
            return if (order == Order.DESC) messages.last() else messages.first()
        }

    val size: Int
        @JvmName("size") // TODO : remove it if there is no place to use it on the java-side.
        get() = messages.size

    fun toList(): MutableList<Message> {
        return messages.toMutableList()
    }

    @Synchronized
    fun clear() {
        messages.clear()
        timelineMap.clear()
    }

    @Synchronized
    fun add(message: Message) {
        Logger.d(">> MessageList::addAll()")
        val createdAt = message.timestamp
        val dateStr = DateUtils.getDateString(createdAt)
        var timeline = timelineMap[dateStr]
        // create new timeline message if not exists
        if (timeline == null) {
            timeline = createTimelineMessage(message)
            messages.add(timeline)
            timelineMap[dateStr] = timeline
            messages.remove(message)
            message.let { messages.add(it) }
            return
        }

        // remove previous timeline message if it exists.
        val timelineCreatedAt = timeline.timestamp
        if (timelineCreatedAt > createdAt) {
            messages.remove(timeline)
            val newTimeline = createTimelineMessage(message)
            timelineMap[dateStr] = newTimeline
            messages.add(newTimeline)
        }
        messages.remove(message)
        message?.let { messages.add(it) }
    }

    fun addAll(messages: List<Message>) {
        Logger.d(">> MessageList::addAll()")
        if (messages.isEmpty()) return
        messages.forEach { add(it) }
    }

    @Synchronized
    fun delete(message: Message): Boolean {
        Logger.d(">> MessageList::deleteMessage()")
        val removed = messages.remove(message)
        if (removed) {
            val createdAt = message.timestamp
            val dateStr = DateUtils.getDateString(createdAt)
            val timeline = timelineMap[dateStr] ?: return true

            // check below item.
            val lower = messages.lower(message)
            if (lower != null && DateUtils.hasSameDate(createdAt, lower.timestamp)) {
                return true
            }

            // check above item.
            val higher = messages.higher(message)
            if (higher != null && DateUtils.hasSameDate(createdAt, higher.timestamp)) {
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

    fun deleteAll(messages: List<Message>) {
        Logger.d(">> MessageList::deleteAllMessages() size = %s", messages.size)
        messages.forEach { delete(it) }
    }

    @Synchronized
    fun deleteByMessageId(msgId: Long): Message? {
        return messages.find { it.clientMsgNo == msgId }?.also { delete(it) }
    }

    @Synchronized
    fun update(message: Message) {
        Logger.d(">> MessageList::updateMessage()")
        if (messages.remove(message)) {
            message?.let { messages.add(it) }
        }
    }

    fun updateAll(messages: List<Message>) {
        Logger.d(">> MessageList::updateAllMessages() size=%s", messages.size)
        messages.forEach { update(it) }
    }

    @Synchronized
    fun getById(messageId: Long): Message? {
        return messages.find { it.clientMsgNo == messageId }
    }

    @Synchronized
    fun getByCreatedAt(createdAt: Long): List<Message> {
        if (createdAt == 0L) return emptyList()
        return messages.filter { it.timestamp == createdAt }
    }

    companion object {
        private fun createTimelineMessage(anchorMessage: Message): Message {
            var timelineMessage = Message()
            timelineMessage.content = TimelineMessage(anchorMessage.timestamp)
            timelineMessage.timestamp=anchorMessage.timestamp
            return timelineMessage
        }
    }
}
