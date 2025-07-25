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
    val videoViewKey = remember(localVideoTrack, remoteVideoTrack) {
        (localVideoTrack?.hashCode() ?: 0) + (remoteVideoTrack?.hashCode() ?: 0)
    }
    val videoViewRef = remember { mutableStateOf<VideoView?>(null) }

    if (localVideoTrack != null || remoteVideoTrack != null) {
        key(videoViewKey) {
            AndroidView(
                factory = { ctx ->
                    VideoView(ctx).apply {
                        mirror = localVideoTrack != null
                        videoViewRef.value = this
                    }
                },
                update = { videoView: VideoView ->
                    // Remove all sinks first to avoid duplicates
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
                modifier = modifier.clip(RoundedCornerShape(12.dp))
            )
        }
        DisposableEffect(localVideoTrack, remoteVideoTrack) {
            onDispose {
                videoViewRef.value?.let { view ->
                    runCatching { localVideoTrack?.removeSink(view) }
                    runCatching { remoteVideoTrack?.removeSink(view) }
                }
            }
        }
    } else {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            placeholder()
        }
    }
} 