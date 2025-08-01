package com.aubynsamuel.clipsync.bluetooth

import android.Manifest
import android.app.NotificationManager
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
import com.aubynsamuel.clipsync.core.Essentials
import com.aubynsamuel.clipsync.core.copyToClipboard
import com.aubynsamuel.clipsync.notification.createNotificationChannel
import com.aubynsamuel.clipsync.notification.createServiceNotification
import com.aubynsamuel.clipsync.notification.showReceivedNotification
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
    companion object {
        const val FOREGROUND_NOTIFICATION_ID = 1001
        private const val TAG = "BluetoothService"
        private val BLE_UUID = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66")
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var selectedDeviceAddresses = arrayOf<String>()
    private var autoCopyEnabled = true
    private var serverSocket: BluetoothServerSocket? = null
    private var receiverThread: Thread? = null
    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): BluetoothService = this@BluetoothService
    }

    private fun cancelErrorNotification() {
        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(1000)
    }

    override fun onCreate() {
        super.onCreate()
        serviceScope.launch {
            Essentials.setBluetoothService(this@BluetoothService)
        }
        createNotificationChannel(this)

        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        startForeground(FOREGROUND_NOTIFICATION_ID, createServiceNotification(this))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.getStringArrayExtra("SELECTED_DEVICES")?.let {
            selectedDeviceAddresses = it
        }
        intent?.getBooleanExtra("AUTO_COPY_ENABLED", true)?.let {
            autoCopyEnabled = it
        }

        startBluetoothServer()

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    fun updateSelectedDevices(selectedDevices: Array<String>) {
        selectedDeviceAddresses = selectedDevices
    }

    fun toggleAutoCopy(value: Boolean) {
        autoCopyEnabled = value
    }

    private fun startBluetoothServer() {
        serviceScope.launch {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)
                        != PackageManager.PERMISSION_GRANTED
                    ) {
                        Log.e(TAG, "Bluetooth connect permission not granted")
                        return@launch
                    }
                }

                serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(
                    "ClipSync", BLE_UUID
                )

                receiverThread = Thread {
                    while (true) {
                        try {
                            val socket = serverSocket?.accept()
                            socket?.let { handleIncomingConnection(it) }
                        } catch (e: IOException) {
                            Log.e(TAG, "Server socket accept failed", e)
                            break
                        }
                    }
                }
                receiverThread?.start()

            } catch (e: Exception) {
                Log.e(TAG, "Failed to start server", e)
            }
        }
    }

    private fun handleIncomingConnection(socket: BluetoothSocket) {
        try {
            val reader = BufferedReader(InputStreamReader(socket.inputStream))
            val message = reader.readLine() ?: ""

            Log.d(TAG, "Full JSON received (${message.length} chars)")

            try {
                val json = JSONObject(message)
                val clipText = json.getString("clip")
                if (autoCopyEnabled) copyToClipboard(clipText, this)
                else showReceivedNotification(clipText, this)
                cancelErrorNotification()
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing JSON", e)
            } finally {
                reader.close()
                socket.close()
            }

        } catch (e: IOException) {
            Log.e(TAG, "Error handling connection", e)
        }
    }

    suspend fun shareClipboard(text: String): SharingResult {
        cancelErrorNotification()
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
                if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    Log.e(TAG, "Bluetooth connect permission not granted")
                    return SharingResult.PERMISSION_NOT_GRANTED
                }
            }

            val socket = device.createRfcommSocketToServiceRecord(BLE_UUID)
            socket.connect()

            val outputStream = socket.outputStream
            val json = JSONObject().apply {
                put("clip", text)
                put("timestamp", System.currentTimeMillis().toString())
            }

            outputStream.write((json.toString() + "\n").toByteArray())
            delay(1000)
            outputStream.flush()
            outputStream.close()
            socket.close()

            return SharingResult.SUCCESS
        } catch (e: IOException) {
            Log.e(TAG, "Error sending to device: ${device.address}", e)
            return SharingResult.SENDING_ERROR
        }
    }

    private fun stopBluetoothServer() {
        try {
            serverSocket?.close()
            receiverThread?.interrupt()
            serviceScope.launch {
                Essentials.clean()
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error closing server socket", e)
        }
    }

    override fun onDestroy() {
        stopBluetoothServer()
        super.onDestroy()
    }
}