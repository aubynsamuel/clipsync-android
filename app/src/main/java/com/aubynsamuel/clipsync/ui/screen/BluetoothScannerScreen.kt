package com.aubynsamuel.clipsync.ui.screen

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.aubynsamuel.clipsync.ui.navigation.safePopBackStack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BluetoothScannerScreen(
    navController: NavController,
    discoveredDevices: List<BluetoothDevice>,
    isScanning: Boolean,
    bluetoothEnabled: Boolean,
    onStartScan: () -> Unit,
    onPairDevice: (BluetoothDevice) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors()
                    .copy(
                        containerColor = Color.Transparent,
                    ),
                title = {
                    Text(
                        text = "Pair New Device",
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onPrimaryContainer
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.safePopBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = colorScheme.onPrimaryContainer
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            if (!bluetoothEnabled) {
                StatusCard(
                    message = "Bluetooth is disabled. Please enable Bluetooth to scan for devices.",
                    isError = true
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Available Devices ${discoveredDevices.size}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Button(
                    onClick = onStartScan,
                    enabled = !isScanning && bluetoothEnabled,
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                ) {
                    Row(
                        modifier = Modifier,
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (isScanning) "Scanning..." else "Scan")
                        if (isScanning) {
                            Spacer(modifier = Modifier.width(5.dp))
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            if (discoveredDevices.isEmpty() && !isScanning) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = colorScheme.surfaceVariant
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No devices found. Tap 'Scan' to search for nearby devices.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Devices List
            LazyColumn {
                items(discoveredDevices) { device ->
                    DeviceItem(
                        device = device,
                        onPairClick = { onPairDevice(device) }
                    )
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceItem(
    device: BluetoothDevice,
    onPairClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onPairClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(Icons.Default.Bluetooth, contentDescription = "")
            Text(
                text = device.name ?: "Unknown Device",
                maxLines = 1,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun StatusCard(
    message: String,
    isError: Boolean = false,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 5.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isError) {
                colorScheme.errorContainer
            } else {
                colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(5.dp)
        ) {
            Text(
                text = message,
                color = if (isError) {
                    colorScheme.onErrorContainer
                } else {
                    colorScheme.onSurfaceVariant
                }
            )

            if (actionText != null && onAction != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onAction,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(actionText)
                }
            }
        }
    }
}