package com.johnlai.twiliovideo.domain.video

import android.content.Context

import android.util.Log
import com.twilio.video.*
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.distinctUntilChanged
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
    
    // Private state flows
    private val _room = MutableStateFlow<Room?>(null)
    private val _participants = MutableStateFlow<List<VideoParticipant>>(emptyList())
    private val _connectionState = MutableStateFlow<VideoConnectionState>(VideoConnectionState.Disconnected)
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
    
    // Local media tracks
    private var _localVideoTrack = MutableStateFlow<LocalVideoTrack?>(null)
    private var _localAudioTrack = MutableStateFlow<LocalAudioTrack?>(null)
    private val _cameraCapture = MutableStateFlow<VideoCapturer?>(null)
    
    override val connectionState: Flow<VideoConnectionState> = _connectionState.asStateFlow()
    
    override val participants: Flow<List<VideoParticipant>> = _participants.asStateFlow()
    override val localVideoTrack: Flow<VideoTrack?> = _localVideoTrack.map { it?.toVideoTrack() }
    override val networkQuality: Flow<NetworkQuality> = _networkQuality.asStateFlow()
    
    // Expose raw LocalVideoTrack for UI rendering
    val rawLocalVideoTrack: Flow<LocalVideoTrack?> = _localVideoTrack.asStateFlow()
    
    // Room listener for handling Twilio SDK events
    private val roomListener = object : Room.Listener {
        override fun onConnected(room: Room) {
            Log.d("RoomListener", "onConnected - room: ${room.name}, participants: ${room.remoteParticipants.size}")
            Log.d("RoomListener", "onConnected - room.state: ${room.state}")
            Log.d("RoomListener", "Setting connection state to Connected")
            _connectionState.value = VideoConnectionState.Connected(room.toVideoRoom())
            Log.d("RoomListener", "Setting _room.value to connected room...")
            _room.value = room
            Log.d("RoomListener", "RoomListener: _room.value set successfully")
            updateParticipants(room)
        }
        
        override fun onConnectFailure(room: Room, twilioException: TwilioException) {
            Log.d("RoomListener", "onConnectFailure - ${twilioException.message}")
            twilioException.printStackTrace()
        }
        
        override fun onParticipantConnected(room: Room, participant: RemoteParticipant) {
            Log.d("RoomListener", "onParticipantConnected - ${participant.identity}")
            updateParticipants(room)
            // Set up participant listener
            participant.setListener(participantListener)
        }
        
        override fun onParticipantDisconnected(room: Room, participant: RemoteParticipant) {
            Log.d("RoomListener", "onParticipantDisconnected - ${participant.identity}")
            updateParticipants(room)
        }
        
        override fun onReconnecting(room: Room, twilioException: TwilioException) {
            Log.d("RoomListener", "onReconnecting - ${twilioException.message}")
            _connectionState.value = VideoConnectionState.Reconnecting
        }
        
        override fun onReconnected(room: Room) {
            Log.d("RoomListener", "onReconnected - room: ${room.name}")
            _connectionState.value = VideoConnectionState.Connected(room.toVideoRoom())
            updateParticipants(room)
        }
        
        override fun onRecordingStarted(room: Room) {
            Log.d("RoomListener", "onRecordingStarted")
        }
        
        override fun onRecordingStopped(room: Room) {
            Log.d("RoomListener", "onRecordingStopped")
        }
        
        override fun onDisconnected(room: Room, twilioException: TwilioException?) {
            Log.d("RoomListener", "onDisconnected - ${twilioException?.message ?: "No error"}")
            _connectionState.value = VideoConnectionState.Disconnected
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
    
    override suspend fun connect(userIdentity: String, roomName: String): VideoResult<VideoRoom> {
        return withContext(Dispatchers.IO) {
            try {
                val appContext = context ?: throw IllegalStateException("Context not provided")
                Log.d("VideoManager", "Starting connection to room: $roomName as $userIdentity")
                // Get token from your API service
                val tokenResult = tokenService.getToken(
                    userIdentity = userIdentity,
                    roomName = roomName
                )
                val token = when (tokenResult) {
                    is VideoResult.Success -> {
                        Log.d("VideoManager", "Token received successfully")
                        tokenResult.data
                    }
                    is VideoResult.Error -> {
                        Log.d("VideoManager", "Token error: ${tokenResult.error}")
                        return@withContext VideoResult.Error(tokenResult.error)
                    }
                }
                Log.d("VideoManager", "Setting up local media tracks...")
                // Setup local media tracks
                setupLocalMediaTracks(appContext)
                Log.d("VideoManager", "Creating connect options...")
                // Connect to room
                val connectOptionsBuilder = ConnectOptions.Builder(token)
                    .roomName(roomName)
                // Add local tracks if available
                _localVideoTrack.value?.let { 
                    Log.d("VideoManager", "Adding local video track")
                    connectOptionsBuilder.videoTracks(listOf(it))
                }
                _localAudioTrack.value?.let { 
                    Log.d("VideoManager", "Adding local audio track")
                    connectOptionsBuilder.audioTracks(listOf(it))
                }
                val connectOptions = connectOptionsBuilder.build()
                Log.d("VideoManager", "Setting connection state to Connecting")
                _connectionState.value = VideoConnectionState.Connecting
                Log.d("VideoManager", "Calling Video.connect...")
                val room = Video.connect(appContext, connectOptions, roomListener)
                _room.value = room
                Log.d("VideoManager", "Video.connect returned, room state: ${room.state}")
                VideoResult.Success(room.toVideoRoom())
            } catch (e: Exception) {
                Log.d("VideoManager", "Connection error: ${e.message}")
                e.printStackTrace()
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
        return withContext(Dispatchers.IO) {
            try {
                Log.d("VideoManager", "enableCamera called with enable=$enable")
                if (enable) {
                    if (_localVideoTrack.value == null) {
                        Log.d("VideoManager", "No local video track, setting up...")
                        context?.let { ctx ->
                            setupLocalVideoTrack(ctx)
                            // Give it a moment to initialize
                            kotlinx.coroutines.delay(100)
                        }
                    }
                    Log.d("VideoManager", "Enabling video track...")
                    _localVideoTrack.value?.enable(true)
                    Log.d("VideoManager", "Video track after enable: ${_localVideoTrack.value}")
                    Log.d("VideoManager", "Video track enabled state: ${_localVideoTrack.value?.isEnabled}")
                    
                    // Publish video track to room if connected
                    _room.value?.let { room ->
                        _localVideoTrack.value?.let { videoTrack ->
                            Log.d("VideoManager", "Publishing video track to room...")
                            room.localParticipant?.publishTrack(videoTrack)
                        }
                    }
                } else {
                    Log.d("VideoManager", "Disabling video track...")
                    _localVideoTrack.value?.enable(false)
                    
                    // Unpublish video track from room if connected
                    _room.value?.let { room ->
                        _localVideoTrack.value?.let { videoTrack ->
                            Log.d("VideoManager", "Unpublishing video track from room...")
                            room.localParticipant?.unpublishTrack(videoTrack)
                        }
                    }
                }
                VideoResult.Success(Unit)
            } catch (e: Exception) {
                Log.d("VideoManager", "enableCamera error: ${e.message}")
                e.printStackTrace()
                VideoResult.Error(VideoError.PermissionDenied)
            }
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
    
    private var isUsingFrontCamera = true

    override suspend fun switchCamera(): VideoResult<Unit> {
        return try {
            val capturer = _cameraCapture.value
            if (capturer is com.twilio.video.Camera2Capturer) {
                val cameraManager = context?.getSystemService(Context.CAMERA_SERVICE) as? android.hardware.camera2.CameraManager
                if (cameraManager == null) return VideoResult.Error(VideoError.UnknownError("CameraManager unavailable"))
                val cameraIds = cameraManager.cameraIdList
                var newCameraId: String? = null
                for (cameraId in cameraIds) {
                    try {
                        val characteristics = cameraManager.getCameraCharacteristics(cameraId)
                        val facing = characteristics.get(android.hardware.camera2.CameraCharacteristics.LENS_FACING)
                        if (isUsingFrontCamera && facing == android.hardware.camera2.CameraCharacteristics.LENS_FACING_BACK) {
                            newCameraId = cameraId
                            break
                        } else if (!isUsingFrontCamera && facing == android.hardware.camera2.CameraCharacteristics.LENS_FACING_FRONT) {
                            newCameraId = cameraId
                            break
                        }
                    } catch (_: Exception) {}
                }
                if (newCameraId != null) {
                    capturer.switchCamera(newCameraId)
                    isUsingFrontCamera = !isUsingFrontCamera
                }
            } else if (capturer is com.twilio.video.CameraCapturer) {
                val newSource = if (isUsingFrontCamera) "back_camera" else "front_camera"
                capturer.switchCamera(newSource)
                isUsingFrontCamera = !isUsingFrontCamera
            }
            VideoResult.Success(Unit)
        } catch (e: Exception) {
            VideoResult.Error(VideoError.UnknownError("Camera switch failed: ${e.message}"))
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
            Log.d("VideoManager", "setupLocalVideoTrack called")
            if (_localVideoTrack.value == null) {
                Log.d("VideoManager", "Creating video track with Twilio Camera2Capturer, fallback to CameraCapturer if needed...")
                try {
                    // Get available camera IDs using Android CameraManager
                    val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as android.hardware.camera2.CameraManager
                    val cameraIds = cameraManager.cameraIdList
                    Log.d("VideoManager", "CameraManager found camera IDs: ${cameraIds.joinToString(", ")}")
                    if (cameraIds.isEmpty()) throw Exception("No cameras found")

                    // Try to find front camera first
                    var selectedCameraId: String? = null
                    for (cameraId in cameraIds) {
                        try {
                            val characteristics = cameraManager.getCameraCharacteristics(cameraId)
                            val facing = characteristics.get(android.hardware.camera2.CameraCharacteristics.LENS_FACING)
                            if (facing == android.hardware.camera2.CameraCharacteristics.LENS_FACING_FRONT) {
                                selectedCameraId = cameraId
                                Log.d("VideoManager", "Selected front camera: '$cameraId'")
                                break
                            }
                        } catch (e: Exception) {
                            Log.d("VideoManager", "Error checking camera $cameraId: ${e.message}")
                        }
                    }
                    if (selectedCameraId == null) {
                        selectedCameraId = cameraIds.first()
                        Log.d("VideoManager", "No front camera found, using first available: '$selectedCameraId'")
                    }

                    // Try Twilio Camera2Capturer first
                    try {
                        Log.d("VideoManager", "Trying Twilio Camera2Capturer with camera ID: '$selectedCameraId'")
                        val camera2Capturer = com.twilio.video.Camera2Capturer(context, selectedCameraId, object : com.twilio.video.Camera2Capturer.Listener {
                            override fun onFirstFrameAvailable() { Log.d("VideoManager", "Camera2Capturer: First frame available") }
                            override fun onCameraSwitched(newCameraId: String) { Log.d("VideoManager", "Camera2Capturer: Camera switched to $newCameraId") }
                            override fun onError(e: com.twilio.video.Camera2Capturer.Exception) { Log.d("VideoManager", "Camera2Capturer error: ${e.message}") }
                        })
                        _cameraCapture.value = camera2Capturer
                        val videoTrack = com.twilio.video.LocalVideoTrack.create(context, true, camera2Capturer)
                        if (videoTrack != null) {
                            _localVideoTrack.value = videoTrack
                            Log.d("VideoManager", "🎉 SUCCESS! LocalVideoTrack created with Twilio Camera2Capturer!")
                            Log.d("VideoManager", "Video track enabled: ${videoTrack.isEnabled}")
                            Log.d("VideoManager", "Video track name: ${videoTrack.name}")
                            camera2Capturer.startCapture(640, 480, 30)
                            Log.d("VideoManager", "Camera2Capturer capture started successfully")
                            return
                        } else {
                            Log.d("VideoManager", "LocalVideoTrack.create returned null for Camera2Capturer")
                        }
                    } catch (e: Exception) {
                        Log.d("VideoManager", "Camera2Capturer failed: ${e.message}")
                    }

                    // Fallback: Try Twilio CameraCapturer (Camera1 API)
                    try {
                        Log.d("VideoManager", "Falling back to Twilio CameraCapturer (Camera1 API)...")
                        val cameraCapturer = com.twilio.video.CameraCapturer(context, "front_camera")
                        _cameraCapture.value = cameraCapturer
                        val videoTrack = com.twilio.video.LocalVideoTrack.create(context, true, cameraCapturer)
                        if (videoTrack != null) {
                            _localVideoTrack.value = videoTrack
                            Log.d("VideoManager", "🎉 SUCCESS! LocalVideoTrack created with Twilio CameraCapturer (Camera1 API)!")
                            Log.d("VideoManager", "Video track enabled: ${videoTrack.isEnabled}")
                            Log.d("VideoManager", "Video track name: ${videoTrack.name}")
                            cameraCapturer.startCapture(640, 480, 30)
                            Log.d("VideoManager", "CameraCapturer (Camera1) capture started successfully")
                            return
                        } else {
                            Log.d("VideoManager", "LocalVideoTrack.create returned null for CameraCapturer (Camera1 API)")
                        }
                    } catch (e: Exception) {
                        Log.d("VideoManager", "CameraCapturer (Camera1 API) failed: ${e.message}")
                    }

                    throw Exception("All Twilio camera capturer attempts failed!")
                } catch (e: Exception) {
                    Log.d("VideoManager", "Camera setup failed: ${e.message}")
                    e.printStackTrace()
                    // Clean up on failure
                    _cameraCapture.value = null
                    _localVideoTrack.value = null
                }
            } else {
                Log.d("VideoManager", "Video track already exists: ${_localVideoTrack.value}")
            }
        } catch (e: Exception) {
            Log.d("VideoManager", "Camera setup failed: ${e.message}")
            e.printStackTrace()
            // Clean up on failure
            _cameraCapture.value = null
            _localVideoTrack.value = null
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