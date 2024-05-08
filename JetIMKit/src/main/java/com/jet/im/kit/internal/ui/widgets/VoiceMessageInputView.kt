package com.jet.im.kit.internal.ui.widgets

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import com.jet.im.kit.R
import com.jet.im.kit.SendbirdUIKit
import com.jet.im.kit.consts.StringSet
import com.jet.im.kit.databinding.SbViewVoiceMessageInputBinding
import com.jet.im.kit.interfaces.OnItemClickListener
import com.jet.im.kit.internal.extensions.setAppearance
import com.jet.im.kit.internal.model.VoicePlayer
import com.jet.im.kit.internal.model.VoicePlayerManager
import com.jet.im.kit.internal.model.VoiceRecorder
import com.jet.im.kit.log.Logger
import com.jet.im.kit.model.VoiceMessageInfo
import com.jet.im.kit.utils.ClearableScheduledExecutorService
import com.jet.im.kit.utils.DrawableUtils
import com.jet.im.kit.utils.ViewUtils
import java.io.File
import java.util.concurrent.TimeUnit

internal class VoiceMessageInputView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.sb_widget_voice_message_input_view
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: SbViewVoiceMessageInputBinding

    private val recorder: VoiceRecorder
    private val recordingIconExecutor by lazy { ClearableScheduledExecutorService() }
    private val onRecorderUpdateListener: VoiceRecorder.OnUpdateListener
    private val onRecorderProgressUpdateListener: VoiceRecorder.OnProgressUpdateListener
    private val onUpdateListener: VoicePlayer.OnUpdateListener
    private val onProgressUpdateListener: VoicePlayer.OnProgressUpdateListener

    var onCancelButtonClickListener: OnClickListener? = null
    var onSendButtonClickListener: OnItemClickListener<VoiceMessageInfo>? = null

    init {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.VoiceMessageInputView, defStyleAttr, 0)
        try {
            binding = SbViewVoiceMessageInputBinding.inflate(LayoutInflater.from(getContext()), this, true)

            val background = a.getResourceId(
                R.styleable.VoiceMessageInputView_sb_voice_message_input_background,
                android.R.color.transparent
            )
            val backgroundColor = a.getResourceId(
                R.styleable.VoiceMessageInputView_sb_voice_message_input_background_color,
                android.R.color.transparent
            )
            val progressColor = a.getResourceId(
                R.styleable.VoiceMessageInputView_sb_voice_message_input_progress_color,
                android.R.color.transparent
            )
            val progressBackgroundColor = a.getResourceId(
                R.styleable.VoiceMessageInputView_sb_voice_message_input_progress_track_color,
                android.R.color.transparent
            )
            val timelineTextAppearance = a.getResourceId(
                R.styleable.VoiceMessageInputView_sb_voice_message_input_timeline_text_appearance,
                R.style.SendbirdCaption1OnDark01
            )
            val timelineTextColor = a.getResourceId(
                R.styleable.VoiceMessageInputView_sb_voice_message_input_timeline_text_color,
                android.R.color.transparent
            )
            val recordButtonIcon = a.getResourceId(
                R.styleable.VoiceMessageInputView_sb_voice_message_input_record_button_icon,
                R.drawable.icon_recording
            )
            val recordButtonTint = a.getResourceId(
                R.styleable.VoiceMessageInputView_sb_voice_message_input_record_button_tint,
                R.color.error_300
            )
            val recordButtonBackground = a.getResourceId(
                R.styleable.VoiceMessageInputView_sb_voice_message_input_record_button_background,
                android.R.color.transparent
            )
            val recordButtonBackgroundTint = a.getResourceId(
                R.styleable.VoiceMessageInputView_sb_voice_message_input_record_button_background_tint,
                android.R.color.transparent
            )
            val playButtonIcon = a.getResourceId(
                R.styleable.VoiceMessageInputView_sb_voice_message_input_play_button_icon,
                R.drawable.icon_play
            )
            val playButtonTint = a.getResourceId(
                R.styleable.VoiceMessageInputView_sb_voice_message_input_play_button_tint,
                R.color.onlight_01
            )
            val playButtonBackground = a.getResourceId(
                R.styleable.VoiceMessageInputView_sb_voice_message_input_play_button_background,
                android.R.color.transparent
            )
            val playButtonBackgroundTint = a.getResourceId(
                R.styleable.VoiceMessageInputView_sb_voice_message_input_play_button_background_tint,
                android.R.color.transparent
            )
            val pauseButtonIcon = a.getResourceId(
                R.styleable.VoiceMessageInputView_sb_voice_message_input_pause_button_icon,
                R.drawable.icon_pause
            )
            val pauseButtonTint = a.getResourceId(
                R.styleable.VoiceMessageInputView_sb_voice_message_input_pause_button_tint,
                R.color.onlight_01
            )
            val pauseButtonBackground = a.getResourceId(
                R.styleable.VoiceMessageInputView_sb_voice_message_input_pause_button_background,
                android.R.color.transparent
            )
            val pauseButtonBackgroundTint = a.getResourceId(
                R.styleable.VoiceMessageInputView_sb_voice_message_input_pause_button_background_tint,
                android.R.color.transparent
            )
            val stopButtonIcon = a.getResourceId(
                R.styleable.VoiceMessageInputView_sb_voice_message_input_stop_button_icon,
                R.drawable.icon_stop
            )
            val stopButtonTint = a.getResourceId(
                R.styleable.VoiceMessageInputView_sb_voice_message_input_stop_button_tint,
                R.color.onlight_01
            )
            val stopButtonBackground = a.getResourceId(
                R.styleable.VoiceMessageInputView_sb_voice_message_input_stop_button_background,
                android.R.color.transparent
            )
            val stopButtonBackgroundTint = a.getResourceId(
                R.styleable.VoiceMessageInputView_sb_voice_message_input_stop_button_background_tint,
                android.R.color.transparent
            )
            val sendButtonIcon = a.getResourceId(
                R.styleable.VoiceMessageInputView_sb_voice_message_input_send_button_icon,
                R.drawable.icon_send
            )
            val sendButtonTint = a.getResourceId(
                R.styleable.VoiceMessageInputView_sb_voice_message_input_send_button_tint,
                R.color.primary_300
            )
            val sendButtonBackground = a.getResourceId(
                R.styleable.VoiceMessageInputView_sb_voice_message_input_send_button_background,
                android.R.color.transparent
            )
            val sendButtonBackgroundTint = a.getResourceId(
                R.styleable.VoiceMessageInputView_sb_voice_message_input_send_button_background_tint,
                android.R.color.transparent
            )
            val cancelButtonTextAppearance = a.getResourceId(
                R.styleable.VoiceMessageInputView_sb_voice_message_input_cancel_button_text_appearance,
                R.style.SendbirdButtonPrimary300
            )
            val cancelButtonTextColor = a.getResourceId(
                R.styleable.VoiceMessageInputView_sb_voice_message_input_cancel_button_text_color,
                R.color.sb_button_uncontained_text_color_cancel_light
            )
            val cancelButtonBackground = a.getResourceId(
                R.styleable.VoiceMessageInputView_sb_voice_message_input_cancel_button_background,
                android.R.color.transparent
            )

            binding.root.setBackgroundResource(background)
            binding.root.setBackgroundColor(ContextCompat.getColor(context, backgroundColor))
            binding.progress.isEnabled = false
            binding.progress.progressColor = AppCompatResources.getColorStateList(context, progressColor)
            binding.progress.trackColor = AppCompatResources.getColorStateList(context, progressBackgroundColor)
            binding.tvTimeline.setAppearance(context, timelineTextAppearance)
            binding.tvTimeline.setTextColor(AppCompatResources.getColorStateList(context, timelineTextColor))
            binding.ibtnSend.background =
                DrawableUtils.setTintList(
                    context,
                    sendButtonBackground,
                    sendButtonBackgroundTint
                )
            binding.ibtnSend.setImageDrawable(DrawableUtils.setTintList(context, sendButtonIcon, sendButtonTint))
            binding.btnCancel.setBackgroundResource(cancelButtonBackground)
            binding.btnCancel.setAppearance(context, cancelButtonTextAppearance)
            binding.btnCancel.setTextColor(AppCompatResources.getColorStateList(context, cancelButtonTextColor))
            binding.recordingIcon.imageTintList = AppCompatResources.getColorStateList(
                context,
                if (SendbirdUIKit.isDarkMode()) R.color.error_200 else R.color.error_300
            )
            binding.ibtnRecord.background =
                DrawableUtils.setTintList(
                    context,
                    recordButtonBackground,
                    recordButtonBackgroundTint
                )
            binding.ibtnRecord.setImageDrawable(
                DrawableUtils.setTintList(
                    context,
                    recordButtonIcon,
                    recordButtonTint
                )
            )
            binding.ibtnPlay.background =
                DrawableUtils.setTintList(
                    context,
                    playButtonBackground,
                    playButtonBackgroundTint
                )
            binding.ibtnPlay.setImageDrawable(
                DrawableUtils.setTintList(
                    context,
                    playButtonIcon,
                    playButtonTint
                )
            )
            binding.ibtnStop.background =
                DrawableUtils.setTintList(
                    context,
                    stopButtonBackground,
                    stopButtonBackgroundTint
                )
            binding.ibtnStop.setImageDrawable(
                DrawableUtils.setTintList(
                    context,
                    stopButtonIcon,
                    stopButtonTint
                )
            )
            binding.ibtnPause.background =
                DrawableUtils.setTintList(
                    context,
                    pauseButtonBackground,
                    pauseButtonBackgroundTint
                )
            binding.ibtnPause.setImageDrawable(
                DrawableUtils.setTintList(
                    context,
                    pauseButtonIcon,
                    pauseButtonTint
                )
            )

            ViewUtils.drawTimeline(binding.tvTimeline, 0)
            drawIdle()
            onRecorderUpdateListener = object : VoiceRecorder.OnUpdateListener {
                override fun onUpdated(status: VoiceRecorder.Status) {
                    drawRecordingStatus(status)
                }
            }
            onRecorderProgressUpdateListener = object : VoiceRecorder.OnProgressUpdateListener {
                override fun onProgressUpdated(
                    status: VoiceRecorder.Status,
                    milliseconds: Int,
                    maxDurationMillis: Int
                ) {
                    if (status == VoiceRecorder.Status.COMPLETED) return
                    if (milliseconds >= 1000) {
                        binding.ibtnSend.isEnabled = true
                    }
                    ViewUtils.drawTimeline(binding.tvTimeline, milliseconds)
                    ViewUtils.drawVoicePlayerProgress(binding.progress, milliseconds, maxDurationMillis)
                }
            }
            recorder = VoiceRecorder(context, SendbirdUIKit.getVoiceRecorderConfig(), onRecorderUpdateListener, onRecorderProgressUpdateListener)
            onUpdateListener = object : VoicePlayer.OnUpdateListener {
                override fun onUpdated(key: String, status: VoicePlayer.Status) {
                    Logger.i("VoiceMessageRecorderView >> onUpdateListener, status: $status")
                    drawPlayerStatus(status)
                }
            }
            onProgressUpdateListener = object : VoicePlayer.OnProgressUpdateListener {
                override fun onProgressUpdated(key: String, status: VoicePlayer.Status, milliseconds: Int, duration: Int) {
                    Logger.i("VoiceMessageRecorderView >> onProgressUpdateListener, milliseconds: $milliseconds")
                    if (duration == 0) return
                    ViewUtils.drawTimeline(
                        binding.tvTimeline,
                        if (status == VoicePlayer.Status.STOPPED) duration else duration - milliseconds
                    )
                    ViewUtils.drawVoicePlayerProgress(binding.progress, milliseconds, duration)
                }
            }
            initControlButton()
            binding.btnCancel.setOnClickListener { view ->
                Logger.i("Cancel button is clicked")
                shutdownRecordingIconExecutor()
                VoicePlayerManager.dispose(recorder.recordFilePath)
                recorder.cancel()
                onCancelButtonClickListener?.onClick(view)
            }
            binding.ibtnSend.setOnClickListener { view ->
                Logger.i("Send button is clicked")
                shutdownRecordingIconExecutor()
                VoicePlayerManager.dispose(recorder.recordFilePath)
                recorder.complete()
                val voiceMimeType = StringSet.audio + "/" + StringSet.m4a + ";" + StringSet.sbu_type + "=" + StringSet.voice
                val duration = recorder.seekTo
                Logger.i("VoiceMessageRecorderView: mimeType : $voiceMimeType, duration : $duration")
                onSendButtonClickListener?.onItemClick(
                    view,
                    0,
                    VoiceMessageInfo(
                        recorder.recordFilePath,
                        voiceMimeType,
                        duration
                    )
                )
            }
        } finally {
            a.recycle()
        }
    }

    private fun initControlButton() {
        binding.ibtnRecord.setOnClickListener {
            VoicePlayerManager.pause()
            recorder.record()
        }
        binding.ibtnPlay.setOnClickListener {
            VoicePlayerManager.play(
                context,
                recorder.recordFilePath,
                File(recorder.recordFilePath),
                recorder.seekTo,
                onUpdateListener,
                onProgressUpdateListener
            )
        }
        binding.ibtnPause.setOnClickListener {
            VoicePlayerManager.pause()
        }
        binding.ibtnStop.setOnClickListener {
            if (binding.ibtnSend.isEnabled) {
                recorder.complete()
            } else {
                recorder.cancel(true)
            }
        }
    }

    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        super.onWindowFocusChanged(hasWindowFocus)
        if (!hasWindowFocus) {
            pauseVoiceMessageInput()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        Logger.i("_________VoiceMessageView::onDetachedFromWindow()")
        VoicePlayerManager.removeOnUpdateListener(recorder.recordFilePath, onUpdateListener)
        VoicePlayerManager.removeOnProgressListener(recorder.recordFilePath, onProgressUpdateListener)
    }

    private fun pauseVoiceMessageInput() {
        if (binding.ibtnPause.visibility == VISIBLE) {
            binding.ibtnPause.callOnClick()
        } else if (binding.ibtnStop.visibility == VISIBLE) {
            if (binding.ibtnSend.isEnabled) {
                recorder.complete()
            } else {
                recorder.cancel(true)
            }
        }
    }

    private fun drawRecordingStatus(status: VoiceRecorder.Status) {
        when (status) {
            VoiceRecorder.Status.IDLE -> {
                drawIdle()
            }
            VoiceRecorder.Status.RECORDING -> {
                binding.tvTimeline.isEnabled = true
                binding.progress.isEnabled = true
                showRecordingIcon()
                binding.ibtnRecord.visibility = GONE
                binding.ibtnStop.visibility = VISIBLE
            }
            VoiceRecorder.Status.COMPLETED -> {
                binding.progress.drawProgress(0)
                File(recorder.recordFilePath)
                dismissRecordingIcon()
                shutdownRecordingIconExecutor()
                binding.ibtnStop.visibility = GONE
                binding.ibtnPlay.visibility = VISIBLE
            }
            VoiceRecorder.Status.PREPARING -> {}
        }
    }

    private fun drawPlayerStatus(status: VoicePlayer.Status) {
        when (status) {
            VoicePlayer.Status.PLAYING -> {
                binding.ibtnPlay.visibility = GONE
                binding.ibtnPause.visibility = VISIBLE
            }
            VoicePlayer.Status.PAUSED -> {
                binding.ibtnPlay.visibility = VISIBLE
                binding.ibtnPause.visibility = GONE
            }
            VoicePlayer.Status.PREPARING -> {}
            VoicePlayer.Status.STOPPED -> {
                binding.ibtnPlay.visibility = VISIBLE
                binding.ibtnPause.visibility = GONE
            }
        }
    }

    private fun showRecordingIcon() {
        recordingIconExecutor.scheduleAtFixedRate({
            Handler(Looper.getMainLooper()).post {
                if (recorder.status == VoiceRecorder.Status.RECORDING) {
                    binding.recordingIcon.visibility =
                        if (binding.recordingIcon.visibility == GONE) VISIBLE else GONE
                }
            }
        }, 0, 500, TimeUnit.MILLISECONDS)
    }

    private fun dismissRecordingIcon() {
        recordingIconExecutor.cancelAllJobs(true)
        binding.recordingIcon.visibility = GONE
    }

    private fun shutdownRecordingIconExecutor() {
        if (recordingIconExecutor.isShutdown) return
        recordingIconExecutor.shutdownNow()
    }

    private fun drawIdle() {
        ViewUtils.drawTimeline(binding.tvTimeline, 0)
        binding.progress.drawProgress(0)
        binding.ibtnSend.isEnabled = false
        binding.tvTimeline.isEnabled = false
        binding.progress.isEnabled = false
        dismissRecordingIcon()
        binding.ibtnRecord.visibility = VISIBLE
        binding.ibtnPlay.visibility = GONE
        binding.ibtnStop.visibility = GONE
        binding.ibtnPause.visibility = GONE
    }
}
