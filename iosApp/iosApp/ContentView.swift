import SwiftUI
import Shared

struct ContentView: View {
    @State private var showContent = false
    var body: some View {
        VStack {
            Button("Test Configuration") {
                withAnimation {
                    showContent = !showContent
                }
            }

            if showContent {
                VStack(spacing: 16) {
                    Image(systemName: "video.circle")
                        .font(.system(size: 100))
                        .foregroundColor(.accentColor)
                    
                    VStack(alignment: .leading, spacing: 8) {
                        Text("🔧 iOS Configuration Test:")
                            .font(.headline)
                        Text("Token URL: \(VideoConfig().twilioTokenUrl)")
                            .font(.caption)
                            .foregroundColor(.secondary)
                        Text("User Identity: \(VideoConfig().testUserIdentity)")
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                    .padding()
                    .background(Color.gray.opacity(0.1))
                    .cornerRadius(8)
                }
                .transition(.move(edge: .top).combined(with: .opacity))
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .top)
        .padding()
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
