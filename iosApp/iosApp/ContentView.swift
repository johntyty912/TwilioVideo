import SwiftUI
import Shared
import TwilioVideo
import AVFoundation

// MARK: - Main Content View
struct ContentView: View {
    @StateObject private var videoCallManager = VideoCallManager()
    
    var body: some View {
        NavigationView {
            if videoCallManager.isConnected {
                MeetingRoomView(videoCallManager: videoCallManager)
            } else {
                LobbyView(videoCallManager: videoCallManager)
            }
        }
        .onAppear {
            videoCallManager.requestPermissions()
        }
    }
}

// MARK: - Lobby View
struct LobbyView: View {
    @ObservedObject var videoCallManager: VideoCallManager
    
    var body: some View {
        VStack(spacing: 20) {
            Text("🎥 Twilio Video KMP Demo")
                .font(.largeTitle)
                .fontWeight(.bold)
                .multilineTextAlignment(.center)
            
            // Permissions status
            if !videoCallManager.permissionsGranted {
                Card {
                    HStack {
                        Text("⚠️")
                        Text("Camera and microphone permissions required")
                            .font(.body)
                    }
                    .padding()
                }
                .background(Color.red.opacity(0.1))
                .cornerRadius(8)
            }
            
            // Connection status
            Text("Status: \(videoCallManager.connectionStatus)")
                .font(.body)
                .foregroundColor(statusColor)
            
            // User Identity field
            VStack(alignment: .leading) {
                Text("User Identity")
                    .font(.caption)
                    .foregroundColor(.secondary)
                TextField("Enter your name or ID", text: $videoCallManager.userIdentity)
                    .textFieldStyle(RoundedBorderTextFieldStyle())
            }
            
            // Room Name field
            VStack(alignment: .leading) {
                Text("Room Name")
                    .font(.caption)
                    .foregroundColor(.secondary)
                TextField("Enter room name or leave empty for random", text: $videoCallManager.roomName)
                    .textFieldStyle(RoundedBorderTextFieldStyle())
            }
            
            // Camera and Mic toggles
            VStack(spacing: 12) {
                HStack {
                    Toggle("Join with camera on", isOn: $videoCallManager.joinWithCameraOn)
                    Spacer()
                }
                
                HStack {
                    Toggle("Join with mic on", isOn: $videoCallManager.joinWithMicOn)
                    Spacer()
                }
            }
            
            // Join button
            Button(action: {
                videoCallManager.joinCall()
            }) {
                Text("🚀 Join Meeting")
                    .font(.headline)
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(canJoin ? Color.blue : Color.gray)
                    .cornerRadius(10)
            }
            .disabled(!canJoin)
            
            Spacer()
            
            // Configuration info
            VStack(spacing: 8) {
                Text("✅ Using real Twilio Video SDK")
                Text("🔗 Your API: \(VideoConfig().twilioTokenUrl)")
                Text("👤 User Identity: \(VideoConfig().testUserIdentity)")
            }
            .font(.caption)
            .foregroundColor(.secondary)
            .multilineTextAlignment(.center)
        }
        .padding()
    }
    
    private var statusColor: Color {
        switch videoCallManager.connectionStatus {
        case "Connected":
            return .green
        case "Failed":
            return .red
        default:
            return .orange
        }
    }
    
    private var canJoin: Bool {
        videoCallManager.connectionStatus == "Disconnected" && videoCallManager.permissionsGranted
    }
}

// MARK: - Meeting Room View
struct MeetingRoomView: View {
    @ObservedObject var videoCallManager: VideoCallManager
    
