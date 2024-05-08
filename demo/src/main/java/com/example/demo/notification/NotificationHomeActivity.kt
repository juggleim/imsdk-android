package com.example.demo.notification

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.jet.im.kit.activities.FeedNotificationChannelActivity
import com.example.demo.R
import com.example.demo.common.ThemeHomeActivity
import com.example.demo.common.extensions.getFeedChannelUrl
import com.example.demo.common.extensions.logout
import com.example.demo.common.extensions.setTextColorResource
import com.example.demo.common.preferences.PreferenceUtils
import com.example.demo.databinding.ActivityNotificationHomeBinding
import com.jet.im.kit.utils.ContextUtils
import java.util.Locale

class NotificationHomeActivity : ThemeHomeActivity() {
    override val binding: ActivityNotificationHomeBinding by lazy { ActivityNotificationHomeBinding.inflate(layoutInflater) }
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }
    private val appSettingLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.apply {
            chatAndFeedButton.visibility = if (PreferenceUtils.isUsingFeedChannelOnly) View.GONE else View.VISIBLE
            chatAndFeedButton.setOnClickListener {
                startActivity(Intent(this@NotificationHomeActivity, NotificationMainActivity::class.java))
            }
            feedOnlyButton.setOnClickListener {
                startActivity(
                    FeedNotificationChannelActivity.newIntent(
                        this@NotificationHomeActivity,
                        getFeedChannelUrl()
                    )
                )

            }
            btSignOut.setOnClickListener { logout() }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val permission = Manifest.permission.POST_NOTIFICATIONS
                if (ContextCompat.checkSelfPermission(
                        this@NotificationHomeActivity,
                        permission
                    ) == PermissionChecker.PERMISSION_GRANTED
                ) {
                    return@apply
                }
                if (ActivityCompat.shouldShowRequestPermissionRationale(this@NotificationHomeActivity, permission)) {
                    showPermissionRationalePopup()
                    return@apply
                }
                requestPermissionLauncher.launch(permission)
            }
        }
    }

    override fun applyTheme() {
        super.applyTheme()
        binding.mainTitle.setTextColorResource(if (isDarkTheme) com.jet.im.kit.R.color.ondark_01 else com.jet.im.kit.R.color.onlight_01)
        binding.btSignOut.setBackgroundResource(
            if (isDarkTheme) R.drawable.selector_home_signout_button_dark
            else R.drawable.selector_home_signout_button
        )
    }

    private fun showPermissionRationalePopup() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(com.jet.im.kit.R.string.sb_text_dialog_permission_title))
        builder.setMessage(
            String.format(
                Locale.US,
                getString(R.string.sb_text_need_to_allow_permission_notification),
                ContextUtils.getApplicationName(this)
            )
        )
        builder.setPositiveButton(com.jet.im.kit.R.string.sb_text_go_to_settings) { _: DialogInterface?, _: Int ->
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            intent.addCategory(Intent.CATEGORY_DEFAULT)
            intent.data = Uri.parse("package:$packageName")
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            appSettingLauncher.launch(intent)
        }
        val dialog = builder.create()
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            .setTextColor(ContextCompat.getColor(this, com.jet.im.kit.R.color.secondary_300))
    }
}
