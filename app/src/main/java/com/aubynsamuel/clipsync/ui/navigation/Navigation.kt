package com.aubynsamuel.clipsync.ui.navigation

import android.bluetooth.BluetoothDevice
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aubynsamuel.clipsync.ui.screen.MainScreen
import com.aubynsamuel.clipsync.ui.screen.SettingsScreen

@Composable
fun Navigation(
    startBluetoothService: (Set<String>) -> Unit,
    pairedDevices: Set<BluetoothDevice>,
    refresh: () -> Unit,
    stopBluetoothService: () -> Unit,
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
                refresh = refresh,
                stopBluetoothService = stopBluetoothService,
                navController = navController
            )
        }

        composable("SettingsScreen") {
            SettingsScreen(navController = navController)
        }
    }
}