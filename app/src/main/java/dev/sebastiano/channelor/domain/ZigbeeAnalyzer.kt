package dev.sebastiano.channelor.domain

import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.pow

class ZigbeeAnalyzer @Inject constructor() {

    // Zigbee channels 11-26
    // Center Freq = 2405 + 5 * (k - 11)
    private val zigbeeChannels =
        (ZIGBEE_MIN_CHANNEL..ZIGBEE_MAX_CHANNEL).map { channel ->
            channel to (ZIGBEE_START_FREQ + ZIGBEE_CHANNEL_SPACING * (channel - ZIGBEE_MIN_CHANNEL))
        }

    fun analyzeCongestion(wifiNetworks: List<WifiNetwork>): List<ZigbeeChannelCongestion> {
        return zigbeeChannels.map { (channel, frequency) ->
            val score = calculateCongestionForChannel(frequency, wifiNetworks)
            ZigbeeChannelCongestion(
                channelNumber = channel,
                centerFrequency = frequency,
                congestionScore = score,
                isZllRecommended = channel in ZLL_RECOMMENDED_CHANNELS,
                annotation = getAnnotationForChannel(channel),
                isWarning = channel == ZIGBEE_MAX_CHANNEL,
            )
        }
    }

    @Suppress("MagicNumber")
    private fun getAnnotationForChannel(channel: Int): String? =
        when (channel) {
            11 -> "Usually crowded by Wi-Fi"
            26 -> "Problematic (low power, poor device support)"
            15,
            20,
            25 -> null
            else -> "Possible compatibility issues (Hue, IKEA, etc.)"
        }

    private fun calculateCongestionForChannel(
        zigbeeCenterFreq: Int,
        scanResults: List<WifiNetwork>,
    ): Double {
        var totalInterference = 0.0

        for (wifi in scanResults) {
            val wifiCenterFreq = wifi.frequency
            // Approximate bandwidth, usually 20MHz for 2.4GHz
            // ScanResult has channelWidth but it requires API 23+, let's assume 20MHz (approx 22MHz
            // spectral mask)
            val wifiBandwidth = WIFI_BANDWIDTH_MHZ // MHz

            // Distance between centers
            val dist = abs(wifiCenterFreq - zigbeeCenterFreq)

            // If the Zigbee channel is within the Wi-Fi channel's spread
            // Wi-Fi spreads +/- 11MHz from center.
            if (
                dist < wifiBandwidth / 2.0 + ZIGBEE_WIDTH_SAFETY_MHZ
            ) { // +2 for Zigbee width safety
                // Calculate interference power based on RSSI
                // RSSI is negative dBm. -30 is strong, -90 is weak.
                // Convert to approx linear power or just shift it to positive range for scoring
                // Let's use 10^(RSSI/10) which is proportional to mW

                val power = POWER_BASE.pow(wifi.rssi / POWER_DIVISOR)

                // Weight by distance (closer to center = more interference)
                // Simple linear falloff? or just full power if overlapping.
                // Let's use a spectral mask approximation or just 1.0 if inside.
                // Let's keep it simple: 1.0

                totalInterference += power
            }
        }

        // Normalize or log scale the result for display?
        // Raw power sum is fine for comparison.
        return totalInterference
    }

    companion object {
        private const val ZIGBEE_MIN_CHANNEL = 11
        private const val ZIGBEE_MAX_CHANNEL = 26
        private const val ZIGBEE_START_FREQ = 2405
        private const val ZIGBEE_CHANNEL_SPACING = 5
        private val ZLL_RECOMMENDED_CHANNELS = listOf(11, 15, 20, 25)

        private const val WIFI_BANDWIDTH_MHZ = 22
        private const val ZIGBEE_WIDTH_SAFETY_MHZ = 2.0
        private const val POWER_BASE = 10.0
        private const val POWER_DIVISOR = 10.0
    }
}
