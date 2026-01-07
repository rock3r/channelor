@file:Suppress("FunctionNaming")

package dev.sebastiano.channelor.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ThumbDown
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.sebastiano.channelor.R
import dev.sebastiano.channelor.domain.ZigbeeChannelCongestion

@Composable
fun ChannelDetailsContent(channel: ZigbeeChannelCongestion, modifier: Modifier = Modifier) {
    Column(
        modifier =
            modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(top = 8.dp, bottom = 48.dp)
    ) {
        ChannelDetailsHeader(channel)

        Spacer(modifier = Modifier.height(24.dp))

        if (channel.pros.isNotEmpty()) {
            ChannelProsSection(channel.pros)
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (channel.cons.isNotEmpty()) {
            ChannelConsSection(channel.cons)
            Spacer(modifier = Modifier.height(24.dp))
        }

        InterferingWifiSection(channel)
    }
}

@Composable
private fun ChannelDetailsHeader(channel: ZigbeeChannelCongestion) {
    Text(
        text = stringResource(R.string.channel_details_title, channel.channelNumber),
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
    )
    Text(
        text = stringResource(R.string.center_frequency_format, channel.centerFrequency),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.secondary,
    )
    Text(
        text =
            stringResource(R.string.total_interference) +
                ": " +
                (channel.congestionDbm?.let { "$it dBm" }
                    ?: stringResource(R.string.not_available)),
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.tertiary,
    )
}

@Composable
private fun ChannelProsSection(pros: List<Int>) {
    Text(
        text = stringResource(R.string.pros),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
    )
    Spacer(modifier = Modifier.height(8.dp))
    pros.forEach { proResId ->
        Row(
            modifier = Modifier.padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Rounded.ThumbUp,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = stringResource(proResId), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun ChannelConsSection(cons: List<Int>) {
    Text(
        text = stringResource(R.string.cons),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.error,
    )
    Spacer(modifier = Modifier.height(8.dp))
    cons.forEach { conResId ->
        Row(
            modifier = Modifier.padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Rounded.ThumbDown,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.error,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = stringResource(conResId), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun InterferingWifiSection(channel: ZigbeeChannelCongestion) {
    Text(
        text = stringResource(R.string.interfering_wifi_networks),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.tertiary,
    )
    Spacer(modifier = Modifier.height(8.dp))

    if (channel.interferingNetworks.isEmpty()) {
        Text(
            text = stringResource(R.string.no_interfering_wifi),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    } else {
        channel.interferingNetworks
            .sortedByDescending { it.rssi }
            .forEach { wifi ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Wifi,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.tertiary,
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.wifi_signal_format, wifi.ssid, wifi.rssi),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
    }
}
