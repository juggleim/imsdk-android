package com.jet.im.kit.internal.model.template_messages

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

internal object Template {
    private fun createImage(
        action: JsonObject? = null,
        width: JsonObject? = null,
        height: JsonObject? = null,
        viewStyle: JsonObject? = null,
        imageUrl: String,
        imageStyle: JsonObject? = null,
    ): JsonObject {
        return buildJsonObject {
            put(KeySet.type, KeySet.image)
            action?.let { put(KeySet.action, it) }
            width?.let { put(KeySet.width, it) }
            height?.let { put(KeySet.height, it) }
            viewStyle?.let { put(KeySet.viewStyle, it) }
            put(KeySet.imageUrl, imageUrl)
            imageStyle?.let { put(KeySet.imageStyle, it) }
        }
    }

    private fun createButton(
        action: JsonObject? = null,
        width: JsonObject? = null,
        height: JsonObject? = null,
        viewStyle: JsonObject? = null,
        text: String,
        maxTextLines: Int? = null,
        textStyle: JsonObject? = null,
    ): JsonObject {
        return buildJsonObject {
            put(KeySet.type, KeySet.textButton)
            action?.let { put(KeySet.action, it) }
            width?.let { put(KeySet.width, it) }
            height?.let { put(KeySet.height, it) }
            viewStyle?.let { put(KeySet.viewStyle, it) }
            put(KeySet.text, text)
            maxTextLines?.let { put(KeySet.maxTextLines, it) }
            textStyle?.let { put(KeySet.textStyle, it) }
        }
    }

    private fun createImageButton(
        action: JsonObject? = null,
        width: JsonObject? = null,
        height: JsonObject? = null,
        viewStyle: JsonObject? = null,
        imageUrl: String,
        imageStyle: JsonObject? = null,
    ): JsonObject {
        return buildJsonObject {
            put(KeySet.type, KeySet.imageButton)
            action?.let { put(KeySet.action, it) }
            width?.let { put(KeySet.width, it) }
            height?.let { put(KeySet.height, it) }
            viewStyle?.let { put(KeySet.viewStyle, it) }
            put(KeySet.imageUrl, imageUrl)
            imageStyle?.let { put(KeySet.imageStyle, it) }
        }
    }

    private fun createText(
        action: JsonObject? = null,
        width: JsonObject? = null,
        height: JsonObject? = null,
        align: JsonObject? = null,
        viewStyle: JsonObject? = null,
        text: String,
        maxTextLines: Int? = null,
        textStyle: JsonObject? = null,
    ): JsonObject {
        return buildJsonObject {
            put(KeySet.type, KeySet.text)
            action?.let { put(KeySet.action, it) }
            width?.let { put(KeySet.width, it) }
            height?.let { put(KeySet.height, it) }
            align?.let { put(KeySet.align, it) }
            viewStyle?.let { put(KeySet.viewStyle, it) }
            put(KeySet.text, text)
            maxTextLines?.let { put(KeySet.maxTextLines, it) }
            textStyle?.let { put(KeySet.textStyle, it) }
        }
    }

    private fun createBox(
        action: JsonObject? = null,
        width: JsonObject? = null,
        height: JsonObject? = null,
        align: JsonObject? = null,
        viewStyle: JsonObject? = null,
        layout: String? = null,
        items: JsonArray
    ): JsonObject {
        return buildJsonObject {
            put(KeySet.type, KeySet.box)
            action?.let { put(KeySet.action, it) }
            width?.let { put(KeySet.width, it) }
            height?.let { put(KeySet.height, it) }
            align?.let { put(KeySet.align, it) }
            viewStyle?.let { put(KeySet.viewStyle, it) }
            layout?.let { put(KeySet.layout, it) }
            put(KeySet.items, items)
        }
    }

    private fun createSize(
        type: String,
        value: Int
    ): JsonObject {
        return buildJsonObject {
            put(KeySet.type, type)
            put(KeySet.value, value)
        }
    }

    private fun createRect(
        top: Int? = null,
        bottom: Int? = null,
        left: Int? = null,
        right: Int? = null,
    ): JsonObject {
        return buildJsonObject {
            top?.let { put(KeySet.top, it) }
            bottom?.let { put(KeySet.bottom, it) }
            left?.let { put(KeySet.left, it) }
            right?.let { put(KeySet.right, it) }
        }
    }

    private fun createTextStyle(
        size: Int? = null,
        color: String? = null,
        weight: String? = null,
    ): JsonObject {
        return buildJsonObject {
            size?.let { put(KeySet.size, it) }
            color?.let { put(KeySet.color, it) }
            weight?.let { put(KeySet.weight, it) }
        }
    }

    private fun createImageStyle(
        contentMode: String? = null,
    ): JsonObject {
        return buildJsonObject {
            contentMode?.let { put(KeySet.contentMode, it) }
        }
    }

    private fun createViewStyle(
        backgroundColor: String? = null,
        backgroundImageUrl: String? = null,
        borderWidth: Int? = null,
        borderColor: String? = null,
        radius: Int? = null,
        margin: JsonObject? = null,
        padding: JsonObject? = null
    ): JsonObject {
        return buildJsonObject {
            backgroundColor?.let { put(KeySet.backgroundColor, it) }
            backgroundImageUrl?.let { put(KeySet.backgroundImageUrl, it) }
            borderWidth?.let { put(KeySet.borderWidth, it) }
            borderColor?.let { put(KeySet.borderColor, it) }
            radius?.let { put(KeySet.radius, it) }
            margin?.let { put(KeySet.margin, it) }
            padding?.let { put(KeySet.padding, it) }
        }
    }
}