    var body: some View {
        VStack(spacing: 16) {
            // Room header
            Card {
                VStack(spacing: 8) {
                    Text("🎥 Meeting Room")
                        .font(.title2)
                        .fontWeight(.bold)
                    
                    Text(videoCallManager.currentRoomName.isEmpty ? "Unknown Room" : videoCallManager.currentRoomName)
                        .font(.body)
                    
                    Text("\(videoCallManager.participants.count + 1) participant(s)")
                        .font(.caption)
                }
                .padding()
            }
            .background(Color.blue.opacity(0.1))
            .cornerRadius(12)
            
            // Participants grid
            ScrollView {
                LazyVGrid(columns: [
                    GridItem(.adaptive(minimum: 160))
                ], spacing: 16) {
                    // Local video (always first)
                    LocalVideoCard(
                        videoCallManager: videoCallManager,
                        userIdentity: videoCallManager.userIdentity
                    )
                    
                    // Remote participants
                    ForEach(videoCallManager.participants, id: \.identity) { participant in
                        ParticipantCard(participant: participant)
                    }
                    
                    // Waiting message if no remote participants
                    if videoCallManager.participants.isEmpty {
                        Card {
                            VStack(spacing: 12) {
                                Text("👋")
                                    .font(.system(size: 48))
                                Text("Waiting for others")
                                    .font(.body)
                                    .foregroundColor(.secondary)
                            }
                            .frame(height: 160)
                        }
                        .background(Color.gray.opacity(0.1))
                        .cornerRadius(12)
                    }
                }
                .padding(.horizontal)
            }
            
            // Meeting controls
            MeetingControlsView(videoCallManager: videoCallManager)
        }
        .padding()
    }
}

// MARK: - Local Video Card
struct LocalVideoCard: View {
    @ObservedObject var videoCallManager: VideoCallManager
    let userIdentity: String
    
