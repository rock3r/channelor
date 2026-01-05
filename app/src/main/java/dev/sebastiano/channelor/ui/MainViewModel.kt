package dev.sebastiano.channelor.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.sebastiano.channelor.data.WifiRepository
import dev.sebastiano.channelor.domain.WifiNetwork
import dev.sebastiano.channelor.domain.ZigbeeAnalyzer
import dev.sebastiano.channelor.domain.ZigbeeChannelCongestion
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class MainViewModel
@Inject
constructor(
        private val wifiRepository: WifiRepository,
        private val zigbeeAnalyzer: ZigbeeAnalyzer
) : ViewModel() {

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning

    private val _permissionGranted = MutableStateFlow(false)

    // We observe the repository flow directly
    val wifiScanResults: StateFlow<List<WifiNetwork>> =
            wifiRepository
                    .getWifiScanResults()
                    .stateIn(
                            scope = viewModelScope,
                            started = SharingStarted.WhileSubscribed(5000),
                            initialValue = emptyList()
                    )

    val zigbeeCongestion: StateFlow<List<ZigbeeChannelCongestion>> =
            wifiScanResults
                    .combine(_permissionGranted) { results, granted ->
                        if (granted) {
                            zigbeeAnalyzer.analyzeCongestion(results)
                        } else {
                            emptyList()
                        }
                    }
                    .stateIn(
                            scope = viewModelScope,
                            started = SharingStarted.WhileSubscribed(5000),
                            initialValue = emptyList()
                    )

    // Pre-compute top 3 channels to avoid sorting in UI layer
    val top3Channels: StateFlow<Set<Int>> =
            zigbeeCongestion
                    .combine(_permissionGranted) { congestion, granted ->
                        if (granted && congestion.isNotEmpty()) {
                            congestion
                                    .sortedBy { it.congestionScore }
                                    .take(3)
                                    .map { it.channelNumber }
                                    .toSet()
                        } else {
                            emptySet()
                        }
                    }
                    .stateIn(
                            scope = viewModelScope,
                            started = SharingStarted.WhileSubscribed(5000),
                            initialValue = emptySet()
                    )

    fun onPermissionResult(granted: Boolean) {
        _permissionGranted.value = granted
        if (granted) {
            triggerScan()
        }
    }

    fun triggerScan() {
        viewModelScope.launch {
            _isScanning.value = true
            wifiRepository.triggerScan()
            // Reset scanning state after a delay or just leave it?
            // The scan results will come in via the flow.
            // triggerScan returns immediate boolean if scan started.
            // We can toggle isScanning off after some time or when results update?
            // For now let's just set it to false after a short delay to simulate "triggering"
            kotlinx.coroutines.delay(1000)
            _isScanning.value = false
        }
    }
}
