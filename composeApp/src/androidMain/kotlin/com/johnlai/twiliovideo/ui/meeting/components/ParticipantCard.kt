package com.johnlai.twiliovideo.ui.meeting.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.johnlai.twiliovideo.domain.video.VideoParticipant
import com.twilio.video.RemoteVideoTrack
import com.johnlai.twiliovideo.ui.meeting.components.TwilioVideoView

@Composable
fun ParticipantCard(participant: VideoParticipant) {
    // Try to get the first enabled remote video track
    val remoteVideoTrack = participant.videoTracks.firstOrNull { it.isEnabled && it.remoteVideoTrack is RemoteVideoTrack }?.remoteVideoTrack as? RemoteVideoTrack
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (remoteVideoTrack != null) {
                TwilioVideoView(
                    remoteVideoTrack = remoteVideoTrack,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Avatar placeholder
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
                            text = participant.identity.take(2).uppercase(),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = participant.identity,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                    Text(
                        text = "Connected",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
 