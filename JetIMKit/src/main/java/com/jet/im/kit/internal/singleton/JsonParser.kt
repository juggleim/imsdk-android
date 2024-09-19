package com.jet.im.kit.internal.singleton

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement

internal object JsonParser {
    private val json by lazy {
        Json {
            ignoreUnknownKeys = true
        }
    }

    @JvmStatic
    inline fun <reified T> fromJson(value: String): T {
        return json.decodeFromString(value)
    }

    @JvmStatic
    inline fun <reified T> toJsonString(value: T): String {
        return json.encodeToString(value)
    }

    @JvmStatic
    inline fun <reified T> toJsonElement(value: T): JsonElement {
        return json.encodeToJsonElement(value)
    }
}
