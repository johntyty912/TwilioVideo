package com.johnlai.twiliovideo.domain.video

import com.twilio.video.RemoteVideoTrack
import com.twilio.video.LocalVideoTrack
import com.twilio.video.VideoSink

/**
 * Android implementation of VideoTrackWrapper
 * Adapts Twilio's Android SDK RemoteVideoTrack to our common interface
 */
class AndroidRemoteVideoTrackWrapper(
    private val remoteVideoTrack: RemoteVideoTrack
) : VideoTrackWrapper {
    
    override fun addRenderer(renderer: Any) {
        // Android Twilio SDK uses addSink instead of addRenderer
        if (renderer is VideoSink) {
            remoteVideoTrack.addSink(renderer)
        } else {
            throw IllegalArgumentException("Expected VideoSink, got ${renderer::class.simpleName}")
        }
    }
    
    override fun removeRenderer(renderer: Any) {
        // Android Twilio SDK uses removeSink instead of removeRenderer
        if (renderer is VideoSink) {
            remoteVideoTrack.removeSink(renderer)
        } else {
            throw IllegalArgumentException("Expected VideoSink, got ${renderer::class.simpleName}")
        }
    }
    
    override val isEnabled: Boolean
        get() = remoteVideoTrack.isEnabled
    
    override val name: String?
        get() = remoteVideoTrack.name
}

/**
 * Android implementation of LocalVideoTrackWrapper
 * Adapts Twilio's Android SDK LocalVideoTrack to our common interface
 */
class AndroidLocalVideoTrackWrapper(
    private val localVideoTrack: LocalVideoTrack
) : LocalVideoTrackWrapper {
    
    override fun addRenderer(renderer: Any) {
        // Android Twilio SDK uses addSink instead of addRenderer
        if (renderer is VideoSink) {
            localVideoTrack.addSink(renderer)
        } else {
            throw IllegalArgumentException("Expected VideoSink, got ${renderer::class.simpleName}")
        }
    }
    
    override fun removeRenderer(renderer: Any) {
        // Android Twilio SDK uses removeSink instead of removeRenderer
        if (renderer is VideoSink) {
            localVideoTrack.removeSink(renderer)
        } else {
            throw IllegalArgumentException("Expected VideoSink, got ${renderer::class.simpleName}")
        }
    }
    
    override fun enable(enable: Boolean) {
        localVideoTrack.enable(enable)
    }
    
    override val isEnabled: Boolean
        get() = localVideoTrack.isEnabled
    
    override val name: String?
        get() = localVideoTrack.name
}

// Platform-specific factory functions for Android
fun createVideoTrackWrapper(platformTrack: Any): VideoTrackWrapper {
    return when (platformTrack) {
        is RemoteVideoTrack -> AndroidRemoteVideoTrackWrapper(platformTrack)
        else -> throw IllegalArgumentException("Unsupported platform track type: ${platformTrack::class.simpleName}")
    }
}

fun createLocalVideoTrackWrapper(platformTrack: Any): LocalVideoTrackWrapper {
    return when (platformTrack) {
        is LocalVideoTrack -> AndroidLocalVideoTrackWrapper(platformTrack)
        else -> throw IllegalArgumentException("Unsupported platform track type: ${platformTrack::class.simpleName}")
    }
} 