package com.johnlai.twiliovideo.ui.meeting.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.johnlai.twiliovideo.domain.video.TwilioVideoManager

@Composable
fun MeetingControls(
    videoManager: TwilioVideoManager,
    onLeaveRoom: () -> Unit,
    onCameraStateChange: (Boolean) -> Unit = {} // Add callback for camera state
) {
    var isMicEnabled by remember { mutableStateOf(true) }
    var isVideoEnabled by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    // Notify parent of camera state changes
    LaunchedEffect(isVideoEnabled) {
        onCameraStateChange(isVideoEnabled)
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Microphone toggle
            FloatingActionButton(
                onClick = {
                    scope.launch {
                        isMicEnabled = !isMicEnabled
                        videoManager.enableMicrophone(isMicEnabled)
                    }
                },
                containerColor = if (isMicEnabled) 
                    MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                contentColor = if (isMicEnabled)
                    MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onError
            ) {
                Text(
                    text = if (isMicEnabled) "🎤" else "🔇",
                    style = MaterialTheme.typography.titleLarge
                )
            }
            
            // Video toggle
            FloatingActionButton(
                onClick = {
                    scope.launch {
                        isVideoEnabled = !isVideoEnabled
                        videoManager.enableCamera(isVideoEnabled)
                    }
                },
                containerColor = if (isVideoEnabled)
                    MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                contentColor = if (isVideoEnabled)
                    MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onError
            ) {
                Text(
                    text = if (isVideoEnabled) "📹" else "📵",
                    style = MaterialTheme.typography.titleLarge
                )
            }
            
            // Switch camera (when video is on)
            if (isVideoEnabled) {
                FloatingActionButton(
                    onClick = {
                        scope.launch {
                            videoManager.switchCamera()
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ) {
                    Text(
                        text = "🔄",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
            
            // Leave meeting
            FloatingActionButton(
                onClick = onLeaveRoom,
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError
            ) {
                Text(
                    text = "📞",
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
        
        // Control labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                text = if (isMicEnabled) "Mute" else "Unmute",
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = if (isVideoEnabled) "Camera Off" else "Camera On",
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            if (isVideoEnabled) {
                Text(
                    text = "Switch",
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
            Text(
                text = "Leave",
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
        }
    }
} 