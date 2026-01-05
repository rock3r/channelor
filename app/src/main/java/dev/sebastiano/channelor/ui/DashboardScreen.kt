package dev.sebastiano.channelor.ui

import android.Manifest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import dev.sebastiano.channelor.domain.WifiNetwork
import dev.sebastiano.channelor.domain.ZigbeeChannelCongestion
import dev.sebastiano.channelor.ui.theme.ChannelorTheme

@Suppress("FunctionNaming")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun DashboardScreen(viewModel: MainViewModel = hiltViewModel()) {
    val permissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val isScanning by viewModel.isScanning.collectAsState()
    val zigbeeCongestion by viewModel.zigbeeCongestion.collectAsState()
    val wifiScanResults by viewModel.wifiScanResults.collectAsState()
    val recommendedChannels by viewModel.recommendedChannels.collectAsState()
    val top3Channels by viewModel.top3Channels.collectAsState()

    LaunchedEffect(permissionState.status) {
        if (permissionState.status.isGranted) {
            viewModel.onPermissionResult(true)
        } else {
            permissionState.launchPermissionRequest()
        }
    }

    DashboardContent(
        state =
            DashboardState(
                isScanning = isScanning,
                zigbeeCongestion = zigbeeCongestion,
                wifiScanResults = wifiScanResults,
                recommendedChannels = recommendedChannels,
                top3Channels = top3Channels,
            ),
        onScanClick = {
            if (permissionState.status.isGranted) {
                viewModel.triggerScan()
            } else {
                permissionState.launchPermissionRequest()
            }
        },
    )
}

@Suppress("FunctionNaming")
@Composable
fun DashboardContent(state: DashboardState, onScanClick: () -> Unit) {
    Scaffold(
        floatingActionButton = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val density = LocalDensity.current
                AnimatedVisibility(
                    visible = state.isScanning,
                    enter = fadeIn() + slideInHorizontally { with(density) { 20.dp.roundToPx() } },
                    exit = fadeOut() + slideOutHorizontally { with(density) { 20.dp.roundToPx() } },
                ) {
                    ScanningStatusCard(modifier = Modifier.padding(end = 16.dp))
                }
                ScanningFab(isScanning = state.isScanning, onScanClick = onScanClick)
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            Column(
                modifier =
                    Modifier.fillMaxSize()
                        .padding(top = 6.dp, bottom = 80.dp) // Avoid overlap with FAB
                        .padding(16.dp)
            ) {
                HeaderSection()

                Spacer(modifier = Modifier.height(24.dp))

                RecommendationSection(state.recommendedChannels)

                Spacer(modifier = Modifier.height(24.dp))

                SpectrumAnalysisSection(state = state, modifier = Modifier.weight(1f))
            }

            if (state.isScanning) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Suppress("MagicNumber", "FunctionNaming")
@Preview(name = "Initial / Empty State", showBackground = true)
@Composable
fun DashboardEmptyPreview() {
    ChannelorTheme {
        DashboardContent(
            state =
                DashboardState(
                    isScanning = false,
                    zigbeeCongestion = emptyList(),
                    wifiScanResults = emptyList(),
                    recommendedChannels = emptyList(),
                    top3Channels = emptySet(),
                ),
            onScanClick = {},
        )
    }
}

@Suppress("MagicNumber", "FunctionNaming")
@Preview(name = "Initial Scanning", showBackground = true)
@Composable
fun DashboardInitialScanningPreview() {
    ChannelorTheme {
        DashboardContent(
            state =
                DashboardState(
                    isScanning = true,
                    zigbeeCongestion = emptyList(),
                    wifiScanResults = emptyList(),
                    recommendedChannels = emptyList(),
                    top3Channels = emptySet(),
                ),
            onScanClick = {},
        )
    }
}

@Suppress("MagicNumber", "FunctionNaming")
@Preview(name = "Results (Light Mode)", showBackground = true)
@Preview(
    name = "Results (Dark Mode)",
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
)
@Composable
fun DashboardResultsPreview() {
    val mockWifi =
        listOf(
            WifiNetwork("Neighbors-2G", 2412, -45),
            WifiNetwork("MyHome-2G", 2437, -30),
            WifiNetwork("IoT-Devices", 2462, -60),
        )
    val mockZigbee =
        (11..26).map {
            ZigbeeChannelCongestion(
                channelNumber = it,
                centerFrequency = 2405 + 5 * (it - 11),
                congestionScore = if (it in listOf(15, 20, 25)) 10.0 else 80.0,
                isZllRecommended = it in listOf(11, 15, 20, 25),
            )
        }

    val state =
        DashboardState(
            isScanning = false,
            zigbeeCongestion = mockZigbee,
            wifiScanResults = mockWifi,
            recommendedChannels = mockZigbee.filter { it.channelNumber in setOf(15, 20, 25) },
            top3Channels = setOf(15, 20, 25),
        )

    ChannelorTheme { DashboardContent(state = state, onScanClick = {}) }
}

@Suppress("MagicNumber", "FunctionNaming")
@Preview(name = "Scanning with Results", showBackground = true)
@Composable
fun DashboardScanningWithResultsPreview() {
    val mockWifi =
        listOf(
            WifiNetwork("Neighbors-2G", 2412, -45),
            WifiNetwork("MyHome-2G", 2437, -30),
            WifiNetwork("IoT-Devices", 2462, -60),
        )
    val mockZigbee =
        (11..26).map {
            ZigbeeChannelCongestion(
                channelNumber = it,
                centerFrequency = 2405 + 5 * (it - 11),
                congestionScore = if (it in listOf(15, 20, 25)) 10.0 else 80.0,
                isZllRecommended = it in listOf(11, 15, 20, 25),
            )
        }

    val state =
        DashboardState(
            isScanning = true,
            zigbeeCongestion = mockZigbee,
            wifiScanResults = mockWifi,
            recommendedChannels = mockZigbee.filter { it.channelNumber in setOf(15, 20, 25) },
            top3Channels = setOf(15, 20, 25),
        )

    ChannelorTheme { DashboardContent(state = state, onScanClick = {}) }
}

@Suppress("FunctionNaming")
@Composable
fun HeaderSection() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier =
                Modifier.size(40.dp)
                    .background(
                        brush =
                            Brush.linearGradient(
                                colors =
                                    listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.secondary,
                                    )
                            ),
                        shape = CircleShape,
                    ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Rounded.Wifi,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = "Channelor",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Zigbee Interference Analyzer",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
    }
}

