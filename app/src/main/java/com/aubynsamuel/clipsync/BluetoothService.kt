package com.aubynsamuel.clipsync

import android.app.*
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
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.IOException
import java.util.*

class BluetoothService : Service() {
    private val tag = "BluetoothService"
    private val channelId = "ClipSyncServiceChannel"
    private val notificationId = 1001
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var selectedDeviceAddresses = arrayOf<String>()
    private var serverSocket: BluetoothServerSocket? = null
    private var receiverThread: Thread? = null
    private val uuidInsecure = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66")

    // Binder given to clients
    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): BluetoothService = this@BluetoothService
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        startForeground(notificationId, createServiceNotification())
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

    override fun onDestroy() {
        stopBluetoothServer()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "ClipSync Service"
            val descriptionText = "Bluetooth clipboard sharing service"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }

            val notificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createServiceNotification(): Notification {
        val shareIntent = Intent(this, TransparentActivity::class.java).apply {
            action = "ACTION_SHARE"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        val sharePendingIntent = PendingIntent.getActivity(
            this,
            0,
            shareIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dismissIntent = Intent(this, NotificationReceiver::class.java).apply {
            action = "ACTION_DISMISS"
        }

        val dismissPendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("ClipSync Active")
            .setContentText("Ready to share clipboard")
            .setSmallIcon(R.drawable.ic_clipboard)
            .addAction(0, "Share", sharePendingIntent)
            .addAction(0, "Dismiss", dismissPendingIntent)
            .setOngoing(true)
            .setPriority(2)
            .build()
    }

    private fun startBluetoothServer() {
        serviceScope.launch {
            try {
                // Check for Bluetooth permissions
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (checkSelfPermission(android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        Log.e(tag, "Bluetooth connect permission not granted")
                        return@launch
                    }
                }

                serverSocket = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(
                    "ClipSync",
                    uuidInsecure
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
            val inputStream = socket.inputStream
            val buffer = ByteArray(1024)
            val bytes = inputStream.read(buffer)
            val message = String(buffer, 0, bytes)

            try {
                val json = JSONObject(message)
                val clipText = json.getString("clip")
//                val timestamp = json.getString("timestamp")

                showReceivedNotification(clipText)
            } catch (e: Exception) {
                Log.e(tag, "Error parsing JSON", e)
            }
            serviceScope.launch {
                delay(3000)
                inputStream.close()
                socket.close()
            }
        } catch (e: IOException) {
            Log.e(tag, "Error handling connection", e)
        }
    }

    private fun showReceivedNotification(text: String) {
        val copyIntent = Intent(this, NotificationReceiver::class.java).apply {
            action = "ACTION_COPY"
            putExtra("CLIP_TEXT", text)
        }

        val copyPendingIntent = PendingIntent.getBroadcast(
            this,
            1,
            copyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Clipboard text received")
            .setContentText(text.take(50) + if (text.length > 50) "..." else "")
            .setSmallIcon(R.drawable.ic_clipboard)
            .addAction(0, "Copy", copyPendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    fun shareClipboard(text: String) {
        serviceScope.launch {
            selectedDeviceAddresses.forEach { address ->
                val device = bluetoothAdapter.getRemoteDevice(address)
                sendToDevice(device, text)
            }
        }
    }

    private fun sendToDevice(device: BluetoothDevice, text: String) {
        Log.d(tag, "Data-Sent: $text")
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (checkSelfPermission(android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    Log.e(tag, "Bluetooth connect permission not granted")
                    return
                }
            }

            val socket = device.createInsecureRfcommSocketToServiceRecord(uuidInsecure)
            socket.connect()

            val outputStream = socket.outputStream
            val json = JSONObject().apply {
                put("clip", text)
                put("timestamp", System.currentTimeMillis().toString())
            }

            outputStream.write(json.toString().toByteArray())
            serviceScope.launch {
                delay(3000)
                outputStream.flush()
                outputStream.close()
                socket.close()
            }

        } catch (e: IOException) {
            Log.e(tag, "Error sending to device: ${device.address}", e)
        }
    }

    private fun stopBluetoothServer() {
        try {
            serverSocket?.close()
            receiverThread?.interrupt()
        } catch (e: IOException) {
            Log.e(tag, "Error closing server socket", e)
        }
    }
}