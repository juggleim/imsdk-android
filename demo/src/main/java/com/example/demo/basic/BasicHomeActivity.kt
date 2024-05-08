package com.example.demo.basic

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
import com.example.demo.R
import com.sendbird.android.SendbirdChat
import com.sendbird.android.exception.SendbirdException
import com.sendbird.android.handler.UnreadMessageCountHandler
import com.sendbird.android.handler.UserEventHandler
import com.sendbird.android.params.GroupChannelTotalUnreadMessageCountParams
import com.sendbird.android.user.UnreadMessageCount
import com.sendbird.android.user.User
import com.example.demo.basic.openchannel.OpenChannelMainActivity
import com.example.demo.common.ThemeHomeActivity
import com.example.demo.common.extensions.logout
import com.example.demo.common.extensions.setTextColorResource
import com.example.demo.databinding.ActivityHomeBinding
import com.jet.im.kit.utils.ContextUtils
import java.util.Locale


class BasicHomeActivity : ThemeHomeActivity() {
    override val binding by lazy { ActivityHomeBinding.inflate(layoutInflater) }
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }
    private val appSettingLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.apply {
            groupChannelButton.setOnClickListener {
                startActivity(Intent(this@BasicHomeActivity, GroupChannelMainActivity::class.java))
            }
            openChannelButton.setOnClickListener {
                startActivity(Intent(this@BasicHomeActivity, OpenChannelMainActivity::class.java))
            }
            btSignOut.setOnClickListener { logout() }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val permission = Manifest.permission.POST_NOTIFICATIONS
                if (ContextCompat.checkSelfPermission(
                        this@BasicHomeActivity,
                        permission
                    ) == PermissionChecker.PERMISSION_GRANTED
                ) {
                    return@apply
                }
                if (ActivityCompat.shouldShowRequestPermissionRationale(this@BasicHomeActivity, permission)) {
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

    override fun onResume() {
        super.onResume()
        // initialize total unread count
        SendbirdChat.getTotalUnreadMessageCount(
            GroupChannelTotalUnreadMessageCountParams(),
            UnreadMessageCountHandler { totalCount: Int, _: Int, e: SendbirdException? ->
                if (e != null) {
                    return@UnreadMessageCountHandler
                }
                if (totalCount > 0) {
                    binding.groupChannelButton.unreadCountVisibility = View.VISIBLE
                    binding.groupChannelButton.unreadCount =
                        if (totalCount > 99) getString(R.string.text_tab_badge_max_count) else totalCount.toString()
                } else {
                    binding.groupChannelButton.unreadCountVisibility = View.GONE
                }
            })
        // register total unread count event
        SendbirdChat.addUserEventHandler(USER_EVENT_HANDLER_KEY, object : UserEventHandler() {
            override fun onFriendsDiscovered(users: List<User>) {}
            override fun onTotalUnreadMessageCountChanged(unreadMessageCount: UnreadMessageCount) {
                val totalCount = unreadMessageCount.groupChannelCount
                if (totalCount > 0) {
                    binding.groupChannelButton.unreadCountVisibility = View.VISIBLE
                    binding.groupChannelButton.unreadCount =
                        if (totalCount > 99) getString(R.string.text_tab_badge_max_count) else totalCount.toString()
                } else {
                    binding.groupChannelButton.unreadCountVisibility = View.GONE
                }
            }
        })
    }

    override fun onPause() {
        super.onPause()
        SendbirdChat.removeUserEventHandler(USER_EVENT_HANDLER_KEY)
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

    companion object {
        private val USER_EVENT_HANDLER_KEY = "USER_EVENT_HANDLER_KEY" + System.currentTimeMillis()
    }
}
