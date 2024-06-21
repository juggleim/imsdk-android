package com.example.demo.basic

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.sendbird.android.SendbirdChat
import com.sendbird.android.exception.SendbirdException
import com.sendbird.android.handler.UnreadMessageCountHandler
import com.sendbird.android.handler.UserEventHandler
import com.sendbird.android.params.GroupChannelTotalUnreadMessageCountParams
import com.sendbird.android.user.UnreadMessageCount
import com.sendbird.android.user.User
import com.jet.im.kit.SendbirdUIKit
import com.jet.im.kit.activities.ChannelActivity
import com.jet.im.kit.providers.FragmentProviders
import com.example.demo.R
import com.example.demo.common.SampleSettingsFragment
import com.example.demo.common.consts.StringSet
import com.example.demo.common.extensions.isUsingDarkTheme
import com.example.demo.common.preferences.PreferenceUtils
import com.example.demo.common.widgets.CustomTabView
import com.example.demo.databinding.ActivityGroupChannelMainBinding

class GroupChannelMainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGroupChannelMainBinding
    private lateinit var unreadCountTab: CustomTabView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(SendbirdUIKit.getDefaultThemeMode().resId)
        binding = ActivityGroupChannelMainBinding.inflate(layoutInflater).apply {
            setContentView(root)
            viewPager.adapter = MainAdapter(this@GroupChannelMainActivity)
            val isDarkMode = PreferenceUtils.themeMode.isUsingDarkTheme()
            val backgroundRedId = if (isDarkMode) com.jet.im.kit.R.color.background_600 else com.jet.im.kit.R.color.background_50
            tabLayout.setBackgroundResource(backgroundRedId)
            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                tab.customView = when (position) {
                    0 -> {
                        unreadCountTab = CustomTabView(this@GroupChannelMainActivity).apply {
                            setBadgeVisibility(View.GONE)
                            setTitle(getString(R.string.text_tab_channels))
                            setIcon(R.drawable.icon_chat_filled)
                        }
                        unreadCountTab
                    }
                    else -> {
                        CustomTabView(this@GroupChannelMainActivity).apply {
                            setBadgeVisibility(View.GONE)
                            setTitle(getString(R.string.text_tab_settings))
                            setIcon(R.drawable.icon_settings_filled)
                        }
                    }
                }
            }.attach()
            redirectChannelIfNeeded(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        SendbirdChat.getTotalUnreadMessageCount(
            GroupChannelTotalUnreadMessageCountParams(),
            UnreadMessageCountHandler { totalCount: Int, _: Int, e: SendbirdException? ->
                if (e != null) {
                    return@UnreadMessageCountHandler
                }
                if (totalCount > 0) {
                    unreadCountTab.setBadgeVisibility(View.VISIBLE)
                    unreadCountTab.setBadgeCount(if (totalCount > 99) getString(R.string.text_tab_badge_max_count) else totalCount.toString())
                } else {
                    unreadCountTab.setBadgeVisibility(View.GONE)
                }
            })
        SendbirdChat.addUserEventHandler(USER_EVENT_HANDLER_KEY, object : UserEventHandler() {
            override fun onFriendsDiscovered(users: List<User>) {}
            override fun onTotalUnreadMessageCountChanged(unreadMessageCount: UnreadMessageCount) {
                val totalCount = unreadMessageCount.groupChannelCount
                if (totalCount > 0) {
                    unreadCountTab.setBadgeVisibility(View.VISIBLE)
                    unreadCountTab.setBadgeCount(if (totalCount > 99) getString(R.string.text_tab_badge_max_count) else totalCount.toString())
                } else {
                    unreadCountTab.setBadgeVisibility(View.GONE)
                }
            }
        })
    }

    override fun onPause() {
        super.onPause()
        SendbirdChat.removeUserEventHandler(USER_EVENT_HANDLER_KEY)
    }

    private fun redirectChannelIfNeeded(intent: Intent?) {
        if (intent == null) return
        if (intent.flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY == Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) {
            intent.removeExtra(StringSet.PUSH_REDIRECT_CHANNEL)
            intent.removeExtra(StringSet.PUSH_REDIRECT_MESSAGE_ID)
        }
        if (intent.hasExtra(StringSet.PUSH_REDIRECT_CHANNEL)) {
            val channelUrl = intent.getStringExtra(StringSet.PUSH_REDIRECT_CHANNEL)
                ?: return
            if (intent.hasExtra(StringSet.PUSH_REDIRECT_MESSAGE_ID)) {
                val messageId = intent.getLongExtra(StringSet.PUSH_REDIRECT_MESSAGE_ID, 0L)
                if (messageId > 0L) {
                    startActivity(ChannelActivity.newRedirectToMessageThreadIntent(this, channelUrl, messageId))
                    intent.removeExtra(StringSet.PUSH_REDIRECT_MESSAGE_ID)
                }
            } else {
                startActivity(ChannelActivity.newIntent(this, channelUrl))
            }
            intent.removeExtra(StringSet.PUSH_REDIRECT_CHANNEL)
        }
    }

    private class MainAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = PAGE_SIZE
        override fun createFragment(position: Int): Fragment {
            return if (position == 0) {
                FragmentProviders.channelList.provide(Bundle())
            } else {
                SampleSettingsFragment()
            }
        }

        companion object {
            private const val PAGE_SIZE = 2
        }
    }

    companion object {
        private val USER_EVENT_HANDLER_KEY = "USER_EVENT_HANDLER_KEY" + System.currentTimeMillis()
    }
}
