package com.juggle.im.JLiveKitCall

import android.content.Context
import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import com.juggle.im.call.internal.media.CallMediaRoom
import com.juggle.im.call.internal.media.CallMediaRoomConfig
import com.juggle.im.call.internal.media.CallMediaUser
import com.juggle.im.call.internal.media.ICallCompleteCallback
import com.juggle.im.call.internal.media.ICallMediaEngine
import com.juggle.im.call.model.CallVideoDenoiseParams
import io.livekit.android.LiveKit
import io.livekit.android.renderer.SurfaceViewRenderer
import io.livekit.android.room.track.CameraPosition
import io.livekit.android.room.track.LocalVideoTrackOptions
import java.util.concurrent.ConcurrentHashMap
import com.juggle.im.JErrorCode
import com.juggle.im.call.internal.media.ICallMediaEngine.ICallMediaEngineListener
import io.livekit.android.events.RoomEvent
import io.livekit.android.events.collect
import io.livekit.android.room.Room
import io.livekit.android.room.participant.Participant
import io.livekit.android.room.track.LocalVideoTrack
import io.livekit.android.room.track.VideoTrack
import kotlinx.coroutines.*
import java.util.HashMap

class CallMediaLiveKitEngine(context: Context) : ICallMediaEngine {
    private val mRoom = LiveKit.create(context)
    private var mEnableCamera = false
    private var mPreviewTrack: VideoTrack? = null
    private var mPreviewView: SurfaceViewRenderer? = null
    private val mVideoTrackMap = ConcurrentHashMap<String, VideoTrack>()
    private val mFrontTrack = mRoom.localParticipant.createVideoTrack(
        options = LocalVideoTrackOptions(position = CameraPosition.FRONT)
    )
    private var mListener: ICallMediaEngineListener? = null
    private val scope = CoroutineScope(Job() + Dispatchers.IO)

