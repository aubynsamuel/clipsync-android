package com.aubynsamuel.clipsync

import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.UUID

class BluetoothService : Service() {
    private val tag = "BluetoothService"
    private val foregroundNotificationId = 1001
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var selectedDeviceAddresses = arrayOf<String>()
    private var serverSocket: BluetoothServerSocket? = null
    private var receiverThread: Thread? = null
    private val uuidInsecure = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66")

    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): BluetoothService = this@BluetoothService
    }

    override fun onCreate() {
        super.onCreate()
        Essentials.serviceStarted = true
        createNotificationChannel(this)

        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        startForeground(foregroundNotificationId, createServiceNotification(this))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.getStringArrayExtra("SELECTED_DEVICES")?.let {
            selectedDeviceAddresses = it
        }

        startBluetoothServer()

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    fun updateSelectedDevices() {
        selectedDeviceAddresses = Essentials.addresses
        createServiceNotification(this)
    }

    private fun startBluetoothServer() {
        serviceScope.launch {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (checkSelfPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
                        != PackageManager.PERMISSION_GRANTED
                    ) {
                        Log.e(tag, "Bluetooth connect permission not granted")
                        return@launch
                    }
                }

                serverSocket = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(
                    "ClipSync", uuidInsecure
                )

                receiverThread = Thread {
                    while (true) {
                        try {
                            val socket = serverSocket?.accept()
                            socket?.let { handleIncomingConnection(it) }
                        } catch (e: IOException) {
                            Log.e(tag, "Server socket accept failed", e)
                            break
                        }
                    }
                }
                receiverThread?.start()

            } catch (e: Exception) {
                Log.e(tag, "Failed to start server", e)
            }
        }
    }

    private fun handleIncomingConnection(socket: BluetoothSocket) {
        try {
            val reader = BufferedReader(InputStreamReader(socket.inputStream))

            val message = reader.readLine() ?: ""

            Log.d(tag, "Full JSON received (${message.length} chars)")

            try {
                val json = JSONObject(message)
                val clipText = json.getString("clip")
                showReceivedNotification(clipText, this)
            } catch (e: Exception) {
                Log.e(tag, "Error parsing JSON", e)
            } finally {
                reader.close()
                socket.close()
            }

        } catch (e: IOException) {
            Log.e(tag, "Error handling connection", e)
        }
    }

    suspend fun shareClipboard(text: String): SharingResult {
        if (selectedDeviceAddresses.isEmpty()) {
            return SharingResult.NO_SELECTED_DEVICES
        }
        var sendingResult: SharingResult = SharingResult.SUCCESS
        selectedDeviceAddresses.forEach { address ->
            val device = bluetoothAdapter.getRemoteDevice(address)
            sendingResult = sendToDevice(device, text)
        }

        return sendingResult
    }

    private suspend fun sendToDevice(device: BluetoothDevice, text: String): SharingResult {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (checkSelfPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    Log.e(tag, "Bluetooth connect permission not granted")
                    return SharingResult.PERMISSION_NOT_GRANTED
                }
            }

            val socket = device.createInsecureRfcommSocketToServiceRecord(uuidInsecure)
            socket.connect()

            val outputStream = socket.outputStream
            val json = JSONObject().apply {
                put("clip", text)
                put("timestamp", System.currentTimeMillis().toString())
            }

            outputStream.write((json.toString() + "\n").toByteArray())
            delay(3000)
            outputStream.flush()
            outputStream.close()
            socket.close()

            return SharingResult.SUCCESS
        } catch (e: IOException) {
            Log.e(tag, "Error sending to device: ${device.address}", e)
            return SharingResult.SENDING_ERROR
        }
    }

    private fun stopBluetoothServer() {
        try {
            serverSocket?.close()
            receiverThread?.interrupt()
            Essentials.serviceStarted = false
        } catch (e: IOException) {
            Log.e(tag, "Error closing server socket", e)
        }
    }

    override fun onDestroy() {
        stopBluetoothServer()
        super.onDestroy()
    }
}

enum class SharingResult {
    SENDING_ERROR,
    PERMISSION_NOT_GRANTED,
    NO_SELECTED_DEVICES,
    SUCCESS
}