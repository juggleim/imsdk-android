package com.jet.im.kit.internal.ui.messages

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.sendbird.android.message.FileMessage
import com.jet.im.kit.R
import com.jet.im.kit.databinding.SbViewVoiceMessageBinding
import com.jet.im.kit.internal.extensions.setAppearance
import com.jet.im.kit.internal.model.VoicePlayer
import com.jet.im.kit.internal.model.VoicePlayerManager
import com.jet.im.kit.log.Logger
import com.jet.im.kit.utils.MessageUtils
import com.jet.im.kit.utils.TextUtils
import com.jet.im.kit.utils.ViewUtils
import com.juggle.im.model.Message
import com.juggle.im.model.messages.VoiceMessage

internal class VoiceMessageView @JvmOverloads internal constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = R.attr.sb_widget_my_voice_message
) : FrameLayout(context, attrs, defStyle) {
    private val binding: SbViewVoiceMessageBinding
    private val onUpdateListener: VoicePlayer.OnUpdateListener
    private val onProgressUpdateListener: VoicePlayer.OnProgressUpdateListener
    private var key: String? = null
    private var duration: Int = 0

    init {
        Logger.i("_________init() this=$this")
        val a =
            context.theme.obtainStyledAttributes(attrs, R.styleable.MessageView_File, defStyle, 0)
        try {
            binding = SbViewVoiceMessageBinding.inflate(LayoutInflater.from(context), this, true)
            onUpdateListener = object : VoicePlayer.OnUpdateListener {
                override fun onUpdated(key: String, status: VoicePlayer.Status) {
                    if (this@VoiceMessageView.key == key) drawPlayerStatus(status)
                }
            }
            onProgressUpdateListener = object : VoicePlayer.OnProgressUpdateListener {
                override fun onProgressUpdated(
                    key: String,
                    status: VoicePlayer.Status,
                    milliseconds: Int,
                    duration: Int
                ) {
                    Logger.i("VoiceMessageView >> OnProgressUpdateListener status : $status, millis : $milliseconds")
                    if (this@VoiceMessageView.key != key) return
                    if (duration == 0) return
                    ViewUtils.drawTimeline(
                        binding.timelineView,
                        if (status == VoicePlayer.Status.STOPPED) duration else duration - milliseconds
                    )
                    ViewUtils.drawVoicePlayerProgress(
                        binding.voiceProgressView,
                        milliseconds,
                        duration
                    )
                }
            }
        } finally {
            a.recycle()
        }
    }

    fun setLoadingDrawable(drawable: Drawable?) {
        binding.loading.indeterminateDrawable = drawable
    }

    fun setProgressCornerRadius(radius: Float) {
        binding.voiceProgressView.cornerRadius = radius
    }

    fun setProgressTrackColor(trackColor: ColorStateList?) {
        binding.voiceProgressView.trackColor = trackColor
    }

    fun setProgressProgressColor(progressColor: ColorStateList?) {
        binding.voiceProgressView.progressColor = progressColor
    }

    fun setTimelineTextAppearance(textAppearance: Int) {
        binding.timelineView.setAppearance(context, textAppearance)
    }

    fun setPlayButtonImageDrawable(drawable: Drawable?) {
        binding.ibtnPlay.setImageDrawable(drawable)
    }

    fun setPauseButtonImageDrawable(drawable: Drawable?) {
        binding.ibtnPause.setImageDrawable(drawable)
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        Logger.i("_________VoiceMessageView::onVisibilityChanged()")
        super.onVisibilityChanged(changedView, visibility)
        if (visibility != VISIBLE) VoicePlayerManager.pause()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        Logger.i("_________VoiceMessageView::onAttachedToWindow()")
        key?.let {
            drawVoiceMessage(it)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        Logger.i("_________VoiceMessageView::onDetachedFromWindow()")
        key?.let {
            VoicePlayerManager.removeOnUpdateListener(it, onUpdateListener)
            VoicePlayerManager.removeOnProgressListener(it, onProgressUpdateListener)
        }
    }

    fun drawVoiceMessage(fileMessage: FileMessage) {
        Logger.i("_________VoiceMessageView::drawVoiceMessage()")
        val key = MessageUtils.getVoiceMessageKey(fileMessage)
        this@VoiceMessageView.key = key
        duration = MessageUtils.extractDuration(fileMessage)
        binding.ibtnPlay.setOnClickListener {
//            VoicePlayerManager.play(context, key, fileMessage, onUpdateListener, onProgressUpdateListener)
        }
        binding.ibtnPause.setOnClickListener {
            VoicePlayerManager.pause()
        }
        drawVoiceMessage(key)
    }

    fun drawVoiceMessage(message: Message, fileMessage: VoiceMessage) {
        Logger.i("_________VoiceMessageView::drawVoiceMessage()")
        val key = MessageUtils.getVoiceMessageKey(message)
        this@VoiceMessageView.key = key
        duration = fileMessage.duration
        binding.ibtnPlay.setOnClickListener {
            VoicePlayerManager.play(
                context,
                message.messageId,
                key,
                fileMessage,
                onUpdateListener,
                onProgressUpdateListener
            )
        }
        binding.ibtnPause.setOnClickListener {
            VoicePlayerManager.pause()
        }
        drawVoiceMessage(key)
    }

    private fun drawVoiceMessage(key: String) {
        if (VoicePlayerManager.getCurrentKey() == key) {
            VoicePlayerManager.addOnUpdateListener(key, onUpdateListener)
            VoicePlayerManager.addOnProgressUpdateListener(key, onProgressUpdateListener)
        }
        val seekTo = VoicePlayerManager.getSeekTo(key)
        Logger.i("VoiceMessageView::drawMessage key : $key, seekTo : $seekTo, duration : $duration")
        drawPlayerStatus(VoicePlayerManager.getStatus(key) ?: VoicePlayer.Status.STOPPED)
        ViewUtils.drawTimeline(
            binding.timelineView,
            if (seekTo == 0) duration else duration - seekTo
        )
        val progress = if (duration != 0) seekTo * 1000 / duration else 0
        binding.voiceProgressView.drawProgress(progress)
    }

    private fun drawPlayerStatus(status: VoicePlayer.Status) {
        Logger.i("_________VoiceMessageView::drawPlayerStatus, status : $status")
        when (status) {
            VoicePlayer.Status.STOPPED -> {
                binding.ibtnPlay.visibility = VISIBLE
                binding.loading.visibility = GONE
                binding.ibtnPause.visibility = GONE
            }

            VoicePlayer.Status.PREPARING -> {
                binding.ibtnPlay.visibility = GONE
                binding.loading.visibility = VISIBLE
                binding.ibtnPause.visibility = GONE
            }

            VoicePlayer.Status.PLAYING -> {
                binding.ibtnPlay.visibility = GONE
                binding.loading.visibility = GONE
                binding.ibtnPause.visibility = VISIBLE
            }

            VoicePlayer.Status.PAUSED -> {
                binding.ibtnPlay.visibility = VISIBLE
                binding.loading.visibility = GONE
                binding.ibtnPause.visibility = GONE
            }
        }
    }

    fun callOnPlayerButtonClick() {
        if (binding.ibtnPlay.visibility == VISIBLE) {
            binding.ibtnPlay.callOnClick()
        } else if (binding.ibtnPause.visibility == VISIBLE) {
            binding.ibtnPause.callOnClick()
        }
    }
}
