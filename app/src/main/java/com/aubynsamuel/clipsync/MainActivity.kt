package com.aubynsamuel.clipsync

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.core.app.ActivityCompat
import com.aubynsamuel.clipsync.ui.MainScreen

class MainActivity : ComponentActivity() {
    private lateinit var bluetoothAdapter: BluetoothAdapter

    private val requestEnableBluetooth = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            // Bluetooth enabled, proceed
            // Devices list gets refreshed in the Compose view via loadPairedDevices lambda.
            loadPairedDevices() // Can be used to trigger other side–effects if needed.
        } else {
            // Handle Bluetooth not enabled case.
        }
    }

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            checkBluetoothEnabled()
        } else {
            // Handle permission denial here.
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        // Pass functions as lambdas to the composable.
        setContent {
            MaterialTheme {
                MainScreen(
                    startBluetoothService = { selectedDeviceAddresses ->
                        startBluetoothService(selectedDeviceAddresses)
                    },
                    loadPairedDevices = { loadPairedDevices() },
                    launchShareActivity = { context ->
                        val shareIntent = Intent(context, TransparentActivity::class.java).apply {
                            action = "ACTION_SHARE"
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(shareIntent)
                    }
                )
            }
        }
        checkPermissions()
    }

    private fun checkPermissions() {
        val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN
            )
        }

        val missingPermissions = requiredPermissions.filter {
            ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (missingPermissions.isNotEmpty()) {
            requestPermissionsLauncher.launch(missingPermissions)
        } else {
            checkBluetoothEnabled()
        }
    }

    private fun checkBluetoothEnabled() {
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            requestEnableBluetooth.launch(enableBtIntent)
        } else {
            loadPairedDevices()
        }
    }

    private fun loadPairedDevices(): Set<BluetoothDevice> {
        return if (ActivityCompat.checkSelfPermission(
                this,
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

    private fun startBluetoothService(selectedDeviceAddresses: Set<String>) {
        val serviceIntent = Intent(this, BluetoothService::class.java).apply {
            putExtra("SELECTED_DEVICES", selectedDeviceAddresses.toTypedArray())
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
        finish() // Optionally finish the activity if that's the desired flow.
    }
}