    init {
        scope.launch {
            mRoom.events.collect { event ->
                when (event) {
                    is RoomEvent.Connected -> onRoomConnected(event)
                    is RoomEvent.Disconnected -> onRoomDisconnected(event)
                    is RoomEvent.TrackPublished -> onTrackPublished(event)
                    is RoomEvent.TrackSubscribed -> onTrackSubscribed(event)
                    is RoomEvent.ParticipantStateChanged -> onParticipantStateChanged(event)
                        is RoomEvent.ParticipantConnected -> onParticipantConnected(event)
                    is RoomEvent.ActiveSpeakersChanged -> onActiveSpeakerChanged(event)
                    else -> {}
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun joinRoom(
        room: CallMediaRoom?,
        user: CallMediaUser?,
        config: CallMediaRoomConfig?,
        callback: ICallCompleteCallback?
    ) {
        scope.launch {
            if (config != null) {
                try {
                    mRoom.connect(config.url, config.token)
                    callback?.onComplete(0, null)
                    var l: Array<String> = emptyArray()
                    mRoom.remoteParticipants.forEach { (t, u) ->
                        if (u.state == Participant.State.ACTIVE) {
                            val userId = t.value
                            l += userId
                        }
                    }
                    if (l.isNotEmpty()) {
                        mListener?.onUsersConnect(l.toMutableList())
                    }
                } catch (e: Exception) {
                    callback?.onComplete(JErrorCode.JOIN_LIVEKIT_FAIL, null)
                }
            }
        }
    }

    override fun leaveRoom(roomId: String?) {
        scope.launch {
            mFrontTrack.stop()
            mRoom.disconnect()
        }
    }

    override fun enableCamera(isEnable: Boolean) {
        scope.launch {
            mEnableCamera = isEnable
            if (mRoom.state == Room.State.CONNECTED) {
                mRoom.localParticipant.setCameraEnabled(isEnable)
            }
        }
    }

    override fun startPreview(view: View?) {
        scope.launch(Dispatchers.Main) {
            if (view is SurfaceViewRenderer) {
                view.release()
                mRoom.initVideoRenderer(view)
                if (mPreviewTrack != null) {
                    mFrontTrack.removeRenderer(view)
                    if (mPreviewView != null) {
                        mPreviewTrack?.removeRenderer(mPreviewView!!)
                    }
                    mPreviewView = view
                    mPreviewTrack?.addRenderer(view)
                } else {
                    mFrontTrack.startCapture()
                    mFrontTrack.addRenderer(view)
                    mPreviewView = view
                }
            }
        }
    }

    override fun stopPreview() {
        scope.launch {
            mFrontTrack.capturer.stopCapture()
        }
    }

    override fun setVideoView(roomId: String?, userId: String?, view: View?) {
        scope.launch(Dispatchers.Main) {
            if (view is SurfaceViewRenderer) {
                view.release()
                mRoom.initVideoRenderer(view)
                val videoTrack = mVideoTrackMap[userId] ?: return@launch
                videoTrack.addRenderer(view)
            }
        }
    }

    override fun muteMicrophone(isMute: Boolean) {
        scope.launch {
            mRoom.localParticipant.setMicrophoneEnabled(!isMute)
        }
    }

    override fun muteSpeaker(isMute: Boolean) {
        //todo 上层实现
    }

    override fun setSpeakerEnable(isEnable: Boolean) {
        //todo 上层实现
    }

    override fun useFrontCamera(isEnable: Boolean) {
        scope.launch {
            if (mRoom.localParticipant.videoTrackPublications.isEmpty()) {
                return@launch
            }
            val track = mRoom.localParticipant.videoTrackPublications.first().second as LocalVideoTrack
            track.switchCamera()
        }
    }

    override fun enableAEC(isEnable: Boolean) {
    }

    override fun setVideoDenoiseParams(params: CallVideoDenoiseParams?) {
    }

    override fun setListener(listener: ICallMediaEngineListener?) {
        mListener = listener
    }

    private suspend fun onRoomConnected(event: RoomEvent.Connected) {
        mRoom.localParticipant.setMicrophoneEnabled(true)
        if (mEnableCamera) {
            stopPreview()
            mRoom.localParticipant.setCameraEnabled(true)
        }
    }

    private suspend fun onRoomDisconnected(event: RoomEvent.Disconnected) {
        mVideoTrackMap.clear()
        mPreviewTrack = null
        mRoom.localParticipant.setCameraEnabled(false)
        mRoom.localParticipant.setMicrophoneEnabled(false)
        mFrontTrack.stop()
    }

    private fun onTrackPublished(event: RoomEvent.TrackPublished) {
        val publication = event.publication
        if (publication.track is VideoTrack) {
            val videoTrack = publication.track as VideoTrack
            mPreviewTrack = videoTrack
            val view = mListener?.viewForSelf() ?: return
            if (view is SurfaceViewRenderer) {
                scope.launch(Dispatchers.Main) {
                    view.release()
                    mRoom.initVideoRenderer(view)
                    mFrontTrack.removeRenderer(view)
                    videoTrack.addRenderer(view)
                    mPreviewView = view
                }
            }
        }
    }

    private fun onTrackSubscribed(event: RoomEvent.TrackSubscribed) {
        val publication = event.publication
        val participant = event.participant
        if (publication.track is VideoTrack) {
            val userId = participant.identity?.value ?: return
            val videoView = mListener?.viewForUserId(userId)
            val videoTrack = publication.track as VideoTrack
            mVideoTrackMap[userId] = videoTrack
            if (videoView is SurfaceViewRenderer) {
                scope.launch (Dispatchers.Main) {
                    mFrontTrack.removeRenderer(videoView)
                    videoView.release()
                    mRoom.initVideoRenderer(videoView)
                    videoTrack.addRenderer(videoView)
                }
            }
        }
    }

    private fun onParticipantStateChanged(event: RoomEvent.ParticipantStateChanged) {
        if (event.newState == Participant.State.ACTIVE) {
            val participant = event.participant
            val userId = participant.identity?.value ?: return
            val l = arrayOf(userId)
            mListener?.onUsersConnect(l.toMutableList())
        }
    }

    private fun onParticipantConnected(event: RoomEvent.ParticipantConnected) {
        val participant = event.participant
        val l = arrayOf(participant.identity.toString())
        mListener?.onUsersConnect(l.toMutableList())
    }

    private fun onActiveSpeakerChanged(event: RoomEvent.ActiveSpeakersChanged) {
        val map = HashMap<String, Float>()
        for (participant in event.speakers) {
            val userId = participant.identity?.value ?: break
            map[userId] = participant.audioLevel
        }
        mListener?.onSoundLevelUpdate(map)
    }
}