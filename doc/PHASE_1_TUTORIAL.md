# Phase 1 Tutorial: Core Architecture Setup for Twilio Video KMP

## 🎯 Overview

This tutorial guides you through setting up the core architecture for Twilio Video SDK integration in Kotlin Multiplatform (KMP). You'll learn how to create shared interfaces, implement the expect/actual pattern, and establish a solid foundation for cross-platform video calling functionality.

## 📚 What You'll Learn

- How to design shared interfaces for video functionality
- Implementing expect/actual pattern for platform-specific code
- Creating robust data models with sealed classes
- Setting up comprehensive unit tests
- Configuring KMP build files for video SDKs

## 🛠️ Prerequisites

- Kotlin Multiplatform project setup
- Basic understanding of KMP concepts
- Android Studio or IntelliJ IDEA
- Gradle knowledge

## 📋 Step 1: Project Structure Planning

### 1.1 Define the Architecture

Our Phase 1 architecture follows these principles:
- **Shared Interface**: Common API for all platforms
- **Data Models**: Shared data classes and sealed classes
- **Expect/Actual**: Platform-specific implementations
- **Result Handling**: Type-safe error handling

### 1.2 Create Directory Structure

```
shared/src/
├── commonMain/kotlin/com/johnlai/twiliovideo/
│   └── domain/video/
│       ├── VideoModels.kt          # Core data models
│       ├── TwilioVideoManager.kt   # Main interface
│       └── TwilioVideoManagerImpl.kt # Expected implementation
├── androidMain/kotlin/com/johnlai/twiliovideo/
│   └── domain/video/
│       └── TwilioVideoManagerImpl.android.kt # Android actual
├── iosMain/kotlin/com/johnlai/twiliovideo/
│   └── domain/video/
│       └── TwilioVideoManagerImpl.ios.kt # iOS actual
└── commonTest/kotlin/com/johnlai/twiliovideo/
    └── domain/video/
        ├── TwilioVideoManagerTest.kt  # Interface tests
        └── VideoModelsTest.kt         # Data model tests
```

## 📋 Step 2: Core Data Models

### 2.1 Create VideoModels.kt

First, define the core data structures that will be shared across platforms:

```kotlin
// shared/src/commonMain/kotlin/com/johnlai/twiliovideo/domain/video/VideoModels.kt
package com.johnlai.twiliovideo.domain.video

import kotlinx.coroutines.flow.Flow

// Core video models for cross-platform video calling

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

data class AudioTrack(
    val sid: String,
    val name: String,
    val isEnabled: Boolean,
    val participantSid: String
)

data class NetworkQuality(
    val level: Int, // 0-5 (0 = unknown, 1 = poor, 5 = excellent)
    val local: NetworkQualityStats,
    val remote: Map<String, NetworkQualityStats>
)

data class NetworkQualityStats(
    val audio: NetworkQualityLevel,
    val video: NetworkQualityLevel
)

enum class NetworkQualityLevel(val value: Int) {
    UNKNOWN(0),
    POOR(1),
    LOW(2),
    MODERATE(3),
    GOOD(4),
    EXCELLENT(5)
}

sealed class VideoError(val message: String) {
    object ConnectionFailed : VideoError("Failed to connect to video room")
    object Disconnected : VideoError("Disconnected from video room")
    object InvalidToken : VideoError("Invalid access token")
    object PermissionDenied : VideoError("Camera or microphone permission denied")
    object NetworkError : VideoError("Network connection error")
    data class UnknownError(val details: String) : VideoError("Unknown error: $details")
    
    fun toException(): Exception = Exception(message)
}

// Result wrapper for async operations
sealed class VideoResult<T> {
    data class Success<T>(val data: T) : VideoResult<T>()
    data class Error<T>(val error: VideoError) : VideoResult<T>()
    
    inline fun onSuccess(action: (T) -> Unit): VideoResult<T> {
        if (this is Success) action(data)
        return this
    }
    
    inline fun onError(action: (VideoError) -> Unit): VideoResult<T> {
        if (this is Error) action(error)
        return this
    }
}
```

