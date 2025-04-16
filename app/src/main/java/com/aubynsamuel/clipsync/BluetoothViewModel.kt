package com.aubynsamuel.clipsync

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.*

class BluetoothViewModel(private var context: Context) : ViewModel() {
    private val tag = "BluetoothViewModel"
    private val appName = R.string.app_name.toString()
    private val appUuid = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66")

    private var bluetoothAdapter: BluetoothAdapter? = null

    // Connection State
    var connectionStatus by mutableStateOf("Not Connected")
        private set
    var isScanning by mutableStateOf(false)
        private set
    var lastSyncTime by mutableStateOf("")
        private set
    var lastClipboardText by mutableStateOf("")
        private set

    // Device Lists
    val pairedDevices = mutableStateListOf<BluetoothDevice>()
    val discoveredDevices = mutableStateListOf<BluetoothDevice>()
    var connectedDevice by mutableStateOf<BluetoothDevice?>(null)
        private set

    // Connection threads
    private var connectThread: ConnectThread? = null
    private var acceptThread: AcceptThread? = null
    private var connectedThread: ConnectedThread? = null

    private val deviceReceiver = object : BroadcastReceiver() {
        @RequiresApi(Build.VERSION_CODES.S)
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            intent.getParcelableExtra(
                                BluetoothDevice.EXTRA_DEVICE,
                                BluetoothDevice::class.java
                            )
                        } else {
                            @Suppress("DEPRECATION")
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                        }

