package dev.sebastiano.channelor.data

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

interface WifiRepository {
    fun getWifiScanResults(): Flow<List<ScanResult>>
    
    suspend fun triggerScan(): Boolean
}

@Singleton
class WifiRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val wifiManager: WifiManager
) : WifiRepository {

    override fun getWifiScanResults(): Flow<List<ScanResult>> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) {
                    val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
                    if (success) {
                        try {
                            if (ActivityCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                                ) == PackageManager.PERMISSION_GRANTED
                            ) {
                                val results = wifiManager.scanResults
                                val filteredResults = results.filter { is24GHz(it.frequency) }
                                Log.d("WifiRepository", "Scan results: ${filteredResults.size} networks found (2.4GHz)")
                                filteredResults.forEach { 
                                    Log.d("WifiRepository", "  - SSID: ${it.SSID}, Freq: ${it.frequency}, Level: ${it.level}")
                                }
                                trySend(filteredResults)
                            } else {
                                // Permission not granted, maybe send empty list or error?
                                // For now, let's just log or ignore.
                            }
                        } catch (e: SecurityException) {
                            // Handle exception
                        }
                    }
                }
            }
        }

        val intentFilter = IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        context.registerReceiver(receiver, intentFilter)
        
        // Emit initial results if available
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
             val initialResults = wifiManager.scanResults
             val filteredInitial = initialResults.filter { is24GHz(it.frequency) }
             Log.d("WifiRepository", "Initial scan results: ${filteredInitial.size} networks found (2.4GHz)")
             trySend(filteredInitial)
        }

        awaitClose {
            context.unregisterReceiver(receiver)
        }
    }

    override suspend fun triggerScan(): Boolean {
         if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }
        return wifiManager.startScan()
    }

    private fun is24GHz(frequency: Int): Boolean {
        return frequency in 2400..2484
    }
}
