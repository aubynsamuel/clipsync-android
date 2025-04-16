package com.aubynsamuel.clipsync

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*

class ClipboardService : Service() {
    private val tag = "ClipboardService"
    private val channelId = "ClipSyncServiceChannel"
    private val notificationId = 1

    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private val viewModel = BluetoothViewModel(this)

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        viewModel.setContext(this)

        // Register clipboard listener
        ClipboardManager.registerClipboardListener(this) { clipboardText ->
            Log.d(tag, "Clipboard changed: $clipboardText")
            viewModel.sendClipboardData(clipboardText)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createForegroundNotification()
        startForeground(notificationId, notification)

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "ClipSync Service"
            val descriptionText = "Monitors clipboard for syncing between devices"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }

            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createForegroundNotification(): Notification {
        val pendingIntent = Intent(this, MainActivity::class.java).let { notificationIntent ->
            PendingIntent.getActivity(
                this, 0, notificationIntent,
                PendingIntent.FLAG_IMMUTABLE
            )
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("ClipSync")
            .setContentText("Monitoring clipboard for sync")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}