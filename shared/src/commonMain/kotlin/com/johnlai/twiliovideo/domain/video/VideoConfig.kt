package com.johnlai.twiliovideo.domain.video

/**
 * Configuration interface for video service settings
 * Platform-specific implementations provide actual values
 */
expect object VideoConfig {
    val twilioTokenUrl: String
    val testUserIdentity: String
}

/**
 * Default configuration values (fallbacks)
 */
object VideoConfigDefaults {
    const val DEFAULT_TOKEN_URL = "https://your-api-endpoint.com/twilio/video_token"
    const val DEFAULT_USER_IDENTITY = "user"
} 