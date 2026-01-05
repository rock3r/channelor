package dev.sebastiano.channelor.domain

data class WifiNetwork(val ssid: String, val frequency: Int, val rssi: Int)

data class ZigbeeChannelCongestion(
    val channelNumber: Int,
    val centerFrequency: Int,
    val congestionScore: Double,
    val isZllRecommended: Boolean = false,
    val isWarning: Boolean = false,
    val pros: List<String> = emptyList(),
    val cons: List<String> = emptyList(),
)
