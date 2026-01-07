# Channelor

Channelor is an Android application designed to analyze Wi-Fi interference in the 2.4GHz spectrum
and recommend the optimal Zigbee channels for smart home setups.

The app has been developed with the help of the AI features in Android Studio and Antigravity.

## Features

- **2.4GHz Wi-Fi Scanning**: Identifies nearby Wi-Fi networks and their interference potential.
- **Zigbee Interference Analysis**: Evaluates impact on all 16 Zigbee channels (11-26).
- **Congestion Scoring**: Quantitative scoring based on RSSI and spectral overlap.
- **Optimal Recommendations**: Highlights the top 5 least congested Zigbee channels.
- **Detailed Channel Insights**: Interactive channel cards that show detailed pros and cons (e.g.,
  ZLL compatibility, standard Wi-Fi overlap).
- **Visual Representation**: Graphical view of spectral congestion.
- **Modern Tech Stack**: Built with Jetpack Compose, Kotlin Coroutines/Flow, and Hilt.

## Getting Started

### Prerequisites

- Android Studio Ladybug or newer.
- Android device or emulator running API 33 (Android 13) or higher.
- Location permissions are required for Wi-Fi scanning.

### Compile and Run

1. Clone the repository.
2. Open the project in Android Studio.
3. Sync the project with Gradle files.
4. Build the project using the `./gradlew assembleDebug` command or via the IDE.
5. Run the application on your device using the "Run" button in Android Studio or
   `./gradlew installDebug`.

## License

```
Copyright 2026 Sebastiano Poggi and the Channelor contributors
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
