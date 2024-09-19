package com.jet.im.kit.model

import android.util.Base64
import com.sendbird.android.message.Emoji
import com.sendbird.android.message.EmojiCategory
import com.sendbird.android.message.EmojiContainer
import com.jet.im.kit.consts.StringSet
import com.jet.im.kit.utils.UIKitPrefs

/**
 * Manager providing emoji information from Sendbird server.
 *
 * @since 1.1.0
 */
object EmojiManager {
    /**
     * Returns the emoji hash
     *
     * @return The emoji hash
     * @since 1.1.0
     */
    @JvmStatic
    var emojiHash: String? = null
        private set
    private val emojiLock = Any()
    private var emojiCategoryMap = LinkedHashMap<Long, EmojiCategory>()
    private var allEmojiMap = LinkedHashMap<String, Emoji>()

    @JvmStatic
    fun init() {
        val emojiContainerStr = UIKitPrefs.getString(StringSet.KEY_EMOJI_CONTAINER)
        if (emojiContainerStr.isNotEmpty()) {
            decodeEmojiContainer(emojiContainerStr)?.let {
                upsertEmojiContainer(it, false)
            }
        }
    }

    @JvmStatic
    fun upsertEmojiContainer(emojiContainer: EmojiContainer) {
        upsertEmojiContainer(emojiContainer, true)
    }

    private fun upsertEmojiContainer(emojiContainer: EmojiContainer, saveToFile: Boolean) {
        emojiHash = emojiContainer.emojiHash
        synchronized(emojiLock) {
            emojiCategoryMap = LinkedHashMap()
            allEmojiMap = LinkedHashMap()
            emojiContainer.emojiCategories.forEach {
                emojiCategoryMap[it.id] = it
                it.emojis.forEach { emoji ->
                    allEmojiMap[emoji.key] = emoji
                }
            }
        }
        if (saveToFile) {
            val emojiContainerSerialized: String = encodeEmojiContainer(emojiContainer)
            UIKitPrefs.putString(StringSet.KEY_EMOJI_CONTAINER, emojiContainerSerialized)
        }
    }

    /**
     * Returns the emoji url corresponding to emoji key
     *
     * @param key emoji key
     * @return The emoji url corresponding to emoji key
     * @since 1.1.0
     */
    @JvmStatic
    fun getEmojiUrl(key: String): String? {
        return synchronized(emojiLock) {
            allEmojiMap[key]?.url
        }
    }

    @JvmStatic
    val allEmojiCategories: List<EmojiCategory>
        /**
         * Returns the list of Emoji registering Sendbird server
         *
         * @return The list of Emoji category list
         * @since 1.1.0
         */
        get() = emojiCategoryMap.values.toList()

    @JvmStatic
    val allEmojis: List<Emoji>
        /**
         * Returns the list of Emoji registering Sendbird server
         *
         * @return The list of Emoji registering Sendbird server
         * @since 1.1.0
         */
        get() = allEmojiMap.values.toList()

    /**
     * Returns the [List] corresponding to emoji category id
     *
     * @param emojiCategoryId Emoji category id
     * @return The [List] corresponding to emoji category id
     * @since 1.1.0
     */
    @JvmStatic
    fun getEmojis(emojiCategoryId: Long): List<Emoji>? {
        return synchronized(emojiLock) {
            emojiCategoryMap[emojiCategoryId]?.emojis?.toList()
        }
    }

    private fun encodeEmojiContainer(container: EmojiContainer): String {
        return Base64.encodeToString(container.serialize(), Base64.DEFAULT)
    }

    private fun decodeEmojiContainer(data: String): EmojiContainer? {
        val array = Base64.decode(data, Base64.DEFAULT)
        return EmojiContainer.buildFromSerializedData(array)
    }
}
