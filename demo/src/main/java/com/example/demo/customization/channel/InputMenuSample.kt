package com.example.demo.customization.channel

import android.app.Activity
import android.app.AlertDialog
import android.widget.Toast
import com.sendbird.android.channel.GroupChannel
import com.jet.im.kit.activities.ChannelActivity
import com.jet.im.kit.fragments.ChannelFragment
import com.jet.im.kit.interfaces.providers.ChannelFragmentProvider
import com.jet.im.kit.modules.components.MessageInputComponent
import com.jet.im.kit.providers.FragmentProviders
import com.example.demo.customization.GroupChannelRepository
import com.jet.im.kit.vm.ChannelViewModel

/**
 * In this sample, the menu that appears when you click the input menu is customized.
 * To customize the input menu, you can use the [ChannelFragment.Builder.setOnInputLeftButtonClickListener] or a custom fragment.
 *
 * step 1. Create [InputMenuSampleFragment] and set it to [ChannelFragment.Builder.setCustomFragment].
 * step 2. Set custom [ChannelFragmentProvider] to [FragmentProviders.channel].
 * step 3. Start [ChannelActivity] with the channel url.
 *
 * The settings for the custom Provider are set up here to show the steps in the sample,
 * but in your application it is recommended to set it up in the Application class.
 */
fun showInputMenuSample(activity: Activity) {
    FragmentProviders.channel = ChannelFragmentProvider { channelUrl, args ->
        ChannelFragment.Builder(channelUrl).withArguments(args)
            .setCustomFragment(InputMenuSampleFragment())
            .setUseHeader(true)
            .build()
    }

    GroupChannelRepository.getRandomChannel(activity) { channel ->
        activity.startActivity(
            ChannelActivity.newIntent(activity, channel.url)
        )
    }
}

/**
 * This class is used to customize the input menu.
 * You can use the default menu methods such as [ChannelFragment.takePhoto], [ChannelFragment.takeVideo], [ChannelFragment.takeFile] and so on.
 *
 * step 1. Inherit [ChannelFragment] and override [onBindMessageInputComponent] to set the custom menu click listener.
 * step 2. Implement to show the custom menu.
 */
class InputMenuSampleFragment : ChannelFragment() {
    override fun onBindMessageInputComponent(inputComponent: MessageInputComponent, viewModel: ChannelViewModel, channel: GroupChannel?) {
        super.onBindMessageInputComponent(inputComponent, viewModel, channel)
        inputComponent.setOnInputLeftButtonClickListener {
            showInputMenu()
        }
    }

    private fun showInputMenu() {
        val menu = arrayOf(
            "Take Camera",
            "Take Photo",
            "Take Custom Menu"
        )
        val dialogBuilder = AlertDialog.Builder(requireContext())
            .setItems(menu) { _, which ->
                when (which) {
                    0 -> {
                        takeCamera()
                    }
                    1 -> {
                        takePhoto()
                    }
                    2 -> {
                        takeCustomMenu()
                    }
                }
            }
        dialogBuilder.show()
    }

    private fun takeCustomMenu() {
        Toast.makeText(requireContext(), "Custom menu", Toast.LENGTH_SHORT).show()
    }
}
