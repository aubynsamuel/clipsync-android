package com.aubynsamuel.clipsync

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat

//import androidx.lifecycle.viewmodel.compose.viewModel

@RequiresApi(Build.VERSION_CODES.S)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClipSyncApp(bluetoothAdapter: BluetoothAdapter?) {
    val context = LocalContext.current
    val viewModel: BluetoothViewModel = BluetoothViewModel(
        context = context
    )

    LaunchedEffect(bluetoothAdapter) {
        bluetoothAdapter?.let { viewModel.initialize(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ClipSync") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(onClick = { viewModel.startDiscovery() }) {
                        Icon(Icons.Default.Search, contentDescription = "Scan for devices")
                    }
                    IconButton(onClick = { viewModel.toggleVisibility() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Make discoverable")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Status Section
            StatusSection(viewModel.connectionStatus)

            Spacer(modifier = Modifier.height(16.dp))

            // Paired Devices Section
            Text(
                text = "Paired Devices",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            if (viewModel.pairedDevices.isEmpty()) {
                Text(
                    text = "No paired devices",
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = Color.Gray
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    items(viewModel.pairedDevices) { device ->
                        DeviceItem(
                            device = device,
                            isConnected = viewModel.connectedDevice == device,
                            onDeviceClick = { viewModel.connectToDevice(device) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Discovered Devices Section
            Text(
                text = "Available Devices",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            if (viewModel.isScanning) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(16.dp)
                )
            } else if (viewModel.discoveredDevices.isEmpty()) {
                Text(
                    text = "No devices found",
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = Color.Gray
                )
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(viewModel.discoveredDevices) { device ->
                    DeviceItem(
                        device = device,
                        isConnected = viewModel.connectedDevice == device,
                        onDeviceClick = { viewModel.connectToDevice(device) }
                    )
                }
            }

            // Last Synced Text
            SyncStatusSection(
                lastSyncTime = viewModel.lastSyncTime,
                lastSyncedText = viewModel.lastClipboardText
            )
        }
    }
}

@Composable
fun StatusSection(status: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                status.contains("Connected") -> Color(0xFFDCEDC8)
                status.contains("Connecting") -> Color(0xFFFFE082)
                status.contains("Error") -> Color(0xFFFFCDD2)
                else -> Color(0xFFE1F5FE)
            }
        )
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(16.dp),
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )
    }
}

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun DeviceItem(
    device: BluetoothDevice,
    isConnected: Boolean,
    onDeviceClick: () -> Unit
) {
    val context = LocalContext.current
    val bluetoothPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
        onResult = {}
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onDeviceClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isConnected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    bluetoothPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)

                }
                Text(
                    text = device.name ?: "Unknown Device",
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = device.address,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            if (isConnected) {
                Text(
                    text = "Connected",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SyncStatusSection(lastSyncTime: String, lastSyncedText: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = "Last Sync: $lastSyncTime",
            fontSize = 14.sp,
            color = Color.Gray
        )

        if (lastSyncedText.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Last Clipboard: ${
                    if (lastSyncedText.length > 30)
                        lastSyncedText.take(30) + "..."
                    else
                        lastSyncedText
                }",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}