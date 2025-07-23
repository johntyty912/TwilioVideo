package com.johnlai.twiliovideo.test

import kotlin.random.Random

/**
 * Test utilities for Twilio Video KMP testing
 */
object TestUtils {
    
    /**
     * Generate a random room name as required by the token service
     * Format: 10-character alphanumeric string (e.g., "a1b2c3d4e5")
     */
    fun generateRandomRoomName(): String {
        val chars = "abcdefghijklmnopqrstuvwxyz0123456789"
        return (1..10)
            .map { chars.random() }
            .joinToString("")
    }
    
    /**
     * Get the standard user identity for testing
     * Always returns "user" as required by the token service
     */
    fun getTestUserIdentity(): String = "user"
    
    /**
     * Generate test room names for different scenarios
     */
    fun generateTestRoomNames(count: Int): List<String> {
        return (1..count).map { generateRandomRoomName() }
    }
    
    /**
     * Create test token request data
     */
    data class TokenRequest(
        val userIdentity: String,
        val roomName: String
    )
    
    /**
     * Create a token request for testing
     */
    fun createTokenRequest(roomName: String? = null): TokenRequest {
        return TokenRequest(
            userIdentity = getTestUserIdentity(),
            roomName = roomName ?: generateRandomRoomName()
        )
    }
} 