package com.example.demo.notification

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.sendbird.android.SendbirdChat.addUserEventHandler
import com.sendbird.android.SendbirdChat.getTotalUnreadMessageCount
import com.sendbird.android.SendbirdChat.removeUserEventHandler
import com.sendbird.android.exception.SendbirdException
import com.sendbird.android.handler.UnreadMessageCountHandler
import com.sendbird.android.handler.UserEventHandler
import com.sendbird.android.push.SendbirdPushHelper
import com.sendbird.android.user.UnreadMessageCount
import com.sendbird.android.user.User
import com.jet.im.kit.SendbirdUIKit
import com.jet.im.kit.activities.ChannelActivity
import com.jet.im.kit.activities.ChatNotificationChannelActivity
import com.jet.im.kit.fragments.FeedNotificationChannelFragment
import com.jet.im.kit.log.Logger
import com.jet.im.kit.providers.FragmentProviders
import com.example.demo.BaseApplication.Companion.initStateChanges
import com.example.demo.R
import com.example.demo.common.SampleSettingsFragment
import com.example.demo.common.consts.InitState
import com.example.demo.common.consts.StringSet
import com.example.demo.common.extensions.getFeedChannelUrl
import com.example.demo.common.extensions.getSerializable
import com.example.demo.common.extensions.isUsingDarkTheme
import com.example.demo.common.preferences.PreferenceUtils
import com.example.demo.common.widgets.CustomTabView
import com.example.demo.common.widgets.WaitingDialog
import com.example.demo.databinding.ActivityNotificationMainBinding

class NotificationMainActivity : AppCompatActivity() {
    private val binding: ActivityNotificationMainBinding by lazy {
        ActivityNotificationMainBinding.inflate(layoutInflater)
    }
    private val channelListTab: CustomTabView by lazy {
        CustomTabView(this).apply {
            setBadgeVisibility(View.GONE)
            setTitle(getString(R.string.text_tab_channels))
            setIcon(R.drawable.icon_chat_filled)
        }
    }
    private val feedChannelTab: CustomTabView by lazy {
        CustomTabView(this).apply {
            setBadgeVisibility(View.GONE)
            setTitle(getString(R.string.text_tab_notifications))
            setIcon(R.drawable.icon_notifications_filled)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(SendbirdUIKit.getDefaultThemeMode().resId)
        setContentView(binding.root)

        // for redirecting to channel from push notification.
        initStateChanges().observe(this) { initState: InitState ->
            Logger.i("++ init state : %s", initState)
            WaitingDialog.dismiss()
            when (initState) {
                InitState.NONE -> {}
                InitState.MIGRATING -> WaitingDialog.show(this@NotificationMainActivity)
                InitState.FAILED, InitState.SUCCEED -> {
                    WaitingDialog.dismiss()
                    SendbirdUIKit.connect { _, _ -> initPage() }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        removeUserEventHandler(USER_EVENT_HANDLER_KEY)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        redirectChannelIfNeeded(intent)
    }

    private fun initPage() {
        val adapter = MainAdapter(this@NotificationMainActivity)
        binding.viewPager.adapter = adapter

        val isDarkMode = PreferenceUtils.themeMode.isUsingDarkTheme()
        val backgroundRedId = if (isDarkMode) com.jet.im.kit.R.color.background_600 else com.jet.im.kit.R.color.background_50
        binding.tabLayout.setBackgroundResource(backgroundRedId)
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.customView = when (position) {
                TAB_CHANNEL_LIST -> channelListTab
                TAB_FEED_CHANNEL -> feedChannelTab
                TAB_SETTINGS -> CustomTabView(this).apply {
                    setBadgeVisibility(View.GONE)
                    setTitle(getString(R.string.text_tab_settings))
                    setIcon(R.drawable.icon_settings_filled)
                }
                else -> null
            }
        }.attach()
        redirectChannelIfNeeded(intent)

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (position == TAB_FEED_CHANNEL) {
                    val fragment: Fragment? = adapter.getItem(TAB_FEED_CHANNEL)
                    if (fragment is FeedNotificationChannelFragment) {
                        fragment.updateLastReadTimeOnCurrentChannel()
                    }
                }
            }
        })

        getTotalUnreadMessageCount(
            handler = UnreadMessageCountHandler { groupChannelCount: Int, feedChannelTotalCount: Int, e: SendbirdException? ->
                if (e != null) {
                    return@UnreadMessageCountHandler
                }
                Logger.i("updateChannelListTab [API] groupChannelCount=$groupChannelCount, feedChannelUnreadCount=$feedChannelTotalCount")
                drawUnreadCount(channelListTab, groupChannelCount)
                drawUnreadCount(feedChannelTab, feedChannelTotalCount)
            }
        )

        addUserEventHandler(USER_EVENT_HANDLER_KEY, object : UserEventHandler() {
            override fun onFriendsDiscovered(users: List<User>) {}
            override fun onTotalUnreadMessageCountChanged(unreadMessageCount: UnreadMessageCount) {
                val groupChannelCount = unreadMessageCount.groupChannelCount
                val feedChannelUnreadCount = unreadMessageCount.feedChannelCount
                Logger.i("updateChannelListTab groupChannelCount=$groupChannelCount, feedChannelUnreadCount=$feedChannelUnreadCount")
                drawUnreadCount(channelListTab, groupChannelCount)
                drawUnreadCount(feedChannelTab, feedChannelUnreadCount)
            }
        })
    }