### 2.2 Key Design Decisions

- **Sealed Classes**: Used for type-safe state management
- **Data Classes**: Immutable data structures for thread safety  
- **Result Wrapper**: Type-safe error handling without exceptions
- **Flow Integration**: Ready for reactive state management

## 📋 Step 3: Main Interface Definition

### 3.1 Create TwilioVideoManager.kt

Define the main interface that will be implemented on each platform:

```kotlin
// shared/src/commonMain/kotlin/com/johnlai/twiliovideo/domain/video/TwilioVideoManager.kt
package com.johnlai.twiliovideo.domain.video

import kotlinx.coroutines.flow.Flow

/**
 * Main interface for Twilio Video SDK integration
 * This interface will be implemented differently on each platform using expect/actual pattern
 */
interface TwilioVideoManager {
    
    // Observable state flows
    val connectionState: Flow<VideoConnectionState>
    val participants: Flow<List<VideoParticipant>>
    val localVideoTrack: Flow<VideoTrack?>
    val networkQuality: Flow<NetworkQuality>
    
    // Room connection management
    suspend fun connect(accessToken: String, roomName: String): VideoResult<VideoRoom>
    suspend fun disconnect(): VideoResult<Unit>
    
    // Media control
    suspend fun enableCamera(enable: Boolean): VideoResult<Unit>
    suspend fun enableMicrophone(enable: Boolean): VideoResult<Unit>
    suspend fun switchCamera(): VideoResult<Unit>
    
    // Screen sharing
    suspend fun startScreenShare(): VideoResult<Unit>
    suspend fun stopScreenShare(): VideoResult<Unit>
    
    // Utility methods
    suspend fun getAvailableCameras(): List<CameraInfo>
    suspend fun getCurrentCameraInfo(): CameraInfo?
    
    // Cleanup
    fun release()
}

/**
 * Camera information
 */
data class CameraInfo(
    val id: String,
    val name: String,
    val isFrontFacing: Boolean,
    val isBackFacing: Boolean
)

/**
 * Factory interface for creating TwilioVideoManager instances
 */
expect object TwilioVideoManagerFactory {
    fun create(): TwilioVideoManager
}
```

### 3.2 Interface Design Principles

- **Suspend Functions**: All async operations use coroutines
- **Flow Properties**: Reactive state management
- **Result Types**: Type-safe error handling
- **Factory Pattern**: Platform-specific instance creation

## 📋 Step 4: Expect/Actual Pattern Setup

### 4.1 Create Expected Class

```kotlin
// shared/src/commonMain/kotlin/com/johnlai/twiliovideo/domain/video/TwilioVideoManagerImpl.kt
package com.johnlai.twiliovideo.domain.video

import kotlinx.coroutines.flow.Flow

/**
 * Expected class that will be implemented on each platform
 * Android and iOS will provide their own actual implementations
 */
expect class TwilioVideoManagerImpl() : TwilioVideoManager {
    
    override val connectionState: Flow<VideoConnectionState>
    override val participants: Flow<List<VideoParticipant>>
    override val localVideoTrack: Flow<VideoTrack?>
    override val networkQuality: Flow<NetworkQuality>
    
    override suspend fun connect(accessToken: String, roomName: String): VideoResult<VideoRoom>
    override suspend fun disconnect(): VideoResult<Unit>
    
    override suspend fun enableCamera(enable: Boolean): VideoResult<Unit>
    override suspend fun enableMicrophone(enable: Boolean): VideoResult<Unit>
    override suspend fun switchCamera(): VideoResult<Unit>
    
    override suspend fun startScreenShare(): VideoResult<Unit>
    override suspend fun stopScreenShare(): VideoResult<Unit>
    
    override suspend fun getAvailableCameras(): List<CameraInfo>
    override suspend fun getCurrentCameraInfo(): CameraInfo?
    
    override fun release()
}
```

## 📋 Step 5: Platform-Specific Implementations

### 5.1 Android Implementation

