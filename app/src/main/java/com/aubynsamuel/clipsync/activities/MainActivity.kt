package com.aubynsamuel.clipsync.activities

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import com.aubynsamuel.clipsync.bluetooth.BluetoothService
import com.aubynsamuel.clipsync.core.Essentials
import com.aubynsamuel.clipsync.ui.screen.MainScreen
import com.aubynsamuel.clipsync.ui.theme.ClipSyncTheme

class MainActivity : ComponentActivity() {
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var pairedDevices by mutableStateOf<Set<BluetoothDevice>>(emptySet())

    private val requestEnableBluetooth = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            getLoadedDevicesList()
        }
        /** else {
        showToast("Bluetooth is required to find devices.", this)
        checkBluetoothEnabled()
        } */
    }

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            checkBluetoothEnabled()
        }
        /** else {
        showToast("Permissions needed to start sharing", this)
        checkPermissions()
        } */
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        setContent {
            com.aubynsamuel.clipsync.core.getTheme(this)
            ClipSyncTheme(darkTheme = Essentials.isDarkMode) {
                MainScreen(
                    startBluetoothService = { selectedDeviceAddresses ->
                        startBluetoothService(selectedDeviceAddresses)
                    },
                    pairedDevices = pairedDevices,
                    launchShareActivity = { context ->
                        val shareIntent =
                            Intent(context, ShareClipboardActivity::class.java).apply {
                                action = "ACTION_SHARE"
                            }
                        context.startActivity(shareIntent)
                    },
                    refresh = { getLoadedDevicesList() },
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
            getLoadedDevicesList()
        }
    }

    /** Wrapper around loadPairedDevices for MainScreen composable*/
    fun getLoadedDevicesList() {
        pairedDevices = loadPairedDevices()
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
    }
}