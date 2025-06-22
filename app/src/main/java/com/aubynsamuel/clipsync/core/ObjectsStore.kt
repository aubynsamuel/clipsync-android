package com.aubynsamuel.clipsync.core

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.aubynsamuel.clipsync.bluetooth.BluetoothService

object Essentials {
    var bluetoothService: BluetoothService? = null
    var isServiceBound by mutableStateOf<Boolean?>(false)
    var addresses: Array<String>? = emptyArray()

    fun clean() {
        this.isServiceBound = null
        this.bluetoothService = null
        this.addresses = null
    }
}
