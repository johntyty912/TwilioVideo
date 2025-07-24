package com.johnlai.twiliovideo

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.johnlai.twiliovideo.domain.video.*
import com.johnlai.twiliovideo.ui.lobby.LobbyScreen
import com.johnlai.twiliovideo.ui.meeting.MeetingRoomScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    MaterialTheme {
        VideoCallDemo()
    }
}

@Composable
fun VideoCallDemo() {
    val context = LocalContext.current
    val videoManager = remember { TwilioVideoManagerFactory.create(context) }
    
    // Observe connection state
    val connectionState by videoManager.connectionState.collectAsStateWithLifecycle(
        initialValue = VideoConnectionState.Disconnected
    )
    
    // Add safe area handling
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
        ) {
            // Show different screens based on connection state
            when (val state = connectionState) {
                is VideoConnectionState.Connected -> {
                    MeetingRoomScreen(
                        videoManager = videoManager,
                        room = state.room
                    )
                }
                else -> {
                    LobbyScreen(
                        videoManager = videoManager,
                        connectionState = connectionState
                    )
                }
            }
        }
    }
}