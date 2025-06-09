package com.zeppelin.app.screens.watchLink.ui

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.zeppelin.app.R
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PairingScreen(
    modifier: Modifier = Modifier,
    viewModel: WatchLinkViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    val permissions =
        listOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
    val permissionState = rememberMultiplePermissionsState(permissions)

    LaunchedEffect (key1 = true) {
        if (!permissionState.allPermissionsGranted) {
            permissionState.launchMultiplePermissionRequest()
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.link_your_watch), style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        // Section to show currently paired device
        if (uiState.pairedAddress != null) {
            Text(stringResource(R.string.paired_device, uiState.pairedAddress?:""))
            Button(onClick = { viewModel.unpairDevice() }) {
                Text(stringResource(R.string.unpair))
            }
        } else {
            Text(stringResource(R.string.no_device_paired))
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        // Section for scanning
        if (uiState.isScanning) {
            CircularProgressIndicator()
            Button(onClick = { viewModel.stopScan() }, modifier = Modifier.padding(top = 8.dp)) {
                Text("Stop Scan")
            }
        } else {
            Button(
                onClick = {
                    // Double-check permissions before starting the scan
                    if (permissionState.allPermissionsGranted) {
                        viewModel.startScan()
                    } else {
                        // If permissions were denied, this will re-trigger the request
                        permissionState.launchMultiplePermissionRequest()
                    }
                },
                // The button is enabled only if permissions are granted
                enabled = permissionState.allPermissionsGranted
            ) {
                Text(stringResource(R.string.scan_for_devices))
            }
            if (!permissionState.allPermissionsGranted) {
                Text(
                    stringResource(R.string.bluetooth_permissions_are_required_to_scan_for_devices),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        if (uiState.error != null) {
            Text(uiState.error!!, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // List of found devices
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            if (uiState.foundDevices.isEmpty()) {
                item {
                    Text(stringResource(R.string.no_devices_found))
                }
            } else {
                item {
                    Text(stringResource(R.string.found_devices), style = MaterialTheme.typography.titleMedium)
                }
            }
            items(uiState.foundDevices.size) { index ->
                val device = uiState.foundDevices[index]
                if (index > 0) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                }

                DeviceItem(device = device, onClick = { viewModel.pairWithDevice(device) })
            }
        }
    }
}


@SuppressLint("MissingPermission")
@Composable
fun DeviceItem(device: BluetoothDevice, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Note: Accessing device.name requires BLUETOOTH_CONNECT permission,
            Text(text = device.name ?: stringResource(R.string.unknown_device), style = MaterialTheme.typography.titleMedium)
            Text(text = device.address, style = MaterialTheme.typography.bodyMedium)
        }
    }
}