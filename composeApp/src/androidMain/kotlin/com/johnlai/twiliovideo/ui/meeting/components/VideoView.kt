package com.johnlai.twiliovideo.ui.meeting.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.twilio.video.LocalVideoTrack
import com.twilio.video.RemoteVideoTrack
import com.twilio.video.VideoView

@Composable
fun TwilioVideoView(
    localVideoTrack: LocalVideoTrack? = null,
    remoteVideoTrack: RemoteVideoTrack? = null,
    modifier: Modifier = Modifier,
    placeholder: @Composable () -> Unit = {}
) {
    val context = LocalContext.current
    
    if (localVideoTrack != null || remoteVideoTrack != null) {
        AndroidView(
            factory = { ctx ->
                VideoView(ctx).apply {
                    // Configure the video view
                    mirror = localVideoTrack != null // Mirror local video
                }
            },
            update = { videoView ->
                // Remove previous video tracks
                localVideoTrack?.removeSink(videoView)
                remoteVideoTrack?.removeSink(videoView)
                
                // Attach the appropriate video track
                when {
                    localVideoTrack != null -> {
                        localVideoTrack.addSink(videoView)
                    }
                    remoteVideoTrack != null -> {
                        remoteVideoTrack.addSink(videoView)
                    }
                }
            },
            modifier = modifier
                .clip(RoundedCornerShape(12.dp))
        )
    } else {
        // Show placeholder when no video track is available
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            placeholder()
        }
    }
    
    // Clean up when composable is disposed
    DisposableEffect(localVideoTrack, remoteVideoTrack) {
        onDispose {
            // VideoView handles cleanup automatically when removed from parent
        }
    }
} 