    var body: some View {
        Card {
            ZStack {
                if videoCallManager.isCameraEnabled {
                    TwilioVideoView(videoTrack: videoCallManager.rawLocalVideoTrack as? LocalVideoTrack)
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else {
                    VStack(spacing: 12) {
                        // Avatar
                        Circle()
                            .fill(Color.blue)
                            .frame(width: 60, height: 60)
                            .overlay(
                                Text(userIdentity.prefix(2).uppercased())
                                    .font(.title2)
                                    .fontWeight(.bold)
                                    .foregroundColor(.white)
                            )
                        
                        Text("You")
                            .font(.body)
                            .fontWeight(.medium)
                        
                        Text(videoCallManager.isCameraEnabled ? "Connecting..." : "Camera Off")
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                }
                
                // "You" badge
                VStack {
                    HStack {
                        Spacer()
                        Text("You")
                            .font(.caption)
                            .fontWeight(.medium)
                            .foregroundColor(.white)
                            .padding(.horizontal, 8)
                            .padding(.vertical, 4)
                            .background(Color.blue)
                            .cornerRadius(8)
                    }
                    Spacer()
                }
                .padding(8)
            }
            .frame(height: 160)
        }
        .background(Color.blue.opacity(0.1))
        .cornerRadius(12)
        .overlay(
            RoundedRectangle(cornerRadius: 12)
                .stroke(Color.blue, lineWidth: 2)
        )
    }
}

// MARK: - Participant Card
struct ParticipantCard: View {
    let participant: VideoParticipant
    
    var body: some View {
        Card {
            VStack(spacing: 12) {
                // Avatar
                Circle()
                    .fill(Color.blue)
                    .frame(width: 60, height: 60)
                    .overlay(
                        Text(participant.identity.prefix(2).uppercased())
                            .font(.title2)
                            .fontWeight(.bold)
                            .foregroundColor(.white)
                    )
                
                Text(participant.identity)
                    .font(.body)
                    .fontWeight(.medium)
                    .lineLimit(1)
                
                Text(participant.videoTracks.first?.isEnabled == true ? "Camera On" : "Camera Off")
                    .font(.caption)
                    .foregroundColor(.blue)
            }
            .frame(height: 160)
        }
        .background(Color.gray.opacity(0.1))
        .cornerRadius(12)
    }
}

// MARK: - Meeting Controls
struct MeetingControlsView: View {
    @ObservedObject var videoCallManager: VideoCallManager
    
    var body: some View {
        Card {
            VStack(spacing: 16) {
                // Control buttons
                HStack(spacing: 20) {
                    // Microphone toggle
                    Button(action: {
                        videoCallManager.toggleMicrophone()
                    }) {
                        Text(videoCallManager.isMicEnabled ? "🎤" : "🔇")
                            .font(.title2)
                            .frame(width: 60, height: 60)
                            .background(videoCallManager.isMicEnabled ? Color.blue : Color.red)
                            .foregroundColor(.white)
                            .clipShape(Circle())
                    }
                    
                    // Camera toggle
                    Button(action: {
                        videoCallManager.toggleCamera()
                    }) {
                        Text(videoCallManager.isCameraEnabled ? "📹" : "📵")
                            .font(.title2)
                            .frame(width: 60, height: 60)
                            .background(videoCallManager.isCameraEnabled ? Color.blue : Color.red)
                            .foregroundColor(.white)
                            .clipShape(Circle())
                    }
                    
                    // Switch camera (when video is on)
                    if videoCallManager.isCameraEnabled {
                        Button(action: {
                            videoCallManager.switchCamera()
                        }) {
                            Text("🔄")
                                .font(.title2)
                                .frame(width: 60, height: 60)
                                .background(Color.orange)
                                .foregroundColor(.white)
                                .clipShape(Circle())
                        }
                    }
                    
                    // Leave meeting
                    Button(action: {
                        videoCallManager.leaveCall()
                    }) {
                        Text("📞")
                            .font(.title2)
                            .frame(width: 60, height: 60)
                            .background(Color.red)
                            .foregroundColor(.white)
                            .clipShape(Circle())
                    }
                }
                
                // Control labels
                HStack(spacing: 20) {
                    Text(videoCallManager.isMicEnabled ? "Mute" : "Unmute")
                        .font(.caption)
                        .frame(maxWidth: .infinity)
                    
                    Text(videoCallManager.isCameraEnabled ? "Camera Off" : "Camera On")
                        .font(.caption)
                        .frame(maxWidth: .infinity)
                    
                    if videoCallManager.isCameraEnabled {
                        Text("Switch")
                            .font(.caption)
                            .frame(maxWidth: .infinity)
                    }
                    
                    Text("Leave")
                        .font(.caption)
                        .frame(maxWidth: .infinity)
                }
            }
            .padding()
        }
        .background(Color.gray.opacity(0.1))
        .cornerRadius(12)
    }
}

// MARK: - Twilio Video View
struct TwilioVideoView: UIViewRepresentable {
    let videoTrack: LocalVideoTrack?
    
    func makeUIView(context: Context) -> UIView {
        let view = UIView()
        view.backgroundColor = .black
        
        // Create the Twilio VideoView
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
        
        // Store the VideoView in context for later updates
        context.coordinator.videoView = twilioVideoView
        
        // Add the video track if available
        if let videoTrack = videoTrack {
            videoTrack.addRenderer(twilioVideoView)
            print("🎥 TwilioVideoView: Added video track to renderer")
        }
        
        return view
    }
    
    func updateUIView(_ uiView: UIView, context: Context) {
        // Handle video track changes
        if let videoTrack = videoTrack {
            // Remove any existing renderer
            context.coordinator.videoView?.removeFromSuperview()
            
            // Create new VideoView
            let twilioVideoView = VideoView()
            twilioVideoView.contentMode = .scaleAspectFill
            twilioVideoView.translatesAutoresizingMaskIntoConstraints = false
            
            uiView.addSubview(twilioVideoView)
            NSLayoutConstraint.activate([
                twilioVideoView.topAnchor.constraint(equalTo: uiView.topAnchor),
                twilioVideoView.leadingAnchor.constraint(equalTo: uiView.leadingAnchor),
                twilioVideoView.trailingAnchor.constraint(equalTo: uiView.trailingAnchor),
                twilioVideoView.bottomAnchor.constraint(equalTo: uiView.bottomAnchor)
            ])
            
            // Add the video track to the new view
            videoTrack.addRenderer(twilioVideoView)
            context.coordinator.videoView = twilioVideoView
            print("🎥 TwilioVideoView: Updated video track renderer")
        } else {
            // No video track, show black background
            context.coordinator.videoView?.removeFromSuperview()
            context.coordinator.videoView = nil
            print("🎥 TwilioVideoView: Removed video track renderer")
        }
    }
    
    func makeCoordinator() -> Coordinator {
        Coordinator()
    }
    
    class Coordinator {
        var videoView: VideoView?
    }
}

// MARK: - Helper Views
struct Card<Content: View>: View {
    let content: Content
    
    init(@ViewBuilder content: () -> Content) {
        self.content = content()
    }
    
    var body: some View {
        content
    }
}

// MARK: - iOS Video Call Manager
@MainActor
class VideoCallManager: ObservableObject {
    @Published var isConnected = false
    @Published var isConnecting = false
    @Published var currentRoomName = ""
    @Published var participants: [VideoParticipant] = []
    @Published var isCameraEnabled = true
    @Published var isMicEnabled = true
    @Published var connectionStatus = "Disconnected"
    @Published var errorMessage = ""
    @Published var localVideoTrack: LocalVideoTrack?
    @Published var rawLocalVideoTrack: Any?
    @Published var permissionsGranted = false
    
    // Lobby state
    @Published var userIdentity = ""
    @Published var roomName = ""
    @Published var joinWithCameraOn = false
    @Published var joinWithMicOn = true
    
    private var twilioManager: TwilioVideoManager?
    
    init() {
        twilioManager = TwilioVideoManagerFactory().create()
        observeConnectionState()
    }
    
    private func observeConnectionState() {
        // Timer-based approach for now since Kotlin Flow doesn't directly conform to AsyncSequence
        Timer.scheduledTimer(withTimeInterval: 1.0, repeats: true) { [weak self] _ in
            Task { @MainActor in
                // Observe raw video track from KMP module
                if let twilioManager = self?.twilioManager as? TwilioVideoManagerImpl {
                    // Access the raw video track from the iOS implementation
                    self?.rawLocalVideoTrack = twilioManager.getNativeLocalVideoTrack()
                }
            }
        }
    }
    
    func requestPermissions() {
        // Request camera and microphone permissions
        AVCaptureDevice.requestAccess(for: .video) { granted in
            AVCaptureDevice.requestAccess(for: .audio) { audioGranted in
                DispatchQueue.main.async {
                    self.permissionsGranted = granted && audioGranted
                }
            }
        }
    }
    
    func joinCall() {
        isConnecting = true
        errorMessage = ""
        connectionStatus = "Connecting..."
        isCameraEnabled = joinWithCameraOn
        isMicEnabled = joinWithMicOn
        
        let finalRoomName = roomName.isEmpty ? generateRandomRoomName() : roomName
        let finalUserIdentity = userIdentity.isEmpty ? "Anonymous" : userIdentity
        
        Task {
            do {
                let result = try await twilioManager?.connect(
                    userIdentity: finalUserIdentity,
                    roomName: finalRoomName,
                    cameraOn: joinWithCameraOn,
                    micOn: joinWithMicOn
                )
                
                if result is VideoResultSuccess<VideoRoom> {
                    await MainActor.run {
                        isConnected = true
                        isConnecting = false
                        currentRoomName = finalRoomName
                        connectionStatus = "Connected"
                        print("✅ iOS UI: Successfully connected to room: \(finalRoomName)")
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
                participants = []
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
    
    private func generateRandomRoomName() -> String {
        let letters = "abcdefghijklmnopqrstuvwxyz0123456789"
        return String((0..<10).map { _ in letters.randomElement()! })
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
