package dev.sebastiano.channelor.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import dev.sebastiano.channelor.domain.WifiNetwork
import dev.sebastiano.channelor.domain.ZigbeeChannelCongestion
import dev.sebastiano.channelor.ui.theme.ChannelorTheme
import kotlin.math.exp

/** Configuration for the spectrum graph display. */
private data class GraphConfig(
        val minFreq: Float = 2400f,
        val maxFreq: Float = 2483.5f,
        val minRssi: Float = -100f,
        val maxRssi: Float = -30f,
        val wifiBandwidthMhz: Float = 22f, // WiFi signal spans ±11 MHz from center
        val wifiCurveSteps: Int = 20, // Number of points to render WiFi signal curve
        val wifiCurveSigma: Double = 4.0, // Gaussian curve width parameter
        val zigbeeLabelTopMargin: Dp = 5.dp,
        val labelBackgroundPadding: Dp = 4.dp,
        val wifiStrokeWidth: Dp = 1.5.dp,
        val zigbeeRegularStrokeWidth: Dp = 1.dp,
        val zigbeeTop3StrokeWidth: Dp = 3.dp,
        val gridLineWidth: Float = 2f,
) {
  val freqRange: Float = maxFreq - minFreq
  val rssiRange: Float = maxRssi - minRssi
}

/** Maps frequency and RSSI values to canvas coordinates. */
private class CoordinateMapper(
        private val config: GraphConfig,
        private val canvasWidth: Float,
        private val canvasHeight: Float,
        private val topPadding: Float = 0f,
) {
  fun freqToX(freq: Float): Float = ((freq - config.minFreq) / config.freqRange) * canvasWidth

  fun freqToX(freq: Int): Float = freqToX(freq.toFloat())

  fun rssiToY(rssi: Int): Float {
    val clampedRssi = rssi.coerceIn(config.minRssi.toInt(), config.maxRssi.toInt())
    val availableHeight = canvasHeight - topPadding
    return canvasHeight - ((clampedRssi - config.minRssi) / config.rssiRange) * availableHeight
  }
}

/** Color scheme for the spectrum graph. */
private data class GraphColors(
        val axis: Color,
        val wifiStroke: Color,
        val wifiFill: Color,
        val zigbeeTop3: Color,
        val zigbeeRecommended: Color,
        val zigbeeRegular: Color,
        val labelBackground: Color,
)

@Composable
fun SpectrumGraph(
        wifiScanResults: List<WifiNetwork>,
        zigbeeCongestion: List<ZigbeeChannelCongestion>,
        top3ChannelNumbers: Set<Int>,
        modifier: Modifier = Modifier,
) {
  val textMeasurer = rememberTextMeasurer()
  val config = remember { GraphConfig() }

  val colors =
          GraphColors(
                  axis = MaterialTheme.colorScheme.outline,
                  wifiStroke = MaterialTheme.colorScheme.tertiary,
                  wifiFill = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f),
                  zigbeeTop3 = MaterialTheme.colorScheme.primary,
                  zigbeeRecommended = MaterialTheme.colorScheme.secondary,
                  zigbeeRegular = MaterialTheme.colorScheme.outlineVariant,
                  labelBackground = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
          )

  Canvas(modifier = modifier) {
    // Calculate top padding once to ensure WiFi signals don't overlap with labels
    // We use a representative label to measure the height
    val sampleTextLayout =
            textMeasurer.measure(text = "88", style = TextStyle(fontWeight = FontWeight.Bold))
    val topPadding =
            config.zigbeeLabelTopMargin.toPx() +
                    sampleTextLayout.size.height +
                    config.labelBackgroundPadding.toPx() +
                    8.dp.toPx() // Extra breathing room

    val mapper = CoordinateMapper(config, size.width, size.height, topPadding)

    drawGrid(size.width, size.height, colors.axis, config)

    drawWifiSignals(wifiScanResults, mapper, config, colors)

    drawZigbeeMarkers(zigbeeCongestion, top3ChannelNumbers, mapper, config, colors, textMeasurer)
  }
}

/** Draws the WiFi signal curves on the canvas. */
private fun DrawScope.drawWifiSignals(
        networks: List<WifiNetwork>,
        mapper: CoordinateMapper,
        config: GraphConfig,
        colors: GraphColors,
) {
  // Pre-calculate Gaussian constants to avoid recalculation for each network
  val twoSigmaSquared = 2 * config.wifiCurveSigma * config.wifiCurveSigma
  val halfBandwidth = config.wifiBandwidthMhz / 2f
  val stepSize = config.wifiBandwidthMhz / config.wifiCurveSteps
  val baseY = mapper.rssiToY(-100) // Bottom of the graph

  networks.forEach { network ->
    val centerFreq = network.frequency.toFloat()
    val peakY = mapper.rssiToY(network.rssi)

    val path =
            buildWifiPath(
                    centerFreq,
                    peakY,
                    baseY,
                    halfBandwidth,
                    stepSize,
                    config.wifiCurveSteps,
                    twoSigmaSquared,
                    mapper,
            )

    // Draw filled area first, then stroke on top
    val brush =
            Brush.verticalGradient(
                    colors =
                            listOf(
                                    colors.wifiStroke.copy(alpha = 0.25f),
                                    colors.wifiFill.copy(alpha = 0.02f)
                            ),
                    startY = peakY,
                    endY = baseY
            )
    drawPath(path = path, brush = brush)
    drawPath(
            path = path,
            color = colors.wifiStroke.copy(alpha = 0.5f),
            style = Stroke(width = config.wifiStrokeWidth.toPx()),
    )
  }
}

