package com.example.demo.basic.openchannel

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.sendbird.android.params.OpenChannelListQueryParams
import com.jet.im.kit.SendbirdUIKit
import com.jet.im.kit.fragments.OpenChannelListFragment
import com.example.demo.R
import com.example.demo.basic.openchannel.community.CommunityListFragment
import com.example.demo.basic.openchannel.livestream.LiveStreamListAdapter
import com.example.demo.basic.openchannel.livestream.LiveStreamListFragment
import com.example.demo.common.SampleSettingsFragment
import com.example.demo.common.consts.StringSet
import com.example.demo.common.extensions.isUsingDarkTheme
import com.example.demo.common.preferences.PreferenceUtils
import com.example.demo.common.widgets.CustomTabView
import com.example.demo.databinding.ActivityOpenChannelMainBinding

class OpenChannelMainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val themeResId = SendbirdUIKit.getDefaultThemeMode().resId
        setTheme(themeResId)
        ActivityOpenChannelMainBinding.inflate(layoutInflater).apply {
            setContentView(root)
            val context = this@OpenChannelMainActivity
            val isDarkMode = PreferenceUtils.themeMode.isUsingDarkTheme()
            background.setBackgroundResource(if (isDarkMode) com.jet.im.kit.R.color.background_600 else com.jet.im.kit.R.color.background_50)
            titleBar.setBackgroundResource(if (isDarkMode) com.jet.im.kit.R.color.background_500 else com.jet.im.kit.R.color.background_50)
            titleBar.setTitleTextColor(
                ResourcesCompat.getColor(
                    resources, if (isDarkMode) com.jet.im.kit.R.color.ondark_01 else com.jet.im.kit.R.color.onlight_01, null
                )
            )
            description.setTextColor(
                ResourcesCompat.getColor(
                    resources, if (isDarkMode) com.jet.im.kit.R.color.ondark_02 else com.jet.im.kit.R.color.onlight_02, null
                )
            )
            setSupportActionBar(titleBar)
            viewPager.adapter = MainAdapter(this@OpenChannelMainActivity)
            val backgroundRedId = if (isDarkMode) com.jet.im.kit.R.color.background_600 else com.jet.im.kit.R.color.background_50
            tabLayout.setBackgroundResource(backgroundRedId)

            TabLayoutMediator(tabLayout, viewPager) { tab: TabLayout.Tab, position: Int ->
                when (position) {
                    TAB_LIVE_STREAMING -> tab.customView = CustomTabView(context).apply {
                        setBadgeVisibility(View.GONE)
                        setTitle(getString(R.string.text_live_streams))
                        setIcon(R.drawable.icon_streaming)
                    }
                    TAB_COMMUNITY -> tab.customView = CustomTabView(context).apply {
                        setBadgeVisibility(View.GONE)
                        setTitle(getString(R.string.text_community))
                        setIcon(R.drawable.icon_channels)
                    }
                    TAB_SETTINGS -> tab.customView = CustomTabView(context).apply {
                        setBadgeVisibility(View.GONE)
                        setTitle(getString(R.string.text_tab_settings))
                        setIcon(R.drawable.icon_settings_filled)
                    }
                }
            }.attach()
            description.visibility = View.VISIBLE
            description.setText(R.string.text_live_streaming_description)
            supportActionBar?.setTitle(R.string.text_live_streams)
            viewPager.offscreenPageLimit = 3
            viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                /**
                 * This method will be invoked when a new page becomes selected. Animation is not
                 * necessarily complete.
                 *
                 * @param position Position index of the new selected page.
                 */
                override fun onPageSelected(position: Int) {
                    when (position) {
                        TAB_LIVE_STREAMING -> {
                            description.visibility = View.VISIBLE
                            description.setText(R.string.text_live_streaming_description)
                            supportActionBar?.setTitle(R.string.text_live_streams)
                        }
                        TAB_COMMUNITY -> {
                            description.visibility = View.GONE
                            supportActionBar?.setTitle(R.string.text_community)
                        }
                        TAB_SETTINGS -> {
                            description.visibility = View.GONE
                            supportActionBar?.setTitle(R.string.text_tab_settings)
                        }
                    }
                }
            })
        }
    }

    companion object {
        private const val TAB_LIVE_STREAMING = 0
        private const val TAB_COMMUNITY = 1
        private const val TAB_SETTINGS = 2
    }

    private class MainAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> {
                    val params = OpenChannelListQueryParams()
                    params.customTypeFilter = StringSet.SB_LIVE_TYPE
                    OpenChannelListFragment.Builder()
                        .setCustomFragment(LiveStreamListFragment())
                        .setOpenChannelListAdapter(
                            LiveStreamListAdapter()
                        )
                        .setUseHeader(false)
                        .setUseRefreshLayout(false)
                        .setCustomQueryParams(params)
                        .build()
                }

                1 -> {
                    val params = OpenChannelListQueryParams()
                    params.customTypeFilter = StringSet.SB_COMMUNITY_TYPE
                    OpenChannelListFragment.Builder()
                        .setCustomFragment(CommunityListFragment())
                        .setUseHeader(false)
                        .setCustomQueryParams(params)
                        .build()
                }

                else -> {
                    val fragment = SampleSettingsFragment()
                    val bundle = Bundle()
                    bundle.putBoolean(StringSet.SETTINGS_USE_HEADER, false)
                    bundle.putBoolean(StringSet.SETTINGS_USE_DO_NOT_DISTURB, false)
                    fragment.arguments = bundle
                    fragment
                }
            }
        }

        /**
         * Returns the total number of items in the data set held by the adapter.
         *
         * @return The total number of items in this adapter.
         */
        override fun getItemCount(): Int {
            return PAGE_SIZE
        }

        companion object {
            private const val PAGE_SIZE = 3
        }
    }
}
