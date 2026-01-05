package dev.sebastiano.channelor.ui

import android.Manifest
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
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import dev.sebastiano.channelor.domain.ZigbeeChannelCongestion

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun DashboardScreen(viewModel: MainViewModel = hiltViewModel()) {
  val permissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
  val isScanning by viewModel.isScanning.collectAsState()

  LaunchedEffect(permissionState.status) {
    if (permissionState.status.isGranted) {
      viewModel.onPermissionResult(true)
    } else {
      permissionState.launchPermissionRequest()
    }
  }

  Scaffold(
      floatingActionButton = {
        FloatingActionButton(
            onClick = {
              if (permissionState.status.isGranted) {
                viewModel.triggerScan()
              } else {
                permissionState.launchPermissionRequest()
              }
            },
            containerColor =
                if (isScanning) MaterialTheme.colorScheme.surfaceVariant
                else MaterialTheme.colorScheme.primaryContainer,
        ) {
          if (isScanning) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary,
            )
          } else {
            Icon(Icons.Rounded.Refresh, contentDescription = "Scan")
          }
        }
      }
  ) { innerPadding ->
    Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
      Column(
          modifier =
              Modifier.fillMaxSize()
                  .padding(
                      top = 6.dp,
                      bottom = 80.dp // Avoid overlap with FAB
                  )
                  .padding(16.dp)
      ) {
        HeaderSection()

        Spacer(modifier = Modifier.height(24.dp))

        val zigbeeCongestion by viewModel.zigbeeCongestion.collectAsState()
        val wifiScanResults by viewModel.wifiScanResults.collectAsState()
        val top3Channels by viewModel.top3Channels.collectAsState()

        RecommendationSection(zigbeeCongestion)

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Spectrum Analysis",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth().weight(1f),
            colors =
                CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        ) {
          Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            if (wifiScanResults.isEmpty() && zigbeeCongestion.isEmpty()) {
              Column(
                  modifier = Modifier.align(Alignment.Center),
                  horizontalAlignment = Alignment.CenterHorizontally,
              ) {
                if (isScanning) {
                  CircularProgressIndicator(
                      modifier = Modifier.size(48.dp),
                      color = MaterialTheme.colorScheme.primary,
                  )
                  Spacer(modifier = Modifier.height(16.dp))
                  Text(
                      text = "Performing initial scan...",
                      style = MaterialTheme.typography.bodyLarge,
                      textAlign = TextAlign.Center,
                  )
                } else {
                  Text(
                      text = "No scan data available.\nTap the button to scan.",
                      textAlign = TextAlign.Center,
                  )
                }
              }
            } else {
              SpectrumGraph(
                  wifiScanResults = wifiScanResults,
                  zigbeeCongestion = zigbeeCongestion,
                  top3ChannelNumbers = top3Channels,
                  modifier = Modifier.fillMaxSize(),
              )
            }
          }
        }
      }

      if (isScanning) {
        LinearProgressIndicator(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
        )
      }

      if (isScanning) {
        Card(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        ) {
          Text(
              text = "Scanning Wi-Fi networks...",
              modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onSecondaryContainer,
          )
        }
      }
    }
  }
}

@Composable
fun HeaderSection() {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Box(
        modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.primary, CircleShape),
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

@Composable
fun RecommendationSection(zigbeeCongestion: List<ZigbeeChannelCongestion>) {
  Text(
      text = "Recommended Channels",
      style = MaterialTheme.typography.titleMedium,
      color = MaterialTheme.colorScheme.primary,
  )
  Spacer(modifier = Modifier.height(8.dp))

  val topChannels = zigbeeCongestion.sortedBy { it.congestionScore }.take(3)

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

@Composable
fun ChannelCard(channel: ZigbeeChannelCongestion) {
  val containerColor =
      if (channel.isZllRecommended) {
        MaterialTheme.colorScheme.primaryContainer
      } else if (channel.isWarning) {
        MaterialTheme.colorScheme.errorContainer
      } else {
        MaterialTheme.colorScheme.secondaryContainer
      }

  val onContainerColor =
      if (channel.isZllRecommended) {
        MaterialTheme.colorScheme.onPrimaryContainer
      } else if (channel.isWarning) {
        MaterialTheme.colorScheme.onErrorContainer
      } else {
        MaterialTheme.colorScheme.onSecondaryContainer
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

@Preview(showBackground = true)
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
