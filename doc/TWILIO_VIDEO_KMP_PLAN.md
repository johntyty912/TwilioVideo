# Twilio Video SDK: Kotlin Multiplatform Integration Plan

## Executive Summary

This document outlines the integration plan for Twilio Video SDK with Kotlin Multiplatform (KMP) for the Remote TemiScript 2 application. The goal is to create a shared interface for video calling functionality while leveraging native Twilio SDKs for optimal performance.

**Estimated Timeline: 6-8 weeks**  
**Approach: Expect/Actual pattern with native SDK wrappers**  
**Risk Level: Medium-High** (due to platform-specific complexity)

---

## Current State Analysis

### Flutter Implementation

The current Flutter app uses Twilio Video through the `programmable_video` package located in:

- `packages/programmable_video/`
- Native Android and iOS implementations
- Custom video UI components
- Connection management and room handling

### Key Features to Preserve

- [ ] Video room joining and leaving
- [ ] Camera enable/disable
- [ ] Microphone mute/unmute
- [ ] Remote participant management
- [ ] Screen sharing capabilities
- [ ] Network quality monitoring
- [ ] Audio/video track management
- [ ] Connection state handling

---

## Architecture Design

### 1. Shared Interface (Common Module)

```kotlin
// domain/video/TwilioVideoManager.kt
interface TwilioVideoManager {
    val connectionState: Flow<VideoConnectionState>
    val participants: Flow<List<VideoParticipant>>
    val localVideoTrack: Flow<VideoTrack?>
    val networkQuality: Flow<NetworkQuality>

    suspend fun connect(accessToken: String, roomName: String): Result<VideoRoom>
    suspend fun disconnect()

    suspend fun enableCamera(enable: Boolean): Result<Unit>
    suspend fun enableMicrophone(enable: Boolean): Result<Unit>
    suspend fun switchCamera(): Result<Unit>

    suspend fun startScreenShare(): Result<Unit>
    suspend fun stopScreenShare(): Result<Unit>
}

// domain/video/VideoModels.kt
data class VideoRoom(
    val name: String,
    val sid: String,
    val participants: List<VideoParticipant>
)

data class VideoParticipant(
    val identity: String,
    val sid: String,
    val isConnected: Boolean,
    val videoTracks: List<VideoTrack>,
    val audioTracks: List<AudioTrack>
)

sealed class VideoConnectionState {
    object Disconnected : VideoConnectionState()
    object Connecting : VideoConnectionState()
    data class Connected(val room: VideoRoom) : VideoConnectionState()
    data class Failed(val error: VideoError) : VideoConnectionState()
    object Reconnecting : VideoConnectionState()
}

data class VideoTrack(
    val sid: String,
    val name: String,
    val isEnabled: Boolean,
    val participantSid: String
)

data class NetworkQuality(
    val level: Int, // 0-5
    val local: NetworkQualityStats,
    val remote: Map<String, NetworkQualityStats>
)
```

### 2. Platform Implementations

#### Android Implementation

```kotlin
// androidMain/video/AndroidTwilioVideoManager.kt
actual class TwilioVideoManagerImpl : TwilioVideoManager {
    private val room = MutableStateFlow<Room?>(null)
    private val localVideoTrack = MutableStateFlow<LocalVideoTrack?>(null)
    private val cameraCapture = MutableStateFlow<CameraCapturer?>(null)

    actual override val connectionState: Flow<VideoConnectionState> =
        room.map { room ->
            when {
                room == null -> VideoConnectionState.Disconnected
                room.state == Room.State.CONNECTED ->
                    VideoConnectionState.Connected(room.toVideoRoom())
                room.state == Room.State.CONNECTING ->
                    VideoConnectionState.Connecting
                room.state == Room.State.RECONNECTING ->
                    VideoConnectionState.Reconnecting
                else -> VideoConnectionState.Failed(VideoError.ConnectionFailed)
            }
        }

    actual override suspend fun connect(
        accessToken: String,
        roomName: String
    ): Result<VideoRoom> = withContext(Dispatchers.IO) {
        try {
            val connectOptions = ConnectOptions.Builder(accessToken)
                .roomName(roomName)
                .localVideoTracks(listOf(localVideoTrack.value))
                .build()

            val connectedRoom = Video.connect(context, connectOptions, roomListener)
            room.value = connectedRoom
            Result.success(connectedRoom.toVideoRoom())
        } catch (e: Exception) {
            Result.failure(VideoError.ConnectionFailed.toException())
        }
    }

    private val roomListener = object : Room.Listener {
        override fun onConnected(room: Room) {
            this@AndroidTwilioVideoManager.room.value = room
        }

        override fun onParticipantConnected(room: Room, participant: RemoteParticipant) {
            // Handle participant connection
        }

        override fun onDisconnected(room: Room, twilioException: TwilioException?) {
            this@AndroidTwilioVideoManager.room.value = null
        }
    }
}
```

