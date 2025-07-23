package com.johnlai.twiliovideo.domain.video

import com.johnlai.twiliovideo.shared.BuildConfig

/**
 * Android implementation of VideoConfig
 * Reads configuration from BuildConfig (injected from .env.local during build)
 */
actual object VideoConfig {
    actual val twilioTokenUrl: String = BuildConfig.TWILIO_TOKEN_URL
    actual val testUserIdentity: String = BuildConfig.TEST_USER_IDENTITY
} 