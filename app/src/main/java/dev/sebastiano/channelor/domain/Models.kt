package dev.sebastiano.channelor.domain

import androidx.annotation.StringRes

data class WifiNetwork(val ssid: String, val frequency: Int, val rssi: Int)

data class ZigbeeChannelCongestion(
        val channelNumber: Int,
        val centerFrequency: Int,
        val congestionScore: Double,
        val isZllRecommended: Boolean = false,
        val isWarning: Boolean = false,
        @StringRes val pros: List<Int> = emptyList(),
        @StringRes val cons: List<Int> = emptyList(),
)
