@file:Suppress("FunctionNaming", "MagicNumber")

package dev.sebastiano.channelor.ui

import android.Manifest
import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import dev.sebastiano.channelor.domain.WifiNetwork
import dev.sebastiano.channelor.domain.ZigbeeChannelCongestion
import dev.sebastiano.channelor.ui.theme.ChannelorTheme

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: MainViewModel = hiltViewModel()) {
    val permissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val isScanning by viewModel.isScanning.collectAsState()
    val zigbeeCongestion by viewModel.zigbeeCongestion.collectAsState()
    val wifiScanResults by viewModel.wifiScanResults.collectAsState()
    val recommendedChannels by viewModel.recommendedChannels.collectAsState()
    val top5Channels by viewModel.top5Channels.collectAsState()

    var selectedChannel by remember { mutableStateOf<ZigbeeChannelCongestion?>(null) }
    val sheetState = rememberModalBottomSheetState()
    val scrollState = rememberScrollState()

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
                top5Channels = top5Channels,
            ),
        actions =
            DashboardActions(
                onScanClick = {
                    if (permissionState.status.isGranted) {
                        viewModel.triggerScan()
                    } else {
                        permissionState.launchPermissionRequest()
                    }
                },
                onChannelClick = { selectedChannel = it },
                onChannelDismiss = { selectedChannel = null },
            ),
        selectedChannel = selectedChannel,
        sheetState = sheetState,
        scrollState = scrollState,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardContent(
    state: DashboardState,
    actions: DashboardActions,
    selectedChannel: ZigbeeChannelCongestion?,
    sheetState: SheetState,
    scrollState: ScrollState,
) {
    Scaffold(
        floatingActionButton = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val density = LocalDensity.current
                val offsetPx = with(density) { 20.dp.roundToPx() }
                AnimatedVisibility(
                    visible = state.isScanning,
                    enter = fadeIn() + slideInHorizontally { offsetPx },
                    exit = fadeOut() + slideOutHorizontally { offsetPx },
                ) {
                    ScanningStatusCard(modifier = Modifier.padding(end = 16.dp))
                }
                ScanningFab(isScanning = state.isScanning, onScanClick = actions.onScanClick)
            }
        }
    ) { innerPadding ->
        BoxWithConstraints(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            val isWide = maxWidth > WIDE_SCREEN_THRESHOLD
            Column(
                modifier = Modifier.fillMaxSize().padding(top = 6.dp, bottom = 80.dp).padding(16.dp)
            ) {
                HeaderSection()

                Spacer(modifier = Modifier.height(24.dp))

                if (isWide) {
                    WideLayout(state, selectedChannel, scrollState, actions.onChannelClick)
                } else {
                    NarrowLayout(state, selectedChannel, actions.onChannelClick)
                }
            }

            if (state.isScanning) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            if (!isWide && selectedChannel != null) {
                ModalBottomSheet(
                    onDismissRequest = actions.onChannelDismiss,
                    sheetState = sheetState,
                ) {
                    ChannelDetailsContent(channel = selectedChannel)
                }
            }
        }
    }
}

