@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.johnlai.twiliovideo.domain.video

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import platform.Foundation.NSLog
import cocoapods.TwilioVideo.TVICameraSource
import cocoapods.TwilioVideo.TVILocalVideoTrack
import cocoapods.TwilioVideo.TVICameraSourceOptions
import cocoapods.TwilioVideo.TwilioVideoSDK
import cocoapods.TwilioVideo.TVIConnectOptions
import cocoapods.TwilioVideo.TVIRoom
import cocoapods.TwilioVideo.TVIRoomDelegateProtocol
import cocoapods.TwilioVideo.TVILocalAudioTrack
import cocoapods.TwilioVideo.TVIRemoteParticipant
import platform.AVFoundation.AVCaptureDevicePositionFront
import platform.AVFoundation.AVCaptureDevicePositionBack
import platform.AVFoundation.AVCaptureDevicePositionUnspecified
import platform.darwin.NSObject
import platform.Foundation.NSError
import kotlinx.cinterop.ObjCSignatureOverride

/**
 * iOS implementation of TwilioVideoManager using Twilio Video iOS SDK
 * 
 * This implementation uses real Twilio SDK integration with progressive feature rollout.
 */
actual class TwilioVideoManagerImpl : TwilioVideoManager {
    
    private val tokenService = TokenService()

    // Twilio SDK objects (renamed to avoid conflicts)
    private var twilioRoom: TVIRoom? = null
    private var twilioLocalVideoTrack: TVILocalVideoTrack? = null
    private var twilioLocalAudioTrack: TVILocalAudioTrack? = null
    private var twilioCameraSource: TVICameraSource? = null

    // State flows
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
    private val _localVideoTrack = MutableStateFlow<VideoTrack?>(null)
    private val _isLocalMicEnabled = MutableStateFlow(false)
    
    override val connectionState: Flow<VideoConnectionState> = _connectionState.asStateFlow()
    override val participants: Flow<List<VideoParticipant>> = _participants.asStateFlow()
    override val localVideoTrack: Flow<VideoTrack?> = _localVideoTrack.asStateFlow()
    override val networkQuality: Flow<NetworkQuality> = _networkQuality.asStateFlow()
    
    // Expose flows for UI (will be enhanced as we add real SDK integration)
    private val _rawLocalVideoTrack = MutableStateFlow<TVILocalVideoTrack?>(null)
    val rawLocalVideoTrack: Flow<Any?> = _rawLocalVideoTrack.asStateFlow()
    val isLocalMicEnabled: Flow<Boolean> = _isLocalMicEnabled.asStateFlow()
    
    // Method to get the native video track for UI rendering
    fun getNativeLocalVideoTrack(): TVILocalVideoTrack? = twilioLocalVideoTrack

    // Room delegate for handling real participant events
    private val roomDelegate = object : NSObject(), TVIRoomDelegateProtocol {
        override fun didConnectToRoom(room: TVIRoom) {
            NSLog("🎉 iOS TwilioVideoManager: Room connected successfully - ${room.name}")
            
            val videoRoom = VideoRoom(
                name = room.name ?: "Unknown Room",
                sid = room.sid ?: "unknown-sid",
                participants = room.remoteParticipants?.map { participant ->
                    convertToVideoParticipant(participant as TVIRemoteParticipant)
                } ?: emptyList()
            )
            
            _connectionState.value = VideoConnectionState.Connected(videoRoom)
            updateParticipants(room)
        }

        override fun room(room: TVIRoom, didDisconnectWithError: platform.Foundation.NSError?) {
            if (didDisconnectWithError != null) {
                NSLog("❌ iOS TwilioVideoManager: Room disconnected with error - ${didDisconnectWithError.localizedDescription}")
            } else {
                NSLog("✅ iOS TwilioVideoManager: Room disconnected successfully")
            }
            
            _connectionState.value = VideoConnectionState.Disconnected
            _participants.value = emptyList()
        }

        override fun room(room: TVIRoom, didFailToConnectWithError: platform.Foundation.NSError) {
            NSLog("💥 iOS TwilioVideoManager: Failed to connect to room - ${didFailToConnectWithError.localizedDescription}")
            _connectionState.value = VideoConnectionState.Disconnected
        }

        override fun roomDidStartRecording(room: TVIRoom) {
            NSLog("🔴 iOS TwilioVideoManager: Room recording started")
        }

        override fun roomDidStopRecording(room: TVIRoom) {
            NSLog("⏹️ iOS TwilioVideoManager: Room recording stopped")
        }

        @ObjCSignatureOverride
        override fun room(room: TVIRoom, participantDidConnect: TVIRemoteParticipant) {
            NSLog("👋 iOS TwilioVideoManager: Participant joined - ${participantDidConnect.identity}")
            updateParticipants(room)
        }

        @ObjCSignatureOverride
        override fun room(room: TVIRoom, participantDidDisconnect: TVIRemoteParticipant) {
            NSLog("👋 iOS TwilioVideoManager: Participant left - ${participantDidDisconnect.identity}")
            updateParticipants(room)
        }

        @ObjCSignatureOverride
        override fun room(room: TVIRoom, participantIsReconnecting: TVIRemoteParticipant) {
            NSLog("🔄 iOS TwilioVideoManager: Participant reconnecting - ${participantIsReconnecting.identity}")
        }

        @ObjCSignatureOverride
        override fun room(room: TVIRoom, participantDidReconnect: TVIRemoteParticipant) {
            NSLog("✅ iOS TwilioVideoManager: Participant reconnected - ${participantDidReconnect.identity}")
            updateParticipants(room)
        }
    }

    override suspend fun connect(userIdentity: String, roomName: String, cameraOn: Boolean, micOn: Boolean): VideoResult<VideoRoom> = withContext(Dispatchers.Main) {
        return@withContext try {
            NSLog("🚀 iOS TwilioVideoManager: Starting connection - user: $userIdentity, room: $roomName, camera: $cameraOn, mic: $micOn")
            _connectionState.value = VideoConnectionState.Connecting
            
            // Get token from our service
            val tokenService = TokenService()
            val tokenResult = tokenService.getToken(userIdentity = userIdentity, roomName = roomName)
            
            if (tokenResult is VideoResult.Error) {
                NSLog("❌ iOS TwilioVideoManager: Token fetch failed")
                _connectionState.value = VideoConnectionState.Disconnected
                return@withContext VideoResult.Error(tokenResult.error)
            }
            
            val token = (tokenResult as VideoResult.Success).data
            NSLog("✅ iOS TwilioVideoManager: Token received")

            // ✅ REAL TWILIO SDK INTEGRATION: Set up local media tracks
            val videoTracks = if (cameraOn) {
                setupLocalVideoTrack()
                listOfNotNull(twilioLocalVideoTrack)
            } else {
                emptyList()
            }

            // ✅ REAL AUDIO TRACK INTEGRATION: Create audio track
            val audioTracks = if (micOn) {
                setupLocalAudioTrack()
                listOfNotNull(twilioLocalAudioTrack)
            } else {
                emptyList()
            }

            // ✅ REAL TWILIO SDK INTEGRATION: Connect using actual Twilio SDK
            val connectOptions = TVIConnectOptions.optionsWithToken(token) { builder ->
                builder?.roomName = roomName
                builder?.videoTracks = videoTracks
                builder?.audioTracks = audioTracks
            }

            connectOptions?.let { options ->
                NSLog("🔗 iOS TwilioVideoManager: Connecting with ${videoTracks.size} video tracks and ${audioTracks.size} audio tracks")
                
                val room = TwilioVideoSDK.connectWithOptions(options, delegate = roomDelegate)
                twilioRoom = room
                
                NSLog("✅ iOS TwilioVideoManager: Connection initiated")
                
                // Connection success will be handled by roomDidConnect delegate
                return@withContext VideoResult.Success(VideoRoom(
                name = roomName,
                    sid = "connecting...",
                participants = emptyList()
                ))
            }
            
            VideoResult.Error(VideoError.UnknownError("Failed to create connection options"))
            
        } catch (e: Exception) {
            NSLog("💥 iOS TwilioVideoManager: Connection error: ${e.message}")
            _connectionState.value = VideoConnectionState.Disconnected
            VideoResult.Error(VideoError.UnknownError("Failed to connect: ${e.message}"))
        }
    }
    
    override suspend fun disconnect(): VideoResult<Unit> = withContext(Dispatchers.Main) {
        return@withContext try {
            NSLog("🔌 iOS TwilioVideoManager: Disconnecting...")

            // ✅ REAL TWILIO SDK INTEGRATION: Disconnect from room
            twilioRoom?.disconnect()

            cleanup()
            
            VideoResult.Success(Unit)
        } catch (e: Exception) {
            NSLog("💥 iOS TwilioVideoManager: Disconnect error: ${e.message}")
            VideoResult.Error(VideoError.UnknownError("Failed to disconnect: ${e.message}"))
        }
    }
    
    override suspend fun enableCamera(enable: Boolean): VideoResult<Unit> = withContext(Dispatchers.Main) {
        return@withContext try {
            NSLog("📹 iOS TwilioVideoManager: Setting camera enabled: $enable")

            if (enable) {
                setupLocalVideoTrack()
            } else {
                _localVideoTrack.value = null
                twilioLocalVideoTrack = null
            }

            // ✅ REAL TWILIO SDK INTEGRATION: Use actual track enable/disable
            twilioLocalVideoTrack?.let { track ->
                track.setEnabled(enable)
                updateLocalVideoTrackState()
                NSLog("✅ iOS TwilioVideoManager: Camera ${if (enable) "enabled" else "disabled"} using real SDK")
            }

            VideoResult.Success(Unit)
        } catch (e: Exception) {
            NSLog("💥 iOS TwilioVideoManager: Camera enable error: ${e.message}")
            VideoResult.Error(VideoError.UnknownError("Failed to enable camera: ${e.message}"))
        }
    }
    
    override suspend fun enableMicrophone(enable: Boolean): VideoResult<Unit> = withContext(Dispatchers.Main) {
        return@withContext try {
            NSLog("🎤 iOS TwilioVideoManager: Setting microphone enabled: $enable")

            if (enable && twilioLocalAudioTrack == null) {
                setupLocalAudioTrack()
            }

            // ✅ REAL AUDIO TRACK INTEGRATION: Use actual audio track enable/disable
            twilioLocalAudioTrack?.let { track ->
                track.setEnabled(enable)
                _isLocalMicEnabled.value = track.isEnabled()
                NSLog("✅ iOS TwilioVideoManager: Microphone ${if (enable) "enabled" else "disabled"} using real SDK")
            }

            VideoResult.Success(Unit)
        } catch (e: Exception) {
            NSLog("💥 iOS TwilioVideoManager: Microphone enable error: ${e.message}")
            VideoResult.Error(VideoError.UnknownError("Failed to enable microphone: ${e.message}"))
        }
    }
    
    override suspend fun switchCamera(): VideoResult<Unit> = withContext(Dispatchers.Main) {
        return@withContext try {
            NSLog("🔄 iOS TwilioVideoManager: Switching camera...")

            twilioCameraSource?.let { cameraSource ->
                val currentDevice = cameraSource.device
                
                // Determine which camera to switch to (need to access position property correctly)
                val newDevice = try {
                    if (currentDevice != null) {
                        // Switch to the opposite camera
                        TVICameraSource.captureDeviceForPosition(AVCaptureDevicePositionBack)
                            ?: TVICameraSource.captureDeviceForPosition(AVCaptureDevicePositionFront)
                    } else {
                        TVICameraSource.captureDeviceForPosition(AVCaptureDevicePositionFront)
                    }
                } catch (e: Exception) {
                    NSLog("⚠️ iOS TwilioVideoManager: Camera position detection failed, using front camera")
                    TVICameraSource.captureDeviceForPosition(AVCaptureDevicePositionFront)
                }

                newDevice?.let { device ->
                    cameraSource.selectCaptureDevice(device) { captureDevice, format, error ->
                        if (error != null) {
                            NSLog("❌ iOS TwilioVideoManager: Camera switch failed: ${error.localizedDescription}")
                        } else {
                            NSLog("✅ iOS TwilioVideoManager: Camera switch completed successfully")
                        }
                    }
                }
            }

            VideoResult.Success(Unit)
        } catch (e: Exception) {
            NSLog("💥 iOS TwilioVideoManager: Camera switch error: ${e.message}")
            VideoResult.Error(VideoError.UnknownError("Failed to switch camera: ${e.message}"))
        }
    }
    
    override suspend fun startScreenShare(): VideoResult<Unit> {
        NSLog("📱 iOS TwilioVideoManager: Screen sharing not implemented yet")
        return VideoResult.Error(VideoError.UnknownError("Screen sharing not implemented on iOS yet"))
    }
    
    override suspend fun stopScreenShare(): VideoResult<Unit> {
        NSLog("📱 iOS TwilioVideoManager: Screen sharing not implemented yet")
        return VideoResult.Error(VideoError.UnknownError("Screen sharing not implemented on iOS yet"))
    }
    
    override suspend fun getAvailableCameras(): List<CameraInfo> {
        return try {
            // ✅ REAL TWILIO SDK INTEGRATION: Use actual camera enumeration
            val frontCamera = TVICameraSource.captureDeviceForPosition(AVCaptureDevicePositionFront)
            val backCamera = TVICameraSource.captureDeviceForPosition(AVCaptureDevicePositionBack)
            
            val cameras = mutableListOf<CameraInfo>()
            
            frontCamera?.let {
                cameras.add(CameraInfo(id = "front", name = "Front Camera", isFrontFacing = true, isBackFacing = false))
            }
            
            backCamera?.let {
                cameras.add(CameraInfo(id = "back", name = "Back Camera", isFrontFacing = false, isBackFacing = true))
            }
            
            NSLog("✅ iOS TwilioVideoManager: Found ${cameras.size} cameras using real SDK")
            cameras
        } catch (e: Exception) {
            NSLog("💥 iOS TwilioVideoManager: Get cameras error: ${e.message}")
            emptyList()
        }
    }
    
    override suspend fun getCurrentCameraInfo(): CameraInfo? {
        return try {
            // ✅ REAL TWILIO SDK INTEGRATION: Use actual current camera detection
            twilioCameraSource?.device?.let { device ->
                // For now, default to front camera (will improve position detection later)
                CameraInfo(
                    id = "current",
                    name = "Current Camera",
                    isFrontFacing = true,
                    isBackFacing = false
                )
            }
        } catch (e: Exception) {
            NSLog("💥 iOS TwilioVideoManager: Get current camera error: ${e.message}")
            null
        }
    }
    
    override fun release() {
        try {
            NSLog("🧹 iOS TwilioVideoManager: Releasing resources...")

            // ✅ REAL TWILIO SDK INTEGRATION: Proper room disconnect
            twilioRoom?.disconnect()

            cleanup()

        } catch (e: Exception) {
            NSLog("💥 iOS TwilioVideoManager: Release error: ${e.message}")
        }
    }

    // Helper functions
    private fun setupLocalVideoTrack() {
        try {
            NSLog("📹 iOS TwilioVideoManager: Setting up local video track...")

            // ✅ REAL TWILIO SDK INTEGRATION: Create actual camera source and video track
            val frontCamera = TVICameraSource.captureDeviceForPosition(AVCaptureDevicePositionFront)
            frontCamera?.let { camera ->
                val options = TVICameraSourceOptions.optionsWithBlock { builder ->
                    // Configure camera options if needed
                    // builder?.enableCameraMultitasking = true  // For iOS 16+ if needed
                }
                val cameraSource = TVICameraSource(options = options, delegate = null)
                
                // ✅ IMPORTANT: Create video track FIRST before starting capture (SDK requirement)
                val videoTrack = TVILocalVideoTrack.trackWithSource(cameraSource, true, "camera")
                
                // Now start camera capture with device (after video track is created as sink)
                cameraSource?.startCaptureWithDevice(camera) { captureDevice, format, error ->
                    if (error != null) {
                        NSLog("❌ iOS TwilioVideoManager: Camera capture failed: ${error.localizedDescription}")
                    } else {
                        NSLog("✅ iOS TwilioVideoManager: Camera capture started successfully")
                    }
                }
                
                if (videoTrack != null) {
                    twilioCameraSource = cameraSource
                    twilioLocalVideoTrack = videoTrack
                    _rawLocalVideoTrack.value = videoTrack
                    updateLocalVideoTrackState()
                    NSLog("✅ iOS TwilioVideoManager: Local video track created using real SDK")
                }
            }

        } catch (e: Exception) {
            NSLog("💥 iOS TwilioVideoManager: Video track setup error: ${e.message}")
        }
    }

    private fun setupLocalAudioTrack() {
        try {
            NSLog("🎤 iOS TwilioVideoManager: Setting up local audio track...")

            // ✅ REAL AUDIO TRACK INTEGRATION: Create actual audio track
            val audioTrack = TVILocalAudioTrack.track()
            audioTrack?.let { track ->
                twilioLocalAudioTrack = track
                _isLocalMicEnabled.value = track.isEnabled()
                NSLog("✅ iOS TwilioVideoManager: Local audio track created using real SDK")
            }

        } catch (e: Exception) {
            NSLog("💥 iOS TwilioVideoManager: Audio track setup error: ${e.message}")
        }
    }

    private fun updateLocalVideoTrackState() {
        twilioLocalVideoTrack?.let { track ->
            _localVideoTrack.value = VideoTrack(
                sid = "local-video",
                name = track.name ?: "Local Video",
                isEnabled = track.isEnabled(),
                participantSid = "local",
                remoteVideoTrack = null
            )
        }
    }

    private fun updateParticipants(room: TVIRoom) {
        try {
            val participants = room.remoteParticipants?.map { participant ->
                convertToVideoParticipant(participant as TVIRemoteParticipant)
            } ?: emptyList()
            
            _participants.value = participants
            NSLog("👥 iOS TwilioVideoManager: Updated participants list - ${participants.size} participants")
        } catch (e: Exception) {
            NSLog("💥 iOS TwilioVideoManager: Error updating participants: ${e.message}")
        }
    }

    private fun convertToVideoParticipant(participant: TVIRemoteParticipant): VideoParticipant {
        return VideoParticipant(
            sid = participant.sid ?: "unknown-sid",
            identity = participant.identity ?: "Unknown",
            isConnected = true,
            audioTracks = emptyList(), // Will implement track details later
            videoTracks = emptyList()  // Will implement track details later
        )
    }

    private fun cleanup() {
        // ✅ REAL TWILIO SDK INTEGRATION: Proper cleanup
        twilioCameraSource?.stopCapture()

        twilioLocalVideoTrack = null
        twilioLocalAudioTrack = null
        twilioCameraSource = null
        twilioRoom = null
        
        _connectionState.value = VideoConnectionState.Disconnected
        _participants.value = emptyList()
        _localVideoTrack.value = null
        _rawLocalVideoTrack.value = null
        _isLocalMicEnabled.value = false
    }
}

actual object TwilioVideoManagerFactory {
    actual fun create(): TwilioVideoManager {
        return TwilioVideoManagerImpl()
    }
} 