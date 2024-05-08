package com.jet.im.kit.internal.model.template_messages

import android.graphics.Typeface
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal enum class ViewType {
    @SerialName(KeySet.box)
    Box,

    @SerialName(KeySet.image)
    Image,

    @SerialName(KeySet.textButton)
    Button,

    @SerialName(KeySet.imageButton)
    ImageButton,

    @SerialName(KeySet.text)
    Text
    ;

    companion object {
        @JvmStatic
        fun from(value: String): ViewType {
            return values().first { it.name == value }
        }
    }
}

@Serializable
internal enum class Orientation(val value: Int) {
    @SerialName(KeySet.row)
    Row(LinearLayout.HORIZONTAL),

    @SerialName(KeySet.column)
    Column(LinearLayout.VERTICAL)
}

@Serializable
internal enum class Weight(val value: Int) {
    @SerialName(KeySet.normal)
    Normal(Typeface.NORMAL),

    @SerialName(KeySet.bold)
    Bold(Typeface.BOLD)
}

@Serializable
internal enum class ContentMode(val scaleType: ImageView.ScaleType) {
    @SerialName(KeySet.aspectFill)
    CenterCrop(ImageView.ScaleType.CENTER_CROP),

    @SerialName(KeySet.aspectFit)
    FitCenter(ImageView.ScaleType.FIT_CENTER),

    @SerialName(KeySet.scalesToFill)
    FitXY(ImageView.ScaleType.FIT_XY);

    fun toValueAsSerialName(): String {
        return when (this) {
            CenterCrop -> KeySet.aspectFill
            FitCenter -> KeySet.aspectFit
            FitXY -> KeySet.scalesToFill
        }
    }
}

@Serializable
internal enum class ActionType {
    @SerialName(KeySet.web)
    Web,

    @SerialName(KeySet.custom)
    Custom,

    @SerialName(KeySet.uikit)
    Uikit
}

@Serializable
internal enum class SizeType {
    @SerialName(KeySet.fixed)
    Fixed,

    @SerialName(KeySet.flex)
    Flex
}

@Serializable
internal enum class VerticalAlign(val value: Int) {
    @SerialName(KeySet.top)
    Top(Gravity.TOP),

    @SerialName(KeySet.bottom)
    Bottom(Gravity.BOTTOM),

    @SerialName(KeySet.center)
    Center(Gravity.CENTER_VERTICAL)
}

@Serializable
internal enum class HorizontalAlign(val value: Int) {
    @SerialName(KeySet.left)
    Left(Gravity.START),

    @SerialName(KeySet.right)
    Right(Gravity.END),

    @SerialName(KeySet.center)
    Center(Gravity.CENTER_HORIZONTAL)
}
