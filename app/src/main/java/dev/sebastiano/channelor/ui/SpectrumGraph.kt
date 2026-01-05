package dev.sebastiano.channelor.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import dev.sebastiano.channelor.domain.WifiNetwork
import dev.sebastiano.channelor.domain.ZigbeeChannelCongestion
import dev.sebastiano.channelor.ui.theme.ChannelorTheme
import kotlin.math.exp

@Composable
fun SpectrumGraph(
  wifiScanResults: List<WifiNetwork>,
  zigbeeCongestion: List<ZigbeeChannelCongestion>,
  top3ChannelNumbers: Set<Int>,
  modifier: Modifier = Modifier,
) {
  val textMeasurer = rememberTextMeasurer()
  val axisColor = MaterialTheme.colorScheme.outline
  val wifiStrokeColor = MaterialTheme.colorScheme.tertiary
  val wifiFillColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
  val zigbeeTop3Color = MaterialTheme.colorScheme.primary
  val zigbeeRecommendedColor = MaterialTheme.colorScheme.secondary
  val zigbeeRegularColor = MaterialTheme.colorScheme.outlineVariant
  val labelBackgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)

  Canvas(modifier = modifier) {
    val width = size.width
    val height = size.height

    // X-Axis: 2400 to 2483.5 MHz
    val minFreq = 2400f
    val maxFreq = 2483.5f
    val freqRange = maxFreq - minFreq

    // Y-Axis: -100 to -30 dBm
    val minRssi = -100f
    val maxRssi = -30f
    val rssiRange = maxRssi - minRssi

    fun freqToX(freq: Int): Float {
      return ((freq - minFreq) / freqRange) * width
    }

    fun freqToX(freq: Float): Float {
      return ((freq - minFreq) / freqRange) * width
    }

    fun rssiToY(rssi: Int): Float {
      val clampedRssi = rssi.coerceIn(minRssi.toInt(), maxRssi.toInt())
      return height - ((clampedRssi - minRssi) / rssiRange) * height
    }

    // Draw Grid
    drawGrid(width, height, axisColor)

    // Draw Wi-Fi Signals - optimized with fewer steps and simpler rendering
    wifiScanResults.forEach { network ->
      val centerFreq = network.frequency
      val rssi = network.rssi

      val path = Path()
      val startFreq = centerFreq - 11f
      val endFreq = centerFreq + 11f

      // Increased steps from 10 to 40 for smoother curves
      val steps = 20
      val stepSize = (endFreq - startFreq) / steps

      val peakY = rssiToY(rssi)
      val baseY = rssiToY(-100) // Bottom

      path.moveTo(freqToX(startFreq), baseY)

      // Pre-calculate constants outside loop
      val sigma = 4.0
      val twoSigmaSquared = 2 * sigma * sigma

      for (i in 0..steps) {
        val f = startFreq + i * stepSize
        val x = freqToX(f)

        // Optimized Gaussian calculation
        val diff = f - centerFreq
        val shapeFactor = exp(-(diff * diff) / twoSigmaSquared).toFloat()

        val y = baseY - (baseY - peakY) * shapeFactor

        path.lineTo(x, y)
      }

      path.lineTo(freqToX(endFreq), baseY)
      path.close()

      // Single fill with reduced gradient complexity
      drawPath(path = path, color = wifiFillColor)

      // Stroke outline
      drawPath(path = path, color = wifiStrokeColor, style = Stroke(width = 1.5.dp.toPx()))
    }

    // Draw Zigbee Markers
    zigbeeCongestion.forEach { zigbee ->
      val x = freqToX(zigbee.centerFrequency)
      val isTop3 = zigbee.channelNumber in top3ChannelNumbers

      val color =
        when {
          isTop3 -> zigbeeTop3Color
          zigbee.isZllRecommended -> zigbeeRecommendedColor
          else -> zigbeeRegularColor.copy(alpha = 0.5f)
        }

      // Channel Number
      val textLayout =
        textMeasurer.measure(
          text = "${zigbee.channelNumber}",
          style =
            TextStyle(
              color = color,
              fontSize = TextUnit.Unspecified,
              fontWeight = if (isTop3) FontWeight.Bold else FontWeight.Normal,
            ),
        )

      val labelX = x - textLayout.size.width / 2
      val labelY = 5f

      val strokeWidth = if (isTop3) 3.dp.toPx() else 1.dp.toPx()

      // Vertical Line
      drawLine(
        color = color,
        start = Offset(x, labelY + textLayout.size.height + 4f),
        end = Offset(x, height),
        strokeWidth = strokeWidth,
      )

      // Background for legibility
      drawRect(
        color = labelBackgroundColor,
        topLeft = Offset(labelX - 4f, labelY - 2f),
        size = Size(textLayout.size.width.toFloat() + 8f, textLayout.size.height.toFloat() + 4f),
      )

      drawText(textLayoutResult = textLayout, topLeft = Offset(labelX, labelY))
    }
  }
}

private fun DrawScope.drawGrid(width: Float, height: Float, color: Color) {
  // X-Axis Labels (Frequency) every 20MHz
  // Y-Axis Labels (RSSI) every 20dBm

  // Simple baseline
  drawLine(color = color, start = Offset(0f, height), end = Offset(width, height), strokeWidth = 2f)
}

@Preview(showBackground = true)
@Composable
fun SpectrumGraphPreview() {
  val mockWifi =
    listOf(
      WifiNetwork("Net 1", 2412, -40),
      WifiNetwork("Net 2", 2437, -65),
      WifiNetwork("Net 3", 2462, -50),
    )

  val mockZigbee =
    (11..26).map {
      ZigbeeChannelCongestion(
        it,
        2405 + 5 * (it - 11),
        if (it in listOf(15, 20, 25)) 0.0 else 100.0,
      )
    }

  dev.sebastiano.channelor.ui.theme.ChannelorTheme {
    Surface {
      Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        SpectrumGraph(
          wifiScanResults = mockWifi,
          zigbeeCongestion = mockZigbee,
          top3ChannelNumbers = setOf(15, 20, 25),
          modifier = Modifier.fillMaxSize(),
        )
      }
    }
  }
}
