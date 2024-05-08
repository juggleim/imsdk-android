package com.jet.im.kit.internal.model.template_messages

import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.sendbird.android.message.BaseMessage
import com.jet.im.kit.SendbirdUIKit
import com.jet.im.kit.internal.model.notifications.NotificationThemeMode
import com.jet.im.kit.internal.ui.widgets.Box
import com.jet.im.kit.internal.ui.widgets.Image
import com.jet.im.kit.internal.ui.widgets.ImageButton
import com.jet.im.kit.internal.ui.widgets.Text
import com.jet.im.kit.internal.ui.widgets.TextButton

internal typealias ViewLifecycleHandler = (view: View, viewParams: ViewParams) -> Unit

internal object TemplateViewGenerator {
    fun inflateViews(
        context: Context,
        params: Params,
        onViewCreated: ViewLifecycleHandler? = null
    ): View {
        when (params.version) {
            1 -> {
                return LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    params.body.items.forEach {
                        addView(
                            generateView(context, it, Orientation.Column, onViewCreated)
                        )
                    }
                }
            }
            else -> {
                throw RuntimeException("unsupported version. current version = ${params.version}")
            }
        }
    }

    private fun generateView(
        context: Context,
        viewParams: ViewParams,
        orientation: Orientation,
        onViewCreated: ViewLifecycleHandler? = null
    ): View {
        return when (viewParams) {
            is BoxViewParams -> createBoxView(context, viewParams, orientation, onViewCreated)
            is ImageViewParams -> createImageView(context, viewParams, orientation, onViewCreated)
            is TextViewParams -> createTextView(context, viewParams, orientation, onViewCreated)
            is ButtonViewParams -> createButtonView(context, viewParams, orientation, onViewCreated)
            is ImageButtonViewParams -> createImageButtonView(context, viewParams, orientation, onViewCreated)
        }
    }

    private fun createTextView(
        context: Context,
        params: TextViewParams,
        orientation: Orientation,
        onViewCreated: ViewLifecycleHandler? = null
    ): View {
        return Text(context).apply {
            onViewCreated?.invoke(this, params)
            apply(params, orientation)
        }
    }

    private fun createImageView(
        context: Context,
        params: ImageViewParams,
        orientation: Orientation,
        onViewCreated: ViewLifecycleHandler? = null
    ): View {
        return Image(context).apply {
            onViewCreated?.invoke(this, params)
            apply(params, orientation)
        }
    }

    private fun createButtonView(
        context: Context,
        params: ButtonViewParams,
        orientation: Orientation,
        onViewCreated: ViewLifecycleHandler? = null
    ): View {
        return TextButton(context).apply {
            onViewCreated?.invoke(this, params)
            apply(params, orientation)
        }
    }

    private fun createImageButtonView(
        context: Context,
        params: ImageButtonViewParams,
        orientation: Orientation,
        onViewCreated: ViewLifecycleHandler? = null
    ): View {
        return ImageButton(context).apply {
            onViewCreated?.invoke(this, params)
            apply(params, orientation)
        }
    }

    private fun createBoxView(
        context: Context,
        params: BoxViewParams,
        orientation: Orientation,
        onViewCreated: ViewLifecycleHandler? = null
    ): ViewGroup {
        return Box(context).apply {
            onViewCreated?.invoke(this, params)
            apply(params, orientation)
            params.items?.forEach {
                addView(
                    generateView(
                        context,
                        it,
                        params.orientation,
                        onViewCreated
                    )
                )
            }
        }
    }

    @JvmStatic
    fun createDefaultViewParam(
        message: BaseMessage,
        defaultFallbackTitle: String,
        defaultFallbackDescription: String,
        themeMode: NotificationThemeMode
    ): Params {
        val hasFallbackMessage = message.message.isNotEmpty()
        val textList = mutableListOf(
            TextViewParams(
                type = ViewType.Text,
                textStyle = TextStyle(
                    size = 14,
                    color = getTitleColor(themeMode)
                ),
                text = message.message.takeIf { it.isNotEmpty() } ?: defaultFallbackTitle
            )
        )

        if (!hasFallbackMessage) {
            textList.add(
                TextViewParams(
                    type = ViewType.Text,
                    textStyle = TextStyle(
                        size = 14,
                        color = getDescTextColor(themeMode)
                    ),
                    text = defaultFallbackDescription
                )
            )
        }
        return Params(
            version = 1,
            body = Body(
                items = listOf(
                    BoxViewParams(
                        type = ViewType.Box,
                        orientation = Orientation.Column,
                        viewStyle = ViewStyle(
                            backgroundColor = getBackgroundColor(themeMode),
                            padding = Padding(
                                12, 12, 12, 12
                            ),
                            radius = 8
                        ),
                        items = textList
                    ),
                )
            )
        )
    }

    private fun getBackgroundColor(themeMode: NotificationThemeMode): Int {
        val color = when (themeMode) {
            NotificationThemeMode.Light -> "#EEEEEE"
            NotificationThemeMode.Dark -> "#2C2C2C"
            NotificationThemeMode.Default -> if (SendbirdUIKit.getDefaultThemeMode() == SendbirdUIKit.ThemeMode.Light) "#EEEEEE" else "#2C2C2C"
        }
        return Color.parseColor(color)
    }

    private fun getTitleColor(themeMode: NotificationThemeMode): Int {
        val color = when (themeMode) {
            NotificationThemeMode.Light -> "#E0000000"
            NotificationThemeMode.Dark -> "#E0FFFFFF"
            NotificationThemeMode.Default -> if (SendbirdUIKit.getDefaultThemeMode() == SendbirdUIKit.ThemeMode.Light) "#E0000000" else "#E0FFFFFF"
        }
        return Color.parseColor(color)
    }

    private fun getDescTextColor(themeMode: NotificationThemeMode): Int {
        val color = when (themeMode) {
            NotificationThemeMode.Light -> "#70000000"
            NotificationThemeMode.Dark -> "#70FFFFFF"
            NotificationThemeMode.Default -> if (SendbirdUIKit.getDefaultThemeMode() == SendbirdUIKit.ThemeMode.Light) "#70000000" else "#70FFFFFF"
        }
        return Color.parseColor(color)
    }

    internal fun getSpinnerColor(themeMode: NotificationThemeMode): Int {
        val color = when (themeMode) {
            NotificationThemeMode.Light -> "#70000000"
            NotificationThemeMode.Dark -> "#70FFFFFF"
            NotificationThemeMode.Default -> if (SendbirdUIKit.getDefaultThemeMode() == SendbirdUIKit.ThemeMode.Light) "#70000000" else "#70FFFFFF"
        }
        return Color.parseColor(color)
    }
}
