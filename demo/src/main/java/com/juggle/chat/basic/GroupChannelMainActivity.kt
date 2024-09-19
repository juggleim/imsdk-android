package com.juggle.chat.basic

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.juggle.chat.R
import com.juggle.chat.common.SampleSettingsFragment
import com.juggle.chat.common.consts.StringSet
import com.juggle.chat.common.extensions.isUsingDarkTheme
import com.juggle.chat.common.preferences.PreferenceUtils
import com.juggle.chat.common.widgets.CustomTabView
import com.juggle.chat.databinding.ActivityGroupChannelMainBinding
import com.juggle.chat.friends.FriendListFragment
import com.juggle.chat.group.GroupListFragment
import com.google.android.material.tabs.TabLayoutMediator
import com.jet.im.kit.SendbirdUIKit
import com.jet.im.kit.activities.ChannelActivity
import com.jet.im.kit.providers.FragmentProviders
import com.juggle.chat.chatroom.ChatRoomListFragment

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
            val backgroundRedId =
                if (isDarkMode) com.jet.im.kit.R.color.background_600 else com.jet.im.kit.R.color.background_50
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

                    1 -> {
                        CustomTabView(this@GroupChannelMainActivity).apply {
                            setBadgeVisibility(View.GONE)
                            setTitle(getString(R.string.text_tab_friends))
                            setIcon(R.drawable.icon_chat_filled)
                        }
                    }

                    2 -> {
                        CustomTabView(this@GroupChannelMainActivity).apply {
                            setBadgeVisibility(View.GONE)
                            setTitle(getString(R.string.text_tab_groups))
                            setIcon(R.drawable.icon_chat_filled)
                        }
                    }
                    3 -> {
                        CustomTabView(this@GroupChannelMainActivity).apply {
                            setBadgeVisibility(View.GONE)
                            setTitle(getString(R.string.text_tab_chatroom))
                            setIcon(R.drawable.icon_chat_filled)
                        }
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
//        SendbirdChat.getTotalUnreadMessageCount(
//            GroupChannelTotalUnreadMessageCountParams(),
//            UnreadMessageCountHandler { totalCount: Int, _: Int, e: SendbirdException? ->
//                if (e != null) {
//                    return@UnreadMessageCountHandler
//                }
//                if (totalCount > 0) {
//                    unreadCountTab.setBadgeVisibility(View.VISIBLE)
//                    unreadCountTab.setBadgeCount(if (totalCount > 99) getString(R.string.text_tab_badge_max_count) else totalCount.toString())
//                } else {
//                    unreadCountTab.setBadgeVisibility(View.GONE)
//                }
//            })
//        SendbirdChat.addUserEventHandler(USER_EVENT_HANDLER_KEY, object : UserEventHandler() {
//            override fun onFriendsDiscovered(users: List<User>) {}
//            override fun onTotalUnreadMessageCountChanged(unreadMessageCount: UnreadMessageCount) {
//                val totalCount = unreadMessageCount.groupChannelCount
//                if (totalCount > 0) {
//                    unreadCountTab.setBadgeVisibility(View.VISIBLE)
//                    unreadCountTab.setBadgeCount(if (totalCount > 99) getString(R.string.text_tab_badge_max_count) else totalCount.toString())
//                } else {
//                    unreadCountTab.setBadgeVisibility(View.GONE)
//                }
//            }
//        })
    }

    override fun onPause() {
        super.onPause()
    }

    private fun redirectChannelIfNeeded(intent: Intent?) {
        if (intent == null) return
        if (intent.flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY == Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) {
            intent.removeExtra(com.jet.im.kit.consts.StringSet.KEY_CONVERSATION_TYPE)
            intent.removeExtra(com.jet.im.kit.consts.StringSet.KEY_CONVERSATION_ID)
        }
        if (intent.hasExtra(com.jet.im.kit.consts.StringSet.KEY_CONVERSATION_ID)) {
            val type =
                intent.getIntExtra(com.jet.im.kit.consts.StringSet.KEY_CONVERSATION_TYPE, 0)
            val id =
                intent.getStringExtra(com.jet.im.kit.consts.StringSet.KEY_CONVERSATION_ID)
                    ?: return
            if (intent.hasExtra(StringSet.PUSH_REDIRECT_MESSAGE_ID)) {
                val messageId = intent.getLongExtra(StringSet.PUSH_REDIRECT_MESSAGE_ID, 0L)
                if (messageId > 0L) {
                    val messageId = intent.getLongExtra(StringSet.PUSH_REDIRECT_MESSAGE_ID, 0L)
                    startActivity(
                        ChannelActivity.newRedirectToMessageThreadIntent(
                            this,
                            type,
                            id,
                            messageId
                        )
                    )
                    intent.removeExtra(StringSet.PUSH_REDIRECT_MESSAGE_ID)
                }
            } else {
                startActivity(ChannelActivity.newIntent(this, type, id))
            }
            intent.removeExtra(com.jet.im.kit.consts.StringSet.KEY_CONVERSATION_TYPE)
            intent.removeExtra(com.jet.im.kit.consts.StringSet.KEY_CONVERSATION_ID)
        }
    }

    private class MainAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = PAGE_SIZE
        override fun createFragment(position: Int): Fragment {
            var fragment: Fragment
            if (position == 0) {
                fragment = FragmentProviders.channelList.provide(Bundle())
            } else if (position == 1) {
                fragment = FriendListFragment()
            } else if (position == 2) {
                fragment = GroupListFragment()
            }
            else if (position == 3) {
                fragment = ChatRoomListFragment()
            }else {
                fragment = SampleSettingsFragment()
            }
            return fragment
        }

        companion object {
            private const val PAGE_SIZE = 5
        }
    }

    companion object {
        private val USER_EVENT_HANDLER_KEY = "USER_EVENT_HANDLER_KEY" + System.currentTimeMillis()
    }
}
