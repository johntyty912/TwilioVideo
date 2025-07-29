@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.johnlai.twiliovideo.domain.video

import cocoapods.TwilioVideo.TVIRemoteVideoTrack
import cocoapods.TwilioVideo.TVILocalVideoTrack
import cocoapods.TwilioVideo.TVIVideoRendererProtocol
import platform.Foundation.NSLog

/**
 * iOS implementation of VideoTrackWrapper
 * Adapts Twilio's iOS SDK TVIRemoteVideoTrack to our common interface
 */
class IOSRemoteVideoTrackWrapper(
    private val remoteVideoTrack: TVIRemoteVideoTrack
) : VideoTrackWrapper {
    
    override fun addRenderer(renderer: Any) {
        if (renderer is TVIVideoRendererProtocol) {
            remoteVideoTrack.addRenderer(renderer)
        } else {
            throw IllegalArgumentException("Expected TVIVideoRendererProtocol, got ${renderer::class.simpleName}")
        }
    }
    
    override fun removeRenderer(renderer: Any) {
        if (renderer is TVIVideoRendererProtocol) {
            remoteVideoTrack.removeRenderer(renderer)
        } else {
            throw IllegalArgumentException("Expected TVIVideoRendererProtocol, got ${renderer::class.simpleName}")
        }
    }
    
    override val isEnabled: Boolean
        get() = remoteVideoTrack.isEnabled()
    
    override val name: String?
        get() = remoteVideoTrack.name
}

/**
 * iOS implementation of LocalVideoTrackWrapper
 * Adapts Twilio's iOS SDK TVILocalVideoTrack to our common interface
 */
class IOSLocalVideoTrackWrapper(
    private val localVideoTrack: TVILocalVideoTrack
) : LocalVideoTrackWrapper {
    
    override fun addRenderer(renderer: Any) {
        if (renderer is TVIVideoRendererProtocol) {
            NSLog("🎥 iOSLocalVideoTrackWrapper: Adding renderer of type ${renderer::class.simpleName}")
            localVideoTrack.addRenderer(renderer)
            NSLog("✅ iOSLocalVideoTrackWrapper: Successfully added renderer")
        } else {
            NSLog("❌ iOSLocalVideoTrackWrapper: Expected TVIVideoRendererProtocol, got ${renderer::class.simpleName}")
            throw IllegalArgumentException("Expected TVIVideoRendererProtocol, got ${renderer::class.simpleName}")
        }
    }
    
    override fun removeRenderer(renderer: Any) {
        if (renderer is TVIVideoRendererProtocol) {
            NSLog("🎥 iOSLocalVideoTrackWrapper: Removing renderer of type ${renderer::class.simpleName}")
            localVideoTrack.removeRenderer(renderer)
            NSLog("✅ iOSLocalVideoTrackWrapper: Successfully removed renderer")
        } else {
            NSLog("❌ iOSLocalVideoTrackWrapper: Expected TVIVideoRendererProtocol, got ${renderer::class.simpleName}")
            throw IllegalArgumentException("Expected TVIVideoRendererProtocol, got ${renderer::class.simpleName}")
        }
    }
    
    override fun enable(enable: Boolean) {
        localVideoTrack.setEnabled(enable)
    }
    
    override val isEnabled: Boolean
        get() = localVideoTrack.isEnabled()
    
    override val name: String?
        get() = localVideoTrack.name
}

// Platform-specific factory functions for iOS
@Throws(IllegalArgumentException::class)
fun createVideoTrackWrapper(platformTrack: Any): VideoTrackWrapper {
    return when (platformTrack) {
        is TVIRemoteVideoTrack -> IOSRemoteVideoTrackWrapper(platformTrack)
        else -> throw IllegalArgumentException("Unsupported platform track type: ${platformTrack::class.simpleName}")
    }
}

@Throws(IllegalArgumentException::class)
fun createLocalVideoTrackWrapper(platformTrack: Any): LocalVideoTrackWrapper {
    return when (platformTrack) {
        is TVILocalVideoTrack -> IOSLocalVideoTrackWrapper(platformTrack)
        else -> throw IllegalArgumentException("Unsupported platform track type: ${platformTrack::class.simpleName}")
    }
} 