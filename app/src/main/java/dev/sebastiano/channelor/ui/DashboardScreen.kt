package dev.sebastiano.channelor.ui

import android.Manifest
import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.ThumbDown
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import dev.sebastiano.channelor.R
import dev.sebastiano.channelor.domain.WifiNetwork
import dev.sebastiano.channelor.domain.ZigbeeChannelCongestion
import dev.sebastiano.channelor.ui.theme.ChannelorTheme

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(windowSizeClass: WindowSizeClass, viewModel: MainViewModel = hiltViewModel()) {
        val permissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
        val isScanning by viewModel.isScanning.collectAsState()
        val zigbeeCongestion by viewModel.zigbeeCongestion.collectAsState()
        val wifiScanResults by viewModel.wifiScanResults.collectAsState()
        val recommendedChannels by viewModel.recommendedChannels.collectAsState()
        val top3Channels by viewModel.top3Channels.collectAsState()

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
                                top3Channels = top3Channels,
                        ),
                onScanClick = {
                        if (permissionState.status.isGranted) {
                                viewModel.triggerScan()
                        } else {
                                permissionState.launchPermissionRequest()
                        }
                },
                onChannelClick = { selectedChannel = it },
                selectedChannel = selectedChannel,
                onChannelDismiss = { selectedChannel = null },
                sheetState = sheetState,
                scrollState = scrollState,
                windowSizeClass = windowSizeClass,
        )
}

@Suppress("FunctionNaming")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardContent(
        state: DashboardState,
        onScanClick: () -> Unit,
        onChannelClick: (ZigbeeChannelCongestion) -> Unit,
        selectedChannel: ZigbeeChannelCongestion?,
        onChannelDismiss: () -> Unit,
        sheetState: SheetState,
        scrollState: ScrollState,
        windowSizeClass: WindowSizeClass,
) {

        Scaffold(
                floatingActionButton = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                                val density = LocalDensity.current
                                AnimatedVisibility(
                                        visible = state.isScanning,
                                        enter =
                                                fadeIn() +
                                                        slideInHorizontally {
                                                                with(density) { 20.dp.roundToPx() }
                                                        },
                                        exit =
                                                fadeOut() +
                                                        slideOutHorizontally {
                                                                with(density) { 20.dp.roundToPx() }
                                                        },
                                ) { ScanningStatusCard(modifier = Modifier.padding(end = 16.dp)) }
                                ScanningFab(
                                        isScanning = state.isScanning,
                                        onScanClick = onScanClick
                                )
                        }
                }
        ) { innerPadding ->
                BoxWithConstraints(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                        val isWide = maxWidth > 600.dp
                        Column(
                                modifier =
                                        Modifier.fillMaxSize()
                                                .padding(
                                                        top = 6.dp,
                                                        bottom = 80.dp
                                                ) // Avoid overlap with FAB
                                                .padding(16.dp)
                        ) {
                                HeaderSection()

                                Spacer(modifier = Modifier.height(24.dp))

                                if (isWide) {
                                        Row(modifier = Modifier.weight(1f)) {
                                                Column(
                                                        modifier =
                                                                Modifier.weight(1f)
                                                                        .verticalScroll(scrollState)
                                                ) {
                                                        RecommendationSection(
                                                                state.recommendedChannels
                                                        ) { onChannelClick(it) }

                                                        if (selectedChannel != null) {
                                                                Spacer(
                                                                        modifier =
                                                                                Modifier.height(
                                                                                        24.dp
                                                                                )
                                                                )
                                                                ChannelDetailsContent(
                                                                        channel = selectedChannel
                                                                )
                                                        }
                                                }

                                                Spacer(modifier = Modifier.width(24.dp))

                                                SpectrumAnalysisSection(
                                                        state = state,
                                                        modifier = Modifier.weight(1f)
                                                )
                                        }
                                } else {
                                        RecommendationSection(state.recommendedChannels) {
                                                onChannelClick(it)
                                        }

                                        Spacer(modifier = Modifier.height(24.dp))

                                        SpectrumAnalysisSection(
                                                state = state,
                                                modifier = Modifier.weight(1f)
                                        )
                                }
                        }

                        if (state.isScanning) {
                                LinearProgressIndicator(
                                        modifier = Modifier.fillMaxWidth(),
                                        color = MaterialTheme.colorScheme.primary
                                )
                        }

                        if (!isWide && selectedChannel != null) {
                                ModalBottomSheet(
                                        onDismissRequest = onChannelDismiss,
                                        sheetState = sheetState
                                ) { ChannelDetailsContent(channel = selectedChannel) }
                        }
                }
        }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalMaterial3Api::class)
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
                        onChannelClick = {},
                        selectedChannel = null,
                        onChannelDismiss = {},
                        sheetState = rememberModalBottomSheetState(),
                        scrollState = rememberScrollState(),
                        windowSizeClass = WindowSizeClass.calculateFromSize(DpSize(400.dp, 800.dp)),
                )
        }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalMaterial3Api::class)
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
                        onChannelClick = {},
                        selectedChannel = null,
                        onChannelDismiss = {},
                        sheetState = rememberModalBottomSheetState(),
                        scrollState = rememberScrollState(),
                        windowSizeClass = WindowSizeClass.calculateFromSize(DpSize(400.dp, 800.dp)),
                )
        }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalMaterial3Api::class)
