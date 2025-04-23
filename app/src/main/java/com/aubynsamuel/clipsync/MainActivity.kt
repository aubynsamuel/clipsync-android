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
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.ActivityCompat
import com.aubynsamuel.clipsync.ui.MainScreen
import com.aubynsamuel.clipsync.ui.theme.ClipSyncTheme

class MainActivity : ComponentActivity() {
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var pairedDevices = mutableStateOf<Set<BluetoothDevice>>(emptySet())

    private val requestEnableBluetooth = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            loadPairedDevices()
        } else {
            showToast("Bluetooth is required to find devices.", this)
            checkBluetoothEnabled()
        }
    }

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            checkBluetoothEnabled()
        } else {
            showToast("Permissions needed to start sharing", this)
            checkPermissions()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        setContent {
            getTheme(this)
            ClipSyncTheme(darkTheme = DarkMode.isDarkMode.value) {
                MainScreen(
                    startBluetoothService = { selectedDeviceAddresses ->
                        startBluetoothService(selectedDeviceAddresses)
                    },
                    pairedDevices = pairedDevices.value,
                    launchShareActivity = { context ->
                        val shareIntent =
                            Intent(context, ShareClipboardActivity::class.java).apply {
                                action = "ACTION_SHARE"
                            }
                        context.startActivity(shareIntent)
                    },
                    refresh = { loadPairedDevices() },
                    stopBluetoothService = { stopBluetoothService() },
                )
            }
        }
        checkPermissions()
    }

    private fun stopBluetoothService() {
        val serviceIntent = Intent(this, BluetoothService::class.java)
        this.stopService(serviceIntent)
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
        if (ActivityCompat.checkSelfPermission(
                this,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    Manifest.permission.BLUETOOTH_CONNECT
                else
                    Manifest.permission.BLUETOOTH
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return emptySet()
        } else {
            pairedDevices.value = bluetoothAdapter.bondedDevices ?: emptySet()
            return bluetoothAdapter.bondedDevices ?: emptySet()
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
    }
}
