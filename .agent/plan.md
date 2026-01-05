# Project Plan: Create a simple app that suggests the least congested Zigbee channel to use, by looking at the 2.4 Ghz Wi-Fi bands and mapping the least busy ones to Zigbee channels. The app should show a real-time visual graph of the Wi-Fi bands signal strength (no need to show the SSIDs) and highlight the three least busy Zigbee channels on the graph. The app must use Material Design 3, be edge-to-edge, and use a vibrant, energetic color scheme.

## Project Brief

### Features

*   **Wi-Fi Spectrum Scanner**: Scans the 2.4 GHz frequency band to detect active Wi-Fi networks and measure their signal strength (RSSI) in real-time.
*   **Zigbee Interference Engine**: Analyzes the Wi-Fi data to calculate congestion levels for standard Zigbee channels (11–26), mapping Wi-Fi frequency overlaps to Zigbee bandwidth.
*   **Live Spectrum Visualization**: Displays a dynamic, edge-to-edge graph representing Wi-Fi traffic, overlaying Zigbee channel markers to visually demonstrate congestion.
*   **Smart Channel Recommendations**: automatically highlights and suggests the top three least congested Zigbee channels to ensure optimal mesh network performance.

### High-Level Technical Stack

*   **Language**: Kotlin
*   **UI Framework**: Jetpack Compose (Material Design 3)
*   **Architecture**: MVVM with Hilt for Dependency Injection
*   **Concurrency**: Kotlin Coroutines & StateFlow for reactive UI updates
*   **Core Android APIs**:
    *   `WifiManager` for network scanning
    *   `LocationManager` (required for Wi-Fi scanning permissions on modern Android)
*   **Permissions**: Accompanist Permissions (or AndroidX Activity Result API)

## Implementation Steps
**Total Duration:** 11m 16s

### Task_1_Core_Logic_Permissions: Implement permissions handling (Location/WiFi), Wi-Fi scanning repository, and Zigbee congestion calculation logic.
- **Status:** COMPLETED
- **Updates:** Implemented Hilt setup, Manifest permissions, WifiRepository with 2.4GHz filter, ZigbeeAnalyzer with congestion logic, and a basic MainViewModel/UI to trigger scans and show results. Code compiles.
- **Acceptance Criteria:**
  - App requests and handles runtime permissions (Location)
  - WifiRepository scans and returns networks
  - ZigbeeAnalyzer correctly maps Wi-Fi signals to Zigbee channel congestion scores
- **Duration:** 6m 30s

### Task_2_UI_Implementation: Build the Jetpack Compose UI with a real-time Spectrum Graph (Canvas) and Zigbee channel recommendations.
- **Status:** COMPLETED
- **Updates:** Built the Jetpack Compose UI with a real-time Spectrum Graph (Canvas) and Zigbee channel recommendations.
Implemented `SpectrumGraph` using Canvas, `DashboardScreen` with top 3 cards, and updated `Color.kt`/`Theme.kt` for vibrant look. Wired up to ViewModel.
- **Acceptance Criteria:**
  - Real-time graph visualizes Wi-Fi signals (RSSI/Frequency)
  - Zigbee channels are overlaid on the graph
  - Top 3 least congested channels are displayed
  - UI updates reactively via ViewModel StateFlow
- **Duration:** 2m

### Task_3_Theming_Assets: Apply vibrant Material 3 styling, ensure strict Edge-to-Edge compliance, and add an adaptive app icon.
- **Status:** COMPLETED
- **Updates:** Added adaptive app icon, enforced custom vibrant theme (disabled dynamic color), and verified edge-to-edge compliance.
- **Acceptance Criteria:**
  - App uses a vibrant, energetic color scheme (Light/Dark support)
  - System bars are transparent (Edge-to-Edge)
  - Adaptive App Icon is implemented
  - UI components follow Material 3 guidelines
- **Duration:** 2m 44s

### Task_4_Run_Verify: Build the full app, ensure stability, and verify all features against requirements.
- **Status:** COMPLETED
- **Updates:** Ran `:app:testDebugUnitTest` and `:app:assembleDebug` successfully. Deployed to emulator. Addressed initial UI performance issues by optimizing `SpectrumGraph` rendering (reduced path steps, pre-calculated constants, simplified fill) and moving non-UI logic (sorting) to `MainViewModel`. Verified performance in logcat (no more frame skips observed).
- **Acceptance Criteria:**
  - Build passes successfully
  - App does not crash on startup or scanning
  - All existing tests pass
  - Critical UI issues (performance) are resolved
- **StartTime:** 2026-01-05 21:02:05 CET
- **Duration:** 5m
