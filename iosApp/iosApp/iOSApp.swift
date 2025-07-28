import SwiftUI
import Shared

@main
struct iOSApp: App {
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
    
    init() {
        // Test our iOS configuration
        print("🔧 iOS Configuration Test:")
        print("   TWILIO_TOKEN_URL: \(VideoConfig().twilioTokenUrl)")
        print("   TEST_USER_IDENTITY: \(VideoConfig().testUserIdentity)")
    }
}