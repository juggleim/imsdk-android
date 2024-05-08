package com.example.demo.customization.channel

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sendbird.android.channel.GroupChannel
import com.jet.im.kit.activities.ChannelActivity
import com.jet.im.kit.interfaces.providers.ChannelModuleProvider
import com.jet.im.kit.modules.ChannelModule
import com.jet.im.kit.modules.components.ChannelHeaderComponent
import com.jet.im.kit.providers.ModuleProviders
import com.example.demo.customization.GroupChannelRepository
import com.example.demo.databinding.ViewCustomHeaderBinding

/**
 * In this sample, the UI of the channel header is changed, and the title and description are updated.
 *
 * step 1. Create a [CustomChannelHeaderComponent] and set it to [ChannelModule.setHeaderComponent].
 * step 2. Create a [ChannelModuleProvider] and set it to [ModuleProviders.channel].
 * step 3. Start [ChannelActivity] with the channel url.
 *
 * The settings for the custom Provider are set up here to show the steps in the sample,
 * but in your application it is recommended to set it up in the Application class.
 */
fun showChannelHeaderSample(activity: Activity) {
    ModuleProviders.channel = ChannelModuleProvider { context, _ ->
        // You can apply the customized module.
        val module = ChannelModule(context)
        // You can apply the customized component.
        module.setHeaderComponent(CustomChannelHeaderComponent())
        module
    }

    GroupChannelRepository.getRandomChannel(activity) { channel ->
        activity.startActivity(
            ChannelActivity.newIntent(activity, channel.url)
        )
    }
}

/**
 * This class is used to customize the channel header.
 * In [onCreateView], you can inflate a custom view and return it.
 *
 * step 1. Inherit [ChannelHeaderComponent] and override [onCreateView].
 * step 2. Create a custom view and return it in [onCreateView].
 * (Optional) step 3. Override [notifyChannelChanged] and [notifyHeaderDescriptionChanged] to update the custom view.
 */
class CustomChannelHeaderComponent : ChannelHeaderComponent() {
    var binding: ViewCustomHeaderBinding? = null

    override fun onCreateView(context: Context, inflater: LayoutInflater, parent: ViewGroup, args: Bundle?): View {
        binding = ViewCustomHeaderBinding.inflate(inflater, parent, false)
        return requireNotNull(binding).root
    }

    override fun notifyChannelChanged(channel: GroupChannel) {
        binding?.title?.text = channel.name
    }

    override fun notifyHeaderDescriptionChanged(description: String?) {
        binding?.description?.visibility = if (description.isNullOrEmpty()) View.GONE else View.VISIBLE
        binding?.description?.text = description
    }
}
