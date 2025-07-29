import SwiftUI
import Shared
import TwilioVideo

// Native video rendering view for Twilio video tracks
struct TwilioVideoView: UIViewRepresentable {
    let videoTrack: LocalVideoTrack?
    
    func makeUIView(context: Context) -> UIView {
        let view = UIView()
        view.backgroundColor = .black
        
        if let videoTrack = videoTrack {
            // Create a VideoView to render the video track
            let twilioVideoView = VideoView()
            twilioVideoView.contentMode = .scaleAspectFill
            twilioVideoView.translatesAutoresizingMaskIntoConstraints = false
            
            view.addSubview(twilioVideoView)
            NSLayoutConstraint.activate([
                twilioVideoView.topAnchor.constraint(equalTo: view.topAnchor),
                twilioVideoView.leadingAnchor.constraint(equalTo: view.leadingAnchor),
                twilioVideoView.trailingAnchor.constraint(equalTo: view.trailingAnchor),
                twilioVideoView.bottomAnchor.constraint(equalTo: view.bottomAnchor)
            ])
            
            // Add the video track to the view
            videoTrack.addRenderer(twilioVideoView)
        }
        
        return view
    }
    
    func updateUIView(_ uiView: UIView, context: Context) {
        // Update if needed
    }
}

// Placeholder video view when no track is available
struct PlaceholderVideoView: View {
    let isCameraEnabled: Bool
    
    var body: some View {
        ZStack {
            Color.black
            
            if isCameraEnabled {
                VStack {
                    Image(systemName: "video.circle.fill")
                        .font(.system(size: 60))
                        .foregroundColor(.white)
                    Text("Camera Active")
                        .foregroundColor(.white)
                        .font(.headline)
                }
            } else {
                VStack {
                    Image(systemName: "video.slash.circle.fill")
                        .font(.system(size: 60))
                        .foregroundColor(.gray)
                    Text("Camera Disabled")
                        .foregroundColor(.gray)
                        .font(.headline)
                }
            }
        }
    }
}

struct ContentView: View {
    @StateObject private var videoCallManager = VideoCallManager()
    @State private var roomName = "test-room"
    @State private var userIdentity = "ios-user"
    @State private var cameraEnabled = true
    @State private var micEnabled = true
    
