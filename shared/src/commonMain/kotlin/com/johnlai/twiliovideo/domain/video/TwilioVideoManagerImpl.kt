package com.johnlai.twiliovideo.domain.video

import kotlinx.coroutines.flow.Flow

/**
 * Expected class that will be implemented on each platform
 * Android and iOS will provide their own actual implementations
 */
expect class TwilioVideoManagerImpl() : TwilioVideoManager {
    
    override val connectionState: Flow<VideoConnectionState>
    override val participants: Flow<List<VideoParticipant>>
    override val localVideoTrack: Flow<VideoTrack?>
    override val networkQuality: Flow<NetworkQuality>
    
    override suspend fun connect(accessToken: String, roomName: String): VideoResult<VideoRoom>
    override suspend fun disconnect(): VideoResult<Unit>
    
    override suspend fun enableCamera(enable: Boolean): VideoResult<Unit>
    override suspend fun enableMicrophone(enable: Boolean): VideoResult<Unit>
    override suspend fun switchCamera(): VideoResult<Unit>
    
    override suspend fun startScreenShare(): VideoResult<Unit>
    override suspend fun stopScreenShare(): VideoResult<Unit>
    
    override suspend fun getAvailableCameras(): List<CameraInfo>
    override suspend fun getCurrentCameraInfo(): CameraInfo?
    
    override fun release()
} 