```kotlin
// shared/src/androidMain/kotlin/com/johnlai/twiliovideo/domain/video/TwilioVideoManagerImpl.android.kt
package com.johnlai.twiliovideo.domain.video

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Android implementation of TwilioVideoManager
 * This is a stub implementation for Phase 1 - will be fully implemented in Phase 2
 */
actual class TwilioVideoManagerImpl : TwilioVideoManager {
    
    private val _connectionState = MutableStateFlow<VideoConnectionState>(VideoConnectionState.Disconnected)
    private val _participants = MutableStateFlow<List<VideoParticipant>>(emptyList())
    private val _localVideoTrack = MutableStateFlow<VideoTrack?>(null)
    private val _networkQuality = MutableStateFlow(
        NetworkQuality(
            level = 0,
            local = NetworkQualityStats(
                audio = NetworkQualityLevel.UNKNOWN,
                video = NetworkQualityLevel.UNKNOWN
            ),
            remote = emptyMap()
        )
    )
    
    actual override val connectionState: Flow<VideoConnectionState> = _connectionState.asStateFlow()
    actual override val participants: Flow<List<VideoParticipant>> = _participants.asStateFlow()
    actual override val localVideoTrack: Flow<VideoTrack?> = _localVideoTrack.asStateFlow()
    actual override val networkQuality: Flow<NetworkQuality> = _networkQuality.asStateFlow()
    
    // Implementation methods...
    actual override suspend fun connect(accessToken: String, roomName: String): VideoResult<VideoRoom> {
        _connectionState.value = VideoConnectionState.Connecting
        val room = VideoRoom(name = roomName, sid = "stub-room-sid", participants = emptyList())
        _connectionState.value = VideoConnectionState.Connected(room)
        return VideoResult.Success(room)
    }
    
    // ... other methods with stub implementations
}

actual object TwilioVideoManagerFactory {
    actual fun create(): TwilioVideoManager {
        return TwilioVideoManagerImpl()
    }
}
```

### 5.2 iOS Implementation

Similar structure for iOS with platform-specific considerations:

```kotlin
// shared/src/iosMain/kotlin/com/johnlai/twiliovideo/domain/video/TwilioVideoManagerImpl.ios.kt
// Similar implementation with iOS-specific stub code
```

## 📋 Step 6: Build Configuration

### 6.1 Update shared/build.gradle.kts

```kotlin
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    val iosX64 = iosX64()
    val iosArm64 = iosArm64()
    val iosSimulatorArm64 = iosSimulatorArm64()
    
    listOf(iosX64, iosArm64, iosSimulatorArm64).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")
            }
        }
        
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
            }
        }
        
        val androidMain by getting {
            dependencies {
                implementation("com.twilio:video-android:7.6.0")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
                implementation("androidx.core:core-ktx:1.12.0")
                implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
            }
        }
        
        val iosMain by creating {
            dependsOn(commonMain)
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
            }
        }
        
        val iosX64Main by getting { dependsOn(iosMain) }
        val iosArm64Main by getting { dependsOn(iosMain) }
        val iosSimulatorArm64Main by getting { dependsOn(iosMain) }
        
        val androidUnitTest by getting {
            dependencies {
                implementation("junit:junit:4.13.2")
                implementation("org.mockito:mockito-core:5.7.0")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
            }
        }
    }
}
```

### 6.2 Update gradle/libs.versions.toml

Add the kotlinCocoapods plugin for future iOS integration:

```toml
[plugins]
androidApplication = { id = "com.android.application", version.ref = "agp" }
androidLibrary = { id = "com.android.library", version.ref = "agp" }
composeMultiplatform = { id = "org.jetbrains.compose", version.ref = "composeMultiplatform" }
composeCompiler = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
kotlinMultiplatform = { id = "org.jetbrains.kotlin.multiplatform", version.ref = "kotlin" }
kotlinCocoapods = { id = "org.jetbrains.kotlin.native.cocoapods", version.ref = "kotlin" }
```

## 📋 Step 7: Comprehensive Unit Testing

