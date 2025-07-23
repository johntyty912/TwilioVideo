package com.johnlai.twiliovideo.test

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for TestUtils functionality
 */
class TestUtilsTest {
    
    @Test
    fun `generateRandomRoomName should return 10-character alphanumeric string`() {
        val roomName = TestUtils.generateRandomRoomName()
        
        // Should be exactly 10 characters
        assertEquals(10, roomName.length)
        
        // Should only contain lowercase letters and numbers
        assertTrue(roomName.all { it.isLetterOrDigit() && (it.isLowerCase() || it.isDigit()) })
    }
    
    @Test
    fun `generateRandomRoomName should return different names`() {
        val roomName1 = TestUtils.generateRandomRoomName()
        val roomName2 = TestUtils.generateRandomRoomName()
        
        // Should be very unlikely to generate the same name twice
        assertTrue(roomName1 != roomName2)
    }
    
    @Test
    fun `getTestUserIdentity should always return user`() {
        val userIdentity = TestUtils.getTestUserIdentity()
        assertEquals("user", userIdentity)
    }
    
    @Test
    fun `generateTestRoomNames should return correct count`() {
        val count = 5
        val roomNames = TestUtils.generateTestRoomNames(count)
        
        assertEquals(count, roomNames.size)
        
        // All should be unique
        assertEquals(count, roomNames.toSet().size)
        
        // All should be 10 characters
        assertTrue(roomNames.all { it.length == 10 })
    }
    
    @Test
    fun `createTokenRequest should use correct parameters`() {
        val request = TestUtils.createTokenRequest()
        
        assertEquals("user", request.userIdentity)
        assertEquals(10, request.roomName.length)
    }
    
    @Test
    fun `createTokenRequest should use provided room name`() {
        val customRoomName = "customroom1"
        val request = TestUtils.createTokenRequest(customRoomName)
        
        assertEquals("user", request.userIdentity)
        assertEquals(customRoomName, request.roomName)
    }
} 