/** Builds a Path representing a WiFi signal using a Gaussian curve. */
private fun buildWifiPath(
        centerFreq: Float,
        peakY: Float,
        baseY: Float,
        halfBandwidth: Float,
        stepSize: Float,
        steps: Int,
        twoSigmaSquared: Double,
        mapper: CoordinateMapper,
): Path {
  val path = Path()
  val startFreq = centerFreq - halfBandwidth
  val endFreq = centerFreq + halfBandwidth
  val yRange = baseY - peakY

  path.moveTo(mapper.freqToX(startFreq), baseY)

  for (i in 0..steps) {
    val freq = startFreq + i * stepSize
    val x = mapper.freqToX(freq)

    // Calculate Gaussian shape factor
    val freqDiff = freq - centerFreq
    val shapeFactor = exp(-(freqDiff * freqDiff) / twoSigmaSquared).toFloat()
    val y = baseY - yRange * shapeFactor

    path.lineTo(x, y)
  }

  path.lineTo(mapper.freqToX(endFreq), baseY)
  path.close()

  return path
}

/** Draws Zigbee channel markers with labels. */
private fun DrawScope.drawZigbeeMarkers(
        channels: List<ZigbeeChannelCongestion>,
        top3Channels: Set<Int>,
        mapper: CoordinateMapper,
        config: GraphConfig,
        colors: GraphColors,
        textMeasurer: TextMeasurer,
) {
  val labelTopMargin = config.zigbeeLabelTopMargin.toPx()
  val backgroundPadding = config.labelBackgroundPadding.toPx()

  channels.forEach { channel ->
    val x = mapper.freqToX(channel.centerFrequency)
    val isTop3 = channel.channelNumber in top3Channels

    val color = getZigbeeColor(isTop3, channel.isZllRecommended, colors)
    val strokeWidth =
            if (isTop3) config.zigbeeTop3StrokeWidth.toPx()
            else config.zigbeeRegularStrokeWidth.toPx()

    // Measure and draw channel number label
    val textLayout =
            textMeasurer.measure(
                    text = channel.channelNumber.toString(),
                    style =
                            TextStyle(
                                    color = color,
                                    fontSize = TextUnit.Unspecified,
                                    fontWeight = if (isTop3) FontWeight.Bold else FontWeight.Normal,
                            ),
            )

    val labelX = x - textLayout.size.width / 2f
    val lineStartY = labelTopMargin + textLayout.size.height + backgroundPadding

    // Draw vertical line
    drawLine(
            color = color,
            start = Offset(x, lineStartY),
            end = Offset(x, size.height),
            strokeWidth = strokeWidth,
    )

    // Draw label background for readability
    drawRoundRect(
            color = colors.labelBackground,
            topLeft = Offset(labelX - backgroundPadding, labelTopMargin - 2f),
            size =
                    Size(
                            textLayout.size.width + backgroundPadding * 2f,
                            textLayout.size.height + 4f
                    ),
            cornerRadius = CornerRadius(4.dp.toPx())
    )

    // Draw label text
    drawText(textLayoutResult = textLayout, topLeft = Offset(labelX, labelTopMargin))
  }
}

/** Determines the appropriate color for a Zigbee channel marker. */
private fun getZigbeeColor(isTop3: Boolean, isRecommended: Boolean, colors: GraphColors): Color =
        when {
          isTop3 -> colors.zigbeeTop3
          isRecommended -> colors.zigbeeRecommended
          else -> colors.zigbeeRegular.copy(alpha = 0.5f)
        }

/** Draws the grid lines and axes. */
private fun DrawScope.drawGrid(width: Float, height: Float, color: Color, config: GraphConfig) {
  // Draw baseline (X-axis)
  drawLine(
          color = color,
          start = Offset(0f, height),
          end = Offset(width, height),
          strokeWidth = config.gridLineWidth,
  )
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(
        name = "Dark Mode",
        showBackground = true,
        uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
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
                    channelNumber = it,
                    centerFrequency = 2405 + 5 * (it - 11),
                    congestionScore = if (it in listOf(15, 20, 25)) 0.0 else 100.0,
                    isZllRecommended = it in listOf(15, 20, 25),
            )
          }

  ChannelorTheme {
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
