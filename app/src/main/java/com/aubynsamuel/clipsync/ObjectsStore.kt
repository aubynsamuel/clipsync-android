package com.aubynsamuel.clipsync

object SelectedDevicesStore {
    @Volatile
    var addresses: Array<String> = emptyArray()
}

object ServiceLocator {
    @Volatile
    var bluetoothService: BluetoothService? = null
}