### 7.1 Create TwilioVideoManagerTest.kt

```kotlin
// shared/src/commonTest/kotlin/com/johnlai/twiliovideo/domain/video/TwilioVideoManagerTest.kt
package com.johnlai.twiliovideo.domain.video

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class TwilioVideoManagerTest {
    
    @Test
    fun `should connect to room successfully`() = runTest {
        val manager = TwilioVideoManagerFactory.create()
        val result = manager.connect("fake-token", "test-room")
        
        assertIs<VideoResult.Success<VideoRoom>>(result)
        assertEquals("test-room", result.data.name)
    }
    
    @Test
    fun `should handle connection state changes`() = runTest {
        val manager = TwilioVideoManagerFactory.create()
        
        val initialState = manager.connectionState.first()
        assertIs<VideoConnectionState.Disconnected>(initialState)
        
        manager.connect("fake-token", "test-room")
        val connectedState = manager.connectionState.first()
        assertIs<VideoConnectionState.Connected>(connectedState)
        assertEquals("test-room", connectedState.room.name)
    }
    
    // ... 11 more comprehensive tests
}
```

### 7.2 Create VideoModelsTest.kt

Test all data models thoroughly:

```kotlin
// shared/src/commonTest/kotlin/com/johnlai/twiliovideo/domain/video/VideoModelsTest.kt
// Comprehensive tests for all data models, sealed classes, and error handling
```

## 📋 Step 8: Building and Testing

### 8.1 Build the Project

```bash
./gradlew shared:build
```

Expected output: ✅ BUILD SUCCESSFUL

### 8.2 Run Tests

```bash
./gradlew shared:test
```

Expected output: ✅ All tests pass

### 8.3 Clean Build

```bash
./gradlew clean build
```

## 📋 Step 9: Verification Checklist

### ✅ Architecture Verification

- [ ] Shared interfaces compile successfully
- [ ] Expect/actual pattern works correctly
- [ ] All data models are properly structured
- [ ] Platform-specific implementations exist
- [ ] Factory pattern creates instances correctly

### ✅ Testing Verification

- [ ] All 13 unit tests pass
- [ ] Test coverage includes all main methods
- [ ] Data model tests verify all properties
- [ ] Error handling tests work correctly
- [ ] Flow state management tests pass

### ✅ Build Verification

- [ ] Android target builds successfully
- [ ] iOS targets (x64, arm64, simulatorArm64) build
- [ ] No compilation warnings about missing implementations
- [ ] Dependencies resolve correctly

## 🎯 Summary

Congratulations! You've successfully completed Phase 1 of the Twilio Video KMP integration. Here's what you've accomplished:

### ✅ What We Built

1. **📋 Core Architecture**: Solid foundation with expect/actual pattern
2. **🏗️ Data Models**: Comprehensive data structures with sealed classes
3. **🔄 Interface Design**: Clean API with coroutines and Flow
4. **🧪 Test Coverage**: 13 unit tests covering all functionality  
5. **⚙️ Build Configuration**: Proper KMP setup with all dependencies

### 🚀 Next Steps

**Phase 2: Android Implementation**
- Integrate actual Twilio Video Android SDK
- Implement real camera and microphone management
- Add permission handling
- Create room connection logic

### 📚 Key Learnings

- **Expect/Actual Pattern**: How to structure platform-specific code
- **Sealed Classes**: Type-safe state management across platforms
- **Flow Integration**: Reactive programming in KMP
- **Result Handling**: Type-safe error management
- **Test Strategy**: Comprehensive testing for shared code

The foundation is now solid and ready for real Twilio SDK integration! 🎉

## 📖 Additional Resources

- [Kotlin Multiplatform Documentation](https://kotlinlang.org/docs/multiplatform.html)
- [Twilio Video Android SDK Documentation](https://www.twilio.com/docs/video/android)
- [Twilio Video iOS SDK Documentation](https://www.twilio.com/docs/video/ios)
- [Coroutines and Flow Guide](https://kotlinlang.org/docs/coroutines-guide.html) 