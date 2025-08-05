package com.aubynsamuel.clipsync.ui.navigation

import android.bluetooth.BluetoothDevice
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aubynsamuel.clipsync.ui.screen.BluetoothScannerScreen
import com.aubynsamuel.clipsync.ui.screen.MainScreen
import com.aubynsamuel.clipsync.ui.screen.SettingsScreen
import com.aubynsamuel.clipsync.ui.screen.SupportScreen
import com.aubynsamuel.clipsync.ui.viewModel.SettingsViewModel

@Composable
fun Navigation(
    startBluetoothService: (Set<String>) -> Unit,
    pairedDevices: Set<BluetoothDevice>,
    refreshPairedDevices: () -> Unit,
    stopBluetoothService: () -> Unit,
    settingsViewModel: SettingsViewModel,
    discoveredDevices: List<BluetoothDevice>,
    isScanning: Boolean,
    bluetoothEnabled: Boolean,
    onStartScan: () -> Unit,
    onPairDevice: (BluetoothDevice) -> Unit,
) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "MainScreen"
    ) {
        composable("MainScreen") {
            MainScreen(
                startBluetoothService = startBluetoothService,
                pairedDevices = pairedDevices,
                refreshPairedDevices = refreshPairedDevices,
                stopBluetoothService = stopBluetoothService,
                navController = navController,
                settingsViewModel = settingsViewModel
            )
        }

        composable("SettingsScreen") {
            SettingsScreen(
                navController = navController,
                settingsViewModel = settingsViewModel,
            )
        }

        composable("SupportScreen") {
            SupportScreen(
                navController = navController,
            )
        }

        composable("BluetoothScannerScreen") {
            BluetoothScannerScreen(
                navController = navController,
                discoveredDevices = discoveredDevices,
                isScanning = isScanning,
                bluetoothEnabled = bluetoothEnabled,
                onStartScan = onStartScan,
                onPairDevice = onPairDevice,
            )
        }
    }
}