package com.johnlai.twiliovideo.domain.video

/**
 * iOS implementation of VideoConfig
 * TODO: Implement iOS-specific configuration reading (e.g., from Info.plist)
 */
actual object VideoConfig {
    actual val twilioTokenUrl: String = VideoConfigDefaults.DEFAULT_TOKEN_URL
    actual val testUserIdentity: String = VideoConfigDefaults.DEFAULT_USER_IDENTITY
} 