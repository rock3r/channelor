@file:Suppress("FunctionNaming")

package dev.sebastiano.channelor.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.sebastiano.channelor.R

@Composable
fun EmptySpectrumView(isScanning: Boolean, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        if (isScanning) {
            CircularProgressIndicator(
                modifier = Modifier.size(INITIAL_SCAN_INDICATOR_SIZE),
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.performing_initial_scan),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
        } else {
            Text(
                text = stringResource(R.string.no_scan_data_available),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun ScanningFab(isScanning: Boolean, onScanClick: () -> Unit) {
    FloatingActionButton(
        onClick = onScanClick,
        containerColor =
            if (isScanning) MaterialTheme.colorScheme.surfaceVariant
            else MaterialTheme.colorScheme.primaryContainer,
    ) {
        if (isScanning) {
            CircularProgressIndicator(
                modifier = Modifier.size(SCANNING_INDICATOR_SIZE),
                strokeWidth = SCANNING_INDICATOR_STROKE_WIDTH,
                color = MaterialTheme.colorScheme.primary,
            )
        } else {
            Icon(Icons.Rounded.Refresh, contentDescription = stringResource(R.string.cd_scan))
        }
    }
}

@Composable
fun ScanningStatusCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors =
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Text(
            text = stringResource(R.string.scanning_wifi_networks),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}

private val SCANNING_INDICATOR_SIZE = 24.dp
private val SCANNING_INDICATOR_STROKE_WIDTH = 2.dp
private val INITIAL_SCAN_INDICATOR_SIZE = 48.dp
