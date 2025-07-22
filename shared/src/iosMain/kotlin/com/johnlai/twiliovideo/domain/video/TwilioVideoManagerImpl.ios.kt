package com.johnlai.twiliovideo.domain.video

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * iOS implementation of TwilioVideoManager
 * This is a stub implementation for Phase 1 - will be fully implemented in Phase 3
 */
actual class TwilioVideoManagerImpl : TwilioVideoManager {
    
    private val _connectionState = MutableStateFlow<VideoConnectionState>(VideoConnectionState.Disconnected)
    private val _participants = MutableStateFlow<List<VideoParticipant>>(emptyList())
    private val _localVideoTrack = MutableStateFlow<VideoTrack?>(null)
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
    
    actual override val connectionState: Flow<VideoConnectionState> = _connectionState.asStateFlow()
    actual override val participants: Flow<List<VideoParticipant>> = _participants.asStateFlow()
    actual override val localVideoTrack: Flow<VideoTrack?> = _localVideoTrack.asStateFlow()
    actual override val networkQuality: Flow<NetworkQuality> = _networkQuality.asStateFlow()
    
    actual override suspend fun connect(accessToken: String, roomName: String): VideoResult<VideoRoom> {
        // Stub implementation - will integrate Twilio iOS SDK in Phase 3
        _connectionState.value = VideoConnectionState.Connecting
        
        // Simulate connection for now
        val room = VideoRoom(
            name = roomName,
            sid = "stub-room-sid-ios",
            participants = emptyList()
        )
        
        _connectionState.value = VideoConnectionState.Connected(room)
        return VideoResult.Success(room)
    }
    
    actual override suspend fun disconnect(): VideoResult<Unit> {
        _connectionState.value = VideoConnectionState.Disconnected
        _participants.value = emptyList()
        _localVideoTrack.value = null
        return VideoResult.Success(Unit)
    }
    
    actual override suspend fun enableCamera(enable: Boolean): VideoResult<Unit> {
        // Stub implementation
        return VideoResult.Success(Unit)
    }
    
    actual override suspend fun enableMicrophone(enable: Boolean): VideoResult<Unit> {
        // Stub implementation
        return VideoResult.Success(Unit)
    }
    
    actual override suspend fun switchCamera(): VideoResult<Unit> {
        // Stub implementation
        return VideoResult.Success(Unit)
    }
    
    actual override suspend fun startScreenShare(): VideoResult<Unit> {
        // Stub implementation
        return VideoResult.Success(Unit)
    }
    
    actual override suspend fun stopScreenShare(): VideoResult<Unit> {
        // Stub implementation
        return VideoResult.Success(Unit)
    }
    
    actual override suspend fun getAvailableCameras(): List<CameraInfo> {
        // Stub implementation
        return listOf(
            CameraInfo(id = "front", name = "Front Camera", isFrontFacing = true, isBackFacing = false),
            CameraInfo(id = "back", name = "Back Camera", isFrontFacing = false, isBackFacing = true)
        )
    }
    
    actual override suspend fun getCurrentCameraInfo(): CameraInfo? {
        // Stub implementation
        return CameraInfo(id = "front", name = "Front Camera", isFrontFacing = true, isBackFacing = false)
    }
    
    actual override fun release() {
        _connectionState.value = VideoConnectionState.Disconnected
        _participants.value = emptyList()
        _localVideoTrack.value = null
    }
}

actual object TwilioVideoManagerFactory {
    actual fun create(): TwilioVideoManager {
        return TwilioVideoManagerImpl()
    }
} 