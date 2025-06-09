package com.zeppelin.app.service.wearCommunication

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class WatchProximityMonitor( private val context: Context
) {

    companion object {
        private const val TAG = "WatchProximityMonitor"
        private const val RSSI_READ_INTERVAL_MS = 5000L // Read RSSI every 5 seconds
    }

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothGatt: BluetoothGatt? = null
    private var watchDeviceAddress: String? = null // Store the MAC address of the target watch

    private val handler = Handler(Looper.getMainLooper())
    private val rssiRunnable = object : Runnable {
        override fun run() {
            requestRssi()
            handler.postDelayed(this, RSSI_READ_INTERVAL_MS)
        }
    }
    private val _currentRssi = MutableStateFlow<Int?>(null)
    val currentRssi: StateFlow<Int?> = _currentRssi.asStateFlow()

    init {
        val bluetoothManager =
            context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
    }

    fun startMonitoring(targetDeviceAddress: String) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG, "BLUETOOTH_CONNECT permission not granted.")
           _currentRssi.value = null
            return
        }
        if (bluetoothAdapter == null || !bluetoothAdapter!!.isEnabled) {
            Log.e(TAG, "Bluetooth not enabled or not available.")
            _currentRssi.value = null
            return
        }

        watchDeviceAddress = targetDeviceAddress
        val device = bluetoothAdapter!!.getRemoteDevice(watchDeviceAddress)

        if (device == null) {
            Log.e(TAG, "Watch device with address $watchDeviceAddress not found.")
            _currentRssi.value = null
            return
        }

        Log.d(TAG, "Attempting to connect GATT to watch: ${device.name} (${device.address})")
        // Close previous GATT connection if any
        bluetoothGatt?.close()
        bluetoothGatt =
            device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
    }

    fun stopMonitoring() {
        handler.removeCallbacks(rssiRunnable)
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Log permission issue, but still try to close
        }
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
        Log.d(TAG, "Stopped proximity monitoring.")
        _currentRssi.value = null
    }

    private fun requestRssi() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w(TAG, "BLUETOOTH_CONNECT permission missing for RSSI read.")
            return
        }
        if (bluetoothGatt != null &&
            (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager)
                .getConnectionState(
                    bluetoothGatt?.device,
                    BluetoothProfile.GATT
                ) == BluetoothProfile.STATE_CONNECTED
        ) {
            bluetoothGatt?.readRemoteRssi()
        } else {
            Log.w(TAG, "Cannot read RSSI: GATT not connected or bluetoothGatt is null.")
            // Optionally try to reconnect or handle disconnection
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.i(TAG, "Connected to watch GATT server: ${gatt?.device?.address}")
                    // Start periodic RSSI reads
                    handler.removeCallbacks(rssiRunnable) // Remove old ones
                    handler.post(rssiRunnable) // Start new periodic reads
                }

                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.i(TAG, "Disconnected from watch GATT server: ${gatt?.device?.address}")
                    handler.removeCallbacks(rssiRunnable)
                    _currentRssi.value = null
                }
            }
        }

        override fun onReadRemoteRssi(gatt: BluetoothGatt?, rssi: Int, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Watch RSSI: $rssi dBm for device ${gatt?.device?.address}")
                _currentRssi.value = rssi
                if (rssi < -85) {
                    Log.i(TAG, "Watch is too far away (RSSI: $rssi dBm). Triggering TOO_FAR_EVENT.")
                } else {
                    Log.i(TAG, "Watch is within range (RSSI: $rssi dBm).")
                }
            } else {
                Log.w(TAG, "Failed to read RSSI from watch, status: $status")
                _currentRssi.value = null // Indicate RSSI read failure
            }
        }
    }
}