package com.zeppelin.app.screens.watchLink.ui

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeppelin.app.screens.watchLink.data.WatchLinkRepository
import com.zeppelin.app.watchLink.data.WatchScanner
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class PairingUiState(
    val isScanning: Boolean = false,
    val foundDevices: List<BluetoothDevice> = emptyList(),
    val pairedAddress: String? = null,
    val error: String? = null
)

class WatchLinkViewModel(
    private val watchScanner: WatchScanner,
    private val watchLinkRepository: WatchLinkRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PairingUiState())
    val uiState: StateFlow<PairingUiState> = _uiState.asStateFlow()

    private var scanJob: Job? = null

    init {
        // Observe the currently paired address from DataStore
        viewModelScope.launch {
            watchLinkRepository.watchAddress.collect { address ->
                _uiState.update { it.copy(pairedAddress = address) }
            }
        }
    }

    fun startScan() {
        if (_uiState.value.isScanning) return

        if (!watchScanner.isBluetoothEnabled()) {
            _uiState.update { it.copy(error = "Please enable Bluetooth.") }
            return
        }

        scanJob?.cancel() // Cancel any previous job
        val foundDevicesMap = mutableMapOf<String, BluetoothDevice>()

        scanJob = viewModelScope.launch {
            _uiState.update { it.copy(isScanning = true, foundDevices = emptyList(), error = null) }
            try {
                watchScanner.foundDevicesFlow.collect { device ->
                    foundDevicesMap[device.address] = device
                    _uiState.update { it.copy(foundDevices = foundDevicesMap.values.toList()) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isScanning = false) }
            }
        }
    }

    fun stopScan() {
        scanJob?.cancel()
        _uiState.update { it.copy(isScanning = false) }
    }

    fun pairWithDevice(device: BluetoothDevice) {
        stopScan()
        viewModelScope.launch {
            watchLinkRepository.saveWatchAddress(device.address)
        }
    }

    fun unpairDevice() {
        viewModelScope.launch {
            watchLinkRepository.clearWatchAddress()
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopScan() // Ensure scan is stopped when ViewModel is cleared
    }
}