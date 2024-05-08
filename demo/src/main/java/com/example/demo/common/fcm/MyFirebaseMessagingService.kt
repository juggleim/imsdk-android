package com.example.demo.common.fcm

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.RemoteMessage
import com.sendbird.android.SendbirdChat.markAsDelivered
import com.sendbird.android.push.SendbirdPushHandler
import com.jet.im.kit.log.Logger
import com.example.demo.R
import com.example.demo.common.consts.StringSet
import com.example.demo.common.extensions.newRedirectToChannelIntent
import com.example.demo.common.preferences.PreferenceUtils
import org.json.JSONException
import org.json.JSONObject
import java.util.concurrent.atomic.AtomicReference


/**
 * Concrete implementation of a sendbird push handler.
 */
class MyFirebaseMessagingService : SendbirdPushHandler() {
    override val isUniquePushToken: Boolean
        get() = false

    override fun onNewToken(newToken: String?) {
        Logger.i("{$TAG onNewToken($newToken)")
        pushToken.set(newToken)
    }

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    override fun onMessageReceived(context: Context, remoteMessage: RemoteMessage) {
        Logger.d("From: " + remoteMessage.from)
        if (remoteMessage.data.isNotEmpty()) {
            Logger.d("Message data payload: " + remoteMessage.data)
        }

        // Check if message contains a notification payload.
        Logger.d("Message Notification Body: " + remoteMessage.notification?.body)
        try {
            if (remoteMessage.data.containsKey(StringSet.sendbird)) {
                val jsonStr = remoteMessage.data[StringSet.sendbird]
                try {
                    markAsDelivered(remoteMessage.data)
                } catch (e: Exception) {
                    Logger.e(e)
                }
                if (jsonStr == null) return
                sendNotification(context, JSONObject(jsonStr), remoteMessage.data)
            }
        } catch (e: JSONException) {
            Logger.e(e)
        }
    }

    companion object {
        private const val TAG = "MyFirebaseMsgService"
        private val pushToken = AtomicReference<String?>()

        /**
         * Create and show a simple notification containing the received FCM message.
         *
         * @param sendBird JSONObject payload from FCM
         */
        @Throws(JSONException::class)
        fun sendNotification(context: Context, sendBird: JSONObject, data: Map<String, String>) {
            val message = sendBird.getString(StringSet.message)
            val channel = sendBird.getJSONObject(StringSet.channel)
            val channelUrl = channel.getString(StringSet.channel_url)
            val messageId = sendBird.getLong(StringSet.message_id)
            val channelType = sendBird.optString(StringSet.channel_type, "")
            val channelKey = sendBird.optString(StringSet.notification_channel_key, "")
            var pushTitle = context.getString(R.string.app_name)
            if (sendBird.has(StringSet.sender)) {
                val sender = sendBird.getJSONObject(StringSet.sender)
                pushTitle = sender.getString(StringSet.name)
            }
            if (sendBird.has(StringSet.push_title) && !sendBird.isNull(StringSet.push_title)) {
                pushTitle = sendBird.getString(StringSet.push_title)
            }

            val channelId = StringSet.CHANNEL_ID
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel(channelId, StringSet.CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH).let {
                    notificationManager.createNotificationChannel(it)
                }
            }

            val hashedData = if (channelKey.isNotEmpty()) HashMap(data) else null
            val intent = newRedirectToChannelIntent(context, channelUrl, messageId, channelType, hashedData)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

            @SuppressLint("UnspecifiedImmutableFlag")
            val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.getActivity(
                    context,
                    messageId.toInt(),
                    intent,
                    PendingIntent.FLAG_IMMUTABLE
                )
            } else {
                PendingIntent.getActivity(context, messageId.toInt(), intent, 0)
            }
            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val notificationBuilder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.icon_push_lollipop)
                .setColor(ContextCompat.getColor(context, com.jet.im.kit.R.color.primary_300)) // small icon background color
                .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.icon_push_oreo))
                .setContentTitle(pushTitle)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentIntent(pendingIntent)
                .setContentText(message)
            notificationManager.notify(System.currentTimeMillis().toString(), 0, notificationBuilder.build())
        }

        private fun newRedirectToChannelIntent(
            context: Context,
            channelUrl: String,
            messageId: Long,
            channelType: String,
            data: HashMap<String, String>?,
        ): Intent {
            return PreferenceUtils.selectedSampleType.newRedirectToChannelIntent(
                context,
                channelUrl,
                messageId,
                channelType
            ).apply {
                data?.let { putExtra(StringSet.PUSH_NOTIFICATION_DATA, it) }
            }
        }
    }
}
