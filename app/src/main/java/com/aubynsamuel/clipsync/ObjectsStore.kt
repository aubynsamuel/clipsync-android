package com.aubynsamuel.clipsync

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

object SelectedDevicesStore {
    @Volatile
    var addresses: Array<String> = emptyArray()
}

object DarkMode {
    var isDarkMode = mutableStateOf<Boolean>(false)
}

object ServiceLocator {
    @Volatile
    var bluetoothService: BluetoothService? = null

    @Volatile
    var serviceStarted: MutableState<Boolean> = mutableStateOf(false)
}
