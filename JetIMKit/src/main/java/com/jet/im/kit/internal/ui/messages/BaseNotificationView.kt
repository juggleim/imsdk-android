package com.jet.im.kit.internal.ui.messages

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import com.sendbird.android.channel.NotificationData
import com.sendbird.android.exception.SendbirdException
import com.sendbird.android.internal.SendbirdStatistics
import com.sendbird.android.message.BaseMessage
import com.jet.im.kit.R
import com.jet.im.kit.interfaces.OnNotificationTemplateActionHandler
import com.jet.im.kit.internal.extensions.intToDp
import com.jet.im.kit.internal.interfaces.GetTemplateResultHandler
import com.jet.im.kit.internal.model.notifications.NotificationThemeMode
import com.jet.im.kit.internal.model.template_messages.KeySet
import com.jet.im.kit.internal.model.template_messages.Params
import com.jet.im.kit.internal.model.template_messages.TemplateViewGenerator
import com.jet.im.kit.internal.singleton.MessageTemplateParser
import com.jet.im.kit.internal.singleton.NotificationChannelManager
import com.jet.im.kit.log.Logger
import com.jet.im.kit.utils.DrawableUtils

internal abstract class BaseNotificationView @JvmOverloads internal constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : BaseMessageView(context, attrs, defStyle) {

    internal fun makeTemplateView(
        message: BaseMessage,
        parentView: ViewGroup,
        themeMode: NotificationThemeMode,
        onNotificationTemplateActionHandler: OnNotificationTemplateActionHandler? = null
    ) {
        val handler = object : GetTemplateResultHandler {
            override fun onResult(templateKey: String, jsonTemplate: String?, e: SendbirdException?) {
                Logger.d("++ get template has been succeed, matched=${parentView.tag == message.messageId}")
                if (parentView.tag != message.messageId) return
                val layout = try {
                    e?.let { throw e }
                    jsonTemplate?.let {
                        val viewParams: Params = MessageTemplateParser.parse(jsonTemplate)
                        TemplateViewGenerator.inflateViews(context, viewParams) { view, params ->
                            params.action?.register(
                                view,
                                { v, action, message ->
                                    sendNotificationStats(templateKey, message)
                                    onNotificationTemplateActionHandler?.onHandleAction(
                                        v, action, message
                                    )
                                }, message
                            )
                        }
                    }
                } catch (e: Throwable) {
                    Logger.w("${e.printStackTrace()}")
                    createFallbackNotification(message, themeMode, onNotificationTemplateActionHandler)
                }
                parentView.removeAllViews()
                parentView.addView(layout)
            }
        }

        val notificationData: NotificationData? = message.notificationData
        val templateKey: String = notificationData?.templateKey ?: ""
        val templateVariables = notificationData?.templateVariables ?: mapOf()
        Logger.d("++ message notificationData=$notificationData")
        try {
            parentView.removeAllViews()
            parentView.tag = message.messageId
            if (templateKey.isEmpty()) {
                throw IllegalArgumentException("this message must have template key.")
            }
            if (!NotificationChannelManager.hasTemplate(templateKey)) {
                val layout = createLoadingView(!message.isFeedChannel, themeMode)
                parentView.addView(layout)
            }

            NotificationChannelManager.makeTemplate(
                templateKey, templateVariables, themeMode, handler
            )
        } catch (e: Throwable) {
            handler.onResult(templateKey, null, SendbirdException(e))
        }
    }

    internal fun createFallbackNotification(
        message: BaseMessage,
        themeMode: NotificationThemeMode,
        onNotificationTemplateActionHandler: OnNotificationTemplateActionHandler? = null
    ): View {
        return TemplateViewGenerator.createDefaultViewParam(
            message,
            context.getString(R.string.sb_text_notification_fallback_title),
            context.getString(R.string.sb_text_notification_fallback_description),
            themeMode
        ).run {
            TemplateViewGenerator.inflateViews(context, this) { view, params ->
                params.action?.register(view, onNotificationTemplateActionHandler, message)
            }
        }
    }

    private fun sendNotificationStats(templateKey: String, message: BaseMessage) {
        try {
            // if `tags` key doesn't exist, empty value has to delivery.(spec)
            val tags: List<String> = message.notificationData?.tags ?: listOf()
            val result = SendbirdStatistics.appendStat(
                KeySet.noti_stats,
                mutableMapOf(
                    KeySet.action to KeySet.clicked,
                    KeySet.template_key to templateKey,
                    KeySet.channel_url to message.channelUrl,
                    KeySet.tags to tags,
                    KeySet.message_id to message.messageId,
                    KeySet.source to KeySet.notification,
                    KeySet.message_ts to message.createdAt,
                ).apply {
                    message.notificationEventDeadline?.let {
                        put(KeySet.notification_event_deadline, it)
                    }
                }.toMap()
            )
            Logger.d("++ appendStat end, result=%s, tags=%s", result, tags)
        } catch (e: Throwable) {
            Logger.w(e)
        }
    }

    private fun createLoadingView(
        isChatNotification: Boolean,
        themeMode: NotificationThemeMode,
    ): View {
        return FrameLayout(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                resources.intToDp(if (isChatNotification) 274 else 294),
            )
            addView(
                ProgressBar(context).apply {
                    val size = resources.intToDp(36)
                    layoutParams = LayoutParams(
                        size, size, Gravity.CENTER
                    )
                    val loading = DrawableUtils.setTintList(
                        context,
                        R.drawable.sb_progress,
                        ColorStateList.valueOf(TemplateViewGenerator.getSpinnerColor(themeMode))
                    )
                    this.indeterminateDrawable = loading
                }
            )
        }
    }
}
