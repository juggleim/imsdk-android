package com.example.demo.customization.channel

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import com.jet.im.kit.activities.ChannelActivity
import com.jet.im.kit.interfaces.providers.ChannelModuleProvider
import com.jet.im.kit.modules.ChannelModule
import com.jet.im.kit.providers.ModuleProviders
import com.example.demo.customization.GroupChannelRepository
import com.example.demo.databinding.ViewChannelLayoutSampleBinding

/**
 * In this sample, the Floating action button is added to the channel layout.
 *
 * step 1. Create a [ChannelLayoutSampleModule] and set it to [ModuleProviders.channel].
 * step 2. Start [ChannelActivity] with the channel url.
 *
 * The settings for the custom Provider are set up here to show the steps in the sample,
 * but in your application it is recommended to set it up in the Application class.
 */
fun showChannelLayoutSample(activity: Activity) {
    ModuleProviders.channel = ChannelModuleProvider { context, _ ->
        ChannelLayoutSampleModule(context)
    }

    GroupChannelRepository.getRandomChannel(activity) { channel ->
        activity.startActivity(ChannelActivity.newIntent(activity, channel.url))
    }
}

/**
 * This class is used to customize the channel layout.
 * In [onCreateView], you can inflate the layout you want and add the channel view to it.
 *
 * step 1. Inherit [ChannelModule] and override [onCreateView].
 * step 2. Create views and layout views in [onCreateView].
 */
class ChannelLayoutSampleModule(context: Context) : ChannelModule(context) {
    override fun onCreateView(context: Context, inflater: LayoutInflater, args: Bundle?): View {
        val binding = ViewChannelLayoutSampleBinding.inflate(inflater)
        val channelView = super.onCreateView(context, inflater, args)
        binding.channelView.addView(channelView)
        binding.fab.setOnClickListener {
            Toast.makeText(context, "Floating Button Clicked", Toast.LENGTH_SHORT).show()
        }
        return binding.root
    }
}
