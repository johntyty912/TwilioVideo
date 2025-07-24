package com.johnlai.twiliovideo.ui.meeting.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.johnlai.twiliovideo.domain.video.TwilioVideoManager
import com.twilio.video.LocalVideoTrack

@Composable
fun LocalVideoView(
    videoManager: TwilioVideoManager,
    isVideoEnabled: Boolean,
    userIdentity: String
) {
    // Observe the actual local video track for rendering
    val rawLocalVideoTrack by remember {
        // Access the raw video track from the Android implementation
        (videoManager as? com.johnlai.twiliovideo.domain.video.TwilioVideoManagerImpl)?.rawLocalVideoTrack
            ?: kotlinx.coroutines.flow.flowOf(null)
    }.collectAsStateWithLifecycle(initialValue = null)
    
    // Debug logging
    LaunchedEffect(rawLocalVideoTrack, isVideoEnabled) {
        println("LocalVideoView: rawLocalVideoTrack = $rawLocalVideoTrack")
        println("LocalVideoView: isVideoEnabled = $isVideoEnabled")
        println("LocalVideoView: should show video = ${isVideoEnabled && rawLocalVideoTrack != null}")
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        colors = CardDefaults.cardColors(
            containerColor = if (isVideoEnabled && rawLocalVideoTrack != null) 
                MaterialTheme.colorScheme.primaryContainer 
            else MaterialTheme.colorScheme.surfaceVariant
        ),
        border = androidx.compose.foundation.BorderStroke(
            2.dp, 
            MaterialTheme.colorScheme.primary
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (isVideoEnabled && rawLocalVideoTrack != null) {
                // Show actual video feed
                TwilioVideoView(
                    localVideoTrack = rawLocalVideoTrack,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Camera off or no track - show avatar
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(
                                MaterialTheme.colorScheme.primary,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userIdentity.take(2).uppercase(),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "You",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Text(
                        text = if (isVideoEnabled) "Connecting..." else "Camera Off",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // "You" badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(4.dp)
                ) {
                    Text(
                        text = "You",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
} 