#### iOS Implementation

```kotlin
// iosMain/video/IosTwilioVideoManager.kt
actual class TwilioVideoManagerImpl : TwilioVideoManager {
    private val room = MutableStateFlow<TVIRoom?>(null)
    private val localVideoTrack = MutableStateFlow<TVILocalVideoTrack?>(null)
    private val camera = MutableStateFlow<TVICameraCapturer?>(null)

    actual override suspend fun connect(
        accessToken: String,
        roomName: String
    ): Result<VideoRoom> = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine { continuation ->
            val connectOptions = TVIConnectOptions { builder in
                builder.token = accessToken
                builder.roomName = roomName
                if let videoTrack = localVideoTrack.value {
                    builder.videoTracks = [videoTrack]
                }
            }

            let connectedRoom = TwilioVideo.connect(
                with: connectOptions,
                delegate: self
            )

            room.value = connectedRoom
            continuation.resume(Result.success(connectedRoom.toVideoRoom()))
        }
    }
}

// iOS delegate methods
extension IosTwilioVideoManager: TVIRoomDelegate {
    func roomDidConnect(room: TVIRoom) {
        self.room.value = room
    }

    func room(_ room: TVIRoom, participantDidConnect participant: TVIRemoteParticipant) {
        // Handle participant connection
    }

    func roomDidDisconnect(room: TVIRoom, error: Error?) {
        self.room.value = nil
    }
}
```

---

## Implementation Phases

### Phase 1: Core Architecture Setup (Week 1-2) ✅ COMPLETED

#### Tasks

- [x] Define shared interfaces and data models
- [x] Set up expect/actual declarations
- [x] Create basic project structure
- [x] Set up platform-specific dependencies

#### Deliverables ✅ COMPLETED

- ✅ **Shared video interfaces** - Complete TwilioVideoManager interface with all required methods
- ✅ **Platform-specific stub implementations** - Android and iOS actual implementations with working stubs
- ✅ **Build configuration** - Updated build.gradle.kts with all necessary dependencies
- ✅ **Dependency setup** - Configured Kotlin Coroutines, Twilio Video SDK dependencies
- ✅ **Comprehensive unit tests** - Created 13 unit tests covering all core functionality
- ✅ **Core data models** - VideoRoom, VideoParticipant, VideoTrack, NetworkQuality, VideoError, etc.

#### Dependencies Setup

```kotlin
// shared/build.gradle.kts
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
            }
        }

        val androidMain by getting {
            dependencies {
                implementation("com.twilio:video-android:7.6.0")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
            }
        }

        val iosMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
            }
        }
    }
}

// iOS CocoaPods
pod("TwilioVideo", "~> 5.8")
```

### Phase 2: Android Implementation (Week 2-3)

#### Tasks

- [ ] Implement Android Twilio Video SDK integration
- [ ] Create camera and microphone management
- [ ] Implement room connection and participant handling
- [ ] Add network quality monitoring
- [ ] Create video track management

#### Key Components

- **Room Management**: Connection, disconnection, reconnection
- **Media Tracks**: Video and audio track handling
- **Camera Control**: Front/back camera switching
- [ ] Permissions: Camera and microphone permissions

