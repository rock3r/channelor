AGENTS.md
----

# Channelor Project Summary

Channelor is an Android application designed to analyze Wi-Fi interference in the 2.4GHz spectrum and recommend the optimal Zigbee channels for smart home setups.

## Features

1.  **2.4GHz Wi-Fi Scanning**: Monitors nearby Wi-Fi networks to identify potential sources of interference.
2.  **Zigbee Interference Analysis**: Evaluates the impact of Wi-Fi signals on all 16 Zigbee channels (11-26).
3.  **Spectral Congestion Scoring**: Calculates a quantitative congestion score for each Zigbee channel using an algorithm that factors in Wi-Fi signal strength (RSSI) and spectral overlap.
4.  **Optimal Channel Recommendations**: Automatically identifies and highlights the top 3 least congested Zigbee channels.
5.  **Real-time Data Updates**: Uses Kotlin Flows and BroadcastReceivers to update scan results and analysis in real-time.
6.  **Visual Spectrum Representation**: Provides a graphical view of congestion across the spectrum (via `SpectrumGraph.kt`).
7.  **Dependency Injection**: Implements Hilt for robust and testable dependency management.
8.  **Reactive Architecture**: Built using a modern MVVM architecture with Jetpack Compose.

## Code Structure and Key Objects

### Data Layer
- **`app/src/main/java/dev/sebastiano/channelor/data/WifiRepository.kt`**:
    - `WifiRepository`: Interface defining the contract for Wi-Fi scanning operations.
    - `WifiRepositoryImpl`: Uses `WifiManager` and a `callbackFlow` to stream `ScanResult` data, filtered for the 2.4GHz band (`is24GHz` function).

### Domain Layer
- **`app/src/main/java/dev/sebastiano/channelor/domain/Models.kt`**:
    - `WifiNetwork`: Plain data model for a Wi-Fi network (SSID, Frequency, RSSI).
    - `ZigbeeChannelCongestion`: Data model for channel number, center frequency, and congestion score.
- **`app/src/main/java/dev/sebastiano/channelor/domain/ZigbeeAnalyzer.kt`**:
    - `ZigbeeAnalyzer`: Contains the core logic for calculating interference, now platform-agnostic.
    - `analyzeCongestion(wifiNetworks)`: Maps `WifiNetwork` list to Zigbee channel congestion.
    - `calculateCongestionForChannel(...)`: Computes interference power using a spectral mask approximation.

### Testing
- **JVM Unit Tests**:
    - `app/src/test/java/dev/sebastiano/channelor/domain/ZigbeeAnalyzerTest.kt`: Tests the core interference calculation logic.
- **Compose UI Tests**:
    - `app/src/androidTest/java/dev/sebastiano/channelor/ui/ChannelCardTest.kt`: Tests individual UI components.

### Presentation Layer
- **`app/src/main/java/dev/sebastiano/channelor/ui/MainViewModel.kt`**:
    - `MainViewModel`: Managed by Hilt. Exposes `StateFlow` objects for UI consumption:
        - `wifiScanResults`: The raw list of detected Wi-Fi networks.
        - `zigbeeCongestion`: The calculated congestion data.
        - `top3Channels`: The set of recommended optimal channels.
    - `triggerScan()`: Initiates a new Wi-Fi scan through the repository.
- **`app/src/main/java/dev/sebastiano/channelor/ui/DashboardScreen.kt`**: The main UI container for the application.
- **`app/src/main/java/dev/sebastiano/channelor/ui/SpectrumGraph.kt`**: Composable responsible for rendering the congestion graph.
- **`app/src/main/java/dev/sebastiano/channelor/MainActivity.kt`**: Handles runtime permissions (`ACCESS_FINE_LOCATION`) and sets up the Compose content.

### Configuration
- **`app/build.gradle.kts`**: Defines the Android application configuration and dependencies (Hilt, Compose, etc.).
- **`settings.gradle.kts`**: Project-level settings.
- **`gradle/libs.versions.toml`**: Centralized version management for dependencies.

### Formatting
Use the `ktfmt` formatter to format the code. It is configured in the root `build.gradle.kts` file. You must run the `ktfmtFormat` task to format the code after every change.
