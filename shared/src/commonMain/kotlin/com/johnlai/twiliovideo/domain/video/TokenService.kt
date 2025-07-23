package com.johnlai.twiliovideo.domain.video

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Token service client for getting Twilio video tokens
 * Uses the existing API endpoint: https://api.robocore.ai/twilio/video_token
 */
class TokenService(private val baseUrl: String = "https://api.robocore.ai") {
    
    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }
    }
    
    /**
     * Request model for token API
     * Matches your Flutter GetTokenModel
     */
    @Serializable
    data class GetTokenRequest(
        val userIdentity: String,
        val roomName: String
    )
    
    /**
     * Response model for token API
     * Matches your Flutter GetTokenResult
     */
    @Serializable
    data class GetTokenResponse(
        val token: String,
        val roomName: String? = null,
        val identity: String? = null
    )
    
    /**
     * Get Twilio video token from the API
     * @param userIdentity User identity (should be "user")
     * @param roomName Room name (10-character alphanumeric string)
     * @return VideoResult containing the token or error
     */
    suspend fun getToken(userIdentity: String, roomName: String): VideoResult<String> {
        return try {
            val request = GetTokenRequest(
                userIdentity = userIdentity,
                roomName = roomName
            )
            
            val response: GetTokenResponse = httpClient.post("$baseUrl/twilio/video_token") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
            
            VideoResult.Success(response.token)
            
        } catch (e: Exception) {
            VideoResult.Error(VideoError.NetworkError)
        }
    }
    
    /**
     * Generate a random room name as required by the API
     * Format: 10-character alphanumeric string
     */
    fun generateRoomName(): String {
        val chars = "abcdefghijklmnopqrstuvwxyz0123456789"
        return (1..10)
            .map { chars.random() }
            .joinToString("")
    }
    
    /**
     * Get the standard user identity
     * Always returns "user" as required by the API
     */
    fun getStandardUserIdentity(): String = "user"
    
    /**
     * Clean up resources
     */
    fun close() {
        httpClient.close()
    }
} 