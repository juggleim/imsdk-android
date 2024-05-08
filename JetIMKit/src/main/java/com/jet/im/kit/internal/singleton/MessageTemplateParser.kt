package com.jet.im.kit.internal.singleton

import com.jet.im.kit.internal.model.template_messages.KeySet
import com.jet.im.kit.internal.model.template_messages.Params
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import org.json.JSONObject

internal object MessageTemplateParser {
    private val json by lazy {
        Json {
            prettyPrint = true
            ignoreUnknownKeys = true

            // https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/json.md#coercing-input-values
            // coerceInputValues = true

            // https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/json.md#encoding-defaults
            // encodeDefaults = true
        }
    }

    @JvmStatic
    fun parseParams(el: JsonElement): Params {
        return json.decodeFromJsonElement(el)
    }

    @JvmStatic
    fun parseParams(jsonStr: String): Params {
        return json.decodeFromString(jsonStr)
    }

    @JvmStatic
    fun parseToMap(jsonStr: String): Map<String, String> {
        return json.decodeFromString(jsonStr)
    }

    @JvmStatic
    @Throws(Exception::class)
    fun parse(jsonTemplate: String): Params {
        return when (val version = JSONObject(jsonTemplate).getInt(KeySet.version)) {
            1 -> parseParams(jsonTemplate)
            else -> throw RuntimeException("unsupported version. current version = $version")
        }
    }
}
