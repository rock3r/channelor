package dev.sebastiano.channelor.domain

import android.net.wifi.ScanResult
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.pow

data class ZigbeeChannelCongestion(
    val channelNumber: Int,
    val centerFrequency: Int,
    val congestionScore: Double,
    val isZllRecommended: Boolean = false,
    val annotation: String? = null,
    val isWarning: Boolean = false
)

class ZigbeeAnalyzer @Inject constructor() {

    // Zigbee channels 11-26
    // Center Freq = 2405 + 5 * (k - 11)
    private val zigbeeChannels = (11..26).map { channel ->
        channel to (2405 + 5 * (channel - 11))
    }

    fun analyzeCongestion(wifiScanResults: List<ScanResult>): List<ZigbeeChannelCongestion> {
        return zigbeeChannels.map { (channel, frequency) ->
            val score = calculateCongestionForChannel(frequency, wifiScanResults)
            ZigbeeChannelCongestion(
                channelNumber = channel,
                centerFrequency = frequency,
                congestionScore = score,
                isZllRecommended = channel in listOf(11, 15, 20, 25),
                annotation = getAnnotationForChannel(channel),
                isWarning = channel == 26
            )
        }
    }

    private fun getAnnotationForChannel(channel: Int): String? = when (channel) {
        11 -> "Usually crowded by Wi-Fi"
        26 -> "Problematic (low power, poor device support)"
        15, 20, 25 -> null
        else -> "Possible compatibility issues (Hue, IKEA, etc.)"
    }

    private fun calculateCongestionForChannel(zigbeeCenterFreq: Int, scanResults: List<ScanResult>): Double {
        var totalInterference = 0.0

        for (wifi in scanResults) {
            val wifiCenterFreq = wifi.frequency
            // Approximate bandwidth, usually 20MHz for 2.4GHz
            // ScanResult has channelWidth but it requires API 23+, let's assume 20MHz (approx 22MHz spectral mask)
            val wifiBandwidth = 22 // MHz

            // Distance between centers
            val dist = abs(wifiCenterFreq - zigbeeCenterFreq)

            // If the Zigbee channel is within the Wi-Fi channel's spread
            // Wi-Fi spreads +/- 11MHz from center.
            if (dist < wifiBandwidth / 2.0 + 2.0) { // +2 for Zigbee width safety
                // Calculate interference power based on RSSI
                // RSSI is negative dBm. -30 is strong, -90 is weak.
                // Convert to approx linear power or just shift it to positive range for scoring
                // Let's use 10^(RSSI/10) which is proportional to mW
                
                val power = 10.0.pow(wifi.level / 10.0)
                
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
}
