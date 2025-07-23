package com.johnlai.twiliovideo.domain.video

import android.content.Context
import com.twilio.video.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

/**
 * Android implementation of TwilioVideoManager using real Twilio Video SDK
 */
actual class TwilioVideoManagerImpl actual constructor() : TwilioVideoManager {
    
    private var context: Context? = null
    private val tokenService = TokenService()
    
    // Constructor with context for Android-specific functionality
    constructor(context: Context) : this() {
        this.context = context
    }
    
    // Twilio SDK objects
    private val _room = MutableStateFlow<Room?>(null)
    private val _localVideoTrack = MutableStateFlow<LocalVideoTrack?>(null)
    private val _localAudioTrack = MutableStateFlow<LocalAudioTrack?>(null)
    private val _cameraCapture = MutableStateFlow<CameraCapturer?>(null)
    private val _participants = MutableStateFlow<List<VideoParticipant>>(emptyList())
    private val _networkQuality = MutableStateFlow(
        NetworkQuality(
            level = 0,
            local = NetworkQualityStats(
                audio = NetworkQualityLevel.UNKNOWN,
                video = NetworkQualityLevel.UNKNOWN
            ),
            remote = emptyMap()
        )
    )
    
    // Flow properties
    override val connectionState: Flow<VideoConnectionState> = 
        _room.map { room ->
            when {
                room == null -> VideoConnectionState.Disconnected
                room.state == Room.State.CONNECTED -> {
                    VideoConnectionState.Connected(room.toVideoRoom())
                }
                room.state == Room.State.CONNECTING -> VideoConnectionState.Connecting
                room.state == Room.State.RECONNECTING -> VideoConnectionState.Reconnecting
                else -> VideoConnectionState.Failed(VideoError.ConnectionFailed)
            }
        }
    
    override val participants: Flow<List<VideoParticipant>> = _participants.asStateFlow()
    override val localVideoTrack: Flow<VideoTrack?> = _localVideoTrack.map { it?.toVideoTrack() }
    override val networkQuality: Flow<NetworkQuality> = _networkQuality.asStateFlow()
    
    // Room listener for handling Twilio SDK events
    private val roomListener = object : Room.Listener {
        override fun onConnected(room: Room) {
            _room.value = room
            updateParticipants(room)
        }
        
        override fun onConnectFailure(room: Room, twilioException: TwilioException) {
            // Connection failed
        }
        
        override fun onParticipantConnected(room: Room, participant: RemoteParticipant) {
            updateParticipants(room)
            // Set up participant listener
            participant.setListener(participantListener)
        }
        
        override fun onParticipantDisconnected(room: Room, participant: RemoteParticipant) {
            updateParticipants(room)
        }
        
        override fun onReconnecting(room: Room, twilioException: TwilioException) {
            // Room is reconnecting
        }
        
        override fun onReconnected(room: Room) {
            updateParticipants(room)
        }
        
        override fun onRecordingStarted(room: Room) {
            // Recording started
        }
        
        override fun onRecordingStopped(room: Room) {
            // Recording stopped
        }
        
        override fun onDisconnected(room: Room, twilioException: TwilioException?) {
            _room.value = null
            _participants.value = emptyList()
        }
    }
    
    // Participant listener for handling participant events
    private val participantListener = object : RemoteParticipant.Listener {
        override fun onVideoTrackSubscribed(
            participant: RemoteParticipant,
            publication: RemoteVideoTrackPublication,
            track: RemoteVideoTrack
        ) {
            _room.value?.let { updateParticipants(it) }
        }
        
        override fun onVideoTrackUnsubscribed(
            participant: RemoteParticipant,
            publication: RemoteVideoTrackPublication,
            track: RemoteVideoTrack
        ) {
            _room.value?.let { updateParticipants(it) }
        }
        
        override fun onVideoTrackSubscriptionFailed(
            participant: RemoteParticipant,
            publication: RemoteVideoTrackPublication,
            twilioException: TwilioException
        ) {
            // Handle subscription failure
        }
        
        override fun onAudioTrackSubscribed(
            participant: RemoteParticipant,
            publication: RemoteAudioTrackPublication,
            track: RemoteAudioTrack
        ) {
            _room.value?.let { updateParticipants(it) }
        }
        
        override fun onAudioTrackUnsubscribed(
            participant: RemoteParticipant,
            publication: RemoteAudioTrackPublication,
            track: RemoteAudioTrack
        ) {
            _room.value?.let { updateParticipants(it) }
        }
        
        override fun onAudioTrackSubscriptionFailed(
            participant: RemoteParticipant,
            publication: RemoteAudioTrackPublication,
            twilioException: TwilioException
        ) {
            // Handle subscription failure
        }
        
        override fun onDataTrackSubscribed(
            participant: RemoteParticipant,
            publication: RemoteDataTrackPublication,
            track: RemoteDataTrack
        ) {
            // Handle data track
        }
        
        override fun onDataTrackUnsubscribed(
            participant: RemoteParticipant,
            publication: RemoteDataTrackPublication,
            track: RemoteDataTrack
        ) {
            // Handle data track
        }
        
        override fun onDataTrackSubscriptionFailed(
            participant: RemoteParticipant,
            publication: RemoteDataTrackPublication,
            twilioException: TwilioException
        ) {
            // Handle subscription failure
        }
        
        override fun onVideoTrackPublished(participant: RemoteParticipant, publication: RemoteVideoTrackPublication) {}
        override fun onVideoTrackUnpublished(participant: RemoteParticipant, publication: RemoteVideoTrackPublication) {}
        override fun onVideoTrackEnabled(participant: RemoteParticipant, publication: RemoteVideoTrackPublication) {}
        override fun onVideoTrackDisabled(participant: RemoteParticipant, publication: RemoteVideoTrackPublication) {}
        override fun onAudioTrackPublished(participant: RemoteParticipant, publication: RemoteAudioTrackPublication) {}
        override fun onAudioTrackUnpublished(participant: RemoteParticipant, publication: RemoteAudioTrackPublication) {}
        override fun onAudioTrackEnabled(participant: RemoteParticipant, publication: RemoteAudioTrackPublication) {}
        override fun onAudioTrackDisabled(participant: RemoteParticipant, publication: RemoteAudioTrackPublication) {}
        override fun onDataTrackPublished(participant: RemoteParticipant, publication: RemoteDataTrackPublication) {}
        override fun onDataTrackUnpublished(participant: RemoteParticipant, publication: RemoteDataTrackPublication) {}
    }
    
    override suspend fun connect(accessToken: String, roomName: String): VideoResult<VideoRoom> {
        return withContext(Dispatchers.IO) {
            try {
                val appContext = context ?: throw IllegalStateException("Context not provided")
                
                // Get token from your API service
                val tokenResult = tokenService.getToken(
                    userIdentity = tokenService.getStandardUserIdentity(),
                    roomName = roomName
                )
                
                val token = when (tokenResult) {
                    is VideoResult.Success -> tokenResult.data
                    is VideoResult.Error -> return@withContext VideoResult.Error(tokenResult.error)
                }
                
                // Setup local media tracks
                setupLocalMediaTracks(appContext)
                
                // Connect to room
                val connectOptionsBuilder = ConnectOptions.Builder(token)
                    .roomName(roomName)
                
                // Add local tracks if available
                _localVideoTrack.value?.let { 
                    connectOptionsBuilder.videoTracks(listOf(it))
                }
                _localAudioTrack.value?.let { 
                    connectOptionsBuilder.audioTracks(listOf(it))
                }
                
                val connectOptions = connectOptionsBuilder.build()
                val room = Video.connect(appContext, connectOptions, roomListener)
                _room.value = room
                
                VideoResult.Success(room.toVideoRoom())
                
            } catch (e: Exception) {
                VideoResult.Error(VideoError.ConnectionFailed)
            }
        }
    }
    
    override suspend fun disconnect(): VideoResult<Unit> {
        return try {
            _room.value?.disconnect()
            _room.value = null
        _participants.value = emptyList()
            
            // Clean up local tracks
            _localVideoTrack.value?.release()
            _localAudioTrack.value?.release()
            _cameraCapture.value?.stopCapture()
            
        _localVideoTrack.value = null
            _localAudioTrack.value = null
            _cameraCapture.value = null
            
            VideoResult.Success(Unit)
        } catch (e: Exception) {
            VideoResult.Error(VideoError.UnknownError(e.message ?: "Disconnect failed"))
        }
    }
    
    override suspend fun enableCamera(enable: Boolean): VideoResult<Unit> {
        return try {
            if (enable) {
                if (_localVideoTrack.value == null) {
                    context?.let { setupLocalVideoTrack(it) }
                }
                _localVideoTrack.value?.enable(true)
            } else {
                _localVideoTrack.value?.enable(false)
            }
            VideoResult.Success(Unit)
        } catch (e: Exception) {
            VideoResult.Error(VideoError.PermissionDenied)
        }
    }
    
    override suspend fun enableMicrophone(enable: Boolean): VideoResult<Unit> {
        return try {
            if (enable) {
                if (_localAudioTrack.value == null) {
                    context?.let { setupLocalAudioTrack(it) }
                }
                _localAudioTrack.value?.enable(true)
            } else {
                _localAudioTrack.value?.enable(false)
            }
            VideoResult.Success(Unit)
        } catch (e: Exception) {
            VideoResult.Error(VideoError.PermissionDenied)
        }
    }
    
    override suspend fun switchCamera(): VideoResult<Unit> {
        return try {
            // Switch to back camera (camera ID "1")
            (_cameraCapture.value as? CameraCapturer)?.switchCamera("1")
            VideoResult.Success(Unit)
        } catch (e: Exception) {
            VideoResult.Error(VideoError.UnknownError("Camera switch failed"))
        }
    }
    
    override suspend fun startScreenShare(): VideoResult<Unit> {
        // Screen sharing implementation would go here
        return VideoResult.Error(VideoError.UnknownError("Screen sharing not implemented yet"))
    }
    
    override suspend fun stopScreenShare(): VideoResult<Unit> {
        // Screen sharing implementation would go here
        return VideoResult.Error(VideoError.UnknownError("Screen sharing not implemented yet"))
    }
    
    override suspend fun getAvailableCameras(): List<CameraInfo> {
        return try {
            val cameras = mutableListOf<CameraInfo>()
            
            // Add front camera - assume it's available for now
            cameras.add(
                CameraInfo(
                    id = "front",
                    name = "Front Camera",
                    isFrontFacing = true,
                    isBackFacing = false
                )
            )
            
            // Add back camera - assume it's available for now  
            cameras.add(
                CameraInfo(
                    id = "back", 
                    name = "Back Camera",
                    isFrontFacing = false,
                    isBackFacing = true
                )
            )
            
            cameras
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override suspend fun getCurrentCameraInfo(): CameraInfo? {
        return try {
            // Default to front camera
            CameraInfo(
                id = "front",
                name = "Front Camera", 
                isFrontFacing = true,
                isBackFacing = false
            )
        } catch (e: Exception) {
            null
        }
    }
    
    override fun release() {
        runBlocking {
            disconnect()
        }
        tokenService.close()
    }
    
    // Private helper methods
    private fun setupLocalMediaTracks(context: Context) {
        setupLocalVideoTrack(context)
        setupLocalAudioTrack(context)
    }
    
    private fun setupLocalVideoTrack(context: Context) {
        try {
            if (_cameraCapture.value == null && _localVideoTrack.value == null) {
                // Create a basic camera capturer using string-based camera ID
                val cameraCapture = CameraCapturer(context, "0")
                _cameraCapture.value = cameraCapture
                
                // Create video track with camera capturer
                val videoTrack = LocalVideoTrack.create(context, true, cameraCapture as VideoCapturer)
                _localVideoTrack.value = videoTrack
            }
        } catch (e: Exception) {
            // Camera setup failed - create video track without camera for now
            // This is a fallback approach
        }
    }
    
    private fun setupLocalAudioTrack(context: Context) {
        try {
            if (_localAudioTrack.value == null) {
                val audioTrack = LocalAudioTrack.create(context, true)
                _localAudioTrack.value = audioTrack
            }
        } catch (e: Exception) {
            // Handle audio setup failure
        }
    }
    
    private fun updateParticipants(room: Room) {
        val participants = room.remoteParticipants.map { it.toVideoParticipant() }
        _participants.value = participants
    }
}

// Extension functions to convert Twilio SDK objects to our data models
private fun Room.toVideoRoom(): VideoRoom {
    return VideoRoom(
        name = this.name,
        sid = this.sid,
        participants = this.remoteParticipants.map { it.toVideoParticipant() }
    )
}

private fun RemoteParticipant.toVideoParticipant(): VideoParticipant {
    val videoTracks = this.remoteVideoTracks.map { it.toVideoTrack() }
    val audioTracks = this.remoteAudioTracks.map { it.toAudioTrack() }
    
    return VideoParticipant(
        identity = this.identity,
        sid = this.sid,
        isConnected = this.state == Participant.State.CONNECTED,
        videoTracks = videoTracks,
        audioTracks = audioTracks
    )
}

private fun RemoteVideoTrackPublication.toVideoTrack(): VideoTrack {
    return VideoTrack(
        sid = this.trackSid,
        name = this.trackName,
        isEnabled = this.isTrackEnabled,
        participantSid = "" // Would need participant reference
    )
}

private fun RemoteAudioTrackPublication.toAudioTrack(): AudioTrack {
    return AudioTrack(
        sid = this.trackSid,
        name = this.trackName,
        isEnabled = this.isTrackEnabled,
        participantSid = "" // Would need participant reference
    )
}

private fun LocalVideoTrack.toVideoTrack(): VideoTrack {
    return VideoTrack(
        sid = this.name,
        name = this.name,
        isEnabled = this.isEnabled,
        participantSid = "local"
    )
}

actual object TwilioVideoManagerFactory {
    actual fun create(): TwilioVideoManager {
        return TwilioVideoManagerImpl()
    }
    
    fun create(context: Context): TwilioVideoManager {
        return TwilioVideoManagerImpl(context)
    }
} 