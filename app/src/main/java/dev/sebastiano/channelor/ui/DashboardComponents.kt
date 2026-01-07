@file:Suppress("MatchingDeclarationName", "FunctionNaming")

package dev.sebastiano.channelor.ui

import android.content.res.Configuration
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.sebastiano.channelor.R
import dev.sebastiano.channelor.domain.WifiNetwork
import dev.sebastiano.channelor.domain.ZigbeeChannelCongestion
import dev.sebastiano.channelor.ui.theme.ChannelorTheme

/** Data class representing the state of the dashboard. */
data class DashboardState(
    val isScanning: Boolean,
    val zigbeeCongestion: List<ZigbeeChannelCongestion>,
    val wifiScanResults: List<WifiNetwork>,
    val recommendedChannels: List<ZigbeeChannelCongestion>,
    val top5Channels: Set<Int>,
)

/** Data class representing the actions available on the dashboard. */
data class DashboardActions(
    val onScanClick: () -> Unit,
    val onChannelClick: (ZigbeeChannelCongestion) -> Unit,
    val onChannelDismiss: () -> Unit,
)

@Composable
fun HeaderSection() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painterResource(R.drawable.ic_zigbee),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                tint = MaterialTheme.colorScheme.primary,
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

@Composable
fun RecommendationSection(
    topChannels: List<ZigbeeChannelCongestion>,
    selectedChannel: ZigbeeChannelCongestion?,
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
                    isSelected = channel.channelNumber == selectedChannel?.channelNumber,
                    onClick = { onChannelClick(channel) },
                )
            }
        }
    }
}

@Composable
fun ChannelCard(
    channel: ZigbeeChannelCongestion,
    isSelected: Boolean = false,
    onClick: () -> Unit,
) {
    val containerColor = getChannelCardContainerColor(channel, isSelected)
    val onContainerColor = getChannelCardOnContainerColor(channel)

    Card(
        modifier = Modifier.size(width = 160.dp, height = 120.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        onClick = onClick,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Icon(
                imageVector = Icons.Rounded.Info,
                contentDescription = null,
                modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).size(16.dp),
                tint = onContainerColor.copy(alpha = 0.5f),
            )

            ChannelCardMainContent(channel, onContainerColor)
        }
    }
}

@Composable
private fun getChannelCardContainerColor(
    channel: ZigbeeChannelCongestion,
    isSelected: Boolean,
): Color {
    val baseContainerColor =
        when {
            channel.isZllRecommended -> MaterialTheme.colorScheme.primaryContainer
            channel.isWarning -> MaterialTheme.colorScheme.errorContainer
            else -> MaterialTheme.colorScheme.secondaryContainer
        }

    return if (isSelected) {
        val overlayColor =
            if (isSystemInDarkTheme()) {
                Color.White.copy(alpha = 0.2f)
            } else {
                Color.Black.copy(alpha = 0.2f)
            }
        overlayColor.compositeOver(baseContainerColor)
    } else {
        baseContainerColor
    }
}

@Composable
private fun getChannelCardOnContainerColor(channel: ZigbeeChannelCongestion): Color =
    when {
        channel.isZllRecommended -> MaterialTheme.colorScheme.onPrimaryContainer
        channel.isWarning -> MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.onSecondaryContainer
    }

@Composable
private fun ChannelCardMainContent(channel: ZigbeeChannelCongestion, onContainerColor: Color) {
    Column(
        modifier = Modifier.fillMaxSize().padding(12.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.channel_number_format, channel.channelNumber),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = onContainerColor,
        )
        Text(
            text = stringResource(R.string.frequency_format, channel.centerFrequency),
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
        if (channel.isWarning) {
            Text(
                text = stringResource(R.string.not_recommended),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = onContainerColor,
            )
        }
    }
}

@Composable
fun SpectrumAnalysisSection(
    state: DashboardState,
    selectedChannel: ZigbeeChannelCongestion?,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            if (state.wifiScanResults.isEmpty() && state.zigbeeCongestion.isEmpty()) {
                EmptySpectrumView(state.isScanning, modifier = Modifier.align(Alignment.Center))
            } else {
                SpectrumGraph(
                    wifiScanResults = state.wifiScanResults,
                    zigbeeCongestion = state.zigbeeCongestion,
                    top5ChannelNumbers = state.top5Channels,
                    selectedChannel = selectedChannel,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Suppress("MagicNumber")
@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ChannelCardPreview() {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ChannelCard(
            channel =
                ZigbeeChannelCongestion(
                    channelNumber = 11,
                    centerFrequency = 2405,
                    congestionScore = 0.0,
                    isZllRecommended = true,
                ),
            onClick = {},
            isSelected = true,
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
                    congestionScore = 0.0,
                ),
            onClick = {},
        )
    }
}

@Suppress("MagicNumber")
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
                    congestionDbm = -95,
                )
        )
    }
}

@Suppress("MagicNumber")
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
                    interferingNetworks = listOf(WifiNetwork("Overlapping-Wifi", 2412, -45)),
                    congestionDbm = -44,
                )
        )
    }
}
