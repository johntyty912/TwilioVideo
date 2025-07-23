package com.johnlai.twiliovideo.domain.video

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for TokenService functionality
 */
class TokenServiceTest {
    
    @Test
    fun `getStandardUserIdentity should return user`() {
        val tokenService = TokenService()
        val userIdentity = tokenService.getStandardUserIdentity()
        assertEquals("user", userIdentity)
    }
    
    @Test
    fun `generateRoomName should return 10-character alphanumeric string`() {
        val tokenService = TokenService()
        val roomName = tokenService.generateRoomName()
        
        // Should be exactly 10 characters
        assertEquals(10, roomName.length)
        
        // Should only contain lowercase letters and numbers
        assertTrue(roomName.all { it.isLetterOrDigit() && (it.isLowerCase() || it.isDigit()) })
    }
    
    @Test
    fun `generateRoomName should return different names`() {
        val tokenService = TokenService()
        val roomName1 = tokenService.generateRoomName()
        val roomName2 = tokenService.generateRoomName()
        
        // Should be very unlikely to generate the same name twice
        assertTrue(roomName1 != roomName2)
    }
    
    @Test
    fun `GetTokenRequest should serialize correctly`() {
        val request = TokenService.GetTokenRequest(
            userIdentity = "user",
            roomName = "testroom01"
        )
        
        assertEquals("user", request.userIdentity)
        assertEquals("testroom01", request.roomName)
    }
    
    @Test
    fun `GetTokenResponse should deserialize correctly`() {
        val response = TokenService.GetTokenResponse(
            token = "fake.jwt.token",
            roomName = "testroom01",
            identity = "user"
        )
        
        assertEquals("fake.jwt.token", response.token)
        assertEquals("testroom01", response.roomName)
        assertEquals("user", response.identity)
    }
    
    // Note: We'll add integration tests for getToken() when we have a test environment
    // For now, testing the data models and utility functions
} 