@Suppress("FunctionNaming")
@Composable
fun RecommendationSection(topChannels: List<ZigbeeChannelCongestion>) {
    Text(
        text = "Recommended Channels",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
    )
    Spacer(modifier = Modifier.height(8.dp))

    if (topChannels.isEmpty()) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Scan to see recommendations",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    } else {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 4.dp),
        ) {
            items(topChannels) { channel -> ChannelCard(channel) }
        }
    }
}

@Suppress("FunctionNaming")
@Composable
fun ChannelCard(channel: ZigbeeChannelCongestion) {
    val containerColor =
        when {
            channel.isZllRecommended -> MaterialTheme.colorScheme.primaryContainer
            channel.isWarning -> MaterialTheme.colorScheme.errorContainer
            else -> MaterialTheme.colorScheme.secondaryContainer
        }

    val onContainerColor =
        when {
            channel.isZllRecommended -> MaterialTheme.colorScheme.onPrimaryContainer
            channel.isWarning -> MaterialTheme.colorScheme.onErrorContainer
            else -> MaterialTheme.colorScheme.onSecondaryContainer
        }

    Card(
        modifier = Modifier.size(width = 160.dp, height = 120.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "CH ${channel.channelNumber}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = onContainerColor,
            )
            Text(
                text = "${channel.centerFrequency} MHz",
                style = MaterialTheme.typography.labelSmall,
                color = onContainerColor.copy(alpha = 0.7f),
            )
            if (channel.isZllRecommended) {
                Text(
                    text = "ZLL Recommended",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = onContainerColor,
                )
            }
            channel.annotation?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    color = onContainerColor,
                    textAlign = TextAlign.Center,
                    lineHeight = androidx.compose.ui.unit.TextUnit.Unspecified,
                )
            }
        }
    }
}

@Suppress("MagicNumber", "FunctionNaming")
@Preview(name = "Light Mode", showBackground = true)
@Preview(
    name = "Dark Mode",
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
)
@Composable
fun ChannelCardPreview() {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ChannelCard(
            ZigbeeChannelCongestion(
                channelNumber = 11,
                centerFrequency = 2405,
                congestionScore = 0.0,
                isZllRecommended = true,
                annotation = "Usually crowded by Wi-Fi",
            )
        )
        ChannelCard(
            ZigbeeChannelCongestion(
                channelNumber = 15,
                centerFrequency = 2425,
                congestionScore = 0.0,
                isZllRecommended = true,
            )
        )
        ChannelCard(
            ZigbeeChannelCongestion(
                channelNumber = 26,
                centerFrequency = 2480,
                congestionScore = 0.0,
                isWarning = true,
                annotation = "Problematic (low power, poor device support)",
            )
        )
        ChannelCard(
            ZigbeeChannelCongestion(
                channelNumber = 12,
                centerFrequency = 2410,
                congestionScore = 0.0,
                annotation = "Possible compatibility issues (Hue, IKEA, etc.)",
            )
        )
    }
}
