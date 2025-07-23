# Phase 2: Testing Environment Setup for Twilio Video KMP

## 🎯 Overview

Before implementing the real Twilio Video SDK integration, we need to establish a robust testing environment. This guide covers all the necessary setup for effective development and testing.

## 📋 Testing Environment Checklist

### 1. 🔑 Twilio Account & Credentials Setup

#### Required Twilio Resources
- [ ] **Twilio Account** (Console access)
- [ ] **Account SID** (unique identifier)
- [ ] **API Key SID** (for token generation)
- [ ] **API Key Secret** (for token generation)
- [ ] **Video Service** (enabled in console)

#### Setup Steps
1. **Create Twilio Account**: https://www.twilio.com/try-twilio
2. **Navigate to Console**: https://console.twilio.com/
3. **Create API Key**:
   - Go to Account → API Keys & Tokens
   - Create new API Key for video access
   - Save SID and Secret securely
4. **Enable Video Service**:
   - Go to Video → Configure
   - Enable Programmable Video

### 2. 📱 Android Testing Environment

#### Physical Device Requirements
- [ ] **Android 7.0+ device** (API 24+)
- [ ] **Camera access** (front and back)
- [ ] **Microphone access**
- [ ] **Network connectivity** (WiFi/cellular)
- [ ] **USB debugging enabled**

#### Android Emulator Setup
```bash
# Check available emulators
mise run android-sdk-check

# Create emulator with camera support
$ANDROID_HOME/cmdline-tools/latest/bin/avdmanager create avd \
  -n TwilioVideoTest \
  -k "system-images;android-34;google_apis;x86_64" \
  -c 2048M

# Enable camera in emulator config
echo "hw.camera.front=webcam0" >> ~/.android/avd/TwilioVideoTest.avd/config.ini
echo "hw.camera.back=webcam0" >> ~/.android/avd/TwilioVideoTest.avd/config.ini
```

#### Permission Testing
- [ ] **Camera permission** (runtime)
- [ ] **Microphone permission** (runtime)
- [ ] **Network permission** (manifest)

### 3. 🧪 Testing Infrastructure

#### Unit Testing Enhancement
- [ ] **Mock Twilio SDK** (for unit tests)
- [ ] **Coroutines testing** (already configured)
- [ ] **Network condition simulation**
- [ ] **Error scenario testing**

#### Integration Testing
- [ ] **Real Twilio room creation**
- [ ] **Multi-device testing**
- [ ] **Cross-platform testing** (Android ↔ iOS)
- [ ] **Network quality testing**

#### Performance Testing
- [ ] **Memory usage monitoring**
- [ ] **Battery consumption**
- [ ] **CPU usage profiling**
- [ ] **Network bandwidth testing**

### 4. 🔧 Development Tools Setup

#### Video Debugging Tools
- [ ] **Android Studio Profiler** (memory, CPU, network)
- [ ] **Twilio Video Insights** (call quality analytics)
- [ ] **Chrome DevTools** (for web testing)
- [ ] **Wireshark** (network packet analysis)

#### Logging & Monitoring
- [ ] **Structured logging** (for video events)
- [ ] **Error tracking** (crashes, API failures)
- [ ] **Performance metrics** (connection time, video quality)
- [ ] **User analytics** (usage patterns)

### 5. 📺 Test Content & Scenarios

#### Test Video Rooms
- [ ] **Empty room** (solo testing)
- [ ] **Two-participant room** (basic calling)
- [ ] **Multi-participant room** (group calling)
- [ ] **Room with screen sharing**

#### Test Scenarios
- [ ] **Happy path** (successful connection)
- [ ] **Network interruption** (disconnect/reconnect)
- [ ] **Permission denied** (camera/mic blocked)
- [ ] **Invalid token** (authentication failure)
- [ ] **Room full** (capacity limit)
- [ ] **Low bandwidth** (quality adaptation)

#### Media Test Files
- [ ] **Test video streams** (various resolutions)
- [ ] **Audio test patterns** (quality verification)
- [ ] **Screen sharing content** (text, images, video)

