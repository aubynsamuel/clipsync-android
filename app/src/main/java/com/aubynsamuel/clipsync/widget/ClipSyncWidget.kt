package com.aubynsamuel.clipsync.widget

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Context.BLUETOOTH_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import com.aubynsamuel.clipsync.bluetooth.BluetoothService

class ClipSyncWidget : GlanceAppWidget() {
    private var pairedDevices by mutableStateOf<Set<BluetoothDevice>>(emptySet())
    private lateinit var bluetoothAdapter: BluetoothAdapter

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val bluetoothManager = context.getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        loadPairedDevices(context)
        provideContent {
            MaterialTheme {
                WidgetContent(
                    pairedDevices = pairedDevices,
                    stopBluetoothService = { stopBluetoothService(context) },
                    startBluetoothService = { it -> startBluetoothService(it, context) },
//                    bluetoothEnabled = bluetoothAdapter.isEnabled, //TODO: Make reactive
                    context = context
                )
            }
        }
    }

    private fun loadPairedDevices(context: Context) {
        pairedDevices = if (ActivityCompat.checkSelfPermission(
                context,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    Manifest.permission.BLUETOOTH_CONNECT
                else
                    Manifest.permission.BLUETOOTH
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            emptySet()
        } else {
            bluetoothAdapter.bondedDevices ?: emptySet()
        }
    }

    private fun stopBluetoothService(context: Context) {
        val serviceIntent = Intent(context, BluetoothService::class.java)
        context.stopService(serviceIntent)
    }

    private fun startBluetoothService(selectedDeviceAddresses: Set<String>, context: Context) {
        loadPairedDevices(context)
        val serviceIntent = Intent(context, BluetoothService::class.java).apply {
            putExtra("SELECTED_DEVICES", selectedDeviceAddresses.toTypedArray())
            putExtra("AUTO_COPY_ENABLED", true)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }
}
