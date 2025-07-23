package com.johnlai.twiliovideo

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.johnlai.twiliovideo.domain.video.*
import kotlinx.coroutines.launch

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
    val scope = rememberCoroutineScope()
    
    var roomName by remember { mutableStateOf("") }
    var connectionStatus by remember { mutableStateOf("Disconnected") }
    
    // Observe connection state
    val connectionState by videoManager.connectionState.collectAsStateWithLifecycle(
        initialValue = VideoConnectionState.Disconnected
    )
    
    LaunchedEffect(connectionState) {
        connectionStatus = when (val state = connectionState) {
            is VideoConnectionState.Disconnected -> "Disconnected"
            is VideoConnectionState.Connecting -> "Connecting..."
            is VideoConnectionState.Connected -> "Connected to ${state.room.name}"
            is VideoConnectionState.Reconnecting -> "Reconnecting..."
            is VideoConnectionState.Failed -> "Failed: ${state.error}"
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "🎥 Twilio Video KMP Demo",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Text(
            text = "Status: $connectionStatus",
            style = MaterialTheme.typography.bodyLarge,
            color = when (connectionState) {
                is VideoConnectionState.Connected -> MaterialTheme.colorScheme.primary
                is VideoConnectionState.Failed -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.onSurface
            }
        )
        
        OutlinedTextField(
            value = roomName,
            onValueChange = { roomName = it },
            label = { Text("Room Name") },
            placeholder = { Text("Enter room name or leave empty for random") }
        )
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    scope.launch {
                        val finalRoomName = roomName.ifEmpty { 
                            // Generate random room name like our API expects
                            (1..10).map { "abcdefghijklmnopqrstuvwxyz0123456789".random() }
                                .joinToString("")
                        }
                        videoManager.connect("", finalRoomName)
                    }
                },
                enabled = connectionState is VideoConnectionState.Disconnected
            ) {
                Text("Connect")
            }
            
            Button(
                onClick = {
                    scope.launch {
                        videoManager.disconnect()
                    }
                },
                enabled = connectionState !is VideoConnectionState.Disconnected
            ) {
                Text("Disconnect")
            }
        }
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    scope.launch {
                        videoManager.enableCamera(true)
                    }
                }
            ) {
                Text("📹 Camera On")
            }
            
            Button(
                onClick = {
                    scope.launch {
                        videoManager.enableCamera(false)
                    }
                }
            ) {
                Text("📹 Camera Off")
            }
        }
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    scope.launch {
                        videoManager.enableMicrophone(true)
                    }
                }
            ) {
                Text("🎤 Mic On")
            }
            
            Button(
                onClick = {
                    scope.launch {
                        videoManager.enableMicrophone(false)
                    }
                }
            ) {
                Text("🎤 Mic Off")
            }
        }
        
        Button(
            onClick = {
                scope.launch {
                    videoManager.switchCamera()
                }
            }
        ) {
            Text("🔄 Switch Camera")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "✅ Using real Twilio Video SDK\n" +
                   "🔗 Your API: ${VideoConfig.twilioTokenUrl}\n" +
                   "👤 User Identity: ${VideoConfig.testUserIdentity}",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(8.dp)
        )
    }
}