    var body: some View {
        NavigationView {
            VStack(spacing: 20) {
                // Header
                VStack {
                    Image(systemName: "video.circle.fill")
                        .font(.system(size: 60))
                        .foregroundColor(.blue)
                    Text("Twilio Video KMP")
                        .font(.title)
                        .fontWeight(.bold)
                }
                .padding()
                
                if videoCallManager.isConnected {
                    // In-call UI with video
                    VStack(spacing: 16) {
                        // Video display area
                        ZStack {
                            if let videoTrack = videoCallManager.localVideoTrack {
                                TwilioVideoView(videoTrack: videoTrack)
                                    .frame(height: 300)
                                    .cornerRadius(12)
                                    .clipped()
                            } else {
                                PlaceholderVideoView(isCameraEnabled: videoCallManager.isCameraEnabled)
                                    .frame(height: 300)
                                    .cornerRadius(12)
                                    .clipped()
                            }
                            
                            // Connection status overlay
                            VStack {
                                HStack {
                                    Text("Connected to: \(videoCallManager.currentRoomName)")
                                        .font(.caption)
                                        .foregroundColor(.white)
                                        .padding(.horizontal, 8)
                                        .padding(.vertical, 4)
                                        .background(Color.black.opacity(0.6))
                                        .cornerRadius(8)
                                    Spacer()
                                }
                                Spacer()
                            }
                            .padding(8)
                        }
                        
                        Text("Participants: \(videoCallManager.participantCount)")
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                        
                        // Call controls
                        HStack(spacing: 30) {
                            Button(action: {
                                videoCallManager.toggleMicrophone()
                            }) {
                                Image(systemName: videoCallManager.isMicEnabled ? "mic.fill" : "mic.slash.fill")
                                    .font(.system(size: 24))
                                    .foregroundColor(videoCallManager.isMicEnabled ? .blue : .red)
                                    .frame(width: 50, height: 50)
                                    .background(Color.gray.opacity(0.2))
                                    .clipShape(Circle())
                            }
                            
                            Button(action: {
                                videoCallManager.toggleCamera()
                            }) {
                                Image(systemName: videoCallManager.isCameraEnabled ? "video.fill" : "video.slash.fill")
                                    .font(.system(size: 24))
                                    .foregroundColor(videoCallManager.isCameraEnabled ? .blue : .red)
                                    .frame(width: 50, height: 50)
                                    .background(Color.gray.opacity(0.2))
                                    .clipShape(Circle())
                            }
                            
                            Button(action: {
                                videoCallManager.switchCamera()
                            }) {
                                Image(systemName: "camera.rotate.fill")
                                    .font(.system(size: 24))
                                    .foregroundColor(.blue)
                                    .frame(width: 50, height: 50)
                                    .background(Color.gray.opacity(0.2))
                                    .clipShape(Circle())
                            }
                        }
                        
                        Button(action: {
                            videoCallManager.leaveCall()
                        }) {
                            HStack {
                                Image(systemName: "phone.down.fill")
                                Text("Leave Call")
                            }
                            .foregroundColor(.white)
                            .padding()
                            .background(Color.red)
                            .cornerRadius(10)
                        }
                    }
                    .padding()
                    
                } else {
                    // Lobby UI
                    VStack(spacing: 16) {
                        VStack(alignment: .leading, spacing: 8) {
                            Text("Room Name")
                                .font(.headline)
                            TextField("Enter room name", text: $roomName)
                                .textFieldStyle(RoundedBorderTextFieldStyle())
                        }
                        
                        VStack(alignment: .leading, spacing: 8) {
                            Text("Your Name")
                                .font(.headline)
                            TextField("Enter your name", text: $userIdentity)
                                .textFieldStyle(RoundedBorderTextFieldStyle())
                        }
                        
                        // Media toggles
                        VStack(spacing: 12) {
                            HStack {
                                Toggle("Camera", isOn: $cameraEnabled)
                                    .toggleStyle(SwitchToggleStyle())
                                Spacer()
                                Image(systemName: cameraEnabled ? "video.fill" : "video.slash.fill")
                                    .foregroundColor(cameraEnabled ? .blue : .gray)
                            }
                            
                            HStack {
                                Toggle("Microphone", isOn: $micEnabled)
                                    .toggleStyle(SwitchToggleStyle())
                                Spacer()
                                Image(systemName: micEnabled ? "mic.fill" : "mic.slash.fill")
                                    .foregroundColor(micEnabled ? .blue : .gray)
                            }
                        }
                        .padding()
                        .background(Color.gray.opacity(0.1))
                        .cornerRadius(10)
                        
                        Button(action: {
                            videoCallManager.joinCall(
                                roomName: roomName,
                                userIdentity: userIdentity,
                                cameraOn: cameraEnabled,
                                micOn: micEnabled
                            )
                        }) {
                            HStack {
                                Image(systemName: "phone.fill")
                                Text(videoCallManager.isConnecting ? "Connecting..." : "Join Call")
                            }
                            .foregroundColor(.white)
                            .padding()
                            .frame(maxWidth: .infinity)
                            .background(videoCallManager.isConnecting ? Color.gray : Color.green)
                            .cornerRadius(10)
                        }
                        .disabled(videoCallManager.isConnecting || roomName.isEmpty || userIdentity.isEmpty)
                    }
                    .padding()
                }
                
                // Status and debug info
                VStack(alignment: .leading, spacing: 4) {
                    Text("Status: \(videoCallManager.connectionStatus)")
                        .font(.caption)
                        .foregroundColor(.secondary)
                    
                    if !videoCallManager.errorMessage.isEmpty {
                        Text("Error: \(videoCallManager.errorMessage)")
                            .font(.caption)
                            .foregroundColor(.red)
                    }
                    
                    VStack(alignment: .leading, spacing: 2) {
                        Text("Config: \(VideoConfig().twilioTokenUrl)")
                            .font(.caption2)
                            .foregroundColor(.gray)
                            .lineLimit(1)
                            .truncationMode(.middle)
                        Text("User: \(VideoConfig().testUserIdentity)")
                            .font(.caption2)
                            .foregroundColor(.gray)
                    }
                }
                .padding(.horizontal)
                
                Spacer()
            }
            .navigationTitle("Video Call")
            .navigationBarTitleDisplayMode(.inline)
        }
    }
}

