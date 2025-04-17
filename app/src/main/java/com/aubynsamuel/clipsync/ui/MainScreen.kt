package com.aubynsamuel.clipsync.ui

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat

/** Define the parameters as functions:
 * - startBluetoothService: takes a set of selected device addresses.
 * - loadPairedDevices: returns a set of BluetoothDevice objects.
 * - launchShareActivity: given a Context, launches the share action.
 **/
@Composable
fun MainScreen(
    startBluetoothService: (selectedDeviceAddresses: Set<String>) -> Unit,
    loadPairedDevices: () -> Set<BluetoothDevice>,
    launchShareActivity: (Context) -> Unit,
) {
    var pairedDevices by remember { mutableStateOf<Set<BluetoothDevice>>(emptySet()) }
    var selectedDeviceAddresses by remember { mutableStateOf<Set<String>>(emptySet()) }
    val context = androidx.compose.ui.platform.LocalContext.current

    // Load paired devices when the composable is launched.
    LaunchedEffect(Unit) {
        pairedDevices = loadPairedDevices()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "ClipSync",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = "Select devices to share clipboard with:",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(pairedDevices.toList()) { device ->
                val deviceName = if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    "Unknown Device"
                } else {
                    device.name ?: "Unknown Device"
                }
                val deviceAddress = device.address
                // Check whether the device is selected.
                val isSelected = selectedDeviceAddresses.contains(deviceAddress)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { checked ->
                            selectedDeviceAddresses = if (checked) {
                                selectedDeviceAddresses + deviceAddress
                            } else {
                                selectedDeviceAddresses - deviceAddress
                            }
                        }
                    )

                    Column(modifier = Modifier.padding(start = 8.dp)) {
                        Text(text = deviceName)
                        Text(
                            text = deviceAddress,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        Button(
            onClick = { startBluetoothService(selectedDeviceAddresses) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            enabled = selectedDeviceAddresses.isNotEmpty()
        ) {
            Text("Start Clipboard Sharing")
        }

        Button(
            onClick = { launchShareActivity(context) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text("Share Clipboard")
        }
    }
}