#### Android-Specific Considerations

```kotlin
// Permission handling
private fun checkPermissions(): Boolean {
    return ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
           PackageManager.PERMISSION_GRANTED &&
           ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
           PackageManager.PERMISSION_GRANTED
}

// Camera initialization
private fun setupLocalVideo() {
    val cameraCapture = CameraCapturer(
        context,
        CameraCapturer.CameraSource.FRONT_CAMERA
    )
    localVideoTrack = LocalVideoTrack.create(context, true, cameraCapture)
}
```

### Phase 3: iOS Implementation (Week 3-4)

#### Tasks

- [ ] Implement iOS Twilio Video SDK integration
- [ ] Create Swift/Objective-C interop layer
- [ ] Implement camera and microphone management
- [ ] Add iOS-specific UI considerations
- [ ] Handle iOS background/foreground states

#### iOS-Specific Considerations

```swift
// TVIVideoView integration for rendering
class VideoViewWrapper: UIViewRepresentable {
    let videoTrack: TVIVideoTrack?

    func makeUIView(context: Context) -> TVIVideoView {
        let videoView = TVIVideoView()
        videoTrack?.addRenderer(videoView)
        return videoView
    }
}

// Background handling
func applicationDidEnterBackground() {
    localVideoTrack?.isEnabled = false
}

func applicationWillEnterForeground() {
    localVideoTrack?.isEnabled = true
}
```

### Phase 4: Advanced Features (Week 4-5)

#### Tasks

- [ ] Implement screen sharing functionality
- [ ] Add bandwidth adaptation
- [ ] Implement recording capabilities
- [ ] Add advanced audio features (noise cancellation, echo cancellation)
- [ ] Network quality optimization

#### Screen Sharing Implementation

```kotlin
// Android screen sharing
expect class ScreenCapturer {
    fun startCapture()
    fun stopCapture()
    val screenTrack: Flow<VideoTrack?>
}

// Android implementation
actual class ScreenCapturer(
    private val mediaProjectionManager: MediaProjectionManager,
    private val activity: Activity
) {
    actual fun startCapture() {
        val intent = mediaProjectionManager.createScreenCaptureIntent()
        activity.startActivityForResult(intent, SCREEN_CAPTURE_REQUEST)
    }
}
```

### Phase 5: UI Integration & Testing (Week 5-6)

#### Tasks

- [ ] Create Compose Multiplatform video UI components
- [ ] Implement video view containers
- [ ] Add UI controls (mute, camera toggle, hang up)
- [ ] Implement participant grid layout
- [ ] Add connection status indicators

#### UI Components

```kotlin
@Composable
expect fun VideoView(
    videoTrack: VideoTrack?,
    modifier: Modifier = Modifier
)

@Composable
fun VideoCallScreen(
    viewModel: VideoCallViewModel
) {
    val connectionState by viewModel.connectionState.collectAsState()
    val participants by viewModel.participants.collectAsState()

    Column {
        // Local video preview
        VideoView(
            videoTrack = viewModel.localVideoTrack.collectAsState().value,
            modifier = Modifier.size(120.dp)
        )

        // Remote participants grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2)
        ) {
            items(participants) { participant ->
                ParticipantVideoView(participant = participant)
            }
        }

        // Control buttons
        VideoControlButtons(
            onToggleCamera = viewModel::toggleCamera,
            onToggleMicrophone = viewModel::toggleMicrophone,
            onHangUp = viewModel::disconnect
        )
    }
}
```

### Phase 6: Integration & Optimization (Week 6-8)

#### Tasks

- [ ] Integrate with main KMP application
- [ ] Performance optimization and memory management
- [ ] Comprehensive testing across devices
- [ ] Documentation and code review
- [ ] Production readiness assessment

---

## Testing Strategy

### Unit Testing

