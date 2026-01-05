package dev.sebastiano.channelor.data

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.util.Log
import androidx.core.app.ActivityCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.sebastiano.channelor.domain.WifiNetwork
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

interface WifiRepository {
    fun getWifiScanResults(): Flow<List<WifiNetwork>>

    suspend fun triggerScan(): Boolean
}

@Singleton
class WifiRepositoryImpl
@Inject
constructor(
    @ApplicationContext private val context: Context,
    private val wifiManager: WifiManager,
) : WifiRepository {

    override fun getWifiScanResults(): Flow<List<WifiNetwork>> = callbackFlow {
        val receiver =
            object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    if (intent.action == WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) {
                        val success =
                            intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
                        if (success) {
                            try {
                                if (
                                    ActivityCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                    ) == PackageManager.PERMISSION_GRANTED
                                ) {
                                    val results = wifiManager.scanResults
                                    val domainResults =
                                        results
                                            .filter { is24GHz(it.frequency) }
                                            .map {
                                                WifiNetwork(it.SSID ?: "", it.frequency, it.level)
                                            }
                                    Log.d(
                                        "WifiRepository",
                                        "Scan results: ${domainResults.size} networks found (2.4GHz)",
                                    )
                                    domainResults.forEach {
                                        Log.d(
                                            "WifiRepository",
                                            "  - SSID: ${it.ssid}, Freq: ${it.frequency}, RSSI: ${it.rssi}",
                                        )
                                    }
                                    trySend(domainResults)
                                } else {
                                    // Permission not granted, maybe send empty list or error?
                                    // For now, let's just log or ignore.
                                }
                            } catch (e: SecurityException) {
                                Log.e(
                                    "WifiRepository",
                                    "Security exception when getting scan results",
                                    e,
                                )
                            }
                        }
                    }
                }
            }

        val intentFilter = IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        context.registerReceiver(receiver, intentFilter)

        // Emit initial results if available
        if (
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
        ) {
            val initialResults = wifiManager.scanResults
            val domainInitial =
                initialResults
                    .filter { is24GHz(it.frequency) }
                    .map { WifiNetwork(it.SSID ?: "", it.frequency, it.level) }
            Log.d(
                "WifiRepository",
                "Initial scan results: ${domainInitial.size} networks found (2.4GHz)",
            )
            trySend(domainInitial)
        }

        awaitClose { context.unregisterReceiver(receiver) }
    }

    override suspend fun triggerScan(): Boolean {
        if (
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }
        return wifiManager.startScan()
    }

    private fun is24GHz(frequency: Int): Boolean {
        return frequency in MIN_FREQ_24GHZ..MAX_FREQ_24GHZ
    }

    companion object {
        private const val MIN_FREQ_24GHZ = 2400
        private const val MAX_FREQ_24GHZ = 2484
    }
}
