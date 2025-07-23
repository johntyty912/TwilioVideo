package com.johnlai.twiliovideo.domain.video

import kotlinx.coroutines.flow.Flow

/**
 * Platform-specific implementation of TwilioVideoManager
 * Each platform provides its own actual implementation
 */
expect class TwilioVideoManagerImpl() : TwilioVideoManager

/**
 * Factory for creating TwilioVideoManager instances
 */
expect object TwilioVideoManagerFactory {
    fun create(): TwilioVideoManager
} 