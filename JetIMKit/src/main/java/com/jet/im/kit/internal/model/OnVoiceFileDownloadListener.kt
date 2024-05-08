package com.jet.im.kit.internal.model

import com.sendbird.android.exception.SendbirdException
import java.io.File

internal interface OnVoiceFileDownloadListener {
    fun onVoiceFileDownloaded(voiceFile: File?, e: SendbirdException?)
}