@Composable
private fun WideLayout(
    state: DashboardState,
    selectedChannel: ZigbeeChannelCongestion?,
    scrollState: ScrollState,
    onChannelClick: (ZigbeeChannelCongestion) -> Unit,
) {
    Row(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.weight(1f).verticalScroll(scrollState)) {
            RecommendationSection(state.recommendedChannels, selectedChannel) { onChannelClick(it) }

            if (selectedChannel != null) {
                Spacer(modifier = Modifier.height(24.dp))
                ChannelDetailsContent(channel = selectedChannel)
            }
        }

        Spacer(modifier = Modifier.width(24.dp))

        SpectrumAnalysisSection(
            state = state,
            selectedChannel = selectedChannel,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun NarrowLayout(
    state: DashboardState,
    selectedChannel: ZigbeeChannelCongestion?,
    onChannelClick: (ZigbeeChannelCongestion) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        RecommendationSection(state.recommendedChannels, selectedChannel) { onChannelClick(it) }

        Spacer(modifier = Modifier.height(24.dp))

        SpectrumAnalysisSection(
            state = state,
            selectedChannel = selectedChannel,
            modifier = Modifier.weight(1f),
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalMaterial3Api::class)
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
                    top5Channels = emptySet(),
                ),
            actions =
                DashboardActions(onScanClick = {}, onChannelClick = {}, onChannelDismiss = {}),
            selectedChannel = null,
            sheetState = rememberModalBottomSheetState(),
            scrollState = rememberScrollState(),
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalMaterial3Api::class)
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
                    top5Channels = emptySet(),
                ),
            actions =
                DashboardActions(onScanClick = {}, onChannelClick = {}, onChannelDismiss = {}),
            selectedChannel = null,
            sheetState = rememberModalBottomSheetState(),
            scrollState = rememberScrollState(),
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalMaterial3Api::class)
@Preview(name = "Results (Light Mode)", showBackground = true)
@Preview(
    name = "Results (Dark Mode)",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
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
                interferingNetworks =
                    if (it !in listOf(15, 20, 25)) mockWifi.take(1) else emptyList(),
            )
        }

    val state =
        DashboardState(
            isScanning = false,
            zigbeeCongestion = mockZigbee,
            wifiScanResults = mockWifi,
            recommendedChannels =
                mockZigbee.filter { it.channelNumber in setOf(15, 17, 20, 22, 25) },
            top5Channels = setOf(15, 17, 20, 22, 25),
        )

    ChannelorTheme {
        DashboardContent(
            state = state,
            actions =
                DashboardActions(onScanClick = {}, onChannelClick = {}, onChannelDismiss = {}),
            selectedChannel = null,
            sheetState = rememberModalBottomSheetState(),
            scrollState = rememberScrollState(),
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalMaterial3Api::class)
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
            recommendedChannels =
                mockZigbee.filter { it.channelNumber in setOf(15, 17, 20, 22, 25) },
            top5Channels = setOf(15, 17, 20, 22, 25),
        )

    ChannelorTheme {
        DashboardContent(
            state = state,
            actions =
                DashboardActions(onScanClick = {}, onChannelClick = {}, onChannelDismiss = {}),
            selectedChannel = null,
            sheetState = rememberModalBottomSheetState(),
            scrollState = rememberScrollState(),
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalMaterial3Api::class)
@Preview(
    name = "Phone Landscape",
    device = "spec:width=411dp,height=891dp,dpi=420,orientation=landscape",
    showBackground = true,
)
@Composable
fun DashboardPhoneLandscapePreview() {
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
            recommendedChannels =
                mockZigbee.filter { it.channelNumber in setOf(15, 17, 20, 22, 25) },
            top5Channels = setOf(15, 17, 20, 22, 25),
        )

    ChannelorTheme {
        DashboardContent(
            state = state,
            actions =
                DashboardActions(onScanClick = {}, onChannelClick = {}, onChannelDismiss = {}),
            selectedChannel = mockZigbee.first(),
            sheetState = rememberModalBottomSheetState(),
            scrollState = rememberScrollState(),
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalMaterial3Api::class)
@Preview(name = "Tablet", device = "spec:width=1280dp,height=800dp,dpi=480", showBackground = true)
@Composable
fun DashboardTabletPreview() {
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
            recommendedChannels =
                mockZigbee.filter { it.channelNumber in setOf(15, 17, 20, 22, 25) },
            top5Channels = setOf(15, 17, 20, 22, 25),
        )

    ChannelorTheme {
        DashboardContent(
            state = state,
            actions =
                DashboardActions(onScanClick = {}, onChannelClick = {}, onChannelDismiss = {}),
            selectedChannel = mockZigbee.first(),
            sheetState = rememberModalBottomSheetState(),
            scrollState = rememberScrollState(),
        )
    }
}

private val WIDE_SCREEN_THRESHOLD = 840.dp
