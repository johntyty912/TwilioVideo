package com.johnlai.twiliovideo.domain.video

import com.johnlai.twiliovideo.shared.BuildConfig

/**
 * Android implementation of VideoConfig
 * Reads configuration from BuildConfig (injected from .env.local during build)
 */
actual object VideoConfig {
    actual val twilioTokenUrl: String = run {
        val url = BuildConfig.TWILIO_TOKEN_URL
        // Warn if using placeholder URL
        if (url.contains("your-api-endpoint.com")) {
            println("⚠️  WARNING: Using placeholder API URL. Please set your real API endpoint in .env.local")
        }
        url
    }
    actual val testUserIdentity: String = BuildConfig.TEST_USER_IDENTITY
} 