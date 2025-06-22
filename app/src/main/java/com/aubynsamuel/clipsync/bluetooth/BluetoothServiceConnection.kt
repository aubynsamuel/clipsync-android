package com.aubynsamuel.clipsync.bluetooth

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder

class BluetoothServiceConnection(
    private val onServiceConnected: (BluetoothService) -> Unit,
    private val onServiceDisconnected: () -> Unit,
) : ServiceConnection {

    override fun onServiceConnected(className: ComponentName, service: IBinder) {
        val binder = service as BluetoothService.LocalBinder
        val bluetoothService = binder.getService()
        onServiceConnected(bluetoothService)
    }

    override fun onServiceDisconnected(arg0: ComponentName) {
        onServiceDisconnected()
    }
}