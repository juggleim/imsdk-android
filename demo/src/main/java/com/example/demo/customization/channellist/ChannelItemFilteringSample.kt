package com.example.demo.customization.channellist

import android.app.Activity
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.channel.SuperChannelFilter
import com.sendbird.android.channel.query.GroupChannelListQueryOrder
import com.sendbird.android.params.GroupChannelListQueryParams
import com.jet.im.kit.activities.ChannelListActivity
import com.jet.im.kit.fragments.ChannelListFragment
import com.jet.im.kit.interfaces.providers.ChannelListFragmentProvider
import com.jet.im.kit.providers.FragmentProviders

/**
 * In this sample, use [GroupChannelListQueryParams] to alphabetize the list by channel name
 * and filter out supergroup channels.
 *
 * step 1. Create a [GroupChannelListQueryParams] to filter the channel list.
 * step 2. Set [GroupChannelListQueryParams] to [ChannelListFragment.Builder.setGroupChannelListQuery].
 * step 3. Start [ChannelListActivity].
 *
 * The settings for the custom Provider are set up here to show the steps in the sample,
 * but in your application it is recommended to set it up in the Application class.
 */
fun showChannelItemFilteringSample(activity: Activity) {
    FragmentProviders.channelList = ChannelListFragmentProvider {
        ChannelListFragment.Builder()
            .setGroupChannelListQuery(
                GroupChannel.createMyGroupChannelListQuery(
                    GroupChannelListQueryParams().apply {
                        order = GroupChannelListQueryOrder.CHANNEL_NAME_ALPHABETICAL
                        superChannelFilter = SuperChannelFilter.NONSUPER_CHANNEL_ONLY
                        /*
                        includeEmpty = false
                        includeFrozen = true
                        unreadChannelFilter = UnreadChannelFilter.ALL
                        hiddenChannelFilter = HiddenChannelFilter.ALL
                        myMemberStateFilter = MyMemberStateFilter.ALL
                        ...
                         */
                    }
                )
            )
            .build()
    }

    val intent = ChannelListActivity.newIntent(activity.applicationContext)
    activity.startActivity(intent)
}
