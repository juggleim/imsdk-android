package com.jet.im.kit.internal.extensions

import android.content.res.Resources
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

// int value is a pure number value. For example it makes 10 and 10DP equal.
internal fun Resources.intToDp(value: Int): Int {
    return (value * displayMetrics.density + 0.5f).toInt()
}

@Throws(JSONException::class)
fun JSONObject.toStringMap(): Map<String, String> {
    val map = mutableMapOf<String, String>()
    val keysItr: Iterator<String> = this.keys()
    while (keysItr.hasNext()) {
        val key = keysItr.next()
        map[key] = this.getString(key)
    }
    return map
}

inline fun <reified T> JSONArray.toList(): List<T> {
    val list = mutableListOf<T>()
    for (i in 0 until length()) {
        list.add(get(i) as T)
    }
    return list
}
