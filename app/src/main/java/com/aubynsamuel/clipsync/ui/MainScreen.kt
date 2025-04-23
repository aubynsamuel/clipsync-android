package com.aubynsamuel.clipsync.ui

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.aubynsamuel.clipsync.DarkMode
import com.aubynsamuel.clipsync.SelectedDevicesStore
import com.aubynsamuel.clipsync.ServiceLocator
import com.aubynsamuel.clipsync.ServiceLocator.bluetoothService
import com.aubynsamuel.clipsync.changeTheme
import com.aubynsamuel.clipsync.showToast
import com.aubynsamuel.clipsync.ui.components.DarkModeToggle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    startBluetoothService: (Set<String>) -> Unit,
    pairedDevices: Set<BluetoothDevice>,
    launchShareActivity: (Context) -> Unit,
    refresh: () -> Unit,
    stopBluetoothService: () -> Unit,
) {
    var selectedDeviceAddresses by remember { mutableStateOf<Set<String>>(emptySet()) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val colorScheme = MaterialTheme.colorScheme

    LaunchedEffect(selectedDeviceAddresses) {
        delay(300)
        SelectedDevicesStore.addresses = selectedDeviceAddresses.toTypedArray()
        delay(300)
        if (bluetoothService != null) {
            bluetoothService?.updateSelectedDevices()
        }
    }

    Scaffold(
        containerColor = colorScheme.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorScheme.primary)
                    .padding(horizontal = 10.dp, vertical = 8.dp)
                    .padding(top = 25.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "ClipSync",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 24.sp,
                        color = colorScheme.onPrimary
                    )
                    AnimatedVisibility(
                        selectedDeviceAddresses.isNotEmpty(),
                        modifier = Modifier.padding(horizontal = 10.dp)
                    ) {
                        Text(
                            text = selectedDeviceAddresses.count().toString(),
                            fontSize = 18.sp,
                            color = colorScheme.onPrimary, fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(15.dp)
                ) {
                    DarkModeToggle(
                        isDarkMode = DarkMode.isDarkMode.value,
                        onToggle = { changeTheme(context) },
                    )
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = colorScheme.onPrimary,
                        modifier = Modifier
                            .clickable {
                                refresh()
                                showToast("Paired Devices Refreshed", context)
                            }
                            .padding(end = 5.dp)
                            .size(28.dp)
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Select devices to share clipboard with",
                style = MaterialTheme.typography.bodyLarge,
                color = colorScheme.primary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(pairedDevices.toList()) { device ->
                    val name = if (ActivityCompat.checkSelfPermission(
                            context, Manifest.permission.BLUETOOTH_CONNECT
                        ) != PackageManager.PERMISSION_GRANTED
                    ) "Unknown Device" else device.name ?: "Unknown Device"
                    val address = device.address
                    val isSel = selectedDeviceAddresses.contains(address)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable(onClick = {
                                selectedDeviceAddresses =
                                    if (!isSel) selectedDeviceAddresses + address
                                    else selectedDeviceAddresses - address
                            }),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isSel,
                            onCheckedChange = { checked ->
                                selectedDeviceAddresses =
                                    if (checked) selectedDeviceAddresses + address
                                    else selectedDeviceAddresses - address
                            }
                        )
                        Column(modifier = Modifier.padding(start = 8.dp)) {
                            Text(text = name, color = colorScheme.onBackground)
                            Text(
                                text = address,
                                style = MaterialTheme.typography.bodySmall,
                                color = colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                Button(
                    onClick = {
                        if (ServiceLocator.serviceStarted.value)
                            stopBluetoothService()
                        else startBluetoothService(selectedDeviceAddresses)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(100.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (ServiceLocator.serviceStarted.value) colorScheme.error else colorScheme.primary,
                        contentColor = colorScheme.onPrimary
                    ),
                ) { Text(if (ServiceLocator.serviceStarted.value) "Stop" else "Start") }

                Button(
                    onClick = {
                        scope.launch {
                            if (!ServiceLocator.serviceStarted.value) {
                                startBluetoothService(selectedDeviceAddresses)
                                delay(500)
                            }
                            launchShareActivity(context)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(100.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.primary,
                        contentColor = colorScheme.onPrimary
                    ),
                    enabled = ServiceLocator.serviceStarted.value && selectedDeviceAddresses.isNotEmpty()
                ) { Text("Share") }
            }
        }
    }
}