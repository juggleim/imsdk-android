package com.jet.im.kit.internal.extensions

import com.jet.im.kit.consts.StringSet
import java.util.Locale

internal fun String?.toDisplayText(default: String): String {
    return when {
        this == null -> default
        this.contains(StringSet.gif) -> StringSet.gif.uppercase(Locale.getDefault())
        this.startsWith(StringSet.image) -> StringSet.photo.upperFirstChar()
        this.startsWith(StringSet.video) -> StringSet.video.upperFirstChar()
        this.startsWith(StringSet.audio) -> StringSet.audio.upperFirstChar()
        else -> default
    }
}

internal fun String.upperFirstChar(): String {
    return this.replaceFirstChar { char -> if (char.isLowerCase()) char.titlecase(Locale.getDefault()) else char.toString() }
}