@Suppress("MagicNumber", "FunctionNaming")
@Preview(name = "Results (Light Mode)", showBackground = true)
@Preview(
        name = "Results (Dark Mode)",
        showBackground = true,
        uiMode = Configuration.UI_MODE_NIGHT_YES
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
                        recommendedChannels =
                                mockZigbee.filter { it.channelNumber in setOf(15, 20, 25) },
                        top3Channels = setOf(15, 20, 25),
                )

        ChannelorTheme {
                DashboardContent(
                        state = state,
                        onScanClick = {},
                        onChannelClick = {},
                        selectedChannel = null,
                        onChannelDismiss = {},
                        sheetState = rememberModalBottomSheetState(),
                        scrollState = rememberScrollState(),
                        windowSizeClass = WindowSizeClass.calculateFromSize(DpSize(400.dp, 800.dp)),
                )
        }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalMaterial3Api::class)
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
                        recommendedChannels =
                                mockZigbee.filter { it.channelNumber in setOf(15, 20, 25) },
                        top3Channels = setOf(15, 20, 25),
                )

        ChannelorTheme {
                DashboardContent(
                        state = state,
                        onScanClick = {},
                        onChannelClick = {},
                        selectedChannel = null,
                        onChannelDismiss = {},
                        sheetState = rememberModalBottomSheetState(),
                        scrollState = rememberScrollState(),
                        windowSizeClass = WindowSizeClass.calculateFromSize(DpSize(400.dp, 800.dp)),
                )
        }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalMaterial3Api::class)
@Suppress("MagicNumber", "FunctionNaming")
@Preview(
        name = "Phone Landscape",
        device = "spec:width=411dp,height=891dp,dpi=420,orientation=landscape",
        showBackground = true
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
                                mockZigbee.filter { it.channelNumber in setOf(15, 20, 25) },
                        top3Channels = setOf(15, 20, 25),
                )

        ChannelorTheme {
                DashboardContent(
                        state = state,
                        onScanClick = {},
                        onChannelClick = {},
                        selectedChannel = mockZigbee.first(),
                        onChannelDismiss = {},
                        sheetState = rememberModalBottomSheetState(),
                        scrollState = rememberScrollState(),
                        windowSizeClass = WindowSizeClass.calculateFromSize(DpSize(800.dp, 400.dp))
                )
        }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalMaterial3Api::class)
@Suppress("MagicNumber", "FunctionNaming")
@Preview(name = "Tablet", device = Devices.TABLET, showBackground = true)
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
                                mockZigbee.filter { it.channelNumber in setOf(15, 20, 25) },
                        top3Channels = setOf(15, 20, 25),
                )

        ChannelorTheme {
                DashboardContent(
                        state = state,
                        onScanClick = {},
                        onChannelClick = {},
                        selectedChannel = mockZigbee.first(),
                        onChannelDismiss = {},
                        sheetState = rememberModalBottomSheetState(),
                        scrollState = rememberScrollState(),
                        windowSizeClass = WindowSizeClass.calculateFromSize(DpSize(1280.dp, 800.dp))
                )
        }
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
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .primary,
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .secondary
                                                                        )
                                                        ),
                                                shape = CircleShape,
                                        ),
                        contentAlignment = Alignment.Center,
                ) {
                        Icon(
                                Icons.Rounded.Wifi,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary
                        )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                        Text(
                                text = stringResource(R.string.app_name),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                        )
                        Text(
                                text = stringResource(R.string.app_subtitle),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.secondary,
                        )
                }
        }
}

@Suppress("FunctionNaming")
@Composable
fun RecommendationSection(
        topChannels: List<ZigbeeChannelCongestion>,
        onChannelClick: (ZigbeeChannelCongestion) -> Unit,
) {
        Text(
                text = stringResource(R.string.recommended_channels_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (topChannels.isEmpty()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                        Text(
                                text = stringResource(R.string.scan_to_see_recommendations),
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium,
                        )
                }
        } else {
                LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp),
                ) {
                        items(topChannels) { channel ->
                                ChannelCard(
                                        channel = channel,
                                        onClick = { onChannelClick(channel) }
                                )
                        }
                }
        }
}