    @Synchronized
    private fun drawUnreadCount(tabView: CustomTabView, count: Int) {
        if (count > 0) {
            tabView.setBadgeVisibility(View.VISIBLE)
            tabView.setBadgeCount(if (count > 99) getString(R.string.text_tab_badge_max_count) else count.toString())
        } else {
            tabView.setBadgeVisibility(View.GONE)
        }
    }

    private fun redirectChannelIfNeeded(intent: Intent?) {
        if (intent == null) return
        Logger.i("++ intent: %s, %s", intent, intent.extras)
        if (intent.hasExtra(StringSet.PUSH_NOTIFICATION_DATA)) {
            intent.getSerializable(StringSet.PUSH_NOTIFICATION_DATA, HashMap::class.java)?.let {
                val resultMap = HashMap<String, String>()
                for ((key, value) in it) {
                    if (key is String && value is String) {
                        resultMap[key] = value
                    }
                }
                SendbirdPushHelper.markPushNotificationAsClicked(resultMap)
            }
            intent.removeExtra(StringSet.PUSH_NOTIFICATION_DATA)
        }
        if (intent.flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY == Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) {
            intent.removeExtra(StringSet.PUSH_REDIRECT_CHANNEL)
            intent.removeExtra(StringSet.PUSH_REDIRECT_MESSAGE_ID)
        }
        if (intent.hasExtra(StringSet.PUSH_REDIRECT_CHANNEL)) {
            val channelUrl = intent.getStringExtra(StringSet.PUSH_REDIRECT_CHANNEL)
            intent.removeExtra(StringSet.PUSH_REDIRECT_CHANNEL)
            if (channelUrl.isNullOrEmpty()) return
            val channelType = intent.getStringExtra(StringSet.PUSH_REDIRECT_CHANNEL_TYPE)
            if (channelType.isNullOrEmpty()) return

            when (channelType) {
                StringSet.notification_feed -> {
                    binding.viewPager.currentItem = TAB_FEED_CHANNEL
                }
                StringSet.notification_chat -> {
                    startActivity(ChatNotificationChannelActivity.newIntent(this, channelUrl))
                }
                else -> {
                    if (intent.hasExtra(StringSet.PUSH_REDIRECT_MESSAGE_ID)) {
                        val messageId = intent.getLongExtra(StringSet.PUSH_REDIRECT_MESSAGE_ID, 0L)
                        if (messageId > 0L) {
                            startActivity(ChannelActivity.newRedirectToMessageThreadIntent(this, channelUrl, messageId))
                            intent.removeExtra(StringSet.PUSH_REDIRECT_MESSAGE_ID)
                        }
                    } else {
                        startActivity(ChannelActivity.newIntent(this, channelUrl))
                    }
                }
            }
        }
    }

    private class MainAdapter(val fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = 3
        override fun createFragment(position: Int): Fragment {
            return when (position) {
                TAB_CHANNEL_LIST -> FragmentProviders.channelList.provide(Bundle())
                TAB_FEED_CHANNEL -> FragmentProviders.feedNotificationChannel.provide(getFeedChannelUrl(), Bundle())
                TAB_SETTINGS -> SampleSettingsFragment()
                else -> Fragment()
            }
        }

        fun getItem(position: Int): Fragment? {
            return fa.supportFragmentManager.findFragmentByTag("f$position")
        }
    }

    companion object {
        private val USER_EVENT_HANDLER_KEY = "USER_EVENT_HANDLER_KEY" + System.currentTimeMillis()
        private const val TAB_CHANNEL_LIST = 0
        private const val TAB_FEED_CHANNEL = 1
        private const val TAB_SETTINGS = 2
        fun newRedirectToChannelIntent(
            context: Context,
            channelUrl: String,
            messageId: Long,
            channelType: String,
        ): Intent {
            return Intent(context, NotificationMainActivity::class.java).apply {
                putExtra(StringSet.PUSH_REDIRECT_CHANNEL, channelUrl)
                putExtra(StringSet.PUSH_REDIRECT_MESSAGE_ID, messageId)
                putExtra(StringSet.PUSH_REDIRECT_CHANNEL_TYPE, channelType)
            }
        }
    }
}
