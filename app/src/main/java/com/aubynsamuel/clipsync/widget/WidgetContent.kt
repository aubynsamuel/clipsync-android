package com.aubynsamuel.clipsync.widget

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.glance.Button
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.aubynsamuel.clipsync.activities.ShareClipboardActivity
import com.aubynsamuel.clipsync.core.Essentials.isServiceBound
import com.aubynsamuel.clipsync.core.Essentials.selectedDeviceAddresses
import com.aubynsamuel.clipsync.core.Essentials.updateSelectedDevices
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun WidgetContent(
    context: Context,
    pairedDevices: Set<BluetoothDevice>,
    stopBluetoothService: () -> Unit,
    startBluetoothService: (Set<String>) -> Unit,
    bluetoothEnabled: Boolean
) {
    var selectedDeviceAddresses by rememberSaveable {
        mutableStateOf<Set<String>>(
            selectedDeviceAddresses.toSet()
        )
    }
    val scope = rememberCoroutineScope()

    LaunchedEffect(selectedDeviceAddresses) {
        delay(300)
        updateSelectedDevices(selectedDeviceAddresses.toTypedArray())
    }

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(
                MaterialTheme.colorScheme.primaryContainer,
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "ClipSync", style = TextStyle().copy(
                fontSize = 16.sp,
                fontWeight = androidx.glance.text.FontWeight.Medium
            )
        )

        Spacer(GlanceModifier.height(10.dp))

        if (!bluetoothEnabled) {
            Text(
                text = "Bluetooth is turned off",
                style = TextStyle().copy(
                    fontSize = 12.sp,
                )
            )
            Spacer(GlanceModifier.height(8.dp))
        }

        Row(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically,
            modifier = GlanceModifier.fillMaxWidth().padding(5.dp)
        ) {
            Button(
                text = if (isServiceBound) "Stop" else "Start",
                onClick = {
                    if (isServiceBound) {
                        stopBluetoothService()
                    } else {
                        if (bluetoothEnabled) {
                            startBluetoothService(selectedDeviceAddresses)
                        }
                    }
                },
                enabled = bluetoothEnabled
            )

            Spacer(GlanceModifier.width(10.dp))

            Button(
                text = "Share",
                onClick = {
                    scope.launch {
                        try {
                            val intent = Intent(context, ShareClipboardActivity::class.java).apply {
                                action = "ACTION_SHARE"
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                },
                enabled = isServiceBound && selectedDeviceAddresses.isNotEmpty() && bluetoothEnabled,
            )
        }

        Spacer(GlanceModifier.height(10.dp))

        when {
            !bluetoothEnabled -> {
                Text(
                    text = "Turn on Bluetooth to see paired devices",
                    style = TextStyle().copy(fontSize = 12.sp)
                )
            }

            pairedDevices.isEmpty() -> {
                Text(
                    text = "No paired devices found",
                    style = TextStyle().copy(fontSize = 12.sp)
                )
            }

            else -> {
                LazyColumn {
                    items(pairedDevices.toTypedArray()) { device ->
                        val name = if (ActivityCompat.checkSelfPermission(
                                context, Manifest.permission.BLUETOOTH_CONNECT
                            ) != PackageManager.PERMISSION_GRANTED
                        ) "Unknown Device" else device.name ?: "Unknown Device"
                        val address = device.address
                        val isSelected = selectedDeviceAddresses.contains(address)

                        DeviceItem(
                            onChecked = {
                                selectedDeviceAddresses =
                                    if (!isSelected) {
                                        selectedDeviceAddresses + address
                                    } else selectedDeviceAddresses - address
                            },
                            checked = isSelected,
                            name = name,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ErrorContent(message: String) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "ClipSync Widget Error",
            style = TextStyle().copy(
                fontSize = 16.sp,
                fontWeight = androidx.glance.text.FontWeight.Medium,
            )
        )

        Spacer(GlanceModifier.height(8.dp))

        Text(
            text = message,
            style = TextStyle().copy(
                fontSize = 14.sp,
            )
        )
    }
}