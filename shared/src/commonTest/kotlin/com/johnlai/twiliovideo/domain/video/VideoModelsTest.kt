package com.johnlai.twiliovideo.domain.video

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for video data models
 */
class VideoModelsTest {
    
    @Test
    fun `should create VideoRoom correctly`() {
        val participants = listOf(
            VideoParticipant(
                identity = "user1",
                sid = "participant-sid-1",
                isConnected = true,
                videoTracks = emptyList(),
                audioTracks = emptyList()
            )
        )
        
        val room = VideoRoom(
            name = "test-room",
            sid = "room-sid-123",
            participants = participants
        )
        
        assertEquals("test-room", room.name)
        assertEquals("room-sid-123", room.sid)
        assertEquals(1, room.participants.size)
        assertEquals("user1", room.participants.first().identity)
    }
    
    @Test
    fun `should create VideoParticipant correctly`() {
        val videoTrack = VideoTrack(
            sid = "video-track-1",
            name = "camera",
            isEnabled = true,
            participantSid = "participant-1"
        )
        
        val audioTrack = AudioTrack(
            sid = "audio-track-1",
            name = "microphone",
            isEnabled = false,
            participantSid = "participant-1"
        )
        
        val participant = VideoParticipant(
            identity = "user1",
            sid = "participant-1",
            isConnected = true,
            videoTracks = listOf(videoTrack),
            audioTracks = listOf(audioTrack)
        )
        
        assertEquals("user1", participant.identity)
        assertEquals("participant-1", participant.sid)
        assertTrue(participant.isConnected)
        assertEquals(1, participant.videoTracks.size)
        assertEquals(1, participant.audioTracks.size)
        assertTrue(participant.videoTracks.first().isEnabled)
        assertFalse(participant.audioTracks.first().isEnabled)
    }
    
    @Test
    fun `should handle VideoConnectionState sealed class`() {
        val disconnected = VideoConnectionState.Disconnected
        val connecting = VideoConnectionState.Connecting
        val reconnecting = VideoConnectionState.Reconnecting
        
        val room = VideoRoom("test", "sid", emptyList())
        val connected = VideoConnectionState.Connected(room)
        
        val error = VideoError.ConnectionFailed
        val failed = VideoConnectionState.Failed(error)
        
        assertTrue(disconnected is VideoConnectionState.Disconnected)
        assertTrue(connecting is VideoConnectionState.Connecting)
        assertTrue(reconnecting is VideoConnectionState.Reconnecting)
        assertTrue(connected is VideoConnectionState.Connected)
        assertTrue(failed is VideoConnectionState.Failed)
        
        assertEquals("test", (connected as VideoConnectionState.Connected).room.name)
        assertEquals("Failed to connect to video room", (failed as VideoConnectionState.Failed).error.message)
    }
    
    @Test
    fun `should handle VideoError types correctly`() {
        val connectionFailed = VideoError.ConnectionFailed
        val disconnected = VideoError.Disconnected
        val invalidToken = VideoError.InvalidToken
        val permissionDenied = VideoError.PermissionDenied
        val networkError = VideoError.NetworkError
        val unknownError = VideoError.UnknownError("Custom error")
        
        assertEquals("Failed to connect to video room", connectionFailed.message)
        assertEquals("Disconnected from video room", disconnected.message)
        assertEquals("Invalid access token", invalidToken.message)
        assertEquals("Camera or microphone permission denied", permissionDenied.message)
        assertEquals("Network connection error", networkError.message)
        assertEquals("Unknown error: Custom error", unknownError.message)
    }
    
    @Test
    fun `should convert VideoError to Exception`() {
        val error = VideoError.ConnectionFailed
        val exception = error.toException()
        
        assertEquals("Failed to connect to video room", exception.message)
    }
    
    @Test
    fun `should handle VideoResult Success case`() {
        val result = VideoResult.Success("test-data")
        
        var successCalled = false
        var errorCalled = false
        
        result.onSuccess { data ->
            assertEquals("test-data", data)
            successCalled = true
        }.onError {
            errorCalled = true
        }
        
        assertTrue(successCalled)
        assertFalse(errorCalled)
    }
    
    @Test
    fun `should handle VideoResult Error case`() {
        val error = VideoError.ConnectionFailed
        val result = VideoResult.Error<String>(error)
        
        var successCalled = false
        var errorCalled = false
        
        result.onSuccess {
            successCalled = true
        }.onError { resultError ->
            assertEquals(error, resultError)
            errorCalled = true
        }
        
        assertFalse(successCalled)
        assertTrue(errorCalled)
    }
    
    @Test
    fun `should create NetworkQuality correctly`() {
        val localStats = NetworkQualityStats(
            audio = NetworkQualityLevel.GOOD,
            video = NetworkQualityLevel.EXCELLENT
        )
        
        val remoteStats = mapOf(
            "participant-1" to NetworkQualityStats(
                audio = NetworkQualityLevel.MODERATE,
                video = NetworkQualityLevel.POOR
            )
        )
        
        val networkQuality = NetworkQuality(
            level = 4,
            local = localStats,
            remote = remoteStats
        )
        
        assertEquals(4, networkQuality.level)
        assertEquals(NetworkQualityLevel.GOOD, networkQuality.local.audio)
        assertEquals(NetworkQualityLevel.EXCELLENT, networkQuality.local.video)
        assertEquals(1, networkQuality.remote.size)
        assertEquals(NetworkQualityLevel.MODERATE, networkQuality.remote["participant-1"]?.audio)
    }
    
    @Test
    fun `should handle NetworkQualityLevel enum values`() {
        assertEquals(0, NetworkQualityLevel.UNKNOWN.value)
        assertEquals(1, NetworkQualityLevel.POOR.value)
        assertEquals(2, NetworkQualityLevel.LOW.value)
        assertEquals(3, NetworkQualityLevel.MODERATE.value)
        assertEquals(4, NetworkQualityLevel.GOOD.value)
        assertEquals(5, NetworkQualityLevel.EXCELLENT.value)
    }
    
    @Test
    fun `should create CameraInfo correctly`() {
        val frontCamera = CameraInfo(
            id = "front-cam",
            name = "Front Camera",
            isFrontFacing = true,
            isBackFacing = false
        )
        
        val backCamera = CameraInfo(
            id = "back-cam",
            name = "Back Camera",
            isFrontFacing = false,
            isBackFacing = true
        )
        
        assertEquals("front-cam", frontCamera.id)
        assertTrue(frontCamera.isFrontFacing)
        assertFalse(frontCamera.isBackFacing)
        
        assertEquals("back-cam", backCamera.id)
        assertFalse(backCamera.isFrontFacing)
        assertTrue(backCamera.isBackFacing)
    }
} 