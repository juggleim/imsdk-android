package com.jet.im.kit.model

import android.media.MediaRecorder.AudioSource

/**
 * Describes a configuration of voice recorder.
 * @since 3.9.2
 */
data class VoiceRecorderConfig @JvmOverloads constructor(
    /**
     * Returns the audio source to be used for recording.
     * The default value is [AudioSource.VOICE_RECOGNITION].
     * @see [android.media.MediaRecorder.AudioSource]
     * @since 3.9.2
     */
    val audioSource: Int = AudioSource.VOICE_RECOGNITION,
    /**
     * Returns the number of audio channels to be used for recording.
     * The default value is 1.
     * the number of audio channels. Usually it is either 1 (mono) or 2 (stereo).
     * @since 3.9.2
     */
    val audioChannels: Int = 1,
    /**
     * Returns the sampling rate to be used for recording.
     * The default value is 11025.
     * @since 3.9.2
     */
    val samplingRate: Int = 11025,
    /**
     * Returns the bit rate to be used for recording.
     * The default value is 12000.
     * @since 3.9.2
     */
    val bitRate: Int = 12000
)