                    device?.let {
                        if (it !in discoveredDevices && it !in pairedDevices) {
                            if (ActivityCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.BLUETOOTH_CONNECT
                                ) != PackageManager.PERMISSION_GRANTED
                            ) {
                                ActivityResultContracts.RequestPermission().createIntent(
                                    context,
                                    Manifest.permission.BLUETOOTH_CONNECT
                                )
                                return
                            }
                            Log.d(tag, "Device found: ${it.name ?: "Unknown"} - ${it.address}")
                            discoveredDevices.add(it)
                        }
                    }
                }

                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    isScanning = true
                    discoveredDevices.clear()
                }

                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    isScanning = false
                }

                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    loadPairedDevices()
                }
            }
        }
    }

    fun initialize(adapter: BluetoothAdapter) {
        this.bluetoothAdapter = adapter
        loadPairedDevices()
    }

    fun setContext(context: Context) {
        this.context = context

        // Register for broadcasts
        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        }
        context.registerReceiver(deviceReceiver, filter)

        // Start accepting connections
        startAcceptThread()
    }

    private fun loadPairedDevices() {
        pairedDevices.clear()

        bluetoothAdapter?.let { adapter ->
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }

            adapter.bondedDevices?.forEach { device ->
                pairedDevices.add(device)
            }
        }
    }

    fun startDiscovery() {
        bluetoothAdapter?.let { adapter ->
            context.let { ctx ->
                if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.BLUETOOTH_SCAN)
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    connectionStatus = "Error: Missing BLUETOOTH_SCAN permission"
                    return
                }

                if (adapter.isDiscovering) {
                    adapter.cancelDiscovery()
                }

                adapter.startDiscovery()
                connectionStatus = "Scanning for devices..."
            }
        }
    }

    fun toggleVisibility() {
        bluetoothAdapter?.let { adapter ->
            context.let { ctx ->
                if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.BLUETOOTH_ADVERTISE)
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    connectionStatus = "Error: Missing BLUETOOTH_ADVERTISE permission"
                    return
                }

                val discoverableIntent =
                    Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
                        putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
                    }
                discoverableIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                ctx.startActivity(discoverableIntent)
                connectionStatus = "Making device discoverable..."
            }
        }
    }

    fun connectToDevice(device: BluetoothDevice) {
        bluetoothAdapter?.let { adapter ->
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED
            ) {
                connectionStatus = "Error: Missing BLUETOOTH_CONNECT permission"
                return
            }

            // Cancel discovery as it's resource intensive
            adapter.cancelDiscovery()

            // Stop any previous connection threads
            connectedThread?.cancel()
            connectThread?.cancel()

            // Start new connection thread
            connectionStatus = "Connecting to ${device.name ?: "Unknown Device"}..."
            connectThread = ConnectThread(device)
            connectThread?.start()
        }
    }

    private fun startAcceptThread() {
        acceptThread = AcceptThread()
        acceptThread?.start()
    }

    private fun manageConnectedSocket(socket: BluetoothSocket) {
        val device = socket.remoteDevice
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        connectedDevice = device
        connectionStatus = "Connected to ${device.name ?: "Unknown Device"}"

        // Cancel any previous connected thread
        connectedThread?.cancel()

        // Start new connected thread
        connectedThread = ConnectedThread(socket)
        connectedThread?.start()
    }

    fun sendClipboardData(text: String) {
        connectedThread?.write(text.toByteArray())
        lastClipboardText = text
        lastSyncTime = System.currentTimeMillis().toString()
    }

    override fun onCleared() {
        super.onCleared()

        context.unregisterReceiver(deviceReceiver)

        // Clean up all threads
        connectThread?.cancel()
        acceptThread?.cancel()
        connectedThread?.cancel()
    }

    private inner class AcceptThread : Thread() {
        private val serverSocket: BluetoothServerSocket?

        init {
            val tmp: BluetoothServerSocket? = try {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_CONNECT
                    )
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    null
                } else {
                    bluetoothAdapter?.listenUsingRfcommWithServiceRecord(appName, appUuid)
                }
            } catch (e: IOException) {
                Log.e(tag, "Socket's listen() method failed", e)
                null
            }

            serverSocket = tmp
        }

        override fun run() {
            var socket: BluetoothSocket?

            while (true) {
                socket = try {
                    serverSocket?.accept()
                } catch (e: IOException) {
                    Log.e(tag, "Socket's accept() method failed", e)
                    break
                }

                socket?.let {
                    // Connection accepted
                    viewModelScope.launch(Dispatchers.Main) {
                        manageConnectedSocket(it)
                    }
                    serverSocket?.close()
//                    break
                }
            }
        }

        fun cancel() {
            try {
                serverSocket?.close()
            } catch (e: IOException) {
                Log.e(tag, "Could not close the server socket", e)
            }
        }
    }

    private inner class ConnectThread(device: BluetoothDevice) : Thread() {
        private val socket: BluetoothSocket?

        init {
            var tmp: BluetoothSocket? = null

            try {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_CONNECT
                    )
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    throw SecurityException("Missing BLUETOOTH_CONNECT permission")
                }

                tmp = device.createRfcommSocketToServiceRecord(appUuid)
            } catch (e: IOException) {
                Log.e(tag, "Socket's create() method failed", e)
            }

            socket = tmp
        }

        override fun run() {
            // Cancel discovery as it's resource intensive
            try {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN)
                    == PackageManager.PERMISSION_GRANTED
                ) {
                    bluetoothAdapter?.cancelDiscovery()
                }
            } catch (e: Exception) {
                Log.e(tag, "Failed to cancel discovery", e)
            }

            try {
                socket?.connect()

                // Connection successful
                viewModelScope.launch(Dispatchers.Main) {
                    socket?.let { manageConnectedSocket(it) }
                }
            } catch (connectException: IOException) {
                Log.e(tag, connectException.message.toString())
                try {
                    socket?.close()

                    viewModelScope.launch(Dispatchers.Main) {
                        connectionStatus = "Failed to connect. Try again."
                    }
                } catch (closeException: IOException) {
                    Log.e(tag, "Could not close the client socket", closeException)
                }
                return
            }
        }

        fun cancel() {
            try {
                socket?.close()
            } catch (e: IOException) {
                Log.e(tag, "Could not close the client socket", e)
            }
        }
    }

    private inner class ConnectedThread(private val socket: BluetoothSocket) : Thread() {
        private val inputStream = socket.inputStream
        private val outputStream = socket.outputStream
        private val buffer = ByteArray(1024)

        override fun run() {
            var numBytes: Int

            while (true) {
                numBytes = try {
                    inputStream.read(buffer)
                } catch (e: IOException) {
                    Log.e(tag, "Input stream was disconnected", e)
                    viewModelScope.launch(Dispatchers.Main) {
                        connectionStatus = "Disconnected"
                        connectedDevice = null
                    }
                    break
                }

                val receivedText = String(buffer, 0, numBytes)

                viewModelScope.launch(Dispatchers.Main) {
                    // Update clipboard with received data
                    context.let { ctx ->
                        ClipboardManager.setClipboard(ctx, receivedText)
                        lastClipboardText = receivedText
                        lastSyncTime = System.currentTimeMillis().toString()
                    }
                }
            }
        }

        fun write(bytes: ByteArray) {
            try {
                outputStream.write(bytes)
            } catch (e: IOException) {
                Log.e(tag, "Error occurred when sending data", e)
                viewModelScope.launch(Dispatchers.Main) {
                    connectionStatus = "Error during data transfer"
                }
                return
            }
        }

        fun cancel() {
            try {
                socket.close()
            } catch (e: IOException) {
                Log.e(tag, "Could not close the connect socket", e)
            }
        }
    }
}