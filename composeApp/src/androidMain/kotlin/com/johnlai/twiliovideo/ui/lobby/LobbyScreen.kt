package com.johnlai.twiliovideo.ui.lobby

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import com.johnlai.twiliovideo.domain.video.*

@Composable
fun LobbyScreen(
    videoManager: TwilioVideoManager,
    connectionState: VideoConnectionState
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var roomName by remember { mutableStateOf("") }
    var connectionStatus by remember { mutableStateOf("Disconnected") }
    var permissionsGranted by remember { mutableStateOf(false) }
    
    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissionsGranted = permissions[Manifest.permission.CAMERA] == true && 
                           permissions[Manifest.permission.RECORD_AUDIO] == true
    }
    
    // Check permissions on start
    LaunchedEffect(Unit) {
        val cameraPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        val audioPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
        
        if (cameraPermission == PackageManager.PERMISSION_GRANTED && 
            audioPermission == PackageManager.PERMISSION_GRANTED) {
            permissionsGranted = true
        } else {
            permissionLauncher.launch(arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ))
        }
    }
    
    LaunchedEffect(connectionState) {
        connectionStatus = when (val state = connectionState) {
            is VideoConnectionState.Disconnected -> "Disconnected"
            is VideoConnectionState.Connecting -> "Connecting..."
            is VideoConnectionState.Connected -> "Connected to ${state.room.name}"
            is VideoConnectionState.Reconnecting -> "Reconnecting..."
            is VideoConnectionState.Failed -> "Failed: ${state.error}"
        }
    }
    
    // Use safe area padding
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "🎥 Twilio Video KMP Demo",
            style = MaterialTheme.typography.headlineMedium
        )
        
        // Permissions status
        if (!permissionsGranted) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Text(
                    text = "⚠️ Camera and microphone permissions required",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        
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
            placeholder = { Text("Enter room name or leave empty for random") },
            modifier = Modifier.fillMaxWidth()
        )
        
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
            enabled = connectionState is VideoConnectionState.Disconnected && permissionsGranted,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("🚀 Join Meeting")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "✅ Using real Twilio Video SDK\n" +
                   "🔗 Your API: ${VideoConfig.twilioTokenUrl}\n" +
                   "👤 User Identity: ${VideoConfig.testUserIdentity}",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(8.dp),
            textAlign = TextAlign.Center
        )
    }
} 