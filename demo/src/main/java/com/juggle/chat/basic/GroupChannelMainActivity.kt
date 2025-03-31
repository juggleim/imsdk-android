package com.juggle.chat.basic

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.jet.im.kit.SendbirdUIKit
import com.jet.im.kit.activities.ChannelActivity
import com.jet.im.kit.fragments.ChannelListFragment
import com.juggle.chat.R
import com.juggle.chat.bots.BotListFragment
import com.juggle.chat.common.SampleSettingsFragment
import com.juggle.chat.common.consts.StringSet
import com.juggle.chat.common.extensions.isUsingDarkTheme
import com.juggle.chat.common.preferences.PreferenceUtils
import com.juggle.chat.common.widgets.CustomTabView
import com.juggle.chat.contacts.FriendListFragment
import com.juggle.chat.contacts.add.AddFriendListActivity
import com.juggle.chat.contacts.group.select.SelectGroupMemberActivity
import com.juggle.chat.databinding.ActivityGroupChannelMainBinding
import com.juggle.chat.settings.MorePopWindow
import com.juggle.chat.settings.MorePopWindow.OnPopWindowItemClickListener
import com.juggle.im.JIM
import com.juggle.im.interfaces.IConversationManager.IConversationListener
import com.juggle.im.model.Conversation
import com.juggle.im.model.ConversationInfo

class GroupChannelMainActivity : AppCompatActivity(), IConversationListener, OnPopWindowItemClickListener {
    private lateinit var binding: ActivityGroupChannelMainBinding
    private lateinit var conversationUnreadCountTab: CustomTabView
    private lateinit var friendUnreadCountTab: CustomTabView
    private val key = "GroupChannelMainActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(SendbirdUIKit.getDefaultThemeMode().resId)
        binding = ActivityGroupChannelMainBinding.inflate(layoutInflater).apply {
            setContentView(root)
            viewPager.adapter = MainAdapter(this@GroupChannelMainActivity)
            viewPager.offscreenPageLimit = 5
            val isDarkMode = PreferenceUtils.themeMode.isUsingDarkTheme()
            val backgroundRedId =
                if (isDarkMode) com.jet.im.kit.R.color.background_600 else com.jet.im.kit.R.color.background_50
            tabLayout.setBackgroundResource(backgroundRedId)
            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                tab.customView = when (position) {
                    0 -> {
                        conversationUnreadCountTab = CustomTabView(this@GroupChannelMainActivity).apply {
                            setBadgeVisibility(View.GONE)
                            setTitle(getString(R.string.text_tab_conversations))
                            setIcon(R.drawable.icon_chat_filled)
                        }
                        conversationUnreadCountTab
                    }

                    1 -> {
                        friendUnreadCountTab = CustomTabView(this@GroupChannelMainActivity).apply {
                            setBadgeVisibility(View.GONE)
                            setTitle(getString(R.string.text_tab_friends))
                            setIcon(com.jet.im.kit.R.drawable.icon_members)
                        }
                        friendUnreadCountTab
                    }

                    2 -> {
                        CustomTabView(this@GroupChannelMainActivity).apply {
                            setBadgeVisibility(View.GONE)
                            setTitle(getString(R.string.text_bots))
                            setIcon(R.drawable.icon_bots)
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
            tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    // 去掉切换动画
                    viewPager.setCurrentItem(tab.position, false)
                }
                override fun onTabUnselected(tab: TabLayout.Tab) {}
                override fun onTabReselected(tab: TabLayout.Tab) {}
            })
            redirectChannelIfNeeded(intent)
        }
        JIM.getInstance().conversationManager.addListener(key, this)
    }

    override fun onResume() {
        super.onResume()
        val conversationTypes = intArrayOf(Conversation.ConversationType.PRIVATE.value, Conversation.ConversationType.GROUP.value)
        JIM.getInstance().conversationManager.getUnreadCountWithTypes(conversationTypes)

        loadUnreadCount()
    }

    override fun onDestroy() {
        super.onDestroy()
        JIM.getInstance().conversationManager.removeListener(key)
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

    private fun loadUnreadCount() {
        val friendConversation = Conversation(Conversation.ConversationType.SYSTEM, SendbirdUIKit.FRIEND_CONVERSATION_ID)
        val friendConversationInfo = JIM.getInstance().conversationManager.getConversationInfo(friendConversation)
        var friendUnreadCount = 0
        if (friendConversationInfo != null) {
            friendUnreadCount = friendConversationInfo.unreadCount
        }
        val totalUnreadCount = JIM.getInstance().conversationManager.totalUnreadCount
        val conversationUnreadCount = totalUnreadCount - friendUnreadCount

        if (friendUnreadCount > 0) {
            friendUnreadCountTab.setBadgeVisibility(View.VISIBLE)
            friendUnreadCountTab.setBadgeCount(if (friendUnreadCount > 99) getString(R.string.text_tab_badge_max_count) else friendUnreadCount.toString())
        } else {
            friendUnreadCountTab.setBadgeVisibility(View.GONE)
        }
        if (conversationUnreadCount > 0) {
            conversationUnreadCountTab.setBadgeVisibility(View.VISIBLE)
            conversationUnreadCountTab.setBadgeCount(if (conversationUnreadCount > 99) getString(R.string.text_tab_badge_max_count) else conversationUnreadCount.toString())
        } else {
            conversationUnreadCountTab.setBadgeVisibility(View.GONE)
        }
    }

    private class MainAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        val activity = fa

        override fun getItemCount(): Int = PAGE_SIZE
        override fun createFragment(position: Int): Fragment {
            val fragment: Fragment
            if (position == 0) {
                fragment = ChannelListFragment.Builder().setOnHeaderRightButtonClickListener(object : View.OnClickListener {
                    override fun onClick(v: View?) {
                        if (activity is GroupChannelMainActivity) {
                            val morePopWindow = MorePopWindow(activity, activity)

                            val scale = activity.resources.displayMetrics.density
                            val marginEnd = (12 * scale + 0.5f).toInt()
                            val popSelfXOffset =
                                activity.resources.getDimension(com.jet.im.kit.R.dimen.sb_size_160) - v!!.width

                            morePopWindow.showPopupWindow(v, 0.8f, (-popSelfXOffset).toInt(), 0)
                        }
                    }
                }).withArguments(Bundle()).setUseHeader(true).build()

//                fragment = FragmentProviders.channelList.provide(Bundle())
            } else if (position == 1) {
                fragment = FriendListFragment()
            } else if (position == 2) {
                fragment =
                    BotListFragment()
            } else {
                fragment = SampleSettingsFragment()
            }
            return fragment
        }

        companion object {
            private const val PAGE_SIZE = 4
        }
    }

    companion object {
        private val USER_EVENT_HANDLER_KEY = "USER_EVENT_HANDLER_KEY" + System.currentTimeMillis()
    }

    override fun onConversationInfoAdd(conversationInfoList: MutableList<ConversationInfo>?) {
    }

    override fun onConversationInfoUpdate(conversationInfoList: MutableList<ConversationInfo>?) {
    }

    override fun onConversationInfoDelete(conversationInfoList: MutableList<ConversationInfo>?) {
    }

    override fun onTotalUnreadMessageCountUpdate(count: Int) {
        loadUnreadCount()
    }

    override fun onCreateGroupClick() {
        startActivity(SelectGroupMemberActivity.newIntent(this, null, 0))
    }

    override fun onAddFriendClick() {
        startActivity(AddFriendListActivity.newIntent(this))
    }

    override fun onScanClick() {
        TODO("Not yet implemented")
    }
}
