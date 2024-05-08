package com.example.demo.basic.openchannel.livestream

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.sendbird.android.SendbirdChat.addChannelHandler
import com.sendbird.android.SendbirdChat.removeChannelHandler
import com.sendbird.android.channel.BaseChannel
import com.sendbird.android.channel.OpenChannel
import com.sendbird.android.exception.SendbirdException
import com.sendbird.android.handler.OpenChannelHandler
import com.sendbird.android.message.BaseMessage
import com.sendbird.android.user.User
import com.jet.im.kit.consts.KeyboardDisplayType
import com.jet.im.kit.fragments.OpenChannelFragment
import com.example.demo.R
import com.example.demo.common.consts.StringSet
import com.example.demo.common.extensions.isUsingDarkTheme
import com.example.demo.common.extensions.toggleVisibility
import com.example.demo.common.preferences.PreferenceUtils
import com.example.demo.databinding.ActivityLiveStreamBinding
import com.jet.im.kit.utils.ContextUtils
import org.json.JSONException
import org.json.JSONObject
import java.util.Locale
import java.lang.ref.WeakReference

/**
 * Displays an open channel screen used for live stream.
 */
class LiveStreamActivity : AppCompatActivity() {
    private val CHANNEL_HANDLER_KEY = javaClass.simpleName + System.currentTimeMillis()
    private lateinit var binding: ActivityLiveStreamBinding
    private lateinit var creatorName: String
    private lateinit var channelUrl: String
    private lateinit var inputText: String
    private val hideHandler = HideHandler(this)

    /**
     * Hides the system UI for full screen.
     */
    private class HideHandler(activity: LiveStreamActivity) : Handler(Looper.getMainLooper()) {
        private val weakReference: WeakReference<LiveStreamActivity>

        init {
            weakReference = WeakReference(activity)
        }

