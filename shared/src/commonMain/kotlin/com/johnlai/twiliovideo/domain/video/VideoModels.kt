package com.johnlai.twiliovideo.domain.video

import kotlinx.coroutines.flow.Flow

// Core video models as defined in the architecture plan

data class VideoRoom(
    val name: String,
    val sid: String,
    val participants: List<VideoParticipant>
)

data class VideoParticipant(
    val identity: String,
    val sid: String,
    val isConnected: Boolean,
    val videoTracks: List<VideoTrack>,
    val audioTracks: List<AudioTrack>
)

sealed class VideoConnectionState {
    object Disconnected : VideoConnectionState()
    object Connecting : VideoConnectionState()
    data class Connected(val room: VideoRoom) : VideoConnectionState()
    data class Failed(val error: VideoError) : VideoConnectionState()
    object Reconnecting : VideoConnectionState()
}

data class VideoTrack(
    val sid: String,
    val name: String,
    val isEnabled: Boolean,
    val participantSid: String
)

data class AudioTrack(
    val sid: String,
    val name: String,
    val isEnabled: Boolean,
    val participantSid: String
)

data class NetworkQuality(
    val level: Int, // 0-5 (0 = unknown, 1 = poor, 5 = excellent)
    val local: NetworkQualityStats,
    val remote: Map<String, NetworkQualityStats>
)

data class NetworkQualityStats(
    val audio: NetworkQualityLevel,
    val video: NetworkQualityLevel
)

enum class NetworkQualityLevel(val value: Int) {
    UNKNOWN(0),
    POOR(1),
    LOW(2),
    MODERATE(3),
    GOOD(4),
    EXCELLENT(5)
}

sealed class VideoError(val message: String) {
    object ConnectionFailed : VideoError("Failed to connect to video room")
    object Disconnected : VideoError("Disconnected from video room")
    object InvalidToken : VideoError("Invalid access token")
    object PermissionDenied : VideoError("Camera or microphone permission denied")
    object NetworkError : VideoError("Network connection error")
    data class UnknownError(val details: String) : VideoError("Unknown error: $details")
    
    fun toException(): Exception = Exception(message)
}

// Result wrapper for async operations
sealed class VideoResult<T> {
    data class Success<T>(val data: T) : VideoResult<T>()
    data class Error<T>(val error: VideoError) : VideoResult<T>()
    
    inline fun onSuccess(action: (T) -> Unit): VideoResult<T> {
        if (this is Success) action(data)
        return this
    }
    
    inline fun onError(action: (VideoError) -> Unit): VideoResult<T> {
        if (this is Error) action(error)
        return this
    }
} 