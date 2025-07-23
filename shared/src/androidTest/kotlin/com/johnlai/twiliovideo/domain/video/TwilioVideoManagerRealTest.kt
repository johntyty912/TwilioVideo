package com.johnlai.twiliovideo.domain.video

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Before
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertNotNull

/**
 * Real integration tests for Android TwilioVideoManager
 * These tests use the actual Android context and can test real functionality
 */
class TwilioVideoManagerRealTest {
    
    private lateinit var context: Context
    private lateinit var videoManager: TwilioVideoManager
    
    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        videoManager = TwilioVideoManagerFactory.create(context)
    }
    
    @Test
    fun `should create video manager with context`() {
        assertNotNull(videoManager)
        assertTrue(videoManager is TwilioVideoManagerImpl)
    }
    
    @Test
    fun `should start in disconnected state`() = runTest {
        val connectionState = videoManager.connectionState.first()
        assertEquals(VideoConnectionState.Disconnected, connectionState)
    }
    
    @Test
    fun `should have empty participants list initially`() = runTest {
        val participants = videoManager.participants.first()
        assertTrue(participants.isEmpty())
    }
    
    @Test
    fun `should get available cameras`() = runTest {
        val cameras = videoManager.getAvailableCameras()
        assertTrue(cameras.isNotEmpty())
        
        // Should have at least one camera (front or back)
        val hasCamera = cameras.any { 
            it.isFrontFacing || it.isBackFacing 
        }
        assertTrue(hasCamera, "Should have at least one camera available")
    }
    
    @Test
    fun `should get current camera info`() = runTest {
        val currentCamera = videoManager.getCurrentCameraInfo()
        assertNotNull(currentCamera)
        assertEquals("front", currentCamera.id)
        assertTrue(currentCamera.isFrontFacing)
    }
    
    @Test
    fun `should enable and disable camera`() = runTest {
        val enableResult = videoManager.enableCamera(true)
        assertTrue(enableResult is VideoResult.Success)
        
        val disableResult = videoManager.enableCamera(false)
        assertTrue(disableResult is VideoResult.Success)
    }
    
    @Test
    fun `should enable and disable microphone`() = runTest {
        val enableResult = videoManager.enableMicrophone(true)
        assertTrue(enableResult is VideoResult.Success)
        
        val disableResult = videoManager.enableMicrophone(false)
        assertTrue(disableResult is VideoResult.Success)
    }
    
    @Test
    fun `should handle camera switch`() = runTest {
        val switchResult = videoManager.switchCamera()
        assertTrue(switchResult is VideoResult.Success)
    }
    
    // Note: We don't test actual room connection here because it requires 
    // network access and valid tokens. That would be better in a separate
    // integration test suite.
    
    @Test
    fun `should handle disconnect gracefully when not connected`() = runTest {
        val disconnectResult = videoManager.disconnect()
        assertTrue(disconnectResult is VideoResult.Success)
    }
    
    @Test
    fun `should handle release without errors`() {
        // Should not throw any exceptions
        videoManager.release()
    }
} 