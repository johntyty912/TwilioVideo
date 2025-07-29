package com.johnlai.twiliovideo.domain.video

/**
 * Common interface for video track operations across platforms
 * This abstracts the differences between Twilio's Android and iOS SDKs
 */
interface VideoTrackWrapper {
    fun addRenderer(renderer: Any)
    fun removeRenderer(renderer: Any)
    val isEnabled: Boolean
    val name: String?
}

/**
 * Common interface for local video track operations
 */
interface LocalVideoTrackWrapper {
    fun addRenderer(renderer: Any)
    fun removeRenderer(renderer: Any)
    fun enable(enable: Boolean)
    val isEnabled: Boolean
    val name: String?
} 