package dev.sebastiano.channelor.domain

data class WifiNetwork(val ssid: String, val frequency: Int, val rssi: Int)

data class ZigbeeChannelCongestion(
        val channelNumber: Int,
        val centerFrequency: Int,
        val congestionScore: Double,
        val isZllRecommended: Boolean = false,
        val annotation: String? = null,
        val isWarning: Boolean = false
)
