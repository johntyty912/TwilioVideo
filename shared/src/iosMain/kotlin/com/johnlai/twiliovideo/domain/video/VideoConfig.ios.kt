package com.johnlai.twiliovideo.domain.video

import platform.Foundation.NSBundle

/**
 * iOS implementation of VideoConfig
 * Reads configuration from xcconfig files via Info.plist
 */
actual object VideoConfig {
    actual val twilioTokenUrl: String = run {
        val baseUrl = getConfigValue("TWILIO_TOKEN_URL") ?: VideoConfigDefaults.DEFAULT_TOKEN_URL.removePrefix("https://")
        val fullUrl = if (baseUrl.startsWith("http://") || baseUrl.startsWith("https://")) {
            baseUrl
        } else {
            "https://$baseUrl"
        }
        
        // Warn if using placeholder URL
        if (fullUrl.contains("your-api-endpoint.com")) {
            println("⚠️  WARNING: Using placeholder API URL. Please set your real API endpoint in .env.local or Config.local.xcconfig")
        }
        
        fullUrl
    }
    actual val testUserIdentity: String = getConfigValue("TEST_USER_IDENTITY") ?: VideoConfigDefaults.DEFAULT_USER_IDENTITY
    
    private fun getConfigValue(key: String): String? {
        return NSBundle.mainBundle.infoDictionary?.get(key) as? String
    }
} 