package com.example.demo.common

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.sendbird.android.SendbirdChat
import com.example.demo.R
import com.example.demo.common.consts.SampleType
import com.example.demo.common.preferences.PreferenceUtils
import com.example.demo.databinding.ActivitySelectServiceBinding
import com.example.demo.enableAiChatBotSample
import com.example.demo.enableNotificationSample
import com.example.demo.notification.NotificationLoginActivity


class SelectServiceActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySelectServiceBinding.inflate(layoutInflater).apply {
            versionInfo.text = String.format(
                resources.getString(R.string.text_version_info),
                "1.0.0",
                SendbirdChat.sdkVersion
            )

            basicSampleButton.setOnClickListener {
                PreferenceUtils.selectedSampleType = SampleType.Basic
                startActivity(Intent(this@SelectServiceActivity, LoginActivity::class.java))
                finish()
            }
            aiChatBotSampleButton.setOnClickListener {
                if (enableAiChatBotSample) {
                    PreferenceUtils.selectedSampleType = SampleType.AiChatBot
                    startActivity(Intent(this@SelectServiceActivity, LoginActivity::class.java))
                    finish()
                } else {
                    showSampleNotSupported("https://github.com/sendbird/sendbird-uikit-android/tree/main/uikit-samples/src/main/java/com/sendbird/uikit/samples/aichatbot/README.md")
                }
            }
            customizationSampleButton.setOnClickListener {
                PreferenceUtils.selectedSampleType = SampleType.Customization
                startActivity(Intent(this@SelectServiceActivity, LoginActivity::class.java))
                finish()
            }
            notificationSampleButton.setOnClickListener {
                if (enableNotificationSample) {
                    PreferenceUtils.selectedSampleType = SampleType.Notification
                    startActivity(Intent(this@SelectServiceActivity, NotificationLoginActivity::class.java))
                    finish()
                } else {
                    showSampleNotSupported("https://github.com/sendbird/sendbird-uikit-android/tree/main/uikit-samples/src/main/java/com/sendbird/uikit/samples/notification/README.md")
                }
            }
        }
        setContentView(binding.root)
    }

    private fun showSampleNotSupported(url: String) {
        val builder = AlertDialog.Builder(this@SelectServiceActivity)
        builder.setTitle(getString(R.string.sb_text_dialog_not_supported_title))
        builder.setMessage(getString(R.string.text_not_supported))
        builder.setPositiveButton(R.string.sb_text_go_to_readme) { _: DialogInterface?, _: Int ->
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(browserIntent)
        }
        val dialog = builder.create()
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            .setTextColor(ContextCompat.getColor(this@SelectServiceActivity, com.jet.im.kit.R.color.secondary_300))
    }
}