@Suppress("FunctionNaming")
@Composable
fun ChannelCard(channel: ZigbeeChannelCongestion, onClick: () -> Unit) {
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
                onClick = onClick,
        ) {
                Box(modifier = Modifier.fillMaxSize()) {
                        Icon(
                                imageVector = Icons.Rounded.Info,
                                contentDescription = null,
                                modifier =
                                        Modifier.align(Alignment.TopEnd).padding(8.dp).size(16.dp),
                                tint = onContainerColor.copy(alpha = 0.5f),
                        )

                        Column(
                                modifier = Modifier.fillMaxSize().padding(12.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                                Text(
                                        text =
                                                stringResource(
                                                        R.string.channel_number_format,
                                                        channel.channelNumber
                                                ),
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = onContainerColor,
                                )
                                Text(
                                        text =
                                                stringResource(
                                                        R.string.frequency_format,
                                                        channel.centerFrequency
                                                ),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = onContainerColor.copy(alpha = 0.7f),
                                )
                                if (channel.isZllRecommended) {
                                        Text(
                                                text = stringResource(R.string.zll_recommended),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = onContainerColor,
                                        )
                                }
                        }
                }
        }
}

@Suppress("FunctionNaming")
@Composable
fun ChannelDetailsContent(channel: ZigbeeChannelCongestion) {
        Column(
                modifier =
                        Modifier.fillMaxWidth()
                                .padding(horizontal = 24.dp)
                                .padding(top = 8.dp, bottom = 48.dp)
        ) {
                Text(
                        text =
                                stringResource(
                                        R.string.channel_details_title,
                                        channel.channelNumber
                                ),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                )
                Text(
                        text =
                                stringResource(
                                        R.string.center_frequency_format,
                                        channel.centerFrequency
                                ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary,
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (channel.pros.isNotEmpty()) {
                        Text(
                                text = stringResource(R.string.pros),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        channel.pros.forEach { proResId ->
                                Row(
                                        modifier = Modifier.padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                        Icon(
                                                imageVector = Icons.Rounded.ThumbUp,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                                tint = MaterialTheme.colorScheme.primary,
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                                text = stringResource(proResId),
                                                style = MaterialTheme.typography.bodyMedium
                                        )
                                }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                }

                if (channel.cons.isNotEmpty()) {
                        Text(
                                text = stringResource(R.string.cons),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        channel.cons.forEach { conResId ->
                                Row(
                                        modifier = Modifier.padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                        Icon(
                                                imageVector = Icons.Rounded.ThumbDown,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                                tint = MaterialTheme.colorScheme.error,
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                                text = stringResource(conResId),
                                                style = MaterialTheme.typography.bodyMedium
                                        )
                                }
                        }
                }
        }
}

@Suppress("MagicNumber", "FunctionNaming")
@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ChannelCardPreview() {
        Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
                ChannelCard(
                        channel =
                                ZigbeeChannelCongestion(
                                        channelNumber = 11,
                                        centerFrequency = 2405,
                                        congestionScore = 0.0,
                                        isZllRecommended = true,
                                ),
                        onClick = {},
                )
                ChannelCard(
                        channel =
                                ZigbeeChannelCongestion(
                                        channelNumber = 15,
                                        centerFrequency = 2425,
                                        congestionScore = 0.0,
                                        isZllRecommended = true,
                                ),
                        onClick = {},
                )
                ChannelCard(
                        channel =
                                ZigbeeChannelCongestion(
                                        channelNumber = 26,
                                        centerFrequency = 2480,
                                        congestionScore = 0.0,
                                        isWarning = true,
                                ),
                        onClick = {},
                )
                ChannelCard(
                        channel =
                                ZigbeeChannelCongestion(
                                        channelNumber = 12,
                                        centerFrequency = 2410,
                                        congestionScore = 0.0
                                ),
                        onClick = {},
                )
        }
}

@Suppress("MagicNumber", "FunctionNaming")
@Preview(name = "Details Sheet (Recommended)", showBackground = true)
@Composable
fun ChannelDetailsRecommendedPreview() {
        ChannelorTheme {
                ChannelDetailsContent(
                        channel =
                                ZigbeeChannelCongestion(
                                        channelNumber = 15,
                                        centerFrequency = 2425,
                                        congestionScore = 10.0,
                                        isZllRecommended = true,
                                        pros = listOf(R.string.pro_zll_recommended),
                                        cons = emptyList(),
                                )
                )
        }
}

@Suppress("MagicNumber", "FunctionNaming")
@Preview(name = "Details Sheet (Problematic)", showBackground = true)
@Composable
fun ChannelDetailsProblematicPreview() {
        ChannelorTheme {
                ChannelDetailsContent(
                        channel =
                                ZigbeeChannelCongestion(
                                        channelNumber = 11,
                                        centerFrequency = 2405,
                                        congestionScore = 80.0,
                                        isZllRecommended = true,
                                        pros = listOf(R.string.pro_zll_recommended),
                                        cons = listOf(R.string.con_wifi_1_interference),
                                )
                )
        }
}
