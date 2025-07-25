# Phase 2 Tutorial: Advanced Twilio Video KMP Integration

## Overview

This tutorial covers all the key features and UX improvements delivered in Phase 2 of the Twilio Video KMP project. You’ll learn how to:
- Let users choose to join a meeting with camera and/or mic ON or OFF
- Ensure your UI state (camera/mic toggles) always matches the real state of the local tracks
- Guarantee remote video/mic state is accurate for all participants, even for late joiners
- Avoid privacy leaks: your video/audio is only published if you choose
- Build a robust, production-ready video meeting experience

---

## 1. User-Driven Camera & Mic State (Lobby)

**Goal:** Let users decide if they want to join with their camera and/or mic enabled.

**How it works:**
- The lobby screen now includes two toggles: “Join with camera on” and “Join with mic on.”
- These are implemented as `Switch` components, each backed by a `Boolean` state variable.
- When the user clicks “Join Meeting,” the values of these toggles are passed to the video manager’s `connect` method.

**Code Example:**
```kotlin
var joinWithCameraOn by remember { mutableStateOf(false) }
var joinWithMicOn by remember { mutableStateOf(true) }
...
Switch(
    checked = joinWithCameraOn,
    onCheckedChange = { joinWithCameraOn = it }
)
Switch(
    checked = joinWithMicOn,
    onCheckedChange = { joinWithMicOn = it }
)
...
videoManager.connect(userIdentity, roomName, joinWithCameraOn, joinWithMicOn)
```

---

## 2. Selective Track Publishing (Video Manager)

**Goal:** Only publish the local video/audio tracks if the user chose to enable them.

**How it works:**
- The `connect` method in `TwilioVideoManager` and its Android implementation now accept `cameraOn` and `micOn` parameters.
- Only if these are `true` do we create and add the local video/audio tracks to the Twilio room.
- This guarantees your video/audio is never published unless you intend it.

**Code Example:**
```kotlin
if (cameraOn) {
    setupLocalVideoTrack(appContext)
} else {
    _localVideoTrack.value = null
}
if (micOn) {
    setupLocalAudioTrack(appContext)
} else {
    _localAudioTrack.value = null
}
// Add to ConnectOptions only if present
```

---

## 3. UI State Sync: Camera & Mic Toggles

**Goal:** The meeting room UI toggles for camera and mic always reflect the real state of the local tracks.

**How it works:**
- The actual state of the local video/audio tracks is exposed as Flows from the video manager.
- In `MeetingRoomScreen`, these Flows are collected and used to initialize the toggle states in the meeting controls.
- When the user toggles camera/mic, the UI and the underlying track state always stay in sync.

**Code Example:**
```kotlin
val rawLocalVideoTrack by videoManager.rawLocalVideoTrack.collectAsStateWithLifecycle(initialValue = null)
val isLocalCameraEnabled = rawLocalVideoTrack?.isEnabled == true
val isLocalMicEnabled by videoManager.isLocalMicEnabled.collectAsStateWithLifecycle(initialValue = false)
...
MeetingControls(
    ...
    isMicEnabledInitial = isLocalMicEnabled,
    isVideoEnabledInitial = isLocalCameraEnabled
)
```

---

## 4. Accurate Remote Video/Mic State (for All Participants)

**Goal:** Always show the correct video/mic state for all remote participants, even if you join late or they toggle their camera/mic after you join.

**How it works:**
- The participant model now only exposes a `RemoteVideoTrack` if it is both subscribed and enabled.
- The participant listener is set for all remote participants (even those already in the room when you join), so you always receive video/mic events.
- The UI only shows the video if the track is both subscribed and enabled; otherwise, it shows the avatar/placeholder.

**Code Example:**
```kotlin
val videoTrack = participant.videoTracks.firstOrNull { it.remoteVideoTrack is RemoteVideoTrack }
val remoteVideoTrack = videoTrack?.remoteVideoTrack as? RemoteVideoTrack
val isVideoEnabled = videoTrack?.isEnabled == true
if (remoteVideoTrack != null && isVideoEnabled) {
    TwilioVideoView(remoteVideoTrack = remoteVideoTrack)
} else {
    // Show avatar/placeholder
}
```

---

## 5. Privacy & UX: No More Surprises

- Your video/audio is only published if you choose.
- The UI always matches the real state of your camera/mic.
- Remote video/mic state is always accurate, even for late joiners.
- All major UX bugs (ghost video, stale toggles, etc.) are resolved.

---

## 6. How to Extend or Customize

- You can add more toggles (e.g., “Join muted,” “Join with screen share”) using the same pattern.
- To add more participant info (e.g., network quality), extend the participant model and update the UI.
- For iOS, follow the same architecture: expose track state as Flows, sync UI state, and only publish tracks as needed.

---

## 7. Summary

Phase 2 delivers a robust, privacy-first, and production-ready Twilio Video experience. The code patterns here are best practice for any real-time video app: always let the user control what’s published, and always keep the UI in sync with the real state. 