```kotlin
class TwilioVideoManagerTest {
    @Test
    fun `should connect to room successfully`() = runTest {
        val manager = TwilioVideoManagerImpl()
        val result = manager.connect("fake-token", "test-room")

        assertTrue(result.isSuccess)
        assertEquals("test-room", result.getOrNull()?.name)
    }

    @Test
    fun `should handle connection failure gracefully`() = runTest {
        val manager = TwilioVideoManagerImpl()
        val result = manager.connect("invalid-token", "test-room")

        assertTrue(result.isFailure)
    }
}
```

### Integration Testing

- Real device testing with actual Twilio accounts
- Network condition simulation (poor connectivity, packet loss)
- Platform-specific feature testing
- Memory leak detection

### Performance Testing

- Video quality assessment
- Battery usage monitoring
- CPU and memory usage profiling
- Network bandwidth optimization

---

## Risk Assessment & Mitigation

| Risk                          | Probability | Impact | Mitigation Strategy                                |
| ----------------------------- | ----------- | ------ | -------------------------------------------------- |
| **Platform API Differences**  | High        | Medium | Thorough testing and platform-specific handling    |
| **Twilio SDK Updates**        | Medium      | High   | Pin SDK versions, comprehensive regression testing |
| **Performance Issues**        | Medium      | High   | Continuous profiling and optimization              |
| **iOS/Android Fragmentation** | High        | Medium | Device-specific testing matrix                     |
| **Network Connectivity**      | Medium      | Medium | Robust reconnection logic and fallback mechanisms  |

---

## Success Metrics

### Technical Metrics

- [ ] **API Compatibility**: 100% feature parity with Flutter implementation
- [ ] **Performance**: No degradation in video quality or connection stability
- [ ] **Memory Usage**: <50MB additional memory footprint
- [ ] **Battery Usage**: Comparable to native implementations

### Quality Metrics

- [ ] **Test Coverage**: >80% code coverage
- [ ] **Device Support**: Support for Android 7+ and iOS 12+
- [ ] **Connection Success Rate**: >95% successful connections
- [ ] **Crash Rate**: <0.1% crash rate in video calls

---

## Deliverables

### Code Deliverables

1. **Shared Video Interface**: Common API for video functionality
2. **Android Implementation**: Complete Android Twilio Video integration
3. **iOS Implementation**: Complete iOS Twilio Video integration
4. **UI Components**: Compose Multiplatform video UI components
5. **Test Suite**: Comprehensive unit and integration tests

### Documentation

1. **Integration Guide**: How to integrate the video module
2. **API Documentation**: Complete API reference
3. **Platform Notes**: Platform-specific implementation details
4. **Troubleshooting Guide**: Common issues and solutions

### Build Artifacts

1. **KMP Library**: Compiled multiplatform library
2. **Sample App**: Demo application showing integration
3. **CI/CD Pipeline**: Automated testing and building

---

## Dependencies & Prerequisites

### Technical Prerequisites

- Kotlin Multiplatform 1.9.20+
- Android SDK 24+ (Android 7.0+)
- iOS 12.0+ deployment target
- Twilio Video Android SDK 7.6.0+
- Twilio Video iOS SDK 5.8+

### Business Prerequisites

- Active Twilio account with video capabilities
- Video API access tokens generation system
- Testing devices (Android and iOS)
- Network testing infrastructure

---

## Next Steps

### Immediate Actions (Week 1)

1. **Environment Setup**: Configure development environment with Twilio SDKs
2. **Prototype Development**: Create minimal working prototype
3. **Architecture Validation**: Validate expect/actual pattern approach
4. **Team Alignment**: Review plan with development team

### Decision Points

1. **Week 2**: Architecture validation and technical feasibility
2. **Week 4**: Platform implementation completeness
3. **Week 6**: Performance and quality assessment
4. **Week 8**: Production readiness and integration approval

---

## Conclusion

The Twilio Video SDK integration for KMP is **technically achievable** using the expect/actual pattern with native SDK wrappers. While complex, this approach provides:

- **Native Performance**: Leverages optimized platform-specific SDKs
- **Shared Interface**: Unified API for both platforms
- **Maintainability**: Clear separation of concerns
- **Future-proofing**: Easy to update and extend

**Recommendation**: Proceed with implementation as a separate module that can be integrated into the main KMP migration project.
