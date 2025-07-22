package com.johnlai.twiliovideo.domain.video

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Unit tests for TwilioVideoManager
 * Following the testing strategy outlined in the plan
 */
class TwilioVideoManagerTest {
    
    @Test
    fun `should connect to room successfully`() = runTest {
        val manager = TwilioVideoManagerFactory.create()
        val result = manager.connect("fake-token", "test-room")
        
        assertIs<VideoResult.Success<VideoRoom>>(result)
        assertEquals("test-room", result.data.name)
    }
    
    @Test
    fun `should handle connection state changes`() = runTest {
        val manager = TwilioVideoManagerFactory.create()
        
        // Initial state should be disconnected
        val initialState = manager.connectionState.first()
        assertIs<VideoConnectionState.Disconnected>(initialState)
        
        // Connect and verify state change
        manager.connect("fake-token", "test-room")
        val connectedState = manager.connectionState.first()
        assertIs<VideoConnectionState.Connected>(connectedState)
        assertEquals("test-room", connectedState.room.name)
    }
    
    @Test
    fun `should disconnect successfully`() = runTest {
        val manager = TwilioVideoManagerFactory.create()
        
        // Connect first
        manager.connect("fake-token", "test-room")
        
        // Then disconnect
        val result = manager.disconnect()
        assertIs<VideoResult.Success<Unit>>(result)
        
        // Verify state changed to disconnected
        val state = manager.connectionState.first()
        assertIs<VideoConnectionState.Disconnected>(state)
    }
    
    @Test
    fun `should enable camera successfully`() = runTest {
        val manager = TwilioVideoManagerFactory.create()
        val result = manager.enableCamera(true)
        
        assertIs<VideoResult.Success<Unit>>(result)
    }
    
    @Test
    fun `should enable microphone successfully`() = runTest {
        val manager = TwilioVideoManagerFactory.create()
        val result = manager.enableMicrophone(false)
        
        assertIs<VideoResult.Success<Unit>>(result)
    }
    
    @Test
    fun `should switch camera successfully`() = runTest {
        val manager = TwilioVideoManagerFactory.create()
        val result = manager.switchCamera()
        
        assertIs<VideoResult.Success<Unit>>(result)
    }
    
    @Test
    fun `should get available cameras`() = runTest {
        val manager = TwilioVideoManagerFactory.create()
        val cameras = manager.getAvailableCameras()
        
        assertTrue(cameras.isNotEmpty())
        assertTrue(cameras.any { it.isFrontFacing })
        assertTrue(cameras.any { it.isBackFacing })
    }
    
    @Test
    fun `should get current camera info`() = runTest {
        val manager = TwilioVideoManagerFactory.create()
        val currentCamera = manager.getCurrentCameraInfo()
        
        assertTrue(currentCamera != null)
    }
    
    @Test
    fun `should start screen share successfully`() = runTest {
        val manager = TwilioVideoManagerFactory.create()
        val result = manager.startScreenShare()
        
        assertIs<VideoResult.Success<Unit>>(result)
    }
    
    @Test
    fun `should stop screen share successfully`() = runTest {
        val manager = TwilioVideoManagerFactory.create()
        val result = manager.stopScreenShare()
        
        assertIs<VideoResult.Success<Unit>>(result)
    }
    
    @Test
    fun `should handle release properly`() = runTest {
        val manager = TwilioVideoManagerFactory.create()
        
        // Connect first
        manager.connect("fake-token", "test-room")
        
        // Release resources
        manager.release()
        
        // Verify state is disconnected after release
        val state = manager.connectionState.first()
        assertIs<VideoConnectionState.Disconnected>(state)
    }
    
    @Test
    fun `should maintain participant list state`() = runTest {
        val manager = TwilioVideoManagerFactory.create()
        
        // Initial participants should be empty
        val initialParticipants = manager.participants.first()
        assertTrue(initialParticipants.isEmpty())
    }
    
    @Test
    fun `should maintain network quality state`() = runTest {
        val manager = TwilioVideoManagerFactory.create()
        
        val networkQuality = manager.networkQuality.first()
        assertEquals(0, networkQuality.level)
        assertEquals(NetworkQualityLevel.UNKNOWN, networkQuality.local.audio)
        assertEquals(NetworkQualityLevel.UNKNOWN, networkQuality.local.video)
    }
} 