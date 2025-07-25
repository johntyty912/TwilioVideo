package com.johnlai.twiliovideo.ui.meeting

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import com.johnlai.twiliovideo.domain.video.*
import com.johnlai.twiliovideo.ui.meeting.components.ParticipantCard
import com.johnlai.twiliovideo.ui.meeting.components.MeetingControls
import com.johnlai.twiliovideo.ui.meeting.components.LocalVideoView

@Composable
fun MeetingRoomScreen(
    videoManager: TwilioVideoManager,
    room: VideoRoom,
    localUserIdentity: String
) {
    val scope = rememberCoroutineScope()
    val participants by videoManager.participants.collectAsStateWithLifecycle(
        initialValue = emptyList()
    )
    
    var isLocalCameraEnabled by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Room header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🎥 Meeting Room",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = room.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "${participants.size + 1} participant(s)", // +1 for local user
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Participants grid (including local video)
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Local video (always first)
            item {
                LocalVideoView(
                    videoManager = videoManager,
                    isVideoEnabled = isLocalCameraEnabled,
                    userIdentity = localUserIdentity
                )
            }
            
            // Remote participants
            items(participants) { participant ->
                ParticipantCard(participant = participant)
            }
            
            // Show message if no remote participants
            if (participants.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "👋",
                                    style = MaterialTheme.typography.displaySmall
                                )
                                Text(
                                    text = "Waiting for others",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Meeting controls
        MeetingControls(
            videoManager = videoManager,
            onLeaveRoom = {
                scope.launch {
                    videoManager.disconnect()
                }
            },
            onCameraStateChange = { enabled ->
                isLocalCameraEnabled = enabled
            }
        )
    }
} 