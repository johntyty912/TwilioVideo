# Twilio Video KMP

**A Kotlin Multiplatform (KMP) project integrating Twilio Video SDK for Android and iOS.**

## 🎯 **Project Status: Phase 2 Complete!**

✅ **Real Android Twilio Video integration complete**  
🔄 **Ready for iOS implementation**  
📱 **Production-ready foundation established**

## 🚀 **What's Working Now**

### ✅ Real Twilio Video Features
- **Token Service Integration**: Uses your existing `https://api.robocore.ai/twilio/video_token` API
- **Camera Management**: Front/back camera switching, enable/disable
- **Audio Management**: Microphone control
- **Room Connection**: Real video room connection with participant management
- **Reactive State**: Kotlin Flow-based state management for UI

### ✅ Technical Foundation
- **Kotlin Multiplatform**: Shared code between Android and iOS
- **Real SDK Integration**: Actual Twilio Video Android SDK v7.6.0
- **HTTP Client**: Ktor-based API integration
- **Testing Environment**: Comprehensive test suite and automation
- **Development Environment**: Automated setup with mise

## 🛠️ **Quick Start**

### Prerequisites
```bash
# Install mise (if not already installed)
curl https://mise.run | sh

# Install project dependencies
mise install
```

### Phase 2 Environment Setup
```bash
# Set up Phase 2 testing environment
mise run phase2-setup

# Verify everything works
mise run phase2-test-check
```

### Build & Test
```bash
# Build the project
./gradlew build

# Run all tests (30 tests including TokenService tests)
./gradlew shared:test

# Build Android app
./gradlew composeApp:assembleDebug
```

## 📱 **How to Use the Video SDK**

### Android Integration Example

```kotlin
// Create video manager with Android context
val videoManager = TwilioVideoManagerFactory.create(context)

// Connect to a video room
val result = videoManager.connect(
    accessToken = "", // Token will be fetched automatically from your API
    roomName = "your-room-name"
)

// Observe connection state
videoManager.connectionState.collect { state ->
    when (state) {
        is VideoConnectionState.Connected -> {
            // Handle successful connection
            println("Connected to room: ${state.room.name}")
        }
        is VideoConnectionState.Failed -> {
            // Handle connection failure
            println("Connection failed: ${state.error}")
        }
        // ... other states
    }
}

// Manage camera and audio
videoManager.enableCamera(true)
videoManager.enableMicrophone(true)
videoManager.switchCamera()

// Clean up
videoManager.disconnect()
videoManager.release()
```

### Token Service Configuration

Your API endpoint is automatically used:
```json
POST https://api.robocore.ai/twilio/video_token
{
  "userIdentity": "user",
  "roomName": "a1b2c3d4e5"  // 10-character alphanumeric
}
```

## 🏗️ **Architecture**

```
shared/
├── commonMain/          # Shared interfaces and models
│   ├── TwilioVideoManager.kt    # Main interface
│   ├── VideoModels.kt           # Data models
│   ├── TokenService.kt          # API client
│   └── TwilioVideoManagerImpl.kt # Expect class
├── androidMain/         # ✅ REAL Android implementation
│   └── TwilioVideoManagerImpl.android.kt
└── iosMain/            # 🔄 iOS stubs (ready for implementation)
    └── TwilioVideoManagerImpl.ios.kt

composeApp/              # Demo Android app
iosApp/                  # Demo iOS app
```

## 🧪 **Testing**

### Test Suite (30 tests passing)
- **Unit Tests**: Data models, interfaces, and business logic
- **Integration Tests**: Real Android functionality with context
- **API Tests**: TokenService and HTTP client functionality
- **Utility Tests**: Room name generation and API parameter validation

### Run Tests
```bash
# All tests
./gradlew shared:test

# Android-specific tests (requires emulator)
./gradlew shared:connectedAndroidTest

# Verify environment
mise run phase2-test-check
```

## 📊 **Dependencies**

### Production Dependencies
```kotlin
// Twilio Video SDK
implementation("com.twilio:video-android:7.6.0")

// HTTP Client
implementation("io.ktor:ktor-client-android:2.3.7")
implementation("io.ktor:ktor-client-content-negotiation:2.3.7")
implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")

// Coroutines
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
```

## 📋 **Next Steps**

### Phase 3: iOS Implementation
```bash
# When ready to start iOS implementation
# 1. Add Twilio Video iOS SDK
# 2. Implement TwilioVideoManagerImpl.ios.kt
# 3. iOS-specific UI components
```

### Phase 4: UI Development
```bash
# Android UI with Compose
# iOS UI with SwiftUI
# Video calling interface
```

## 🛠️ **Development Environment**

### Mise Tasks
```bash
mise run verify-env          # Check all dependencies
mise run phase2-setup        # Set up Phase 2 environment
mise run phase2-test-check    # Verify Phase 2 setup
mise run android-sdk-check    # Check Android SDK
```

### Environment Files
- `.env.local` - Token service configuration (auto-generated)
- `mise.toml` - Development environment automation

## 📚 **Documentation**

- [`doc/TWILIO_VIDEO_KMP_PLAN.md`](doc/TWILIO_VIDEO_KMP_PLAN.md) - Complete project plan and progress
- [`doc/PHASE_1_TUTORIAL.md`](doc/PHASE_1_TUTORIAL.md) - Phase 1 architecture tutorial
- [`doc/PHASE_2_TESTING_SETUP.md`](doc/PHASE_2_TESTING_SETUP.md) - Phase 2 testing environment guide

## 🎯 **Key Achievements**

✅ **Real Twilio Integration**: Working Android implementation with actual SDK  
✅ **Your API Integration**: Using your existing token service  
✅ **Clean Architecture**: Kotlin Multiplatform with expect/actual pattern  
✅ **Reactive State**: Flow-based state management  
✅ **Comprehensive Testing**: 30 tests covering all functionality  
✅ **Development Automation**: One-command environment setup  
✅ **Production Ready**: Ready for iOS implementation or Android UI development  

---

**Status**: ✅ Phase 2 Complete - Ready for iOS or UI Development! 🚀