package com.aubynsamuel.clipsync.notification

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.aubynsamuel.clipsync.bluetooth.BluetoothService
import com.aubynsamuel.clipsync.core.copyToClipboard

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            "ACTION_DISMISS" -> {
                val serviceIntent = Intent(context, BluetoothService::class.java)
                context.stopService(serviceIntent)

                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(BluetoothService.FOREGROUND_NOTIFICATION_ID)
            }

            "ACTION_COPY" -> {
                val clipText = intent.getStringExtra("CLIP_TEXT") ?: return
                val notificationId = intent.getIntExtra("NOTIFICATION_ID", 0)

                copyToClipboard(clipText, context)

                // Dismiss the notification
                if (notificationId != 0) {
                    val notificationManager =
                        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.cancel(notificationId)
                }
            }
        }
    }
}