// iOS Video Call Manager that bridges to KMP
@MainActor
class VideoCallManager: ObservableObject {
    @Published var isConnected = false
    @Published var isConnecting = false
    @Published var currentRoomName = ""
    @Published var participantCount = 0
    @Published var isCameraEnabled = true
    @Published var isMicEnabled = true
    @Published var connectionStatus = "Disconnected"
    @Published var errorMessage = ""
    @Published var localVideoTrack: LocalVideoTrack?
    
    private var twilioManager: TwilioVideoManager?
    
    init() {
        twilioManager = TwilioVideoManagerFactory().create()
        observeConnectionState()
    }
    
    private func observeConnectionState() {
        // For now, we'll use a timer-based approach since Kotlin Flow doesn't directly conform to AsyncSequence
        // TODO: Implement proper Flow observation when KMP provides better Swift interop
        Timer.scheduledTimer(withTimeInterval: 1.0, repeats: true) { _ in
            // This would be replaced with proper Flow observation
            // For now, we'll rely on the connection state being updated in the joinCall method
        }
    }
    
    deinit {
        // Cleanup if needed
    }
    
    func joinCall(roomName: String, userIdentity: String, cameraOn: Bool, micOn: Bool) {
        isConnecting = true
        errorMessage = ""
        connectionStatus = "Connecting..."
        isCameraEnabled = cameraOn
        isMicEnabled = micOn
        
        Task {
            do {
                let result = try await twilioManager?.connect(
                    userIdentity: userIdentity,
                    roomName: roomName,
                    cameraOn: cameraOn,
                    micOn: micOn
                )
                
                if result is VideoResultSuccess<VideoRoom> {
                    await MainActor.run {
                        isConnected = true
                        isConnecting = false
                        currentRoomName = roomName
                        connectionStatus = "Connected"
                        print("✅ iOS UI: Successfully connected to room: \(roomName)")
                    }
                } else if let error = result as? VideoResultError {
                    await MainActor.run {
                        isConnecting = false
                        errorMessage = "Connection failed: \(error.error)"
                        connectionStatus = "Failed"
                        print("❌ iOS UI: Connection failed: \(error.error)")
                    }
                }
            } catch {
                await MainActor.run {
                    isConnecting = false
                    errorMessage = "Error: \(error.localizedDescription)"
                    connectionStatus = "Error"
                    print("💥 iOS UI: Connection error: \(error)")
                }
            }
        }
    }
    
    func leaveCall() {
        Task {
            _ = try await twilioManager?.disconnect()
            await MainActor.run {
                isConnected = false
                isConnecting = false
                currentRoomName = ""
                participantCount = 0
                connectionStatus = "Disconnected"
                errorMessage = ""
                localVideoTrack = nil
            }
        }
    }
    
    func toggleCamera() {
        isCameraEnabled.toggle()
        Task {
            _ = try await twilioManager?.enableCamera(enable: isCameraEnabled)
        }
    }
    
    func toggleMicrophone() {
        isMicEnabled.toggle()
        Task {
            _ = try await twilioManager?.enableMicrophone(enable: isMicEnabled)
        }
    }
    
    func switchCamera() {
        Task {
            _ = try await twilioManager?.switchCamera()
        }
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
