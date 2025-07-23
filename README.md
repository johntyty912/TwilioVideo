# Twilio Video KMP

A Kotlin Multiplatform project for integrating Twilio Video SDK across Android and iOS platforms with shared business logic.

## 🚀 Quick Start

### Prerequisites

1. **Install Mise** (recommended for environment management):
   ```bash
   curl https://mise.run | sh
   ```

2. **Setup Development Environment**:
   ```bash
   # Clone the repository
   git clone <repository-url>
   cd TwilioVideo
   
   # Setup everything with one command
   mise run setup
   ```

### Alternative Manual Setup

If you prefer not to use mise, ensure you have:

- **Java 17** (JetBrains Runtime recommended)
- **Gradle 8.13+**
- **Android SDK** (API 24+)
- **Xcode 14+** (for iOS development, macOS only)
- **CocoaPods** (for iOS dependencies)
- **Node.js 20+** (for tooling)
- **Python 3.11+** (for scripts)

## 📋 Development Commands

### Using Mise (Recommended)

```bash
# Quick commands (aliases)
mise s          # Setup project (alias for: mise run setup)
mise b          # Build project (alias for: mise run build)
mise t          # Run tests (alias for: mise run test)
mise c          # Clean build (alias for: mise run clean)
mise v          # Verify environment (alias for: mise run verify-env)

# Full commands
mise run setup                     # Complete project setup
mise run dev-docs                  # Show development guide
mise run build                     # Build entire project
mise run test                      # Run all tests
mise run android-build             # Build Android app
mise run android-sdk-check         # Check Android SDK installation
mise run android-sdk-install-guide # Show Android SDK installation guide
mise run ios-setup                 # Setup iOS dependencies (macOS)
mise run verify-env                # Verify development environment
mise run dev-docs                  # Show development guide
```

### Using Gradle Directly

```bash
# Build and test
./gradlew build                    # Build entire project
./gradlew shared:test              # Run shared module tests
./gradlew check                    # Run all checks

# Android
./gradlew composeApp:assembleDebug # Build Android app
./gradlew composeApp:installDebug  # Install on Android device

# Clean
./gradlew clean                    # Clean build artifacts
```

## 🏗️ Project Structure

```
TwilioVideo/
├── mise.toml                      # Development environment configuration
├── doc/
│   ├── TWILIO_VIDEO_KMP_PLAN.md  # Complete project plan
│   └── PHASE_1_TUTORIAL.md       # Phase 1 implementation guide
├── composeApp/                    # Compose Multiplatform app
│   ├── src/androidMain/           # Android-specific app code
│   └── src/commonMain/            # Shared app code
├── shared/                        # Shared business logic
│   ├── src/commonMain/kotlin/     # Shared Kotlin code
│   │   └── com/johnlai/twiliovideo/domain/video/
│   │       ├── TwilioVideoManager.kt     # Main interface
│   │       ├── VideoModels.kt            # Data models
│   │       └── TwilioVideoManagerImpl.kt # Expected implementation
│   ├── src/androidMain/kotlin/    # Android-specific implementations
│   └── src/iosMain/kotlin/        # iOS-specific implementations
└── iosApp/                        # iOS application
    └── iosApp.xcodeproj/          # Xcode project
```

## 🎯 Current Status: Phase 1 Complete ✅

### What's Working

- ✅ **Core Architecture**: Shared interfaces and data models
- ✅ **Platform Setup**: Android and iOS stub implementations  
- ✅ **Build System**: KMP build configuration with dependencies
- ✅ **Testing**: 13 comprehensive unit tests
- ✅ **Documentation**: Complete planning and tutorial docs

### Next: Phase 2 - Android Implementation

The foundation is ready for Twilio Video SDK integration. See `doc/TWILIO_VIDEO_KMP_PLAN.md` for the complete roadmap.

## 🧪 Testing

```bash
# Run all tests
mise run test

# Run specific module tests
./gradlew shared:test              # Shared module tests
./gradlew composeApp:testDebug     # Android app tests

# Test specific classes
./gradlew shared:test --tests "*TwilioVideoManagerTest*"
./gradlew shared:test --tests "*VideoModelsTest*"
```

## 📱 Platform Development

### Android Development

```bash
# Build and install
mise run android-build
./gradlew composeApp:installDebug

# Run on emulator/device
./gradlew composeApp:installDebug
adb shell am start -n com.johnlai.twiliovideo/com.johnlai.twiliovideo.MainActivity
```

### iOS Development (macOS only)

```bash
# Setup iOS dependencies
mise run ios-setup

# Open in Xcode
open iosApp/iosApp.xcodeproj

# Or build from command line
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -destination 'platform=iOS Simulator,name=iPhone 15 Pro' build
```

## 🔧 Troubleshooting

### Environment Issues

```bash
# Verify your environment
mise run verify-env

# Check tool versions
mise list
mise current

# Reinstall tools if needed
mise install
```

### Common Issues

1. **Android SDK not found**: Set `ANDROID_HOME` in your shell profile
2. **Java version conflicts**: Use `mise use java@jetbrains-17.0.9` 
3. **Gradle daemon issues**: Run `./gradlew --stop` then rebuild
4. **iOS build failures**: Run `mise run ios-setup` and check Xcode version

### Get Help

```bash
# Show development documentation
mise run dev-docs

# View project plan
cat doc/TWILIO_VIDEO_KMP_PLAN.md

# View Phase 1 tutorial
cat doc/PHASE_1_TUTORIAL.md
```

## 📚 Documentation

- **[Project Plan](doc/TWILIO_VIDEO_KMP_PLAN.md)** - Complete 6-8 week implementation plan
- **[Phase 1 Tutorial](doc/PHASE_1_TUTORIAL.md)** - Detailed Phase 1 implementation guide
- **[Twilio Video Docs](https://www.twilio.com/docs/video)** - Official Twilio documentation
- **[KMP Docs](https://kotlinlang.org/docs/multiplatform.html)** - Kotlin Multiplatform documentation

## 🤝 Contributing

1. Clone the repository
2. Run `mise run setup` to setup your environment
3. Make your changes
4. Run `mise run test` to ensure tests pass
5. Submit a pull request

## 📄 License

This project is part of the Remote TemiScript 2 application development.