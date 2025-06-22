package com.aubynsamuel.clipsync.activities

import android.Manifest
import android.annotation.SuppressLint
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aubynsamuel.clipsync.bluetooth.BluetoothService
import com.aubynsamuel.clipsync.core.SettingsPreferences
import com.aubynsamuel.clipsync.core.showToast
import com.aubynsamuel.clipsync.ui.navigation.Navigation
import com.aubynsamuel.clipsync.ui.theme.ClipSyncTheme
import com.aubynsamuel.clipsync.ui.viewModel.SettingsViewModel

class MainActivity : ComponentActivity() {
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var autoCopyEnabled: Boolean = false
    private var pairedDevices by mutableStateOf<Set<BluetoothDevice>>(emptySet())
//    private var bluetoothService: BluetoothService? = null
//    private var isServiceBound by mutableStateOf(false)

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        setContent {
            val settingsPrefs = SettingsPreferences(this)
            val settingsViewModel = viewModel { SettingsViewModel(settingsPrefs) }
            autoCopyEnabled = settingsViewModel.autoCopy.collectAsStateWithLifecycle().value

            ClipSyncTheme(
                darkTheme =
                    settingsViewModel.isDarkMode.collectAsStateWithLifecycle().value
            ) {
                Navigation(
//                    isServiceBound = isServiceBound,
//                    bluetoothService = bluetoothService,
                    startBluetoothService = { selectedDeviceAddresses ->
                        startBluetoothService(selectedDeviceAddresses)
                    },
                    pairedDevices = pairedDevices,
                    refresh = { getLoadedDevicesList() },
                    stopBluetoothService = { stopBluetoothService() },
                    settingsViewModel = settingsViewModel
                )
            }
        }
        checkPermissions()
    }

    private val requestEnableBluetooth = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            getLoadedDevicesList()
        } else {
            showToast("Bluetooth is required to find devices.", this)
//        checkBluetoothEnabled()
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
//            checkPermissions()
        }
    }

    @SuppressLint("InlinedApi")
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

//    private val bluetoothServiceConnection =
//        BluetoothServiceConnection(
//            onServiceConnected = {
//                bluetoothService = it
//                isServiceBound = true
//            },
//            onServiceDisconnected = {
//                bluetoothService = null
//                isServiceBound = false
//            }
//        )

    private fun startBluetoothService(selectedDeviceAddresses: Set<String>) {
        checkPermissions()
        val serviceIntent = Intent(this, BluetoothService::class.java).apply {
            putExtra("SELECTED_DEVICES", selectedDeviceAddresses.toTypedArray())
            putExtra("AUTO_COPY_ENABLED", autoCopyEnabled)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
//        bindService(serviceIntent, bluetoothServiceConnection, BIND_AUTO_CREATE)
    }

//    private fun unbindBluetoothService() {
//        if (isServiceBound) {
//            unbindService(bluetoothServiceConnection)
//            isServiceBound = false
//        }
//        bluetoothService = null
//    }

    private fun stopBluetoothService() {
//        unbindBluetoothService()
        val serviceIntent = Intent(this, BluetoothService::class.java)
        stopService(serviceIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
//        unbindBluetoothService()
    }
}