        override fun handleMessage(msg: Message) {
            val activity = weakReference.get()
            activity?.hideSystemUI()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(StringSet.KEY_INPUT_TEXT, inputText)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLiveStreamBinding.inflate(layoutInflater).apply {
            setContentView(root)
            if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                sbFragmentContainer.setBackgroundResource(android.R.color.transparent)
            } else {
                val isDark = PreferenceUtils.themeMode.isUsingDarkTheme()
                sbFragmentContainer.setBackgroundResource(if (isDark) com.jet.im.kit.R.color.background_600 else com.jet.im.kit.R.color.background_50)
            }
            addChannelHandler()
            ivLive.visibility = View.VISIBLE
            ivLive.setOnClickListener { groupLiveControl.toggleVisibility() }
            ivClose.setOnClickListener { finish() }
            ivChatToggle?.setOnClickListener {
                if (sbFragmentContainer.visibility == View.GONE) {
                    sbFragmentContainer.animate()
                        .setDuration(300)
                        .alpha(1.0f)
                        .translationX(0.0f)
                        .setListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationStart(animation: Animator) {
                                super.onAnimationStart(animation)
                                sbFragmentContainer.visibility = View.VISIBLE
                            }
                        })
                    ivChatToggle.animate()?.setDuration(300)?.translationX(0.0f)
                    ivChatToggle.setImageResource(R.drawable.ic_chat_hide)
                } else {
                    sbFragmentContainer.animate()
                        .setDuration(300)
                        .alpha(0.0f)
                        .translationX(sbFragmentContainer.width.toFloat())
                        .setListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                super.onAnimationEnd(animation)
                                sbFragmentContainer.visibility = View.GONE
                            }
                        })
                    ivChatToggle.animate()?.setDuration(300)?.translationX(sbFragmentContainer.width.toFloat())
                    ivChatToggle.setImageResource(R.drawable.ic_chat_show)
                }
            }
            channelUrl = intent.getStringExtra(StringSet.KEY_CHANNEL_URL) ?: ""
            if (channelUrl.isEmpty()) {
                ContextUtils.toastError(this@LiveStreamActivity, com.jet.im.kit.R.string.sb_text_error_get_channel)
                return
            }
            OpenChannel.getChannel(channelUrl) { openChannel: OpenChannel?, e: SendbirdException? ->
                if (e != null) {
                    return@getChannel
                }
                if (this@LiveStreamActivity.isFinishing || openChannel == null) return@getChannel
                updateParticipantCount(openChannel.participantCount)
                try {
                    val channelData = LiveStreamingChannelData(JSONObject(openChannel.data))
                    creatorName = channelData.creator?.nickname ?: ""
                    Glide.with(root.context)
                        .load(channelData.liveUrl)
                        .override(ivLive.measuredWidth, ivLive.height)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .error(com.jet.im.kit.R.color.background_600)
                        .into(ivLive)
                } catch (ex: JSONException) {
                    ex.printStackTrace()
                    creatorName = ""
                }
                inputText = savedInstanceState?.getString(StringSet.KEY_INPUT_TEXT) ?: ""
                savedInstanceState?.clear()
                val fragment = createOpenChannelFragment(channelUrl)
                val manager = supportFragmentManager
                manager.popBackStack()
                manager.beginTransaction()
                    .replace(R.id.sb_fragment_container, fragment)
                    .commit()
            }
        }
    }

    private fun addChannelHandler() {
        addChannelHandler(CHANNEL_HANDLER_KEY, object : OpenChannelHandler() {
            override fun onMessageReceived(channel: BaseChannel, message: BaseMessage) {}
            override fun onUserEntered(channel: OpenChannel, user: User) {
                if (channel.url == channelUrl) {
                    updateParticipantCount(channel.participantCount)
                }
            }

            override fun onUserExited(channel: OpenChannel, user: User) {
                if (channel.url == channelUrl) {
                    updateParticipantCount(channel.participantCount)
                }
            }
        })
    }

    private fun updateParticipantCount(count: Int) {
        var text = count.toString()
        if (count > 1000) {
            text = String.format(Locale.US, "%.1fK", count / 1000f)
        }
        binding.tvParticipantCount.text = String.format(getString(R.string.text_participants), text)
    }

    override fun onDestroy() {
        super.onDestroy()
        hideHandler.removeMessages(0)
        removeChannelHandler(CHANNEL_HANDLER_KEY)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            delayedHide()
        } else {
            hideHandler.removeMessages(0)
        }
    }

    private fun hideSystemUI() {
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LOW_PROFILE or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        }
    }

    private fun delayedHide() {
        hideHandler.removeMessages(0)
        hideHandler.sendEmptyMessageDelayed(0, 300)
    }

    /**
     * Creates `OpenChannelFragment` with channel url.
     *
     *
     * In preparation for screen configuration change, the value is initialized.
     *
     * @param channelUrl The channel url to be applied to this screen
     * @return `OpenChannelFragment` instance
     */
    private fun createOpenChannelFragment(channelUrl: String): OpenChannelFragment {
        val args = Bundle()
        args.putString(StringSet.KEY_CHANNEL_URL, channelUrl)
        args.putString(StringSet.KEY_HEADER_DESCRIPTION, creatorName)
        args.putString(StringSet.KEY_INPUT_TEXT, inputText)
        args.putSerializable(StringSet.KEY_KEYBOARD_DISPLAY_TYPE, KeyboardDisplayType.Dialog)
        return OpenChannelFragment.Builder(channelUrl)
            .withArguments(args)
            .setCustomFragment(LiveStreamChannelFragment())
            .setUseHeader(true)
            .build()
    }

    companion object {
        fun newIntent(context: Context, channelUrl: String): Intent {
            val intent = Intent(context, LiveStreamActivity::class.java)
            intent.putExtra(StringSet.KEY_CHANNEL_URL, channelUrl)
            return intent
        }
    }
}