## 🛠️ Implementation Plan

### Step 1: Environment Verification
```bash
# Verify mise environment
mise run verify-env

# Check Android SDK
mise run android-sdk-check

# Test Gradle build
./gradlew shared:test
```

### Step 2: Token Service Configuration (SIMPLIFIED!)
```bash
# Create environment file for testing
touch .env.local
echo "# Twilio Video Token Service" >> .env.local
echo "TWILIO_TOKEN_URL=https://your-api-endpoint.com/twilio/video_token" >> .env.local
echo "" >> .env.local
echo "# Testing Configuration" >> .env.local
echo "# User identity should always be 'user'" >> .env.local
echo "TEST_USER_IDENTITY=user" >> .env.local
echo "# Room name should be random 10-char alphanumeric string" >> .env.local
echo "TEST_ROOM_NAME_LENGTH=10" >> .env.local
echo "# Example room name format: a1b2c3d4e5" >> .env.local
echo "EXAMPLE_ROOM_NAME=a1b2c3d4e5" >> .env.local
```

**No manual Twilio credentials needed!** ✅ Your existing token service handles everything.

#### API Parameters
- **User Identity**: Always `"user"`
- **Room Name**: Random 10-character alphanumeric string (e.g., "a1b2c3d4e5")

#### Example API Call
```json
POST https://your-api-endpoint.com/twilio/video_token
Content-Type: application/json

{
  "userIdentity": "user",
  "roomName": "a1b2c3d4e5"
}
```

### Step 3: Testing Module Setup
```bash
# Create testing utilities
mkdir -p shared/src/androidTest/kotlin/com/johnlai/twiliovideo/test
mkdir -p shared/src/commonTest/kotlin/com/johnlai/twiliovideo/test
```

### Step 4: Mock & Test Data
```bash
# Create test fixtures
mkdir -p testData/video
mkdir -p testData/audio
mkdir -p testData/scenarios
```

## 📊 Testing Strategy

### Unit Testing (90% coverage target)
- **Models**: VideoRoom, VideoParticipant, etc.
- **Interfaces**: TwilioVideoManager contract
- **Utils**: Token handling, error mapping
- **State Management**: Connection states, flows

### Integration Testing
- **SDK Integration**: Real Twilio SDK calls
- **Platform Testing**: Android-specific features
- **Cross-platform**: Shared logic consistency
- **Performance**: Memory, CPU, network

### End-to-End Testing
- **User Flows**: Complete video call scenarios
- **Multi-device**: Different Android versions
- **Network Conditions**: Various connectivity scenarios
- **Edge Cases**: Error handling, recovery

## 🔍 Quality Metrics

### Performance Benchmarks
- **Connection Time**: < 3 seconds
- **Memory Usage**: < 50MB additional
- **Battery Impact**: < 10% increase
- **Network Efficiency**: Adaptive bitrate

### Quality Metrics
- **Video Resolution**: 720p default, adaptive
- **Frame Rate**: 30fps target
- **Audio Quality**: 44.1kHz, low latency
- **Connection Success**: > 95%

### Reliability Metrics
- **Crash Rate**: < 0.1%
- **Connection Drops**: < 1%
- **Reconnection Success**: > 90%
- **Error Recovery**: Graceful handling

## 🚀 Next Steps

1. **Run environment setup** (`mise run phase2-setup`)
2. **Configure Twilio credentials** (secure storage)
3. **Create test scenarios** (comprehensive coverage)
4. **Setup continuous testing** (CI/CD integration)
5. **Begin Android implementation** (with testing foundation)

## 📚 Resources

- [Twilio Video Android Quickstart](https://www.twilio.com/docs/video/android-v7-getting-started)
- [Android Testing Guide](https://developer.android.com/training/testing)
- [KMP Testing Best Practices](https://kotlinlang.org/docs/multiplatform-run-tests.html)
- [Video Quality Testing](https://webrtc.github.io/test-pages/)

---

**🎯 Goal**: Robust testing environment ready for real Twilio SDK integration in Phase 2 implementation. 