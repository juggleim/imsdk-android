package com.example.demo.basic.openchannel.livestream

import com.example.demo.common.consts.StringSet
import org.json.JSONObject

data class Creator(
    val userId: String,
    val nickname: String,
    val profileUrl: String
) {
    constructor(jsonObject: JSONObject) : this(
        jsonObject.optString(StringSet.id),
        jsonObject.optString(StringSet.name),
        jsonObject.optString(StringSet.profile_url)
    )
}

/**
 * Model class for a live streaming channel data.
 */
data class LiveStreamingChannelData(
    val name: String,
    val tags: MutableList<String>,
    val creator: Creator?,
    val thumbnailUrl: String,
    val liveUrl: String
) {
    constructor(jsonObject: JSONObject) : this(
        jsonObject.optString(StringSet.name),
        mutableListOf<String>().apply {
            val tagsJsonArray = jsonObject.optJSONArray(StringSet.tags)
            if (tagsJsonArray != null) {
                for (i in 0 until tagsJsonArray.length()) {
                    add(tagsJsonArray.opt(i).toString())
                }
            }
        },
        if (jsonObject.has(StringSet.creator_info)) Creator(jsonObject.getJSONObject(StringSet.creator_info)) else null,
        jsonObject.optString(StringSet.thumbnail_url),
        jsonObject.optString(StringSet.live_channel_url)
    )
}
