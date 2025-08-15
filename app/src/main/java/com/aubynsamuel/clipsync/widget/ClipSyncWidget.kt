package com.aubynsamuel.clipsync.widget

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.BLUETOOTH_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.provideContent
import com.aubynsamuel.clipsync.bluetooth.BluetoothService
import com.aubynsamuel.clipsync.widget.ui.ErrorContent
import com.aubynsamuel.clipsync.widget.ui.WidgetContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ClipSyncWidget : GlanceAppWidget() {
    private var pairedDevices by mutableStateOf<Set<BluetoothDevice>>(emptySet())
    private var bluetoothEnabled by mutableStateOf(false)
    private var isBluetoothReceiverRegistered = false
    private lateinit var bluetoothAdapter: BluetoothAdapter

    private val bluetoothStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    val state = intent.getIntExtra(
                        BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR
                    )

                    val wasEnabled = bluetoothEnabled
                    bluetoothEnabled = when (state) {
                        BluetoothAdapter.STATE_ON -> true
                        BluetoothAdapter.STATE_OFF -> false
                        else -> bluetoothEnabled
                    }

                    if (bluetoothEnabled && !wasEnabled) {
                        context?.let { loadPairedDevices(it) }
                    } else if (!bluetoothEnabled) {
                        pairedDevices = emptySet()
                    }

                    context?.let { ctx ->
                        CoroutineScope(Dispatchers.Main).launch {
                            try {
                                val glanceAppWidgetManager = GlanceAppWidgetManager(ctx)
                                val glanceIds =
                                    glanceAppWidgetManager.getGlanceIds(ClipSyncWidget::class.java)
                                glanceIds.forEach { glanceId ->
                                    update(ctx, glanceId)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            }
        }
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        try {
            val bluetoothManager = context.getSystemService(BLUETOOTH_SERVICE) as? BluetoothManager
            if (bluetoothManager?.adapter == null) {
                provideContent {
                    MaterialTheme {
                        ErrorContent("Bluetooth not available")
                    }
                }
            }

            bluetoothAdapter = bluetoothManager.adapter
            bluetoothEnabled = bluetoothAdapter.isEnabled

            registerBluetoothReceiver(context)

            if (bluetoothEnabled) {
                loadPairedDevices(context)
            } else {
                pairedDevices = emptySet()
            }

            provideContent {
                MaterialTheme {
                    WidgetContent(
                        pairedDevices = pairedDevices,
                        stopBluetoothService = { stopBluetoothService(context) },
                        startBluetoothService = { devices ->
                            startBluetoothService(
                                devices,
                                context
                            )
                        },
                        bluetoothEnabled = bluetoothEnabled,
                        context = context
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            provideContent {
                MaterialTheme {
                    ErrorContent("Failed to load widget: ${e.message}")
                }
            }
        }
    }

    private fun registerBluetoothReceiver(context: Context) {
        if (!isBluetoothReceiverRegistered) {
            try {
                val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
                context.registerReceiver(bluetoothStateReceiver, filter)
                isBluetoothReceiverRegistered = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun unregisterBluetoothReceiver(context: Context) {
        if (isBluetoothReceiverRegistered) {
            try {
                context.unregisterReceiver(bluetoothStateReceiver)
                isBluetoothReceiverRegistered = false
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadPairedDevices(context: Context) {
        pairedDevices = try {
            if (ActivityCompat.checkSelfPermission(
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
        } catch (e: Exception) {
            e.printStackTrace()
            emptySet()
        }
    }

    private fun stopBluetoothService(context: Context) {
        try {
            val serviceIntent = Intent(context, BluetoothService::class.java)
            context.stopService(serviceIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startBluetoothService(selectedDeviceAddresses: Set<String>, context: Context) {
        try {
            if (bluetoothEnabled) {
                loadPairedDevices(context)
            }

            val serviceIntent = Intent(context, BluetoothService::class.java).apply {
                putExtra("SELECTED_DEVICES", selectedDeviceAddresses.toTypedArray())
                putExtra("AUTO_COPY_ENABLED", true)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun onDelete(context: Context, glanceId: GlanceId) {
        super.onDelete(context, glanceId)
        unregisterBluetoothReceiver(context)
    }
}