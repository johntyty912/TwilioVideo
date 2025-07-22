package com.johnlai.twiliovideo.domain.video

import kotlinx.coroutines.flow.Flow

/**
 * Main interface for Twilio Video SDK integration
 * This interface will be implemented differently on each platform using expect/actual pattern
 */
interface TwilioVideoManager {
    
    // Observable state flows
    val connectionState: Flow<VideoConnectionState>
    val participants: Flow<List<VideoParticipant>>
    val localVideoTrack: Flow<VideoTrack?>
    val networkQuality: Flow<NetworkQuality>
    
    // Room connection management
    suspend fun connect(accessToken: String, roomName: String): VideoResult<VideoRoom>
    suspend fun disconnect(): VideoResult<Unit>
    
    // Media control
    suspend fun enableCamera(enable: Boolean): VideoResult<Unit>
    suspend fun enableMicrophone(enable: Boolean): VideoResult<Unit>
    suspend fun switchCamera(): VideoResult<Unit>
    
    // Screen sharing
    suspend fun startScreenShare(): VideoResult<Unit>
    suspend fun stopScreenShare(): VideoResult<Unit>
    
    // Utility methods
    suspend fun getAvailableCameras(): List<CameraInfo>
    suspend fun getCurrentCameraInfo(): CameraInfo?
    
    // Cleanup
    fun release()
}

/**
 * Camera information
 */
data class CameraInfo(
    val id: String,
    val name: String,
    val isFrontFacing: Boolean,
    val isBackFacing: Boolean
)

/**
 * Factory interface for creating TwilioVideoManager instances
 */
expect object TwilioVideoManagerFactory {
    fun create(): TwilioVideoManager
} 