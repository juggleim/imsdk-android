package com.example.demo.notification

import android.os.Bundle
import android.view.View
import com.sendbird.android.exception.SendbirdException
import com.sendbird.android.push.SendbirdPushHelper
import com.sendbird.android.user.User
import com.jet.im.kit.log.Logger
import com.example.demo.common.LoginActivity
import com.example.demo.common.extensions.authenticate
import com.example.demo.common.extensions.startingIntent
import com.example.demo.common.fcm.MyFirebaseMessagingService
import com.example.demo.common.preferences.PreferenceUtils
import com.example.demo.common.widgets.WaitingDialog
import com.jet.im.kit.utils.ContextUtils

class NotificationLoginActivity : LoginActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.useFeedOnly.visibility = View.VISIBLE
        binding.title.visibility = View.GONE
        binding.notificationTitle.visibility = View.VISIBLE
    }

    override fun onSignUp(userId: String, nickname: String) {
        Logger.i(">> NotificationLoginActivity::onSignUp(), userId=$userId, nickname=$nickname")
        WaitingDialog.show(this)
        PreferenceUtils.isUsingFeedChannelOnly = binding.useFeedOnly.isChecked
        authenticate { _: User?, e: SendbirdException? ->
            WaitingDialog.dismiss()
            if (e != null) {
                Logger.e(e)
                ContextUtils.toastError(this@NotificationLoginActivity, "${e.message}")
                return@authenticate
            }
            PreferenceUtils.userId = userId
            PreferenceUtils.nickname = nickname
            SendbirdPushHelper.registerPushHandler(MyFirebaseMessagingService())
            val intent = PreferenceUtils.selectedSampleType.startingIntent(this)
            startActivity(intent)
            finish()
        }
    }
}
