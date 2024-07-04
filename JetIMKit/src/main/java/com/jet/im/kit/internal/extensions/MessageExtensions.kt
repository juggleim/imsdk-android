package com.jet.im.kit.internal.extensions

import android.content.Context
import com.sendbird.android.message.BaseFileMessage
import com.sendbird.android.message.BaseMessage
import com.sendbird.android.message.FileMessage
import com.sendbird.android.message.FormField
import com.sendbird.android.message.MultipleFilesMessage
import com.jet.im.kit.R
import com.jet.im.kit.consts.StringSet
import com.jet.im.kit.model.UserMessageDisplayData
import com.jet.im.kit.utils.MessageUtils

internal fun BaseMessage.hasParentMessage() = parentMessageId != 0L

internal fun MultipleFilesMessage.containsOnlyImageFiles(): Boolean {
    return files.all { it.fileType.contains(StringSet.image) }
}

internal fun MultipleFilesMessage.getCacheKey(index: Int): String = "${requestId}_$index"

internal fun BaseFileMessage.toDisplayText(context: Context): String {
    return when (this) {
        is FileMessage -> {
            if (MessageUtils.isVoiceMessage(this)) {
                context.getString(R.string.sb_text_voice_message)
            } else {
                this.type.toDisplayText(StringSet.file.upperFirstChar())
            }
        }

        is MultipleFilesMessage -> {
            StringSet.photo.upperFirstChar()
        }
    }
}

internal fun BaseFileMessage.getType(): String {
    return when (this) {
        is FileMessage -> {
            if (MessageUtils.isVoiceMessage(this)) {
                StringSet.voice
            } else {
                this.type
            }
        }

        is MultipleFilesMessage -> {
            this.files.firstOrNull()?.fileType ?: ""
        }
    }
}

internal fun BaseFileMessage.getName(context: Context): String {
    return when (this) {
        is FileMessage -> {
            if (MessageUtils.isVoiceMessage(this)) {
                context.getString(R.string.sb_text_voice_message)
            } else {
                this.name
            }
        }

        is MultipleFilesMessage -> {
            this.files.firstOrNull()?.fileName ?: ""
        }
    }
}

internal fun List<BaseMessage>.clearLastValidations() {
    this.flatMap { message -> message.forms }
        .flatMap { form -> form.formFields }
        .forEach { formField -> formField.lastValidation = null }
}

internal val lastValidations: MutableMap<String, Boolean?> = mutableMapOf()
internal var FormField.lastValidation: Boolean?
    get() = lastValidations[this.identifier]
    set(value) {
        if (value == null) {
            lastValidations.remove(this.identifier)
        } else {
            lastValidations[this.identifier] = value
        }
    }

private val FormField.identifier: String
    get() = "${this.messageId}_${this.key}"
