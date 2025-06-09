package com.zeppelin.app.watchLink.data

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class WatchScanner(private val context: Context) {

    companion object {
        private const val TAG = "WatchScanner"
    }

    private val bluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
    private val bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner

    @SuppressLint("MissingPermission")
    val foundDevicesFlow: Flow<BluetoothDevice> = callbackFlow {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "BLUETOOTH_SCAN permission not granted.")
            close(IllegalStateException("BLUETOOTH_SCAN permission not granted."))
            return@callbackFlow
        }

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "BLUETOOTH_CONNECT permission not granted.")
            close(IllegalStateException("BLUETOOTH_CONNECT permission not granted."))
            return@callbackFlow
        }

        val scanCallback = object : ScanCallback() {
            @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                super.onScanResult(callbackType, result)
                if (result.device.name != null) {
                    Log.d(TAG, "Device found: ${result.device.name} - ${result.device.address}")
                    trySend(result.device)
                }
            }

            override fun onScanFailed(errorCode: Int) {
                super.onScanFailed(errorCode)
                Log.e(TAG, "Bluetooth scan failed with error code: $errorCode")
                close(IllegalStateException("Scan failed with error code: $errorCode"))
            }
        }

        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        Log.d(TAG, "Starting Bluetooth LE scan...")
        bluetoothLeScanner?.startScan(null, scanSettings, scanCallback)

        awaitClose {
            Log.d(TAG, "Stopping Bluetooth LE scan.")
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                bluetoothLeScanner?.stopScan(scanCallback)
            }
        }
    }

    fun isBluetoothEnabled(): Boolean = bluetoothAdapter?.isEnabled == true
}