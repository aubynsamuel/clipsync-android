package com.aubynsamuel.clipsync.core

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.aubynsamuel.clipsync.bluetooth.BluetoothService

object Essentials {
    var serviceStarted by mutableStateOf(false)
    var isDarkMode by mutableStateOf(false)
    var autoCopy by mutableStateOf(false)

    @Volatile
    var addresses: Array<String> = emptyArray()

    var bluetoothService: BluetoothService? = null
}
