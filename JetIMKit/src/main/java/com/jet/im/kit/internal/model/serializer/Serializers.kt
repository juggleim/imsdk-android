package com.jet.im.kit.internal.model.serializer

import android.graphics.Color
import com.jet.im.kit.consts.ReplyType
import com.jet.im.kit.consts.ThreadReplySelectType
import com.jet.im.kit.internal.model.notifications.CSVColor
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

internal object ColorIntAsStringSerializer : KSerializer<Int> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ColorInt", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Int {
        val decoded = decoder.decodeString()
        // Logger.i("deserialize hex=$decoded")
        return Color.parseColor(decoded)
    }

    override fun serialize(encoder: Encoder, value: Int) {
        val hex = String.format("#%08X", 0xFFFFFFFF and value.toLong())
        // Logger.i("serialize hex=$hex")
        encoder.encodeString(hex)
    }
}

internal object CSVColorIntAsStringSerializer : KSerializer<CSVColor> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("CSVColor class", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): CSVColor {
        val decoded = decoder.decodeString()
        // Logger.i("deserialize hex=$decoded")
        return CSVColor(decoded)
    }

    override fun serialize(encoder: Encoder, value: CSVColor) {
        // Logger.i("serialize hex=$hex")
        encoder.encodeString(value.getColorHexString())
    }
}

internal object JsonElementToStringSerializer : KSerializer<String> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("StringJsonSerializer", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: String) {
        encoder.encodeSerializableValue(JsonElement.serializer(), Json.parseToJsonElement(value))
    }

    override fun deserialize(decoder: Decoder): String {
        return decoder.decodeSerializableValue(JsonElement.serializer()).toString()
    }
}

internal object ReplyTypeAsStringSerializer : KSerializer<ReplyType> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ReplyType enum class", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): ReplyType {
        val decoded = decoder.decodeString()
        return ReplyType.from(decoded)
    }

    override fun serialize(encoder: Encoder, value: ReplyType) {
        encoder.encodeString(value.value)
    }
}

internal object ThreadReplySelectTypeAsStringSerializer : KSerializer<ThreadReplySelectType> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Thread reply select type enum class", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): ThreadReplySelectType {
        val decoded = decoder.decodeString()
        return ThreadReplySelectType.from(decoded)
    }

    override fun serialize(encoder: Encoder, value: ThreadReplySelectType) {
        encoder.encodeString(value.value)
    }
}
